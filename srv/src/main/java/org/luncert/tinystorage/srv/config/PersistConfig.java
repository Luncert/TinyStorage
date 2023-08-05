package org.luncert.tinystorage.srv.config;

import org.luncert.tinystorage.srv.model.LineRecord;
import org.luncert.tinystorage.storemodule.TinyStorage;
import org.luncert.tinystorage.storemodule.TinyStorageFactory;
import org.luncert.tinystorage.storemodule.TsConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class PersistConfig {

  @Bean
  public TinyStorage tinyStorage(ThreadPoolTaskExecutor taskExecutor) {
    return TinyStorageFactory.createTinyStorage(
        TsConfig.DEFAULT.config()
            .maxStoreSize("3GB")
            .maxFileSize("256KB")
            .reader(LineRecord.READER)
            .writer(LineRecord.WRITER)
            .taskExecutor(taskExecutor::submit)
            .build());
  }
}
