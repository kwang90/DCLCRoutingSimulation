package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.tum.ei.lkn.eces.dclc_routing.BelmanfordAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.DCLCRouting;
import de.tum.ei.lkn.eces.dclc_routing.ExtendedSFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.OldCBF;
import de.tum.ei.lkn.eces.dclc_routing.SFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.EdgePath;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.SDpare;
import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.framework.Entity;
import de.tum.ei.lkn.eces.framework.Mapper;
import de.tum.ei.lkn.eces.framework.MapperManager;
import de.tum.ei.lkn.eces.graphsystem.GraphSystem;
import de.tum.ei.lkn.eces.graphsystem.components.Edge;
import de.tum.ei.lkn.eces.graphsystem.components.Graph;
import de.tum.ei.lkn.eces.graphsystem.components.Node;
import de.tum.ei.lkn.eces.networkcalculus.NCSystem;
import de.tum.ei.lkn.eces.networkcalculus.components.NCCostFunction;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings.RoutingAlgorithm;
import de.tum.ei.lkn.eces.networking.NetworkingSystem;
import de.tum.ei.lkn.eces.networking.components.Delay;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.networktopologies.OneRingFunnel;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;

public class ASimulationTest {
	
	Controller controller;
	GraphSystem m_GraphSystem;
	NetworkingSystem m_NetSys;
	MapperManager mm;
	NCSystem m_NCSystem;
	
	TopologyRingSettings m_TopoRingSetting;
	RoutingAlgorithmSettings m_RASetting;
	NetworkTopologyInterface m_Topology;
	Vector<Node> m_SendingNodes;
	Vector<Node> m_ReceivingNodes;

	Mapper<SDpare> sdMapper;
	Mapper<Delay> delayMapper;
	Mapper<EdgePath> edgePathMapper;

	@Before
	public void setUp(){
		controller = new Controller();
		m_GraphSystem = new GraphSystem(controller);
		m_NetSys = new NetworkingSystem(controller, m_GraphSystem);
		mm = new MapperManager(controller);
		
		sdMapper = new Mapper<SDpare>(SDpare.class);
		delayMapper = new Mapper<Delay>(Delay.class);
		edgePathMapper = new Mapper<EdgePath>(EdgePath.class);
		sdMapper.setController(controller);
		delayMapper.setController(controller);
		edgePathMapper.setController(controller);

		m_RASetting = new RoutingAlgorithmSettings();
		m_RASetting.setRoutingAlgorithm(RoutingAlgorithm.BelmanFord);
		m_NCSystem = new NCSystem(controller, m_GraphSystem, m_NetSys, m_RASetting, false);
		
		Mapper.initThreadlocal();
		m_NCSystem.initRoutingAlgorithm(null);
		mm.process();
		
		m_TopoRingSetting = new TopologyRingSettings();
		m_TopoRingSetting.setRingSize(2);
		m_TopoRingSetting.setBranchLength(2);
		m_Topology = new OneRingFunnel(controller, m_NetSys, m_TopoRingSetting);
		m_Topology.initTopology();
		m_SendingNodes = m_Topology.getNodesAllowedToSend();
		m_ReceivingNodes = m_Topology.getNodesAllowedToReceive();
	}
	
	private void addSDPairRand(Entity flow){
		Random r = new Random();
		int src = 0, dest = 0;
		double delay = 0;
		do{
			src = r.nextInt(m_SendingNodes.size());
			dest = r.nextInt(m_ReceivingNodes.size());
			delay = r.nextDouble();
		}while(src == dest);
		
		Mapper.initThreadlocal();
		sdMapper.attatchComponent(flow, new SDpare(m_SendingNodes.get(src), m_ReceivingNodes.get(dest)));
		delayMapper.attatchComponent(flow, new Delay(delay));
		mm.process();
	}
	
	@Test
	public void randomRouting(){
		Entity myFlow = controller.generateEntity();
		boolean _pathFound = false;
		int counter = 1000;
		do{
			addSDPairRand(myFlow);
			
			Mapper.initThreadlocal();
			_pathFound = m_NCSystem.ncRequest(myFlow);
			mm.process();
			
			EdgePath path = edgePathMapper.get(myFlow);
			String strPath = "Path Found: ";
			for(Edge e : path.getPath()){
				strPath += e.getDestination() + " --> ";
			}
			System.out.print(strPath);
			counter--;
		}while(_pathFound && counter > 0);
	}
}
