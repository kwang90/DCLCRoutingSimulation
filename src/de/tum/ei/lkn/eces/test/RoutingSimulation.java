package de.tum.ei.lkn.eces.test;

import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings;
import de.tum.ei.lkn.eces.networkcalculus.genetic.RoutingAlgorithmSettings.RoutingAlgorithm;
import de.tum.ei.lkn.eces.networkcalculus.genetic.TopologieSettings;
import de.tum.ei.lkn.eces.networkcalculus.genetic.TopologieSettings.Topologie;
import de.tum.ei.lkn.eces.topologies.networktopologies.RA_TestTopologie;
import de.tum.ei.lkn.eces.topologies.networktopologies.TopologieOne;
import de.tum.ei.lkn.eces.topologies.networktopologies.TopologieThree;
import de.tum.ei.lkn.eces.topologies.networktopologies.TopologieTwo;

public class RoutingSimulation {

	public static void main(String[] args) throws InterruptedException
	{
		boolean m_bShortTest = false;
		boolean m_bLongTest = true;
		boolean m_bLogTest = false;
		
		//Topologie settings
		TopologieSettings m_oTopologieSettings = new TopologieSettings();
		m_oTopologieSettings.setTopologie(Topologie.TopologieFour);
		m_oTopologieSettings.setRingsize(10);
		m_oTopologieSettings.setLength(10);
		m_oTopologieSettings.setNumberOfQueues(4);
		m_oTopologieSettings.setSimulationCycles(5);
		m_oTopologieSettings.setUseLogging(true);
		
		//Routing Algorithm settings
		RoutingAlgorithmSettings m_oRoutingSettings = new RoutingAlgorithmSettings();
		m_oRoutingSettings.setRoutingAlgorithm(RoutingAlgorithm.BelmanFord);
		
		System.out.println("Start Simulation for " + m_oTopologieSettings.getTopologie().toString());
		
		//Test Settings
		int m_iDivider = 1;
		int m_iMinRingsize = 2;
		int m_iMinLength = 2;
		int m_iMinQueues = 3;
		
		//Logging
		TestLog m_oLog = null;
		if(m_bLogTest)
		{
			m_oLog = new TestLog(m_oTopologieSettings.getTopologie());
			System.out.println("Logging activated");
		}
		
		int m_iTotalFlows = 0;
		int m_iFlows = 0;
		
		
		if(m_bShortTest)
		{
			System.out.println("Short Test");
			
			//Stopwatch stopwatch = Stopwatch.createStarted();
			
			for(int i = 1; i <= m_oTopologieSettings.getSimulationCycles(); i++)
			{
				if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieOne)){
					TopologieOne topOne = new TopologieOne(m_oTopologieSettings,m_oRoutingSettings);
					m_iFlows = topOne.runSimulation();
				}
				
				if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieTwo)){
					TopologieTwo topTwo = new TopologieTwo(m_oTopologieSettings,m_oRoutingSettings);
					m_iFlows = topTwo.runSimulation();
				}
				
				if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieThree)){
					TopologieThree topThree = new TopologieThree(m_oTopologieSettings,m_oRoutingSettings);
					m_iFlows = topThree.runSimulation();
				}
				
				if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieFour)){
					RA_TestTopologie topRA = new RA_TestTopologie(m_oTopologieSettings, m_oRoutingSettings);
					m_iFlows = topRA.runSimulation();
				}
				
				m_iTotalFlows = m_iTotalFlows + m_iFlows;
				
				//if(i%10 == 0)
					System.out.println("Simulation " + (i) + "/" + m_oTopologieSettings.getSimulationCycles() + "\t Flows: " + m_iFlows);
				
			}
			
			//stopwatch.stop();
			
			System.out.println("Average number of flows: " + m_iTotalFlows/m_oTopologieSettings.getSimulationCycles());
			
			//stopwatch.elapsed(TimeUnit.SECONDS);
			//System.out.println("Simulation Time: " + stopwatch); // formatted string like "12.3 ms"
		}
		
		//long test
		if(m_bLongTest)
		{
			System.out.println("Long Test");
			
			//Stopwatch stopwatch = Stopwatch.createStarted();
			int counter = 0;
			
			for(int q = m_iMinQueues; q <= m_oTopologieSettings.getNumberOfQueues(); q++)
			{
				for(int r = m_iMinRingsize; r <= m_oTopologieSettings.getRingsize(); r++)
				{
					for(int l = m_iMinLength; l <= m_oTopologieSettings.getLength(); l++)
					{
						if((r%m_iDivider == 0) && (l%m_iDivider == 0))
						{
							int totalFlows = 0;
							
							for(int i = 1; i <= m_oTopologieSettings.getSimulationCycles(); i++)
							{
								int flows = 0;
								
								if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieOne)){
									TopologieOne topOne = new TopologieOne(r,l,q,m_oRoutingSettings);
									flows = topOne.runSimulation();
								}
								
								if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieTwo)){
									TopologieTwo topTwo = new TopologieTwo(r,l,q,m_oRoutingSettings);
									flows = topTwo.runSimulation();
								}
								
								if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieThree)){
									TopologieThree topThree = new TopologieThree(r,l,q,m_oRoutingSettings);
									flows = topThree.runSimulation();
								}
								
								if(m_oTopologieSettings.getTopologie().equals(Topologie.TopologieFour)){
									RA_TestTopologie topRA = new RA_TestTopologie(r,l,q, m_oRoutingSettings);
									flows = topRA.runSimulation();
								}
								
								totalFlows = totalFlows + flows;
								counter++;
							}
						
							double averageFlows = totalFlows/m_oTopologieSettings.getSimulationCycles();
							
							System.out.println("Simulation: " + counter + "/" + Integer.toString(((m_oTopologieSettings.getRingsize()-m_iMinRingsize+1)/m_iDivider) * ((m_oTopologieSettings.getLength()-m_iMinLength+1)/m_iDivider) * (m_oTopologieSettings.getNumberOfQueues()-m_iMinQueues+1) * m_oTopologieSettings.getSimulationCycles()) + "\t Ringsize: " + r + "\t Length: " + l + "\t Queues: " + q + "\t Average Flows: " + averageFlows);
							
							if(m_bLogTest)
							{
								m_oLog.log(r,l,q, averageFlows);
							}
						}
					}
				}
			}
			
			//stopwatch.elapsed(TimeUnit.SECONDS);
			//System.out.println("Simulation Time: " + stopwatch); // formatted string like "12.3 ms"
		}		
	}
}
