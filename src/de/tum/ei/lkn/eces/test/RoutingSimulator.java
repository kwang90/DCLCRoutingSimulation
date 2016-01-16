package de.tum.ei.lkn.eces.test;

import java.util.Random;
import java.util.Vector;

import com.tinkerpop.blueprints.Graph;

import de.tum.ei.lkn.eces.dclc_routing.ConstrainedBellmanFord;
import de.tum.ei.lkn.eces.dclc_routing.ConstrainedDijkstraAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.DCLCRouting;
import de.tum.ei.lkn.eces.dclc_routing.ExtendedSFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.OldCBF;
import de.tum.ei.lkn.eces.dclc_routing.SFAlgorithm;
import de.tum.ei.lkn.eces.dclc_routing.datamodel.SDpare;
import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.framework.Entity;
import de.tum.ei.lkn.eces.framework.Mapper;
import de.tum.ei.lkn.eces.graphsystem.components.Node;
import de.tum.ei.lkn.eces.networkcalculus.components.NCCostFunction;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings.RoutingAlgorithm;
import de.tum.ei.lkn.eces.networking.NetworkingSystem;
import de.tum.ei.lkn.eces.networking.components.Delay;
import de.tum.ei.lkn.eces.networking.components.Queue;
import de.tum.ei.lkn.eces.networking.components.Rate;
import de.tum.ei.lkn.eces.topologies.gml.GmlReader;
import de.tum.ei.lkn.eces.topologies.networktopologies.GmlTopology;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.networktopologies.OneRingFunnel;
import de.tum.ei.lkn.eces.topologies.networktopologies.TwoRingFunnel;
import de.tum.ei.lkn.eces.topologies.networktopologies.TwoRingRandom;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;
import de.tum.ei.lkn.eces.topologies.settings.TopologySettings;

public class RoutingSimulator {

	private Controller controller;
	private NetworkingSystem m_NetSys;

	public RoutingSimulator(Controller controller, NetworkingSystem m_NetSys){
		this.controller = controller;
		this.m_NetSys = m_NetSys;
	}
	
	public Vector<Entity> entitiesGenerator(NetworkTopologyInterface m_Topology, int numberOfEntities){
		Vector<Entity> entitySet = new Vector<Entity>();
		Vector<Entity[]> entCube = getEntireEntitySet(m_Topology.getNodesAllowedToSend(), m_Topology.getNodesAllowedToReceive());
		for(Entity[] eArray : entCube){
			for(Entity e : eArray){
				entitySet.addElement(e);
			}
		}
		//randomly pick entities
		if(entitySet.size() < numberOfEntities)
			numberOfEntities = entitySet.size();
		Vector<Entity> set = new Vector<Entity>();
		Random r = new Random();
		for(int i = 0; i< numberOfEntities; i++){
			int rand = r.nextInt(entitySet.size()-1);
			set.add(entitySet.get(rand));
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
			case 3:
				int num = new Random().nextInt(260);
				m_Topology = topoZoo(m_TopoRingSetting).get(num);
				top = "ZOO #" + num;
				break;
			default:
				m_Topology = new TwoRingRandom(controller, m_NetSys, m_TopoRingSetting);
				top = "Tow Ring Random";
				break;
		}
		System.out.println("Topology: " + top);
		return m_Topology;
	}
	
	public Vector<NetworkTopologyInterface> topoZoo(TopologySettings topSettings){
		Vector<NetworkTopologyInterface> zoo = new Vector<NetworkTopologyInterface>();
		GmlReader gmlReader = new GmlReader();
		Vector<Graph> graphs = gmlReader.getAllGraphs("D:/topZoo");
		for(Graph g : graphs){
			NetworkTopologyInterface topo = new GmlTopology(controller, m_NetSys, g, topSettings);
			zoo.add(topo);
		}
		return zoo;
	}
	
	
	public DCLCRouting<NCCostFunction> initRoutingAlgorithm(NetworkTopologyInterface m_Topology, RoutingAlgorithm ra) {
		DCLCRouting<NCCostFunction> test_ra;
		switch(ra){
			case BelmanFord:
				test_ra = new ConstrainedBellmanFord<NCCostFunction>(controller, NCCostFunction.class);
				break;
			case BelmanFordOld:
				test_ra = new OldCBF<NCCostFunction>(controller, NCCostFunction.class);
				break;
			case SF_DCLC:
				test_ra = new SFAlgorithm<NCCostFunction>(controller, NCCostFunction.class, m_Topology.getQGraph());
				break;
			case Extended_SF:
				test_ra = new ExtendedSFAlgorithm<NCCostFunction>(controller, NCCostFunction.class, m_Topology.getQGraph());
				break;
			case CDijkstra:
				test_ra = new ConstrainedDijkstraAlgorithm<NCCostFunction>(controller, NCCostFunction.class);
				break;
			default:			
				test_ra = new ConstrainedBellmanFord<NCCostFunction>(controller, NCCostFunction.class);
				break;
		}
		return test_ra;
	}
	
	/** from TopologySimuator */
	public Vector<Entity[]> getEntireEntitySet(Vector<Node> qNodesAllowedToSend, Vector<Node> qNodesAllowedToReceive) {
		Mapper<Delay> m_oMapperDelay = new Mapper<Delay>(Delay.class);
		Mapper<Rate> m_oMapperRate = new Mapper<Rate>(Rate.class);
		Mapper<Queue> m_oMapperQueue = new Mapper<Queue>(Queue.class);
		Mapper<SDpare> m_oMapperSdPare = new Mapper<SDpare>(SDpare.class);
		
		m_oMapperDelay.setController(controller);
		m_oMapperRate.setController(controller);
		m_oMapperQueue.setController(controller);
		m_oMapperSdPare.setController(controller);
		
		Vector<Entity[]> entityVec = new Vector<Entity[]>();
		
		double[][] traffic = getTraffic(0.01, 0.025, 20);
		
		for(int i = 0; i < qNodesAllowedToSend.size(); i++)
		{
			for(int k = 0; k < qNodesAllowedToReceive.size(); k++)
			{
				if(!qNodesAllowedToSend.get(i).equals(qNodesAllowedToReceive.get(k)))
				{
					Entity entity[] = new Entity[traffic.length];
					for(int j = 0; j < traffic.length; j++)
					{
						entity[j] = controller.generateEntity();
						m_oMapperSdPare.attatchComponent( 	entity[j],new SDpare(qNodesAllowedToSend.get(i),qNodesAllowedToReceive.get(k)));
						m_oMapperRate.attatchComponent( 	entity[j],new Rate(traffic[j][0]));
						m_oMapperQueue.attatchComponent(    entity[j],new Queue(traffic[j][1]));
						m_oMapperDelay.attatchComponent(	entity[j],new Delay(traffic[j][2]));
					}
					entityVec.addElement(entity);
				}
			}
		}
		
		return entityVec;
	}

	private double[][] getTraffic(double start, double increment, int number) {
		double[][] traffic = new double[number][3];
		int counter = 0;
		for(double[] d : traffic){
			d[0] = 10000;
			d[1] = 100;
			d[2] = start + counter * increment;
			counter++;
		}
		return traffic;
	}

	public Vector<Entity> duplicateEntities(Vector<Entity> origin){
		
		Vector<Entity> copy = new Vector<Entity>();
		
		Mapper<Delay> m_oMapperDelay = new Mapper<Delay>(Delay.class);
		Mapper<Rate> m_oMapperRate = new Mapper<Rate>(Rate.class);
		Mapper<Queue> m_oMapperQueue = new Mapper<Queue>(Queue.class);
		Mapper<SDpare> m_oMapperSdPare = new Mapper<SDpare>(SDpare.class);
		m_oMapperDelay.setController(controller);
		m_oMapperRate.setController(controller);
		m_oMapperQueue.setController(controller);
		m_oMapperSdPare.setController(controller);
		
		for(Entity o : origin){
			Delay d = m_oMapperDelay.get_optimistic(o);
			Rate r = m_oMapperRate.get_optimistic(o);
			Queue q = m_oMapperQueue.get_optimistic(o);
			SDpare sd = m_oMapperSdPare.get_optimistic(o);
						
			Entity c = controller.generateEntity();
			m_oMapperDelay.attatchComponent(c, d);
			m_oMapperRate.attatchComponent(c, r);
			m_oMapperQueue.attatchComponent(c, q);
			m_oMapperSdPare.attatchComponent(c, sd);
			
			copy.add(c);
		}
		
		return copy;
	}
}
