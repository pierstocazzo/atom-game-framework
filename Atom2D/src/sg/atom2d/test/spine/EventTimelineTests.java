/******************************************************************************
 * Spine Runtimes Software License
 * Version 2
 * 
 * Copyright (c) 2013, Esoteric Software
 * All rights reserved.
 * 
 * You are granted a perpetual, non-exclusive, non-sublicensable and
 * non-transferable license to install, execute and perform the Spine Runtimes
 * Software (the "Software") solely for internal use. Without the written
 * permission of Esoteric Software, you may not (a) modify, translate, adapt or
 * otherwise create derivative works, improvements of the Software or develop
 * new applications using the Software or (b) remove, delete, alter or obscure
 * any trademarks or any copyright, trademark, patent or other intellectual
 * property or proprietary rights notices on or in the Software, including
 * any copy thereof. Redistributions in binary or source form must include
 * this license and terms. THIS SOFTWARE IS PROVIDED BY ESOTERIC SOFTWARE
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL ESOTERIC SOFTARE BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *****************************************************************************/

package com.esotericsoftware.spine;

import com.esotericsoftware.spine.Animation.EventTimeline;

import sg.atom.utils.collection.Array;
import sg.atom.utils.collection.StringBuilder;

import java.util.Arrays;

/** Unit tests for {@link EventTimeline}. */
public class EventTimelineTests {
	private final SkeletonData skeletonData;
	private final Skeleton skeleton;
	private final Array<Event> firedEvents = new Array();
	private EventTimeline timeline = new EventTimeline(0);
	private char[] events;
	private float[] frames;

	public EventTimelineTests () {
		skeletonData = new SkeletonData();
		skeleton = new Skeleton(skeletonData);

		test(0);
		test(1);
		test(1, 1);
		test(1, 2);
		test(1, 2);
		test(1, 2, 3);
		test(1, 2, 3);
		test(0, 0, 0);
		test(0, 0, 1);
		test(0, 1, 1);
		test(1, 1, 1);
		test(1, 2, 3, 4);
		test(0, 2, 3, 4);
		test(0, 2, 2, 4);
		test(0, 0, 0, 0);
		test(2, 2, 2, 2);
		test(0.1f);
		test(0.1f, 0.1f);
		test(0.1f, 50f);
		test(0.1f, 0.2f, 0.3f, 0.4f);
		test(1, 2, 3, 4, 5, 6, 6, 7, 7, 8, 9, 10, 11, 11.01f, 12, 12, 12, 12);

		System.out.println("All tests passed.");
	}

	private void test (float... frames) {
		int eventCount = frames.length;

		StringBuilder buffer = new StringBuilder();
		for (int i = 0; i < eventCount; i++)
			buffer.append((char)('a' + i));

		this.events = buffer.toString().toCharArray();
		this.frames = frames;
		timeline = new EventTimeline(eventCount);

		float maxFrame = 0;
		int distinctCount = 0;
		float lastFrame = -1;
		for (int i = 0; i < eventCount; i++) {
			float frame = frames[i];
			Event event = new Event(new EventData("" + events[i]));
			timeline.setFrame(i, frame, event);
			maxFrame = Math.max(maxFrame, frame);
			if (lastFrame != frame) distinctCount++;
			lastFrame = frame;
		}

		run(0, 99, 0.1f);
		run(0, maxFrame, 0.1f);
		run(frames[0], 999, 2f);
		run(frames[0], maxFrame, 0.1f);
		run(0, maxFrame, (float)Math.ceil(maxFrame / 100));
		run(0, 99, 0.1f);
		run(0, 999, 100f);
		if (distinctCount > 1) {
			float epsilon = 0.02f;
			// Ending before last.
			run(frames[0], maxFrame - epsilon, 0.1f);
			run(0, maxFrame - epsilon, 0.1f);
			// Starting after first.
			run(frames[0] + epsilon, maxFrame, 0.1f);
			run(frames[0] + epsilon, 99, 0.1f);
		}
	}

	private void run (float startTime, float endTime, float timeStep) {
		timeStep = Math.max(timeStep, 0.00001f);
		boolean loop = false;
		try {
			fire(startTime, endTime, timeStep, loop, false);
			loop = true;
			fire(startTime, endTime, timeStep, loop, false);
		} catch (FailException ignored) {
			try {
				fire(startTime, endTime, timeStep, loop, true);
			} catch (FailException ex) {
				System.out.println(ex.getMessage());
				System.exit(0);
			}
		}
	}

	private void fire (float timeStart, float timeEnd, float timeStep, boolean loop, boolean print) {
		if (print) {
			System.out.println("events: " + Arrays.toString(events));
			System.out.println("frames: " + Arrays.toString(frames));
			System.out.println("timeStart: " + timeStart);
			System.out.println("timeEnd: " + timeEnd);
			System.out.println("timeStep: " + timeStep);
			System.out.println("loop: " + loop);
		}

		// Expected starting event.
		int eventIndex = 0;
		while (frames[eventIndex] < timeStart)
			eventIndex++;

		// Expected number of events when not looping.
		int eventsCount = frames.length;
		while (frames[eventsCount - 1] > timeEnd)
			eventsCount--;
		eventsCount -= eventIndex;

		float duration = frames[eventIndex + eventsCount - 1];
		if (loop && duration > 0) { // When looping timeStep can't be > nyquist.
			while (timeStep > duration / 2f)
				timeStep /= 2f;
		}
		// duration *= 2; // Everything should still pass with this uncommented.

		firedEvents.clear();
		int i = 0;
		float lastTime = timeStart - 0.00001f;
		float timeLooped, lastTimeLooped;
		while (true) {
			float time = Math.min(timeStart + timeStep * i, timeEnd);
			lastTimeLooped = lastTime;
			timeLooped = time;
			if (loop && duration != 0) {
				lastTimeLooped %= duration;
				timeLooped %= duration;
			}

			int beforeCount = firedEvents.size;
			Array<Event> original = new Array(firedEvents);
			timeline.apply(skeleton, lastTimeLooped, timeLooped, firedEvents, 1);

			while (beforeCount < firedEvents.size) {
				char fired = firedEvents.get(beforeCount).getData().getName().charAt(0);
				if (loop) {
					eventIndex %= events.length;
				} else {
					if (firedEvents.size > eventsCount) {
						if (print) System.out.println(lastTimeLooped + "->" + timeLooped + ": " + fired + " == ?");
						timeline.apply(skeleton, lastTimeLooped, timeLooped, original, 1);
						fail("Too many events fired.");
					}
				}
				if (print) {
					System.out.println(lastTimeLooped + "->" + timeLooped + ": " + fired + " == " + events[eventIndex]);
				}
				if (fired != events[eventIndex]) {
					timeline.apply(skeleton, lastTimeLooped, timeLooped, original, 1);
					fail("Wrong event fired.");
				}
				eventIndex++;
				beforeCount++;
			}

			if (time >= timeEnd) break;
			lastTime = time;
			i++;
		}
		if (firedEvents.size < eventsCount) {
			timeline.apply(skeleton, lastTimeLooped, timeLooped, firedEvents, 1);
			if (print) System.out.println(firedEvents);
			fail("Event not fired: " + events[eventIndex] + ", " + frames[eventIndex]);
		}
	}

	private void fail (String message) {
		throw new FailException(message);
	}

	static class FailException extends RuntimeException {
		public FailException (String message) {
			super(message);
		}
	}

	static public void main (String[] args) throws Exception {
		new EventTimelineTests();
	}
}
