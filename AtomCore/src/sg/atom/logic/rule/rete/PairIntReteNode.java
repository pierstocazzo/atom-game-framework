package sg.atom.logic.rule.rete;

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

/**
 * Auxiliar class used to group pairs of integers and rete nodes.
 *
 * @author Carlos Figueira Filho (<a href="mailto:csff@cin.ufpe.br">csff@cin.ufpe.br</a>)
 * @version 1.0   13 Jul 2000
 */
class PairIntReteNode {

	/**
	 * The integer value.
	 */
	private int intValue;
	
	/**
	 * The rete node.
	 */
	private ReteNode node;
	
	/**
	 * Class constructor.
	 *
	 * @param intValue the integer value.
	 * @param node the rete node.
	 */
	public PairIntReteNode(int intValue, ReteNode node) {
		this.intValue = intValue;
		this.node = node;
	}
	/**
	 * Compares the given object with this one.
	 *
	 * @return <code>true</code> if they are the same object;
	 *          <code>false</code> otherwise.
	 */
	public boolean equals(Object obj) {
		if (obj instanceof PairIntReteNode) {
			PairIntReteNode tmp = (PairIntReteNode) obj;
			return (tmp.intValue == intValue && tmp.node.equals(node));
		}
		return false;
	}
	/**
	 * Returns the integer value of this pair.
	 *
	 * @return the integer value of this pair.
	 */
	public int getIntValue() {
		return intValue;
	}
	/**
	 * Returns the rete node of this pair.
	 *
	 * @return the rete node of this pair.
	 */
	public ReteNode getNode() {
		return node;
	}
	/**
	 * Returns a hash code for this object.
	 *
	 * @return a hash code for this object.
	 */
	public int hashCode() {
		return intValue + node.hashCode();
	}
	/**
	 * Returns a string representation of this object. Useful
	 * for debugging.
	 *
	 * @return a string representation of this object.
	 */
	public String toString() {
		return ("Pair["+intValue+","+node+"]");
	}
}
