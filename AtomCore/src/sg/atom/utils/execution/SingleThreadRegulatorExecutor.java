/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.utils.execution;

import com.google.common.collect.HashBiMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import sg.atom.core.timing.Regulator;

/**
 * A Simple in thread executor.
 *
 * <p>For complex executor use ThreadPoolExecutor or
 * ScheduledThreadPoolExecutor.
 *
 * FIXME: Remove this.
 *
 * @author cuong.nguyenmanh2
 */
@Deprecated
public class SingleThreadRegulatorExecutor extends SingleThreadRegularExecutor {

    public SingleThreadRegulatorExecutor() {
        init();
    }

    public void init() {
        executionQueue = HashBiMap.create();
    }

    @Override
    public void updateRegulators(float tpf) {
        // FIXME: Should be executed by executor in another thread?

        Set<Map.Entry<Regulator, Runnable>> queue = executionQueue.entrySet();
        Iterator<Map.Entry<Regulator, Runnable>> iterator = queue.iterator();
        while (iterator.hasNext()) {
            Map.Entry<Regulator, Runnable> action = iterator.next();
            action.getKey().update(tpf);
            if (action.getKey().isReady()) {
                this.execute(action.getValue());
            }
        }
    }
}
