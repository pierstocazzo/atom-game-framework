/*
 *    Copyright 2008,2009 Tim Jansen
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
package sg.atom.core.actor.internal;

import sg.atom.core.concurrent.ThreadState;
import sg.atom.core.actor.controller.Controller;
import sg.atom.core.event.message.MessageInvocation;
import sg.atom.core.actor.Actor;

/**
 * ActorState implementation for single-threaded actors.
 */
public final class MultiThreadedActorState extends ActorState {
	/**
	 * Contains the number of additional threads that could now start running for this actor.
	 * (This is the sum of unprocessed multi-threaded messages plus one of there is a single-threaded
	 * message that could be executed now)  
	 */
	private int numberOfThreadsNeeded;
	
	/**
	 * Creates a new MultiThreadedActorState instance.
	 * @param scheduler the Actor's scheduler
	 * @param actor the actor whose state this instance is representing
	 */
	public MultiThreadedActorState(Controller scheduler, Actor actor) {
		super(scheduler, actor);
	}
	
	/*
	 * (non-Javadoc)
	 * @see sg.atom.core.actor.internal.ActorState#tryExecuteNow(sg.atom.core.actor.internal.MessageInvocation, sg.atom.core.actor.internal.ThreadState)
	 */
	public boolean tryExecuteNow(MessageInvocation<?> msgI, ThreadState ts) {
		if (!removeMessageFromQueue(msgI))
			return false;
		msgI.runMessageNowNoLocking(ts);
		return true;
	}
	
	/**
	 * Removes the given message from the queue.
	 * @param message the message to remove. Must be multi-threaded
	 * @return true if the message has been removed. False if it was not in the queue anymore.
	 */
	private  boolean removeMessageFromQueue(MessageInvocation<?> message) {
		boolean success;
		controller.getActorLock().lock();
		try {	
			success = mailbox.remove(message);
			updateControllerQueueUnsynchronized();	
		}
		finally {
			controller.getActorLock().unlock();
		}
		return success;
	}
	
	/*
	 * (non-Javadoc)
	 * @see sg.atom.core.actor.internal.ActorState#executeAllQueuedMessagesUnsynchronized(sg.atom.core.actor.internal.ThreadState, sg.atom.core.actor.internal.KeepRunningInterface)
	 */
	public int executeAllQueuedMessagesUnsynchronized(ThreadState ts, KeepRunningInterface keepRunning) {
		int msgsExecuted = 0;
		while (keepRunning.shouldContinue()) {
			MessageInvocation<?> msg = mailbox.pop();
			if (msg == null) {// no msg left -> leave
				updateControllerQueueUnsynchronized();
				return msgsExecuted;
			}
			
			updateControllerQueueUnsynchronized();
			controller.getActorLock().unlock(); // unlock for the execution!!
			try {
				msg.runMessageNowNoLocking(ts);
			}
			finally {
				controller.getActorLock().lock();
			}

			msgsExecuted++;
		}
		updateControllerQueueUnsynchronized();
		return msgsExecuted;
	}
	
	/*
	 * (non-Javadoc)
	 * @see sg.atom.core.actor.internal.ActorState#updateControllerQueueUnsynchronized()
	 */
	public void updateControllerQueueUnsynchronized() {
		int oldNumberOfThreadsNeeded = numberOfThreadsNeeded;
		numberOfThreadsNeeded = mailbox.size();
		if (oldNumberOfThreadsNeeded == numberOfThreadsNeeded)
			return;
		controller.updateActorStateQueueUnsynchronized(this, oldNumberOfThreadsNeeded, numberOfThreadsNeeded);
	}
}
