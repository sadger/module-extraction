package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.prism.PrisM;

import java.io.File;
import java.io.IOException;
import java.util.Set;


public class PrismGuy {



    public static void main(String[] args) throws JRDFStoreException, IOException {



        OWLOntology example = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/iterative/iter-example-mod.owl");
        SigManager example_man = new SigManager(new File(ModulePaths.getOntologyLocation() + "/iterative/"));
        System.out.println(example.getLogicalAxiomCount());
        example.getLogicalAxioms().forEach(System.out::println);
        Set<OWLEntity> sig = example_man.readFile("signature-mod");

        PrisM prisM = new PrisM(example, PrisM.InseparabilityRelation.MODEL_INSEPARABILITY);



        System.out.println(prisM.extract(sig));


        System.out.println("Initial sig: " + sig);

        prisM.finishDisposal();



    }

}


