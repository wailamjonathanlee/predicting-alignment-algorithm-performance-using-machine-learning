package org.processmining.decomposedreplayer.configurations.impl;

import java.util.HashSet;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNetArray;
import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericStrategy;
import org.processmining.activityclusterarray.models.ActivityClusterArray;
import org.processmining.activityclusterarray.models.impl.ActivityClusterArrayFactory;
import org.processmining.decomposedreplayer.parameters.DecomposedReplayParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.log.models.EventLogArray;
import org.processmining.logalignment.models.LogAlignment;
import org.processmining.logalignment.models.LogAlignmentArray;
import org.processmining.logalignment.models.ReplayCostFactor;
import org.processmining.logalignment.models.ReplayResultArray;
import org.processmining.logdecomposer.filters.impl.DecompositionInFilter;
import org.processmining.logdecomposer.parameters.DecomposeEventLogUsingActivityClusterArrayParameters;
import org.processmining.logdecomposer.plugins.DecomposeEventLogUsingActivityClusterArrayPlugin;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedNotDecomposedReplayConfiguration extends DecomposedAbstractReplayConfiguration {

	public final static String NAME = "Do not decompose";
	
	public String getName() {
		return NAME;
	}

	public void update(NiceSlider slider) {
		slider.getSlider().setValue(0);
		slider.setEnabled(false);
	}

	public PNRepResult apply(PluginContext context, XLog log, AcceptingPetriNet net, DecomposedReplayParameters parameters,
			ActivityClusterArray ignoredClusters) {
		parameters.setPercentage(0);
		parameters.setDeadline(60 * 60 * 1000); // Give the non-decomposed replay an hour to do the replay.
		XLogInfo logInfo = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
		Set<XEventClass> activities = new HashSet<XEventClass>(logInfo.getEventClasses().getClasses());
		ActivityClusterArray clusters = ActivityClusterArrayFactory.createActivityClusterArray();
		clusters.init(NAME, activities);
		clusters.addCluster(activities);
		long msecs;
		AcceptingPetriNetArray nets = null;
		parameters.displayMessage("[DecomposedReplayPlugin] Found " + clusters.getClusters().size() + " clusters.");
		msecs = -System.currentTimeMillis();
		ReplayCostFactor factor = getFactor(context, clusters, parameters);
		context.getProvidedObjectManager().createProvidedObject("Factor", factor, ReplayCostFactor.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Creating factor took " + msecs + " milliseconds.");
		msecs = -System.currentTimeMillis();
		EventLogArray logs = getLogs(context, log, clusters, parameters);
		context.getProvidedObjectManager().createProvidedObject("Logs", logs, EventLogArray.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Decomposing log took " + msecs + " milliseconds.");
		msecs = -System.currentTimeMillis();
		AcceptingPetriNetArray replaceNets;
		replaceNets = null;
		msecs = -System.currentTimeMillis();
		AcceptingPetriNetArray filterNets = (nets != null ? nets : getNets(context, net, clusters, parameters,
				DecompositionGenericStrategy.NAME));
		context.getProvidedObjectManager().createProvidedObject("Nets (filter)", filterNets,
				AcceptingPetriNetArray.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage(
				"[DecomposedReplayPlugin] Decomposing net by filtering took " + msecs + " milliseconds.");
		if (replaceNets == null) {
			replaceNets = filterNets;
		}
		msecs = -System.currentTimeMillis();
		ReplayResultArray results = getResults(context, logs, replaceNets, factor, clusters, parameters);
		context.getProvidedObjectManager().createProvidedObject("Results", results, ReplayResultArray.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Decomposed replay took " + msecs + " milliseconds.");
		msecs = -System.currentTimeMillis();
		LogAlignmentArray alignments = getAlignments(context, logs, replaceNets, results, factor, parameters);
		context.getProvidedObjectManager().createProvidedObject("Alignments", alignments, LogAlignmentArray.class,
				context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Creating alignments took " + msecs + " milliseconds.");
		LogAlignmentArray filteredAlignments;
		filteredAlignments = alignments;
		msecs = -System.currentTimeMillis();
		LogAlignment alignment = getAlignment(context, filteredAlignments, log, filterNets, parameters);
		context.getProvidedObjectManager().createProvidedObject("Alignment", alignment, LogAlignment.class, context);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Merging alignments took " + msecs + " milliseconds.");
		msecs = -System.currentTimeMillis();
		PNRepResult result = getResult(context, alignment, log, net, parameters);
		msecs += System.currentTimeMillis();
		parameters.displayMessage("[DecomposedReplayPlugin] Creating replay took " + msecs + " milliseconds.");
		return result;
	}

	public EventLogArray getLogs(PluginContext context, XLog log, ActivityClusterArray clusters,
			DecomposedReplayParameters parameters) {
		DecomposeEventLogUsingActivityClusterArrayPlugin plugin = new DecomposeEventLogUsingActivityClusterArrayPlugin();
		DecomposeEventLogUsingActivityClusterArrayParameters params = new DecomposeEventLogUsingActivityClusterArrayParameters(
				log);
		params.setClassifier(parameters.getClassifier());
		params.setRemoveEmptyTraces(false);
		params.setAddStartEndEvents(false);
		params.setFilter(DecompositionInFilter.NAME);
		params.setTryConnections(parameters.isTryConnections());
		return plugin.run(context, log, clusters, params);
	}
}
