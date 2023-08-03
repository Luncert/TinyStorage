package org.luncert.tinystorage.storemodule;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TsFileHeader {

  // readOnly + startAt + endAt + writePosition
  static final int HEADER_SIZE = 1 + 8 + 8 + 4;

  private boolean readOnly;

  // first record's timestamp for convenient query.
  private long startAt;

  private long endAt;
}
