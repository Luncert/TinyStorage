package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.descriptor.TsBucketDesc;
import org.luncert.tinystorage.storemodule.descriptor.TsFileDesc;
import org.luncert.tinystorage.storemodule.descriptor.TsDesc;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(JUnit4.class)
public class TestTinyStorage {

  private final TsConfig config = TsConfig.DEFAULT.config()
      .dataStorePath("./target/test-data/")
      .maxStoreSize("1MB")
      .maxFileSize("1KB") // 1kb
      .reader(RecordImpl.READER)
      .writer(RecordImpl.WRITER)
      .build();

  @Test
  public void testSimpleIO() {
    TinyStorage tinyStorage = TinyStorageFactory.createTinyStorage(config);

    List<RecordImpl> data = RecordImpl.genList(16);

    for (int i = 0; i < 16; i++) {
      tinyStorage.append("0", data.get(i));
    }

    AtomicInteger count = new AtomicInteger();
    tinyStorage.subscribe("0", TimeRange.UNSET, new Subscriber<>() {
      @Override
      public void onSubscribe(Subscription subscription) {
      }

      @Override
      public void onNext(Record record) {
        Assert.assertEquals(data.get(count.getAndIncrement()), record);
      }

      @Override
      public void onError(Throwable throwable) {
        Assert.fail();
      }

      @Override
      public void onComplete() {
      }
    });

    Assert.assertEquals(16, count.get());
  }

  @Test
  public void testReadPhysicalFile() {
    TinyStorage tinyStorage = TinyStorageFactory.createTinyStorage(config);

    List<RecordImpl> data = RecordImpl.genList(32);

    for (int i = 0; i < 32; i++) {
      tinyStorage.append("0", data.get(i));
    }

    TsDesc descriptor = tinyStorage.getDescriptor();
    for (Map.Entry<String, TsBucketDesc> entry : descriptor.getCache().getBuckets().entrySet()) {
      for (TsFileDesc tsFileDesc : entry.getValue().getFiles()) {
        tinyStorage.readStorePhysicalFile(tsFileDesc.getId(), new Subscriber<>() {
          @Override
          public void onSubscribe(Subscription subscription) {
          }

          @Override
          public void onNext(String s) {
            System.out.print(s);
          }

          @Override
          public void onError(Throwable throwable) {
            throwable.printStackTrace();
            Assert.fail();
          }

          @Override
          public void onComplete() {
          }
        });
      }
    }

  }
}
