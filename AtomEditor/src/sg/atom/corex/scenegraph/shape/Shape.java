/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.corex.scenegraph.shape;

import com.jme3.scene.Node;

/**
 * Atom Shape is selectable Geometry wrapper.
 *
 * <p> It's modifiable and can extract its topology imformation.
 *
 * <p> Initial design of AtomShape is to be used as Smart Object for
 * modification and animation helper.
 *
 * @author hungcuong
 */
public class Shape extends Node {

    public static final int POINT = 0;
    public static final int BOX = 1;
    public static final int CYLINDER = 2;
    public static final int CONE = 3;
    public static final int SPHERE = 4;
    public static final int CAM = 5;
    public static final int ARROW = 6;
    public static final int LIGHT = 7;
    public static final int BONE = 8;
    public static final int PIE = 9;
    public static final int FORCE = 10;
    public static final int WIND = 11;
    public static final int GRAVITY = 12;
    public static final int GRID = 13;
    public static final int LINE = 14;
    public static final int CIRCLE = 15;
    public static final int GRIDHELPER = 16;
    public static final int GEO = 100;
    public static final int SHAPE = 101;

    public Shape(ShapeUtilBuilder builder) {
        super(builder.getName());
    }
}
