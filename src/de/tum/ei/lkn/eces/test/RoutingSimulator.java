package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import de.tum.ei.lkn.eces.dclc_routing.ConstrainedBellmanFord;
import de.tum.ei.lkn.eces.dclc_routing.ConstrainedDijkstraAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.DCLCRouting;
import de.tum.ei.lkn.eces.dclc_routing.ExtendedSFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.OldCBF;
import de.tum.ei.lkn.eces.dclc_routing.SFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.DCLCData;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.SDpare;
import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.framework.Entity;
import de.tum.ei.lkn.eces.framework.Mapper;
import de.tum.ei.lkn.eces.graphsystem.components.Graph;
import de.tum.ei.lkn.eces.graphsystem.components.Node;
import de.tum.ei.lkn.eces.networkcalculus.components.NCCostFunction;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings.RoutingAlgorithm;
import de.tum.ei.lkn.eces.networking.NetworkingSystem;
import de.tum.ei.lkn.eces.networking.components.Delay;
import de.tum.ei.lkn.eces.networking.components.Queue;
import de.tum.ei.lkn.eces.networking.components.Rate;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.networktopologies.OneRingFunnel;
import de.tum.ei.lkn.eces.topologies.networktopologies.TwoRingFunnel;
import de.tum.ei.lkn.eces.topologies.networktopologies.TwoRingRandom;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;
import de.tum.ei.lkn.simulation.TrafficSettings;

public class RoutingSimulator {

	private Controller controller;
	private NetworkingSystem m_NetSys;

	public RoutingSimulator(Controller controller, NetworkingSystem m_NetSys){
		this.controller = controller;
		this.m_NetSys = m_NetSys;
	}
	
	public Vector<Entity> entitiesGenerator(NetworkTopologyInterface m_Topology){
		Vector<Entity> entitySet = new Vector<Entity>();
		Vector<Entity[][]> entCube = getEntireEntitySet(m_Topology.getNodesAllowedToSend(), m_Topology.getNodesAllowedToReceive());
		for(Entity[][] eArray : entCube){
			for(int i = 0; i < eArray[0].length; i++){
				entitySet.addElement(eArray[0][i]);
			}
		}
		//randomly pick 1000 entities
		Vector<Entity> set = new Vector<Entity>();
		Random r = new Random();
		for(int i = 0; i< 1000; i++){
			int rand = r.nextInt(entitySet.size()-1);
			set.add(entitySet.get(rand));
			entitySet.remove(rand);
		}			
		return set;
	}
	
	/** topology selection */
	public NetworkTopologyInterface topoSelection(TopologyRingSettings m_TopoRingSetting, int i) {

		NetworkTopologyInterface m_Topology = null;
		String top = "";
		switch(i){
			case 0:
				m_Topology = new OneRingFunnel(controller, m_NetSys, m_TopoRingSetting);
				top = "One Ring";
				break;
			case 1: 
				m_Topology = new TwoRingFunnel(controller, m_NetSys, m_TopoRingSetting);
				top = "Two Ring";
				break;
			case 2:
				m_Topology = new TwoRingRandom(controller, m_NetSys, m_TopoRingSetting);
				top = "Tow Ring Random";
				break;
			default:
				m_Topology = new TwoRingRandom(controller, m_NetSys, m_TopoRingSetting);
				top = "Tow Ring Random";
				break;
		}
		System.out.println("Topology: " + top);
		return m_Topology;
	}
	
	/** from TopologySimuator */
	public Vector<Entity[][]> getEntireEntitySet(Vector<Node> qNodesAllowedToSend, Vector<Node> qNodesAllowedToReceive) {
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


	public void initRoutingAlgorithm(RoutingAlgorithm ra, DCLCRouting<? extends DCLCData> m_oAlgorithm, Graph qnet) {
		switch(ra){
			case BelmanFord:
				m_oAlgorithm = new ConstrainedBellmanFord<NCCostFunction>(controller, NCCostFunction.class);
				break;
			case BelmanFordOld:
				m_oAlgorithm = new OldCBF<NCCostFunction>(controller, NCCostFunction.class);
				break;
			case SF_DCLC:
				m_oAlgorithm = new SFAlgorithm<NCCostFunction>(controller, NCCostFunction.class, qnet);
				break;
			case Extended_SF:
				m_oAlgorithm = new ExtendedSFAlgorithm<NCCostFunction>(controller, NCCostFunction.class, qnet);
				break;
			case CDijkstra:
				m_oAlgorithm = new ConstrainedDijkstraAlgorithm<NCCostFunction>(controller, NCCostFunction.class);
				break;
			default:			
				m_oAlgorithm = new ConstrainedBellmanFord<NCCostFunction>(controller, NCCostFunction.class);
				break;
		}
	}
	
	public void runSimulation(DCLCRouting<? extends DCLCData> algorithm, 
							  NetworkTopologyInterface topology, Vector<Entity> entities){
		
	}

}
