package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericFiftyStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedGenericFilterFiftyReplayConfiguration extends DecomposedAbstractGenericFilterReplayConfiguration {

	public final static String NAME = "Decompose 50%";

	public String getName() {
		return NAME;
	}
	
	public String getStrategy() {
		return DecompositionGenericFiftyStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.getSlider().setValue(50);
		slider.setEnabled(false);
	}
}