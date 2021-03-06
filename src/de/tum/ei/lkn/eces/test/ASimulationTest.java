package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.tum.ei.lkn.eces.dclc_routing.DCURAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.ConstrainedBellmanFord;
import de.tum.ei.lkn.eces.dclc_routing.ExtendedSFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.SFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.EdgePath;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.SDpare;
import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.framework.Entity;
import de.tum.ei.lkn.eces.framework.Mapper;
import de.tum.ei.lkn.eces.framework.MapperManager;
import de.tum.ei.lkn.eces.framework.exceptions.ComponentLocationException;
import de.tum.ei.lkn.eces.generaldijkstra.GeneralDijkstra;
import de.tum.ei.lkn.eces.genmst.GenMST;
import de.tum.ei.lkn.eces.graphsystem.GraphSystem;
import de.tum.ei.lkn.eces.graphsystem.components.Edge;
import de.tum.ei.lkn.eces.graphsystem.components.Node;
import de.tum.ei.lkn.eces.networkcalculus.NCSystem;
import de.tum.ei.lkn.eces.networkcalculus.components.NCCostFunction;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings.RoutingAlgorithm;
import de.tum.ei.lkn.eces.networking.NetworkingSystem;
import de.tum.ei.lkn.eces.networking.components.Delay;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;

public class ASimulationTest {

	//Logger
	private TestLog logger;
	//Framework
	private Controller controller;
	private GraphSystem m_GraphSystem;
	private NetworkingSystem m_NetSys;
	private MapperManager mm;
	private NCSystem m_NCSystem;
	//Topology
	private RoutingAlgorithmSettings m_RASetting;
	private NetworkTopologyInterface m_Topology;
	private Vector<NetworkTopologyInterface> topologies;
	private Vector<Entity> entities;
	//Mapper
	private Mapper<EdgePath> edgePathMapper;
	private Mapper<NCCostFunction> m_MapperNcData;
	private Mapper<SDpare> m_MapperSdPare;
	private Mapper<Delay> m_MapperDelay;
	//For SF Routing
	private GeneralDijkstra genDijkLC;
	private GeneralDijkstra genDijkLD;
	private GenMST mstLC;
	private GenMST mstLD;
	//Simulator
	private RoutingSimulator simulator;
	//CBF
	ConstrainedBellmanFord<NCCostFunction> optimalSolution;
	
	Mapper<Delay> m_oMapperDelay = new Mapper<Delay>(Delay.class);

	@Before
	public void setUp(){
		controller = new Controller();
		m_GraphSystem = new GraphSystem(controller);
		m_NetSys = new NetworkingSystem(controller, m_GraphSystem);
		mm = new MapperManager(controller);
		simulator = new RoutingSimulator(controller, m_NetSys);
		
		edgePathMapper = new Mapper<EdgePath>(EdgePath.class);
		m_MapperNcData = new Mapper<NCCostFunction>(NCCostFunction.class);
		m_MapperSdPare = new Mapper<SDpare>(SDpare.class);
		m_MapperDelay = new Mapper<Delay>(Delay.class);
		edgePathMapper.setController(controller);
		m_MapperNcData.setController(controller);
		m_MapperSdPare.setController(controller);
		m_MapperDelay.setController(controller);
	}

	private void routingSetup(RoutingAlgorithm ra, NetworkTopologyInterface m_Topology, int NUMBER_OF_ENTITIES) {
		Mapper.initThreadlocal();

		m_RASetting = new RoutingAlgorithmSettings();
		m_RASetting.setRoutingAlgorithm(ra);
		m_NCSystem = new NCSystem(controller, m_GraphSystem, m_NetSys, m_RASetting, false);
		
		m_Topology.initGraph();
		
		Mapper.initThreadlocal();
		m_NCSystem.initRoutingAlgorithm(m_Topology.getQGraph());
		optimalSolution = new ConstrainedBellmanFord<NCCostFunction>(controller, NCCostFunction.class);
		mm.process();
		
		//Settings for SF and Extended_SF
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC 
			|| m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF 
			|| m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.DCUR){
			genDijkLC = new GeneralDijkstra(controller, m_Topology.getQGraph()) {
				@Override
				public double getEdgeCost(Edge nxtEdge) {
					return m_MapperNcData.get_optimistic(nxtEdge.getEntity()).getCosts(controller.generateEntity(), null);
				}
			};
			
			genDijkLD = new GeneralDijkstra(controller, m_Topology.getQGraph()) {
				@Override
				public double getEdgeCost(Edge nxtEdge) {
					return m_MapperNcData.get_optimistic(nxtEdge.getEntity()).getDelay(controller.generateEntity(), null);
				}
			};
			genDijkLC.clearMemo();
			genDijkLD.clearMemo();
			mstLC = new GenMST(controller, genDijkLC);
			mstLD = new GenMST(controller, genDijkLD);
		}
		
		m_Topology.initTopology();
		
		Mapper.initThreadlocal();
		entities = simulator.entitiesGenerator(m_Topology, NUMBER_OF_ENTITIES);
		mm.process();
		
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC){
			((SFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preSF(controller, mstLC, mstLD);
		}
		
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
			((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLDRun(controller, mstLD);
		}
		
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.DCUR){
			((DCURAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preRunDCUR(controller, mstLC, mstLD);
		}
	}

	/** AUT and CBF running on same set of entities.
	 * 	Average cost, delay, running time, cost inefficiency according to delay constraint levels
	 * */
	@Ignore
	@Test
	public void A_DelayLevels() throws ComponentLocationException, InterruptedException{

		RoutingAlgorithm ra = RoutingAlgorithm.DCUR;
		logger = new TestLog(ra.toString() + "_Delaylevels");
		/* 0: One Ring
		 * 1: Two Ring
		 * 2: Two Ring Random
		 * 3: Topology Zoo
		 * */
		int TOPOLOTY = 3;
		int RING_SIZE = 10;
		int BRANCH_LENTH = 10;
		int NUMBER_OF_ENTITIES = 5000;
		TopologyRingSettings m_TopoRingSetting = new TopologyRingSettings();
		
		m_TopoRingSetting.setRingSize(RING_SIZE);
		m_TopoRingSetting.setBranchLength(BRANCH_LENTH);
		m_TopoRingSetting.setQueues(10);
		m_Topology = simulator.topoSelection(m_TopoRingSetting, TOPOLOTY);
		
		routingSetup(ra, m_Topology, NUMBER_OF_ENTITIES);
		
		int counter = 0;
		int correctCnt = 0;
		//Data
		Vector<Long> runtimeAUT = new Vector<Long>();
		Vector<Long> runtimeCBF = new Vector<Long>();
		Vector<Double> costAUT = new Vector<Double>();
		Vector<Double> delayAUT = new Vector<Double>();
		Vector<Double> costCBF = new Vector<Double>();
		Vector<Double> delayCBF = new Vector<Double>();

		//logging
		logger.logString("Topology,Sending Nodes,Receving Nodes,AUT");
		logger.log(m_Topology.toString() , 
					m_Topology.getNodesAllowedToSend().size(), 
					m_Topology.getNodesAllowedToReceive().size(),
					ra.toString());		
		logger.logString("Algorithm,Source,Destination,Cost,Delay,Running Time,Delay Constraint");
		Random r = new Random();
		while(counter < NUMBER_OF_ENTITIES){
			Entity e = entities.get(r.nextInt(entities.size()));
			long preRunningTime_AUT = 0;
			Mapper.initThreadlocal();
			Node src = m_MapperSdPare.get_optimistic(e).getSource();
			Node dest = m_MapperSdPare.get_optimistic(e).getDestination();
			//CBF clean run
			EdgePath cbfPath = optimalSolution.runCleanAddRoute(e);
			//For ExtendedSF pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
				long t0 = System.nanoTime();
				((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
				preRunningTime_AUT = System.nanoTime() - t0;
			}
			//For SF-DCLC pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC){
				long t0 = System.nanoTime();
				((SFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preSF(controller, mstLC, mstLD);
				preRunningTime_AUT = System.nanoTime() - t0;
			}
			//For DCUR pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.DCUR){
				long t0 = System.nanoTime();
				((DCURAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preRunDCUR(controller, mstLC, mstLD);
				preRunningTime_AUT = System.nanoTime() - t0;
			}
			
			//AUT run
			boolean b = m_NCSystem.ncRequest(e);
			EdgePath path = edgePathMapper.get_optimistic(e);
			mm.process();
			
			if(path == null || !b || cbfPath == null)
				continue;
			//the path cost CBF found should be less or equal to that of AUT
			if(cbfPath.getCosts() <= path.getCosts()){
				correctCnt++;
			}
			//else continue;

			//print out
			System.out.println("\n" + src.getIdentifier() + " -> " + dest.getIdentifier() + " : ");
			System.out.println("AUT");
			for(Edge edge : path.getPath()){
				System.out.print(edge.getSource().getIdentifier() + "-" + edge.getDestination().getIdentifier() + " > ");
			}
			System.out.println("\nCBF");
			for(Edge edge : cbfPath.getPath()){
				System.out.print(edge.getSource().getIdentifier() + "-" + edge.getDestination().getIdentifier() + " > ");
			}
			//logging AUF
			logger.log("AUT", src.getIdentifier(), dest.getIdentifier(), 
						path.getCosts(), path.getTime(), m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT, 
						m_MapperDelay.get_optimistic(e));
			//logging CBF
			logger.log("CBF", src.getIdentifier(), dest.getIdentifier(), 
					cbfPath.getCosts(), cbfPath.getTime(), optimalSolution.algrRunningTime_clean(),
						m_MapperDelay.get_optimistic(e));
			
			runtimeAUT.add(m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT); 
			runtimeCBF.add(optimalSolution.algrRunningTime_clean());
			costAUT.add(path.getCosts());
			delayAUT.add(path.getTime());
			costCBF.add(cbfPath.getCosts());
			delayCBF.add(cbfPath.getTime());
			counter++;
		}
		
		//Output Result
		long sumRuntimeAUT = 0;
		long sumRuntimeCBF = 0;
		double sumCostAUT = 0;
		double sumDelayAUT = 0;
		double sumCostCBF = 0;
		double sumDelayCBF = 0;
		for(long t : runtimeAUT)
			sumRuntimeAUT += t;
		for(long t : runtimeCBF)
			sumRuntimeCBF += t;
		for(double c : costAUT)
			sumCostAUT += c;
		for(double c : costCBF)
			sumCostCBF += c;
		for(double d : delayAUT)
			sumDelayAUT += d;
		for(double d : delayCBF)
			sumDelayCBF += d;
		System.out.println("\nResult");
		if(counter != 0)
			System.out.println("Correct run: " + correctCnt + " out of " + counter + " , " + Math.rint(100 * correctCnt/counter) + "%");
		System.out.println(ra.toString() + " run " + counter +	" calculations: " + "total running time: " + sumRuntimeAUT);
		System.out.println("Cost : " + sumCostAUT + "		Delay: " + sumDelayAUT);
		System.out.println("CBF run " + counter +	" calculations: " + "total running time: " + sumRuntimeCBF);
		System.out.println("Cost : " + sumCostCBF + "		Delay: " + sumDelayCBF);
	}

	@Ignore
	@Test
	public void B_TopoSize() throws ComponentLocationException, InterruptedException{
		
		RoutingAlgorithm ra = RoutingAlgorithm.Extended_SF;
		logger = new TestLog(ra.toString() + "_TopologySize");	
		

		int TOPOLOTY = 3;	/* 0: One Ring,	1: Two Ring,	2: Two Ring Random,	3: Topology Zoo */
		int NUMBER_OF_ENTITIES = 5000;
		int NUMBER_OF_TOPOS = 20;
		Random r = new Random();
		TopologyRingSettings m_TopoRingSetting = new TopologyRingSettings();
		
		if(TOPOLOTY == 3){
			topologies = simulator.topoZoo(m_TopoRingSetting);
		}
		else{
			topologies = new Vector<NetworkTopologyInterface>();
			if(TOPOLOTY == 0 || TOPOLOTY == 1)
				NUMBER_OF_ENTITIES = 5;
			for(int i = 2; i <= 10; i++){
				m_TopoRingSetting .setRingSize(i);
				m_TopoRingSetting.setBranchLength(i);
				topologies.add(simulator.topoSelection(m_TopoRingSetting, TOPOLOTY));
			}
		}
		
		//logging
		logger.logString("AUT,Topos,EntitiesPerTop,TOPOLOTY");	// Data Info
		logger.logString(ra.toString() + "," + NUMBER_OF_TOPOS + "," + NUMBER_OF_ENTITIES + "," + TOPOLOTY);	
		logger.logString("Algorithm,Source,Destination,Cost,Delay,Running Time,#Nodes(Sending)");
		
		//Data
		Vector<Long> runtimeAUT = new Vector<Long>();
		Vector<Long> runtimeCBF = new Vector<Long>();
		Vector<Double> costAUT = new Vector<Double>();
		Vector<Double> delayAUT = new Vector<Double>();
		Vector<Double> costCBF = new Vector<Double>();
		Vector<Double> delayCBF = new Vector<Double>();
		
		int topoCounter = 0;
		
		while(topoCounter < NUMBER_OF_TOPOS){
			NetworkTopologyInterface top = topologies.get(r.nextInt(topologies.size()));
			
			routingSetup(ra, top, NUMBER_OF_ENTITIES);
			int num_SendingNodes = top.getNodesAllowedToSend().size();
			
			int entityCounter = 0;
			int correctCnt = 0;

			while(entityCounter < NUMBER_OF_ENTITIES){
				Entity e = entities.get(r.nextInt(entities.size()));
				long preRunningTime_AUT = 0;
				Mapper.initThreadlocal();
				Node src = m_MapperSdPare.get_optimistic(e).getSource();
				Node dest = m_MapperSdPare.get_optimistic(e).getDestination();
				//CBF clean run
				EdgePath cbfPath = optimalSolution.runCleanAddRoute(e);
				//For ExtendedSF pre-run
				if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
					long t0 = System.nanoTime();
					((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
					preRunningTime_AUT = System.nanoTime() - t0;
				}
				//For SF-DCLC pre-run
				if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC){
					long t0 = System.nanoTime();
					((SFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preSF(controller, mstLC, mstLD);
					preRunningTime_AUT = System.nanoTime() - t0;
				}
				//For DCUR pre-run
				if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.DCUR){
					long t0 = System.nanoTime();
					((DCURAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preRunDCUR(controller, mstLC, mstLD);
					preRunningTime_AUT = System.nanoTime() - t0;
				}
				//AUT run
				boolean b = m_NCSystem.ncRequest(e);
				EdgePath path = edgePathMapper.get_optimistic(e);
				mm.process();
				
				if(path == null || !b || cbfPath == null)
					continue;
				//the path cost CBF found should be less or equal to that of AUT
				if(cbfPath.getCosts() <= path.getCosts()){
					correctCnt++;
				}
				//else continue;

				//print out
				System.out.println("\n" + src.getIdentifier() + " -> " + dest.getIdentifier() + " : ");
				System.out.println("AUT");
				for(Edge edge : path.getPath()){
					System.out.print(edge.getSource().getIdentifier() + "-" + edge.getDestination().getIdentifier() + " > ");
				}
				System.out.println("\nCBF");
				for(Edge edge : cbfPath.getPath()){
					System.out.print(edge.getSource().getIdentifier() + "-" + edge.getDestination().getIdentifier() + " > ");
				}
				//logging AUF
				logger.log("AUT", src.getIdentifier(), dest.getIdentifier(), 
							path.getCosts(), path.getTime(), m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT, num_SendingNodes);
				//logging CBF
				logger.log("CBF", src.getIdentifier(), dest.getIdentifier(), 
						cbfPath.getCosts(), cbfPath.getTime(), optimalSolution.algrRunningTime_clean(), num_SendingNodes);
				
				runtimeAUT.add(m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT); 
				runtimeCBF.add(optimalSolution.algrRunningTime_clean());
				costAUT.add(path.getCosts());
				delayAUT.add(path.getTime());
				costCBF.add(cbfPath.getCosts());
				delayCBF.add(cbfPath.getTime());
				entityCounter++;
			}
			topoCounter++;
		}

		//Output Result
		long sumRuntimeAUT = 0;
		long sumRuntimeCBF = 0;
		double sumCostAUT = 0;
		double sumDelayAUT = 0;
		double sumCostCBF = 0;
		double sumDelayCBF = 0;
		for(long t : runtimeAUT)
			sumRuntimeAUT += t;
		for(long t : runtimeCBF)
			sumRuntimeCBF += t;
		for(double c : costAUT)
			sumCostAUT += c;
		for(double c : costCBF)
			sumCostCBF += c;
		for(double d : delayAUT)
			sumDelayAUT += d;
		for(double d : delayCBF)
			sumDelayCBF += d;
		
		System.out.println("\nResult");
		System.out.println(ra.toString() + "	Cost : " + sumCostAUT + "	Delay: " + sumDelayAUT + "	Total running time: " + sumRuntimeAUT);
		System.out.println();
		System.out.println("CBF	Cost : " + sumCostCBF + "	Delay: " + sumDelayCBF + " Total running time: " + sumRuntimeCBF);
	}
	
	@Ignore
	@Test
	public void C_QueueNumber() throws ComponentLocationException, InterruptedException{

		RoutingAlgorithm ra = RoutingAlgorithm.LARAC;
		logger = new TestLog(ra.toString() + "_QueueNumbers");
		
		int TOPOLOTY = 3;	/* 0: One Ring,	1: Two Ring,	2: Two Ring Random,	3: Topology Zoo */
		int RING_SIZE = 10;
		int BRANCH_LENTH = 10;
		int NUMBER_OF_ENTITIES = 50;
		int NUMBER_OF_TOPOS = 20;
		Random r = new Random();
		Vector<Integer> nQ = new Vector<Integer>();  
		topologies = new Vector<NetworkTopologyInterface>();
		
		if(TOPOLOTY == 3){
			for(int i = 1; i < 20; i = i + 3){
				TopologyRingSettings set = new TopologyRingSettings();
				set.setQueues(i);
				for(NetworkTopologyInterface t : simulator.topoZoo(set)){
					topologies.add(t);
					nQ.add(i);
				}
			}
		}
		else{
			if(TOPOLOTY == 0 || TOPOLOTY == 1)
				NUMBER_OF_ENTITIES = 50;
			for(int i = 1; i < 20; i = i + 3){
				TopologyRingSettings m_TopoRingSetting = new TopologyRingSettings();
				m_TopoRingSetting .setRingSize(RING_SIZE);
				m_TopoRingSetting.setBranchLength(BRANCH_LENTH);
				m_TopoRingSetting.setQueues(i);
				topologies.add(simulator.topoSelection(m_TopoRingSetting, TOPOLOTY));
				nQ.add(i);
			}
		}
		
		//logging
		logger.logString("AUT,Topos,EntitiesPerTop,TOPOLOTY");	// Data Info
		logger.logString(ra.toString() + "," + NUMBER_OF_TOPOS + "," + NUMBER_OF_ENTITIES + "," + TOPOLOTY);	
		logger.logString("Algorithm,Source,Destination,Cost,Delay,Running Time,#Queues");
		
		//Data
		Vector<Long> runtimeAUT = new Vector<Long>();
		Vector<Long> runtimeCBF = new Vector<Long>();
		Vector<Double> costAUT = new Vector<Double>();
		Vector<Double> delayAUT = new Vector<Double>();
		Vector<Double> costCBF = new Vector<Double>();
		Vector<Double> delayCBF = new Vector<Double>();
		
		int topoCounter = 0;
		
		while(topoCounter < NUMBER_OF_TOPOS){
			NetworkTopologyInterface top = topologies.get(r.nextInt(topologies.size()));
			
			routingSetup(ra, top, NUMBER_OF_ENTITIES);
			int num_queues = nQ.get(topologies.indexOf(top));
			
			int entityCounter = 0;
			int correctCnt = 0;

			while(entityCounter < NUMBER_OF_ENTITIES){
				Entity e = entities.get(r.nextInt(entities.size()));
				long preRunningTime_AUT = 0;
				Mapper.initThreadlocal();
				Node src = m_MapperSdPare.get_optimistic(e).getSource();
				Node dest = m_MapperSdPare.get_optimistic(e).getDestination();
				//CBF clean run
				EdgePath cbfPath = optimalSolution.runCleanAddRoute(e);
				//For ExtendedSF pre-run
				if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
					long t0 = System.nanoTime();
					((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
					preRunningTime_AUT = System.nanoTime() - t0;
				}
				//For SF-DCLC pre-run
				if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC){
					long t0 = System.nanoTime();
					((SFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preSF(controller, mstLC, mstLD);
					preRunningTime_AUT = System.nanoTime() - t0;
				}
				//For DCUR pre-run
				if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.DCUR){
					long t0 = System.nanoTime();
					((DCURAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preRunDCUR(controller, mstLC, mstLD);
					preRunningTime_AUT = System.nanoTime() - t0;
				}
				//AUT run
				boolean b = m_NCSystem.ncRequest(e);
				EdgePath path = edgePathMapper.get_optimistic(e);
				mm.process();
				
				if(path == null || !b || cbfPath == null){
					System.out.println("Continue!");
					continue;
				}
									//the path cost CBF found should be less or equal to that of AUT
				if(cbfPath.getCosts() <= path.getCosts()){
					correctCnt++;
				}
				//else continue;

				//print out
				System.out.println("\n" + src.getIdentifier() + " -> " + dest.getIdentifier() + " : ");
				System.out.println("AUT");
				for(Edge edge : path.getPath()){
					System.out.print(edge.getSource().getIdentifier() + "-" + edge.getDestination().getIdentifier() + " > ");
				}
				System.out.println("\nCBF");
				for(Edge edge : cbfPath.getPath()){
					System.out.print(edge.getSource().getIdentifier() + "-" + edge.getDestination().getIdentifier() + " > ");
				}
				//logging AUF
				logger.log("AUT", src.getIdentifier(), dest.getIdentifier(), 
							path.getCosts(), path.getTime(), m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT, num_queues);
				//logging CBF
				logger.log("CBF", src.getIdentifier(), dest.getIdentifier(), 
						cbfPath.getCosts(), cbfPath.getTime(), optimalSolution.algrRunningTime_clean(), num_queues);
				
				runtimeAUT.add(m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT); 
				runtimeCBF.add(optimalSolution.algrRunningTime_clean());
				costAUT.add(path.getCosts());
				delayAUT.add(path.getTime());
				costCBF.add(cbfPath.getCosts());
				delayCBF.add(cbfPath.getTime());
				entityCounter++;
			}
			topoCounter++;
		}

		//Output Result
		long sumRuntimeAUT = 0;
		long sumRuntimeCBF = 0;
		double sumCostAUT = 0;
		double sumDelayAUT = 0;
		double sumCostCBF = 0;
		double sumDelayCBF = 0;
		for(long t : runtimeAUT)
			sumRuntimeAUT += t;
		for(long t : runtimeCBF)
			sumRuntimeCBF += t;
		for(double c : costAUT)
			sumCostAUT += c;
		for(double c : costCBF)
			sumCostCBF += c;
		for(double d : delayAUT)
			sumDelayAUT += d;
		for(double d : delayCBF)
			sumDelayCBF += d;
		
		System.out.println("\nResult");
		System.out.println(ra.toString() + "	Cost : " + sumCostAUT + "	Delay: " + sumDelayAUT + "	Total running time: " + sumRuntimeAUT);
		System.out.println();
		System.out.println("CBF	Cost : " + sumCostCBF + "	Delay: " + sumDelayCBF + " Total running time: " + sumRuntimeCBF);
	}

	/** run AUT to get maximum flows*/
//	@Ignore
	@Test
	public void D_MaxFlowTest() throws ComponentLocationException, InterruptedException{
		
		/* 0: One Ring
		 * 1: Two Ring
		 * 2: Two Ring Random
		 * 3: Topology Zoo
		 * */
		int TOPOLOTY = 0;
		int RING_SIZE = 10;
		int BRANCH_LENTH = 10;
		int NUMBER_OF_ENTITIES = 5000;
		TopologyRingSettings m_TopoRingSetting = new TopologyRingSettings();
		m_TopoRingSetting.setRingSize(RING_SIZE);
		m_TopoRingSetting.setBranchLength(BRANCH_LENTH);
		RoutingAlgorithm ra;
		
		if(TOPOLOTY == 3)
			m_Topology = simulator.topoZoo(m_TopoRingSetting).get(59);
		else
			m_Topology = simulator.topoSelection(m_TopoRingSetting, TOPOLOTY);

		// AUT Test
		ra = RoutingAlgorithm.DCUR;
		routingSetup(ra, m_Topology, NUMBER_OF_ENTITIES);
		
		int counter = 0;
		//Data
		Vector<Long> runtimeAUT = new Vector<Long>();
		Vector<Double> costAUT = new Vector<Double>();
		Vector<Double> delayAUT = new Vector<Double>();
		
		while(true){
			Entity e = entities.get(new Random().nextInt(entities.size()));
			Mapper.initThreadlocal();
			Node dest = m_MapperSdPare.get_optimistic(e).getDestination();
			long preRunningTime_AUT = 0;
			//For ExtendedSF pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
				long t0 = System.currentTimeMillis();
				((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
				preRunningTime_AUT = System.nanoTime() - t0;
			}
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC){
				long t0 = System.currentTimeMillis();
				((SFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preSF(controller, mstLC, mstLD);
				preRunningTime_AUT = System.nanoTime() - t0;
			}
			boolean b = m_NCSystem.ncRequest(e);
			mm.process();
			EdgePath path = edgePathMapper.get_optimistic(e);
			if(path == null || !b)
				break;
			runtimeAUT.add(m_NCSystem.getAlgorithm().algrRunningTime() + preRunningTime_AUT); // Running time for addflow
			costAUT.add(path.getCosts());
			delayAUT.add(path.getTime());
			counter++;
		}
		
		//Output Result
		long sumRuntimeAUT = 0;
		double sumCostAUT = 0;
		double sumDelayAUT = 0;
		for(int i = 0; i < counter; i++){
			sumRuntimeAUT += runtimeAUT.get(i);
			sumCostAUT += costAUT.get(i);
			sumDelayAUT += delayAUT.get(i);
		}
		System.out.println("\n Max number of flows for " + ra.toString());
		System.out.println(ra.toString() + " run " + counter +	" calculations: " + "total running time: " + sumRuntimeAUT);
		System.out.println("Cost : " + sumCostAUT + "		Delay: " + sumDelayAUT);
	
		// CBF Test
		ra = RoutingAlgorithm.BelmanFord;		
		routingSetup(ra, m_Topology, NUMBER_OF_ENTITIES);
		
		int counterCBF = 0;
		//Data
		Vector<Long> runtimeCBF = new Vector<Long>();
		Vector<Double> costCBF = new Vector<Double>();
		Vector<Double> delayCBF = new Vector<Double>();
		
		while(true){
			Entity e = entities.get(new Random().nextInt(entities.size()));
			Mapper.initThreadlocal();
			boolean b = optimalSolution.addRoute(e);
			mm.process();
			EdgePath cbfPath = edgePathMapper.get_optimistic(e);
			if(!b || cbfPath == null)
			{
				System.out.println("Break!");
				break;
			}
			runtimeCBF.add(optimalSolution.algrRunningTime());
			costCBF.add(cbfPath.getCosts());
			delayCBF.add(cbfPath.getTime());
			counterCBF++;
		}
		
		//Output Result
		long sumRuntimeCBF = 0;
		double sumCostCBF = 0;
		double sumDelayCBF = 0;
		for(int i = 0; i < counterCBF; i++){
			sumRuntimeCBF += runtimeCBF.get(i);
			sumCostCBF += costCBF.get(i);
			sumDelayCBF += delayCBF.get(i);
		}
		System.out.println("\n Max number of flows of CBF");
		System.out.println("CBF run " + counterCBF +	" calculations: " + "total running time: " + sumRuntimeCBF);
		System.out.println("Cost : " + sumCostCBF + "		Delay: " + sumDelayCBF);
		
	}
}
