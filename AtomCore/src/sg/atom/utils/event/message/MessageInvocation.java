/*
 *    Copyright 2008 Tim Jansen
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package sg.atom.utils.event.message;

import java.util.concurrent.atomic.AtomicLong;
import sg.atom.core.actor.exceptions.ActorRuntimeException;
import sg.atom.core.actor.internal.ActorState;
import sg.atom.core.actor.internal.AsyncResultImpl;
import sg.atom.utils.concurrent.AsyncResult;
import sg.atom.utils.concurrent.ThreadState;
import sg.atom.core.execution.workers.WorkerState;
import sg.atom.utils._commons.MultiThreadLogger;
import sg.atom.utils.concurrent.ThreadUsage;
import sg.atom.utils.datastructure.collection.fast.FastQueue;

/**
 * Represents the invocation of a message. Immutable thread-safe.
 *
 * @param <R> the result type
 */
public final class MessageInvocation<R> extends FastQueue.Entry {

    private final static MultiThreadLogger log = new MultiThreadLogger(MessageInvocation.class);
    private final static AtomicLong messageIdGenerator = new AtomicLong();

    enum Status {

        /**
         * The message is not running yet. Probably waiting in a queue for a
         * thread to pick it up.
         */
        NotRunningYet,
        /**
         * The message is currently being executed.
         */
        Running,
        /**
         * The message is waiting for a result.
         */
        Waiting,
        /**
         * The message is done.
         */
        Done
    };
    /**
     * The id of the message.
     */
    private final long messageNumber = messageIdGenerator.addAndGet(1);
    /**
     * The actor instance the message has been sent to.
     */
    final private ActorState target;
    /**
     * The caller to invoke the message.
     */
    final private MessageCaller<R> caller;
    /**
     * The arguments of the message. Don't modify the content after
     * construction!
     */
    final private Object[] arguments;
    /**
     * If not null, the message that invoked this message. Null for unmanaged
     * threads.
     */
    final private MessageInvocation<?> superInvocation;
    /**
     * The thread usage of the message.
     */
    final private ThreadUsage threadUsage;
    /**
     * True if the message is an initializer.
     */
    final private boolean isInitializer;
    /**
     * The AsyncResult representing the message's result.
     */
    final private AsyncResultImpl<R> result;

    /**
     * Creates a new instance
     *
     * @param target the target of the message
     * @param caller the caller to invoke the method
     * @param arguments the arguments of the message. Null for messages without
     * arguments
     * @param superInvocation the message that creates this message, or null for
     * unmanaged threads
     * @param usage the thread usage of the message
     * @param isInitializer true if the invocation is a initializer
     */
    public MessageInvocation(ActorState target, MessageCaller<R> caller,
            Object[] arguments, MessageInvocation<?> superInvocation,
            ThreadUsage usage, boolean isInitializer) {
        this.target = target;
        this.caller = caller;
        this.superInvocation = superInvocation;

        this.threadUsage = usage;
        this.isInitializer = isInitializer;

        this.result = new AsyncResultImpl<R>(this);

        if (arguments != null) {
            this.arguments = new Object[arguments.length];
            System.arraycopy(arguments, 0, this.arguments, 0, arguments.length);
        } else {
            this.arguments = null;
        }
    }

    /**
     * Executes the message now. The caller is responsible for all locking.
     *
     * @param ts the current ThreadState
     */
    public void runMessageNowNoLocking(ThreadState ts) {
        if (target.getController().isLoggingActions()) {
            log.info("Executing message #%d now.", messageNumber);
        }

        WorkerState oldState = ts.startInvocation(this);
        try {
            AsyncResult<R> r;
            r = caller.invoke(target.getActor(), arguments);
            if (r == null) {
                result.resultException(new ActorRuntimeException(
                        String.format("Actor %s message %s() returned null. This is not allowed. If you don't want to return a value, return \"noResult()\".",
                        target.getActor().getClass().getName(), caller.getMessageName())));
            } else if (isInitializer && r.isReady() && (r.get() != target.getActor())) {
                result.resultException(new ActorRuntimeException(
                        String.format("Actor %s initializer %s() must return a reference to itself. The easiest way to do this is to return \"result(this)\".",
                        target.getActor().getClass().getName(), caller.getMessageName(), r.get())));
            } else {
                result.resultReady(r);
            }
        } catch (Throwable t) {
            result.resultException(t);
        }
        ts.endInvocation(oldState);
        if (target.getController().isLoggingActions()) {
            log.info("Finished message #%d.", messageNumber);
        }
    }

    /**
     * Returns the message that has invoked this message. Null if there is no
     * such message because an unmanaged thread invoked this message.
     *
     * @return the invoking message
     */
    public MessageInvocation<?> getSuperInvocation() {
        return superInvocation;
    }

    /**
     * Returns the AsyncResultImpl representing the message's result.
     *
     * @return the AsyncResultImpl
     */
    public AsyncResultImpl<R> getAsyncResult() {
        return result;
    }

    /**
     * Checks whether this is a initializer invocation.
     *
     * @return true if it is an initializer invocationS
     */
    public boolean isInitializer() {
        return isInitializer;
    }

    /**
     * Returns the ThreadUsage of the message.
     *
     * @return the threadUsage
     */
    public ThreadUsage getThreadUsage() {
        return threadUsage;
    }

    /**
     * Returns the target of the message.
     *
     * @return the message's target
     */
    public ActorState getTargetActor() {
        return target;
    }

    /**
     * The number of this message (a sequential number).
     *
     * @return the id
     */
    public long getMessageNumber() {
        return messageNumber;
    }

    @Override
    public String toString() {
        return "MessageInvocation #" + messageNumber + " name=" + caller.getMessageName();
    }
}
