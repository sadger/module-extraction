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
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

public class WriteTopLevelSignatures {
	OWLOntology ontology;
	File location;
	ChainDependencies d;
	
	public WriteTopLevelSignatures(OWLOntology ont, File location) {
		this.ontology = ont;
		this.location = location;
		d = new ChainDependencies(ont);
	}
	
	public void writeSignatures(){
		SigManager sigManager = new SigManager(location);
		ArrayList<OWLClass> topLevelClasses = collectTopLevelClasses();

		for(OWLClass topLevelClass : topLevelClasses){
			HashSet<OWLEntity> levelClasses = new HashSet<OWLEntity>();
			for(OWLClass cls : ontology.getClassesInSignature()){
				if(d.containsKey(cls) && d.get(cls).contains(topLevelClass)){
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
