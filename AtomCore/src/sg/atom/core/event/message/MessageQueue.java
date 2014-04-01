// Copyright © 2011-2012, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0
package sg.atom.core.event.message;

import java.util.concurrent.*;
import sg.atom.utils.concurrent.ThreadSafe;

/**
 * Asynchronous unbounded queue for message passing.
 * 
 * FIXME: Replace with generic BlockingQueue or RingBuffer.
 */
@ThreadSafe
public class MessageQueue<T> implements MessageSender<T>, MessageReceiver<T> {

    private final BlockingQueue<T> queue = new LinkedBlockingQueue<T>();

    @Override
    public void send(T message) {
        queue.add(message);
    }

    @Override
    public T take() throws InterruptedException {
        return queue.take();
    }

    @Override
    public T poll() {
        return queue.poll();
    }
}
