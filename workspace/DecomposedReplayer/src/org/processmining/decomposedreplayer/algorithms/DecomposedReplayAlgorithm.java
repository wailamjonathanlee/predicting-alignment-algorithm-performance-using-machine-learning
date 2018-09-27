package org.processmining.decomposedreplayer.algorithms;

import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.decomposedreplayer.configurations.DecomposedReplayConfigurationManager;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

public class DecomposedReplayAlgorithm {

	public PNRepResult apply(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		return DecomposedReplayConfigurationManager.getInstance().getConfiguration(parameters.getConfiguration())
				.apply(context, log, net, parameters, null);
	}
}
