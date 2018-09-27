package org.processmining.decomposedreplayer.experiments.plugins;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.extension.XExtensionManager;
import org.deckfour.xes.extension.std.XConceptExtension;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.ImportAcceptingPetriNetPlugin;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.decomposedreplayer.algorithms.replay.impl.RecomposingReplayWithRecomposeStrategyAlgorithm;
import org.processmining.decomposedreplayer.experiments.parameters.TestRecomposingReplayWithMergeStrategyParameters;
import org.processmining.decomposedreplayer.experiments.utils.LogImporter;
import org.processmining.decomposedreplayer.experiments.utils.ReplayResultCsvWriter;
import org.processmining.decomposedreplayer.experiments.utils.StringFileWriter;
import org.processmining.decomposedreplayer.models.stats.IterationStats;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.abstractplugins.ImportPlugin;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginCategory;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.framework.util.HTMLToString;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.log.OpenLogFilePlugin;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

@Plugin(name = "Evaluate Recomposing Replay with Merge", 
		categories = { PluginCategory.ConformanceChecking }, 
		parameterLabels = { "Parameters" }, 
		returnLabels = { "Report" }, returnTypes = { HTMLToString.class })
public class TestRecomposingReplayWithMergeStrategyPlugin implements HTMLToString {

	private StringBuffer buf = new StringBuffer();
	private static ImportPlugin logImporter = new OpenLogFilePlugin();
	private static ImportAcceptingPetriNetPlugin netImporter = new ImportAcceptingPetriNetPlugin();

	@UITopiaVariant(affiliation = UITopiaVariant.EHV, author = "Jonathan Lee", email = "walee@uc.cl", pack = "JonathanLee")
	@PluginVariant(variantLabel = "Default", requiredParameterLabels = { 0 })
	public static HTMLToString run(final PluginContext context, TestRecomposingReplayWithMergeStrategyParameters parameters) {
		
		XLog log;
		AcceptingPetriNet apn;
		try {
			/*
			 * Import the event log.
			 */
			System.out.println("Importing " + parameters.logPath);
			log = LogImporter.importFile(parameters.logPath);
			/*
			 * Import the Accepting Petri net discovered for the corresponding noise-free event log.
			 */
			System.out.println("Importing " + parameters.modelPath);
			apn = (AcceptingPetriNet) netImporter.importFile(context, parameters.modelPath);
			return new TestRecomposingReplayWithMergeStrategyPlugin(context, log, apn, parameters);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public TestRecomposingReplayWithMergeStrategyPlugin(final PluginContext context, final XLog log, final AcceptingPetriNet apn,
			final TestRecomposingReplayWithMergeStrategyParameters parameters) {
		try {
			RecomposingReplayParameters replayParameters = new RecomposingReplayParameters(log, apn);
			
			replayParameters.setGlobalDuration(parameters.globalDuration);
			replayParameters.setLocalDuration(parameters.localDuration);
			replayParameters.setMoveOnLogCosts(parameters.moveOnLogCosts);
			replayParameters.setMoveOnModelCosts(parameters.moveOnModelCosts);
			replayParameters.setIntervalRelative(parameters.intervalRelative);
			replayParameters.setIntervalAbsolute(parameters.intervalAbsolute);
			replayParameters.setMaxConflicts(parameters.maxConflicts);
			replayParameters.setAlignmentPercentage(parameters.alignmentPercentage);
			replayParameters.setNofIterations(parameters.nofIterations);
			replayParameters.setPlanB(false);
			
			// set unsplittable activity list
			Set<XEventClass> unsplittableActivities = new HashSet<>();
			final XLogInfo info = XLogInfoFactory.createLogInfo(log, log.getClassifiers().get(0));
			unsplittableActivities.addAll(info.getEventClasses().getClasses());
			
			// it seems that XLogInfo creates all possible combinations of concept:name and 
			// lifecycle:transition, which means that in the case where there are no such combination
			// in the net, it will cause null exception when we try creating decompositions involving 
			// these non-existent activities
			// Solution: Check that all unsplittableActivities can be mapped to a transition
			Set<XEventClass> toRemove = new HashSet<>();
			for (XEventClass activity: unsplittableActivities) {
				if (!replayParameters.getMapping().containsValue(activity))
					toRemove.add(activity);
			}
			unsplittableActivities.removeAll(toRemove);
			
			Set<XEventClass> initialDecompositionActivities = new HashSet<>();
			for (XEventClass activity: info.getEventClasses().getClasses()) {
				if (parameters.initialDecompositionSet.contains(activity.getId())) {
					// add initial decomposition activity
					initialDecompositionActivities.add(activity);
					System.out.println("Add initial decomposition activity: " + activity.getId());
				}
			}
			
			if (!initialDecompositionActivities.isEmpty()) {
				unsplittableActivities.removeAll(initialDecompositionActivities);
				replayParameters.setUnsplittableActivities(unsplittableActivities);
			} 
			
			
			System.out.println("GlobalDuration: " + replayParameters.getGlobalDuration());
			System.out.println("LocalDuration: " + replayParameters.getLocalDuration());
			System.out.println("MoveOnLogCosts: " + replayParameters.getMoveOnLogCosts());
			System.out.println("MoveOnModelCosts: " + replayParameters.getMoveOnModelCosts());
			System.out.println("IntervalRelative: " + replayParameters.getIntervalRelative());
			System.out.println("IntervalAbsolute: " + replayParameters.getIntervalAbsolute());
			System.out.println("MaxConflicts: " + replayParameters.getMaxConflicts());
			System.out.println("AlignmentPercentage: " + replayParameters.getAlignmentPercentage());
			System.out.println("NofIterations: " + replayParameters.getNofIterations());
			
			RecomposingReplayWorkspace workspace = new RecomposingReplayWorkspace(log, replayParameters);
			
			long time = -System.currentTimeMillis();
			
			// create replayer and replay
			RecomposingReplayWithRecomposeStrategyAlgorithm replayer = new RecomposingReplayWithRecomposeStrategyAlgorithm(
					context, log, apn, workspace, replayParameters, parameters.recomposeStrategy, 
					parameters.logCreationStrategy);
			PNRepResult repResult = replayer.apply();
			time += System.currentTimeMillis();
			
			System.out.println("Finished recomposing replay in " + time + " millis.");
			
			System.out.println(parameters.toString());
			
			boolean isReliable = true;
			int numOfTraces = repResult.size();
			int numOfAligned = workspace.alignments.size();
			int numOfToAlign = workspace.openAlignments.size();
			int numOfRejected = workspace.pseudoAlignments.size();
			
			// getting the number of recomposition steps taken
			int numOfRecompositionSteps = workspace.nofIterations;
			
			double sumLoCosts = 0;
			double sumHiCosts = 0;
			double sumMaxCosts = 0;
			int sumWeight = 0;
			
			// get the trace indices of the rejected alignments
			XConceptExtension extension = (XConceptExtension) XExtensionManager.instance().getByName("concept");
			for (SyncReplayResult rejected: workspace.pseudoAlignments) {

				// print out the indexes of the rejected alignments
				System.out.println("[" + getClass().getSimpleName() + "] Rejected alignments "
						+ "trace indexes: " + rejected.getTraceIndex());
				for (int index: rejected.getTraceIndex()) {
					// print the trace concept:name
					XTrace trace = log.get(index);
					System.out.println("[" + getClass().getSimpleName() + "] Rejected trace "
							+ " concept:name: " + trace.getAttributes().get("concept:name").toString());
				}

			}
			
			// Save all the fitness cost per alignment in one csv
			String dirname = "./test_results";
			File dir = new File(dirname);
			if (!(dir.exists() || dir.isDirectory())) 
				dir.mkdirs();
			String filepath = dirname + "/per_alignment_results.csv";
			List<String> repResultPerAlignment = new LinkedList<>();
			String header = "Raw Fitness Cost Lo, Raw Fitness Cost Hi, Case Id";
			repResultPerAlignment.add(header);
			for (SyncReplayResult alignment: repResult) {
				String fitnessLo = alignment.getInfo().get("Raw Fitness Cost Lo") + "";
				String fitnessHi = alignment.getInfo().get("Raw Fitness Cost Hi") + "";
				String caseIds = "";
				for (int index: alignment.getTraceIndex()) {
					XTrace trace = log.get(index);
					String caseId = trace.getAttributes().get("concept:name").toString();
					if (caseIds.equals(""))
						caseIds = caseIds + caseId;
					else
						caseIds = caseIds + "; " + caseId;
				}
				String row = fitnessLo + ", " + fitnessHi + ", " + caseIds;
				repResultPerAlignment.add(row);
			}
			StringFileWriter writer = new StringFileWriter();
//			writer.writeStringListToFile(repResultPerAlignment, filepath);
			
			String[] outPathSplit = parameters.outFile.split(File.separator);
			String outdir = outPathSplit[0];
			String statsFilePath = outPathSplit[0];
			String toCopy;
			for (int i = 1; i < outPathSplit.length; i++) {
				toCopy = outPathSplit[i];
				if (i == outPathSplit.length - 1)
					toCopy = toCopy.replace("results", "stats");
				statsFilePath += (File.separator + toCopy);
				if (i != outPathSplit.length - 1)
					// only add to the out directory if it is not the final filename
					outdir += (File.separator + toCopy);
			}
			System.out.println("[" + getClass().getSimpleName() + "] Printing performance stats to " + statsFilePath);
			
			// output the performance stats
			List<String> statsStringList = new ArrayList<>();
			// add header if need be
			if (!(new File(statsFilePath)).exists())
				statsStringList.add("Model, Log, " + IterationStats.HEADER);
			else
				statsStringList.add("\n");
			for (IterationStats stats: replayer.getPerformanceStats()) {
				statsStringList.add(parameters.model + ", " + parameters.log + ", " + stats.toRowString());
			}
			writer.writeStringListToFile(statsStringList, statsFilePath, true);

			// write out the log alignments per iteration
			// make a directory for all the alignments
			outdir += (File.separator + parameters.iteration);
			boolean madeDir = new File(outdir).mkdirs();
			System.out.printf("[%s] Created directory for alignments at %s: %b", 
					getClass().getSimpleName(), outdir, madeDir);
			String alignmentFilePath;
//			for (int i = 0; i != replayer.getLogAlignments().size(); ++i) {
//				LogAlignmentJson logAlignmentIter = replayer.getLogAlignments().get(i);
//				alignmentFilePath = String.format("%s%s%s%d.json", 
//						outdir, File.separator, "alignments", i);
//				logAlignmentIter.writeToFile(alignmentFilePath);
//			}
			
			// print alignments as csv
			TransEvClassMapping mapping = replayParameters.getMapping();
			
			int i = 0;
			for (SyncReplayResult alignment: repResult) {
				alignmentFilePath = String.format("%s%s%s%d.csv", 
						outdir, File.separator, "alignments", i);
				ReplayResultCsvWriter.writeReplayResultToCsv(alignment, alignmentFilePath, mapping);
				i += 1;
			}
			
			// print out the state information
			double logStateCount = 0.0;
			double logQueuedStates = 0.0;
			double logTraversedArcs = 0.0;
			double avgLogStateCount = 0.0;
			double avgLogQueuedStates = 0.0;
			double avgLogTraversedArcs = 0.0;
			int logSize = 0;
			
			for (SyncReplayResult alignment : repResult) {
				int nofTraces = alignment.getTraceIndex().size();
				logSize += nofTraces;
				double costLo = alignment.getInfo().get("Raw Fitness Cost Lo");
				double costHi = alignment.getInfo().get("Raw Fitness Cost Hi");
				double maxCosts = alignment.getInfo().get("Raw Fitness Cost Max");
				sumLoCosts += nofTraces * costLo;
				sumMaxCosts += nofTraces * maxCosts;
				sumHiCosts += nofTraces * costHi;
				sumWeight += nofTraces;
				
				double stateCount = alignment.getInfo().get(PNRepResult.NUMSTATEGENERATED);
				double queuedStates = alignment.getInfo().get(PNRepResult.QUEUEDSTATE);
				double traversedArcs = alignment.getInfo().get(PNRepResult.TRAVERSEDARCS);
				
				logStateCount += stateCount * nofTraces;
				logQueuedStates += queuedStates * nofTraces;
				logTraversedArcs += traversedArcs * nofTraces;
				
				// get the caseid of the first associated trace
				int index = alignment.getTraceIndex().first();
				XTrace xtrace = log.get(index);
				String caseId = xtrace.getAttributes().get("concept:name").toString();
				
				System.out.printf("[%s] Alignment of caseid %s: "
						+ "No. of generated states: %.2f, "
						+ "No. of queued states: %.2f, "
						+ "No. of traversed arcs: %.2f%n", 
						getClass().getSimpleName(), caseId, stateCount, queuedStates, traversedArcs);
			}
			
			if (logSize > 0) {
				avgLogStateCount = logStateCount / logSize;
				avgLogQueuedStates = logQueuedStates / logSize;
				avgLogTraversedArcs = logTraversedArcs / logSize;
			}
			
			System.out.printf("[%s] (Log level average) No. of generated states: %.2f, "
					+ "No. of queued states: %.2f, No. of traversed arcs: %.2f%n", 
					getClass().getSimpleName(), logStateCount, logQueuedStates, logTraversedArcs);
			
			double costLo = sumLoCosts / sumWeight;
			double costHi = sumHiCosts / sumWeight;
			double percLo = (1.0 - sumHiCosts / sumMaxCosts);
			double percHi = (1.0 - sumLoCosts / sumMaxCosts);
			
			// Save results as a CSV format
			buf.append(parameters.iteration + ", ");
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.modelPath + ", ");
			// log and model names
			buf.append(parameters.log + ", ");
			buf.append(parameters.model + ", ");
			// not monolithic
			buf.append(false + ", ");
			// type of decomposition
			buf.append(parameters.decomposition + ", ");
			buf.append(parameters.recomposeStrategy + ", ");
			buf.append(parameters.logCreationStrategy + ", ");
			buf.append(parameters.preferBorderTransitions + ", ");
			buf.append(parameters.addConflictOnlyOnce + ", ");
			buf.append(parameters.useHideAndReduceAbstraction + ", ");
			buf.append(replayParameters.getGlobalDuration() + ", ");
			buf.append(replayParameters.getLocalDuration() + ", ");
			buf.append(replayParameters.getMoveOnLogCosts() + ", ");
			buf.append(replayParameters.getMoveOnModelCosts() + ", ");
			buf.append(replayParameters.getIntervalRelative() + ", ");
			buf.append(replayParameters.getIntervalAbsolute() + ", ");
			buf.append(replayParameters.getMaxConflicts() + ", ");
			buf.append(replayParameters.getAlignmentPercentage() + ", ");
			buf.append(replayParameters.getNofIterations() + ", ");
			buf.append(costLo + ", ");
			buf.append(costHi + ", ");
			buf.append(percLo + ", ");
			buf.append(percHi + ", ");
			buf.append(numOfAligned + ", ");
			buf.append(numOfToAlign + ", ");
			buf.append(numOfRejected + ", ");
			buf.append(numOfTraces + ", ");
			buf.append(numOfRecompositionSteps + ", ");
			buf.append(time + ", ");
			buf.append(logStateCount + ", ");
			buf.append(logQueuedStates + ", ");
			buf.append(logTraversedArcs + ", ");
			buf.append(avgLogStateCount + ", ");
			buf.append(avgLogQueuedStates + ", ");
			buf.append(avgLogTraversedArcs + "");
		} catch (Exception e) {
			System.out.println("There is an exception: " + e.getMessage());
			buf.append(parameters.logPath + ", ");
			buf.append(parameters.iteration + ", ");
			buf.append("na, na, na, na, na, na, na, na, na, na, na, na, na, "
					+ "na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na, na");
		}
	}
	
	public String toHTMLString(boolean includeHTMLTags) {
		// to a comma separated string (csv) 
		StringBuffer buffer = new StringBuffer();
		if (includeHTMLTags) {
			buffer.append("<html>");
		}
		System.out.println(buf);
		buffer.append(buf);
		if (includeHTMLTags) {
			buffer.append("</html>");
		}
		buffer.append("\n");
		return buffer.toString();
	}
	
	
}