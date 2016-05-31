package uk.ac.liv.moduleextraction.profling;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import com.google.common.base.Joiner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import org.semanticweb.owlapi.util.DLExpressivityChecker;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
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

        HashSet<String> names = new HashSet<>();
        String[] equiOnts = {"ontology-metadata.owl", "PR", "TAO", "VSAO", "ProPreO", "MHC", "PATO","Activity.owl", "BFO", "GO-EXT", "OPL", "study_design.owl", "BSPO", "PhylOnt", "SPO", "JERM", "OntoDM-core", "UBERON", "MF", "statistics.owl", "NBO", "vivo", "SDO", "FBbi", "NeoMark", "EP", "OMIT", "IDODEN", "DetectionMechanisms.owl", "OntoKBCF", "CAO", "FYPO", "IMGT", "VSO", "PHARE", "SIO", "CCONT", "PO_PAE", "amino-acid", "Genomic-CDS", "SAO", "ERO", "GRO", "invertebrata", "BHO", "OntoDM-KDD", "OBIws", "BDO", "NPO", "RNAO", "OBOE", "EFO", "CDAO", "BT", "OBI", "time-entry.owl", "CHEMINF", "GALEN", "OntoMA", "cpo-inferred.owl", "uni-ece", "ECG", "CL", "FHHO", "SWO", "ICF", "LiPrO", "biocode", "ECO", "dikb-evidence", "NTDO", "DC_CL", "HAO", "TMO", "GRO_CPGA", "SitBAC", "OMRSE", "ICNP", "AERO", "PO"};
        ELValidator validator = new ELValidator();

        File ontLoc = new File(ModulePaths.getOntologyLocation() + "/Bioportal");
        ArrayList<String> equiNames = new ArrayList<>(Arrays.asList(equiOnts));
        Collections.sort(equiNames,String.CASE_INSENSITIVE_ORDER);

        System.out.println("Name, |Ontology|, |TBox|, |≡|, TBox EL?, TBox cyclic?");
        for(String f: equiNames){

            OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath() + "/" + f);
//
            ArrayList<String> metrics = new ArrayList<>();


            Set<OWLLogicalAxiom> core = ModuleUtils.getCoreAxioms(ont);

            metrics.add(f);
            metrics.add(String.valueOf(ont.getLogicalAxiomCount()));
            metrics.add(String.valueOf(core.size()));
            metrics.add(String.valueOf(ont.getAxiomCount(AxiomType.EQUIVALENT_CLASSES)));
            metrics.add(String.valueOf(validator.isELOntology(core)));

            OntologyCycleVerifier cycle = new OntologyCycleVerifier(core);
            metrics.add(String.valueOf(cycle.isCyclic()));

            Joiner j = Joiner.on(",");
            System.out.println(j.join(metrics));


            }



        }










}








