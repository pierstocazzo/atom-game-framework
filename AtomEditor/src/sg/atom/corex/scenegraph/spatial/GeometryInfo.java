/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.corex.scenegraph.spatial;

import com.jme3.scene.Spatial;

/**
 * Contain useful information of a Geometry wraper.
 *
 * <p> It later used in Editor program so it's have better mechanism of saving
 * data and beans related to this Geometry.
 *
 * @author hungcuong
 */
public class GeometryInfo extends EditorSpatialInfo {

    public GeometryInfo(Spatial spatial, EditorSpatialInfo parentInfo) {
        super(spatial, parentInfo);
    }
}
