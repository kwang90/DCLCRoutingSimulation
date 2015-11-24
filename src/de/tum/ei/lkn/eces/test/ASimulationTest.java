package de.tum.ei.lkn.eces.test;

import org.junit.Before;
import org.junit.Test;

import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;

public class ASimulationTest {

	NetworkTopologyInterface m_Topology;
	
	@Before
	public void setUp(){
		m_Topology.initTopology();
	}
	
	@Test
	public void firstTest(){
		
	}
}
