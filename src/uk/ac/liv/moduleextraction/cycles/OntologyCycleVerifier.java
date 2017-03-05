package uk.ac.liv.moduleextraction.cycles;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.STARAMEXHybridExtractor;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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
	

	public Set<OWLLogicalAxiom> canWeDoBetter(){
		Set<OWLLogicalAxiom> cycleCausing = new HashSet<OWLLogicalAxiom>();
        Set<OWLClass> selfCyclic = new HashSet<>();
        Set<Set<OWLClass>> largeSCCs =
                tarj.getStronglyConnectComponents().
                        stream().
                        filter(x -> x.size() > 1).
                        collect(Collectors.toSet());

        //Collect the names of axioms which is defined directly in terms of itself
        for(GraphBuilder.Vertex v : g.values()){
            if(v.joinedToSelf){
                selfCyclic.add(v.value);
            }
        }
        for(OWLLogicalAxiom axiom : axioms){
            OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
            if(selfCyclic.contains(name)){
                cycleCausing.add(axiom);
            }
            else{
                for(Set<OWLClass> component : largeSCCs){
                    if(component.contains(name)){
                        OWLClassExpression def = AxiomSplitter.getDefinitionofAxiom(axiom);
                        Set<OWLClass> defCls = ModuleUtils.getNamedClassesInSignature(def);
						Set<OWLClass> inter = Sets.intersection(component, defCls);
						if(!inter.isEmpty()){
							cycleCausing.add(axiom);
						}
                    }
                }
            }
        }
		return cycleCausing;
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

	public Set<OWLLogicalAxiom> getNaiveCycleCausingAxioms(){
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

    public Set<OWLLogicalAxiom> getCycleCausingAxioms(boolean useImprovedVersion){
	    return (useImprovedVersion) ? canWeDoBetter() : getNaiveCycleCausingAxioms();
    }


	public void printSCC(){
		for(HashSet<OWLClass> component : tarj.getStronglyConnectComponents()){
			if(component.size() > 1){
				System.out.println(component);
			}
		}
	}

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/NCI/Thesaurus_15.04d.owl");
        System.out.println("Loaded: " + ont.getLogicalAxiomCount());



        int val = 100;
        long total = 0;
        long moduleTotal = 0;
        Stopwatch timer = Stopwatch.createStarted();


        STARAMEXHybridExtractor hybrid = new STARAMEXHybridExtractor(ont.getLogicalAxioms());
        hybrid.setUseImprovedCycleRemoval(true);

        Set<OWLLogicalAxiom> randomSet = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(), val);

        int count = 1;
        for(OWLLogicalAxiom ax : randomSet){
            Set<OWLLogicalAxiom> module = hybrid.extractModule(ax.getSignature());
            System.out.println("Module " + count++ + ": " + module.size());
            moduleTotal += module.size();
        }
        timer.stop();
		total += timer.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("Time: " + timer);
        System.out.println("Total: " + total);
        System.out.println("Avg Time: " + (double) total/val);
        System.out.println("Avg Size: " + (double) moduleTotal/val);


        count = 1;
        hybrid.setUseImprovedCycleRemoval(false);

        timer = Stopwatch.createStarted();

        for(OWLLogicalAxiom ax : randomSet){
            Set<OWLLogicalAxiom> module = hybrid.extractModule(ax.getSignature());
            System.out.println("Module " + count++ + ": " + module.size());
            moduleTotal += module.size();
        }
        timer.stop();
        total += timer.elapsed(TimeUnit.MILLISECONDS);
        System.out.println("Avg Time: " + (double) total/val);
        System.out.println("Avg Size: " + (double) moduleTotal/val);
    }
}


