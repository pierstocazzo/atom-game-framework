package sg.atom.fx.automatic;

import de.lessvoid.nifty.tools.LinearInterpolator;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;
import sg.atom.fx.timing.TimingSource;
import sg.atom.fx.timing.TimingSource.TickListener;
import sg.atom.fx.timing.TimingTarget;
import sg.atom.fx.tween.Interpolator;

/**
 * This class controls the timing of animations. Instances are constructed by a
 * {@link Animator.Builder} instance by invoking various set methods control the
 * parameters under which the desired animation is run. The parameters of this
 * class use the concepts of a "cycle" (the base animation) and an "envelope"
 * that controls how the cycle is started, ended, and repeated. <p> For example,
 * this animation will run for 1 second, calling your
 * {@link TimingTarget}, {@code myTarget}, with timing events when the animation
 * is started, running, and stopped:
 *
 * <pre>
 * Animator.setDefaultTimingSource(source); // shared timing source
 *
 * Animator animator = new Animator.Builder().setDuration(1, TimeUnit.SECONDS).addTarget(myTarget).build();
 * animator.start();
 * </pre>
 *
 * The following variation will run a half-second animation 4 times, reversing
 * direction each time:
 *
 * <pre>
 * Animator animator = new Animator.Builder().setDuration(500, TimeUnit.MILLISECONDS).setRepeatCount(4).addTarget(myTarget).build();
 * animator.start();
 * </pre>
 *
 * More complex animations can be created through the use of the complete set of
 * properties in {@link Animator.Builder}. <p> This class provides a useful
 * "debug" name via {@link Builder#setDebugName(String)} and
 * {@link #getDebugName()}. The debug name is also output by
 * {@link #toString()}. This feature is intended to aid debugging. <p> Instances
 * can be started again after they complete, however, ensure that they are not
 * running, via <tt>!</tt>{@link #isRunning()} or {@link #await()}, before
 * {@link #start()} or {@link #startReverse()} is called. Even if you
 * successfully invoked {@link #stop()} or {@link #cancel()} it can take some
 * time for all the calls to registered {@link TimingTarget}s to complete. Use
 * of {@link #await()} is far more efficient than polling the state of the
 * animation with {@link #isRunning()}. <p> This class is thread-safe.
 *
 * @author Chet Haase
 * @author Tim Halloran
 *
 * @see Builder
 */
public final class Animator implements TickListener {

    public static Logger logger = Logger.getLogger(Animator.class.getName());

    /**
     * EndBehavior determines what happens at the end of the animation.
     *
     * @see Builder#setEndBehavior(Animator.EndBehavior)
     */
    public static enum EndBehavior {

        /**
         * Timing sequence will maintain its final value at the end.
         */
        HOLD,
        /**
         * Timing sequence should reset to the initial value at the end.
         */
        RESET,
    };

    /**
     * Direction is used to set the initial direction in which the animation
     * starts.
     *
     * @see Builder#setStartDirection(Animator.Direction)
     */
    public static enum Direction {

        /**
         * The cycle proceeds forward.
         */
        FORWARD {
            @Override
            public Direction getOppositeDirection() {
                return BACKWARD;
            }
        },
        /**
         * The cycle proceeds backward.
         */
        BACKWARD {
            @Override
            public Direction getOppositeDirection() {
                return FORWARD;
            }
        };

        abstract public Direction getOppositeDirection();
    };

    /**
     * RepeatBehavior determines how each successive cycle will flow.
     *
     * @see Builder#setRepeatBehavior(Animator.RepeatBehavior)
     */
    public static enum RepeatBehavior {

        /**
         * Each repeated cycle proceeds in the same direction as the previous
         * one.
         */
        LOOP,
        /**
         * Each repeated cycle proceeds in the opposite direction as the
         * previous one.
         */
        REVERSE
    };
    /**
     * Used to specify unending repeat count.
     *
     * @see Builder#setRepeatCount(long)
     *
     */
    public static final long INFINITE = -1;

    /**
     * Sets the passed timing source as the default used for the construction of
     * animations. The no-argument constructor for {@link Animator.Builder} uses
     * the default timing source set by this method. <p> Passing {@code null} to
     * this method clears the default timing source. <p> The client code remains
     * responsible for disposing of the timing source when it is finished using
     * it.
     *
     * @param timingSource a timing source or {@code null} to clear the default.
     */
    public static void setDefaultTimingSource(TimingSource timingSource) {
        Builder.setDefaultTimingSource(timingSource);
    }

    /**
     * Gets the timing source being used as the default for the construction on
     * animations. The no-argument constructor for {@link Animator.Builder} uses
     * the default timing source. A {@code null} result indicates that no
     * default timing source has been set.
     *
     * @return the timing source being used as the default for the construction
     * on animations, or {@code null} if none.
     */
    public static TimingSource getDefaultTimingSource() {
        return Builder.getDefaultTimingSource();
    }

    /**
     * This class is used to construct {@link Animator} instances. <p> The
     * default values are listed in the table below. <p> <table border="1"> <tr>
     * <th>Method</th> <th>Description</th> <th>Default</th> </tr> <tr>
     * <td>{@link #addTarget(TimingTarget)}</td> <td>gets timing events while
     * the animation is running</td> <td align="right"><i>none</i></td> </tr>
     * <tr> <td>{@link Animator#setDefaultTimingSource(TimingSource)} or
     * {@link Animator.Builder#Animator.Builder(TimingSource)}</td> <td>a timing
     * source for the animation</td> <td align="right"><i>none</i></td> </tr>
     * <tr> <td>{@link #setDuration(long, TimeUnit)}</td> <td>the duration of
     * one cycle of the animation</td> <td align="right">1 second</td> </tr>
     * <tr> <td>{@link #setDisposeTimingSource(boolean)}</td> <td>if the
     * {@link TimingSource} used by the animation should be disposed at the end
     * of the animation.</td> <td align="right">{@code false}</td> </tr> <tr>
     * <td>{@link #setEndBehavior(Animator.EndBehavior)}</td> <td>what happens
     * at the end of the animation</td> <td
     * align="right">{@link Animator.EndBehavior#HOLD}</td> </tr> <tr>
     * <td>{@link #setInterpolator(Interpolator)}</td> <td>the interpolator for
     * each animation cycle</td> <td
     * align="right">{@link LinearInterpolator}</td> </tr> <tr>
     * <td>{@link #setRepeatBehavior(Animator.RepeatBehavior)}</td> <td>the
     * repeat behavior of the animation</td> <td
     * align="right">{@link Animator.RepeatBehavior#REVERSE}</td> </tr> <tr>
     * <td>{@link #setRepeatCount(long)}</td> <td>the number of times the
     * animation cycle will repeat</td> <td align="right">1</td> </tr> <tr>
     * <td>{@link #setStartDirection(Animator.Direction)}</td> <td>the start
     * direction for the initial animation cycle</td> <td
     * align="right">{@link Animator.Direction#FORWARD}</td> </tr> <tr>
     * <td>{@link #setDebugName(String)}</td> <td>a meaningful name for the
     * animation used by the {@link Animator#toString()} method</td> <td
     * align="right">null</td> </tr> </table>
     *
     * <p> Instances of this class are not thread safe and are intended to be
     * thread-confined. However, the {@link Animator} objects produced are
     * thread-safe.
     *
     * @author Tim Halloran
     */
    public static class Builder {

        /**
         * A default timing source used for the construction of animations. <p>
         * <i>Implementation note:</i> The setting and getting of the default
         * timing source is thread safe.
         */
        private static AtomicReference<TimingSource> f_defaultTimingSource = new AtomicReference<TimingSource>();

        /**
         * Sets the passed timing source as the default used for the
         * construction of animations. The no-argument constructor
         * ({@link #Builder()}) uses the default timing source set by this
         * method. <p> Passing {@code null} to this method clears the default
         * timing source. <p> This method is only called by
         * {@link Animator#setDefaultTimingSource(TimingSource)}.
         *
         * @param timingSource a timing source or {@code null} to clear the
         * default.
         */
        private static void setDefaultTimingSource(TimingSource timingSource) {
            f_defaultTimingSource.set(timingSource);
        }

        /**
         * Gets the timing source being used as the default for the construction
         * on animations.
         *
         * @return the timing source being used as the default for the
         * construction on animations, or {@code null} if none.
         */
        private static TimingSource getDefaultTimingSource() {
            return f_defaultTimingSource.get();
        }
        private String f_debugName = null;
        private long f_duration = 1;
        private TimeUnit f_durationTimeUnit = TimeUnit.SECONDS;
        private Animator.EndBehavior f_endBehavior = Animator.EndBehavior.HOLD;
        private Interpolator f_interpolator = null; // use the built-in default
        private Animator.RepeatBehavior f_repeatBehavior = Animator.RepeatBehavior.REVERSE;
        private long f_repeatCount = 1;
        private Animator.Direction f_startDirection = Animator.Direction.FORWARD;
        private final List<TimingTarget> f_targets = new ArrayList<TimingTarget>();
        private final TimingSource f_timingSource;
        private boolean f_disposeTimingSource = false;

        /**
         * Constructs an animation builder instance.
         *
         * @param timingSource the timing source for the animation.
         */
        public Builder(TimingSource timingSource) {
            if (timingSource == null) {
                throw new IllegalArgumentException("");
            }
            f_timingSource = timingSource;
        }

        /**
         * Constructs an animation builder instance using the default timing
         * source.
         *
         * @see #setDefaultTimingSource(TimingSource)
         */
        public Builder() {
            this(f_defaultTimingSource.get());
        }

        /**
         * Adds a {@link TimingTarget} to the list of targets that get notified
         * of each timing event while the animation is running. <p>
         * {@link TimingTarget}s will be called in the order they are added.
         *
         * @param target a {@link TimingTarget} object.
         * @return this builder (to allow chained operations).
         */
        public Builder addTarget(TimingTarget target) {
            if (target != null) {
                f_targets.add(target);
            }
            return this;
        }

        /**
         * Sets the "debug" name of the animation. The default value is
         * {@code null} .
         *
         * @param name a name of the animation. A {@code null} value is allowed.
         * @return this builder (to allow chained operations).
         */
        public Builder setDebugName(String name) {
            f_debugName = name;
            return this;
        }

        /**
         * Sets if the animation should invoke {@link TimingSource#dispose()} on
         * its timing source when it ends. The default value is {@code false}.
         *
         * @param value {@code true} if the animation should invoke
         * {@link TimingSource#dispose()} on its timing source when it ends,
         * {@code false} if not.
         * @return this builder (to allow chained operations).
         */
        public Builder setDisposeTimingSource(boolean value) {
            f_disposeTimingSource = value;
            return this;
        }

        /**
         * Sets the duration of one cycle of the animation. The default value is
         * one second.
         *
         * @param value the duration of the animation. This value must be >= 1.
         * @param unit the time unit of the value parameter. A {@code null}
         * value is equivalent to setting the default unit of
         * {@link TimeUnit#SECONDS}.
         * @return this builder (to allow chained operations).
         *
         * @throws IllegalStateException if value is not >= 1.
         */
        public Builder setDuration(long value, TimeUnit unit) {
            if (value < 1) {
                throw new IllegalArgumentException("");
            }

            f_duration = value;
            f_durationTimeUnit = unit != null ? unit : TimeUnit.SECONDS;
            return this;
        }

        /**
         * Sets the behavior at the end of the animation. The default value is
         * {@link Animator.EndBehavior#HOLD}.
         *
         * @param value the behavior at the end of the animation. A {@code null}
         * value is equivalent to setting the default value.
         * @return this builder (to allow chained operations).
         */
        public Builder setEndBehavior(Animator.EndBehavior value) {
            f_endBehavior = value != null ? value : Animator.EndBehavior.HOLD;
            return this;
        }

        /**
         * Sets the interpolator for each animation cycle. The default
         * interpolator is the built-in linear interpolator.
         *
         * @param value the interpolation to use each animation cycle. A
         * {@code null} value is equivalent to setting the default value.
         * @return this builder (to allow chained operations).
         */
        public Builder setInterpolator(Interpolator value) {
            f_interpolator = value;
            return this;
        }

        /**
         * Sets the repeat behavior of the animation. The default value is
         * {@link Animator.RepeatBehavior#REVERSE}.
         *
         * @param value the behavior for each successive animation cycle. A
         * {@code null} value is equivalent to setting the default value.
         * @return this builder (to allow chained operations).
         */
        public Builder setRepeatBehavior(Animator.RepeatBehavior value) {
            f_repeatBehavior = value != null ? value : Animator.RepeatBehavior.REVERSE;
            return this;
        }

        /**
         * Sets the number of times the animation cycle will repeat. The default
         * value is 1.
         *
         * @param value number of times the animation cycle will repeat. This
         * value must be >= 1 or {@link Animator#INFINITE} for animations that
         * repeat indefinitely.
         * @return this builder (to allow chained operations).
         *
         * @throws IllegalArgumentException if value is not >=1 or
         * {@link Animator#INFINITE}.
         */
        public Builder setRepeatCount(long value) {
            if (value < 1 && value != Animator.INFINITE) {
                throw new IllegalArgumentException("");
            }

            f_repeatCount = value;
            return this;
        }

        /**
         * Sets the start direction for the initial animation cycle. The default
         * start direction is {@link Animator.Direction#FORWARD}.
         *
         * @param value initial animation cycle direction. A {@code null} value
         * is equivalent to setting the default value.
         * @return this builder (to allow chained operations).
         */
        public Builder setStartDirection(Animator.Direction value) {
            f_startDirection = value != null ? value : Animator.Direction.FORWARD;
            return this;
        }

        /**
         * Constructs an animation with the settings defined by this builder.
         *
         * @return an animation.
         */
        public Animator build() {
            final Animator result = new Animator(f_debugName, f_duration, f_durationTimeUnit, f_endBehavior, f_interpolator,
                    f_repeatBehavior, f_repeatCount, f_startDirection, f_timingSource, f_disposeTimingSource);
            for (TimingTarget target : f_targets) {
                result.addTarget(target);
            }
            return result;
        }
    }

    /*
     * Immutable state set by the builder.
     */
    private final String f_debugName; // may be null
    private final long f_duration;
    private final TimeUnit f_durationTimeUnit;
    private final EndBehavior f_endBehavior;
    private final Interpolator f_interpolator; // null means linear
    private final RepeatBehavior f_repeatBehavior;
    private final long f_repeatCount;
    private final Direction f_startDirection;
    private final TimingSource f_timingSource;
    private final boolean f_disposeTimingSource; // at end

    /**
     * Gets the "debug" name of this animation.
     *
     * @return the "debug" name of this animation. May be {@code null}.
     */
    public String getDebugName() {
        return f_debugName;
    }

    /**
     * Gets the duration of one cycle of this animation. The units of this value
     * are obtained by calling {@link #getDurationTimeUnit()}.
     *
     * @return the duration of the animation. This value must be >= 1 or
     * {@link Animator#INFINITE}, meaning the animation will run until manually
     * stopped.
     *
     * @see #getDurationTimeUnit()
     */
    public long getDuration() {
        return f_duration;
    }

    /**
     * Gets the time unit of the duration of one cycle of this animation. The
     * duration is obtained by calling {@link #getDuration()}.
     *
     * @return the time unit of the value parameter.
     *
     * @see #getDuration()
     */
    public TimeUnit getDurationTimeUnit() {
        return f_durationTimeUnit;
    }

    /**
     * Gets the behavior at the end of this animation.
     *
     * @return the behavior at the end of the animation.
     */
    public EndBehavior getEndBehavior() {
        return f_endBehavior;
    }

    /**
     * Gets the interpolator for this animation.
     *
     * @return the interpolation to use each animation cycle.
     */
    public Interpolator getInterpolator() {
        //FIXME: Changed!
        //return f_interpolator != null ? f_interpolator : LinearInterpolator.getInstance();
        return null;
    }

    /**
     * Gets the repeat behavior of this animation.
     *
     * @return the behavior for each successive animation cycle.
     */
    public RepeatBehavior getRepeatBehavior() {
        return f_repeatBehavior;
    }

    /**
     * Gets the number of times the animation cycle will repeat.
     *
     * @return number of times the animation cycle will repeat. This value is >=
     * 1 or {@link Animator#INFINITE} for animations that repeat indefinitely.
     */
    public long getRepeatCount() {
        return f_repeatCount;
    }

    /**
     * Gets the start direction for the initial animation cycle.
     *
     * @return initial animation cycle direction.
     */
    public Direction getStartDirection() {
        return f_startDirection;
    }

    /**
     * Gets the timing source for this animation.
     *
     * @return a timing source.
     */
    public TimingSource getTimingSource() {
        return f_timingSource;
    }

    /*
     * Mutable thread-safe state that is managed by this animation.
     */
    /**
     * This animation may have multiple {@link TimingTarget} listeners. <p>
     * Protects the mutable state of this animation (rather than creating a new
     * Object). <p> Do not hold this lock when invoking any callbacks, e.g.,
     * looping through {@link #f_targets}. <p> Do not hold this lock when
     * invoking any method on {@link #f_timingSource}.
     */
    private final CopyOnWriteArrayList<TimingTarget> f_targets = new CopyOnWriteArrayList<TimingTarget>();
    /**
     * Tracks the original start time in nanoseconds of the animation. <p>
     * Accesses must be guarded by a lock on {@link #f_targets}.
     */
    private long f_startTimeNanos;
    /**
     * Tracks start time of current cycle. <p> Accesses must be guarded by a
     * lock on {@link #f_targets}.
     */
    private long f_cycleStartTimeNanos;
    /**
     * Used for pause/resume. If this value is non-zero and the animation is
     * running, then the animation is paused. <p> Accesses must be guarded by a
     * lock on {@link #f_targets}.
     */
    private long f_pauseBeginTimeNanos;
    /**
     * The current direction of the animation. <p> Accesses must be guarded by a
     * lock on {@link #f_targets}.
     */
    private Direction f_currentDirection;
    /**
     * Indicates that {@link #reverseNow()} was invoked <i>x</i> times. The
     * actual reverse occurs during the next call to this animation's
     * {@link #timingSourceTick(TimingSource, long)} method so we need to
     * remember how many calls were made.
     */
    private int f_reverseNowCallCount;
    /**
     * A latch used to indicate the animation is running and to allow client
     * code to wait until the animation is completed. When this field is non-
     * {@code null} then the animation is running (note that a paused animation
     * is still considered to be running). <p> This field may be
     * non-{@code null} long after {@link #stop()} or {@link #cancel()} are
     * called because the latch is not triggered and changed to a {@code null}
     * value until all callbacks to registered {@link TimingTarget}s have
     * completed. The flag {@link #f_stopping} indicates the animation is in the
     * process of stopping. <p> Accesses must be guarded by a lock on
     * {@link #f_targets}.
     */
    private CountDownLatch f_runningAnimationLatch;
    /**
     * Indicates the animation is stopping &mdash; it is in a shutdown phase.
     * This gets set when the animation completes normally or when {@link #stop()} /
     * {@link #cancel()} are invoked by the client. A value of {@code true}
     * indicates that a running animation is in a shutdown phase and is
     * finishing up any needed callbacks to registered {@link TimingSource}s.
     * <p> This flag is used as a guard so that we don't run try to stop the
     * animation multiple times. <p> This guard is needed because a long period
     * of time can elapse between when the animation knows it is trying to stop
     * and when the callbacks to the client code complete. The animation is
     * still running but should not try to stop again &mdash; avoiding this
     * problem is the purpose of this guard. <p> Accesses must be guarded by a
     * lock on {@link #f_targets}.
     */
    private boolean f_stopping;

    /**
     * Constructs an animation. <p> This constructor should only be called from
     * {@link Builder#build()}.
     */
    Animator(String debugName, long duration, TimeUnit durationTimeUnit, EndBehavior endBehavior, Interpolator interpolator,
            RepeatBehavior repeatBehavior, long repeatCount, Direction startDirection, TimingSource timingSource,
            boolean disposeTimingSource) {
        f_debugName = debugName;
        f_duration = duration;
        f_durationTimeUnit = durationTimeUnit;
        f_endBehavior = endBehavior;
        f_interpolator = interpolator;
        f_repeatBehavior = repeatBehavior;
        f_repeatCount = repeatCount;
        f_startDirection = f_currentDirection = startDirection;
        f_timingSource = timingSource;
        f_disposeTimingSource = disposeTimingSource;
    }

    /**
     * Adds a {@link TimingTarget} to the list of targets that get notified of
     * each timing event while the animation is running. <p> This can be done at
     * any time before, during, or after the animation has started or completed;
     * the new target will begin having its methods called as soon as it is
     * added. <p> {@link TimingTarget}s will be called in the order they are
     * added.
     *
     * @param target a {@link TimingTarget} object.
     */
    public void addTarget(TimingTarget target) {
        if (target != null) {
            f_targets.add(target);
        }
    }

    /**
     * Removes the specified {@link TimingTarget} from the list of targets that
     * get notified of each timing event while the animation is running. <p>
     * This can be done at any time before, during, or after the animation has
     * started or completed; the target will cease having its methods called as
     * soon as it is removed.
     *
     * @param target a {@link TimingTarget} object.
     */
    public void removeTarget(TimingTarget target) {
        f_targets.remove(target);
    }

    /**
     * Removes all of the elements from from the list of targets that get
     * notified of each timing event while the animation is running. <p> The set
     * of registered {@link TimingTarget} objects will be empty after this call
     * returns.
     */
    public void clearTargets() {
        f_targets.clear();
    }

    /**
     * Starts the animation.
     *
     * @throws IllegalStateException if animation is already running; this
     * command may only be run prior to starting the animation or after the
     * animation has ended.
     */
    public void start() {
        startHelper(f_startDirection, "start()");
    }

    /**
     * Starts the animation in the reverse direction.
     *
     * @throws IllegalStateException if animation is already running; this
     * command may only be run prior to starting the animation or after the
     * animation has ended.
     */
    public void startReverse() {
        startHelper(f_startDirection.getOppositeDirection(), "startReverse()");
    }

    /**
     * Returns whether this has been started and has not yet completed. An
     * animation is running from when it is started via a call to
     * {@link #start()} or {@link #startReverse()} and when it (a) completes
     * normally, (b) {@link #stop()} is called on it and all callbacks to
     * registered {@link TimingTarget}s have completed, or (c) {@link #cancel()}
     * is called on it. <p> A paused animation is still considered to be
     * running.
     *
     * @return {@code true} if the animation is running, {@code false} if it is
     * not.
     */
    public boolean isRunning() {
        synchronized (f_targets) {
            return f_runningAnimationLatch != null;
        }
    }

    /**
     * Returns the current direction of the animation. If the animation is not
     * running then the value returned will be the starting direction of the
     * animation.
     *
     * @return the current direction of the animation.
     */
    public Direction getCurrentDirection() {
        synchronized (f_targets) {
            return f_currentDirection;
        }
    }

    /**
     * Clients may invoke this method to stop a running animation, however, most
     * animations will stop on their own. If the animation is not running, or is
     * stopping, then this method returns {@code false}. <p> This call will
     * result in calls to the {@link TimingTarget#end(Animator)} method of all
     * the registered timing targets of this animation. <p> The animation may
     * take some period of time to actually stop, it waits for all calls to
     * registered {@link TimingTarget}s to complete. Invoking {@link #await()}
     * after this method will wait until the animation stops. This means that
     * the code snippet "{@code a.stop(); a.start();}" could fail throwing an
     * {@link IllegalStateException} at the call to {@link #start()} because the
     * animation may not have stopped right away. This can be fixed by inserting
     * a call to {@link #await()} or using the snippet "
     * {@code a.stopAndAwait(); a.start();}" instead.
     *
     * @return {@code true} if the animation was running and was successfully
     * stopped, {@code false} if the animation was not running or was in the
     * process of stopping and didn't need to be stopped.
     *
     * @see #await()
     * @see #stopAndAwait()
     * @see #cancel()
     */
    public boolean stop() {
        return stopHelper(true);
    }

    /**
     * A convenience method that is equivalent to the code below.
     *
     * <pre>
     * a.stop();
     * try {
     *   a.await();
     * } catch (InterruptedException ignore) {
     * }
     * </pre>
     *
     * {@code a} is the animator this method is invoked on.
     */
    public void stopAndAwait() {
        stop();
        try {
            await();
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * This method is like the {@link #stop} method, only this one will not
     * result in a calls to the {@link TimingTarget#end(Animator)} method of all
     * the registered timing targets of this animation; it simply stops the
     * animation immediately and returns. If the animation is not running, or is
     * stopping, then this method returns {@code false}. <p> The animation may
     * take some period of time to actually stop, it waits for all calls to
     * registered {@link TimingTarget}s to complete. Invoking {@link #await()}
     * after this method will wait until the animation stops. This means that
     * the code snippet "{@code a.cancel(); a.start();}" could fail throwing an
     * {@link IllegalStateException} at the call to {@link #start()} because the
     * animation may not have stopped right away. This can be fixed by inserting
     * a call to {@link #await()} or using the snippet "
     * {@code a.cancelAndAwait(); a.start();}" instead.
     *
     * @return {@code true} if the animation was running and was successfully
     * stopped, {@code false} if the animation was not running or was in the
     * process of stopping and didn't need to be stopped.
     *
     * @see #await()
     * @see #cancelAndAwait()
     * @see #stop()
     */
    public boolean cancel() {
        return stopHelper(false);
    }

    /**
     * A convenience method that is equivalent to the code below.
     *
     * <pre>
     * a.cancel();
     * try {
     *   a.await();
     * } catch (InterruptedException ignore) {
     * }
     * </pre>
     *
     * {@code a} is the animator this method is invoked on.
     */
    public void cancelAndAwait() {
        cancel();
        try {
            await();
        } catch (InterruptedException ignore) {
        }
    }

    /**
     * This method pauses a running animation. No further events are sent to
     * registered timing targets. A paused animation may be started again by
     * calling the {@link #resume} method. <p> Pausing a non-running, stopping,
     * or already paused animation has no effect.
     *
     * @see #resume()
     * @see #isRunning()
     * @see #isPaused()
     */
    public void pause() {
        synchronized (f_targets) {
            final boolean canPause = isRunning() && !f_stopping && f_pauseBeginTimeNanos == 0;
            if (canPause) {
                f_timingSource.removeTickListener(this);
                f_pauseBeginTimeNanos = System.nanoTime();
            }
        }
    }

    /**
     * Returns whether this animation is currently running &mdash; but paused.
     * If the animation is not running or is in the process of stopping
     * {@code false} is returned.
     *
     * @return {@code true} if the animation is currently running &mdash; but
     * paused, {@code false} otherwise.
     */
    public boolean isPaused() {
        synchronized (f_targets) {
            return isRunning() && !f_stopping && f_pauseBeginTimeNanos > 0;
        }
    }

    /**
     * This method resumes a paused animation. Resuming an animation that is not
     * paused has no effect.
     *
     * @see #pause()
     */
    public void resume() {
        synchronized (f_targets) {
            final boolean paused = isPaused();
            if (paused) {
                long pauseDeltaNanos = System.nanoTime() - f_pauseBeginTimeNanos;
                f_startTimeNanos += pauseDeltaNanos;
                f_cycleStartTimeNanos += pauseDeltaNanos;
                f_pauseBeginTimeNanos = 0;
                f_timingSource.addTickListener(this);
            }
        }
    }

    /**
     * Reverses the direction of the animation if it is running and is not
     * paused or stopping. If it is not possible to reverse the animation now,
     * the method returns {@code false}. <p> The actual reverse occurs at the
     * next tick of this animation's {@link TimingSource}. All calls are
     * remembered, however, so no successful reversals are lost.
     *
     * @return {@code true} if the animation was successfully reversed,
     * {@code false} if the attempt to reverse the animation failed.
     */
    public boolean reverseNow() {
        synchronized (f_targets) {
            final boolean canReverse = isRunning() && !f_stopping && f_pauseBeginTimeNanos == 0;
            if (canReverse) {
                f_reverseNowCallCount++;
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Causes the current thread to wait until the animation completes, either
     * on its own or due to a call to {@link #stop()} or {@link #cancel()},
     * unless the thread is {@linkplain Thread#interrupt interrupted}. All
     * callbacks to registered {@link TimingTarget}s have been completed when
     * this method returns (unless, as noted above, the thread is
     * {@linkplain Thread#interrupt interrupted}). <p> If the animation is not
     * running then this method returns immediately. <p> If the current thread:
     * <ul> <li>has its interrupted status set on entry to this method; or
     * <li>is {@linkplain Thread#interrupt interrupted} while waiting, </ul>
     * then {@link InterruptedException} is thrown and the current thread's
     * interrupted status is cleared.
     *
     * @throws InterruptedException if the current thread is interrupted while
     * waiting.
     */
    public void await() throws InterruptedException {
        final CountDownLatch latch;
        synchronized (f_targets) {
            latch = f_runningAnimationLatch;
        }
        if (latch != null) {
            latch.await();
        }
    }

    /**
     * Returns the elapsed time in nanoseconds for the current animation
     * cycle.Uses {@link System#nanoTime()} to get the current time.
     *
     * @return the time elapsed in nanoseconds between the time the current
     * animation cycle started and the current time.
     */
    public long getCycleElapsedTime() {
        return getCycleElapsedTime(System.nanoTime());
    }

    /**
     * Returns the elapsed time in nanoseconds for the current animation cycle.
     *
     * @param currentTimeNanos value of current time, from
     * {@link System#nanoTime()}, to use in calculating the elapsed time.
     * @return the time elapsed in nanoseconds between the time this cycle
     * started and the passed time.
     */
    public long getCycleElapsedTime(long currentTimeNanos) {
        synchronized (f_targets) {
            return (currentTimeNanos - f_cycleStartTimeNanos);
        }
    }

    /**
     * Returns the total elapsed time in nanoseconds for the current animation.
     * Uses {@link System#nanoTime()} to get the current time.
     *
     * @return the total time elapsed in nanoseconds between the time this
     * animation started and the current time.
     */
    public long getTotalElapsedTime() {
        return getTotalElapsedTime(System.nanoTime());
    }

    /**
     * Returns the total elapsed time in nanoseconds for the current animation.
     *
     * @param currentTimeNanos value of current time, from
     * {@link System#nanoTime()}, to use in calculating elapsed time.
     * @return the total time elapsed between the time the Animator started and
     * the passed time.
     */
    public long getTotalElapsedTime(long currentTimeNanos) {
        synchronized (f_targets) {
            return (currentTimeNanos - f_startTimeNanos);
        }
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(Animator.class.getSimpleName()).append('@');
        b.append(f_debugName != null ? f_debugName : Integer.toHexString(hashCode()));
        b.append("(duration=").append(f_duration).append(' ').append(f_durationTimeUnit.toString());
        b.append(", interpolator=").append(getInterpolator().toString());
        b.append(", startDirection=").append(f_startDirection.toString());
        b.append(", repeatBehavior=").append(f_repeatBehavior.toString());
        b.append(", repeatCount=").append(f_repeatCount);
        b.append(", endBehavior=").append(f_endBehavior.toString());
        b.append(", timingSource=").append(f_timingSource.toString());
        b.append(')');
        return b.toString();
    }

    /**
     * Factors out common code between {@link #start()} and
     * {@link #startReverse()}.
     *
     * @param direction the direction to start the animation going in.
     * @param methodName the short name of the calling method, used only for
     * error reporting.
     *
     * @throws IllegalStateException if the this animation is already running.
     */
    private void startHelper(Direction direction, String methodName) {
        synchronized (f_targets) {
            if (isRunning()) {
                throw new IllegalStateException("");
            }

            f_startTimeNanos = f_cycleStartTimeNanos = System.nanoTime();
            f_currentDirection = direction;
            f_stopping = false;
            f_pauseBeginTimeNanos = f_reverseNowCallCount = 0;
            f_runningAnimationLatch = new CountDownLatch(1);
            /*
             * Because the submit() call only places the Runnable into a queue holding
             * the lock below cannot lead to a deadlock.
             * 
             * Holding the lock is not really necessary, but it makes this code
             * similar to reverseNow() (where holding the lock is critical to correct
             * behavior).
             */
            if (!f_targets.isEmpty()) {
                final Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        for (TimingTarget target : f_targets) {
                            target.begin(Animator.this);
                        }
                    }
                };
                f_timingSource.submit(task);
            }
        }
        f_timingSource.addTickListener(this);
    }

    /**
     * Helper routine to stop the running animation. It optionally invokes the
     * {@link TimingTarget#end(Animator)} method of registered timing targets in
     * the correct thread context. If the animation was not running (or is
     * already stopping) then this method returns {@code false}.
     *
     * @param notify {@code true} if the {@link TimingTarget#end(Animator)}
     * method should be called for registered timing targets, {@code false} if
     * calls should not be made.
     *
     * @return {@code true} if the animation was running and was successfully
     * stopped, {@code false} if the animation was not running or was in the
     * process of stopping and didn't need to be stopped.
     */
    private boolean stopHelper(final boolean notify) {
        synchronized (f_targets) {
            /*
             * If we are not running at all we return immediately.
             */
            if (f_runningAnimationLatch == null) {
                return false;
            }
            /*
             * If we are already stopping we return immediately.
             */
            if (f_stopping) {
                return false;
            }

            f_stopping = true;
        }
        f_timingSource.removeTickListener(this);
        final Runnable task = new Runnable() {
            @Override
            public void run() {
                try {
                    if (f_disposeTimingSource) {
                        f_timingSource.dispose();
                    }
                    if (notify) {
                        for (TimingTarget target : f_targets) {
                            target.end(Animator.this);
                        }
                    }
                } finally {
                    latchCountDown();
                }
            }
        };
        f_timingSource.submit(task);
        return true;
    }

    /**
     * Helper routine to trip the latch after all callbacks have finished up
     * that need to finish up. <p> {@link #f_targets} should NOT be held when
     * invoking this method.
     */
    private void latchCountDown() {
        final CountDownLatch latch;
        synchronized (f_targets) {
            latch = f_runningAnimationLatch;
            f_runningAnimationLatch = null;
        }
        latch.countDown();
    }

    /**
     * Not intended for use by client code.
     */
    @Override
    public void timingSourceTick(TimingSource source, long nanoTime) {
        /*
         * Implementation note: This is a big method, however, breaking it up
         * requires the introduction of several fields that are really
         * implementation details of the calculations below and flags about what to
         * do next.
         */

        final double fraction;
        boolean timeToStop = false;
        boolean notifyRepeat = false;
        boolean notifyOfReverse = false;
        synchronized (f_targets) {
            /*
             * A guard against running logic within this method if any of the
             * following conditions are true:
             * 
             * o The animation is not running
             * 
             * o The animation is stopping
             * 
             * o The animation is paused
             */
            final boolean skipTick = f_runningAnimationLatch == null || f_stopping || f_pauseBeginTimeNanos != 0;
            if (skipTick) {
                return;
            }

            /*
             * Note that we need to notify of a reverseNow() call and reset the field.
             */
            if (f_reverseNowCallCount > 0) {
                notifyOfReverse = true;
                final boolean reverseCallsCancelOut = /* isEven */ (f_reverseNowCallCount & 1) == 0;
                f_reverseNowCallCount = 0;

                if (!reverseCallsCancelOut) {
                    final long cycleElapsedTimeNanos = getCycleElapsedTime(nanoTime);
                    final long durationNanos = f_durationTimeUnit.toNanos(f_duration);
                    final long timeLeft = durationNanos - cycleElapsedTimeNanos;
                    final long deltaNanos = (nanoTime - timeLeft) - f_cycleStartTimeNanos;
                    f_cycleStartTimeNanos += deltaNanos;
                    f_startTimeNanos += deltaNanos;
                    f_currentDirection = f_currentDirection.getOppositeDirection();
                }
            }

            /*
             * This code calculates and returns the fraction elapsed of the current
             * cycle based on the current time and the {@link Interpolator} used by
             * the animation.
             */
            final long cycleElapsedTimeNanos = getCycleElapsedTime(nanoTime);
            final long totalElapsedTimeNanos = getTotalElapsedTime(nanoTime);
            final long durationNanos = f_durationTimeUnit.toNanos(f_duration);
            final long currentCycleCount = totalElapsedTimeNanos / durationNanos;

            double fractionScratch;

            if (f_repeatCount != INFINITE && currentCycleCount >= f_repeatCount) {
                /*
                 * Animation End: Stop based on specified end behavior.
                 */
                switch (f_endBehavior) {
                    case HOLD:
                        /*
                         * HOLD requires setting the final end value.
                         */
                        if (f_currentDirection == Direction.BACKWARD) {
                            fractionScratch = 0;
                        } else {
                            fractionScratch = 1;
                        }
                        break;
                    case RESET:
                        /*
                         * RESET requires setting the final value to the start value.
                         */
                        fractionScratch = 0;
                        break;
                    default:
                        throw new IllegalStateException(f_endBehavior.toString());
                }
                timeToStop = true;
            } else if (cycleElapsedTimeNanos > durationNanos) {
                /*
                 * Animation Cycle End: Time to stop or change the behavior of the
                 * timer.
                 */
                final long overCycleTimeNanos = cycleElapsedTimeNanos % durationNanos;
                fractionScratch = (double) overCycleTimeNanos / durationNanos;
                /*
                 * Set a new start time for this cycle.
                 */
                f_cycleStartTimeNanos = nanoTime - overCycleTimeNanos;

                if (f_repeatBehavior == RepeatBehavior.REVERSE) {
                    /*
                     * Reverse the direction of the animation.
                     */
                    f_currentDirection = f_currentDirection.getOppositeDirection();
                }
                if (f_currentDirection == Direction.BACKWARD) {
                    fractionScratch = 1 - fractionScratch;
                }
                notifyRepeat = true;
            } else {
                /*
                 * Animation Mid-Stream: Calculate fraction of animation between start
                 * and end times and send fraction to target.
                 */
                fractionScratch = (double) cycleElapsedTimeNanos / (double) durationNanos;
                if (f_currentDirection == Direction.BACKWARD) {
                    /*
                     * If this is a backwards cycle, want to send the inverse fraction;
                     * how much from start to finish, not finish to start.
                     */
                    fractionScratch = 1.0 - fractionScratch;
                }
                /*
                 * Clamp fraction in case timing mechanism caused out of bounds value.
                 */
                fractionScratch = Math.min(fractionScratch, 1.0);
                fractionScratch = Math.max(fractionScratch, 0.0);
            }
            fraction = f_interpolator == null ? fractionScratch : 0;// f_interpolator.interpolate(fractionScratch);
        } // lock release

        if (notifyOfReverse && !f_targets.isEmpty()) {
            for (TimingTarget target : f_targets) {
                target.reverse(this);
            }
        }
        if (notifyRepeat && !f_targets.isEmpty()) {
            for (TimingTarget target : f_targets) {
                target.repeat(this);
            }
        }
        if (!f_targets.isEmpty()) {
            for (TimingTarget target : f_targets) {
                target.timingEvent(this, fraction);
            }
        }
        if (timeToStop) {
            stopHelper(true);
        }
    }
}
