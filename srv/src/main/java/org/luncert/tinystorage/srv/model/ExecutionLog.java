package org.luncert.tinystorage.srv.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.luncert.tinystorage.storemodule.Record;
import org.luncert.tinystorage.storemodule.TsReader;
import org.luncert.tinystorage.storemodule.TsWriter;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutionLog implements Record {

  private long timestamp;
  protected String source;

  public static final TsReader<ExecutionLog> READER = buffer ->
      // timestamp + type + payloadLength + payload
      ExecutionLog.builder()
          .timestamp(buffer.readLong())
          .source(buffer.readString())
          .build();

  public static final TsWriter<ExecutionLog> WRITER = (r, buffer) -> {
    buffer.appendLong(r.getTimestamp());
    buffer.appendString(r.getSource());
  };
}
