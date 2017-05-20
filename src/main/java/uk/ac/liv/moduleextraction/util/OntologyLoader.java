package uk.ac.liv.moduleextraction.util;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.FileDocumentSource;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.io.UnparsableOntologyException;
import org.semanticweb.owlapi.model.*;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import java.io.File;

public class OntologyLoader {

		
	public static OWLOntology loadOntologyAllAxioms(String pathName){
		ToStringRenderer stringRender = ToStringRenderer.getInstance();
		DLSyntaxObjectRenderer renderer;
		renderer =  new DLSyntaxObjectRenderer();
		stringRender.setRenderer(renderer);


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
