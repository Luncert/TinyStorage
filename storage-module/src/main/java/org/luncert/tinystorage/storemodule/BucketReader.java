package org.luncert.tinystorage.storemodule;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class BucketReader implements Iterable<Record>, Iterator<Record> {

  private final Iterator<ConcurrentBuffer.ReadContext> readers;

  private ConcurrentBuffer.ReadContext currentReader;

  private boolean hasNext = true;

  BucketReader(Collection<ConcurrentBuffer.ReadContext> readerContexts) {
    this.readers = readerContexts.iterator();
    checkNext();
  }

  @Override
  public Iterator<Record> iterator() {
    return this;
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public Record next() {
    if (hasNext) {
      Record record = currentReader.next();
      checkNext();
      return record;
    }

    throw new NoSuchElementException();
  }

  private void checkNext() {
    while (currentReader == null || !currentReader.hasNext()) {
      if (!readers.hasNext()) {
        hasNext = false;
        return;
      }

      currentReader = readers.next();
    }

    hasNext = true;
  }
}
