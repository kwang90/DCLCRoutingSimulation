package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import de.tum.ei.lkn.eces.dclc_routing.datamodel.EdgePath;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.SDpare;
import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.framework.Entity;
import de.tum.ei.lkn.eces.framework.Mapper;
import de.tum.ei.lkn.eces.framework.MapperManager;
import de.tum.ei.lkn.eces.framework.exceptions.ComponentLocationException;
import de.tum.ei.lkn.eces.graphsystem.GraphSystem;
import de.tum.ei.lkn.eces.graphsystem.components.Node;
import de.tum.ei.lkn.eces.networkcalculus.NCSystem;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings.RoutingAlgorithm;
import de.tum.ei.lkn.eces.networking.NetworkingSystem;
import de.tum.ei.lkn.eces.networking.components.Delay;
import de.tum.ei.lkn.eces.networking.components.Queue;
import de.tum.ei.lkn.eces.networking.components.Rate;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.networktopologies.OneRingFunnel;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;
import de.tum.ei.lkn.simulation.TrafficSettings;

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
	Mapper<EdgePath> edgePathMapper;
	Vector<Entity[][]> entities;

	@Before
	public void setUp(){
		controller = new Controller();
		m_GraphSystem = new GraphSystem(controller);
		m_NetSys = new NetworkingSystem(controller, m_GraphSystem);
		mm = new MapperManager(controller);
		
		edgePathMapper = new Mapper<EdgePath>(EdgePath.class);
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

		Mapper.initThreadlocal();
		entities = getEntireEntitySet(controller, m_SendingNodes, m_ReceivingNodes);
		mm.process();
	}
	
	@Test
	public void randomRouting() throws ComponentLocationException, InterruptedException{
		Random rand = new Random();
		boolean _pathFound = false;
		int counter = 1000;
		do{
			Entity myFlow = entities.get(entities.size()-1)[0][rand.nextInt(3)];
			Mapper.initThreadlocal();
			_pathFound = m_NCSystem.ncRequest(myFlow);
			mm.process();
			//EdgePath path = edgePathMapper.get_wait(myFlow);
			//String strPath = "Path Found: ";
			//for(Edge e : path.getPath()){	strPath += e.getDestination() + " --> ";}
			//System.out.print(strPath);
			counter--;
		}while(_pathFound && counter > 0);
		System.out.print("1000 paths found");
	}
	
	/** from TopologySimuator */
	private Vector<Entity[][]> getEntireEntitySet(Controller controller, Vector<Node> qNodesAllowedToSend, Vector<Node> qNodesAllowedToReceive) {
		// TODO Auto-generated method stub
		Mapper<Delay> m_oMapperDelay = new Mapper<Delay>(Delay.class);
		Mapper<Rate> m_oMapperRate = new Mapper<Rate>(Rate.class);
		Mapper<Queue> m_oMapperQueue = new Mapper<Queue>(Queue.class);
		Mapper<SDpare> m_oMapperSdPare = new Mapper<SDpare>(SDpare.class);
		
		m_oMapperDelay.setController(controller);
		m_oMapperRate.setController(controller);
		m_oMapperQueue.setController(controller);
		m_oMapperSdPare.setController(controller);
		
		Vector<Entity[][]> entityVec = new Vector<Entity[][]>();
		
		double[][] traffic = TrafficSettings.getTraffic();
		
		for(int i = 0; i < qNodesAllowedToSend.size(); i++)
		{
			for(int k = 0; k < qNodesAllowedToReceive.size(); k++)
			{
				if(!qNodesAllowedToSend.get(i).equals(qNodesAllowedToReceive.get(k)))
				{
					Entity entity[][] = new Entity[1][7];
					for(int j = 0; j < 7; j++)
					{
						entity[0][j] = controller.generateEntity();
						m_oMapperSdPare.attatchComponent( 	entity[0][j],new SDpare(qNodesAllowedToSend.get(i),qNodesAllowedToReceive.get(k)));
						m_oMapperDelay.attatchComponent(	entity[0][j],new Delay(traffic[j][2]));
						m_oMapperRate.attatchComponent( 	entity[0][j],new Rate(traffic[j][0]));
						m_oMapperQueue.attatchComponent(    entity[0][j],new Queue(traffic[j][1]));
					}
					entityVec.addElement(entity);
				}
			}
		}
		
		return entityVec;
	}
}
