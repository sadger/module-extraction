package uk.ac.liv.moduleextraction.extractor;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLAPIStreamUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.liv.moduleextraction.axiomdependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.axiomdependencies.DefinitorialAxiomStore;
import uk.ac.liv.moduleextraction.checkers.AxiomDependencyChecker;
import uk.ac.liv.moduleextraction.cycles.OntologyCycleVerifier;
import uk.ac.liv.moduleextraction.util.*;

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

    private Logger logger = LoggerFactory.getLogger(MEX.class);


    public MEX(OWLOntology ontology) throws ExtractorException{
        this(OWLAPIStreamUtils.asSet(ontology.logicalAxioms()));
    }

    public MEX(Set<OWLLogicalAxiom> axioms) throws ExtractorException{
        logger.trace("MEX module extraction: input ontology: {}", (axioms.size() < 15) ? axioms : "too large to show");
        if(isOntologyValid(axioms)){
            dependencies = new AxiomDependencies(axioms);
            equivalenceDependencies = new AxiomDependencies(collectEquivalenceAxioms(axioms));
            axiomStore = new DefinitorialAxiomStore(dependencies.getDefinitorialSortedAxioms());
            axiomDependencyChecker = new AxiomDependencyChecker();
        }

    }

    private boolean isOntologyValid(Set<OWLLogicalAxiom> ontology) throws ExtractorException{
        ELIOntologyValidator eliOntologyValidator = new ELIOntologyValidator();
        TerminologyValidator termValid = new TerminologyValidator(ontology);

        if(eliOntologyValidator.isELIOntology(ontology) && termValid.isTerminologyWithRCIs()) {
            OntologyCycleVerifier cycleVerifier = new OntologyCycleVerifier(ontology);
            return !cycleVerifier.isCyclic();
        }
        else{
            throw new ExtractorException("Input ontology must be a valid acyclic EL terminology with (optional) repeated inclusions");
        }
    }


    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLEntity> signature) {
       return extractModule(new HashSet<>(), signature);
    }

    @Override
    public Set<OWLLogicalAxiom> extractModule(Set<OWLLogicalAxiom> existingModule, Set<OWLEntity> signature) {
        logger.debug("Extracting MEX module for signature {}", (signature.size() < 15) ? signature : "|" + signature.size() + "|");
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
                        logger.trace("Axiom dependency: {}", chosenAxiom);
                        change = true;
                        module.add(chosenAxiom);
                        terminology[i] = false;
                        sigUnionSigM.addAll(OWLAPIStreamUtils.asSet(chosenAxiom.signature()));
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
                        logger.trace("Indirect dependency: {}", chosenAxiom);
                        module.add(chosenAxiom);
                        terminology[i] = false;
                        sigUnionSigM.addAll(OWLAPIStreamUtils.asSet(chosenAxiom.signature()));
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

    public static void main(String[] args) throws ExtractorException {
        OWLOntology equiv = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/equiv.krss");
        ModuleUtils.remapIRIs(equiv, "X");

        equiv.logicalAxioms().forEach(System.out::println);
        OWLDataFactory f = equiv.getOWLOntologyManager().getOWLDataFactory();

        OWLClass a = f.getOWLClass(IRI.create("X#A"));
        OWLClass a1 = f.getOWLClass(IRI.create("X#A1"));
        OWLClass a2 = f.getOWLClass(IRI.create("X#A2"));
        OWLClass c = f.getOWLClass(IRI.create("X#C"));

        Set<OWLEntity> sig = new HashSet<>(Arrays.asList(a,a1,a2));

        MEX mex = new MEX(OWLAPIStreamUtils.asSet(equiv.logicalAxioms()));
        System.out.println("Mod: " + mex.extractModule(sig));
    }

}
