package org.processmining.sesedecomposition.models.rpst;

import java.util.Collection;

import org.processmining.acceptingpetrinet.models.AcceptingPetriNet;

import edu.uci.ics.jung.graph.DirectedGraph;


/**
 * Data structure representing the RPST structure of a Petri Net.
 * 
 * See: Artem Polyvyanyy, Jussi Vanhatalo, Hagen Volzer: Simplified Computation 
 * and Generalization of the Refined Process Structure Tree. WS-FM 2010: 25-41
 * 
 * @author Jorge Munoz-Gama (jmunoz)
 */
public class PetriNetRPST implements Cloneable {
	
	// Name of the RPST
	private String name;
	
	// Petri Net base of the RPST decomposition
	private AcceptingPetriNet net;
	
	// Tree Structure
	private DirectedGraph<PetriNetRPSTNode, String> tree;
	
	// root
	private PetriNetRPSTNode root;
	
	protected PetriNetRPST(){	
	}
	
	public PetriNetRPST(String name, AcceptingPetriNet net, 
			DirectedGraph<PetriNetRPSTNode, String> tree, PetriNetRPSTNode root){
		setName(name);
		setNet(net);
		setTree(tree);
		setRoot(root);
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public AcceptingPetriNet getNet() {
		return net;
	}
	
	public void setNet(AcceptingPetriNet net) {
		this.net = net;
	}

	public PetriNetRPSTNode getRoot() {
		return root;
	}
	
	public void setRoot(PetriNetRPSTNode root) {
		this.root = root;
	}
	
	public Collection<PetriNetRPSTNode> getNodes(){
		return tree.getVertices();
	}

	public DirectedGraph<PetriNetRPSTNode, String> getTree() {
		return tree;
	}
	
	public void setTree(DirectedGraph<PetriNetRPSTNode, String> tree) {
		this.tree = tree;
	}
	
	public Collection<PetriNetRPSTNode> getChildren(PetriNetRPSTNode parent){
		return tree.getSuccessors(parent);
	}
}