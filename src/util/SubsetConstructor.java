package util;

import java.util.ArrayList;

import loader.OntologyLoader;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;

import checkers.DefinitorialDependencies;

public class SubsetConstructor {

	public SubsetConstructor() {
		// TODO Auto-generated constructor stub
	}


	public void subsets(int n, int k)
	{
		subsets(n, k, "");
	}

	public void subsets(int n, int k, String suffix)
	{
		// This method is seemingly more general than required. It
		// constructs all subsets of size k of {1,...,n}, each followed
		// by the string suffix. This more general method is required
		// due to the way the recursive method works.
		if(k == 0){
			System.out.println(suffix);
		}
		else
		{
			subsets(n-1, k-1, " " + n + suffix);
			if(k < n)
				subsets(n-1, k, suffix);
		}
	}
	

}
