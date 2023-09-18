package org.luncert.tinystorage.storemodule.physics;

import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import lombok.Getter;
import org.luncert.tinystorage.storemodule.config.Configuration;

import java.io.IOException;
import java.nio.file.Paths;
import org.luncert.tinystorage.storemodule.exception.TinyStorageException;
import sun.misc.Unsafe;

public class File extends MetadataAccessor {

    private final MappedFile mappedFile;

    public File(MappedFile mappedFile, DataType... metadataDataTypes) {
        this(mappedFile, 0, metadataDataTypes);
    }

    public File(MappedFile mappedFile, int metadataOffset, DataType... metadataDataTypes) {
        super(metadataOffset, metadataDataTypes);
        this.mappedFile = mappedFile;
        buffer = mappedFile.buffer;
    }

    public static MappedFile open(String id) {
        return new MappedFile(id);
    }

    public static class MappedFile extends ByteBufferIO {

        @Getter
        private final String id;
        private RandomAccessFile handle;
        private volatile boolean closed;

        public MappedFile(String id) {
            this.id = id;

            String fileName = Paths.get(Configuration.get().getDataStorePath(), id).toString();
            try {
                handle = new RandomAccessFile(fileName, "rw");
                buffer = handle.getChannel().map(FileChannel.MapMode.READ_WRITE, 0,
                    Configuration.get().getMaxFileSize());
            } catch (IOException e) {
                throw new TinyStorageException("cannot open file", e);
            }
        }

        public boolean close() throws IOException {
            if (!closed) {
                Unsafe.getUnsafe().invokeCleaner(buffer);
                handle.close();
                handle = null;
                closed = true;
            }

            return closed;
        }
    }

    public void close() throws IOException {
        mappedFile.close();
    }
}
