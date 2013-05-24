package uk.ac.liv.moduleextraction.signature;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.chaindependencies.ChainDependencies;
import uk.ac.liv.moduleextraction.chaindependencies.DefinitorialDepth;
import uk.ac.liv.moduleextraction.chaindependencies.Dependency;
import uk.ac.liv.moduleextraction.datastructures.LinkedHashList;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class WriteTopLevelSignatures {
	OWLOntology ontology;
	File location;
	ChainDependencies d;
	
	public WriteTopLevelSignatures(OWLOntology ont, File location) {
		this.ontology = ont;
		this.location = location;
		
		DefinitorialDepth definitorialDepth = new DefinitorialDepth(ont.getLogicalAxioms());
		ArrayList<OWLLogicalAxiom> depthSortedAxioms = definitorialDepth.getDefinitorialSortedList();
		LinkedHashList<OWLLogicalAxiom> terminology = new LinkedHashList<OWLLogicalAxiom>(depthSortedAxioms);
		
		d = new ChainDependencies();
		d.updateDependenciesWith(terminology);
	}
	
	public void writeSignatures(){
		SigManager sigManager = new SigManager(location);
		ArrayList<OWLClass> topLevelClasses = collectTopLevelClasses();

		for(OWLClass topLevelClass : topLevelClasses){
			HashSet<OWLEntity> levelClasses = new HashSet<OWLEntity>();
			for(OWLClass cls : ontology.getClassesInSignature()){
				if(d.containsKey(cls) && d.get(cls).contains(new Dependency(topLevelClass))){
					levelClasses.add(cls);
				}
			}
			try {
				sigManager.writeFile(levelClasses, topLevelClass.toString());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<OWLClass> collectTopLevelClasses() {
		ArrayList<OWLClass> topLevelClasses = new ArrayList<OWLClass>();
		for(OWLClass cls : ontology.getClassesInSignature()){
			if(!d.containsKey(cls)){
				topLevelClasses.add(cls);
			}
		}
		return topLevelClasses;
	}
	
	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "nci-08.09d-terminology.owl");
		WriteTopLevelSignatures t = new WriteTopLevelSignatures(ont, new File(ModulePaths.getSignatureLocation() + "/NCI-Kinds"));
		t.writeSignatures();
		
	}
}
