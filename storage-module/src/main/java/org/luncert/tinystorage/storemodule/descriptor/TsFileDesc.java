package org.luncert.tinystorage.storemodule.descriptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TsFileDesc extends Descriptor {

  private String id;

  private boolean readOnly;

  private long startAt;

  private long endAt;
}
