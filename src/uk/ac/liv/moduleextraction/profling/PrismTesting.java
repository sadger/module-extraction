/*
package uk.ac.liv.moduleextraction.profling;

import com.sun.org.apache.xpath.internal.operations.Mod;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;

import java.util.Set;

*/
/**
 * Created by wgatens on 15/09/15.
 *//*

public class PrismTesting {

    public static void main(String[] args) {

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/Bioportal/treatment.obo");
        PrisM p = new PrisM(ont, PrisM.InseparabilityRelation.MODEL_INSEPARABILITY);


        Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(),1);

        for(OWLLogicalAxiom ax : subset){
            try {
                p.extract(ax.getSignature());
            } catch (JRDFStoreException e) {
                e.printStackTrace();
            }
        }



    }
}
*/
