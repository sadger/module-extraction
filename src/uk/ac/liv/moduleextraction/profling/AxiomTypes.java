package uk.ac.liv.moduleextraction.profling;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import uk.ac.liv.ontologyutils.axioms.OneDepletingSupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.axioms.SupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.liv.propositional.convertors.ALCAxiomToPropositionalConvertor;


public class AxiomTypes {


	public static void main(String[] args) {
		//	ArrayList<File> files = ModuleUtils.getListAsFiles(new File(ModulePaths.getResultLocation() + "/small-location.txt"));
			File[] files = new File("/LOCAL/wgatens/Ontologies//OWL-Corpus-All/qbf-only").listFiles();
			int i = 1;
			Set<AxiomType<?>> types = new HashSet<AxiomType<?>>();
			for(File f : files){
			//	System.out.println(i++);
				if(f.exists()){
					System.out.print(f.getName() + ": ");
					OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
					int unsupportedcount = 0;
					for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
						SupportedAxiomVerifier verifier = new SupportedAxiomVerifier();
						AxiomType<?> type = axiom.getAxiomType();
						if(!axiom.accept(new OneDepletingSupportedAxiomVerifier())){
							System.out.println(axiom.getAxiomType());
							System.out.println(axiom);
						//	System.out.println(axiom.accept(new ALCAxiomToPropositionalConvertor()));
							types.add(type);
						}
					}
					System.out.println(unsupportedcount);
				}

			}
			System.out.println("====================================");
			System.out.println();
			for(AxiomType<?> type : types){
				System.out.println(type);
			}
	}
}
