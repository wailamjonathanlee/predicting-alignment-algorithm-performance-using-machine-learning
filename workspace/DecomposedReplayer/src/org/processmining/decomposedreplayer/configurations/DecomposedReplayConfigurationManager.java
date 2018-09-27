package org.processmining.decomposedreplayer.configurations;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterFiftyReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterHundredReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericFilterSeventyFiveReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericReplaceReduceReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedGenericReplaceReplayConfiguration;
import org.processmining.decomposedreplayer.configurations.impl.DecomposedNotDecomposedReplayConfiguration;

public class DecomposedReplayConfigurationManager {

	private static DecomposedReplayConfigurationManager instance = null;
	private List<DecomposedReplayConfiguration> configurations;
	private DecomposedReplayConfiguration defaultConfiguration;
	private boolean sorted;

	private DecomposedReplayConfigurationManager() {
		configurations = new ArrayList<DecomposedReplayConfiguration>();
		defaultConfiguration = null;
		sorted = false;
//		register(new DecomposedDefaultReplayConfiguration(), true);
//		register(new DecomposedFilterReplayConfiguration(), false);
//		register(new DecomposedReplaceReplayConfiguration(), false);
//		register(new DecomposedReplaceReduceReplayConfiguration(), false);
		register(new DecomposedGenericFilterReplayConfiguration(), true);
		register(new DecomposedGenericFilterHundredReplayConfiguration(), false);
		register(new DecomposedGenericFilterSeventyFiveReplayConfiguration(), false);
		register(new DecomposedGenericFilterFiftyReplayConfiguration(), false);
		register(new DecomposedGenericReplaceReplayConfiguration(), false);
		register(new DecomposedGenericReplaceReduceReplayConfiguration(), false);
//		register(new DecomposedMatrixFilterReplayConfiguration(), false);
//		register(new DecomposedMatrixReplaceReplayConfiguration(), false);
//		register(new DecomposedMatrixReplaceReduceReplayConfiguration(), false);
		register(new DecomposedNotDecomposedReplayConfiguration(), false);
//		register(new DecomposedConnectedReplayConfiguration(), false);
	}

	public static DecomposedReplayConfigurationManager getInstance() {
		if (instance == null) {
			instance = new DecomposedReplayConfigurationManager();
		}
		return instance;
	}
	
	public void register(DecomposedReplayConfiguration configuration, boolean isDefault) {
		configurations.add(configuration);
		if (isDefault) {
			defaultConfiguration = configuration;
		}
		sorted = false;
	}

	public List<DecomposedReplayConfiguration> getConfigurations() {
		if (!sorted) {
			Collections.sort(configurations, new Comparator<DecomposedReplayConfiguration>() {
				public int compare(DecomposedReplayConfiguration m1, DecomposedReplayConfiguration m2) {
					return m1.getName().compareTo(m2.getName());
				}
			});
			sorted = true;
		}
		return configurations;
	}
	
	public boolean isDefault(DecomposedReplayConfiguration configuration) {
		return configuration == defaultConfiguration;
	}
	
	public DecomposedReplayConfiguration getConfiguration(String name) {
		if (name != null) {
			for (DecomposedReplayConfiguration configuration : configurations) {
				if (name.equals(configuration.getName())) {
					return configuration;
				}
			}
		}
		return defaultConfiguration;
	}
}