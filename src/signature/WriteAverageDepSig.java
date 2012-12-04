package signature;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import util.ModulePaths;
import checkers.DefinitorialDependencies;

public class WriteAverageDepSig {

	DefinitorialDependencies dependencies;
	SigManager sigManager;
	int[] values;
	String name;

	public WriteAverageDepSig(String folderName,DefinitorialDependencies deps, int[] values) {
		this.dependencies = deps;
		this.values = values;
		this.sigManager = new SigManager(new File(ModulePaths.getOntologyLocation() + "/" + folderName));
		this.name = folderName;
	}
	
	public void writeSignatures(){
		for(int i : values){
			SubsetConstructor constructor = new SubsetConstructor(dependencies.getDependenciesByNumber(),i,50);
			Set<OWLClass> sig = constructor.generateRandomSignature();
			try {
				sigManager.writeFile(sig, name + i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation() + "NCI/expr/nci-08.09d-terminology.owl");
		DefinitorialDependencies deps = new DefinitorialDependencies(ont.getLogicalAxioms());
		int[] values = {25,65,105,145,228,269,310,340,380,420};
		
		new WriteAverageDepSig("nci-08.09d-avgdeps",deps, values).writeSignatures();
	}
}
