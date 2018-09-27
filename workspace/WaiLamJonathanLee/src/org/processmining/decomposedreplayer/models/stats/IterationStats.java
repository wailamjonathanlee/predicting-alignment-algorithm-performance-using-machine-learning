package org.processmining.decomposedreplayer.models.stats;

public class IterationStats {
	
	public static String HEADER = "Iteration, NofTraceToAlign, "
			+ "NofAlignmentValid, NofAlignmentOpen, NofAlignmentRejected, "
			+ "NofLeftOutTraces, NofSubnets, NofRecomposeActivities, "
			+ "NofBorderActivities, LowerBoundCosts";

	private int iteration = -1;
	private int nofTraceToAlign = -1;
	private int nofAlignmentOpen = -1;
	private int nofAlignmentValid = -1;
	private int nofAlignmentRejected = -1;
	private int nofRecomposeActivities = -1;
	private int nofSubnets = -1;
	private int nofBorderActivities = -1;
	private int nofLeftOutTraces = -1;
	private double lowerBoundCosts = 0.0;
	
	public int getIteration() {
		return iteration;
	}

	public void setIteration(int iteration) {
		this.iteration = iteration;
	}

	public int getNofTraceToAlign() {
		return nofTraceToAlign;
	}
	
	public void setNofTraceToAlign(int nofTraceToAlign) {
		this.nofTraceToAlign = nofTraceToAlign;
	}
	
	public int getNofAlignmentOpen() {
		return nofAlignmentOpen;
	}
	
	public void setNofAlignmentOpen(int nofAlignmentOpen) {
		this.nofAlignmentOpen = nofAlignmentOpen;
	}
	
	public int getNofAlignmentValid() {
		return nofAlignmentValid;
	}
	
	public void setNofAlignmentValid(int nofAlignmentValid) {
		this.nofAlignmentValid = nofAlignmentValid;
	}
	
	public int getNofAlignmentRejected() {
		return nofAlignmentRejected;
	}
	
	public void setNofAlignmentRejected(int nofAlignmentRejected) {
		this.nofAlignmentRejected = nofAlignmentRejected;
	}

	@Override
	public String toString() {
		return "Iteration: " + iteration + "\n" + 
				"Nb of traces replayed: " + getNofTraceToAlign() + "\n" + 
				"Nb of traces aligned: " + getNofAlignmentValid() + "\n" + 
				"Nb of traces open: " + getNofAlignmentOpen() + "\n" + 
				"Nb of traces rejected: " + getNofAlignmentRejected() + "\n" + 
				"Nb of left out traces: " + getNofLeftOutTraces() + "\n" +
				"Nb of subnets: " + getNofSubnets() + "\n" +
				"Nb of recompose activities: " + getNofRecomposeActivities() + "\n" + 
				"Nb of border activities: " + getNofBorderActivities() + "\n" + 
				"Lower bound costs: " + getLowerBoundCosts() + "\n";
	}
	
	public String toRowString() {
		return getIteration() + ", " + 
				getNofTraceToAlign() + ", " + 
				getNofAlignmentValid() + ", " + 
				getNofAlignmentOpen() + ", " + 
				getNofAlignmentRejected() + ", " + 
				getNofLeftOutTraces() + ", " + 
				getNofSubnets() + ", " +
				getNofRecomposeActivities() + ", " + 
				getNofBorderActivities() + ", " + 
				getLowerBoundCosts();
	}

	public int getNofRecomposeActivities() {
		return nofRecomposeActivities;
	}

	public void setNofRecomposeActivities(int nofRecomposeActivities) {
		this.nofRecomposeActivities = nofRecomposeActivities;
	}

	public int getNofBorderActivities() {
		return nofBorderActivities;
	}

	public void setNofBorderActivities(int nofBorderActivities) {
		this.nofBorderActivities = nofBorderActivities;
	}

	public int getNofLeftOutTraces() {
		return nofLeftOutTraces;
	}

	public void setNofLeftOutTraces(int nofLeftOutTraces) {
		this.nofLeftOutTraces = nofLeftOutTraces;
	}

	public int getNofSubnets() {
		return nofSubnets;
	}

	public void setNofSubnets(int nofSubnets) {
		this.nofSubnets = nofSubnets;
	}

	public double getLowerBoundCosts() {
		return lowerBoundCosts;
	}

	public void setLowerBoundCosts(double lowerBoundCosts) {
		this.lowerBoundCosts = lowerBoundCosts;
	}
	
}