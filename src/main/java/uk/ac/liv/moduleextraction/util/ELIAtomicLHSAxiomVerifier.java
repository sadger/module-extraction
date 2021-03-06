package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;

/**
 *  Methods to check if an axiom is supported
 *  by our extraction algorithm. Checking
 *  axiom types is no sufficent as they can contain
 *  data literals as well as logical meaning.
 *  
 *  Currently this is inclusions and equalities up to ALCQI
 *
 */
public class ELIAtomicLHSAxiomVerifier {

	public boolean isELIandHasAtomicLHS(OWLLogicalAxiom axiom){
		
		AxiomType<?> type = axiom.getAxiomType();
		ELIExpressionValidator eliExpressionVerifier = new ELIExpressionValidator();
	
		if(type == AxiomType.SUBCLASS_OF || type == AxiomType.EQUIVALENT_CLASSES){
			
			OWLClassExpression name = AxiomSplitter.getNameofAxiom(axiom);
			OWLClassExpression definition = AxiomSplitter.getDefinitionofAxiom(axiom);
			
			return (name.getClassExpressionType() == ClassExpressionType.OWL_CLASS) &&
					definition.accept(eliExpressionVerifier);
		}
		else{
			return false;
		}
	}


}
