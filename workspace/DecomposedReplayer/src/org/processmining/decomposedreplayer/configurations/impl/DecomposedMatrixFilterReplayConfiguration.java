package org.processmining.decomposedreplayer.configurations.impl;

import org.processmining.acceptingpetrinetdecomposer.strategies.impl.DecompositionFilterStrategy;

import com.fluxicon.slickerbox.components.NiceSlider;

/*
 * This configuration does not always result in positive costs if the non-decomposed configuration results in positive costs.
 * This is caused by the fact that source and sink places get lost in the decomposition, as they are no in-between any
 * two activities.
 * Therefore, it is better not to use this configuration.
 */
@Deprecated
public class DecomposedMatrixFilterReplayConfiguration extends DecomposedAbstractMatrixFilterReplayConfiguration {

	public final static String NAME = "Decompose using matrix";

	public String getName() {
		return NAME;
	}

	public String getStrategy() {
		return DecompositionFilterStrategy.NAME;
	}

	public void update(NiceSlider slider) {
		slider.setEnabled(true);
	}

}
