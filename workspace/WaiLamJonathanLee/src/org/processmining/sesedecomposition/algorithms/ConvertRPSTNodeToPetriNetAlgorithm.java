package org.processmining.sesedecomposition.algorithms;

import java.util.Map;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.models.graphbased.directed.DirectedGraphElement;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetNode;
import org.processmining.models.graphbased.directed.petrinet.elements.Arc;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.sesedecomposition.models.rpst.PetriNetRPSTNode;
import org.processmining.sesedecomposition.parameters.ConvertRPSTNodeToPetriNetParameters;

public class ConvertRPSTNodeToPetriNetAlgorithm {
	
	public Petrinet apply(PluginContext context, PetriNetRPSTNode node, ConvertRPSTNodeToPetriNetParameters parameters) {
		Map<DirectedGraphElement, DirectedGraphElement> map = parameters.getMap();
	
		Petrinet net = PetrinetFactory.newPetrinet(node.getName());	
		for (Transition origT: node.getTrans()){
			Transition newT = net.addTransition(origT.getLabel());
			newT.setInvisible(origT.isInvisible());
			map.put(origT, newT);
		}
		for(Place origP: node.getPlaces()){
			Place newP = net.addPlace(origP.getLabel());
			map.put(origP, newP);
		}
		for(Arc origA: node.getArcs()){
			PetrinetNode source = (PetrinetNode) map.get(origA.getSource());
			PetrinetNode target = (PetrinetNode) map.get(origA.getTarget());
			if(source instanceof Transition){
				Arc newA = net.addArc((Transition)source, (Place)target, origA.getWeight());
				map.put(origA, newA);
			}
			else if (source instanceof Place){
				Arc newA = net.addArc((Place) source, (Transition) target, origA.getWeight());
				map.put(origA, newA);
			}
		}
		return net;
	}
}