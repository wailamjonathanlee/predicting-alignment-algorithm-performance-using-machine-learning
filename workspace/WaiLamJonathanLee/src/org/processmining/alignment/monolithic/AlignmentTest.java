package org.processmining.alignment.monolithic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.deckfour.xes.classification.XEventClass;
import org.deckfour.xes.classification.XEventClasses;
import org.deckfour.xes.classification.XEventClassifier;
import org.deckfour.xes.in.XUniversalParser;
import org.deckfour.xes.info.XLogInfo;
import org.deckfour.xes.info.XLogInfoFactory;
import org.deckfour.xes.model.XLog;
import org.jbpt.petri.Flow;
import org.jbpt.petri.NetSystem;
import org.jbpt.petri.io.PNMLSerializer;
import org.processmining.alignment.utils.AlignUtils;
import org.processmining.alignment.utils.StringFileReader;
import org.processmining.models.graphbased.directed.petrinet.Petrinet;
import org.processmining.models.graphbased.directed.petrinet.PetrinetGraph;
import org.processmining.models.graphbased.directed.petrinet.elements.Place;
import org.processmining.models.graphbased.directed.petrinet.elements.Transition;
import org.processmining.models.graphbased.directed.petrinet.impl.PetrinetFactory;
import org.processmining.models.semantics.petrinet.Marking;
import org.processmining.plugins.connectionfactories.logpetrinet.TransEvClassMapping;
import org.processmining.plugins.petrinet.replayresult.PNRepResult;
import org.processmining.plugins.replayer.replayresult.SyncReplayResult;

import lpsolve.LpSolve;
import nl.tue.alignment.Progress;
import nl.tue.alignment.Replayer;
import nl.tue.alignment.ReplayerParameters;
import nl.tue.alignment.Utils;
import nl.tue.alignment.algorithms.ReplayAlgorithm;
import nl.tue.alignment.algorithms.ReplayAlgorithm.Debug;

public class AlignmentTest {

	private static final int THREADS = Math.max(1, Runtime.getRuntime().availableProcessors() / 4);
	private static String SEP = Utils.SEP;
	private static final Logger LOGGER = Logger.getLogger(AlignmentTest.class.getName());
	private static FileHandler fh = null;
	
	static {
		
		try {
			System.loadLibrary("lpsolve55");
			System.loadLibrary("lpsolve55j");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		LpSolve.lpSolveVersion();
	}
	
	public static void setupLogger(String logFp) {
		
		// set up the logger handler
		try {
			
			fh = new FileHandler(logFp, false);
			fh.setFormatter(new SimpleFormatter());
			LOGGER.addHandler(fh);

		} catch (SecurityException | IOException e) {
			
			e.printStackTrace();
			LOGGER.log(Level.CONFIG, e.toString(), e);
			
		}
		
	}
	
	private static Petrinet importNet(String netFp) {
		
		PNMLSerializer PNML = new PNMLSerializer();
		NetSystem sys = PNML.parse(netFp);
		
		Petrinet net = PetrinetFactory.newPetrinet(netFp);
		
		// places
		Map<org.jbpt.petri.Place, Place> p2p = new HashMap<org.jbpt.petri.Place, Place>();
		
		for (org.jbpt.petri.Place p: sys.getPlaces()) {
			Place pp = net.addPlace(p.toString());
			p2p.put(p, pp);
		}
		
		// transitions
		Map<org.jbpt.petri.Transition, Transition> t2t = new HashMap<org.jbpt.petri.Transition, Transition>();
		
		for (org.jbpt.petri.Transition t: sys.getTransitions()) {
			Transition tt = net.addTransition(t.getLabel());
			if (t.isSilent() || t.getLabel().startsWith("tau") || t.getLabel().equals("$invisible$")) {
				tt.setInvisible(true);
			}
			t2t.put(t, tt);
		}
		
		// flow 
		for (Flow f: sys.getFlow()) {
			if (f.getSource() instanceof org.jbpt.petri.Place) {
				net.addArc(p2p.get(f.getSource()), t2t.get(f.getTarget()));
			} else {
				net.addArc(t2t.get(f.getSource()), p2p.get(f.getTarget()));
			}
		}
		
		// add unique start node
		if (sys.getSourceNodes().isEmpty()) {
			Place i = net.addPlace("START_P");
			Transition t = net.addTransition("");
			t.setInvisible(true);
			net.addArc(i, t);
			
			for (org.jbpt.petri.Place p : sys.getMarkedPlaces()) {
				net.addArc(t, p2p.get(p));
			}
		}
		
		return net;
	}
	
	private static Marking getFinalMarking(PetrinetGraph net) {
		Marking finalMarking = new Marking();
		
		for (Place p: net.getPlaces()) {
			if (net.getOutEdges(p).isEmpty())
				finalMarking.add(p);
		}
		
		return finalMarking;
	}
	
	private static Marking getInitialMarking(PetrinetGraph net) {
		Marking initialMarking = new Marking();
		
		for (Place p : net.getPlaces()) {
			if (net.getInEdges(p).isEmpty())
				initialMarking.add(p);
		}
		
		return initialMarking;
	}
	
	private static TransEvClassMapping constructMapping(PetrinetGraph net, XLog log, 
			XEventClass dummyEvClass, XEventClassifier eventClassifier) {
		TransEvClassMapping mapping = new TransEvClassMapping(eventClassifier, dummyEvClass);
		
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		
		for (Transition t: net.getTransitions()) {
			
			boolean mapped = false;
			
//			System.out.println(String.format("Transition label: %s", t.getLabel()));
			
			for (XEventClass evClass : summary.getEventClasses().getClasses()) {
				
				String id = evClass.getId();
				
				if (t.getLabel().equals(id)) {
//					System.out.println(String.format("Mapping transition label %s to event class %s", t.getLabel(), id));
					mapping.put(t, evClass);
					mapped = true;
					break;
				}

				if (!mapped && !t.isInvisible()) {
//					System.out.println(String.format("Putting %s as dummy event class", id));
					mapping.put(t, dummyEvClass);
				}
			}
			
		}
		
		return mapping;
	}
	
	private static void doReplayExperiment(AlignmentTestParameters params, Debug debug, String folder, Petrinet net, Marking initialMarking, 
			Marking finalMarking, XLog log, XEventClassifier eventClassifier, String configuration, 
			int timeoutPerTraceInSec) throws FileNotFoundException, InterruptedException, ExecutionException {
		
		XEventClass dummyEvClass = new XEventClass("DUMMY", 99999);
		TransEvClassMapping mapping = constructMapping(net, log, dummyEvClass, eventClassifier);
		XLogInfo summary = XLogInfoFactory.createLogInfo(log, eventClassifier);
		XEventClasses classes = summary.getEventClasses();
		
		int threads;
		if (debug == Debug.STATS) {
			threads = THREADS;
		} else if (debug == Debug.DOT) {
			threads = 1;
		} else {
			threads = THREADS;
		}
		
		LOGGER.info("Number of threads: " + threads);
		
		// timeout per trace
		int timeout = log.size() * timeoutPerTraceInSec * 1000 / 10;
		int maxNumberOfStates = Integer.MAX_VALUE;
		
		ReplayerParameters parameters;
 		
		switch (configuration) {
			case AlignmentTestParameters.ASTAR:
				parameters = new ReplayerParameters.AStar(params.moveSort, params.queueSort, params.preferExact, 
						threads, params.useInt, debug, timeout, maxNumberOfStates, Integer.MAX_VALUE, params.partialOrder);
				doReplay(params, debug, folder, "alignment", net, initialMarking, finalMarking, log, mapping, classes,
							parameters);
				break;

			case AlignmentTestParameters.INC0 :
				parameters = new ReplayerParameters.IncementalAStar(params.moveSort, threads, params.useInt, 
						debug, timeout, maxNumberOfStates, Integer.MAX_VALUE, params.partialOrder, 0);
				doReplay(params, debug, folder, "alignment", net, initialMarking, finalMarking, log, mapping, classes,
							parameters);
				break;
			case AlignmentTestParameters.INC3 :
				parameters = new ReplayerParameters.IncementalAStar(params.moveSort, threads, params.useInt, 
						debug, timeout, maxNumberOfStates, Integer.MAX_VALUE, params.partialOrder, 3);
				doReplay(params, debug, folder, "alignment", net, initialMarking, finalMarking, log, mapping, classes,
							parameters);
				break;

			case AlignmentTestParameters.INC_PLUS :
				parameters = new ReplayerParameters.IncementalAStar(params.moveSort, threads, params.useInt, 
						debug, timeout, maxNumberOfStates, Integer.MAX_VALUE, params.partialOrder, true);
				doReplay(params, debug, folder, "alignment", net, initialMarking, finalMarking, log, mapping, classes,
							parameters);
				break;
			
			default:
				break;
		}
		
	}
	
	private static Map<Transition, Integer> constructCostMM(PetrinetGraph net, int invCost, int cost) {
		Map<Transition, Integer> costMM = new HashMap<>();
		for (Transition t : net.getTransitions()) {
			if (t.isInvisible()) {
				costMM.put(t, invCost);
			} else {
				costMM.put(t, cost);
			}
		}
		
		return costMM;
	}
	
	private static Map<Transition, Integer> constructCostSM(PetrinetGraph net, int cost) {
		Map<Transition, Integer> costSM = new HashMap<>();
		for (Transition t : net.getTransitions()) {
			costSM.put(t, cost);
		}
		
		return costSM;
	}
	
	private static Map<XEventClass, Integer> constructCostLM(XEventClasses classes, int cost) {
		Map<XEventClass, Integer> costLM = new HashMap<>();
		for (XEventClass clazz : classes.getClasses()) {
			costLM.put(clazz, cost);
		}
		return costLM;
	}
	
	private static void doReplay(AlignmentTestParameters params, Debug debug, String folder, String postfix, PetrinetGraph net, 
			Marking initialMarking, Marking finalMarking, XLog log, TransEvClassMapping mapping,
			XEventClasses classes, ReplayerParameters parameters) throws FileNotFoundException, InterruptedException, ExecutionException {
		PrintStream stream;
		
		if (debug == Debug.STATS) {
			stream = new PrintStream(new File(folder + "/" + params.iteration + "/" + postfix + ".csv"));
		} else if (debug == Debug.DOT) {
			stream = new PrintStream(new File(folder + "/" + params.iteration + "/" + postfix + ".dot"));
		} else {
			stream = System.out;
		}
		ReplayAlgorithm.Debug.setOutputStream(stream);
		
		Map<Transition, Integer> costMM = constructCostMM(net, 0, 4);
		Map<Transition, Integer> costSM = constructCostSM(net, 0);
		Map<XEventClass, Integer> costLM = constructCostLM(classes, 10);
		
		long start = System.nanoTime();
		
		Replayer replayer = new Replayer(parameters, (Petrinet) net, initialMarking, finalMarking, 
				classes, costMM, costLM, costSM, mapping, true);
		
		PNRepResult result = replayer.computePNRepResult(Progress.INVISIBLE, log);
		long end = System.nanoTime();
		
//		int cost = (int) Double.parseDouble((String) result.getInfo().get(Replayer.MAXMODELMOVECOST));
		int cost = 0;
		int timeout = 0;
		double time = 0;
		int mem = 0;
		int lps = 0;
		double pretime = 0;
		int weight = 0;
		double avgCost = 0;
		
		String alignDirpath = folder + "/" + params.iteration + "/alignment";
		File alignmentDir = new File(alignDirpath);
		
		if (!alignmentDir.exists() && params.printAlignment) {
			alignmentDir.mkdirs();
		}
		
		File alignFp;
		PrintStream alignStream;
		String traceId;
		
		for (SyncReplayResult res : result) {
			weight += res.getTraceIndex().size();
			cost += res.getTraceIndex().size() * res.getInfo().get(PNRepResult.RAWFITNESSCOST);
			timeout += res.getTraceIndex().size() * (res.getInfo().get(Replayer.TRACEEXITCODE).intValue() != 1 ? 1 : 0);
			time += res.getInfo().get(PNRepResult.TIME);
			pretime += res.getInfo().get(Replayer.PREPROCESSTIME);
			lps += res.getInfo().get(Replayer.HEURISTICSCOMPUTED);
			mem = Math.max(mem, res.getInfo().get(Replayer.MEMORYUSED).intValue());
			
			// output alignment
			if (params.printAlignment) {
				Iterator<Integer> iterator = res.getTraceIndex().iterator();
				while (iterator.hasNext()) {
					traceId = iterator.next().toString();
					alignFp = new File(alignDirpath + "/" + traceId + ".csv");
					alignStream = new PrintStream(alignFp);
					AlignUtils.toCsv(res, mapping, alignStream);
					
					alignStream.close();
				}
			}
		}
		
		avgCost = cost * 1.0 / weight;
		
		if (stream != System.out) {
			stream.close();
		}

		LOGGER.info(String.format(
				"\nTimeout: %d" + "\n" +
				"Clock time (ms): %.3f" + "\n" +
				"CPU time (ms): %.3f" + "\n" +
				"Preprocess time (ms): %.3f" + "\n" +
				"Max memory (kb): %d" + "\n" +
				"LPs: %d" + "\n" +
				"Total costs: %d" + "\n" + 
				"Avg. costs: %.3f" + "\n" +
				"Aligned: %d"
				, timeout, (end - start) / 1000000.0, time, pretime, mem, lps,
				cost, avgCost, weight));
//		
//		// number timeouts
//		System.out.print(timeout + SEP);
//		// clocktime
//		System.out.print(String.format("%.3f", (end - start) / 1000000.0) + SEP);
//		// cpu time
//		System.out.print(String.format("%.3f", time) + SEP);
//		// preprocess time
//		System.out.print(String.format("%.3f", pretime) + SEP);
//		// max memory
//		System.out.print(mem + SEP);
//		// solves lps.
//		System.out.print(lps + SEP);
//		// total cost.
//		System.out.print(cost + SEP);
//		// average cost
//		System.out.println(String.format("%.3f%s", avgCost, SEP));
//		
//		System.out.println("Done!");
//
//		System.out.flush();
	}
	
	public static void main(String[] args) {
		
		LOGGER.info("Arguments: " + args[0] + ", " + args[1]);
		
		setupLogger(args[0]);

		StringFileReader sfr = new StringFileReader();
		String jsonString = sfr.readFileAsString(args[1]);
		
		AlignmentTestParameters params = AlignmentTestParameters.readParams(jsonString);
		
		Petrinet net = importNet(params.modelPath);
		Marking initialMarking = getInitialMarking(net);
		Marking finalMarking = getFinalMarking(net);
		XLog log = null;
		XEventClassifier eventClassifier = null;
		
		XUniversalParser parser = new XUniversalParser();
		
		try {
			
			log = parser.parse(new File(params.logPath)).iterator().next();
			eventClassifier = log.getClassifiers().get(0);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(String.format("Cannot import log file: %s", params.logPath));
			System.exit(1);
		}
		
		Debug debug;

		switch (params.debug) {
			case "stats":
				debug = Debug.STATS;
				break;
			case "dot":
				debug = Debug.DOT;
				break;
			default:
				debug = Debug.STATS;
		}
		
		try {
			
			doReplayExperiment(params, debug, params.resultDir, net, initialMarking, finalMarking, 
					log, eventClassifier, params.configuration, params.timeoutPerTraceInSec);
		
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
}