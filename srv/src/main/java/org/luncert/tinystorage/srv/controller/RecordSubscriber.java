package org.luncert.tinystorage.srv.controller;

import lombok.extern.slf4j.Slf4j;
import org.luncert.tinystorage.srv.model.LineRecord;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

@Slf4j
public abstract class RecordSubscriber implements Subscriber<LineRecord> {

  private Subscription subscription;
  
  RecordSubscriber() {
  }

  void poll(long n) {
    subscription.request(n);
  }
  
  void cancel() {
    subscription.cancel();
  }

  @Override
  public void onSubscribe(Subscription s) {
    subscription = s;
  }
}
