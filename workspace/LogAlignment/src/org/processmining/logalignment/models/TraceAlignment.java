package org.processmining.logalignment.models;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.deckfour.xes.classification.XEventClass;
import org.processmining.framework.util.HTMLToString;
import org.processmining.framework.util.Pair;
import org.processmining.plugins.petrinet.replayresult.StepTypes;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

public interface TraceAlignment extends HTMLToString {

	public void init();
	public int getSize();
    public List<Pair<StepTypes, Pair<XEventClass, String>>> getLegalMoves();
	public void setLegalMoves(List<Pair<StepTypes, Pair<XEventClass, String>>> legalMoves);
	public void addLegalMove(StepTypes stepType, XEventClass activity, String transitionId);
	public List<XEventClass> getLogMoves();
	public void exportToCSVFile(CsvWriter writer) throws IOException;
	public void importFromCSVFile(CsvReader reader, Map<String, XEventClass> activities) throws IOException;
	public void setCosts(double costs);
	public double getCosts();
	public void setMaxCosts(double costs);
	public double getMaxCosts();
	public double getDecomposedSyncMoveNumerator();
	public void setDecomposedSyncMoveNumerator(double numerator);
	public double getDecomposedSyncMoveDenominator();
	public void setDecomposedSyncMoveDenominator(double denominator);
	public boolean isReliable();
	public void setReliable(boolean isReliable);
	public void setStateCount(double stateCount);
	public double getStateCount();
	public void setQueuedStates(double queuedStates);
	public double getQueuedStates();
	public void setTraversedArcs(double traversedArcs);
	public double getTraversedArcs();
	
	public void addConflictingActivity(XEventClass activity);
	public Map<XEventClass, Double> getConflictingActivities();
	public void setMillis(double millis);
	public double getMillis();
}