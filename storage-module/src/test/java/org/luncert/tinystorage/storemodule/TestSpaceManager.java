package org.luncert.tinystorage.storemodule;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;
import java.nio.file.Paths;

@RunWith(JUnit4.class)
public class TestSpaceManager {

  private final TsRuntime.TsRuntimeBuilder runtimeBuilder = TsRuntime.builder()
      .dataStorePath("./target/test-data/")
      .maxStoreSize(FileHeader.HEADER_SIZE + 1024)
      .maxFileSize(FileHeader.HEADER_SIZE + 1024) // 1kb
      .reader(RecordImpl.READER)
      .writer(RecordImpl.WRITER);

  public TestSpaceManager() throws IOException {
    FileUtils.forceMkdir(Paths.get("./target/test-data/").toFile());
  }

  @Test
  public void testSimpleRecycle() {
    TsRuntime runtime = runtimeBuilder.build();
    SpaceManager manager = new SpaceManager(runtime);
    TsBucket bucket = manager.createBucket("0");
    genOneFile(bucket);
    bucket.append(RecordImpl.random(52)); // 8 + 4 + 52 = 64
    Assert.assertEquals(FileHeader.HEADER_SIZE + 64, bucket.size());
  }

  private void genOneFile(TsBucket bucket) {
    // 16 * (8 + 56) = 1024 = one log file
    for (int i = 0; i < 16; i++) {
      bucket.append(RecordImpl.random(52));
    }
  }

  @Test
  public void testComplexRecycle() {
    TsRuntime runtime = runtimeBuilder.maxStoreSize((FileHeader.HEADER_SIZE + 1024) * 2).build();
    SpaceManager manager = new SpaceManager(runtime);
    TsBucket bucket0 = manager.createBucket("0");
    TsBucket bucket1 = manager.createBucket("1");
    genOneFile(bucket0);
    genOneFile(bucket1);
    bucket1.append(RecordImpl.random(52));
    genOneFile(bucket0); // 8 + 4 + 52 = 64
    Assert.assertEquals(FileHeader.HEADER_SIZE + 1024, bucket0.size());
    Assert.assertEquals(FileHeader.HEADER_SIZE + 64, bucket1.size());
  }
}
