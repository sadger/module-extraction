package uk.ac.liv.moduleextraction.qbf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



import org.sat4j.specs.IVec;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.IteratorInt;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.propositional.convertors.ALCtoPropositionalConvertor;
import uk.ac.liv.propositional.convertors.ClauseSettoSATClauses;
import uk.ac.liv.propositional.convertors.OWLOntologyToClauseSet;
import uk.ac.liv.propositional.formula.PropositionalFormula;
import uk.ac.liv.propositional.satclauses.ClauseSet;
import uk.ac.liv.propositional.satclauses.NumberMap;

public class QBFFileWriter {

	/* Factories and convertors */
	private static 	OWLDataFactory factory = OWLManager.getOWLDataFactory();
	private static ALCtoPropositionalConvertor convertor = new ALCtoPropositionalConvertor();
	/* Caches ALC->Clause conversion so need to remain static */
	private static OWLOntologyToClauseSet ontologyConvertor = new OWLOntologyToClauseSet();

	/* File writing structures */
	private ArrayList<String> toWrite;
	static String FILE_TO_WRITE;

	/*OWL Structures*/
	private Set<OWLLogicalAxiom> ontology;
	private HashSet<OWLEntity> classesNotInSignature;
	private Set<OWLEntity> signature;

	/*QBF Structures */
	private ClauseSet ontologyAsClauseSet;
	private ClauseSettoSATClauses clauseSetConvertor;
	private NumberMap numberMap;
	private IVec<IVecInt> clauses;
	
	private int variableCount;
	private int clauseCount;

	public QBFFileWriter(Set<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM) {
		FILE_TO_WRITE =  ModulePaths.getQBFSolverLocation() + "Files/qbf" + System.currentTimeMillis() + ".qdimacs";
		this.toWrite = new ArrayList<String>();
		this.ontology = ontology;
		this.signature = new HashSet<OWLEntity>(signatureAndSigM);
		this.classesNotInSignature = new HashSet<OWLEntity>();

		convertOntologyToQBFClauses();
		populateSignatures();		
	}
	
	public boolean convertedClauseSetIsEmpty() {
		return ontologyAsClauseSet.isEmpty();
	}
	
	public int getClauseCount(){
		return clauseCount;
	}
	
	public int getVariableCount(){
		return variableCount;
	}

	private void convertOntologyToQBFClauses(){
		this.ontologyAsClauseSet = ontologyConvertor.convertOntology(ontology);
		this.clauseSetConvertor = new ClauseSettoSATClauses(ontologyAsClauseSet);
		this.numberMap = clauseSetConvertor.getNumberMap();
		this.clauses = new ClauseSettoSATClauses(ontologyAsClauseSet).convert();
	}
	
	

	private void populateSignatures() {
		Set<OWLEntity> ontologyEntities = ModuleUtils.getClassAndRoleNamesInSet(ontology);
		
		signature.retainAll(ontologyEntities);
	
		classesNotInSignature.addAll(ontologyEntities);
		classesNotInSignature.removeAll(signature);
		
		/* Remove Top and Bottom classes */
		classesNotInSignature.remove(factory.getOWLThing());
		classesNotInSignature.remove(factory.getOWLNothing());
		
	}

	public File generateQBFProblem() throws IOException{
		return createQBFFile(createStringsToWrite());
	}

	private void writeHeaders() {
		variableCount = ontologyAsClauseSet.getVariables().size();
		clauseCount = ontologyAsClauseSet.getClauses().size();
		toWrite.add("p cnf " + variableCount + " " + clauseCount + "\n");		
	}

	private List<String> createStringsToWrite(){
		writeHeaders();
		writeUniversalQuantifiers();
		writeExistentialQuantifiers();
		writeClauses();

		/* Clear possibly large number map */
		numberMap.clear();
		return toWrite;
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
			e.printStackTrace();System.out.println(clauses);
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


	private void writeUniversalQuantifiers() {
		if(!signature.isEmpty()){
			toWrite.add("a ");
			for(OWLEntity ent : signature){
				PropositionalFormula clsAsVar = convertor.convert(ent);
				Integer associatedNumber = numberMap.get(clsAsVar);
				/* 
				 * If there is no mapping for the entity it no longer
				 * appears in the ontology converted to CNF
				 */
				if(!(associatedNumber == null)){
					toWrite.add(numberMap.get(clsAsVar) + " ");
				}
			}
			toWrite.add("0\n");
		}
	}

	private void writeExistentialQuantifiers() {
		if(!classesNotInSignature.isEmpty()){
			toWrite.add("e ");
			for(OWLEntity ent : classesNotInSignature){
				PropositionalFormula clsAsVar = convertor.convert(ent);
				
				/* 
				 * If there is no mapping for the entity it no longer
				 * appears in the ontology converted to CNF
				 */
				Integer associatedNumber = numberMap.get(clsAsVar);
				if(!(associatedNumber == null)){
					toWrite.add(numberMap.get(clsAsVar) + " ");
				}
				
			}
			toWrite.add("0\n");
		}
	}

	private void writeClauses() {
		Iterator<IVecInt> vectorsIterator = clauses.iterator();

		while(vectorsIterator.hasNext()){
			IteratorInt intIterator =  vectorsIterator.next().iterator();
			while(intIterator.hasNext())
				toWrite.add(intIterator.next() + " ");

			toWrite.add("0" + "\n");
		}
	}


	





}
