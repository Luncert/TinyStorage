package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.common.Utils;
import org.luncert.tinystorage.storemodule.descriptor.TsDesc;
import org.luncert.tinystorage.storemodule.util.ReflectUtils;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;


@Slf4j
class TinyStorageImpl implements TinyStorage {

  private final TsRuntime runtime;

  private final TsCache tsCache;

  public TinyStorageImpl(TsRuntime runtime) {
    this.runtime = runtime;
    this.tsCache = new TsCache(runtime);
  }

  @Override
  public void append(String bucketId, Record record) {
    tsCache.getBucket(bucketId).append(record);
  }

  @Override
  @SuppressWarnings("unchecked")
  public void subscribe(@Nonnull String bucketId,
                        @Nonnull TimeRange timeRange,
                        @Nonnull Subscriber<? extends Record> consumer) {
    Objects.requireNonNull(timeRange);
    Objects.requireNonNull(consumer);

    TsBucket bucket = tsCache.getBucket(bucketId);

    AtomicBoolean cancelSignal = new AtomicBoolean();
    consumer.onSubscribe(new Subscription() {
      @Override
      public void request(long l) {
        // ignored
      }

      @Override
      public void cancel() {
        cancelSignal.set(true);
      }
    });

    runtime.getTaskExecutor().submit(() -> {
      try {
        Subscriber<Record> consumerTypeWrapper = (Subscriber<Record>) consumer;

        for (Record record : bucket.createReader(timeRange)) {
          if (timeRange.accept(record.getTimestamp())) {
            consumerTypeWrapper.onNext(record);
          }
          if (cancelSignal.get()) {
            break;
          }
        }

        consumer.onComplete();
      } catch (Exception ex) {
        log.error("exception on loading records", ex);
        consumer.onError(ex);
      }
    });
  }

  @Override
  public void readStorePhysicalFile(String fileName, Subscriber<String> consumer) {
    AtomicBoolean cancelSignal = new AtomicBoolean();
    consumer.onSubscribe(new Subscription() {
      @Override
      public void request(long l) {
      }

      @Override
      public void cancel() {
        cancelSignal.set(true);
      }
    });

    runtime.getTaskExecutor().submit(() -> {
      String filePath = Paths.get(runtime.getDataStorePath(), fileName).toString();
      try (FileInputStream in = new FileInputStream(filePath)) {
        readPhysicalFile(new PhysicalFileOperator(in), consumer, cancelSignal);
        consumer.onComplete();
      } catch (EOFException e) {
        consumer.onComplete();
      } catch (IOException | IllegalAccessException | InvocationTargetException e) {
        consumer.onError(e);
      }
    });
  }

  private void readPhysicalFile(ReadOperator operator, Subscriber<String> consumer, AtomicBoolean cancelSignal)
      throws EOFException, InvocationTargetException, IllegalAccessException {
    // read file header

    boolean readonly = (operator.readByte() & 0x1) == 1;
    if (!readonly) {
      consumer.onNext("cannot access non-readonly file");
      return;
    }

    long startAt = Utils.byteArrayToLong(i -> operator.readByte(), 8);
    long endAt = Utils.byteArrayToLong(i -> operator.readByte(), 8);
    int bufferPosition = Math.max(Utils.byteArrayToInt(i -> operator.readByte(), 4), TsFileHeader.HEADER_SIZE);

    consumer.onNext("[header]\n"
        + "isReadonly: " + true + "\n"
        + "startAt: " + startAt + "\n"
        + "endAt: " + endAt + "\n"
        + "bufferPosition: " + bufferPosition + "\n"
        + "\n[Records]\n");

    // read logs

    Record record = runtime.getReader().read(operator);
    if (record == null) {
      return;
    }

    Map<String, Method> getterMap = Arrays.stream(
        ReflectUtils.getPropertyMethods(ReflectUtils.getBeanGetters(record.getClass()), true, false))
        .collect(Collectors.toMap(m -> decapitalize(m.getName().substring(3)), m -> m));

    do {
      StringBuilder builder = new StringBuilder(record.getClass().getSimpleName()).append("{");
      for (Map.Entry<String, Method> entry : getterMap.entrySet()) {
        builder.append(entry.getKey()).append(":").append(entry.getValue().invoke(record)).append(",");
      }

      consumer.onNext(builder.substring(0, builder.length() - 1) + "}\n");

      if (cancelSignal.get()) {
        break;
      }

      record = runtime.getReader().read(operator);
    } while (record != null);
  }

  private static String decapitalize(String string) {
    if (string == null || string.length() == 0) {
      return string;
    }

    char[] c = string.toCharArray();
    c[0] = Character.toLowerCase(c[0]);

    return new String(c);
  }

  @Override
  public TsDesc getDescriptor() {
    return TsDesc.builder()
        .runtime(runtime.getDescriptor())
        .cache(tsCache.getDescriptor())
        .build();
  }
}
