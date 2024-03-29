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
package sg.atom.core.actor.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.locks.ReentrantLock;

import sg.atom.core.actor.Agent;
import sg.atom.core.actor.internal.ActorState;
import sg.atom.core.actor.internal.KeepRunningInterface;
import sg.atom.core.execution.workers.Worker;
import sg.atom.core.execution.workers.WorkerState;
import sg.atom.utils.datastructure.collection.fast.FastQueue;

/**
 * Main class. Controls threads as well as the list of actors that have work to do.
 */
public final class ControllerImplementation implements Controller {

	/**
	 * Contains the Agent (interface) of the controller.
	 */
	private final Agent agent;

	/**
	 * An executor (for thread-pooling) to use.
	 */
	private final ExecutorService executor;
	
	/**
	 * Lock for accessing an ActorState of this Controller or and the mActorsWithWork list. 
	 */
	private final ReentrantLock actorLock = new ReentrantLock();
	
	/**
	 * Contains a list of all actors that have unprocessed messages. Note that some
	 * actors may be busy and are unable to execute a message. It is also not guaranteed 
	 * that the list is always complete, for implementation reasons 
	 * (in {@link ActorState#reacquireBusyLock(MessageInvocation)}) there is a small lag.
	 * 
	 * Threads should pick up the first ActorState that can be executed. If they find a task,
	 * they should put it at the end. New entries should be inserted at the end as well.
	 * 
	 * Locking policy: you must synchronize actorLock before accessing this queue. 
	 */	
	private final FastQueue<ActorState> actorsWithWork = new FastQueue<ActorState>(); 
	

	/**
	 * Returns the maximum number of physical worker threads to run.
	 */
	final int maxPhysicalWorker;
	
	/**
	 * Returns the maximum number of effective worker threads to run.
	 */
	final int maxEffectiveWorker;

	/**
	 * If enabled, the Agent will log all messages that Agents send
	 * (using Java's logging system as INFO messages).
	 */
	private final boolean logActions;
	
	/**
	 * Lock for accessing threadStatistics and activeThreads. 
	 * Anti-Deadlock: You may lock this after the actorLock, but never before!
	 */
	private Object threadLock = new Object();
	
	/**
	 * An array that counts threads in the {@link WorkerState} states.
	 * The index corresponds to the WorkerState ordinal number.
	 * 
	 * Locking policy: you must synchronize threadLock before accessing this queue. 
	 */
	private int[] threadStatistics = new int[WorkerState.values().length];
	
	/**
	 * Counts the number of threads that the Controller is currently managing. 
	 * 
	 * Locking policy: you must synchronize threadLock before accessing this field. 
	 */
	private int workerThreads;

	/**
	 * Returns the number of additional parallel tasks that could be processed, if there were
	 * enough threads for this.
	 * 
	 * Locking policy: you must synchronize threadLock before accessing this field. 
	 */
	private int numberOfOpenParallelTasks;

	/**
	 * Returns the number of threads that should terminate themselves in order to get 
	 * have an optimal number of threads running
	 * 
	 * Locking policy: you must synchronize threadLock before accessing this field. 
	 */
	private int numberOfThreadsToKill;
	
	/**
	 * Creates a new controller.
	 * @param agent the agent of the controller. Possible not initialized yet.
	 * @param threadFactory the ThreadFactory to use
	 * @param maxPhysicalWorker the maximum number of physical worker threads to run
	 * @param maxEffectiveWorker the maximum number of effective worker threads to run
	 * @param logActions if true, actions like messages will be logged
	 */
	public ControllerImplementation(Agent agent, ThreadFactory threadFactory, int maxPhysicalWorker, int maxEffectiveWorker,
			boolean logActions) {
		this.agent = agent;
		this.executor = Executors.newCachedThreadPool(threadFactory);
		this.maxPhysicalWorker = maxPhysicalWorker;
		this.maxEffectiveWorker = maxEffectiveWorker;
		this.logActions = logActions;
	}
	
	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#getNextFromQueueUnsynchronized()
	 */
	public ActorState getNextFromQueueUnsynchronized() throws InterruptedException{
		return actorsWithWork.rotate();
	} 

	
	/**
	 * Returns the number of effective threads running in the system.
	 * This is the number of threads that is really running if you ignore waiting threads,
	 * and don't count I/O threads fully.
	 * 
	 * You must be synchronized on threadLock before calling this!
	 * @return the number of effective threads
	 */
	private int getEffectiveThreadsUnsynchronized() {
		return threadStatistics[WorkerState.Running.ordinal()] +
			   threadStatistics[WorkerState.RunningIO.ordinal()] / 8 +
			   threadStatistics[WorkerState.WaitingExternal.ordinal() / 128];
	}
	
	/**
	 * Returns the number of threads by which the number of worker threads should change. 
	 * If the number is positive, new threads should be created (or, rather, woken up
	 * from the pool). If it is negative, threads should be terminated (or returned to the pool).
	 * 
	 * You must be synchronized on threadLock before calling this!
	 * @return the number of threads needed
	 */
	private int getThreadNumberCorrectionUnsynchronized() {
		final int n = workerThreads;
		if (n > maxPhysicalWorker)
			return maxPhysicalWorker - n;
		
		final int e = getEffectiveThreadsUnsynchronized();
		if (e > maxEffectiveWorker)
			return -Math.min(n, e - maxEffectiveWorker);
		
		return Math.min(Math.min(numberOfOpenParallelTasks, maxPhysicalWorker - n), maxEffectiveWorker - e);
	}

	/**
	 * Creates the given number of threads.
	 * 
	 * You must be synchronized on threadLock before calling this!
	 * @param n the number of threads to create
	 */
	private void createThreadsUnsynchronized(int n) {
		for (int i = 0; i < n; i++)
			executor.execute(new Worker(this, createKeepRunningInterface()));

		workerThreads += n;
		threadStatistics[WorkerState.Running.ordinal()] += n;
	}

	
	/**
	 * Adds or kills threads to have the right number running.
	 * 
	 * You must be synchronized on threadLock before calling this!
	 */
	private void correctWorkerThreadsUnsynchronized() {
		int c = getThreadNumberCorrectionUnsynchronized();
		
		if (c > 0) {
			if (numberOfThreadsToKill > 0) {
				int k = Math.min(c, numberOfThreadsToKill);
				numberOfThreadsToKill -= k;
				c -= k;
			}
			if (c > 0)
				createThreadsUnsynchronized(c);
 		}
		else if (c < 0)
			numberOfThreadsToKill += -c;
	}
	

	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#updateActorStateQueueUnsynchronized(sg.atom.core.actor.internal.ActorState, int, int)
	 */
	public void updateActorStateQueueUnsynchronized(ActorState actorState, int oldNumberOfOpenParallelTasks, int newNumberOfOpenParallelTasks) {
		boolean shouldBeInQueue = (newNumberOfOpenParallelTasks>0);
		if (shouldBeInQueue != actorsWithWork.isInQueue(actorState)) {
			if (shouldBeInQueue)
				actorsWithWork.add(actorState);
			else
				actorsWithWork.remove(actorState);
		}

		synchronized (threadLock) {
			numberOfOpenParallelTasks += (newNumberOfOpenParallelTasks - oldNumberOfOpenParallelTasks);
			correctWorkerThreadsUnsynchronized();
		}
	}

	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#changeWorkerThreadState(sg.atom.core.actor.internal.WorkerState, sg.atom.core.actor.internal.WorkerState)
	 */
	public void changeWorkerThreadState(WorkerState oldState, WorkerState newState) {
		if (oldState == newState)
			return;
		
		synchronized (threadLock) {
			threadStatistics[oldState.ordinal()]--;
			threadStatistics[newState.ordinal()]++;
			correctWorkerThreadsUnsynchronized();
		}
	}

	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#removeWorkerThreadState(sg.atom.core.actor.internal.WorkerState)
	 */
	public void removeWorkerThreadState(WorkerState oldState) {
		synchronized (threadLock) {
			threadStatistics[oldState.ordinal()]--;
			workerThreads--;
			correctWorkerThreadsUnsynchronized();
		}
	}
	
	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#getAgent()
	 */
	public Agent getAgent() {
		return agent;
	}

	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#createKeepRunningInterface()
	 */
	public KeepRunningInterface createKeepRunningInterface() {
		return new KeepRunningInterface() {
			private boolean gotKilled = false; 
			public boolean shouldContinue() {
				synchronized (threadLock) {
					if (gotKilled)
						return false;
					if (numberOfThreadsToKill == 0)
						return true;
					
					numberOfThreadsToKill--;
					gotKilled = true;
					return false;
				}
			}
		};
	}

	/* (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#isLoggingActions()
	 */
	public boolean isLoggingActions() {
		return logActions;
	}

	/**
	 * Returns the actor lock for accessing an ActorState of this Controller or and the 
	 * mActorsWithWork list. 
	 * @return the actor lock
	 */
	public ReentrantLock getActorLock() {
		return actorLock;
	}

	/*
	 * (non-Javadoc)
	 * @see sg.atom.core.actor.internal.Controller#shutdown()
	 */
	public void shutdown() {
		actorLock.lock();
		try {
			actorsWithWork.clear();
			executor.shutdownNow();
		}
		finally {
			actorLock.unlock();
		}
	}
}

