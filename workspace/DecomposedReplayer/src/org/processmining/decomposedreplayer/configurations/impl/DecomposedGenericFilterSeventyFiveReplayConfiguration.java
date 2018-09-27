package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericSeventyFiveStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedGenericFilterSeventyFiveReplayConfiguration extends DecomposedAbstractGenericFilterReplayConfiguration {

	public final static String NAME = "Decompose 75%";

	public String getName() {
		return NAME;
	}
	
	public String getStrategy() {
		return DecompositionGenericSeventyFiveStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.getSlider().setValue(75);
		slider.setEnabled(false);
	}
}