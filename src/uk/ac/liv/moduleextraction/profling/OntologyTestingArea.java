package uk.ac.liv.moduleextraction.profling;

import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor;
import uk.ac.liv.ontologyutils.axioms.AtomicLHSAxiomVerifier;
import uk.ac.liv.ontologyutils.axioms.NDepletingSupportedAxiomVerifier;
import uk.ac.liv.ontologyutils.axioms.SupportedPlusNominalVerifier;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

/**
 * Created by william on 17/05/16.
 */
public class OntologyTestingArea {

    public static void main(String[] args) {
        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms("/home/william/PhD/Ontologies/tones/repos/Ontology-galen/owlxml/ontology.ont");
        System.out.println(ont.getLogicalAxiomCount());


        HybridModuleExtractor hybrid = new HybridModuleExtractor(ont.getLogicalAxioms());
        SyntacticLocalityModuleExtractor star = new SyntacticLocalityModuleExtractor(ont.getOWLOntologyManager(),ont, ModuleType.STAR);
        AtomicLHSAxiomVerifier verifier = new AtomicLHSAxiomVerifier();

        NDepletingSupportedAxiomVerifier ndep = new NDepletingSupportedAxiomVerifier(new SupportedPlusNominalVerifier());

        for(OWLLogicalAxiom ax : ont.getLogicalAxioms()){

            int hybridSize = hybrid.extractModule(ax.getSignature()).size();
            int starSize = star.extract(ax.getSignature()).size();

            if(!ax.accept(ndep)){
                System.out.println(ax.getAxiomType() + ":" + ax);
            }

/*
            if(hybridSize < starSize){
                System.out.println(starSize + ":" + hybridSize + ":" + (starSize - hybridSize)) ;
            }*/
        }
    }
}
