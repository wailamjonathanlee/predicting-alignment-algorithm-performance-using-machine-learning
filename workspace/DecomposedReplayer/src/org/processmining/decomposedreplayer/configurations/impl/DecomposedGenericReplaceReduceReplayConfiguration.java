package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionReplaceReduceStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

public class DecomposedGenericReplaceReduceReplayConfiguration extends DecomposedAbstractGenericReplaceReplayConfiguration {

	public final static String NAME = "Hide and reduce";

	public String getName() {
		return NAME;
	}

	public String getStrategy() {
		return DecompositionReplaceReduceStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.setEnabled(true);
	}

}