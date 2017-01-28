package uk.ac.liv.moduleextraction.cycles;

import org.semanticweb.owlapi.model.OWLClass;
import uk.ac.liv.moduleextraction.cycles.GraphBuilder.Graph;
import uk.ac.liv.moduleextraction.cycles.GraphBuilder.Vertex;

import java.util.HashSet;
import java.util.Stack;

public class TarjanStronglyConnectedComponents {

	private Stack<Vertex> vertexStack = new Stack<Vertex>();
	private HashSet<HashSet<OWLClass>> stronglyconnected = new HashSet<HashSet<OWLClass>>();
	
	int index = 0;
	
	int max = 1;
	int total = 0;
	int count = 0;
	
	
	public TarjanStronglyConnectedComponents() {
		
	}
	
	public void performTarjan(Graph graph){
		for(Vertex v : graph.values()){
			if(v.index == -1){
				stronglyConnect(v);
			}
		}
//		System.out.println("C: " + count);
//		System.out.println("Max: " + max);
//		System.out.println("Avg: " + (double) total/count);
	}
	
	public HashSet<HashSet<OWLClass>> getStronglyConnectComponents() {
		return stronglyconnected;
	}

	public void stronglyConnect(Vertex v){
		//System.out.println(v);
		v.index = index;
		v.lowlink = index;
		index++;
		v.onStack = true;
		vertexStack.push(v);
		
		for(Vertex adj : v.getConnections()){
			if(adj.index == -1){
				stronglyConnect(adj);
				v.lowlink = Math.min(v.lowlink, adj.lowlink);
				//System.out.println(v + " lowlink = " + v.lowlink);
			}
			else if (adj.onStack){
				v.lowlink = Math.min(v.lowlink, adj.index);
				//System.out.println(v + " lowlink = " + v.lowlink);
			}
			
		}
		if (v.lowlink == v.index) {
			HashSet<OWLClass> sc = new HashSet<OWLClass>();
			Vertex n = null;
			do {
				n = vertexStack.pop();
				n.onStack = false;
				sc.add(n.value);
			}while(!n.equals(v));
			if(sc.size() > 1){
//				count++;
//				total += sc.size();
//				max = Math.max(max, sc.size());
//				System.out.println(sc.size());
			}
			stronglyconnected.add(sc);
		}
	}
	  
}