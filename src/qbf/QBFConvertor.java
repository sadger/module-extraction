package qbf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import ontologyutils.OntologyLoader;

import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import formula.CNFFormula;

import sat.CNFtoSATConvertor;
import sat.OntologytoCNFConvertor;
import util.ModuleUtils;

public class QBFConvertor {

	OntologytoCNFConvertor convertor = new OntologytoCNFConvertor();
	OWLDataFactory factory = OWLManager.getOWLDataFactory();
	CNFtoSATConvertor cnftosat;

	private HashSet<OWLEntity> classesNotInSignature = new HashSet<OWLEntity>();
	private Set<OWLClass> signature;
	private CNFFormula cnfOntology;
	private Set<OWLLogicalAxiom> ontology;
	ArrayList<String> toWrite;


	public QBFConvertor(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature) {
		this.ontology = ontology;
		this.signature = signature;
		this.cnfOntology = convertor.convertOntologyToCNFClauses(ontology);
		cnftosat = new CNFtoSATConvertor(cnfOntology);
		populateSignatures();
		toWrite = new ArrayList<String>();
	}

	private void populateSignatures() {
		classesNotInSignature.addAll(ModuleUtils.getEntitiesInSet(ontology));
		classesNotInSignature.removeAll(signature);
		
		/* Remove Top and Bottom classes */
		classesNotInSignature.remove(factory.getOWLThing());
		classesNotInSignature.remove(factory.getOWLNothing());

		mapNewVariables();
	}

	private void mapNewVariables() {
		/* Give the variables not in the ontology but in the signature a number mapping */
		for(OWLClass cls : signature){
			Integer i = cnfOntology.getNumberMap().get(cls.toString());
			if(i == null){
				cnfOntology.addToNumberMap(cls.toString());
			}
		}
	}

	public File generateQBFProblem() throws IOException{
		return createQBFFile(createStringsToWrite());
	}

	private List<String> createStringsToWrite(){
		toWrite.add("c " + cnfOntology.getNumberMap() + "\n");
		toWrite.add("p cnf " + cnfOntology.variableCount() + " " + cnfOntology.clauseCount() + "\n");

		writeUniversalQuantifiers();
		writeExistentialQuantifiers();
		writeClauses();

		/* To save memory clear possible large map 
		 * */
		cnfOntology.clearNumberMap();
		
		return toWrite;
	}


	private void writeUniversalQuantifiers() {
		if(!signature.isEmpty()){
			toWrite.add("a ");
			for(OWLClass cls : signature){
				toWrite.add(cnfOntology.getNumberMap().get(cls.toString()) + " ");
			}
			toWrite.add("0\n");
		}
	}

	
	private void writeExistentialQuantifiers() {
		if(!classesNotInSignature.isEmpty()){
			toWrite.add("e ");
			for(OWLEntity cls : classesNotInSignature){
				if(!cls.isOWLClass()){
					//TODO fix this it's horrible
					toWrite.add(cnfOntology.getNumberMap().get("r_"+cls.toString()) + " ");
				}
				else{
					toWrite.add(cnfOntology.getNumberMap().get(cls.toString()) + " ");
				}
			}
			toWrite.add("0\n");
		}
	}
	
	private void writeClauses() {
		Iterator<IVecInt> vectorsIterator = cnftosat.convert().iterator();
		while(vectorsIterator.hasNext()){
			IteratorInt intIterator =  vectorsIterator.next().iterator();

			while(intIterator.hasNext()){
				toWrite.add(intIterator.next() + " ");
			}

			toWrite.add("0" + "\n");
		}
	}

	private File createQBFFile(List<String> list){
		File file = new File("/users/loco/wgatens/QBF/Files/temper.qdimacs");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		FileWriter fileWriter = null;
		try {
			fileWriter = new FileWriter(file.getAbsoluteFile());
		} catch (IOException e) {
			e.printStackTrace();
		}
		BufferedWriter writer = new BufferedWriter(fileWriter);

		try{
			for(String s : list){
				writer.write(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			try{
				fileWriter.flush();
				writer.flush();
				fileWriter.close();
				writer.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}

		return file;
	}

	public static void main(String[] args) {

		OWLDataFactory f = OWLManager.getOWLDataFactory();
		OWLOntology ont = OntologyLoader.loadOntology();
		System.out.println(ont);
		OWLClass a = f.getOWLClass(IRI.create(ont.getOntologyID().toString() + "#A"));
		OWLClass b = f.getOWLClass(IRI.create(ont.getOntologyID().toString() + "#B"));
		OWLClass c = f.getOWLClass(IRI.create(ont.getOntologyID().toString() + "#C"));
		OWLClass d = f.getOWLClass(IRI.create(ont.getOntologyID().toString() + "#Z"));

		HashSet<OWLClass> signature = new HashSet<OWLClass>();
		signature.add(a);
		signature.add(b);
		signature.add(c);
		//		signature.add(d);

		System.out.println(signature);

		File qbfproblem = null;

		QBFConvertor qbf = new QBFConvertor(ont.getLogicalAxioms(), signature);
		try {
			qbfproblem = qbf.generateQBFProblem();
		} catch (IOException e) {
			e.printStackTrace();
		}

		QBFSolver solver = new QBFSolver();
		try {
			System.out.println(!solver.isSatisfiable(qbfproblem));
		} catch (QBFSolverException e) {
			e.printStackTrace();
		}




	}
}
