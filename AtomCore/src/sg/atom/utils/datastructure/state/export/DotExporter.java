package sg.atom.utils.datastructure.state.export;

import sg.atom.utils.datastructure.state.impl.AbstractTransitionModel;
import sg.atom.utils.datastructure.state.impl.BasicTransition;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DotExporter<S, E, C> {
	private final AbstractTransitionModel<S, E, C> model;
	private final String name;

	public DotExporter(AbstractTransitionModel<S, E, C> model, String name) {
		this.model = model;
		this.name = name;
	}

	public void asDot(OutputStream os, boolean includeFromAllTransitions) {
		PrintWriter writer = new PrintWriter(os);
		writer.println("digraph " + name + " {");
		Map<S, Map<E, Collection<BasicTransition<S, E, C>>>> stateTransitions = model.getStateTransitions();
		Set<S> states = stateTransitions.keySet();
		Set<S> allStates = new HashSet<S>(states);
		for (S state : states) {
			Map<E, Collection<BasicTransition<S, E, C>>> transitionsFromState = stateTransitions.get(state);
			for (Map.Entry<E, Collection<BasicTransition<S, E, C>>> transitions : transitionsFromState.entrySet()) {
				for (BasicTransition<S, E, C> transition : transitions.getValue()) {
					printTransition(writer, state, transition.getTo(), transitions.getKey(), "");
					allStates.add(transition.getTo());
				}
			}

		}
		if (includeFromAllTransitions) {
			Map<E, Collection<BasicTransition<S, E, C>>> fromAllTransitions = model.getFromAllTransitions();
			for (E event : fromAllTransitions.keySet()) {
				Collection<BasicTransition<S, E, C>> fromAll = fromAllTransitions.get(event);
				for (S state : allStates) {
					for (BasicTransition<S, E, C> transition : fromAll) {
						printTransition(writer, state, transition.getTo(), event, "(fromall)");
					}
				}
			}
		}
		writer.println("}");
		writer.flush();
	}

	private void printTransition(PrintWriter writer, S from, S to, E cause, String extra) {
		writer.println("\t" + from + " -> " + to + " [label = \"" + cause + extra + "\"]");
	}
}
