package uk.ac.liv.moduleextraction.cycles;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

public class GraphBuilder {

	public Graph buildGraph(Collection<OWLLogicalAxiom> axioms){
		Graph graph = new Graph();
		for(OWLLogicalAxiom axiom : axioms){
			OWLClassExpression name = AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression descript = AxiomSplitter.getDefinitionofAxiom(axiom);

            //System.out.println(axiom);
            for(OWLClass lhsClass : ModuleUtils.getNamedClassesInSignature(name)){
				Vertex lhsVertex = graph.get(lhsClass);
				if(lhsVertex == null){
					lhsVertex = new Vertex(lhsClass);
					graph.addVertex(lhsVertex);
				}
				for(OWLClass rhsClass : ModuleUtils.getNamedClassesInSignature(descript)){
					Vertex rhsVertex = graph.get(rhsClass);
					if(rhsVertex == null){
					  rhsVertex = new Vertex(rhsClass);
					  graph.addVertex(rhsVertex);
					}
					if(lhsClass.equals(rhsClass)){
						lhsVertex.joinedToSelf = true;
					}
					lhsVertex.addConnection(rhsVertex);
				}
				lhsVertex.axioms.add(axiom);
			}

		}
		
		return graph;
	}
	
	
	class Graph extends HashMap<OWLClass, Vertex>{
		
		public void addVertex(Vertex v){
			put(v.value, v);
		}
		
		@Override
		public String toString(){
			String s = "";
			for(Vertex v : this.values()){
				s += v.toString() + "->" + v.getConnections() + "\n";
			}
			return s;
		}
	}
	
	public class Vertex{
		public OWLClass value;
		private HashSet<Vertex> connections;
		public boolean onStack = false;
		public int index = -1;
		public int lowlink = -1;
		public HashSet<OWLLogicalAxiom> axioms = new HashSet<OWLLogicalAxiom>();
		public boolean joinedToSelf = false;

		
		public Vertex(OWLClass value) {
			this.value = value;
			this.connections = new HashSet<Vertex>();
		}
		

		public void addConnection(Vertex v){
			connections.add(v);
		}
		
		public HashSet<Vertex> getConnections() {
			return connections;
		}
		
		@Override
		public boolean equals(Object obj){
			if (!(obj instanceof Vertex)) return false;
			if (obj == this)  return true;

			//Vertex with the same value are equal
			Vertex v = (Vertex) obj;
			return v.value.equals(this.value);
		}

		@Override
		public int hashCode() {
			return value.hashCode();
		}
		
		@Override
		public String toString(){
			return value.toString();
		}
	}
	
	public static void main(String[] args) {
		
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "examples/top.krss");
		System.out.println(ont.getLogicalAxioms());

		GraphBuilder b = new GraphBuilder();
		Graph g = b.buildGraph(ont.getLogicalAxioms());
		System.out.println(g);
		
		TarjanStronglyConnectedComponents scc = new TarjanStronglyConnectedComponents();
		scc.performTarjan(g);
		scc.getStronglyConnectComponents();


		
	}
}
