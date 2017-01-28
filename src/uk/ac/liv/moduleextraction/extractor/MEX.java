package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.*;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.axiomdependencies.DefinitorialAxiomStore;
import uk.ac.liv.moduleextraction.checkers.AxiomDependencyChecker;
import uk.ac.liv.moduleextraction.util.AxiomSplitter;
import uk.ac.liv.moduleextraction.util.ModulePaths;
import uk.ac.liv.moduleextraction.util.ModuleUtils;
import uk.ac.liv.moduleextraction.util.OntologyLoader;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by william on 23/09/16.
 */
public class MEX implements Extractor {

    private AxiomDependencyChecker axiomDependencyChecker;
    private AxiomDependencies dependencies;
    private AxiomDependencies equivalenceDependencies;
    private Set<OWLClass> defT;
    private DefinitorialAxiomStore axiomStore;

    private Set<OWLLogicalAxiom> module;
    private Set<OWLEntity> sigUnionSigM;

    public MEX(OWLOntology ontology) {
        this(ontology.getLogicalAxioms());
    }

    public MEX(Set<OWLLogicalAxiom> axioms){
        dependencies = new AxiomDependencies(axioms);
        equivalenceDependencies = new AxiomDependencies(collectEquivalenceAxioms(axioms));
        axiomStore = new DefinitorialAxiomStore(dependencies.getDefinitorialSortedAxioms());
        axiomDependencyChecker = new AxiomDependencyChecker();
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
       return extractModule(new HashSet<>(), signature);
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        boolean[] terminology = axiomStore.allAxiomsAsBoolean();
        module = existingModule;
        sigUnionSigM = ModuleUtils.getClassAndRoleNamesInSet(existingModule);
        sigUnionSigM.addAll(signature);

        applyRules(terminology);

        return module;
    }

    private void applyRules(boolean[] terminology){
        locateAxiomDependencies(terminology);
        if(locateIndirectDependency(terminology)){
            applyRules(terminology);
        }
    }

    private void locateAxiomDependencies(boolean[] terminology){
        boolean change = true;
        while(change){
            change = false;
            for (int i = 0; i < terminology.length; i++) {
                if(terminology[i]){
                    OWLLogicalAxiom chosenAxiom = axiomStore.getAxiom(i);
                    if(axiomDependencyChecker.hasSyntacticSigDependency(chosenAxiom, dependencies, sigUnionSigM)){
                        change = true;
                        module.add(chosenAxiom);
                        terminology[i] = false;
                        sigUnionSigM.addAll(chosenAxiom.getSignature());
                    }
                }
            }
        }
    }

    private boolean locateIndirectDependency(boolean[] terminology){
        boolean containsIndirectDependency = false;
        for (int i = 0; i < terminology.length; i++) {
            if(terminology[i]){
                OWLLogicalAxiom chosenAxiom = axiomStore.getAxiom(i);
                OWLClass name = (OWLClass) AxiomSplitter.getNameofAxiom(chosenAxiom);

                //Candidate for causing an indirect dependency
                if(chosenAxiom.getAxiomType() == AxiomType.EQUIVALENT_CLASSES
                    && sigUnionSigM.contains(name)){

                    //Compute LHS dependencies
                    Set<OWLEntity> lhsDependencies = new HashSet<>(equivalenceDependencies.get(chosenAxiom));
                    lhsDependencies.removeAll(defT);

                    //Compute RHS dependencies
                    Set<OWLEntity> rhsDependencies = new HashSet<>();
                    for (int j = 0; j < terminology.length; j++) {
                        if (i != j && terminology[j]) {
                            OWLLogicalAxiom dependencyAxiom = axiomStore.getAxiom(j);
                            OWLClass dependencyName = (OWLClass) AxiomSplitter.getNameofAxiom(dependencyAxiom);
                            if(sigUnionSigM.contains(dependencyName)){
                                rhsDependencies.addAll(dependencies.get(dependencyAxiom));
                            }
                        }
                    }

                    //Determine if RHS contains all LHS - an indirect dependency is detected
                    containsIndirectDependency = rhsDependencies.containsAll(lhsDependencies);

                    if(containsIndirectDependency){
                        module.add(chosenAxiom);
                        terminology[i] = false;
                        sigUnionSigM.addAll(chosenAxiom.getSignature());
                    }
                }

            }
        }
        return containsIndirectDependency;
    }

    private Set<OWLLogicalAxiom> collectEquivalenceAxioms(Set<OWLLogicalAxiom> axioms){
        Set<OWLLogicalAxiom> equivalenceAxioms = new HashSet<>();
        defT = new HashSet<>();
        axioms.stream()
                .filter(ax -> ax.getAxiomType() == AxiomType.EQUIVALENT_CLASSES)
                .forEach(ax -> {
                    equivalenceAxioms.add(ax);
                    defT.add((OWLClass) AxiomSplitter.getNameofAxiom(ax));
                });
        return equivalenceAxioms;
    }

    public static void main(String[] args) {
        OWLOntology equiv = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/equiv.krss");
        ModuleUtils.remapIRIs(equiv, "X");

        equiv.getLogicalAxioms().forEach(System.out::println);
        OWLDataFactory f = equiv.getOWLOntologyManager().getOWLDataFactory();

        OWLClass a = f.getOWLClass(IRI.create("X#A"));
        OWLClass a1 = f.getOWLClass(IRI.create("X#A1"));
        OWLClass a2 = f.getOWLClass(IRI.create("X#A2"));
        OWLClass c = f.getOWLClass(IRI.create("X#C"));

        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(a,a1,a2));

        MEX mex = new MEX(equiv.getLogicalAxioms());
        System.out.println("Mod: " + mex.extractModule(sig));
    }

}
