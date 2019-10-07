package org.softlang.qegal.qmodles;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.softlang.qegal.IMinedRepository;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.QegalProcess2;
import org.softlang.qegal.io.IOFilesystem;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.CSVSink;
import org.softlang.qegal.jutils.CSVSink.SinkType;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;

public class QModelProcessLocal {


	/**
	 * Preceding Steps: 
	 * 1. Download demo project files from
	 * "http://www.informit.com/content/images/9780321331885/downloads/examples.zip"
	 * 2. Create new EMF projects based on the models.
	 * 3. Scope: Create a CSV file that consists of all paths to relevant demo projects.
	 * 
	 * @param datapath: This folder contains all relevant data.
	 * @param localProjectList: The CSV file contains the paths to projects.
	 * @param qegalDir: This folder contains all .qegal files that shall be used.
	 * 
	 * @return This procedure executes the inference rules from qegalDir on each project linked by localProjectList
	 * and creates the respective .ttl files in the datapath.
	 */
	public static void detection(String datapath, File localProjectList, File qegalDir) {
		String ttlout = datapath + "ttls/";

		CSVSink csvsink = new CSVSink(new File("src/main/java/org/softlang/qegal/qmodles/out.csv").getAbsolutePath(),
				Charsets.UTF_8, SinkType.DYNAMIC);

		
		
		
		Map<String, String> properties = new HashMap<>();
		try {
			// Delet the ttl outs.
			if(new File(ttlout).exists())
				for (File file : new File(ttlout).listFiles())
					file.delete();
			else
				FileUtils.forceMkdir(new File(ttlout));
			
			// Run inference
			for (String inLocal : Files.readAllLines(localProjectList.toPath())) {
				File projectDir = new File(inLocal);
				IOLayer iolayer = new IOFilesystem(projectDir);

				// Run mining.
				IMinedRepository mined = QegalProcess2.execute(iolayer,
						new File(JUtils.configuration("temp") + "/qmodles/" + projectDir.getName()),
						Collections.singleton(qegalDir), 1000 * 60 * 60 * 6, QegalLogging.NONE, false);

				QegalUtils.write(mined.model(), new File(ttlout + projectDir.getName() + ".ttl"));

				properties.putAll(mined.properties());

				System.out.println("Mined: " + mined.properties());
			}
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			properties.put("exception_in_process", e.toString());
			e.printStackTrace();
		} finally {
			csvsink.write(properties);
			csvsink.flush();
		}

	}

	public static void createStatistics(String stagepath) {
		File stage0 = new File("data/qmodles/stage0/ttls");

		File out = new File("data/qmodles/stage0/statistics.csv");

		// prep columns
		String[] relations = { "ModelGenerator", "ModelPackageGenerator", "ModelClassGenerator",
				"ModelFeatureGenerator", "GeneratorToEClassInterfaceJava" };
		String[] partname = { "?ePackage", "?eFactory", "?eClassInterface", "?eClassImpl", "?feature", "?featureImpl",
				"?ecoreXMI", "?ePackageXMI", "?eClassXMI", "?eStructuralFeatureXMI", "?generatorXMI", "?genmodelXMI",
				"?genPackageXMI", "?genClassXMI", "?genFeatureXMI", "?genOperationXMI" };
		String[] languages = { "EcorePackageJava", "EcoreFactoryJava", "EClassInterfaceJava", "EClassImplJava",
				"EAttributeInterfaceJava", "EAttributeImplJava", "EcoreXMI", "PackageXMI", "ClassXMI",
				"StructuralFeatureXMI", "GeneratorXMI", "GenmodelXMI", "GenPackageXMI", "GenClassXMI", "GenFeatureXMI",
				"GenOperationXMI" };
		String[] overestimates = { "Java", "Java", "Java", "Java", "MethodJava", "MethodJava", "XMI", "XMIElement",
				"XMIElement", "XMIElement", "XMI", "XMIElement", "XMIElement", "XMIElement", "XMIElement",
				"XMIElement" };

		String[] columnnames = ArrayUtils.addAll(languages, relations);

		try {
			FileWriter fw = new FileWriter(out, false);
			fw.write("repo," + StringUtils.join(columnnames, ',') + "\n");

			for (File file : stage0.listFiles(f -> f.getAbsolutePath().endsWith(".ttl"))) {
				// read RDF graph
				Model model = RDFDataMgr.loadModel(file.getAbsolutePath());

				// count
				int[] counts = new int[columnnames.length];
				int[] ocounts = new int[overestimates.length];
				for (int i = 0; i < languages.length; i++) {
					counts[i] = countLanguage(model, languages[i]);
					ocounts[i] = countLanguage(model, overestimates[i]);
				}
				for (int i = 0; i < relations.length; i++) {
					counts[languages.length + i] = countRelation(model, relations[i]);
				}
				// fw.write(file.getName() + "," + StringUtils.join(counts, ',') + "\n");
				fw.write(file.getName());
				for (int i = 0; i < languages.length; i++) {
					fw.write("," + counts[i] + "/" + ocounts[i]);
				}
				for (int i = languages.length; i < counts.length; i++) {
					fw.write("," + counts[i]);
				}
				fw.write("\n");

			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static int countLanguage(Model model, String language) {
		// execute queries
		String qtext = "PREFIX sl: <http://org.softlang.com/> \n" + "SELECT DISTINCT ?x WHERE { ?x sl:elementOf sl:"
				+ language + " . }";
		Query query = QueryFactory.create(qtext, Syntax.syntaxARQ);

		try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qe.execSelect();
			int count = 0;
			while (results.hasNext()) {
				results.next();
				count++;
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	public static int countRelation(Model model, String relation) {
		// execute queries
		String qtext = "PREFIX sl: <http://org.softlang.com/> \n" + "SELECT DISTINCT ?x ?y WHERE { ?x sl:" + relation
				+ " ?y . }";
		Query query = QueryFactory.create(qtext, Syntax.syntaxARQ);
		try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
			ResultSet results = qe.execSelect();
			int count = 0;
			while (results.hasNext()) {
				results.next();
				count++;
			}
			return count;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}
}
