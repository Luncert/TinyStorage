package org.luncert.tinystorage.storemodule.descriptor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TsRuntimeDesc extends Descriptor {

  private String dataStorePath;

  private long maxStoreSize;

  private int maxFileSize;

  private SpaceManagerDesc spaceManager;
}
