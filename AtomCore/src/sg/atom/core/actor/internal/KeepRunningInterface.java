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
package sg.atom.core.actor.internal;

/**
 * Simple interface to allow a worker to find out whether it should continue running.
 */
public interface KeepRunningInterface {
	
	/**
	 * Returns true if the worker should continue. False otherwise.
	 * @return true to continue, false to stop
	 */
	boolean shouldContinue();
}
