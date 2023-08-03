package org.luncert.tinystorage.storemodule;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.ArrayList;
import java.util.List;

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
public class RecordImpl implements Record {

  private final long timestamp;

  @Getter
  private final String payload;

  @Override
  public long getTimestamp() {
    return timestamp;
  }

  public static RecordImpl random(int payloadSize) {
    return new RecordImpl(System.currentTimeMillis(), RandomStringUtils.randomAlphabetic(payloadSize));
  }

  public static RecordImpl random(int payloadLower, int payloadUpper) {
    return new RecordImpl(System.currentTimeMillis(), RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(payloadLower, payloadUpper)));
  }


  public static List<RecordImpl> genList(int size) {
    List<RecordImpl> data = new ArrayList<>(size);
    for (int i = 0; i < size; i++) {
      data.add(RecordImpl.random(32, 52));
    }
    return data;
  }

  public static final TsWriter<RecordImpl> WRITER = (log, buffer) -> {
    buffer.appendLong(log.getTimestamp());
    buffer.appendString(log.payload);
  };

  public static final TsReader<RecordImpl> READER = buffer ->
      new RecordImpl(buffer.readLong(), buffer.readString());
}
