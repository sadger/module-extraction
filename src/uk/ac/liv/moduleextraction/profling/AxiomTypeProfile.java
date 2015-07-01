package uk.ac.liv.moduleextraction.profling;

import java.io.*;
import java.util.Collections;
import java.util.HashMap;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import org.semanticweb.owlapi.util.DLExpressivityChecker;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.OntologyCycleVerifier;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class AxiomTypeProfile {

    private HashMap<AxiomType<?>, Integer> typeMap = new HashMap<AxiomType<?>, Integer>();

    public AxiomTypeProfile(OWLOntology ontology) {

        for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
            AxiomType<?> axiomType = axiom.getAxiomType();
            Integer count = typeMap.get(axiomType);
            if(axiomType == AxiomType.INVERSE_OBJECT_PROPERTIES){
                //System.out.println(axiom);
            }
            if(count == null)
                typeMap.put(axiomType, 1);
            else
                typeMap.put(axiomType, ++count);
        }
    }

    public void printMetrics(){
        System.out.println("== Axiom Types ==");
        for(AxiomType<?> type : typeMap.keySet()){
            System.out.println(type.getName() + ":" + typeMap.get(type));
        }
    }



    public static void main(String[] args) throws IOException {


        String ontologyName = "3ac2a2b1-a86e-453b-830d-6814b286da46_owl%2Fcoma-QBF";
        File ontLoc =
                new File(ModulePaths.getOntologyLocation() + "/NCI/1");




        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());
        System.out.println(ont.getLogicalAxiomCount());
        OntologyCycleVerifier verifier = new OntologyCycleVerifier(ModuleUtils.getCoreAxioms(ont));
        DLExpressivityChecker checker = new DLExpressivityChecker(Collections.singleton(ont));

        ExpressionTypeProfiler type = new ExpressionTypeProfiler();
        type.profileOntology(ont);
        type.printMetrics();
        String express = checker.getDescriptionLogicName();
        System.out.println("Expressivity: " + express);
        System.out.println("Axioms: " + ont.getLogicalAxiomCount());
        System.out.println("Core Cyclic:  " + verifier.isCyclic());
        new AxiomTypeProfile(ont).printMetrics();

    }



}








