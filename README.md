# TinyStorage

MappedByteBuffer based storage service. Can be used as log storage and etc.

## Features

- recycle old data in case disk space is full.
- thread-safe operation on MappedByteBuffer, supports concurrentlty write/read.

## Performance

Write records:

```properties
url: http://localhost:8080/storage/test
method: PUT
headers:
body: 14 bytes
request num: 10000
concurrency: 10
----------------
total time: 1.617236 sec
qps: 6183.391349
total: 10000
failed: 0
```
