/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.world;

import com.google.common.base.Function;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.MaterialDef;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Node;
import com.jme3.texture.Texture;
import java.util.List;
import java.util.Properties;
import sg.atom.core.AbstractManager;

/**
 * MaterialManager.
 *
 * <p>Manager and Wrapper for useful functions for Material, including Material
 * clone, conversion, Color, Light attributes (Shiny,diffuse,...) and low level
 * effects. Tracked Material can be automaticly sync with each other in Atom
 * life cycle.
 *
 * <p>Convert various Material format to JME pipeline.
 *
 * <p>Manipulate Material quality, blending.
 *
 * <p>Ready for different Material scheme.
 *
 * @author atomix
 */
public class MaterialManager extends AbstractManager {

    protected AssetManager assetManager;
    //FIXME: Default instance. Not a singleton!
    private static MaterialManager defaultInstance;
    private Material matWire;
    private Material unshadedMat;

    public MaterialManager(WorldManager worldManager) {
        this.assetManager = worldManager.getAssetManager();
    }

    public MaterialManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public static MaterialManager getDefaultInstance(AssetManager assetManager) {
        if (defaultInstance == null) {
            defaultInstance = new MaterialManager(assetManager);
        } else {
        }
        return defaultInstance;
    }

    public Material getUnshadeMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", ColorRGBA.White);
        return mat;
    }

    public Material getLightMat() {
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        Texture texture = assetManager.loadTexture("");
        texture.setWrap(Texture.WrapMode.Repeat);
        mat.setTexture("DiffuseMap", texture);
        return mat;
    }

    public Material getColoredMat(ColorRGBA color) {
        if (unshadedMat == null) {
            unshadedMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");

        }
        Material unshadedMatClone = unshadedMat.clone();
        unshadedMatClone.setColor("Color", color);
        return unshadedMatClone;
    }

    public Material getWireFrameMat() {
        // WIREFRAME material
        if (matWire == null) {
            matWire = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        }
        Material matWireClone = matWire.clone();
        matWireClone.getAdditionalRenderState().setWireframe(true);
        matWireClone.setColor("Color", ColorRGBA.Green);
        return matWireClone;
    }

    public Material getCommonMat() {
        return null;
    }

    public static boolean isLightBlowMat(MaterialDef matDef) {
        return (matDef.getAssetName().endsWith("LightBlow.j3md"));
    }

    public static boolean isLightingMat(MaterialDef matDef) {
        return (matDef.getAssetName().endsWith("Lighting.j3md"));
    }

    public Material convertToLightBlow(Material orgMat) {
        // check if org is Lighting.jmd
        if (isLightingMat(orgMat.getMaterialDef())) {
            // construct the clone Mat
            Material newMat = new Material(assetManager, "MatDefs/LightBlow/LightBlow.j3md");
            Texture diffuseMap = orgMat.getTextureParam("DiffuseMap").getTextureValue();
            newMat.setTexture("DiffuseMap", diffuseMap);
            return newMat;
        } else {
            return orgMat;
        }
    }

    public Material convertMaterial(Material orginal, String type) {
        return null;
    }

    public Material convertMaterial(Material orginal, Function convertFunction) {
        return null;
    }

    public MaterialDef compileMaterialDef(String definitionScript) {
        return null;
    }

    public List<MaterialDef> listMaterialDef(Node scene) {
        return null;
    }

    public void injectShader(String code) {
    }

    public Material blendMaterials(Material mat1, Material mat2, float fraction) {
        return null;
    }

    public MaterialDef bridge(MaterialDef def1, MaterialDef def2) {
        return null;
    }
    /* Some optimization functions */

    public void batchTextures() {
    }
    //Cycle------------------------------------------------------------------------

    @Override
    public void init() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void load() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void config(Properties props) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void update(float tpf) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void finish() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public LifeCyclePhase getCurrentPhase() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public float getProgressPercent(LifeCyclePhase aPhrase) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
