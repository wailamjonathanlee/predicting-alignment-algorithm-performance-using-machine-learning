package org.processmining.decomposedreplayer.help;

public class DecomposedReplayHelp {

	public final static String TEXT = ""
			+ "Replays the given log on the given accepting Petri net using decomposition. "
			+ "First, the log and net will be decomposed into sublogs and subnets. "
			+ "Second, each sublog will be replayed on the corresponding subnet. "
			+ "Third, the resulting replay results will be combined in a single replay result, which is returned. ";
}
