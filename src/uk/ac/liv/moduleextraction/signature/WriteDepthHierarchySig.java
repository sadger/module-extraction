package uk.ac.liv.moduleextraction.signature;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.main.ModulePaths;

public class WriteDepthHierarchySig {

	private DependencyHierarchy hierarchy;
	private OWLClass cls;
	private SigManager sigManager;

	public WriteDepthHierarchySig(DependencyHierarchy hierarchy, OWLClass cls) {
		this.hierarchy = hierarchy;
		this.cls = cls;
		this.sigManager = new SigManager(new File(ModulePaths.getSignatureLocation() + "/" + cls.toString()));
	}

	public void writeSignatures(){
		/* If there is more than one with the maximum size set of
		   dependencies, only write the first */
		Set<OWLEntity> sigToWrite = new HashSet<OWLEntity>();
		//Add the class itself
		sigToWrite.add(cls);

		boolean maxWritten = false;
		int lastSeenSize = 0;
		int dependencyLevel = 1;

		while(!maxWritten){
			sigToWrite.addAll(hierarchy.getDependencyForDepth(cls, dependencyLevel));
			if(lastSeenSize == sigToWrite.size())
				maxWritten = true;
			else{
				try {
					sigManager.writeFile(sigToWrite, cls.toString() + "-level_" + dependencyLevel + "-" + "size_" + sigToWrite.size());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			lastSeenSize = sigToWrite.size();
			dependencyLevel++;
		}
	}

	public static void main(String[] args) {
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		OWLOntology ont = OntologyLoader.loadOntologyInclusionsAndEqualities(ModulePaths.getOntologyLocation() + "/nci-08.09d-terminology.owl");
		DependencyHierarchy hier = new DependencyHierarchy(ont.getLogicalAxioms());
		OWLClass cls = factory.getOWLClass(IRI.create("http://ncicb.nci.nih.gov/xml/owl/EVS/Thesaurus.owl#Intermediate_Fibrocytic_Neoplasm"));
		new WriteDepthHierarchySig(hier, cls).writeSignatures();
	}

}
