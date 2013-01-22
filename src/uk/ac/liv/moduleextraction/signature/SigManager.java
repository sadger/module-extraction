package uk.ac.liv.moduleextraction.signature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;


import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;

public class SigManager {

	private File directory;

	public SigManager(File directory) {
		if(!directory.exists()){
			directory.mkdir();
		}
		this.directory = directory;
	}

	public void writeFile(Set<OWLClass> signature, String name) throws IOException{
		BufferedWriter writer = new BufferedWriter(new FileWriter(new File(directory.getAbsolutePath() + "/" + name)));

		for(OWLClass cls : signature){
			writer.write(cls.getIRI().toString() + "\n");
		}

		writer.flush();
		writer.close();
	}

	public Set<OWLEntity> readFile(String location) throws IOException{
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		File signatureFile = new File(location);
		Set<OWLEntity> signature = new HashSet<OWLEntity>();
		if(signatureFile.exists()){
			BufferedReader br = new BufferedReader(new FileReader(signatureFile));
			String line;
			while((line = br.readLine()) != null) {
				String classIRI = line.trim();
				signature.add(factory.getOWLClass(IRI.create(classIRI)));
			}
			
			try{
				br.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		else
			System.err.println("No signature file found");

		return signature;
	}

	public static void main(String[] args) {
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"/NCI/nci-08.09d-terminology.owl");
		SigManager writer = new SigManager(new File(ModulePaths.getOntologyLocation() + "sigs/random"));
		SignatureGenerator gen = new SignatureGenerator(ontology.getLogicalAxioms());

		for(int i=1; i<=50; i++){
			Set<OWLClass> randomSig = gen.generateRandomClassSignature(50);
			try {
				writer.writeFile(randomSig, "random50-" + i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}
}
