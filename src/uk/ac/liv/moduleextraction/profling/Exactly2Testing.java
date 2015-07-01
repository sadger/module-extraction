package uk.ac.liv.moduleextraction.profling;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by william on 08/06/15.
 */
public class Exactly2Testing {

    public static void main(String[] args) throws IOException {
        File ontLocation = new File(ModulePaths.getOntologyLocation() + "/exactly2");
        //OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLocation.getAbsolutePath() + "/exactly2diff-3.owl");

        int n_value = 8;
        OWLDataFactory f = OWLManager.getOWLDataFactory();
        OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));

        ArrayList<OWLClass> a_vals = new ArrayList<>();
        ArrayList<OWLClass> b_vals = new ArrayList<>();
        for (int i = 1; i <= n_value ; i++) {
            a_vals.add(f.getOWLClass(IRI.create("X#A" + i)));
            b_vals.add(f.getOWLClass(IRI.create("X#B" + i)));
        }

        Set<OWLLogicalAxiom> axioms = new HashSet<>();

        System.out.println(a_vals);
        System.out.println(b_vals);

        for (int i = 0; i < n_value ; i++) {
            OWLSubClassOfAxiom ax = f.getOWLSubClassOfAxiom(a_vals.get(i), f.getOWLObjectSomeValuesFrom(r,b_vals.get(i)));
            axioms.add(ax);
        }


        OWLDisjointClassesAxiom disj_a = f.getOWLDisjointClassesAxiom(new HashSet<OWLClassExpression>(a_vals));
        OWLDisjointClassesAxiom disj_b = f.getOWLDisjointClassesAxiom(new HashSet<OWLClassExpression>(b_vals));


        axioms.addAll(disj_a.asOWLSubClassOfAxioms());
        System.out.println(disj_a.asOWLSubClassOfAxioms().size());
        axioms.addAll(disj_b.asOWLSubClassOfAxioms());



        Set<OWLEntity> sig = new HashSet<>(a_vals);


        Set<OWLLogicalAxiom> input = axioms;
        SigManager sigMan = new SigManager(ontLocation);
        //Set<OWLEntity> sig = sigMan.readFile("exact2sig-3");
        System.out.println(sig);
        input.forEach(System.out::println);
        System.out.println("Ont: " + input.size());

        for (int i = 1; i <= n_value ; i++) {
            NDepletingModuleExtractor dep = new NDepletingModuleExtractor(i,input);
            System.out.println(dep.extractModule(sig).size());
        }


    }
}
