package org.softlang.qegal.qmodles.model1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.softlang.qegal.IMinedRepository;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.QegalProcess2;
import org.softlang.qegal.io.IOFilesystem;
import org.softlang.qegal.io.IOGitBare;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.CSVSink;
import org.softlang.qegal.jutils.Gits;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.jutils.CSVSink.SinkType;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;

public class WildQueryPos {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		File projectsCSV = new File("data/qmodles/model1/wild/scope1.csv");
		String ttlout = "data/qmodles/model1/wild/ttls/";

		//detect(projectsCSV.getAbsolutePath(), ttlout, 10, false);
		statistics(new File(ttlout));
	}

	public static void detect(String projectsCSV, String ttlout, int n, boolean bare)
			throws FileNotFoundException, IOException {

		CSVParser records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(projectsCSV));

		CSVSink csvsink = new CSVSink(new File("data/qmodles/model1/wild/out.csv").getAbsolutePath(),
				Charsets.UTF_8, SinkType.DYNAMIC);

		// Delete the ttl outs.
		File fttlout = new File(ttlout);
		if(fttlout.exists())
			for (File file : new File(ttlout).listFiles(f -> f.getAbsolutePath().endsWith(".ttl")))
				file.delete();
		else
			FileUtils.forceMkdir(fttlout);
		
		int i = 0;
		for (CSVRecord record : records) {
			if(i==n)
				break;
			Repository repository = null;
			IOLayer iolayer = null;
			Map<String, String> properties = new HashMap<>();
			try {
				String address = record.get("repository");
				System.out.println("Starting " + address);
				String sha = record.get("sha");
				if (bare) {
					// Initialise bare repository.
					Git git = Gits.collect(address, true);
					repository = git.getRepository();
					iolayer = new IOGitBare(repository, sha);
				} else {
					Git git = Gits.collect(address, false);
					repository = git.getRepository();
					git.checkout().setName(sha).call();
					iolayer = new IOFilesystem(repository.getWorkTree());
				}

				// Run mining.
				IMinedRepository mined = QegalProcess2.execute(iolayer,
						new File(JUtils.configuration("temp") + "/qmodles/" + address),
						Collections.singleton(new File("src/main/java/org/softlang/qegal/qmodles/model1")),
						1000 * 60 * 60 * 6, QegalLogging.NONE, false);

				QegalUtils.write(mined.model(), new File(ttlout + address.replace("/", "#") + ".ttl"));

				properties.putAll(mined.properties());
				properties.putAll(record.toMap());

				System.out.println("Mined: " + mined.properties());
			} catch (Exception e) {
				System.err.println("Exception: " + e.toString());
				properties.put("exception_in_process", e.toString());
			} finally {
				if (repository != null)
					repository.close();
				csvsink.write(properties);
				csvsink.flush();
			}
			i++;
		}
		records.close();
	}
	
	public static void statistics(File ttlout) {
		CSVSink csvsink = new CSVSink(ttlout.getAbsolutePath()+"/stats.csv",
				Charsets.UTF_8, SinkType.DYNAMIC);
		for (File file : ttlout.listFiles(f -> f.getAbsolutePath().endsWith(".ttl"))) {
			Model model = RDFDataMgr.loadModel(file.getAbsolutePath());
			Map<String,String> properties = new HashMap<>();
			Set<QuerySolution> results = QegalUtils.query(model, countInstances());
			results.forEach(r -> properties.put(r.get("?t").toString(),""+r.get("?n").asLiteral().getInt()));
			properties.put("Repository", file.getName().replace("#", "/").replace(".ttl",""));
			csvsink.write(properties);
			csvsink.flush();
		}
		csvsink.close();
	}
	
	public static String countInstances() {
		String query = "PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT ?t (COUNT(?x) as ?n) WHERE {"
				+ "	?x sl:instanceOf ?t . "
				+ "} GROUP BY ?t";
		return query;
	}
}
