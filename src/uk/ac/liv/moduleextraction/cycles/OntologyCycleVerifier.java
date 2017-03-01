package uk.ac.liv.moduleextraction.cycles;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class OntologyCycleVerifier {

	
	TarjanStronglyConnectedComponents tarj;
	Collection<OWLLogicalAxiom> axioms;
	private GraphBuilder.Graph g;

	public OntologyCycleVerifier(OWLOntology ontology) {
		this(ontology.getLogicalAxioms());
	}
	
	public OntologyCycleVerifier(Collection<OWLLogicalAxiom> axioms) {
		this.axioms = axioms;
		GraphBuilder builder = new GraphBuilder();
		g = builder.buildGraph(axioms);
		tarj = new TarjanStronglyConnectedComponents();
		tarj.performTarjan(g);
	}
	
	public boolean isCyclic() {
		for(HashSet<OWLClass> components : tarj.getStronglyConnectComponents()){
			// If there is a set of strongly connected components > 1 then there must be a cycle
			if(components.size() > 1){
				return true;
			}
		}
		//Or if there exists an axiom which is defined directly in terms of itself
		for(GraphBuilder.Vertex v : g.values()){
			if(v.joinedToSelf){
				return true;
			}
		}
		return false;
	}
	
	public HashSet<OWLClass> getCycleCausingNames(){
		HashSet<OWLClass> cycleCausing = new HashSet<OWLClass>();
		for(HashSet<OWLClass> components : tarj.getStronglyConnectComponents()){
			if(components.size() > 1){
				cycleCausing.addAll(components);
			}
		}
		for(GraphBuilder.Vertex v : g.values()){
			if(v.joinedToSelf){
				cycleCausing.add(v.value);
			}
		}
		return cycleCausing;
	}
	

	/* Naive approach - must currently be used in one-depleting modules */
	public Set<OWLLogicalAxiom> getCycleCausingAxioms(){
		Set<OWLLogicalAxiom> cycleCausing = new HashSet<OWLLogicalAxiom>();
		Set<OWLClass> cycleCausingNames = getCycleCausingNames();
		for(OWLLogicalAxiom axiom : axioms){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(cycleCausingNames.contains(name)){
				cycleCausing.add(axiom);
			}
		}
		return cycleCausing;
	}
	


	public void printSCC(){
		for(HashSet<OWLClass> component : tarj.getStronglyConnectComponents()){
			if(component.size() > 1){
				System.out.println(component);
			}
		}
	}

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/examples/recur.krss");
		System.out.println("Loaded");
		Set<OWLLogicalAxiom> coreaxioms = ModuleUtils.getCoreAxioms(ont);
		OntologyCycleVerifier cycle = new OntologyCycleVerifier(coreaxioms);
		System.out.println(ont.getLogicalAxioms());
		System.out.println("Cyclic?: " + cycle.isCyclic());
		cycle.printSCC();



	}
}
