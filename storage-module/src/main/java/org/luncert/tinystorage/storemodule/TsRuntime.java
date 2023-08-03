package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.descriptor.DescribedObject;
import org.luncert.tinystorage.storemodule.descriptor.TsRuntimeDesc;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class TsRuntime implements DescribedObject<TsRuntimeDesc> {

  private String dataStorePath;

  private long maxStoreSize;

  private int maxFileSize;

  private TsReader<? extends Record> reader;

  private TsWriter<? extends Record> writer;

  private TaskExecutor taskExecutor;

  @Setter
  private SpaceManager spaceManager;

  @Override
  public TsRuntimeDesc getDescriptor() {
    return TsRuntimeDesc.builder()
        .dataStorePath(dataStorePath)
        .maxStoreSize(maxStoreSize)
        .maxFileSize(maxFileSize)
        .spaceManager(spaceManager.getDescriptor())
        .build();
  }
}
