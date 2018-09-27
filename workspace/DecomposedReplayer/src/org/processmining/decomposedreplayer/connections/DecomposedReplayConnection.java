package org.processmining.decomposedreplayer.connections;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.connections.impl.AbstractConnection;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class DecomposedReplayConnection extends AbstractConnection {

	public final static String LOG = "Log";
	public final static String NET = "Net";
	public final static String RESULT = "Result";

	private DecomposedReplayParameters parameters;

	public DecomposedReplayConnection(XLog log, AcceptingPetriNet net, PNRepResult result,
			DecomposedReplayParameters parameters) {
		super("Replay Accepting Petri Net Using Decomposition Connection");
		put(LOG, log);
		put(NET, net);
		put(RESULT, result);
		this.parameters = new DecomposedReplayParameters(parameters);
	}

	public DecomposedReplayParameters getParameters() {
		return parameters;
	}
}