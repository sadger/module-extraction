package qbf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import loader.OntologyLoader;


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
import util.ModulePaths;
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
	
	SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmms");


	/* Must have .qdimacs extention or skizzo complains */
	static String FILE_TO_WRITE;

	public QBFConvertor(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature) {
		FILE_TO_WRITE =  ModulePaths.getQBFSolverLocation() + "Files/qbf" + System.currentTimeMillis() + ".qdimacs";
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
		System.out.printf("V:%d C:%d\n", cnfOntology.variableCount(), cnfOntology.clauseCount());
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
		File file = new File(FILE_TO_WRITE);
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

}
