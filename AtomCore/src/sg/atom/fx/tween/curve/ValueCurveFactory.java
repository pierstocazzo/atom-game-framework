/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.fx.tween.curve;

import sg.atom.utils.factory.IAtomFactory;

/**
 *
 * @author cuong.nguyenmanh2
 */
public class ValueCurveFactory implements IAtomFactory<ValueCurve<Float>> {

    @Override
    public ValueCurve<Float> create(Object param) {
        return null;
    }

    @Override
    public ValueCurve<Float> create(Object... params) {
        return null;
    }

    @Override
    public ValueCurve<Float> cloneObject(ValueCurve<Float> orginal) {
        return null;
    }

    /*
     private static InterpolatorFunction linearInterpolatorFunction = new LinearBezierInterpolatorFunction(0f, 1f);

     private static InterpolatorFunction easeInInterpolatorFunction = new CubicBezierInterpolatorFunction(0f, 0.42f, 1f, 1f);

     private static InterpolatorFunction easeOutInterpolatorFunction = new CubicBezierInterpolatorFunction(0f, 0f, 0.58f, 1.0f);

     private static InterpolatorFunction easeInOutInterpolatorFunction = new CubicBezierInterpolatorFunction(0f, 0.42f, 0.58f, 1.0f);

     private static InterpolatorFunction easeInterpolatorFunction = new CubicBezierInterpolatorFunction(0f, 0.25f, 0.25f, 1.0f);

     public static InterpolatorFunction cubicBezier(float p0, float p1, float p2, float p3) {
     return new CubicBezierInterpolatorFunction(p0, p1, p2, p3);
     }

     public static InterpolatorFunction quadratic(float p0, float p1, float p2) {
     return new QuadraticBezierInterpolatorFunction(p0, p1, p2);
     }

     public static InterpolatorFunction ease() {
     return easeInterpolatorFunction;
     }

     public static InterpolatorFunction linear() {
     return linearInterpolatorFunction;
     }

     public static InterpolatorFunction easeIn() {
     return easeInInterpolatorFunction;
     }

     public static InterpolatorFunction easeOut() {
     return easeOutInterpolatorFunction;
     }

     public static InterpolatorFunction easeInOut() {
     return easeInOutInterpolatorFunction;
     }
        
     */
}
