package org.processmining.decomposedreplayer.workspaces;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.factory.XFactory;
import org.deckfour.xes.factory.XFactoryRegistry;
import org.deckfour.xes.model.XLog;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

public class RecomposingReplayWorkspace {

	/**
	 * Factory to use to create a new log.
	 */
	public XFactory factory;
	/**
	 * The log to replay next. Initially, this will be the original log.
	 */
	public XLog log;
	/**
	 * The alignments under total border agreement found so far.
	 */
	public Set<SyncReplayResult> alignments;
	/**
	 * The alignments rejected so far.
	 */
	public Set<SyncReplayResult> pseudoAlignments;
	/**
	 * The alignments still to replay, if not stopped earlier.
	 */
	public Set<SyncReplayResult> openAlignments;
	/**
	 * The parameters to use next. Initially, this will be the original parameters.
	 */
	public RecomposingReplayParameters parameters;
	/**
	 * A map that maps the indices in the current log to the indices in the original log.
	 * A replay uses these indices to link back to the traces in the log.
	 */
	public Map<Integer, Integer> indexMap;
	/**
	 * The number of iterations (replays) performed so far.
	 */
	public int nofIterations;
	
	public RecomposingReplayWorkspace(XLog log, RecomposingReplayParameters parameters) {
		factory = XFactoryRegistry.instance().currentDefault();
		this.log = log; 
		this.parameters = new RecomposingReplayParameters(parameters);
		alignments = new HashSet<SyncReplayResult>();
		pseudoAlignments = new HashSet<SyncReplayResult>();
		openAlignments = new HashSet<SyncReplayResult>();
		indexMap = new HashMap<Integer, Integer>(log.size());
		for (int i = 0; i < log.size(); i++) {
			indexMap.put(i,  i);
		}
		nofIterations = 1;
	}	
}
