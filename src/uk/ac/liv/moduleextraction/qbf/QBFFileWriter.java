package uk.ac.liv.moduleextraction.qbf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

import uk.ac.liv.ontologyutils.util.ModuleUtils;
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

	/*OWL Structures*/
	private Set<OWLLogicalAxiom> ontology;
	private HashSet<OWLEntity> classesNotInSignature;
	private Set<OWLEntity> signature;

	/*QBF Structures */
	private ClauseSet ontologyAsClauseSet;
	private ClauseSettoSATClauses clauseSetConvertor;
	private NumberMap numberMap;
	private List<List<Integer>> clauses;
	
	private int variableCount;
	private int clauseCount;
	
	private File qbfFile;


	public QBFFileWriter(Set<OWLLogicalAxiom> ontology, Set<OWLEntity> signatureAndSigM) throws IOException {
		File directoryToWrite = new File("/tmp/");
		//this.qbfFile = new File(directoryToWrite + "/qbf" + System.currentTimeMillis() + ".qdimacs");

		this.qbfFile = File.createTempFile("qbf", ".qdimacs",directoryToWrite);
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
		this.clauses = new ClauseSettoSATClauses(ontologyAsClauseSet).getSatNumberMapping();
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
		return createQBFFile();
	}
	



	private File createQBFFile() throws IOException{
		if (!qbfFile.exists()) {
			try {
				qbfFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedWriter writer = new BufferedWriter(new FileWriter(qbfFile));

		try{
			writeHeaders(writer);
			writeUniversalQuantifiers(writer);
			writeExistentialQuantifiers(writer);
			writeClauses(writer);
		}
		finally{
			try {
				writer.flush();
				writer.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}


		/* Clear possibly large number map */
		numberMap.clear();
		
	
		return qbfFile;
	
	}
	
	private void writeHeaders(BufferedWriter writer) throws IOException {
		variableCount = ontologyAsClauseSet.getVariables().size();
		clauseCount = ontologyAsClauseSet.size();
		writer.write("p cnf " + variableCount + " " + clauseCount);
		writer.newLine();
	}



	private void writeUniversalQuantifiers(BufferedWriter writer) throws IOException {
		if(!signature.isEmpty()){
			writer.write("a ");
			for(OWLEntity ent : signature){
				PropositionalFormula clsAsVar = convertor.convert(ent);
				Integer associatedNumber = numberMap.get(clsAsVar);
				/* 
				 * If there is no mapping for the entity it no longer
				 * appears in the ontology converted to CNF
				 */
				if(!(associatedNumber == null)){
					writer.write(numberMap.get(clsAsVar) + " ");
				}
			}
			writer.write("0");
			writer.newLine();
		}
	}

	private void writeExistentialQuantifiers(BufferedWriter writer) throws IOException {
		if(!classesNotInSignature.isEmpty()){
			writer.write("e ");
			for(OWLEntity ent : classesNotInSignature){
				PropositionalFormula clsAsVar = convertor.convert(ent);
				
				/* 
				 * If there is no mapping for the entity it no longer
				 * appears in the ontology converted to CNF
				 */
				Integer associatedNumber = numberMap.get(clsAsVar);
				if(!(associatedNumber == null)){
					writer.write(numberMap.get(clsAsVar) + " ");
				}
				
			}
			writer.write("0");
			writer.newLine();
		}
	}

	private void writeClauses(BufferedWriter writer) throws IOException {
		for(List<Integer> clauseAsNumbers : clauses){
			for(Integer i : clauseAsNumbers){
				writer.write(i + " ");
			}
			writer.newLine();		
		}

	}


	





}
