package uk.ac.liv.moduleextraction.util;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;

import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class OntologySplitter {

	OWLOntologyManager manager;
	public OntologySplitter() {
		manager = OWLManager.createOWLOntologyManager();
	}
	
	/**
	 * Splits ontology into inclusions
	 * and equalities and saves result to disk
	 * Other axioms discarded.
	 * @param Location of ontology to split
	 * @throws OWLOntologyCreationException 
	 * @throws OWLOntologyStorageException 
	 */
	public void splitOntology(File ontLocation) throws OWLOntologyCreationException, OWLOntologyStorageException{
		
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ontLocation.getAbsolutePath());
		
		HashSet<OWLAxiom> subClassAxioms = new HashSet<OWLAxiom>();
		subClassAxioms.addAll(ont.getAxioms(AxiomType.SUBCLASS_OF));
		
		HashSet<OWLAxiom> equivAxioms = new HashSet<OWLAxiom>();
		equivAxioms.addAll(ont.getAxioms(AxiomType.EQUIVALENT_CLASSES));
		
		
		OWLOntology subOnt = manager.createOntology(subClassAxioms);
		OWLOntology equivOnt = manager.createOntology(equivAxioms);
		
		OWLXMLOntologyFormat owlxmlFormat = new OWLXMLOntologyFormat();
		

		String baseName = ontLocation.getName();
		String baseDir = ontLocation.getParent();
		
		
		System.out.println("Subclassof count:" + subOnt.getLogicalAxiomCount());
		System.out.println("Equiv Count " + equivOnt.getLogicalAxiomCount());
		
		manager.saveOntology(subOnt,owlxmlFormat,IRI.create(new File(baseDir + "/" + baseName + "-sub")));
		manager.saveOntology(equivOnt,owlxmlFormat,IRI.create(new File(baseDir + "/" + baseName + "-equiv")));
	
	}
	
	public static void main(String[] args) {
		
		OntologySplitter splitter = new OntologySplitter();
		try {
			splitter.splitOntology(new File(ModulePaths.getOntologyLocation() + "NCI/Profile/Thesaurus_08.09d.OWL"));
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			e.printStackTrace();
		}
	}
}
