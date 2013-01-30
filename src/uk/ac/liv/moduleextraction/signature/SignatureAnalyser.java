package uk.ac.liv.moduleextraction.signature;


import java.util.Set;


import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import uk.ac.liv.moduleextraction.checkers.DefinitorialDependencies;
import uk.ac.liv.moduleextraction.util.DefinitorialDepth;


public class SignatureAnalyser {

	private Set<OWLLogicalAxiom> logicalAxioms;
	private DefinitorialDepth definitorialDepth;
	private DefinitorialDependencies dependencies;

	public SignatureAnalyser(Set<OWLLogicalAxiom> axioms) {
		this.logicalAxioms = axioms;
		this.definitorialDepth = new DefinitorialDepth(logicalAxioms);
		this.dependencies = new DefinitorialDependencies(logicalAxioms);
	}

	public int averageDefinitorialDepth(Set<OWLClass> signature){
		int totalDepth = 0;

		for(OWLClass cls : signature)
			totalDepth += definitorialDepth.lookup(cls);

		return totalDepth/signature.size();
	}

	public int averageDependencySize(Set<OWLClass> signature){
		int totalSize = 0;
		for(OWLClass cls : signature)
			totalSize += dependencies.getDependenciesFor(cls).size();
		
		return totalSize/signature.size();
	}

	public int averageDistanceBetweenConcepts(Set<OWLClass> signature){
		return 0;
	}
}
