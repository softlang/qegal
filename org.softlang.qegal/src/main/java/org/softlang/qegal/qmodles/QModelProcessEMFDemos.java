package org.softlang.qegal.qmodles;

import static org.softlang.qegal.utils.QegalUtils.query;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

public class QModelProcessEMFDemos {

	public static void main(String[] args) throws IOException {

		String stagepath = "data/qmodles/stage0/";
		
		download();
		//detection(stagepath);
		//createStatistics(stagepath);
		
		//createTestProtocol(stagepath);
	}
	
	public static void download() {
		URL website;
		try {
			website = new URL("http://www.informit.com/content/images/9780321331885/downloads/examples.zip");
			File f = new File(JUtils.configuration("temp")+"/demos.zip");
			FileUtils.copyURLToFile(website, f);
			
			//unzip
	        File destDir = new File(JUtils.configuration("temp"));
	        byte[] buffer = new byte[1024];
	        ZipInputStream zis = new ZipInputStream(new FileInputStream(f));
	        ZipEntry zipEntry = zis.getNextEntry();
	        while (zipEntry != null) {
	        	File newFile = new File(destDir, zipEntry.getName());
	        	newFile.getParentFile().mkdirs();
	        	boolean exists = newFile.createNewFile();
	        	newFile.setWritable(true);
	            String destDirPath = destDir.getCanonicalPath();
	            String destFilePath = newFile.getCanonicalPath();
	             
	            if (!destFilePath.startsWith(destDirPath + File.separator)) {
	                throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
	            }
	            FileOutputStream fos = new FileOutputStream(newFile);
	            int len;
	            while ((len = zis.read(buffer)) > 0) {
	                fos.write(buffer, 0, len);
	            }
	            fos.close();
	            zipEntry = zis.getNextEntry();
	        }
	        zis.closeEntry();
	        zis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void detection(String stagepath) throws IOException {
		String ttlout = stagepath + "ttls/";

		// list of local projects
		File f = new File(stagepath+"in_local.csv");

		CSVSink csvsink = new CSVSink(new File("src/main/java/org/softlang/qegal/qmodles/out.csv").getAbsolutePath(),
				Charsets.UTF_8, SinkType.DYNAMIC);

		// Delet the ttl outs.
		System.out.println(ttlout);
		for (File file : new File(ttlout).listFiles())
			file.delete();

		for (String inLocal : Files.readAllLines(f.toPath())) {
			File projectDir = new File(inLocal);
			IOLayer iolayer = new IOFilesystem(projectDir);

			Map<String, String> properties = new HashMap<>();
			try {
				// Run mining.
				IMinedRepository mined = QegalProcess2.execute(iolayer,
						new File(JUtils.configuration("temp") + "/qmodles/" + projectDir.getName()),
						Collections.singleton(new File("src/main/java/org/softlang/qegal/qmodles/stage0")),
						1000 * 60 * 60 * 6, QegalLogging.NONE, false);

				QegalUtils.write(mined.model(), new File(ttlout + projectDir.getName() + ".ttl"));

				properties.putAll(mined.properties());

				System.out.println("Mined: " + mined.properties());
			} catch (Exception e) {
				System.err.println("Exception: " + e.toString());
				properties.put("exception_in_process", e.toString());
			} finally {
				csvsink.write(properties);
				csvsink.flush();
			}
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
				
				//count
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
	
	public static void createTestProtocol(String stagepath) {
		String ttlout = stagepath + "ttls/";
		
		File stage0 = new File(ttlout);
		
		Map<String, String[]> options = new HashMap<>();
		String[] subsetJava = {"EcorePackageJava","EcoreFactoryJava","EClassInterfaceJava","EClassImplJava"}; 
		options.put("Java", subsetJava);
		String[] subsetMethodJava = {"EAttributeInterfaceJava", "EAttributeImplJava"};
		options.put("MethodJava", subsetMethodJava);
		String[] subsetXMI = {"EcoreXMI", "GeneratorXMI"};
		options.put("XMI", subsetXMI);
		String[] subsetXMIElement = {"PackageXMI", "ClassXMI","StructuralFeatureXMI"
				,"GenmodelXMI", "GenPackageXMI", "GenClassXMI", "GenFeatureXMI","GenOperationXMI"};
		options.put("XMIElement", subsetXMIElement);
		
		for (File file : stage0.listFiles(f -> f.getAbsolutePath().endsWith(".ttl"))) {
			File out = new File(stagepath+ "Testprotocol-"+file.getName()+".csv");
			try {
				FileWriter fw = new FileWriter(out, false);
				Model model = RDFDataMgr.loadModel(file.getAbsolutePath());
				
				for(String overlang : options.keySet()) {
					String querytext = "PREFIX sl: <http://org.softlang.com/> \n" + "SELECT DISTINCT ?x WHERE { ?x sl:elementOf" + " sl:"+ overlang +". }";
					Set<String> elements = query(model,querytext);
					for(String el : elements) {
						fw.write(el + ", " + String.join(",", options.get(overlang)) +", " +overlang +"\n");
					}
				}
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
