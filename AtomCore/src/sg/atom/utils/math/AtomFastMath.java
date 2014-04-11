package sg.atom.utils.math;

import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Spline;
import com.jme3.math.Transform;
import com.jme3.math.Vector3f;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import sg.atom.utils.datastructure.collection.IntPair;
import sg.atom.utils.datastructure.collection.Pair;

/**
 * Utility and fast math functions for 3D.
 *
 * <p><b>DONE </b><s>Merged with MathUtils.</s>
 *
 * <p>FIXME: Replace with Common's Math!
 *
 * @author atomix
 */
public class AtomFastMath {

    public static Random random = new Random();
    public static float PI = 3.14159265358979323846f;
    public static float HALF_PI = (PI / 2);
    static public final float PI2 = PI * 2;
    static private final int SIN_BITS = 14; // 16KB. Adjust for accuracy.
    static private final int SIN_MASK = ~(-1 << SIN_BITS);
    static private final int SIN_COUNT = SIN_MASK + 1;
    static private final float radFull = PI * 2;
    static private final float degFull = 360;
    static private final float radToIndex = SIN_COUNT / radFull;
    static private final float degToIndex = SIN_COUNT / degFull;
    static public final float radiansToDegrees = 180f / PI;
    static public final float radDeg = radiansToDegrees;
    static public final float degreesToRadians = PI / 180;
    static public final float degRad = degreesToRadians;

    public static float Sq(float val) {
        return (val * val);
    }

    public static float Max(float a, float b) {
        return ((a) < (b) ? (b) : (a));
    }

    public static float Min(float a, float b) {
        return ((a) > (b) ? (b) : (a));
    }

    public static float RadToDeg(float rad) {
        return ((rad) * (180.0f / PI));
    }

    public static float DegToRad(float deg) {
        return ((deg) * (PI / 180.0f));
    }

    public static float Clamp(float val, float minVal, float maxVal) {
        return (Min(Max(minVal, val), maxVal));
    }

    public static float Lerp(float val1, float val2, float fraction) {
        return (val1 + ((val2 - val1) * fraction));
    }

    public static float Rescale(float val, float currMin, float currMax, float newMin, float newMax) {
        return (Lerp(newMin, newMax, ((val - currMin) / (currMax - currMin))));
    }
    /*
     public static float UnitRand() {return ( (float)rand()/(float)RAND_MAX );}
     */

    public static float UnitRand() {
        return random.nextFloat();
    }

    public static float RangeRand(float minVal, float maxVal) {
        return ((UnitRand() * (maxVal - minVal)) + minVal);
    }

    // aditional 3d maths
    public static Vector3f getPenVec(Vector3f vec) {
        //Plane p = new Plane(vec, point.dot(vec));
        Vector3f upVec = Vector3f.UNIT_Y;
        if (vec.normalize().angleBetween(upVec) < 0.0001f) {
            upVec = Vector3f.UNIT_X;
        } else {
            upVec = Vector3f.UNIT_Y;
        }
        Vector3f penVec = vec.cross(upVec);

        return penVec;
    }

    public static Vector3f normalCal(Vector3f a, Vector3f b, Vector3f c) {
        return a.subtract(b).cross(a.subtract(c)).normalize();
    }

    public static Transform inverseTrans(Transform t) {
        Vector3f iT = t.getTranslation().negate();
        Quaternion iQ = t.getRotation().inverse();
        Vector3f iS = Vector3f.UNIT_XYZ.divide(t.getScale());
        return new Transform(iS, iQ, iS);

    }

    public static Transform subtractTransform(Transform t1, Transform t2) {
        return t1.clone().combineWithParent(inverseTrans(t2));
    }

    public static Vector3f randomVec3(Vector3f limit) {

        float x = random.nextFloat() * limit.x;
        float y = random.nextFloat() * limit.y;
        float z = random.nextFloat() * limit.z;
        return new Vector3f(x, 0, z);
    }

    public static Vector3f randomVec3(float min, float max) {
        float limit = min - max;
        float x = min + random.nextFloat() * limit;
        float y = min + random.nextFloat() * limit;
        float z = min + random.nextFloat() * limit;
        return new Vector3f(x, 0, z);
    }

    public static Pair<Integer, Integer> randomIntPairGeneric(int min, int max) {
        return new Pair<Integer, Integer>(FastMath.nextRandomInt(min, max), FastMath.nextRandomInt(min, max));
    }

    public static IntPair randomIntPair(int min, int max) {
        return new IntPair(FastMath.nextRandomInt(min, max), FastMath.nextRandomInt(min, max));
    }

    static private class Sin {

        static final float[] table = new float[SIN_COUNT];

        static {
            for (int i = 0; i < SIN_COUNT; i++) {
                table[i] = (float) Math.sin((i + 0.5f) / SIN_COUNT * radFull);
            }
            for (int i = 0; i < 360; i += 90) {
                table[(int) (i * degToIndex) & SIN_MASK] = (float) Math.sin(i * degreesToRadians);
            }
        }
    }

    /**
     * Returns the sine in radians from a lookup table.
     */
    static public final float sin(float radians) {
        return Sin.table[(int) (radians * radToIndex) & SIN_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table.
     */
    static public final float cos(float radians) {
        return Sin.table[(int) ((radians + PI / 2) * radToIndex) & SIN_MASK];
    }

    /**
     * Returns the sine in radians from a lookup table.
     */
    static public final float sinDeg(float degrees) {
        return Sin.table[(int) (degrees * degToIndex) & SIN_MASK];
    }

    /**
     * Returns the cosine in radians from a lookup table.
     */
    static public final float cosDeg(float degrees) {
        return Sin.table[(int) ((degrees + 90) * degToIndex) & SIN_MASK];
    }
    // ---
    static private final int ATAN2_BITS = 7; // Adjust for accuracy.
    static private final int ATAN2_BITS2 = ATAN2_BITS << 1;
    static private final int ATAN2_MASK = ~(-1 << ATAN2_BITS2);
    static private final int ATAN2_COUNT = ATAN2_MASK + 1;
    static final int ATAN2_DIM = (int) Math.sqrt(ATAN2_COUNT);
    static private final float INV_ATAN2_DIM_MINUS_1 = 1.0f / (ATAN2_DIM - 1);

    static private class Atan2 {

        static final float[] table = new float[ATAN2_COUNT];

        static {
            for (int i = 0; i < ATAN2_DIM; i++) {
                for (int j = 0; j < ATAN2_DIM; j++) {
                    float x0 = (float) i / ATAN2_DIM;
                    float y0 = (float) j / ATAN2_DIM;
                    table[j * ATAN2_DIM + i] = (float) Math.atan2(y0, x0);
                }
            }
        }
    }

    /**
     * Returns atan2 in radians from a lookup table.
     */
    static public final float atan2(float y, float x) {
        float add, mul;
        if (x < 0) {
            if (y < 0) {
                y = -y;
                mul = 1;
            } else {
                mul = -1;
            }
            x = -x;
            add = -PI;
        } else {
            if (y < 0) {
                y = -y;
                mul = -1;
            } else {
                mul = 1;
            }
            add = 0;
        }
        float invDiv = 1 / ((x < y ? y : x) * INV_ATAN2_DIM_MINUS_1);

        if (invDiv == Float.POSITIVE_INFINITY) {
            return ((float) Math.atan2(y, x) + add) * mul;
        }

        int xi = (int) (x * invDiv);
        int yi = (int) (y * invDiv);
        return (Atan2.table[yi * ATAN2_DIM + xi] + add) * mul;
    }

    /**
     * Returns a random number between 0 (inclusive) and the specified value
     * (inclusive).
     */
    static public final int random(int range) {
        return random.nextInt(range + 1);
    }

    /**
     * Returns a random number between start (inclusive) and end (inclusive).
     */
    static public final int random(int start, int end) {
        return start + random.nextInt(end - start + 1);
    }

    /**
     * Returns a random boolean value.
     */
    static public final boolean randomBoolean() {
        return random.nextBoolean();
    }

    /**
     * Returns true if a random value between 0 and 1 is less than the specified
     * value.
     */
    static public final boolean randomBoolean(float chance) {
        return AtomFastMath.random() < chance;
    }

    /**
     * Returns random number between 0.0 (inclusive) and 1.0 (exclusive).
     */
    static public final float random() {
        return random.nextFloat();
    }

    /**
     * Returns a random number between 0 (inclusive) and the specified value
     * (exclusive).
     */
    static public final float random(float range) {
        return random.nextFloat() * range;
    }

    /**
     * Returns a random number between start (inclusive) and end (exclusive).
     */
    static public final float random(float start, float end) {
        return start + random.nextFloat() * (end - start);
    }

    // ---
    /**
     * Returns the next power of two. Returns the specified value if the value
     * is already a power of two.
     */
    static public int nextPowerOfTwo(int value) {
        if (value == 0) {
            return 1;
        }
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    static public boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    // ---
    static public int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public short clamp(short value, short min, short max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public float clamp(float value, float min, float max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }

    static public double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
    // ---
    static private final int BIG_ENOUGH_INT = 16 * 1024;
    static private final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    static private final double CEIL = 0.9999999;
// static private final double BIG_ENOUGH_CEIL = NumberUtils
// .longBitsToDouble(NumberUtils.doubleToLongBits(BIG_ENOUGH_INT + 1) - 1);
    static private final double BIG_ENOUGH_CEIL = 16384.999999999996;
    static private final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;

    /**
     * Returns the largest integer less than or equal to the specified float.
     * This method will only properly floor floats from -(2^14) to
     * (Float.MAX_VALUE - 2^14).
     */
    static public int floor(float x) {
        return (int) (x + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the largest integer less than or equal to the specified float.
     * This method will only properly floor floats that are positive. Note this
     * method simply casts the float to int.
     */
    static public int floorPositive(float x) {
        return (int) x;
    }

    /**
     * Returns the smallest integer greater than or equal to the specified
     * float. This method will only properly ceil floats from -(2^14) to
     * (Float.MAX_VALUE - 2^14).
     */
    static public int ceil(float x) {
        return (int) (x + BIG_ENOUGH_CEIL) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the smallest integer greater than or equal to the specified
     * float. This method will only properly ceil floats that are positive.
     */
    static public int ceilPositive(float x) {
        return (int) (x + CEIL);
    }

    /**
     * Returns the closest integer to the specified float. This method will only
     * properly round floats from -(2^14) to (Float.MAX_VALUE - 2^14).
     */
    static public int round(float x) {
        return (int) (x + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    /**
     * Returns the closest integer to the specified float. This method will only
     * properly round floats that are positive.
     */
    static public int roundPositive(float x) {
        return (int) (x + 0.5f);
    }

    public static int min(int a, int b) {
        if (a < b) {
            return a;
        }
        return b;
    }

    public static int min(int a, int b, int c) {
        if (min(a, b) < c) {
            return min(a, b);
        }
        return c;
    }

    public static int max(int a, int b) {
        if (a > b) {
            return a;
        }
        return b;
    }

    public static int pow(int a, int b) {
        if (b > 1) {
            return a * pow(a, b - 1);
        } else {
            return a;
        }
    }

    public static String roundDouble(double num, int decimal) {
        String s = "0.";
        for (int i = 0; i < decimal; i++) {
            s += "0";
        }
        NumberFormat f = new DecimalFormat(s);
        s = f.format(num);
        return s;
    }

    //Curves Math --------------------------------------------------------------
    public static ArrayList<Vector3f> interpolateCurve(Spline curve, int segs) {
        List<Vector3f> controlPoints = curve.getControlPoints();
        ArrayList<Vector3f> interPoints = new ArrayList<Vector3f>();
        for (int i = 0; i < controlPoints.size() - 1; i++) {
            interPoints.add(controlPoints.get(i).clone());
            for (int j = 1; j < segs; j++) {
                Vector3f newPoint = new Vector3f();
                curve.interpolate((float) j / segs, i, newPoint);
                interPoints.add(newPoint);
            }
        }
        interPoints.add(controlPoints.get(controlPoints.size() - 1).clone());
        return interPoints;
    }

    /**
     * Interpolate a curve and adding an offset with direction.
     *
     * @param curve
     * @param segs
     */
    public static void interpolateCurveOffest(Spline curve, int segs) {
    }
    //Surface Math--------------------------------------------------------------

    //Algebra--------------------------------------------------------------------
//    public static String inverseFunction(String functionText) {
//        return functionText;
//    }
//    
//    public static Number parse(String functionText){
//        return 0;
//    }
}
