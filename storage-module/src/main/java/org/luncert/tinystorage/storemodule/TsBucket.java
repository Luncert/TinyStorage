package org.luncert.tinystorage.storemodule;

import org.luncert.tinystorage.storemodule.descriptor.DescribedObject;
import org.luncert.tinystorage.storemodule.descriptor.TsBucketDesc;
import org.luncert.tinystorage.storemodule.descriptor.TsFileDesc;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
class TsBucket implements DescribedObject<TsBucketDesc> {

  @Getter
  private final String id;

  private final TsRuntime runtime;

  private long bucketSize;

  @Getter
  private long lastAccessTime;

  private final LinkedList<TsFile> tsFiles = new LinkedList<>();

  void append(Object record) {
    synchronized (this) {
      lastAccessTime = System.currentTimeMillis();

      TsFile last = getFile();

      try {
        int n;
        while ((n = last.append(record)) <= 0) {
          // create new file, and try again
          last = createFile();
        }

        bucketSize += n;
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private TsFile getFile() {
    try {
      return tsFiles.getLast();
    } catch (NoSuchElementException e) {
      return createFile();
    }
  }

  private TsFile createFile() {
    TsFile file = runtime.getSpaceManager().createFile();
    tsFiles.add(file);
    bucketSize += file.size(); // header size
    return file;
  }

  /**
   * Create bucket reader with time range as filter.
   * @return BucketReader
   */
  BucketReader createReader() {
    synchronized (this) {
      List<ConcurrentBuffer.ReadContext> readerContexts = tsFiles.stream()
          .map(file -> {
            try {
              return file.createReader(false);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          }).collect(Collectors.toList());
      return new BucketReader(readerContexts);
    }
  }

  long size() {
    synchronized (this) {
      return bucketSize;
    }
  }

  int fileNum() {
    synchronized (this) {
      return tsFiles.size();
    }
  }

  /**
   * Try to close oldest file.
   * @return True if success
   */
  Optional<TsFile> recycleFile(boolean force) {
    synchronized (this) {
      if (tsFiles.isEmpty()) {
        return Optional.empty();
      }

      TsFile file = tsFiles.getFirst();
      long fileSize = file.size();
      if ((force || file.isReadOnly()) && file.reset()) {
        /*
        cannot delete mapped file in windows
        see https://bugs.java.com/view_bug.do?bug_id=4715154
        solution: https://stackoverflow.com/questions/2972986/how-to-unmap-a-file-from-memory-mapped-using-filechannel-in-java
        */
        // FileUtils.forceDelete(Paths.get(runtime.getDataStorePath(), first.getId()).toFile());
        tsFiles.removeFirst();
        bucketSize -= fileSize;
        return Optional.of(file);
      }

      return Optional.empty();
    }
  }

  @Override
  public TsBucketDesc getDescriptor() {
    List<TsFileDesc> tsFileDescList;
    synchronized (this) {
      tsFileDescList = tsFiles.stream()
          .map(TsFile::getDescriptor)
          .collect(Collectors.toList());
    }
    return TsBucketDesc.builder()
        .id(id)
        .bucketSize(bucketSize)
        .lastAccessTime(lastAccessTime)
        .files(tsFileDescList)
        .build();
  }
}
