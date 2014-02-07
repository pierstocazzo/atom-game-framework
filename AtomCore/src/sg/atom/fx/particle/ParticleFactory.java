/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.fx.particle;

import com.jme3.asset.AssetManager;
import com.jme3.effect.ParticleEmitter;
import com.jme3.effect.ParticleMesh;
import com.jme3.effect.shapes.EmitterSphereShape;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import sg.atom.utils.factory.IAtomFactory;

/**
 *
@author atomix
 */
public class ParticleFactory implements IAtomFactory<ParticleEmitter>{
    private AssetManager assetManager;
    
    /* Constants */
    private static final int COUNT_FACTOR = 1;
    private static final float COUNT_FACTOR_F = 1f;


    public ParticleFactory(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public ParticleEmitter createFlame() {
        ParticleEmitter flame = new ParticleEmitter("Flame", ParticleMesh.Type.Point, 320 * COUNT_FACTOR);
        flame.setSelectRandomImage(true);
        flame.setStartColor(new ColorRGBA(1f, 0.4f, 0.05f, (float) (1f / COUNT_FACTOR_F)));
        flame.setEndColor(new ColorRGBA(.4f, .22f, .12f, 0f));
        flame.setStartSize(2f);
        flame.setEndSize(4f);
        flame.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        flame.setParticlesPerSec(30);
        flame.setGravity(0, 1, 0);
        flame.setLowLife(4f);
        flame.setHighLife(5f);
        flame.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
        //flame.getParticleInfluencer().setVelocityVariation(1f);
        flame.setImagesX(2);
        flame.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flame.png"));
        mat.setBoolean("PointSprite", true);
        flame.setMaterial(mat);
        return flame;
    }

    public ParticleEmitter createFlash() {
        ParticleEmitter flash = new ParticleEmitter("Flash", ParticleMesh.Type.Point, 24 * COUNT_FACTOR);
        flash.setSelectRandomImage(true);
        flash.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1f / COUNT_FACTOR_F)));
        flash.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        flash.setStartSize(.1f);
        flash.setEndSize(3.0f);
        flash.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        flash.setParticlesPerSec(0);
        flash.setGravity(0, 0, 0);
        flash.setLowLife(.2f);
        flash.setHighLife(.2f);
        flash.setInitialVelocity(new Vector3f(0, 5f, 0));
        flash.setVelocityVariation(1);
        flash.setImagesX(2);
        flash.setImagesY(2);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/flash.png"));
        mat.setBoolean("PointSprite", true);
        flash.setMaterial(mat);
        return flash;
    }

    public ParticleEmitter createRoundSpark() {
        ParticleEmitter roundspark = new ParticleEmitter("RoundSpark", ParticleMesh.Type.Point, 20 * COUNT_FACTOR);
        roundspark.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, (float) (1.0 / COUNT_FACTOR_F)));
        roundspark.setEndColor(new ColorRGBA(0, 0, 0, (float) (0.5f / COUNT_FACTOR_F)));
        roundspark.setStartSize(1.2f);
        roundspark.setEndSize(1.8f);
        roundspark.setShape(new EmitterSphereShape(Vector3f.ZERO, 2f));
        roundspark.setParticlesPerSec(0);
        roundspark.setGravity(0, -.5f, 0);
        roundspark.setLowLife(1.8f);
        roundspark.setHighLife(2f);
        roundspark.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 3, 0));
        roundspark.getParticleInfluencer().setVelocityVariation(.5f);
        roundspark.setImagesX(1);
        roundspark.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/roundspark.png"));
        mat.setBoolean("PointSprite", true);
        roundspark.setMaterial(mat);
        return roundspark;
    }

    public ParticleEmitter createRain(String texturePath) {
        ParticleEmitter rain = new ParticleEmitter("Rain", ParticleMesh.Type.Triangle, 20 * COUNT_FACTOR);
        rain.setStartColor(new ColorRGBA(1f, 0.29f, 0.34f, (float) (1.0 / COUNT_FACTOR_F)));
        rain.setEndColor(new ColorRGBA(0, 0, 0, (float) (0.5f / COUNT_FACTOR_F)));
        rain.setStartSize(.5f);
        rain.setEndSize(.5f);
        rain.setFacingVelocity(true);
        rain.setParticlesPerSec(10);
        rain.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        rain.setGravity(0, 1, 0);
        rain.setLowLife(4f);
        rain.setHighLife(5f);
        rain.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 7, 0));
        rain.setImagesX(1);
        rain.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        if (texturePath.isEmpty()) {
            texturePath = "Effects/Explosion/roundspark.png";
        }
        texturePath = "Effects/Explosion/roundspark.png";
        mat.setTexture("Texture", assetManager.loadTexture(texturePath));
        //mat.setBoolean("PointSprite", POINT_SPRITE);
        rain.setMaterial(mat);
        return rain;
    }

    public ParticleEmitter createSpark() {
        ParticleEmitter spark = new ParticleEmitter("Spark", ParticleMesh.Type.Triangle, 30 * COUNT_FACTOR);
        spark.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        spark.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        spark.setStartSize(.5f);
        spark.setEndSize(.5f);
        spark.setFacingVelocity(true);
        spark.setParticlesPerSec(0);
        spark.setGravity(0, 5, 0);
        spark.setLowLife(1.1f);
        spark.setHighLife(1.5f);
        spark.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 20, 0));
        spark.getParticleInfluencer().setVelocityVariation(1);
        spark.setImagesX(1);
        spark.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/spark.png"));
        spark.setMaterial(mat);
        return spark;
    }

    public ParticleEmitter createSmokeTrail() {
        ParticleEmitter smoketrail = new ParticleEmitter("SmokeTrail", ParticleMesh.Type.Triangle, 22 * COUNT_FACTOR);
        smoketrail.setStartColor(new ColorRGBA(1f, 0.8f, 0.36f, (float) (1.0f / COUNT_FACTOR_F)));
        smoketrail.setEndColor(new ColorRGBA(1f, 0.8f, 0.36f, 0f));
        smoketrail.setStartSize(.2f);
        smoketrail.setEndSize(1f);

//        smoketrail.setShape(new EmitterSphereShape(Vector3f.ZERO, 1f));
        smoketrail.setFacingVelocity(true);
        smoketrail.setParticlesPerSec(0);
        smoketrail.setGravity(0, 1, 0);
        smoketrail.setLowLife(.4f);
        smoketrail.setHighLife(.5f);
        smoketrail.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 12, 0));
        smoketrail.getParticleInfluencer().setVelocityVariation(1);
        smoketrail.setImagesX(1);
        smoketrail.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/smoketrail.png"));
        smoketrail.setMaterial(mat);
        return smoketrail;
    }

    public ParticleEmitter createDebris() {
        ParticleEmitter debris = new ParticleEmitter("Debris", ParticleMesh.Type.Triangle, 15 * COUNT_FACTOR);
        debris.setSelectRandomImage(true);
        debris.setRandomAngle(true);
        debris.setRotateSpeed(FastMath.TWO_PI * 4);
        debris.setStartColor(new ColorRGBA(1f, 0.59f, 0.28f, (float) (1.0f / COUNT_FACTOR_F)));
        debris.setEndColor(new ColorRGBA(.5f, 0.5f, 0.5f, 0f));
        debris.setStartSize(.2f);
        debris.setEndSize(.2f);

//        debris.setShape(new EmitterSphereShape(Vector3f.ZERO, .05f));
        debris.setParticlesPerSec(0);
        debris.setGravity(0, 12f, 0);
        debris.setLowLife(1.4f);
        debris.setHighLife(1.5f);
        debris.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 15, 0));
        debris.getParticleInfluencer().setVelocityVariation(.60f);
        debris.setImagesX(3);
        debris.setImagesY(3);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/Debris.png"));
        debris.setMaterial(mat);
        return debris;
    }

    public ParticleEmitter createShockwave() {
        ParticleEmitter shockwave = new ParticleEmitter("Shockwave", ParticleMesh.Type.Triangle, 1 * COUNT_FACTOR);
//        shockwave.setRandomAngle(true);
        shockwave.setFaceNormal(Vector3f.UNIT_Y);
        shockwave.setStartColor(new ColorRGBA(.48f, 0.17f, 0.01f, (float) (.8f / COUNT_FACTOR_F)));
        shockwave.setEndColor(new ColorRGBA(.48f, 0.17f, 0.01f, 0f));

        shockwave.setStartSize(0f);
        shockwave.setEndSize(7f);

        shockwave.setParticlesPerSec(0);
        shockwave.setGravity(0, 0, 0);
        shockwave.setLowLife(0.5f);
        shockwave.setHighLife(0.5f);
        shockwave.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 0, 0));
        shockwave.getParticleInfluencer().setVelocityVariation(0f);
        shockwave.setImagesX(1);
        shockwave.setImagesY(1);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Particle.j3md");
        mat.setTexture("Texture", assetManager.loadTexture("Effects/Explosion/shockwave.png"));
        shockwave.setMaterial(mat);
        return shockwave;
    }

    public Node createExplosionNode() {
        Node explosion = new Node("Explosion");
        ParticleEmitter flame = createFlame();
        ParticleEmitter flash = createFlash();
        ParticleEmitter spark = createSpark();
        ParticleEmitter roundSpark = createRoundSpark();
        ParticleEmitter smokeTrail = createSmokeTrail();
        ParticleEmitter debris = createDebris();
        ParticleEmitter shockwave = createShockwave();

        explosion.attachChild(flame);
        explosion.attachChild(flash);
        explosion.attachChild(spark);
        explosion.attachChild(roundSpark);
        explosion.attachChild(smokeTrail);
        explosion.attachChild(debris);
        explosion.attachChild(shockwave);

        return explosion;
    }


    public void onEffect(ParticleEmitter ef) {
        if (ef != null) {
            ef.emitAllParticles();
            ef.setEnabled(true);
        }
    }

    public void onEffects(ParticleEmitter[] efs) {
        for (ParticleEmitter ef : efs) {
            onEffect(ef);
        }
    }

    public void shutEffects(ParticleEmitter[] efs) {
        for (ParticleEmitter ef : efs) {
            shutEffect(ef);
        }
    }

    public void shutEffect(ParticleEmitter ef) {
        if (ef != null) {
            ef.killAllParticles();
            ef.setEnabled(false);
        }
    }

    @Override
    public ParticleEmitter create(Object param) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ParticleEmitter create(Object... params) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ParticleEmitter cloneObject(ParticleEmitter orginal) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
