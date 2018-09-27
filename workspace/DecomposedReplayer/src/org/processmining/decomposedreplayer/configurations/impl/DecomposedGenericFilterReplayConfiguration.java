package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionGenericStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedGenericFilterReplayConfiguration extends DecomposedAbstractGenericFilterReplayConfiguration {

	public final static String NAME = "Decompose";

	public String getName() {
		return NAME;
	}
	
	public String getStrategy() {
		return DecompositionGenericStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.setEnabled(true);
	}
}
