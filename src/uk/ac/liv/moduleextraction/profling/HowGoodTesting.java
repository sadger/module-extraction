package uk.ac.liv.moduleextraction.profling;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.extractor.HybridModuleExtractor;
import uk.ac.liv.moduleextraction.extractor.NDepletingModuleExtractor;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

import java.util.Set;

/**
 * Created by william on 12/06/16.
 */
public class HowGoodTesting {

    public static void main(String[] args) {

        OWLOntology guards = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/how-good/guards.owl");
        ModuleUtils.remapIRIs(guards,"X");

        guards.getLogicalAxioms().forEach(System.out::println);

        OWLDataFactory f = guards.getOWLOntologyManager().getOWLDataFactory();

        OWLClass guard = f.getOWLClass(IRI.create("X#Guard"));
        OWLClass first_guard = f.getOWLClass(IRI.create("X#FirstGuard"));

        Set<OWLEntity> sig = Sets.newHashSet(first_guard);

        NDepletingModuleExtractor ndep = new NDepletingModuleExtractor(1, guards.getLogicalAxioms());
        HybridModuleExtractor hybrid = new HybridModuleExtractor(guards);

        System.out.println(hybrid.extractModule(sig));
        System.out.println(ndep.extractModule(sig));

    }
}
