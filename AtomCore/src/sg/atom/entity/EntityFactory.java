/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.entity;

import sg.atom.stage.StageManager;
import sg.atom.world.WorldManager;
import sg.atom.utils.factory.IAtomFactory;

/**
 * EntityFactory to procedure Entity. 
 * 
 * (CommonImplementation) Consider as
 * Suggestion to use the Factory pattern along with EntitySystem.
 * <ul>
 * <li>It has a Cache implementation of original entities beside of one in AssetManager.</li>
 * 
 * <li>Also support dependency injection to create Entity.</li>
 *
 * </ul>
 * @author atomix
 */
public class EntityFactory implements IAtomFactory<Entity> {

    protected EntityManager entityManager;
    protected StageManager stageManager;
    protected WorldManager worldManager;

    public EntityFactory(EntityManager entityManager, StageManager stageManager) {
        this.entityManager = entityManager;
        this.stageManager = stageManager;
        this.worldManager = stageManager.getWorldManager();
    }

    @Override
    public Entity create(Object param) {
        return null;
    }

    @Override
    public Entity create(Object... params) {
        return null;
    }

    @Override
    public Entity cloneObject(Entity orginal) {
        return null;
    }

    @Override
    public Entity get() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
