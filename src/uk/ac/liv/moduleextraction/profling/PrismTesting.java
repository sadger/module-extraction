/*
package uk.ac.liv.moduleextraction.profling;

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;

import java.util.Set;

class PrismTesting{

    public static void main(String[] args) throws JRDFStoreException {

        OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "LiPrO.owl");
        PrisM prisM = new PrisM(ont, PrisM.InseparabilityRelation.MODEL_INSEPARABILITY);
        SyntacticLocalityModuleExtractor syntacticLocalityModuleExtractor = new SyntacticLocalityModuleExtractor(ont.getOWLOntologyManager(),ont, ModuleType.STAR);
        Set<OWLLogicalAxiom> subset = ModuleUtils.generateRandomAxioms(ont.getLogicalAxioms(),5);


        ont.getLogicalAxioms().forEach(System.out::println);

        for(OWLLogicalAxiom ax : subset){
            Set<OWLAxiom> starMod = syntacticLocalityModuleExtractor.extract(ax.getSignature());
            Set<OWLAxiom> datalogMod = prisM.extract(ax.getSignature());
            if(datalogMod.size() > starMod.size()){
*/
/*                System.out.println(starMod);
                System.out.println(datalogMod);*//*


                System.out.println(datalogMod.containsAll(starMod));
               starMod.removeAll(datalogMod);
                System.out.println(starMod.size());
                System.out.println(datalogMod);
            }
            //System.out.println("H: " + hybridModuleExtractor.extractModule(ax.getSignature()).size());
            System.out.println();
        }

        System.out.println("HERE");
        prisM.finishDisposal();

    }
}*/
