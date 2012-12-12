package signature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import loader.OntologyLoader;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;

import util.ModulePaths;

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

	public Set<OWLClass> readFile(String location) throws IOException{
		OWLDataFactory factory = OWLManager.getOWLDataFactory();
		File signatureFile = new File(location);
		Set<OWLClass> signature = new HashSet<OWLClass>();
		if(signatureFile.exists()){
			BufferedReader br = new BufferedReader(new FileReader(signatureFile));
			String line;
			while((line = br.readLine()) != null) {
				String classIRI = line.trim();
				signature.add(factory.getOWLClass(IRI.create(classIRI)));
			}
		}
		else
			System.err.println("No signature file found");

		return signature;
	}

	public static void main(String[] args) {
		OWLOntology ontology = OntologyLoader.loadOntology(ModulePaths.getOntologyLocation()+"/nci-08.09d-terminology.owl");
		SigManager writer = new SigManager(new File(ModulePaths.getOntologyLocation() + "random"));
		SignatureGenerator gen = new SignatureGenerator(ontology.getLogicalAxioms());

		for(int i=0; i<=10; i++){
			Set<OWLClass> randomSig = gen.generateRandomClassSignature(50);
			try {
				writer.writeFile(randomSig, "random50-" + i);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


	}
}
