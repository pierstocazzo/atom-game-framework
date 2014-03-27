package sg.atom.logic.rule.conflict;

/*
 * sg.atom.logic.rule - The Java Embedded Object Production System
 * Copyright (c) 2000   Carlos Figueira Filho
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 * Contact: Carlos Figueira Filho (csff@cin.ufpe.br)
 */

import java.util.Vector;

/**
 * This class provides a skeletal implementation of the ConflictSet
 * interface to minimize the effort required to implement this interface.
 * It helps mainly in dealing with the registered listeners for conflict
 * set events. Its subclasses must invoke the methods
 * <code>elementAdded</code> and <code>elementRemoved</code> wheneved
 * applicable to make full use of the notification mechanism this skeleton
 * provides.
 *
 * @version 0.01  08 Jun 2000
 * @author Carlos Figueira Filho (<a href="mailto:csff@cin.ufpe.br">csff@cin.ufpe.br</a>)
 */
public abstract class AbstractConflictSet implements ConflictSet {

	/**
	 * The event listeners.
	 */
	protected Vector listeners;

	/**
	 * Flag indicating whether there is some registered listener.
	 */
	protected boolean debug = false;

	/**
	 * Class constructor.
	 */
	public AbstractConflictSet() {
		this.listeners = new Vector();
	}
	/**
	 * Adds the specified listener to receive events from
	 * this conflict set.
	 *
	 * @param l the conflict set listener
	 */
	public void addInternalConflictSetListener(InternalConflictSetListener l) {
		if (!listeners.contains(l)) {
			listeners.addElement(l);
			debug = true;
		}
	}
	/**
	 * Callback method, used to indicate that an element has been added
	 * to this conflict set.
	 *
	 * @param element the element that has been added to this conflict
	 *          set.
	 */
	protected void elementAdded(ConflictSetElement e) {
		if (debug) {
			fireInternalElementAddedEvent(new InternalConflictSetEvent(this, e));
		}
	}
	/**
	 * Callback method, used to indicate that an element has been removed
	 * from this conflict set. Elements that are returned by the
	 * <code>nextElement</code> are <b>not</b> considered as been removed.
	 *
	 * @param element the element that has been removed from this conflict
	 *          set.
	 */
	protected void elementRemoved(ConflictSetElement e) {
		if (debug) {
			fireInternalElementRemovedEvent(new InternalConflictSetEvent(this, e));
		}
	}
	/**
	 * Dispatch an internal element added event to all registered listeners.
	 */
	private void fireInternalElementAddedEvent(InternalConflictSetEvent e) {
		for (int i = 0; i < listeners.size(); i++) {
			((InternalConflictSetListener) listeners.elementAt(i)).internalElementAdded(e);
		}
	}
	/**
	 * Dispatch an internal element removed event to all registered listeners.
	 */
	private void fireInternalElementRemovedEvent(InternalConflictSetEvent e) {
		for (int i = 0; i < listeners.size(); i++) {
			((InternalConflictSetListener) listeners.elementAt(i)).internalElementRemoved(e);
		}
	}
	/**
	 * Auxiliar method, used to remove the elements that use a given
	 * object in its instantiations. This method should be used when the
	 * fireable rules are stored in a linear (unidimensional) Vector.
	 *
	 * @param fireableRules the Vector that stores the fireable rules.
	 * @param obj the given object.
	 */
	protected void removeElementsWith_1D(Vector fireableRules, Object obj) {
		Vector toBeRemoved = new Vector();
		for (int i = fireableRules.size() - 1; i >= 0; i--) {
			ConflictSetElement element = (ConflictSetElement) fireableRules.elementAt(i);
			if (element.isDeclared(obj)) {
				toBeRemoved.addElement(new Integer(i));
				elementRemoved(element);  // the callback method.
			}
		}
		for (int i = 0; i < toBeRemoved.size(); i++) {
			int index = ((Integer) toBeRemoved.elementAt(i)).intValue();
			fireableRules.removeElementAt(index);
		}
	}
	/**
	 * Auxiliar method, used to remove the elements that use a given
	 * object in its instantiations. This method should be used when the
	 * fireable rules are stored in a matricial (bidimensional) Vector.
	 *
	 * @param fireableRules the Vector that stores the fireable rules.
	 * @param obj the given object.
	 * @return the number of elements that have been removed.
	 */
	protected int removeElementsWith_2D(Vector fireableRules, Object obj) {
		int result = 0;
		for (int rule = 0; rule < fireableRules.size(); rule++) {
			Vector toBeRemoved = new Vector();
			Vector rules = (Vector) fireableRules.elementAt(rule);
			for (int i = rules.size() - 1; i >= 0; i--) {
				ConflictSetElement element = (ConflictSetElement) rules.elementAt(i);
				if (element.isDeclared(obj)) {
					toBeRemoved.addElement(new Integer(i));
					elementRemoved(element); // Callback method
				}
			}
			for (int i = 0; i < toBeRemoved.size(); i++) {
				int index = ((Integer) toBeRemoved.elementAt(i)).intValue();
				rules.removeElementAt(index);
				result++;
			}
		}
		return result;
	}
	/**
	 * Removes the specified listener so that it no longer
	 * receives events from this conflict set.
	 *
	 * @param l the conflict set listener
	 */
	public void removeInternalConflictSetListener(InternalConflictSetListener l) {
		listeners.removeElement(l);
		if (listeners.size() == 0) {
			debug = false;
		}
	}
}
