package org.luncert.tinystorage.storemodule;

import java.nio.file.Paths;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TsConfig {

  public static final TsConfig DEFAULT;

  static {
    String userDir = System.getProperty("user.dir");
    DEFAULT = TsConfig.builder()
        .dataStorePath(Paths.get(userDir, "TinyStorage").toString())
        .maxStoreSize("2G")
        .maxFileSize("64M")
        .reader(null)
        .writer(null)
        .taskExecutor(Runnable::run)
        .build();
  }

  private String dataStorePath;

  private String maxStoreSize;

  private String maxFileSize;

  private TsReader<? extends Record> reader;

  private TsWriter<? extends Record> writer;

  private TaskExecutor taskExecutor;

  public TsConfigBuilder config() {
    return TsConfig.builder()
        .dataStorePath(dataStorePath)
        .maxStoreSize(maxStoreSize)
        .maxFileSize(maxFileSize)
        .reader(reader)
        .writer(writer)
        .taskExecutor(taskExecutor);
  }
}
