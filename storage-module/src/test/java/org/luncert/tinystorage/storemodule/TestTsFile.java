package org.luncert.tinystorage.storemodule;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@RunWith(JUnit4.class)
public class TestTsFile {

  private final ExecutorService executorService = Executors.newFixedThreadPool(4);

  private final TsRuntime runtime;

  public TestTsFile() throws IOException {
    runtime = TsRuntime.builder()
        .dataStorePath("./target/test-data/")
        .maxFileSize(1024) // 1kb
        .reader(RecordImpl.READER)
        .writer(RecordImpl.WRITER)
        .build();
    FileUtils.forceMkdir(Paths.get(runtime.getDataStorePath()).toFile());
  }

  @Test
  public void testSimpleIO() throws IOException {
    TsFile file = new TsFile(UUID.randomUUID().toString(), runtime);

    List<RecordImpl> testData = RecordImpl.genList(16);

    for (RecordImpl log : testData) {
      Assert.assertEquals(12 + log.getPayload().length(), file.append(log));
    }

    int i = 0;
    for (Record record : file.createReader(false)) {
      Assert.assertEquals(testData.get(i), record);
      i++;
    }

    Assert.assertTrue(file.close());
  }

  @Test
  public void testConcurrentIO() throws IOException {
    TsFile file = new TsFile(UUID.randomUUID().toString(), runtime);

    List<RecordImpl> testData = RecordImpl.genList(16);

    executorService.submit(() -> {
      for (RecordImpl log : testData) {
        try {
          Assert.assertEquals(12 + log.getPayload().length(), file.append(log));
          Thread.sleep(10);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });

    int i = 0;
    for (Record record : file.createReader(true)) {
      Assert.assertEquals(testData.get(i), record);
      i++;
    }

    Assert.assertTrue(file.close());
  }

  @Test
  public void testMultipleReaders() throws IOException, InterruptedException {
    TsFile file = new TsFile(UUID.randomUUID().toString(), runtime);

    List<RecordImpl> testData = RecordImpl.genList(16);

    executorService.submit(() -> {
      for (RecordImpl log : testData) {
        try {
          Assert.assertEquals(12 + log.getPayload().length(), file.append(log));
          Thread.sleep(10);
        } catch (IOException | InterruptedException e) {
          throw new RuntimeException(e);
        }
      }
    });

    executorService.submit(() -> {
      try {
        int i = 0;
        for (Record record : file.createReader(true)) {
          Assert.assertEquals(testData.get(i), record);
          i++;
        }
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    });

    int i = 0;
    for (Record record : file.createReader(true)) {
      Assert.assertEquals(testData.get(i), record);
      i++;
    }

    Thread.sleep(500);
    Assert.assertTrue(file.close());
  }
}
