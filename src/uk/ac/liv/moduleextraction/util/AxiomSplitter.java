package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.*;

import java.util.ArrayList;


public class AxiomSplitter {

	public static OWLClassExpression getNameofAxiom(OWLLogicalAxiom axiom){
//		AxiomMetricStore axiomStore = null;
//		try {
//			axiomStore = AxiomCache.getCache().get(axiom);
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//		return axiomStore.getName();
		AxiomType<?> type = axiom.getAxiomType();
		OWLClassExpression name = null;
		
		if(type == AxiomType.SUBCLASS_OF) {
            name = ((OWLSubClassOfAxiom) axiom).getSubClass();
        }
		else if(type == AxiomType.EQUIVALENT_CLASSES) {
            name = ((OWLEquivalentClassesAxiom) axiom).getClassExpressionsAsList().get(0);
        }
        else if(type ==  AxiomType.DISJOINT_CLASSES) {
            OWLDisjointClassesAxiom ax = (OWLDisjointClassesAxiom) axiom;
            ArrayList<OWLSubClassOfAxiom> asSubset = new ArrayList(ax.asOWLSubClassOfAxioms());
            if(asSubset.size() == 1){
                OWLSubClassOfAxiom disjointSub = asSubset.get(0);
                return getNameofAxiom(disjointSub);
            }
        }
		return name;
	}

	public static OWLClassExpression getDefinitionofAxiom(OWLLogicalAxiom axiom){
//		AxiomMetricStore axiomStore = null;
//		try {
//			axiomStore = AxiomCache.getCache().get(axiom);
//		} catch (ExecutionException e) {
//			e.printStackTrace();
//		}
//		return axiomStore.getDefinition();
		AxiomType<?> type = axiom.getAxiomType();
		OWLClassExpression definition = null;

		if(type == AxiomType.SUBCLASS_OF)
			definition = ((OWLSubClassOfAxiom) axiom).getSuperClass();
		else if(type == AxiomType.EQUIVALENT_CLASSES)
			definition = ((OWLEquivalentClassesAxiom) axiom).getClassExpressionsAsList().get(1);
        else if(type ==  AxiomType.DISJOINT_CLASSES) {
            OWLDisjointClassesAxiom ax = (OWLDisjointClassesAxiom) axiom;
            ArrayList<OWLSubClassOfAxiom> asSubset = new ArrayList(ax.asOWLSubClassOfAxioms());
            if(asSubset.size() == 1){
                OWLSubClassOfAxiom disjointSub = asSubset.get(0);
                return getDefinitionofAxiom(disjointSub);
            }
        }
		return definition;
	}

}
