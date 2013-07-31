package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.liv.moduleextraction.extractor.IteratingExtractor;
import uk.ac.liv.ontologyutils.expressions.ALCValidator;
import uk.ac.liv.ontologyutils.expressions.ELValidator;
import uk.ac.liv.ontologyutils.util.ModuleUtils;
import uk.ac.manchester.cs.owlapi.modularity.ModuleType;
import uk.ac.manchester.cs.owlapi.modularity.SyntacticLocalityModuleExtractor;

public class IteratingExperiment implements Experiment {

	private SyntacticLocalityModuleExtractor starExtractor;
	private IteratingExtractor iteratingExtractor;
	private int starSize = 0;
	private int itSize = 0;
	private Set<OWLLogicalAxiom> starModule;
	private Set<OWLLogicalAxiom> itModule;
	private OWLOntology ontology;
	static int subcount = 0;
	static int equivcount = 0;
	static int othercount = 0;
	static int alcCount = 0;

	private static HashMap<OWLLogicalAxiom, Integer> differenceMap = new HashMap<OWLLogicalAxiom, Integer>();

	public IteratingExperiment(OWLOntology ont) {
		this.ontology = ont;
		OWLOntologyManager manager = ont.getOWLOntologyManager();
		this.starExtractor = new SyntacticLocalityModuleExtractor(manager, ont, ModuleType.STAR);
		this.iteratingExtractor = new IteratingExtractor(ont);
	}

	public static HashMap<OWLLogicalAxiom, Integer> getDifferenceMap(){
		return differenceMap;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {

		starModule = ModuleUtils.getLogicalAxioms(starExtractor.extract(signature));
		starSize = starModule.size();

		ALCValidator valid = new ALCValidator();
		ELValidator el = new ELValidator();

		if(starModule.size() == ontology.getLogicalAxiomCount()){
			Set<OWLLogicalAxiom> axioms = ModuleUtils.getAxiomsForSignature(ontology, signature);
			for(OWLLogicalAxiom axiom : axioms){
				System.out.println(axiom);
		
				if(valid.isALCAxiom(axiom) && !el.isELAxiom(axiom)){
					alcCount++;
				}
			}
		}


		itModule = iteratingExtractor.extractModule(signature);
		itSize = itModule.size();


	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		writer.write("StarSize, IteratedSize, Difference, QBFChecks, StarExtractions, AmexExtractions" + "\n");
		writer.write(starSize + "," + itSize + "," + ((starSize == itSize) ? "0" : "1") + "," +  iteratingExtractor.getQBFChecks() + "," +
				iteratingExtractor.getStarExtractions() + "," + iteratingExtractor.getAmexExtrations() + "\n");
		writer.flush();
		writer.close();

		for(OWLLogicalAxiom ax : starModule){
			Integer count = differenceMap.get(ax);
			if(count == null){
				differenceMap.put(ax, 1);
			}
			else{
				differenceMap.put(ax, count + 1);
			}
		}
	}


}
