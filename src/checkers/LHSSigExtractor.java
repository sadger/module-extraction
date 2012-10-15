package checkers;

import java.util.HashSet;
import java.util.Set;

import interpretation.util.AxiomSplitter;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;


public class LHSSigExtractor {
	Dependencies deps;

	public HashSet<OWLLogicalAxiom> getLHSSigAxioms(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature){
		HashSet<OWLLogicalAxiom> lhsSigT = new HashSet<OWLLogicalAxiom>();
		deps = new Dependencies(ontology);
		for(OWLLogicalAxiom axiom : ontology){
			OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
			if(signature.contains(name) || isInDependencies(name, signature)){
				lhsSigT.add(axiom);
			}
		}
		return lhsSigT;
	}
	

	private boolean isInDependencies(OWLClass name, Set<OWLClass> signature){
		for(OWLClass sigElem : signature){
			Set<OWLClass> sigDeps = deps.getDependenciesFor(sigElem);
			if(!(sigDeps == null) && sigDeps.contains(name)){
				return true;
			}
		}
		return false;
	}
}
