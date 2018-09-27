package org.processmining.decomposedreplayer.plugins;

import org.deckfour.uitopia.api.event.TaskListener.InteractionResult;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.algorithms.RecomposingReplayAlgorithm;
import org.processmining.decomposedreplayer.dialogs.RecomposingReplayDialog;
import org.processmining.decomposedreplayer.help.RecomposingReplayHelp;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginLevel;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

@Plugin(name = "Replay using Recomposition", categories = {
		PluginCategory.ConformanceChecking }, level = PluginLevel.PeerReviewed, parameterLabels = { "Event Log", "Accepting Petri net",
"Parameters" }, returnLabels = { "Replay Result" }, returnTypes = { PNRepResult.class }, help = RecomposingReplayHelp.TEXT)
public class RecomposingReplayPlugin extends RecomposingReplayAlgorithm {

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Recomposing Replay, UI", requiredParameterLabels = { 0, 1 })
	public PNRepResult runUI(UIPluginContext context, XLog log, AcceptingPetriNet net) {
		RecomposingReplayParameters parameters = new RecomposingReplayParameters(log, net);
		RecomposingReplayDialog dialog = new RecomposingReplayDialog();
		int n = 0;
		String[] title = { "Configure replay (configuration, classifier)", "Configure replay (transition-activity map)", "Configure replay (rejection)", "Configure replay (termination)", "Configure replay (decomposition)" };
		InteractionResult result = InteractionResult.NEXT;
		while (result != InteractionResult.FINISHED) {
			result = context.showWizard(title[n], n == 0, n == 4, dialog.getPanel(context, log, net, parameters, n));
			if (result == InteractionResult.NEXT) {
				n++;
			} else if (result == InteractionResult.PREV) {
				n--;
			} else if (result == InteractionResult.FINISHED) {
			} else {
				return null;
			}
		}
		return runParameters(context, log, net, parameters);
	}
	
	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "H.M.W. Verbeek", email = "h.m.w.verbeek@tue.nl")
	@PluginVariant(variantLabel = "Recomposing Replay, Default", requiredParameterLabels = { 0, 1 })
	public PNRepResult runDefault(PluginContext context, XLog log, AcceptingPetriNet net) {
		RecomposingReplayParameters parameters = new RecomposingReplayParameters(log, net);
		return runParameters(context, log, net, parameters);
	}

	private PNRepResult runParameters(PluginContext context, XLog log, AcceptingPetriNet net, RecomposingReplayParameters parameters) {
		RecomposingReplayWorkspace workspace = new RecomposingReplayWorkspace(log, parameters);
		XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		context.getProgress().setMinimum(0);
		context.getProgress().setMaximum(info.getEventClasses().size());
		context.getProgress().setValue(0);
		return apply(context, log, net, workspace, parameters);
	}
}
