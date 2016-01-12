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
	RoutingAlgorithm ra = RoutingAlgorithm.Extended_SF;
	private int RING_SIZE = 10;
	private int BRANCH_LENTH = 10;
	private int NUMBER_OF_ENTITIES = 10000;
	
	/* 0: One Ring
	 * 1: Two Ring
	 * 2: Two Ring Random
	 * 3: Topology Zoo
	 * */
	private int TOPOLOTY = 3; //new Random().nextInt(4);
	//Logger
	private TestLog logger;
	//Framework
	private Controller controller;
	private GraphSystem m_GraphSystem;
	private NetworkingSystem m_NetSys;
	private MapperManager mm;
	private NCSystem m_NCSystem;
	//Topology
	private TopologyRingSettings m_TopoRingSetting;
	private RoutingAlgorithmSettings m_RASetting;
	private NetworkTopologyInterface m_Topology;
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

		Mapper.initThreadlocal();

		m_RASetting = new RoutingAlgorithmSettings();
		m_RASetting.setRoutingAlgorithm(ra);
		m_NCSystem = new NCSystem(controller, m_GraphSystem, m_NetSys, m_RASetting, false);
		
		m_TopoRingSetting = new TopologyRingSettings();
		m_TopoRingSetting.setRingSize(RING_SIZE);
		m_TopoRingSetting.setBranchLength(BRANCH_LENTH);
		m_Topology = simulator.topoSelection(m_TopoRingSetting, TOPOLOTY);
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

	/** AUT and CBF running on same set of entities */
	//@Ignore
	@Test
	public void A_RoutingTest() throws ComponentLocationException, InterruptedException{
		logger = new TestLog("RuntimeTest");
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
		logger.logTitle("Topology,Sending Nodes,Receving Nodes,AUT");
		logger.log(m_Topology.toString() , 
					m_Topology.getNodesAllowedToSend().size(), 
					m_Topology.getNodesAllowedToReceive().size(),
					ra.toString());
		logger.logSectionSeperater();
		
		logger.logTitle("Algorithm,Source,Destination,Cost,Delay,Running Time,Delay Constraint");
		for(Entity e : entities){
			Mapper.initThreadlocal();
			Node src = m_MapperSdPare.get_optimistic(e).getSource();
			Node dest = m_MapperSdPare.get_optimistic(e).getDestination();
			//For ExtendedSF pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
				long t0 = System.nanoTime();
				((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
				runtimeAUT.add(System.nanoTime() - t0); //Running time for pre-run
			}
			boolean b = m_NCSystem.ncRequest(e);
			EdgePath path = edgePathMapper.get_optimistic(e);
			EdgePath cbfPath = optimalSolution.runCleanAddRoute(e);
			mm.process();
			
			runtimeAUT.add(m_NCSystem.getAlgorithm().algrRunningTime()); // Running time for addflow
			runtimeCBF.add(optimalSolution.algrRunningTime_clean());
			if(path == null || !b || cbfPath == null)
				continue;
			//the path cost CBF found should be less or equal to that of AUT
			if(cbfPath.getCosts() <= path.getCosts()){
				correctCnt++;
			}
			//else continue;	//JUST FOR TEMP TEST : "CHEATER"
			
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
						path.getCosts(), path.getTime(), m_NCSystem.getAlgorithm().algrRunningTime(), 
						m_MapperDelay.get_optimistic(e));
			//logging CBF
			logger.log("CBF", src.getIdentifier(), dest.getIdentifier(), 
						path.getCosts(), path.getTime(), m_NCSystem.getAlgorithm().algrRunningTime(),
						m_MapperDelay.get_optimistic(e));
			
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
		for(int i = 0; i < counter; i++){
			sumRuntimeAUT += runtimeAUT.get(i);
			sumRuntimeCBF += runtimeCBF.get(i);
			sumCostAUT += costAUT.get(i);
			sumDelayAUT += delayAUT.get(i);
			sumCostCBF += costCBF.get(i);
			sumDelayCBF += delayCBF.get(i);
		}
		System.out.println("\nResult");
		System.out.println("Correct run: " + correctCnt + " out of " + counter + " , " + Math.rint(100 * correctCnt/counter) + "%");
		System.out.println(ra.toString() + " run " + counter +	" calculations: " + "total running time: " + sumRuntimeAUT);
		System.out.println("Cost : " + sumCostAUT + "		Delay: " + sumDelayAUT);
		System.out.println("CBF run " + counter +	" calculations: " + "total running time: " + sumRuntimeCBF);
		System.out.println("Cost : " + sumCostCBF + "		Delay: " + sumDelayCBF);
		//logging
		logger.logSectionSeperater();
		logger.logTitle("Algorithm,Loop Number,Cost Sum,Delay Sum,Rumtime Sum");
		logger.log(ra.toString(), counter, sumCostAUT, sumDelayAUT, sumRuntimeAUT);
		logger.log(RoutingAlgorithm.BelmanFord.toString(), counter, sumCostCBF, sumDelayCBF, sumRuntimeCBF);
	}

	/** run AUT to get maximum flows*/
	@Ignore
	@Test
	public void B_MaxFlowTest_AUT() throws ComponentLocationException, InterruptedException{
		int counter = 0;
		//Data
		Vector<Long> runtimeAUT = new Vector<Long>();
		Vector<Double> costAUT = new Vector<Double>();
		Vector<Double> delayAUT = new Vector<Double>();
		
		while(true){
			Entity e = entities.get(new Random().nextInt(entities.size()));
			Mapper.initThreadlocal();
			Node dest = m_MapperSdPare.get_optimistic(e).getDestination();
			//For ExtendedSF pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
				long t0 = System.currentTimeMillis();
				((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
				runtimeAUT.add(System.currentTimeMillis() - t0); //Running time for pre-run
			}
			boolean b = m_NCSystem.ncRequest(e);
			mm.process();
			runtimeAUT.add(m_NCSystem.getAlgorithm().algrRunningTime()); // Running time for addflow
			EdgePath path = edgePathMapper.get_optimistic(e);
			if(path == null || !b)
				break;
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
		System.out.println("\n Max number of flows for" + ra.toString());
		System.out.println(ra.toString() + " run " + counter +	" calculations: " + "total running time: " + sumRuntimeAUT);
		System.out.println("Cost : " + sumCostAUT + "		Delay: " + sumDelayAUT);
	}

	/** run CBF to get maximum flows*/
	@Ignore
	@Test
	public void C_MaxFlowTest_CBF() throws ComponentLocationException, InterruptedException{
		int counter = 0;
		//Data
		Vector<Long> runtimeCBF = new Vector<Long>();
		Vector<Double> costCBF = new Vector<Double>();
		Vector<Double> delayCBF = new Vector<Double>();
		
		while(true){
			Entity e = entities.get(new Random().nextInt(entities.size()));
			Mapper.initThreadlocal();
			boolean b = optimalSolution.addRoute(e);
			mm.process();
			runtimeCBF.add(optimalSolution.algrRunningTime());
			EdgePath cbfPath = edgePathMapper.get_optimistic(e);
			if(!b || cbfPath == null)
			{
				System.out.println("Break!");
				break;
			}
			costCBF.add(cbfPath.getCosts());
			delayCBF.add(cbfPath.getTime());
			counter++;
		}
		
		//Output Result
		long sumRuntimeCBF = 0;
		double sumCostCBF = 0;
		double sumDelayCBF = 0;
		for(int i = 0; i < counter; i++){
			sumRuntimeCBF += runtimeCBF.get(i);
			sumCostCBF += costCBF.get(i);
			sumDelayCBF += delayCBF.get(i);
		}
		System.out.println("\n Max number of flows of CBF");
		System.out.println("CBF run " + counter +	" calculations: " + "total running time: " + sumRuntimeCBF);
		System.out.println("Cost : " + sumCostCBF + "		Delay: " + sumDelayCBF);
	}
}
