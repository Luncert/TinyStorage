package org.luncert.tinystorage.srv.controller;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.luncert.tinystorage.srv.service.PersistService;
import org.luncert.tinystorage.storemodule.TimeRange;
import org.luncert.tinystorage.storemodule.TinyStorage;
import org.luncert.tinystorage.storemodule.descriptor.TsDesc;
import org.luncert.tinystorage.storemodule.exception.ResourceMissingException;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;

import static org.luncert.tinystorage.srv.base.Constants.ANONYMOUS_CHANNEL;
import static org.luncert.tinystorage.srv.base.Constants.STREAMING_CHANNEL;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM_VALUE;

@Slf4j
@RestController
@RequestMapping("/storage")
@RequiredArgsConstructor
public class StorageController {

  private final TinyStorage ts;
  private final PersistService persistService;

  @GetMapping("/descriptors")
  public TsDesc getStatus() {
    return ts.getDescriptor();
  }

  @PutMapping("/{bucketId}")
  public void write(@PathVariable String bucketId, @RequestBody byte[] source) {
    persistService.persist(bucketId, source);
  }

  @GetMapping("/{bucketId}")
  public ResponseEntity<ResponseBodyEmitter> read(@PathVariable String bucketId) throws IOException {
    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    RecordSubscriber subscriber = new ResponseBodyEmitterSubscriber(emitter);
    ts.subscribe(bucketId, TimeRange.UNSET, subscriber);
    return new ResponseEntity<>(emitter, HttpStatus.OK);
  }

  @GetMapping(value = "/stream/{bucketId}", produces = TEXT_EVENT_STREAM_VALUE)
  public ResponseEntity<SseEmitter> stream(@PathVariable String bucketId) throws IOException {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

    RecordSubscriber subscriber = new SseSubscriber(emitter);
    ts.subscribe(bucketId, TimeRange.UNSET, subscriber);
    emitter.onError(subscriber::onError);
    emitter.onCompletion(subscriber::onComplete);

    // send empty event to let client receive response header immediately
    emitter.send(SseEmitter.event().name(ANONYMOUS_CHANNEL).data(STREAMING_CHANNEL));

    return ResponseEntity.ok().body(emitter);
  }

  @GetMapping(value = "/physical-file/{fileId}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
  public ResponseEntity<ResponseBodyEmitter> readFile(@PathVariable String fileId) {
    ResponseBodyEmitter emitter = new ResponseBodyEmitter();
    ts.readStorePhysicalFile(fileId, new Subscriber<>() {
      public void onSubscribe(Subscription s) {
      }

      @SneakyThrows
      @Override
      public void onNext(String s) {
        emitter.send(s, MediaType.TEXT_PLAIN);
      }

      @Override
      public void onError(Throwable t) {
        emitter.completeWithError(t);
      }

      @Override
      public void onComplete() {
        emitter.complete();
      }
    });
    return new ResponseEntity<>(emitter, HttpStatus.OK);
  }

  @ExceptionHandler(ResourceMissingException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public void handleException() {
  }
}
