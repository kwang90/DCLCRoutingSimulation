package de.tum.ei.lkn.eces.test;

import java.util.Vector;

import org.junit.Test;

import com.tinkerpop.blueprints.Graph;

import de.tum.ei.lkn.eces.framework.Controller;
import de.tum.ei.lkn.eces.graphsystem.GraphSystem;
import de.tum.ei.lkn.eces.networking.NetworkingSystem;
import de.tum.ei.lkn.eces.topologies.gml.GmlReader;
import de.tum.ei.lkn.eces.topologies.networktopologies.GmlTopology;
import de.tum.ei.lkn.eces.topologies.networktopologies.NetworkTopologyInterface;
import de.tum.ei.lkn.eces.topologies.settings.TopologyRingSettings;

public class TestsTrivial {

	private Controller controller;
	private NetworkingSystem m_NetSys;
	private TopologyRingSettings m_TopoRingSetting;

	@Test
	public void topzoo(){
		controller = new Controller();
		m_NetSys = new NetworkingSystem(controller, new GraphSystem(controller));
		m_TopoRingSetting = new TopologyRingSettings();
		
		Vector<NetworkTopologyInterface> zoo = new Vector<NetworkTopologyInterface>();
		GmlReader gmlReader = new GmlReader();
		Vector<Graph> graphs = gmlReader.getAllGraphs("D:/topZoo");
		System.out.println("GRAPHS ACHIEVED");
		for(Graph g : graphs){
			NetworkTopologyInterface topo = new GmlTopology(controller, m_NetSys, g, m_TopoRingSetting);
			zoo.add(topo);
		}
		System.out.println("TOPOLOGIES ACHIEVED");
		int counter = 0;
		for(NetworkTopologyInterface z : zoo){
			z.initGraph();
			z.initTopology();
			int receive = z.getNodesAllowedToReceive().size();
			int send = z.getNodesAllowedToSend().size();
			System.out.println(++counter + "	#SendingNodes: " + send + "	#ReceivingNodes: " + receive);
		}
	}
}

/*
GRAPHS ACHIEVED
TOPOLOGIES ACHIEVED
1	#SendingNodes: 19	#ReceivingNodes: 19
2	#SendingNodes: 11	#ReceivingNodes: 11
3	#SendingNodes: 23	#ReceivingNodes: 23
4	#SendingNodes: 23	#ReceivingNodes: 23
5	#SendingNodes: 25	#ReceivingNodes: 25
6	#SendingNodes: 10	#ReceivingNodes: 10
7	#SendingNodes: 16	#ReceivingNodes: 16
8	#SendingNodes: 25	#ReceivingNodes: 25
9	#SendingNodes: 18	#ReceivingNodes: 18
10	#SendingNodes: 30	#ReceivingNodes: 30
11	#SendingNodes: 34	#ReceivingNodes: 34
12	#SendingNodes: 4	#ReceivingNodes: 4
13	#SendingNodes: 9	#ReceivingNodes: 9
14	#SendingNodes: 18	#ReceivingNodes: 18
15	#SendingNodes: 25	#ReceivingNodes: 25
16	#SendingNodes: 29	#ReceivingNodes: 29
17	#SendingNodes: 65	#ReceivingNodes: 65
18	#SendingNodes: 21	#ReceivingNodes: 21
19	#SendingNodes: 25	#ReceivingNodes: 25
20	#SendingNodes: 22	#ReceivingNodes: 22
21	#SendingNodes: 22	#ReceivingNodes: 22
22	#SendingNodes: 7	#ReceivingNodes: 7
23	#SendingNodes: 27	#ReceivingNodes: 27
24	#SendingNodes: 48	#ReceivingNodes: 48
25	#SendingNodes: 51	#ReceivingNodes: 51
26	#SendingNodes: 23	#ReceivingNodes: 23
27	#SendingNodes: 23	#ReceivingNodes: 23
28	#SendingNodes: 23	#ReceivingNodes: 23
29	#SendingNodes: 23	#ReceivingNodes: 23
30	#SendingNodes: 21	#ReceivingNodes: 21
31	#SendingNodes: 21	#ReceivingNodes: 21
32	#SendingNodes: 21	#ReceivingNodes: 21
33	#SendingNodes: 22	#ReceivingNodes: 22
34	#SendingNodes: 53	#ReceivingNodes: 53
35	#SendingNodes: 33	#ReceivingNodes: 33
36	#SendingNodes: 29	#ReceivingNodes: 29
37	#SendingNodes: 37	#ReceivingNodes: 37
38	#SendingNodes: 18	#ReceivingNodes: 18
39	#SendingNodes: 20	#ReceivingNodes: 20
40	#SendingNodes: 24	#ReceivingNodes: 24
41	#SendingNodes: 51	#ReceivingNodes: 51
42	#SendingNodes: 36	#ReceivingNodes: 36
43	#SendingNodes: 32	#ReceivingNodes: 32
44	#SendingNodes: 44	#ReceivingNodes: 44
45	#SendingNodes: 41	#ReceivingNodes: 41
46	#SendingNodes: 10	#ReceivingNodes: 10
47	#SendingNodes: 13	#ReceivingNodes: 13
48	#SendingNodes: 13	#ReceivingNodes: 13
49	#SendingNodes: 23	#ReceivingNodes: 23
50	#SendingNodes: 29	#ReceivingNodes: 29
51	#SendingNodes: 39	#ReceivingNodes: 39
52	#SendingNodes: 39	#ReceivingNodes: 39
53	#SendingNodes: 44	#ReceivingNodes: 44
54	#SendingNodes: 52	#ReceivingNodes: 52
55	#SendingNodes: 42	#ReceivingNodes: 42
56	#SendingNodes: 15	#ReceivingNodes: 15
57	#SendingNodes: 197	#ReceivingNodes: 197
58	#SendingNodes: 153	#ReceivingNodes: 153
59	#SendingNodes: 70	#ReceivingNodes: 70
60	#SendingNodes: 14	#ReceivingNodes: 14
61	#SendingNodes: 33	#ReceivingNodes: 33
62	#SendingNodes: 51	#ReceivingNodes: 51
63	#SendingNodes: 36	#ReceivingNodes: 36
64	#SendingNodes: 30	#ReceivingNodes: 30
65	#SendingNodes: 28	#ReceivingNodes: 28
66	#SendingNodes: 6	#ReceivingNodes: 6
67	#SendingNodes: 113	#ReceivingNodes: 113
68	#SendingNodes: 39	#ReceivingNodes: 39
69	#SendingNodes: 58	#ReceivingNodes: 58
70	#SendingNodes: 193	#ReceivingNodes: 193
71	#SendingNodes: 31	#ReceivingNodes: 31
72	#SendingNodes: 19	#ReceivingNodes: 19
73	#SendingNodes: 13	#ReceivingNodes: 13
74	#SendingNodes: 20	#ReceivingNodes: 20
75	#SendingNodes: 6	#ReceivingNodes: 6
76	#SendingNodes: 30	#ReceivingNodes: 30
77	#SendingNodes: 68	#ReceivingNodes: 68
78	#SendingNodes: 15	#ReceivingNodes: 15
79	#SendingNodes: 37	#ReceivingNodes: 37
80	#SendingNodes: 17	#ReceivingNodes: 17
81	#SendingNodes: 23	#ReceivingNodes: 23
82	#SendingNodes: 62	#ReceivingNodes: 62
83	#SendingNodes: 26	#ReceivingNodes: 26
84	#SendingNodes: 28	#ReceivingNodes: 28
85	#SendingNodes: 16	#ReceivingNodes: 16
86	#SendingNodes: 23	#ReceivingNodes: 23
87	#SendingNodes: 23	#ReceivingNodes: 23
88	#SendingNodes: 22	#ReceivingNodes: 22
89	#SendingNodes: 24	#ReceivingNodes: 24
90	#SendingNodes: 27	#ReceivingNodes: 27
91	#SendingNodes: 22	#ReceivingNodes: 22
92	#SendingNodes: 54	#ReceivingNodes: 54
93	#SendingNodes: 54	#ReceivingNodes: 54
94	#SendingNodes: 55	#ReceivingNodes: 55
95	#SendingNodes: 54	#ReceivingNodes: 54
96	#SendingNodes: 54	#ReceivingNodes: 54
97	#SendingNodes: 54	#ReceivingNodes: 54
98	#SendingNodes: 54	#ReceivingNodes: 54
99	#SendingNodes: 55	#ReceivingNodes: 55
100	#SendingNodes: 55	#ReceivingNodes: 55
101	#SendingNodes: 55	#ReceivingNodes: 55
102	#SendingNodes: 56	#ReceivingNodes: 56
103	#SendingNodes: 56	#ReceivingNodes: 56
104	#SendingNodes: 56	#ReceivingNodes: 56
105	#SendingNodes: 57	#ReceivingNodes: 57
106	#SendingNodes: 58	#ReceivingNodes: 58
107	#SendingNodes: 59	#ReceivingNodes: 59
108	#SendingNodes: 59	#ReceivingNodes: 59
109	#SendingNodes: 59	#ReceivingNodes: 59
110	#SendingNodes: 59	#ReceivingNodes: 59
111	#SendingNodes: 59	#ReceivingNodes: 59
112	#SendingNodes: 59	#ReceivingNodes: 59
113	#SendingNodes: 60	#ReceivingNodes: 60
114	#SendingNodes: 61	#ReceivingNodes: 61
115	#SendingNodes: 61	#ReceivingNodes: 61
116	#SendingNodes: 8	#ReceivingNodes: 8
117	#SendingNodes: 27	#ReceivingNodes: 27
118	#SendingNodes: 34	#ReceivingNodes: 34
119	#SendingNodes: 37	#ReceivingNodes: 37
120	#SendingNodes: 40	#ReceivingNodes: 40
121	#SendingNodes: 7	#ReceivingNodes: 7
122	#SendingNodes: 9	#ReceivingNodes: 9
123	#SendingNodes: 67	#ReceivingNodes: 67
124	#SendingNodes: 17	#ReceivingNodes: 17
125	#SendingNodes: 16	#ReceivingNodes: 16
126	#SendingNodes: 9	#ReceivingNodes: 9
127	#SendingNodes: 37	#ReceivingNodes: 37
128	#SendingNodes: 149	#ReceivingNodes: 149
129	#SendingNodes: 32	#ReceivingNodes: 32
130	#SendingNodes: 30	#ReceivingNodes: 30
131	#SendingNodes: 33	#ReceivingNodes: 33
132	#SendingNodes: 21	#ReceivingNodes: 21
133	#SendingNodes: 35	#ReceivingNodes: 35
134	#SendingNodes: 21	#ReceivingNodes: 21
135	#SendingNodes: 7	#ReceivingNodes: 7
136	#SendingNodes: 13	#ReceivingNodes: 13
137	#SendingNodes: 55	#ReceivingNodes: 55
138	#SendingNodes: 8	#ReceivingNodes: 8
139	#SendingNodes: 18	#ReceivingNodes: 18
140	#SendingNodes: 15	#ReceivingNodes: 15
141	#SendingNodes: 22	#ReceivingNodes: 22
142	#SendingNodes: 18	#ReceivingNodes: 18
143	#SendingNodes: 16	#ReceivingNodes: 16
144	#SendingNodes: 24	#ReceivingNodes: 24
145	#SendingNodes: 18	#ReceivingNodes: 18
146	#SendingNodes: 37	#ReceivingNodes: 37
147	#SendingNodes: 31	#ReceivingNodes: 31
148	#SendingNodes: 14	#ReceivingNodes: 14
149	#SendingNodes: 27	#ReceivingNodes: 27
150	#SendingNodes: 73	#ReceivingNodes: 73
151	#SendingNodes: 19	#ReceivingNodes: 19
152	#SendingNodes: 66	#ReceivingNodes: 66
153	#SendingNodes: 110	#ReceivingNodes: 110
154	#SendingNodes: 39	#ReceivingNodes: 39
155	#SendingNodes: 125	#ReceivingNodes: 125
156	#SendingNodes: 33	#ReceivingNodes: 33
157	#SendingNodes: 51	#ReceivingNodes: 51
158	#SendingNodes: 23	#ReceivingNodes: 23
159	#SendingNodes: 11	#ReceivingNodes: 11
160	#SendingNodes: 29	#ReceivingNodes: 29
161	#SendingNodes: 12	#ReceivingNodes: 12
162	#SendingNodes: 20	#ReceivingNodes: 20
163	#SendingNodes: 18	#ReceivingNodes: 18
164	#SendingNodes: 25	#ReceivingNodes: 25
165	#SendingNodes: 754	#ReceivingNodes: 754
166	#SendingNodes: 23	#ReceivingNodes: 23
167	#SendingNodes: 28	#ReceivingNodes: 28
168	#SendingNodes: 26	#ReceivingNodes: 26
169	#SendingNodes: 38	#ReceivingNodes: 38
170	#SendingNodes: 16	#ReceivingNodes: 16
171	#SendingNodes: 13	#ReceivingNodes: 13
172	#SendingNodes: 42	#ReceivingNodes: 42
173	#SendingNodes: 69	#ReceivingNodes: 69
174	#SendingNodes: 6	#ReceivingNodes: 6
175	#SendingNodes: 43	#ReceivingNodes: 43
176	#SendingNodes: 20	#ReceivingNodes: 20
177	#SendingNodes: 16	#ReceivingNodes: 16
178	#SendingNodes: 67	#ReceivingNodes: 67
179	#SendingNodes: 6	#ReceivingNodes: 6
180	#SendingNodes: 37	#ReceivingNodes: 37
181	#SendingNodes: 6	#ReceivingNodes: 6
182	#SendingNodes: 13	#ReceivingNodes: 13
183	#SendingNodes: 7	#ReceivingNodes: 7
184	#SendingNodes: 35	#ReceivingNodes: 35
185	#SendingNodes: 17	#ReceivingNodes: 17
186	#SendingNodes: 36	#ReceivingNodes: 36
187	#SendingNodes: 19	#ReceivingNodes: 19
188	#SendingNodes: 7	#ReceivingNodes: 7
189	#SendingNodes: 14	#ReceivingNodes: 14
190	#SendingNodes: 9	#ReceivingNodes: 9
191	#SendingNodes: 18	#ReceivingNodes: 18
192	#SendingNodes: 10	#ReceivingNodes: 10
193	#SendingNodes: 13	#ReceivingNodes: 13
194	#SendingNodes: 48	#ReceivingNodes: 48
195	#SendingNodes: 47	#ReceivingNodes: 47
196	#SendingNodes: 93	#ReceivingNodes: 93
197	#SendingNodes: 20	#ReceivingNodes: 20
198	#SendingNodes: 18	#ReceivingNodes: 18
199	#SendingNodes: 21	#ReceivingNodes: 21
200	#SendingNodes: 15	#ReceivingNodes: 15
201	#SendingNodes: 45	#ReceivingNodes: 45
202	#SendingNodes: 16	#ReceivingNodes: 16
203	#SendingNodes: 127	#ReceivingNodes: 127
204	#SendingNodes: 36	#ReceivingNodes: 36
205	#SendingNodes: 38	#ReceivingNodes: 38
206	#SendingNodes: 24	#ReceivingNodes: 24
207	#SendingNodes: 20	#ReceivingNodes: 20
208	#SendingNodes: 84	#ReceivingNodes: 84
209	#SendingNodes: 19	#ReceivingNodes: 19
210	#SendingNodes: 5	#ReceivingNodes: 5
211	#SendingNodes: 24	#ReceivingNodes: 24
212	#SendingNodes: 24	#ReceivingNodes: 24
213	#SendingNodes: 30	#ReceivingNodes: 30
214	#SendingNodes: 33	#ReceivingNodes: 33
215	#SendingNodes: 33	#ReceivingNodes: 33
216	#SendingNodes: 43	#ReceivingNodes: 43
217	#SendingNodes: 19	#ReceivingNodes: 19
218	#SendingNodes: 37	#ReceivingNodes: 37
219	#SendingNodes: 16	#ReceivingNodes: 16
220	#SendingNodes: 31	#ReceivingNodes: 31
221	#SendingNodes: 42	#ReceivingNodes: 42
222	#SendingNodes: 48	#ReceivingNodes: 48
223	#SendingNodes: 18	#ReceivingNodes: 18
224	#SendingNodes: 43	#ReceivingNodes: 43
225	#SendingNodes: 7	#ReceivingNodes: 7
226	#SendingNodes: 19	#ReceivingNodes: 19
227	#SendingNodes: 28	#ReceivingNodes: 28
228	#SendingNodes: 74	#ReceivingNodes: 74
229	#SendingNodes: 11	#ReceivingNodes: 11
230	#SendingNodes: 15	#ReceivingNodes: 15
231	#SendingNodes: 11	#ReceivingNodes: 11
232	#SendingNodes: 26	#ReceivingNodes: 26
233	#SendingNodes: 50	#ReceivingNodes: 50
234	#SendingNodes: 74	#ReceivingNodes: 74
235	#SendingNodes: 42	#ReceivingNodes: 42
236	#SendingNodes: 74	#ReceivingNodes: 74
237	#SendingNodes: 145	#ReceivingNodes: 145
238	#SendingNodes: 73	#ReceivingNodes: 73
239	#SendingNodes: 6	#ReceivingNodes: 6
240	#SendingNodes: 53	#ReceivingNodes: 53
241	#SendingNodes: 12	#ReceivingNodes: 12
242	#SendingNodes: 76	#ReceivingNodes: 76
243	#SendingNodes: 20	#ReceivingNodes: 20
244	#SendingNodes: 82	#ReceivingNodes: 82
245	#SendingNodes: 25	#ReceivingNodes: 25
246	#SendingNodes: 13	#ReceivingNodes: 13
247	#SendingNodes: 74	#ReceivingNodes: 74
248	#SendingNodes: 69	#ReceivingNodes: 69
249	#SendingNodes: 24	#ReceivingNodes: 24
250	#SendingNodes: 158	#ReceivingNodes: 158
251	#SendingNodes: 63	#ReceivingNodes: 63
252	#SendingNodes: 49	#ReceivingNodes: 49
253	#SendingNodes: 25	#ReceivingNodes: 25
254	#SendingNodes: 24	#ReceivingNodes: 24
255	#SendingNodes: 88	#ReceivingNodes: 88
256	#SendingNodes: 92	#ReceivingNodes: 92
257	#SendingNodes: 30	#ReceivingNodes: 30
258	#SendingNodes: 24	#ReceivingNodes: 24
259	#SendingNodes: 34	#ReceivingNodes: 34
260	#SendingNodes: 23	#ReceivingNodes: 23
261	#SendingNodes: 36	#ReceivingNodes: 36
 * */
