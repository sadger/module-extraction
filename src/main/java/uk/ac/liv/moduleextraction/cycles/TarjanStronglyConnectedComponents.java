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

	public void performTarjan(Graph graph){
		graph.values().stream().filter(v -> v.index == -1).forEach(this::stronglyConnect);
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
			}
			else if (adj.onStack){
				v.lowlink = Math.min(v.lowlink, adj.index);
			}
			
		}
		if (v.lowlink == v.index) {
			HashSet<OWLClass> sc = new HashSet<OWLClass>();
			Vertex n;
			do {
				n = vertexStack.pop();
				n.onStack = false;
				sc.add(n.value);
			} while(!n.equals(v));
			stronglyconnected.add(sc);
		}
	}
	  
}