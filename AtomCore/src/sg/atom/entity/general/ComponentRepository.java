/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sg.atom.entity.general;

/**
 * ComponentHolder is a mark for class which act like a "repository" for
 * Component.
 *
 * <p>Atom's Entity framework have no contract about the enviroment of Entities
 * and Components. This term commonly refered as a compounded World of Entities
 * and Components, which is an unnessary design. The Entities can be managed in
 * an sequential/concurent enviroment. Components also can be kept in another
 * sequential/concurent enviroment. The extra futher works belong to
 * implementation! </p>
 *
 * <p>ComponentRepository try to procedure a "managed view", a fullfilled view
 * of available component to the outside interesters/obsversers. See:
 * ConsistentView.
 *
 * Sensitive infomation may be hidden away without notification to the outsider.
 * This contract is very important to fix the common mistake of other component
 * repository which exposed all the inside components.
 *
 * Naturally it's a Collection in term of Java language.</p>
 *
 * <p>Read:
 * http://hub.jmonkeyengine.org/forum/topic/entity-system-topic-united/</p>
 *
 * @author cuong.nguyenmanh2
 */
public interface ComponentRepository {
}
