package uk.ac.liv.moduleextraction.experiments;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;

public interface Experiment {
	
	public void performExperiment(Set<OWLEntity> signature);
	
	public void writeMetrics(File experimentLocation) throws IOException;
}
