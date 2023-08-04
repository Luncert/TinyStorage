package org.luncert.tinystorage.srv.controller;

import static org.luncert.tinystorage.srv.base.Constants.LOG_TRANSPORT_INTERVAL;
import static org.luncert.tinystorage.srv.base.Constants.STREAMING_CHANNEL;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Timer;
import java.util.TimerTask;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.luncert.tinystorage.srv.model.ExecutionLog;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
class SseSubscriber extends RecordSubscriber {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private final SseEmitter emitter;
  private final Timer timer;
  private int counter;
  private SseEmitter.SseEventBuilder eventBuilder;
  private int eventBuilderSize;

  SseSubscriber(SseEmitter emitter) {
    this.emitter = emitter;

    resetEventBuilder();

    timer = new Timer();
    timer.schedule(new TimerTask() {
      @SneakyThrows
      @Override
      public void run() {
        synchronized (SseSubscriber.this) {
          if (eventBuilderSize > 0) {
            emitter.send(eventBuilder);
            resetEventBuilder();
          }
        }
      }
    }, LOG_TRANSPORT_INTERVAL, LOG_TRANSPORT_INTERVAL);
  }

  private void resetEventBuilder() {
    eventBuilderSize = 0;
    eventBuilder = SseEmitter.event().id(String.valueOf(counter++)).name(STREAMING_CHANNEL);
  }

  @Override
  @SneakyThrows
  public void onNext(ExecutionLog r) {
    synchronized (this) {
      // buffering to avoid taking up too much network resources
      eventBuilderSize++;
      eventBuilder.data(objectMapper.writeValueAsString(r));
    }
  }

  @Override
  public void onError(Throwable t) {
    cleanEventBuilder();
    emitter.completeWithError(t);
  }

  @Override
  public void onComplete() {
    cleanEventBuilder();
    emitter.complete();
  }

  @SneakyThrows
  private void cleanEventBuilder() {
    timer.cancel();
    if (eventBuilderSize > 0) {
      emitter.send(eventBuilder);
      resetEventBuilder();
    }
  }
  
  @Override
  void cancel() {
    super.cancel();
    onComplete();
  }
}
