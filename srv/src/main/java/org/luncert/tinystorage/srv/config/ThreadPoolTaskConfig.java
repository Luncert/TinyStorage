package org.luncert.tinystorage.srv.config;

import java.util.concurrent.ThreadPoolExecutor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class ThreadPoolTaskConfig {
  
  private static final int CORE_POOL_SIZE = 20;

  private static final int MAX_POOL_SIZE = 100;

  private static final int KEEP_ALIVE_TIME = 10;

  private static final int QUEUE_CAPACITY = 200;

  private static final String THREAD_NAME_PREFIX = "async-";
  
  @Bean
  public ThreadPoolTaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(CORE_POOL_SIZE);
    executor.setMaxPoolSize(MAX_POOL_SIZE);
    executor.setQueueCapacity(QUEUE_CAPACITY);
    executor.setKeepAliveSeconds(KEEP_ALIVE_TIME);
    executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
  }
}
