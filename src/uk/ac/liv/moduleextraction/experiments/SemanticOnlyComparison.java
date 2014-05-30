package uk.ac.liv.moduleextraction.experiments;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLogicalAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;

import com.google.common.base.Stopwatch;
import com.google.common.collect.Sets;

import uk.ac.liv.moduleextraction.chaindependencies.AxiomDependencies;
import uk.ac.liv.moduleextraction.extractor.SemanticOnlyExtractor;
import uk.ac.liv.moduleextraction.signature.SigManager;
import uk.ac.liv.moduleextraction.signature.SignatureGenerator;
import uk.ac.liv.ontologyutils.axioms.AxiomSplitter;
import uk.ac.liv.ontologyutils.loader.OntologyLoader;
import uk.ac.liv.ontologyutils.util.ModulePaths;
import uk.ac.liv.ontologyutils.util.ModuleUtils;

public class SemanticOnlyComparison implements Experiment {


	private File originalLocation;
	private OWLOntology ontology;
	private NewIteratingExperiment starIterExperiment;
	private File sigLocation;
	private SemanticOnlyExtractor semanticExtractor;
	private Set<OWLLogicalAxiom> semanticModule;
	private Set<OWLEntity> refsig;
	static Set<OWLLogicalAxiom> differences = new HashSet<OWLLogicalAxiom>();
	static int total = 0;
	private Stopwatch semanticStopwatch;
	private Stopwatch hybridStopwatch;

	public SemanticOnlyComparison(OWLOntology ont, File originalLocation) {
		this.ontology = ont;
		this.originalLocation = originalLocation;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature) {
		this.refsig = signature;
		starIterExperiment = 
				new NewIteratingExperiment(ontology,originalLocation);

		hybridStopwatch = new Stopwatch().start();
		starIterExperiment.performExperiment(signature);
		hybridStopwatch.stop();

		Set<OWLLogicalAxiom> hybridModule = starIterExperiment.getHybridModule();


		semanticStopwatch = new Stopwatch().start();
		semanticExtractor = new SemanticOnlyExtractor(hybridModule);
		semanticModule = semanticExtractor.extractModule(signature);
		semanticStopwatch.stop();


	}
	
	public Set<OWLLogicalAxiom> get1DepletingModule(){
		return semanticModule;
	}

	@Override
	public void performExperiment(Set<OWLEntity> signature, File signatureLocation) {
		this.sigLocation = signatureLocation;

		performExperiment(signature);

	}

	@Override
	public void writeMetrics(File experimentLocation) throws IOException {

		BufferedWriter writer = new BufferedWriter(new FileWriter(experimentLocation.getAbsoluteFile() + "/" + "experiment-results", false));

		int qbfSmaller = (semanticModule.size() < starIterExperiment.getHybridModule().size()) ? 1 : 0;

		writer.write("StarSize, HybridSize, QBFSize, QBFSmaller, QBFChecks, TimeHybrid, TimeQBF, SignatureLocation" + "\n");
		writer.write(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size() + "," + String.valueOf(qbfSmaller) + "," 
				+ semanticStopwatch.elapsed(TimeUnit.MILLISECONDS) + "," + hybridStopwatch.elapsed(TimeUnit.MILLISECONDS) + 
				"," + semanticExtractor.getQBFCount() + 
				"," + sigLocation.getAbsolutePath() + "\n");

		writer.flush();
		writer.close();

	}

	public void printMetrics() throws IOException{
		System.out.println("============================================================================");
		System.out.println("StarSize, HybridSize, QBFSize");
		System.out.println(starIterExperiment.getStarSize() + "," + starIterExperiment.getIteratedSize() + 
				"," + semanticModule.size());


//		System.out.println("Hybrid time:" + hybridStopwatch.elapsed(TimeUnit.MILLISECONDS));
//		System.out.println("Semantic time:" + semanticStopwatch.elapsed(TimeUnit.MILLISECONDS));
//		System.out.println();
//		//System.out.println("Signature:" + refsig);
//
//		//		System.out.println("Hybrid:");
//		//		for(OWLLogicalAxiom ax : starIterExperiment.getHybridModule()){
//		//			System.out.println(ax);
//		//		}
//		//		System.out.println();
//		//		System.out.println("QBF:");
//		//		for(OWLLogicalAxiom ax : semanticModule){
//		//			System.out.println(ax);
//		//		}
//
//
//		System.out.println("QBF checks: " + semanticExtractor.getQBFCount());
//		Set<OWLEntity> qbfSig = ModuleUtils.getClassAndRoleNamesInSet(semanticModule);
//		qbfSig.addAll(refsig);
//
//
//		Set<OWLLogicalAxiom> difference = new HashSet<OWLLogicalAxiom>(starIterExperiment.getHybridModule());
//		difference.removeAll(semanticModule);
//		differences.addAll(difference);
//		total += difference.size();
//
//		qbfSig.retainAll(ModuleUtils.getClassAndRoleNamesInSet(difference));
//
//		System.out.println("(sig (QBF) ∪ Σ) ∩ sig(Hybrid\\QBF):" + qbfSig);
//
//
//		System.out.println("\nDifference - Hybrid\\QBF:");
//
//		System.out.println("Diff size: " + difference.size());
//
//
//
//		for(OWLLogicalAxiom ax : difference){
//			Set<OWLEntity> sigCopy = new HashSet<OWLEntity>(qbfSig);
//			OWLClass cls = (OWLClass) AxiomSplitter.getNameofAxiom(ax);
//			//System.out.println();
//			//System.out.println(dependencies.get(ax));
//			//	sigCopy.retainAll(dependencies.get(ax));
//			//	System.out.println(qbfSig.contains(cls) + "/"  + !sigCopy.isEmpty());
//			System.out.println(ax);
//		}

	}


	public static void main(String[] args) throws IOException, OWLOntologyCreationException {

//		int role = 0;
//		int size = 100;
//
//		int[] exprs = {171,123,137,91,177,181,8,159,171,196,120};
		
		String[] axioms = {
				"axiom-1414073621",
				"axiom-1422189972",
				"axiom-1461015509",
				"axiom-1465557453",
				"axiom-1468971940",
				"axiom-1479687584",
				"axiom-1484786339",
				"axiom-149618946",
				"axiom-1802718320",
				"axiom-1812357334",
				"axiom-1815612041",
				"axiom-1825135003",
				"axiom-1579863160",
				"axiom-1594998370",
				"axiom-159772046",
				"axiom-1599793239",
				"axiom-1261167737",
				"axiom-1023735277",
				"axiom-1832817413",
				"axiom-1867324164",
				"axiom-1868804400",
				"axiom-1869212959",
				"axiom-1871080873",
				"axiom-1871451059",
				"axiom-1874762578",
				"axiom-1031561180",
				"axiom-1064379383",
				"axiom-1070219288",
				"axiom-1092525734",
				"axiom-1904598090",
				"axiom-1905897672",
				"axiom-1921713217",
				"axiom-1935044395",
				"axiom-1936534385",
				"axiom-1959205614",
				"axiom-1104527295",
				"axiom-1131914610",
				"axiom-1157622664",
				"axiom-1159625075",
				"axiom-2057032076",
				"axiom-2064659139",
				"axiom-1513270934",
				"axiom-1516663721",
				"axiom-1520939075",
				"axiom-128774144",
				"axiom-13172955",
				"axiom-210786719",
				"axiom-1322511641",
				"axiom-1330177472",
				"axiom-1344261461",
				"axiom-1358855361",
				"axiom-1370391263",
				"axiom-1372103623",
				"axiom-1373444105",
				"axiom-1375304143",
				"axiom-373535840",
				"axiom-38914227",
				"axiom-398264497",
				"axiom-1626760845",
				"axiom-1543711731",
				"axiom-1561319469",
				"axiom-1561595647",
				"axiom-156331581",
				"axiom-568889391",
				"axiom-578781597",
				"axiom-582675068",
				"axiom-625572821",
				"axiom-1663480113",
				"axiom-1676156061",
				"axiom-1676645922",
				"axiom-1680649711",
				"axiom-644358292",
				"axiom-647045241",
				"axiom-650323036",
				"axiom-65664904",
				"axiom-675604307",
				"axiom-691180495",
				"axiom-1735423457",
				"axiom-1763791175",
				"axiom-1775090247",
				"axiom-81629434",
				"axiom-817922122",
				"axiom-822503023",
				"axiom-844577476",
				"axiom-855724922",
				"axiom-856376761",
				"axiom-860637653",
				"axiom1041376571",
				"axiom1049318757",
				"axiom1051497373",
				"axiom-1988948537",
				"axiom-2011683730",
				"axiom1084852060",
				"axiom1092173611",
				"axiom1105836064",
				"axiom1129409429",
				"axiom1132675708",
				"axiom1152398717",
				"axiom-2126952935",
				"axiom-213957410",
				"axiom-2142497967",
				"axiom-217600167",
				"axiom-244175003",
				"axiom1272832706",
				"axiom-271373036",
				"axiom-28411544",
				"axiom-298811643",
				"axiom1298052196",
				"axiom1300210724",
				"axiom1324094404",
				"axiom1331013766",
				"axiom1344965130",
				"axiom134545151",
				"axiom1346575844",
				"axiom-441370404",
				"axiom-44948671",
				"axiom-452170811",
				"axiom-463206405",
				"axiom-472827395",
				"axiom-498077259",
				"axiom-499322350",
				"axiom1494274992",
				"axiom1498929871",
				"axiom1366970236",
				"axiom1368377134",
				"axiom1375404830",
				"axiom-701811707",
				"axiom-723340094",
				"axiom1529698142",
				"axiom1564003477",
				"axiom1571235287",
				"axiom-725729059",
				"axiom-738897852",
				"axiom-751751872",
				"axiom-755353826",
				"axiom-76314909",
				"axiom-767558258",
				"axiom-770819287",
				"axiom-771508808",
				"axiom-772086671",
				"axiom-772900149",
				"axiom172007098",
				"axiom1722627033",
				"axiom-896125993",
				"axiom-897094459",
				"axiom-90760248",
				"axiom-917022772",
				"axiom-936765339",
				"axiom-939020019",
				"axiom-940050772",
				"axiom-942984073",
				"axiom-951654912",
				"axiom1783765009",
				"axiom1795843498",
				"axiom-978381171",
				"axiom-986819566",
				"axiom-996632613",
				"axiom1004051788",
				"axiom1012891307",
				"axiom1023171643",
				"axiom1951087565",
				"axiom1863176082",
				"axiom1866434171",
				"axiom1875487085",
				"axiom1978956002",
				"axiom1994496066",
				"axiom1997393517",
				"axiom2004452712",
				"axiom2004590740",
				"axiom2028411004",
				"axiom1201447318",
				"axiom1218253031",
				"axiom262465596",
				"axiom293231749",
				"axiom31977921",
				"axiom466581589",
				"axiom349497086",
				"axiom1444129673",
				"axiom1451512364",
				"axiom1451547373",
				"axiom1469468822",
				"axiom510914029",
				"axiom529452836",
				"axiom533168436",
				"axiom533921469",
				"axiom547873090",
				"axiom552039367",
				"axiom57608589",
				"axiom1577625367",
				"axiom1585747408",
				"axiom1603801833",
				"axiom1610543620",
				"axiom1636006723",
				"axiom663073729",
				"axiom670668225",
				"axiom702835148",
				"axiom721630235",
				"axiom1687501312",
				"axiom1695924101",
				"axiom169760044",
				"axiom1699377433",
				"axiom907252283",
				"axiom909564920",
				"axiom921409716",
				"axiom95348070",
				"axiom964489521",
				"axiom777420984",
				"axiom78581669",
				"axiom1898526901",
				"axiom1899576822",
				"axiom1926234571",
				"axiom819316837",
				"axiom849338897",
				"axiom858883535",
				"axiom2056331267",
				"axiom2064972424",
				"axiom2067507574",
				"axiom2067970144",
				"axiom2109184175",
				"axiom583490041",
				"axiom586026373",
				"axiom605927574",
				"axiom612455445",
				"axiom2143846226",
				"axiom229172185",
				"axiom381438701",
				"axiom389874964",
				"axiom410473660",
				"axiom445668809"
				
		};
//
//		System.out.println("StarSize, HybridSize, QBFSize");
		File ontLoc = new File(ModulePaths.getOntologyLocation() + "/qbf-only/Thesaurus_08.09d.OWL-QBF");
//		OWLOntology nciStar = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "/NCI/Profile/NCI-star.owl");
		OWLOntology ontology = OntologyLoader.loadOntologyAllAxioms(ontLoc.getAbsolutePath());
//		AxiomDependencies dependencies = new AxiomDependencies(nciStar);
//		Set<OWLClass> axiomNames = new HashSet<OWLClass>();
//		Set<OWLClass> classes = ModuleUtils.getClassesInSet(nciStar.getLogicalAxioms());
//		for(OWLLogicalAxiom axiom : nciStar.getLogicalAxioms()){
//			OWLClass nameofAxiom = (OWLClass) AxiomSplitter.getNameofAxiom(axiom);
//			axiomNames.add(nameofAxiom);
//			classes.remove(nameofAxiom);		
//		}
//		
//		System.out.println(nciStar.getLogicalAxiomCount());
//		System.out.println("Top level:" + classes.size());
//		
//		for(OWLLogicalAxiom axiom : nciStar.getLogicalAxioms()){
//			if(Sets.intersection(dependencies.get(axiom),classes).isEmpty()){
//				System.out.println(axiom);
//			}
//		}
		
		
//		int count = 0;
//		for(OWLLogicalAxiom axiom : ontology.getLogicalAxioms()){
//			if(!ModuleUtils.isInclusionOrEquation(axiom)){
//				System.out.println(axiom + ":" + Sets.intersection(axiom.getSignature(), classes).isEmpty());
//				count++;
//				//System.out.println(axiom.getAxiomType());
//			}
//		}
//		System.out.println("Other axioms: " + count);
		
//		
//		
	
			SigManager man = new SigManager(
					new File("/LOCAL/wgatens/Results/ecai/Signatures/qbf-only/AxiomSignatures/Thesaurus_08.09d.OWL-QBF"));
			


			SemanticOnlyComparison compareNCIFull = new SemanticOnlyComparison(ontology, null);

			
			Set<OWLLogicalAxiom> fullModule = compareNCIFull.get1DepletingModule();
			
			for(String siggy : axioms){
				System.out.println(siggy);
				Set<OWLEntity> sig = man.readFile(siggy);
				System.out.println("Class signature?: " + ModuleUtils.isClassSignature(sig));
				compareNCIFull.performExperiment(sig);
				compareNCIFull.printMetrics();

			}

			
		
 		}

//		OWLOntology ont = OntologyLoader.loadOntologyAllAxioms(ModulePaths.getOntologyLocation() + "mantest.owl");
//		ModuleUtils.remapIRIs(ont, "X");
//		OWLDataFactory f = OWLManager.getOWLDataFactory();
//		OWLClass a = f.getOWLClass(IRI.create("X#A"));
//		OWLClass b = f.getOWLClass(IRI.create("X#B"));
//		OWLClass c = f.getOWLClass(IRI.create("X#C"));
//		OWLObjectProperty r = f.getOWLObjectProperty(IRI.create("X#r"));
//
//		System.out.println(ont);
//		SemanticOnlyComparison compare = new SemanticOnlyComparison(ont,null);
//		Set<OWLEntity> signature = new HashSet<OWLEntity>();
//		signature. add(a);
//		signature.add(r);
//		System.out.println("Sig: " + signature);
//		compare.performExperiment(signature);
//		compare.printMetrics();
//
//		System.out.println(compare.get1DepletingModule());


//		/LOCAL/wgatens/Results/womo-new/RandomSignatures/NCI-star.owl/role-0/size-100-SemanticOnlyComparison/random_100_0-178

	
//	axiom-1825135003
//	Class signature?: true
//	============================================================================
//	StarSize, HybridSize, QBFSize
//	172,29,1
//	axiom-1579863160



	//		System.out.println(SemanticOnlyComparison.total);
	//		System.out.println(SemanticOnlyComparison.differences.size());



}
