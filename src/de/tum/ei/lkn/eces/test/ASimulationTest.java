package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.tum.ei.lkn.eces.dclc_routing.DCLCRouting;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.SDpare;
import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.framework.Entity;
import de.tum.ei.lkn.eces.framework.Mapper;
import de.tum.ei.lkn.eces.graphsystem.components.Node;
import de.tum.ei.lkn.eces.networking.components.Delay;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;

public class ASimulationTest {
	
	Controller controller;
	
	Mapper<SDpare> sdMapper;
	Mapper<Delay> delayMapper;
	
	NetworkTopologyInterface m_Topology;
	Vector<Node> m_SendingNodes;
	Vector<Node> m_ReceivingNodes;
	DCLCRouting<?> m_RoutingAlgorithm;
	
	@Before
	public void setUp(){
		
		sdMapper = new Mapper<SDpare>(SDpare.class);
		delayMapper = new Mapper<Delay>(Delay.class);
		
		m_Topology.initTopology();
		m_SendingNodes = m_Topology.getNodesAllowedToSend();
		m_ReceivingNodes = m_Topology.getNodesAllowedToReceive();
	}
	
	private void addSDPairRand(Entity flow){
		Random r = new Random();
		int src = 0, dest = 0;
		do{
			src = r.nextInt(m_SendingNodes.size());
			dest = r.nextInt(m_ReceivingNodes.size());
		}while(src == dest);
		sdMapper.attatchComponent(flow, new SDpare(m_SendingNodes.get(src), m_ReceivingNodes.get(dest)));
	}
	
	@Test
	public void randomRouting(){
		Entity myFlow = controller.generateEntity();
		boolean _pathFound = false;
		int counter = 1000;
		do{
			addSDPairRand(myFlow);
			_pathFound = m_RoutingAlgorithm.addRoute(myFlow);
			
			counter--;
		}while(_pathFound && counter > 0);
	}
}
