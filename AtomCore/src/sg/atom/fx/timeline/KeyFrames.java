package sg.atom.fx.timeline;

import de.lessvoid.nifty.tools.LinearInterpolator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicInteger;

import sg.atom.fx.automatic.KnownEvaluators;
import sg.atom.fx.tween.Evaluator;
import sg.atom.fx.tween.Interpolator;

/**
 * This class manages a list of key frames to animate values via interpolation
 * between a series of key values at key times. It holds information about the
 * times at which values are sampled and the values at those times. It also
 * holds information about how to interpolate between these values for times
 * that lie between the sampling points. <p> Client code should <i>never</i>
 * mutate values that have been passed into a key frames instance. The behavior
 * of this implementation is undefined if values are mutate by client code.
 *
 * @param <T> the type of the values.
 *
 * @author Chet Haase
 * @author Tim Halloran
 */
public class KeyFrames<T> implements Iterable<Frame<T>> {

    /**
     * This class is used to construct {@link KeyFrames} instances. <p>
     * Instances of this class are not thread safe and are intended to be
     * thread-confined. However, the {@link KeyFrames} objects produces are
     * thread-safe. <p> Client code should <i>never</i> mutate values that have
     * been passed into a key frames instance. The behavior of the constructed
     * key frames instance is undefined if its values are mutate by client code.
     *
     * @param <T> the type of the values the {@link KeyFrames} instance
     * constructed by this builder will hold.
     *
     * @author Tim Halloran
     */
    public static class Builder<T> {

        private Evaluator<T> f_evaluator = null;
        private final List<T> f_values = new ArrayList<T>();
        private final LinkedList<Double> f_timeFractions = new LinkedList<Double>();
        private final List<Interpolator> f_interpolators = new ArrayList<Interpolator>();
        private Interpolator f_interpolator = null;

        /**
         * Constructs an key frames builder instance.
         */
        public Builder() {
            // Nothing to do
        }

        /**
         * Constructs an key frames builder instance and specifies the first, or
         * starting, key frame.
         *
         * @param startValue the key frame value at zero. Client code should
         * <i>never</i> mutate values that have been passed into a key frames
         * instance. The behavior of the constructed key frames instance is
         * undefined if its values are mutate by client code.
         */
        public Builder(T startValue) {
            f_values.add(startValue);
            f_timeFractions.add(Double.valueOf(0));
            f_interpolators.add(null);
        }

        /**
         * Adds a frame to the list of key frames being built. <p> The time
         * fraction when this fame occurs will be calculated, linearly, from the
         * previous and next specified time fractions. <p> The interpolator
         * between the previous key frame and the one being added is set with
         * {@link #setInterpolator(Interpolator)} or the default
         * {@link LinearInterpolator} will be used. The first key frame does not
         * have an interpolator.
         *
         * @param value the value for the key frame. Client code should
         * <i>never</i> mutate values that have been passed into a key frames
         * instance. The behavior of the constructed key frames instance is
         * undefined if its values are mutate by client code.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> addFrame(T value) {
            f_values.add(value);
            f_timeFractions.add(null);
            f_interpolators.add(null);
            return this;
        }

        /**
         * Adds a frame to the list of key frames being built. <p> The
         * interpolator between the previous key frame and the one being added
         * is set with {@link #setInterpolator(Interpolator)} or the default
         * {@link LinearInterpolator} will be used. The first key frame does not
         * have an interpolator.
         *
         * @param value the value for the key frame. Client code should
         * <i>never</i> mutate values that have been passed into a key frames
         * instance. The behavior of the constructed key frames instance is
         * undefined if its values are mutate by client code.
         * @param atTimeFraction the time fraction in the range [0,1] when the
         * value should occur. A negative value indicates that the time fraction
         * when this fame occurs should be calculated, linearly, from the
         * previous and next specified time fractions.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> addFrame(T value, double atTimeFraction) {
            f_values.add(value);
            f_timeFractions.add(atTimeFraction);
            f_interpolators.add(null);
            return this;
        }

        /**
         * Adds a frame to the list of key frames being built. <p> The time
         * fraction when this fame occurs will be calculated, linearly, from the
         * previous and next specified time fractions.
         *
         * @param value the value for the key frame. Client code should
         * <i>never</i> mutate values that have been passed into a key frames
         * instance. The behavior of the constructed key frames instance is
         * undefined if its values are mutate by client code.
         * @param interpolator the interpolator that should be used between the
         * previous key frame and the one being added. A {@code null} value
         * indicates that either the interpolator set with
         * {@link #setInterpolator(Interpolator)} or the default
         * {@link LinearInterpolator} should be used for this key frame. The
         * first key frame does not have an interpolator&mdash;if set, it will
         * be ignored.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> addFrame(T value, Interpolator interpolator) {
            f_values.add(value);
            f_timeFractions.add(null);
            f_interpolators.add(interpolator);
            return this;
        }

        /**
         * Adds a frame to the list of key frames being built.
         *
         * @param value the value for the key frame. Client code should
         * <i>never</i> mutate values that have been passed into a key frames
         * instance. The behavior of the constructed key frames instance is
         * undefined if its values are mutate by client code.
         * @param atTimeFraction the time fraction in the range [0,1] when the
         * value should occur. A negative value indicates that the time fraction
         * when this fame occurs should be calculated, linearly, from the
         * previous and next specified time fractions.
         * @param interpolator the interpolator that should be used between the
         * previous key frame and the one being added. A {@code null} value
         * indicates that either the interpolator set with
         * {@link #setInterpolator(Interpolator)} or the default
         * {@link LinearInterpolator} should be used for this key frame. The
         * first key frame does not have an interpolator&mdash;if set, it will
         * be ignored.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> addFrame(T value, double atTimeFraction, Interpolator interpolator) {
            f_values.add(value);
            f_timeFractions.add(atTimeFraction);
            f_interpolators.add(interpolator);
            return this;
        }

        /**
         * Adds a frame to the list of key frames being built.
         *
         * @param frame a frame.
         * @return this builder (to allow chained operations).
         *
         * @see Frame
         *
         * @throws IllegalArgumentException if <tt>frame</tt> is {@code null}.
         */
        public Builder<T> addFrame(Frame<T> frame) {
            if (frame == null) {
                throw new IllegalArgumentException("");
            }
            f_values.add(frame.getValue());
            f_timeFractions.add(frame.getTimeFraction() < 0 ? null : frame.getTimeFraction());
            f_interpolators.add(frame.getInterpolator());
            return this;
        }

        /**
         * Adds a list of frames to the list of key frames being built. <p> This
         * is a convenience method that invokes {@link #addFrame(Object)} for
         * each of the passed values.
         *
         * @param values a series values. Client code should <i>never</i> mutate
         * values that have been passed into a key frames instance. The behavior
         * of the constructed key frames instance is undefined if its values are
         * mutate by client code.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> addFrames(T... values) {
            return addFrames(Arrays.asList(values));
        }

        /**
         * Adds a list of frames to the list of key frames being built. <p> This
         * is a convenience method that invokes {@link #addFrame(Object)} for
         * each of the passed values.
         *
         * @param values a series values. Client code should <i>never</i> mutate
         * values that have been passed into a key frames instance. The behavior
         * of the constructed key frames instance is undefined if its values are
         * mutate by client code.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> addFrames(List<T> values) {
            for (T value : values) {
                addFrame(value);
            }
            return this;
        }

        /**
         * Sets the global interpolator to be used for the list of key frames
         * being built. This value will override any interpolators set on
         * individual frames. <p> A value of {@code null} will clear the global
         * interpolator, if any, that was previously set via a call to this
         * method and use the interpolators set on individual frames.
         *
         * @param interpolator a global interpolator, or {@code null} to clear
         * any previously set global interpolator.
         * @return this builder (to allow chained operations).
         */
        public Builder<T> setInterpolator(Interpolator interpolator) {
            f_interpolator = interpolator;
            return this;
        }

        /**
         * Sets the evaluator between values for the list of key frames being
         * built. <p> Typically, this method does not need to be called because
         * {@link #build()} obtains an {@link Evaluator} instance by examining
         * the type of the values the {@link KeyFrames} instance constructed by
         * this builder will hold and calling
         * {@link KnownEvaluators#getEvaluatorFor(Class)}. <p> If the type of
         * the values the {@link KeyFrames} instance constructed by this builder
         * will hold is not known, then you will have to implement a custom
         * evaluator for that type and pass it to this method&mdash;or to
         * {@link KnownEvaluators#register(Evaluator)}. <p> A value of
         * {@code null} will clear the evaluator, if any, that was previously
         * set via a call to this method and have {@link #build()} invoke
         * {@link KnownEvaluators#getEvaluatorFor(Class)} to obtain an
         * {@link Evaluator} instance.
         *
         * @param evaluator an evaluator, or {@code null} to clear any
         * previously set evaluator.
         * @return this builder (to allow chained operations).
         *
         * @see KnownEvaluators
         */
        public Builder<T> setEvaluator(Evaluator<T> evaluator) {
            f_evaluator = evaluator;
            return this;
        }

        /**
         * Constructs a key frames instance with the settings defined by this
         * builder.
         *
         * @return a key frames instance.
         *
         * @throws IllegalArgumentException if the settings defined by this
         * builder are invalid and are not conducive to the construction of a
         * valid key frames instance.
         */
        public KeyFrames<T> build() {
            final int frameCount = f_values.size();
            /*
             * There must be at least two frames and the lists must be the same size.
             */
            if (frameCount < 2) {
                throw new IllegalArgumentException("");
            }
            if (f_timeFractions.size() != frameCount) {
                throw new IllegalArgumentException("");
            }
            if (f_interpolators.size() != frameCount) {
                throw new IllegalArgumentException("");
            }
            /*
             * Change the first key time to zero, unless it already is zero.
             */
            if (f_timeFractions.getFirst() == null || f_timeFractions.getFirst() != 0) {
                f_timeFractions.removeFirst();
                f_timeFractions.addFirst(Double.valueOf(0));
            }
            /*
             * Change the last key time to one, unless it already is one.
             */
            if (f_timeFractions.getLast() == null || f_timeFractions.getLast() != 1) {
                f_timeFractions.removeLast();
                f_timeFractions.addLast(Double.valueOf(1));
            }
            /*
             * For any unspecified (null) time fractions we compute a linear
             * interpolated value from the previous and next specified fractions.
             */
            List<Integer> fillList = new ArrayList<Integer>();
            double prev = -1;
            for (int i = 0; i < f_timeFractions.size(); i++) {
                Double curr = f_timeFractions.get(i);
                if (curr == null) {
                    if (prev == -1) {
                        throw new IllegalArgumentException("Time fraction of the first key frame is null, it should be zero");
                    }

                    fillList.add(i);
                } else {
                    if (!fillList.isEmpty()) {
                        final double delta = (curr.doubleValue() - prev) / (fillList.size() + 1);
                        int count = 1;
                        for (int j : fillList) {
                            final double timeFraction = prev + (count * delta);
                            f_timeFractions.set(j, timeFraction);
                            count++;
                        }
                        fillList.clear();
                    }
                    prev = curr.doubleValue();
                }
            }

            /*
             * Construct an array of frames and perform null checks.
             */
            @SuppressWarnings("unchecked")
            final Frame<T>[] frames = new Frame[frameCount];
            for (int i = 0; i < frameCount; i++) {
                final T value = f_values.get(i);
                if (value == null) {
                    throw new IllegalArgumentException("");
                }

                final Double atTimeFraction = f_timeFractions.get(i);
                if (atTimeFraction == null) {
                    throw new IllegalArgumentException("");
                }

                final Interpolator interpolator;
                if (i == 0) {
                    interpolator = null;
                } else {
                    final Interpolator canidate = f_interpolator == null ? f_interpolators.get(i) : f_interpolator;
                    interpolator = canidate;// == null ? LinearInterpolator.getInstance() : canidate;
                }

                frames[i] = new Frame<T>(value, atTimeFraction, interpolator);
            }

            /*
             * Check that key times are less than one and that they increase.
             */
            double prevTime = 0;
            for (Frame<T> frame : frames) {
                final double atTime = frame.getTimeFraction();
                if (atTime < prevTime) {
                    throw new IllegalArgumentException(f_timeFractions.toString());
                }

                prevTime = atTime;
            }

            /*
             * Try to find an evaluator unless one was set.
             */
            if (f_evaluator == null) {
                @SuppressWarnings("unchecked")
                final Class<T> c = (Class<T>) frames[0].getValue().getClass();
                f_evaluator = KnownEvaluators.getInstance().getEvaluatorFor(c);
            }

            final KeyFrames<T> result = new KeyFrames<T>(frames, f_evaluator);
            return result;
        }
    }
    /**
     * The ordered list of key frames managed by this instance.
     */
    private final Frame<T>[] f_frames;
    /**
     * Used to evaluates between two key frame values.
     */
    private final Evaluator<T> f_evaluator;

    /**
     * Constructs a key frames instance. <p> This constructor should only be
     * called from {@link KeyFrames.Builder#build()}.
     */
    KeyFrames(Frame<T>[] frames, Evaluator<T> evaluator) {
        f_frames = frames;
        f_evaluator = evaluator;
    }

    /**
     * Gets the number of key frames contained in this list. The returned value
     * is never less that two.
     *
     * @return the number of key frames in this list.
     */
    public int size() {
        return f_frames.length;
    }

    /**
     * Returns the key frame at the specified position in this list.
     *
     * @param index index of the key frame to return.
     * @return the key frame at the specified position in this list.
     * @throws IndexOutOfBoundsException if the index is out of range (
     * <tt>index &lt; 0 || index &gt;= size()</tt>)
     */
    public Frame<T> getFrame(int index) {
        return f_frames[index];
    }

    public Iterator<Frame<T>> iterator() {
        class It implements Iterator<Frame<T>> {

            final AtomicInteger f_index = new AtomicInteger(0);

            public It() {
                super();
            }

            public boolean hasNext() {
                return f_index.get() < size();
            }

            public Frame<T> next() {
                if (!hasNext()) {
                    throw new NoSuchElementException("No further key frames exist.");
                }
                final int index = f_index.getAndIncrement();
                final Frame<T> result = f_frames[index];
                return result;
            }

            public void remove() {
                throw new UnsupportedOperationException("Removal of a key frame is not supported.");
            }
        }

        final Iterator<Frame<T>> result = new It();
        return result;
    }

    /**
     * Returns interval of time, 0 to {@link KeyFrames#size()} - 2, that
     * contains the passed time fraction based upon the list of key frames
     * managed by this instance. The return value is the index of the key frame
     * closest to, but not after, the passed time fraction. More precisely, the
     * returned interval is <i>i</i> if <tt>fraction</tt> is within the range (
     * <tt>getFrame(</tt> <i>i</i><tt>).getTimeFraction()</tt>,
     * <tt>getFrame(</tt> <i>i</i> <tt>+1).getTimeFraction()</tt>] unless
     * <i>i</i>=0 in which case <tt>fraction</tt> is within the range [0,
     * <tt>getFrame(</tt><i>i</i> <tt>+1).getTimeFraction()</tt>] (i.e., zero
     * inclusive). <p> For example, consider the following instance (ignoring
     * the integer key frame values):
     *
     * <pre>
     * KeyFrames.Builder&lt;Integer&gt; builder = new KeyFrames.Builder&lt;Integer&gt;(1);
     * builder.addFrame(2, 0.1); // addFrame(value, atTimeFraction)
     * builder.addFrame(3, 0.2);
     * builder.addFrame(4, 0.5);
     * builder.addFrame(5, 1);
     * KeyFrames&lt;Integer&gt; k = builder.build();
     * </pre>
     *
     * The table below shows the results obtained from calls on this instance.
     * <p> <table border="1"> <tr> <th><i>f<i></th>
     * <th><i>i</i><tt>=k.getFrameIndexAt(</tt><i>f</i><tt>)</tt></th>
     * <th><tt>k.getFrame(</tt><i>i</i><tt>).getTimeFraction()</tt></th>
     * <th><tt>k.getFrame(</tt><i>i</i><tt>+1).getTimeFraction()</tt></th> </tr>
     * <tr> <td align="right">-1&dagger;</td> <td align="right">0</td> <td
     * align="right">0.0</td> <td align="right">0.1</td> </tr> <tr> <td
     * align="right">0</td> <td align="right">0</td> <td align="right">0.0</td>
     * <td align="right">0.1</td> </tr> <tr> <td align="right">0.1</td> <td
     * align="right">0</td> <td align="right">0.0</td> <td
     * align="right">0.1</td> </tr> <tr> <td align="right">0.11</td> <td
     * align="right">1</td> <td align="right">0.1</td> <td
     * align="right">0.2</td> </tr> <tr> <td align="right">0.2</td> <td
     * align="right">1</td> <td align="right">0.1</td> <td
     * align="right">0.2</td> </tr> <tr> <td align="right">0.34</td> <td
     * align="right">2</td> <td align="right">0.2</td> <td
     * align="right">0.5</td> </tr> <tr> <td align="right">0.5</td> <td
     * align="right">2</td> <td align="right">0.2</td> <td
     * align="right">0.5</td> </tr> <tr> <td align="right">0.6</td> <td
     * align="right">3</td> <td align="right">0.5</td> <td
     * align="right">1.0</td> </tr> <tr> <td align="right">1</td> <td
     * align="right">3</td> <td align="right">0.5</td> <td
     * align="right">1.0</td> </tr> <tr> <td align="right">2&dagger;</td> <td
     * align="right">3</td> <td align="right">0.5</td> <td
     * align="right">1.0</td> </tr> </table>
     *
     * &dagger; The first and the last entries, -1 and 2, are outside the range
     * [0,1], however the implementation clamps them to [0,1] and returns a
     * valid result.
     *
     * @param fraction a time fraction in the range [0,1].
     * @return the index of the key frame closest to, but not after, the passed
     * time fraction.
     */
    public int getFrameIndexAt(double fraction) {
        final int size = size();
        for (int i = 1; i < size; ++i) {
            if (fraction <= f_frames[i].getTimeFraction()) {
                return i - 1;
            }
        }
        return size - 2;
    }

    /**
     * Gets the interpolated value at the passed time fraction based upon the
     * list of key frames managed by this instance. The returned value is
     * calculated by the key frames' {@link Evaluator} using the two key frames
     * <tt>fraction</tt> lies between and the {@link Interpolator} set for that
     * interval of time.
     *
     * @param fraction a time fraction in the range [0,1].
     * @return the evaluated value at the passed time fraction.
     */
    public T getInterpolatedValueAt(double fraction) {
        final int interval = getFrameIndexAt(fraction);
        /*
         * First, figure out the real fraction to use, given the interpolation type
         * and start and end time of the interval.
         */
        final double t0 = f_frames[interval].getTimeFraction();
        final double t1 = f_frames[interval + 1].getTimeFraction();
        final double t = (fraction - t0) / (t1 - t0);
        double iFraction = 1;
        //FIXME: Changed
        //f_frames[interval + 1].getInterpolator().interpolate(t);
        /*
         * Clamp to [0,1] to any avoid problems with buggy interpolators.
         */
        if (iFraction < 0) {
            iFraction = 0;
        } else if (iFraction > 1) {
            iFraction = 1;
        }
        /*
         * Second evaluate between the two key values.
         */
        final T v0 = f_frames[interval].getValue();
        final T v1 = f_frames[interval + 1].getValue();
        return f_evaluator.evaluate(v0, v1, iFraction);
    }
}
