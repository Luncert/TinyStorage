package org.luncert.tinystorage.srv.queue;

import com.lmax.disruptor.EventFactory;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RecordEvent {

    private String bucketId;
    private byte[] source;
    private long createdAt;

    public final static EventFactory<RecordEvent> EVENT_FACTORY = RecordEvent::new;
}