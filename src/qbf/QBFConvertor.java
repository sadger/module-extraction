package qbf;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

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
import temp.ontologyloader.OntologyLoader;
import util.ModuleUtils;

public class QBFConvertor {

	OntologytoCNFConvertor convertor = new OntologytoCNFConvertor();
	CNFtoSATConvertor cnftosat;


	public QBFConvertor() {
	}

	public File generateQBFProblem(Set<OWLLogicalAxiom> ontology, Set<OWLClass> signature) throws IOException{
		OWLDataFactory factory = OWLManager.getOWLDataFactory();

		CNFFormula cnfOntology = convertor.convertOntologyToCNFClauses(ontology);
		cnftosat = new CNFtoSATConvertor(cnfOntology);

		HashSet<OWLEntity> classesNotInSignature = new HashSet<OWLEntity>();
		classesNotInSignature.addAll(ModuleUtils.getEntitiesInSet(ontology));
		classesNotInSignature.removeAll(signature);

		//Remove Top and Bottom classes
		classesNotInSignature.remove(factory.getOWLThing());
		classesNotInSignature.remove(factory.getOWLNothing());

		//Give the variables not in the ontology but in the signature a number mapping
		for(OWLClass cls : signature){
			Integer i = cnfOntology.getNumberMap().get(cls.toString());
			if(i == null){
				cnfOntology.addToNumberMap(cls.toString());
			}
		}


		return createQBFFile(signature,classesNotInSignature,cnfOntology);

	}

	private File createQBFFile(Set<OWLClass> signature, 
			HashSet<OWLEntity> classesNotInSignature, 
			CNFFormula cnfOntology) throws IOException{

		File file = new File("/users/loco/wgatens/QBF/Files/temper.qdimacs");
		if (!file.exists()) {
			file.createNewFile();
		}

		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);


		Iterator<IVecInt> vectorsIterator = cnftosat.convert().iterator();
		
		bw.write("c " + cnfOntology.getNumberMap() + "\n");
		bw.write("p cnf " + cnfOntology.variableCount() + " " + cnfOntology.clauseCount() + "\n");
		
		if(!signature.isEmpty()){
			bw.write("a ");
			for(OWLClass cls : signature){
				bw.write(cnfOntology.getNumberMap().get(cls.toString()) + " ");
			}
			bw.write("0\n");
		}


		if(!classesNotInSignature.isEmpty()){
			bw.write("e ");
			for(OWLEntity cls : classesNotInSignature){
				if(!cls.isOWLClass()){
					//TODO fix this it's horrible
					bw.write(cnfOntology.getNumberMap().get("r_"+cls.toString()) + " ");
				}
				else{
					bw.write(cnfOntology.getNumberMap().get(cls.toString()) + " ");
				}
				
			}
			bw.write("0\n");
		}


		while(vectorsIterator.hasNext()){
			IteratorInt intIterator =  vectorsIterator.next().iterator();

			while(intIterator.hasNext()){
				bw.write(intIterator.next() + " ");
			}

			bw.write("0" + "\n");
		}

		bw.flush();
		bw.close();

		cnfOntology.clearNumberMap();
		
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

		QBFConvertor qbf = new QBFConvertor();
		try {
			qbfproblem = qbf.generateQBFProblem(ont.getLogicalAxioms(), signature);
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
