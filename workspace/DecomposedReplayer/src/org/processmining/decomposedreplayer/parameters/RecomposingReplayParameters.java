package org.processmining.decomposedreplayer.parameters;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.basicutils.parameters.impl.PluginParametersImpl;
import org.processmining.decomposedreplayer.workspaces.RecomposingReplayWorkspace;
import org.processmining.framework.util.Pair;
import org.processmining.log.parameters.ClassifierParameter;
import org.processmining.log.utils.XUtils;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;
import org.processmining.pnetreplayer.parameters.TransEvClassMappingParameter;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

public class RecomposingReplayParameters extends PluginParametersImpl
		implements ClassifierParameter, TransEvClassMappingParameter {

	/*
	 * The classifier to use. Maps log events onto activities.
	 */
	private XEventClassifier classifier;
	/*
	 * The transition-to-activity mapping to use. Maps net transitions into activities.
	 */
	private TransEvClassMapping mapping;
	/*
	 * Start time of the entire recomposing replay.
	 */
	private long millis;
	/*
	 * Threshold for the total calculation time of the entire recomposing replay.
	 * If the current total calculation time > this threshold, no new iteration should be started.
	 */
	private int globalDuration; // in millis
	/*
	 * Threshold for the calculation time of a single alignment.
	 * If the calculation time of an alignment > this threshold, the alignment must be rejected 
	 * for the next iteration.
	 */
	private int localDuration; // in millis
	/*
	 * Collection of activities that may not be on the border of any subnet.
	 * If empty, the decomposition will be maximal. 
	 */
	private Set<XEventClass> unsplittableActivities;
	/*
	 * The replay costs to use for every mode-on-log step.
	 */
	private int moveOnLogCosts;
	/*
	 * The replay costs to use for every move-on-model step.
	 */
	private int moveOnModelCosts;
	/*
	 * Threshold for the percentage of the lower bound when compared to the upper bound of the fitness interval.
	 * If the lower bound times 100 > the upper bound times this threshold, no new iteration should be started. 
	 */
	private int intervalRelative;
	/*
	 * Threshold for the absolute width of the fitness interval.
	 * If the upper bound minus the lower bound < this threshold, no new iteration should be started. 
	 */
	private int intervalAbsolute;
	/*
	 * Threshold for number of conflicts in a single alignment.
	 * If the actual number of conflicts in an alignment > this threshold, 
	 * the alignment should be rejected for a new iteration.
	 */
	private int maxConflicts;
	/*
	 * Threshold for the percentage of traces aligned under total border agreement.
	 * If the current percentage of traces that have been aligned under total border agreement >
	 * this threshold, no new iteration should be started.
	 */
	private int alignmentPercentage;
	/*
	 * Threshold for the number of iterations.
	 * If the current iteration > this threshold, no new iteration should be started.
	 */
	private int nofIterations;
	private boolean planB;
	private boolean useHideAndReduceAbstraction;
	private boolean addConflictOnlyOnce;
	private boolean preferBorderTransitions;

	public RecomposingReplayParameters(XLog log, AcceptingPetriNet net, XEventClassifier classifier) {
		super();
		setClassifier(classifier);
		XLogInfo info = XLogInfoFactory.createLogInfo(log, classifier);
		Set<XEventClass> activities = new HashSet<XEventClass>();
		activities.addAll(info.getEventClasses().getClasses());
		activities.add(XUtils.MOVEONMODELACTIVITY);
		setMapping(TransEvClassMappingUtils.getInstance().getMapping(net.getNet(), activities, classifier));
		setGlobalDuration(1000 * 60 * 1000); // Stop if at least 1000 minutes have passed.
		setLocalDuration(1000 * 1000); // Reject if average alignment takes more than 1000 seconds.
		setMillis(System.currentTimeMillis());
		setUnsplittableActivities(new HashSet<XEventClass>());
		setMoveOnModelCosts(4);
		setMoveOnLogCosts(10);
		setIntervalRelative(100); // Stop if loFi / hiFi >= 0.99
		setIntervalAbsolute(0); // Stop if hiFi - loFi <= 0.1
		setMaxConflicts(100); // Reject if conflict on 10 different activities.
		setAlignmentPercentage(100); // Stop if more than 90% of all traces correpond to alignments.
		setNofIterations(100); // Stop after 10 iterations.
		setPlanB(false);
		setUseHideAndReduceAbstraction(true);
		setAddConflictOnlyOnce(false);
		setPreferBorderTransitions(true);
	}
	
	public RecomposingReplayParameters(XLog log, AcceptingPetriNet net) {
		this(log, net, XUtils.getDefaultClassifier(log));
	}
	
	public RecomposingReplayParameters(RecomposingReplayParameters parameters) {
		super();
		setClassifier(parameters.getClassifier());
		setMapping(parameters.getMapping());
		setGlobalDuration(parameters.getGlobalDuration());
		setLocalDuration(parameters.getLocalDuration());
		setMillis(parameters.getMillis());
		setUnsplittableActivities(parameters.getUnsplittableActivities());
		setMoveOnModelCosts(parameters.getMoveOnModelCosts());
		setMoveOnLogCosts(parameters.getMoveOnLogCosts());
		setIntervalRelative(parameters.getIntervalRelative());
		setIntervalAbsolute(parameters.getIntervalAbsolute());
		setMaxConflicts(parameters.getMaxConflicts());
		setAlignmentPercentage(parameters.getAlignmentPercentage());
		setNofIterations(parameters.getNofIterations());
		setPlanB(parameters.isPlanB());
		setUseHideAndReduceAbstraction(parameters.isUseHideAndReduceAbstraction());
		setAddConflictOnlyOnce(parameters.isAddConflictOnlyOnce());
		setPreferBorderTransitions(parameters.isPreferBorderTransitions());
	}
	
	public void setMapping(TransEvClassMapping mapping) {
		this.mapping = mapping;
	}

	public TransEvClassMapping getMapping() {
		return mapping;
	}

	public void setClassifier(XEventClassifier classifier) {
		this.classifier = classifier;
	}

	public XEventClassifier getClassifier() {
		return classifier;
	}

	public void setGlobalDuration(int duration) {
		this.globalDuration = duration;
	}
	
	public int getGlobalDuration() {
		return globalDuration;
	}
	
	public void setMillis(long millis) {
		this.millis = millis;
	}
	
	public long getMillis() {
		return millis;
	}
	
	public boolean reject(SyncReplayResult traceResult, Map<XEventClass, Double> conflicts) {
		if (conflicts.keySet().size() > maxConflicts) {
			/*
			 * Too many conflicts. Reject.
			 */
			System.out.println("[RecomposingReplayParameters] Rejected: Threshold for number of border agreement issues exceeded.");
			return true;
		}
		if (traceResult.getInfo().get(PNRepResult.TIME) > localDuration) {
			/*
			 * Too much time spend on this alignment already. Reject.
			 */
			System.out.println("[RecomposingReplayParameters] Rejected: Threshold for alignment computation time (in ms) exceeded.");
			return true;
		}
		/*
		 * Do not reject.
		 */
		return false;
	}
	
	public boolean stop(RecomposingReplayWorkspace workspace, Pair<Double, Double> fitnessInterval) {
		if (workspace.nofIterations > getNofIterations()) {
			/*
			 * Too many iterations already done. Stop.
			 */
			System.out.println("[RecomposingReplayParameters] Stopped: Threshold for number of iterations exceeded.");
			return true;
		}
		if (100 * fitnessInterval.getFirst() > getIntervalRelative() * fitnessInterval.getSecond()) {
			/*
			 * Fitness interval lower bound close enough to upper bound. Stop.
			 */
			System.out.println("[RecomposingReplayParameters] Stopped: Threshold for overall fitness interval (relative, in perc) exceeded.");
			return true;
		}
		if (100 * fitnessInterval.getFirst() > 100 * fitnessInterval.getSecond() - getIntervalAbsolute()) {
			/*
			 * Interval width small enough. Stop.
			 */
			System.out.println("[RecomposingReplayParameters] Stopped: Threshold for overall fitness interval (absolute, in perc) exceeded.");
			return true;
		}
		int nofOKTraces = 0;
		int nofNotOKTraces = 0;
		for (SyncReplayResult alignment : workspace.alignments) {
			nofOKTraces += alignment.getTraceIndex().size();
		}
		for (SyncReplayResult alignment : workspace.pseudoAlignments) {
			nofNotOKTraces += alignment.getTraceIndex().size();
		}
		for (SyncReplayResult alignment : workspace.openAlignments) {
			nofNotOKTraces += alignment.getTraceIndex().size();
		}
		if (nofOKTraces * 100 > (nofOKTraces + nofNotOKTraces) * alignmentPercentage) {
			/*
			 * Sufficient traces have been aligned under total border agreement. Stop.
			 */
			System.out.println("[RecomposingReplayParameters] Stopped: Threshold for percentage of traces under total border agreement exceeded.");
			return true;
		}
		if (System.currentTimeMillis() > (millis + globalDuration)) {
			/*
			 * Too much has been spend already. Stop.
			 */
			System.out.println("[RecomposingReplayParameters] Stopped: Threshold for overall computation time (in seconds) exceeded.");
			return true;
		}
		/*
		 * Do not stop.
		 */
		return false;
	}

	public Set<XEventClass> getUnsplittableActivities() {
		return unsplittableActivities;
	}

	public void setUnsplittableActivities(Set<XEventClass> unsplittableActivities) {
		this.unsplittableActivities = unsplittableActivities;
	}

	public int getMoveOnLogCosts() {
		return moveOnLogCosts;
	}

	public void setMoveOnLogCosts(int moveOnLogCosts) {
		this.moveOnLogCosts = moveOnLogCosts;
	}

	public int getMoveOnModelCosts() {
		return moveOnModelCosts;
	}

	public void setMoveOnModelCosts(int moveOnModelCosts) {
		this.moveOnModelCosts = moveOnModelCosts;
	}

	public int getLocalDuration() {
		return localDuration;
	}

	public void setLocalDuration(int localDuration) {
		this.localDuration = localDuration;
	}

	public int getIntervalRelative() {
		return intervalRelative;
	}

	public void setIntervalRelative(int intervalRelative) {
		this.intervalRelative = intervalRelative;
	}

	public int getIntervalAbsolute() {
		return intervalAbsolute;
	}

	public void setIntervalAbsolute(int intervalAbsolute) {
		this.intervalAbsolute = intervalAbsolute;
	}

	public int getMaxConflicts() {
		return maxConflicts;
	}

	public void setMaxConflicts(int maxConflicts) {
		this.maxConflicts = maxConflicts;
	}

	public int getAlignmentPercentage() {
		return alignmentPercentage;
	}

	public void setAlignmentPercentage(int alignmentPercentage) {
		this.alignmentPercentage = alignmentPercentage;
	}

	public int getNofIterations() {
		return nofIterations;
	}

	public void setNofIterations(int nofIterations) {
		this.nofIterations = nofIterations;
	}

	public boolean isPlanB() {
		return planB;
	}

	public void setPlanB(boolean planB) {
		this.planB = planB;
	}

	public boolean isUseHideAndReduceAbstraction() {
		return useHideAndReduceAbstraction;
	}

	public void setUseHideAndReduceAbstraction(boolean useHideAndReduceAbstraction) {
		this.useHideAndReduceAbstraction = useHideAndReduceAbstraction;
	}

	public boolean isAddConflictOnlyOnce() {
		return addConflictOnlyOnce;
	}

	public void setAddConflictOnlyOnce(boolean addConflictOnlyOnce) {
		this.addConflictOnlyOnce = addConflictOnlyOnce;
	}

	public boolean isPreferBorderTransitions() {
		return preferBorderTransitions;
	}

	public void setPreferBorderTransitions(boolean preferBorderTransitions) {
		this.preferBorderTransitions = preferBorderTransitions;
	}
}