package org.luncert.tinystorage.storemodule.physics;

import org.luncert.tinystorage.storemodule.config.Configuration;
import org.luncert.tinystorage.storemodule.exception.TinyStorageException;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.util.UUID;

public class File extends FileMetadata {

    private final String id;

    private ConcurrentBuffer buffer;

    private volatile boolean closed;

    public File(String id) {
        this.id = id;
        buffer = new ConcurrentBuffer(Paths.get(Configuration.get().getDataStorePath(), id).toString(),
              Configuration.get().getMaxFileSize());
    }

    /**
     * Close log file and release resources.
     * @return True if no active reader, else False
     */
    public boolean close() throws IOException {
        if (!closed) {
            buffer.close();
            buffer = null;
            closed = true;
        }

        return closed;
    }
}
