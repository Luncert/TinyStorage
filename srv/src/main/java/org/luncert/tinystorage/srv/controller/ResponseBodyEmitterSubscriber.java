package org.luncert.tinystorage.srv.controller;

import java.io.IOException;
import lombok.SneakyThrows;
import org.luncert.tinystorage.srv.model.ExecutionLog;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyEmitter;

public class ResponseBodyEmitterSubscriber extends RecordSubscriber {

  private final ResponseBodyEmitter emitter;

  ResponseBodyEmitterSubscriber(ResponseBodyEmitter emitter) throws IOException {
    this.emitter = emitter;
  }

  @SneakyThrows
  @Override
  public void onNext(ExecutionLog record) {
    emitter.send(record.getSource(), MediaType.TEXT_PLAIN);
  }

  @Override
  public void onError(Throwable t) {
    emitter.completeWithError(t);
  }

  @Override
  public void onComplete() {
    emitter.complete();
  }
}
