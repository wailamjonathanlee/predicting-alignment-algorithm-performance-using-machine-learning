package org.processmining.decomposedreplayer.dialogs;

import info.clearthought.layout.TableLayout;
import info.clearthought.layout.TableLayoutConstants;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.deckfour.xes.classification.XEventAndClassifier;
import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.classification.XEventLifeTransClassifier;
import org.deckfour.xes.classification.XEventNameClassifier;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;
import org.processmining.acceptingpetrinet.plugins.VisualizeAcceptingPetriNetPlugin;
import org.processmining.decomposedreplayer.parameters.RecomposingReplayParameters;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.util.ui.widgets.ProMList;
import org.processmining.log.dialogs.ClassifierPanel;
import org.processmining.log.utils.XUtils;
import org.processmining.pnetreplayer.dialogs.TransEvClassMappingPanel;
import org.processmining.pnetreplayer.utils.TransEvClassMappingUtils;

import com.fluxicon.slickerbox.components.NiceSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.factory.SlickerFactory;

public class RecomposingReplayDialog extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9221170896562732670L;

	public JPanel getPanel(PluginContext context, XLog log, AcceptingPetriNet net,
			final RecomposingReplayParameters parameters, int n) {
		removeAll();

		int labelHeight = 60;
		int sliderHeight = 30;

		if (n == 0) {
			double size[][] = { { TableLayoutConstants.FILL, TableLayoutConstants.FILL },
					{ TableLayoutConstants.FILL, 30, 30, 30, 30, 30 } };
			setLayout(new TableLayout(size));

			List<XEventClassifier> classifiers = new ArrayList<XEventClassifier>();
			classifiers.addAll(log.getClassifiers());
			if (classifiers.isEmpty()) {
				classifiers.add(new XEventAndClassifier(new XEventNameClassifier(), new XEventLifeTransClassifier()));
			}
			ClassifierPanel classifierPanel = new ClassifierPanel(classifiers, parameters);
			add(classifierPanel, "0, 0");

			/*
			 * Show the accepting Petri net. This may provide information on
			 * which classifier to choose. Real use is that now we're certain
			 * the net has been visualized, so the visualizer can project the
			 * replay results on it.
			 */
			VisualizeAcceptingPetriNetPlugin visualizer = new VisualizeAcceptingPetriNetPlugin();
			add(visualizer.visualize(context, net), "1, 0, 1, 5");

			final NiceSlider moveOnLogSlider = SlickerFactory.instance().createNiceIntegerSlider("Move on Log Costs", 1,
					100, parameters.getMoveOnLogCosts(), Orientation.HORIZONTAL);
			moveOnLogSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMoveOnLogCosts(moveOnLogSlider.getSlider().getValue());
				}
			});
			add(moveOnLogSlider, "0, 1");

			final NiceSlider moveOnModelSlider = SlickerFactory.instance().createNiceIntegerSlider(
					"Move on Model Costs", 1, 100, parameters.getMoveOnModelCosts(), Orientation.HORIZONTAL);
			moveOnModelSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMoveOnModelCosts(moveOnModelSlider.getSlider().getValue());
				}
			});
			add(moveOnModelSlider, "0, 2");

			final JCheckBox planBCheckBox = SlickerFactory.instance()
//					.createCheckBox("Estimate model move costs for empty trace", false);
					.createCheckBox("Add conflict only once", false);
//			planBCheckBox.setSelected(parameters.isPlanB());
			planBCheckBox.setSelected(parameters.isAddConflictOnlyOnce());
			planBCheckBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
//					parameters.setPlanB(planBCheckBox.isSelected());
					parameters.setAddConflictOnlyOnce(planBCheckBox.isSelected());
				}

			});
			planBCheckBox.setOpaque(false);
			planBCheckBox.setPreferredSize(new Dimension(100, 30));
			add(planBCheckBox, "0, 3");

			final JCheckBox useHideAndReduceAbstractionCheckBox = SlickerFactory.instance()
					.createCheckBox("Use hide-and-reduce abstraction", false);
			useHideAndReduceAbstractionCheckBox.setSelected(parameters.isUseHideAndReduceAbstraction());
			useHideAndReduceAbstractionCheckBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					parameters.setUseHideAndReduceAbstraction(useHideAndReduceAbstractionCheckBox.isSelected());
				}

			});
			useHideAndReduceAbstractionCheckBox.setOpaque(false);
			useHideAndReduceAbstractionCheckBox.setPreferredSize(new Dimension(100, 30));
			add(useHideAndReduceAbstractionCheckBox, "0, 4");

			final JCheckBox preferBorderTransitionsCheckBox = SlickerFactory.instance()
					.createCheckBox("Prefer border transitions in alignment", false);
			preferBorderTransitionsCheckBox.setSelected(parameters.isPreferBorderTransitions());
			preferBorderTransitionsCheckBox.addActionListener(new ActionListener() {

				public void actionPerformed(ActionEvent e) {
					parameters.setPreferBorderTransitions(preferBorderTransitionsCheckBox.isSelected());
				}

			});
			preferBorderTransitionsCheckBox.setOpaque(false);
			preferBorderTransitionsCheckBox.setPreferredSize(new Dimension(100, 30));
			add(preferBorderTransitionsCheckBox, "0, 5");

		} else if (n == 1) {
			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));
			Set<XEventClass> activities = new HashSet<XEventClass>();
			XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
			activities.addAll(info.getEventClasses().getClasses());
			activities.add(XUtils.MOVEONMODELACTIVITY);
			parameters.setMapping(TransEvClassMappingUtils.getInstance().getMapping(net.getNet(), activities, parameters.getClassifier()));
			TransEvClassMappingPanel mapPanel = new TransEvClassMappingPanel(new HashSet<XEventClass>(activities),
					parameters);
			add(mapPanel, "0, 0");

		} else if (n == 2) {
			int i = 0;
			double size[][] = { { TableLayoutConstants.FILL },
					{ labelHeight, sliderHeight, labelHeight, sliderHeight, TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));

			add(new JLabel(
					"<html><h2>Select the number of conflicts threshold</h2><p>An alignment is rejected if it has at least this many conflicts.</p></html>"),
					"0, " + i++);
			final NiceSlider maxConflictsSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold", 0, 100,
					parameters.getMaxConflicts(), Orientation.HORIZONTAL);
			maxConflictsSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setMaxConflicts(maxConflictsSlider.getSlider().getValue());
				}
			});
			add(maxConflictsSlider, "0, " + i++);

			add(new JLabel(
					"<html><h2>Select the calculation time threshold</h2><p>An alignment is rejected if it took more than this time (in seconds) to calculate it.</p></html>"),
					"0, " + i++);
			final NiceSlider localDurationSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold", 1,
					1000, parameters.getLocalDuration() / 1000, Orientation.HORIZONTAL);
			localDurationSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setLocalDuration(1000 * localDurationSlider.getSlider().getValue());
				}
			});
			add(localDurationSlider, "0, " + i++);

		} else if (n == 3) {
			int i = 0;
			double size[][] = { { TableLayoutConstants.FILL }, { labelHeight, sliderHeight, labelHeight, sliderHeight,
					labelHeight, sliderHeight, labelHeight, sliderHeight, labelHeight, sliderHeight } };
			setLayout(new TableLayout(size));

			add(new JLabel(
					"<html><h2>Select the overall calculation time threshold</h2><p>No new replay is started if it already took more than this time (in minutes) to calculate the previous replays.</p></html>"),
					"0, " + i++);
			final NiceSlider globalDurationSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold", 1,
					1000, parameters.getGlobalDuration() / (60 * 1000), Orientation.HORIZONTAL);
			globalDurationSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setGlobalDuration(globalDurationSlider.getSlider().getValue() * 60 * 1000);
				}
			});
			add(globalDurationSlider, "0, " + i++);

			add(new JLabel(
					"<html><h2>Select the alignment percentage threshold</h2><p>No new replay is started if the percentage of traces under total border agreement exceeds this percentage.</p></html>"),
					"0, " + i++);
			final NiceSlider alignmentPercentageSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold",
					0, 100, parameters.getAlignmentPercentage(), Orientation.HORIZONTAL);
			alignmentPercentageSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setAlignmentPercentage(alignmentPercentageSlider.getSlider().getValue());
				}
			});
			add(alignmentPercentageSlider, "0, " + i++);

			add(new JLabel(
					"<html><h2>Select the fitness interval percentage threshold</h2><p>No new replay is started if the lower bound exceeds this percentage of the upper bound.</p></html>"),
					"0, " + i++);
			final NiceSlider intervalRelativeSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold", 0,
					100, parameters.getIntervalRelative(), Orientation.HORIZONTAL);
			intervalRelativeSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setIntervalRelative(intervalRelativeSlider.getSlider().getValue());
				}
			});
			add(intervalRelativeSlider, "0, " + i++);

			add(new JLabel(
					"<html><h2>Select the fitness interval width threshold</h2><p>No new replay is started if the interval width is below this width.</p></html>"),
					"0, " + i++);
			final NiceSlider intervalAbsoluteSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold", 0,
					100, parameters.getIntervalAbsolute(), Orientation.HORIZONTAL);
			intervalAbsoluteSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setIntervalAbsolute(intervalAbsoluteSlider.getSlider().getValue());
				}
			});
			add(intervalAbsoluteSlider, "0, " + i++);

			add(new JLabel(
					"<html><h2>Select the number of replays threshold</h2><p>No new replay is started if this number of replays have already been done.</p></html>"),
					"0, " + i++);
			final NiceSlider nofIterationsSlider = SlickerFactory.instance().createNiceIntegerSlider("Threshold", 1,
					100, parameters.getNofIterations(), Orientation.HORIZONTAL);
			nofIterationsSlider.addChangeListener(new ChangeListener() {

				public void stateChanged(ChangeEvent e) {
					parameters.setNofIterations(nofIterationsSlider.getSlider().getValue());
				}
			});
			add(nofIterationsSlider, "0, " + i++);
		} else if (n == 4) {
			double size[][] = { { TableLayoutConstants.FILL }, { TableLayoutConstants.FILL } };
			setLayout(new TableLayout(size));

			setOpaque(false);

			List<XEventClass> activities = new ArrayList<XEventClass>();
			final XLogInfo info = XLogInfoFactory.createLogInfo(log, parameters.getClassifier());
			activities.addAll(info.getEventClasses().getClasses());
			Collections.sort(activities);
			DefaultListModel<XEventClass> listModel = new DefaultListModel<XEventClass>();
			for (XEventClass activity : activities) {
				listModel.addElement(activity);
			}
			final ProMList<XEventClass> list = new ProMList<XEventClass>("Select decomposable activities", listModel);
			list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

//			AcceptingPetriNet seseNet = (new SESEAlgorithm()).apply(context, net);
//			Collection<XEventClass> seseActivities = new HashSet<XEventClass>();
//			for (Transition transition : seseNet.getNet().getTransitions()) {
//				if (transition.getAttributeMap().get(AttributeMap.FILLCOLOR) == Color.GREEN
//						|| transition.getAttributeMap().get(AttributeMap.FILLCOLOR) == Color.RED) {
//					seseActivities.add(new XEventClass(transition.getLabel(), 0));
//				}
//			}

			// Preselect all, to retain old behavior if no action is taken here.
//			int[] indices = new int[seseActivities.size()];
			int[] indices = new int[listModel.getSize()];
			int j = 0;
			for (int i = 0; i < listModel.getSize(); i++) {
//				if (seseActivities.contains(listModel.get(i))) {
					indices[j++] = i;
				}
//			}
			list.setSelectedIndices(indices);
			Set<XEventClass> unsplittableActivities = new HashSet<XEventClass>(
					info.getEventClasses().getClasses());
			unsplittableActivities.removeAll(list.getSelectedValuesList());
			parameters.setUnsplittableActivities(unsplittableActivities);
			list.addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent e) {
					Set<XEventClass> unsplittableActivities = new HashSet<XEventClass>(
							info.getEventClasses().getClasses());
					unsplittableActivities.removeAll(list.getSelectedValuesList());
					System.out.println("[RecomposingReplayDialog] Unsplittable = " + unsplittableActivities);
					parameters.setUnsplittableActivities(unsplittableActivities);
				}
			});
			list.setPreferredSize(new Dimension(100, 100));
			add(list, "0, 0");
		}

		return this;
	}

}