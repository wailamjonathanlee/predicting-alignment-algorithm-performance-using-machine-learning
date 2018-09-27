package org.processmining.decomposedreplayer.plugins;

import java.util.Collection;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.algorithms.DecomposedReplayAlgorithm;
import org.processmining.decomposedreplayer.connections.DecomposedReplayConnection;
import org.processmining.decomposedreplayer.dialogs.DecomposedReplayDialog;
import org.processmining.decomposedreplayer.help.DecomposedReplayHelp;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.connections.ConnectionCannotBeObtained;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

@Plugin(name = "Replay using Decomposition", categories = {
		PluginCategory.ConformanceChecking }, level = PluginLevel.PeerReviewed, parameterLabels = { "Event Log",
				"Accepting Petri net", "Parameters" }, returnLabels = {
						"Replay Result" }, returnTypes = { PNRepResult.class }, help = DecomposedReplayHelp.TEXT)
public class DecomposedReplayPlugin extends DecomposedReplayAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Decomposed Replay, UI", requiredParameterLabels = { 0, 1 })
	public PNRepResult runUI(UIPluginContext context, XLog log, AcceptingPetriNet net) {
		DecomposedReplayParameters parameters = new DecomposedReplayParameters(log, net);
		DecomposedReplayDialog dialog = new DecomposedReplayDialog();
		int n = 0;
		String[] title = { "Configure replay (configuration, classifier)",
				"Configure replay (transition-activity map)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 1, dialog.getPanel(log, net, parameters, n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
			} else {
				return null;
			}
		}
		return runConnection(context, log, net, parameters);
	}

	@Deprecated
	public PNRepResult replayUI(UIPluginContext context, XLog log, AcceptingPetriNet net) {
		return runUI(context, log, net);
	}

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Decomposed Replay, Default", requiredParameterLabels = { 0, 1 })
	public PNRepResult runDefault(PluginContext context, XLog log, AcceptingPetriNet net) {
		DecomposedReplayParameters parameters = new DecomposedReplayParameters(log, net);
		return runConnection(context, log, net, parameters);
	}

	@Deprecated
	public PNRepResult replayDefault(PluginContext context, XLog log, AcceptingPetriNet net) {
		return runDefault(context, log, net);
	}

	@PluginVariant(variantLabel = "Decomposed Replay, Parameters", requiredParameterLabels = { 0, 1, 2 })
	public PNRepResult run(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		return runConnection(context, log, net, parameters);
	}

	@Deprecated
	public PNRepResult replayDefault(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		return run(context, log, net, parameters);
	}

	private PNRepResult runConnection(PluginContext context, XLog log, AcceptingPetriNet net,
			DecomposedReplayParameters parameters) {
		if (parameters.isTryConnections()) {
			Collection<DecomposedReplayConnection> connections;
			try {
				connections = context.getConnectionManager().getConnections(DecomposedReplayConnection.class, context,
						log);
				for (DecomposedReplayConnection connection : connections) {
					if (connection.getObjectWithRole(DecomposedReplayConnection.LOG).equals(log)
							&& connection.getObjectWithRole(DecomposedReplayConnection.NET).equals(net)
							&& connection.getParameters().equals(parameters)) {
						return connection.getObjectWithRole(DecomposedReplayConnection.RESULT);
					}
				}
			} catch (ConnectionCannotBeObtained e) {
			}
		}

		PNRepResult result = apply(context, log, net, parameters);

		if (parameters.isTryConnections()) {
			context.getConnectionManager().addConnection(new DecomposedReplayConnection(log, net, result, parameters));
		}
		return result;
	}

}
