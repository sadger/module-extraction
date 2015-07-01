package uk.ac.liv.moduleextraction.profling;


import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.Files;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import uk.ac.liv.ontologyutils.axioms.AxiomStructureInspector;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.ontologies.ABoxPropertyChecker;
import uk.ac.liv.ontologyutils.util.ModulePaths;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class CSVWriter {

    public CSVWriter() {

    }

    public static void main(String[] args) throws IOException {

        File criteriaOntologies = new File("");

        System.out.println("Name, DisjointClasses, TransitiveRole, SymmetricRole, InverseRole, FunctionalRole, InverseFunctionRole"
        		+ "ReflexiveRole, DisjointRoles, RoleInclusions, DomainRestriction, RangeRestriction, ClassAssertion, RoleAssertion");

        HashSet<AxiomType> types = new HashSet<AxiomType>();


        List<String> lines = Files.readLines(new File(ModulePaths.getSignatureLocation() + "/ExtendedCriteria/extended.txt"), Charsets.UTF_8);

        for (String filePath : lines) {
            File f = new File(filePath);
            if (f.isFile()) {
                String shortName = f.getName().substring(Math.max(0, f.getName().length() - 20));
                OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(f.getAbsolutePath());
                ArrayList<String> csvRow = new ArrayList<String>();
                for(OWLLogicalAxiom axiom : ont.getLogicalAxioms()){
                    types.add(axiom.getAxiomType());
                }
                
    
 
               csvRow.add(shortName);

             
               
               //Class properties
               csvRow.add(String.valueOf(ont.getAxiomCount(AxiomType.DISJOINT_CLASSES)));
              
               //Role properties
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.TRANSITIVE_OBJECT_PROPERTY)));
               csvRow.add(String.valueOf(  ont.getAxiomCount(AxiomType.SYMMETRIC_OBJECT_PROPERTY)));
               csvRow.add(String.valueOf(  ont.getAxiomCount(AxiomType.INVERSE_OBJECT_PROPERTIES)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.FUNCTIONAL_OBJECT_PROPERTY)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.REFLEXIVE_OBJECT_PROPERTY)));
               
               
               //Relating roles
               csvRow.add(String.valueOf(  ont.getAxiomCount(AxiomType.DISJOINT_OBJECT_PROPERTIES)));
               csvRow.add(String.valueOf(  ont.getAxiomCount(AxiomType.SUB_OBJECT_PROPERTY)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.OBJECT_PROPERTY_DOMAIN)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.OBJECT_PROPERTY_RANGE)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.EQUIVALENT_OBJECT_PROPERTIES)));
            
               //ABox
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.CLASS_ASSERTION)));
               csvRow.add(String.valueOf( ont.getAxiomCount(AxiomType.OBJECT_PROPERTY_ASSERTION)));
               


               Joiner joiner = Joiner.on(",");
               System.out.println(joiner.join(csvRow));
           
                ont = null;
            }
        }
        System.out.println(types);
    }

}
