package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

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
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;

public class ASimulationTest {
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
	private Vector<Node> m_SendingNodes;
	private Vector<Node> m_ReceivingNodes;
	private Vector<Entity[][]> entities;
	//Mapper
	private Mapper<EdgePath> edgePathMapper;
	private Mapper<NCCostFunction> m_MapperNcData;
	private Mapper<SDpare> m_MapperSdPare;	
	//For SF Routing
	private GeneralDijkstra genDijkLC;
	private GeneralDijkstra genDijkLD;
	private GenMST mstLC;
	private GenMST mstLD;
	//Simulator
	private RoutingSimulator simulator;
	

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
		edgePathMapper.setController(controller);
		m_MapperNcData.setController(controller);
		m_MapperSdPare.setController(controller);

		Mapper.initThreadlocal();

		m_RASetting = new RoutingAlgorithmSettings();
		m_RASetting.setRoutingAlgorithm(RoutingAlgorithm.Extended_SF);
		m_NCSystem = new NCSystem(controller, m_GraphSystem, m_NetSys, m_RASetting, false);
		
		m_TopoRingSetting = new TopologyRingSettings();
		m_TopoRingSetting.setRingSize(15);
		m_TopoRingSetting.setBranchLength(10);
		//topoSelection(new Random().nextInt(3));
		m_Topology = simulator.topoSelection(m_TopoRingSetting, 2);
		m_Topology.initGraph();
		
		Mapper.initThreadlocal();
		m_NCSystem.initRoutingAlgorithm(m_Topology.getQGraph());
		mm.process();
		
		//Settings for SF and Extended_SF
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC 
			|| m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF ){
			genDijkLC = new GeneralDijkstra(controller, m_Topology.getQGraph()) {
				@Override
				public double getEdgeCost(Edge nxtEdge) {
					// TODO Auto-generated method stub
					return m_MapperNcData.get_optimistic(nxtEdge.getEntity()).getCosts(controller.generateEntity(), null);
				}
			};
			
			genDijkLD = new GeneralDijkstra(controller, m_Topology.getQGraph()) {
				@Override
				public double getEdgeCost(Edge nxtEdge) {
					// TODO Auto-generated method stub
					return m_MapperNcData.get_optimistic(nxtEdge.getEntity()).getDelay(controller.generateEntity(), null);
				}
			};
			genDijkLC.clearMemo();
			genDijkLD.clearMemo();
			mstLC = new GenMST(controller, genDijkLC);
			mstLD = new GenMST(controller, genDijkLD);
		}
		
		m_Topology.initTopology();
		m_SendingNodes = m_Topology.getNodesAllowedToSend();
		m_ReceivingNodes = m_Topology.getNodesAllowedToReceive();
		
		Mapper.initThreadlocal();
		entities = simulator.getEntireEntitySet(m_SendingNodes, m_ReceivingNodes);
		mm.process();
		
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.SF_DCLC){
			((SFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preSF(controller, mstLC, mstLD);
		}
		
		if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
			((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLDRun(controller, mstLD);
		}
	}

	@Test
	public void randomRouting() throws ComponentLocationException, InterruptedException{
		Vector<Long> runningtimes = new Vector<Long>();
		Random rand = new Random();
		boolean _pathFound = false;
		int counter = 0;
		do{
			Entity[][] entCube = entities.get(rand.nextInt(entities.size()));
			Entity myFlow = entCube[0][rand.nextInt(4)];
			Mapper.initThreadlocal();
			Node src = m_MapperSdPare.get_optimistic(myFlow).getSource();
			Node dest = m_MapperSdPare.get_optimistic(myFlow).getDestination();
			System.out.println("src: " + src.getIdentifier() + "	--> dest : " + dest.getIdentifier());
			//For ExtendedSF pre-run
			if(m_RASetting.getRoutingAlgorithm() == RoutingAlgorithm.Extended_SF){
				long t0 = System.currentTimeMillis();
				((ExtendedSFAlgorithm<NCCostFunction>)(m_NCSystem.getAlgorithm())).preLCRun(controller, mstLC, dest);
				runningtimes.add(System.currentTimeMillis() - t0); //Running time for pre-run
			}
			_pathFound = m_NCSystem.ncRequest(myFlow);
			runningtimes.add(m_NCSystem.getAlgorithm().algrRunningTime()); // Running time for addflow
			mm.process();
			EdgePath path = edgePathMapper.get_optimistic(myFlow);
			if(path == null)
				continue;
			String strPath = "Path Found: ";
			for(Edge e : path.getPath()){	strPath += e.getDestination().getIdentifier() + " > ";}
			System.out.println(strPath);
			counter++;
		}while(_pathFound && counter < 1000);
		
		long sum = 0;
		for(long l : runningtimes)
			sum += l;
		System.out.println(counter + " calculations: " + "total running time: " + sum);
	}
}
