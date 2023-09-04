# TinyStorage

MappedByteBuffer based storage service. Can be used as log storage and etc.

## Features

- recycle old data in case disk space is full.
- thread-safe operation on MappedByteBuffer, supports concurrentlty write/read.
