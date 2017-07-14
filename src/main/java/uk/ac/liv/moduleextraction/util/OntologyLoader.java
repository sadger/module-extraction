package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.dlsyntax.renderer.DLSyntaxObjectRenderer;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.*;

import java.io.File;

public class OntologyLoader {

		
	public static OWLOntology loadOntologyAllAxioms(String pathName){

		ToStringRenderer stringRenderer= new ToStringRenderer();
		stringRenderer.setRenderer(() -> new DLSyntaxObjectRenderer());


		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		OWLOntologyLoaderConfiguration configuration = new OWLOntologyLoaderConfiguration();
		configuration.setMissingImportHandlingStrategy(MissingImportHandlingStrategy.SILENT);

		OWLOntology ontology = null;
		
		FileDocumentSource source = new FileDocumentSource(new File(pathName));
		MissingImportListener listener = importer -> System.out.println("Missing import");
		manager.addMissingImportListener(listener);
		
		try {
			ontology =  
					manager.loadOntologyFromOntologyDocument(source, configuration);
			
		}
		catch(UnparsableOntologyException e){
			System.out.println("Unparsable");
			e.printStackTrace();
		}
        catch (UnloadableImportException importe){
            System.out.println("Unloadable Import");
        }
		catch (OWLOntologyCreationException e) {
			System.out.println("Creation failed: " + e.getCause());
			e.printStackTrace();
		}
		
		

		return ontology;
	}
	

	public static void main(String[] args) {
		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/OWL-Corpus-All/qbf-only/3");
		System.out.println(ont.getLogicalAxiomCount());
			
		
	}


}
