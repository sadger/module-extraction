package uk.ac.liv.moduleextraction.extractor;

public class NotEquivalentToTerminologyException extends Exception {
	private static final long serialVersionUID = -4211072461164166268L;

	@Override
	public String toString() {
		return "Ontology not logically equivalent to terminology - cannot extract module";
	}
}
