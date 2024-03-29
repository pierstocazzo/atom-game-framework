/*
 * Copyright (c) 2007 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package sg.atom.core.execution.schedulers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionHandler;

/**
 * A Scheduler provides basic execution facilities across a number of worker
 * threads.
 *
 * @author blainey
 * @since 6
 */
public interface Scheduler extends ExecutorService {

	/**
	 * Get number of workers.
	 * @return number of workers.
	 */
	int numWorkers();

	/**
	 * Get handler used for rejected executions.
	 * @return handler for rejected executions.
	 */
	RejectedExecutionHandler getRejectedExecutionHandler();

	/**
	 * Set handler.
	 * @param handler Rejected execution handler.
	 */
	void setRejectedExecutionHandler(RejectedExecutionHandler handler);
}
