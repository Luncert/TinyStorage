package org.luncert.tinystorage.srv.service;

import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.lmax.disruptor.util.DaemonThreadFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.luncert.tinystorage.srv.model.LineRecord;
import org.luncert.tinystorage.srv.queue.RecordEvent;
import org.luncert.tinystorage.storemodule.TinyStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Slf4j
@Service
@RequiredArgsConstructor
public class PersistService implements EventHandler<RecordEvent> {

    private final TinyStorage ts;
    private final Disruptor<RecordEvent> disruptor;
    private final RingBuffer<RecordEvent> ringBuffer;

    @Autowired
    public PersistService(TinyStorage ts) {
        this.ts = ts;
        disruptor = new Disruptor<>(
                RecordEvent.EVENT_FACTORY,
                128,
                DaemonThreadFactory.INSTANCE,
                ProducerType.MULTI,
                new BusySpinWaitStrategy());
        disruptor.handleEventsWith(this);
        ringBuffer = disruptor.start();
    }

    public void persist(String bucketId, byte[] source) {
        long sequenceId = ringBuffer.next();
        RecordEvent recordEvent = disruptor.get(sequenceId);
        recordEvent.setBucketId(bucketId);
        recordEvent.setSource(source);
        recordEvent.setCreatedAt(System.currentTimeMillis());
        ringBuffer.publish(sequenceId);
    }

    @Override
    public void onEvent(RecordEvent recordEvent, long l, boolean b) {
        byte[] source = recordEvent.getSource();
        int pre = 0;
        int len = source.length;
        // split by newline
        for (int i = 0; i < len; i++) {
            if (source[i] == '\n') {
                ts.append(recordEvent.getBucketId(), LineRecord.builder()
                        .timestamp(recordEvent.getCreatedAt())
                        .source(Arrays.copyOfRange(source, pre, i + 1))
                        .build());
                pre = i + 1;
            }
        }
        if (pre < len) {
            ts.append(recordEvent.getBucketId(), LineRecord.builder()
                    .timestamp(recordEvent.getCreatedAt())
                    .source(Arrays.copyOfRange(source, pre, len))
                    .build());
        }
    }
}
