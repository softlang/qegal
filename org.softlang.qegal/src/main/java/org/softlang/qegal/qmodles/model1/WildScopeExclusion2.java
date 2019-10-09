package org.softlang.qegal.qmodles.model1;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.Repository;
import org.softlang.qegal.IMinedRepository;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.QegalProcess2;
import org.softlang.qegal.io.IOFilesystem;
import org.softlang.qegal.io.IOGitBare;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.CSVSink;
import org.softlang.qegal.jutils.CSVSink.SinkType;
import org.softlang.qegal.jutils.Gits;
import org.softlang.qegal.jutils.JUtils;

import com.google.common.base.Charsets;

public class WildScopeExclusion2 {
	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		// Input arguments.
		boolean bare = false;

		CSVParser records = CSVFormat.DEFAULT.withHeader().parse(new FileReader("data/qmodles/model1/wild/scope0.csv"));

		CSVSink csvsink = new CSVSink(new File("data/qmodles/model1/wild/scope1.csv").getAbsolutePath(), Charsets.UTF_8,
				SinkType.DYNAMIC);

		records.getRecords().parallelStream().map(r -> mineProperties(r, bare)).filter(p -> isIncluded(p)).sequential()
				.forEach(csvsink::write);
		csvsink.flush();

		records.close();
	}

	public static Map<String, String> mineProperties(CSVRecord record, boolean bare) {
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
					Collections.singleton(new File("src/main/java/org/softlang/qegal/qmodles/exclusion")),
					1000 * 60 * 10, QegalLogging.EXCEPTIONS, true);

			properties.putAll(mined.properties());
			properties.putAll(record.toMap());

			System.out.println("Mined: " + mined.properties());
		} catch (Exception e) {
			System.err.println("Exception: " + e.toString());
			properties.put("exception_in_process", e.toString());
		} finally {
			if (repository != null)
				repository.close();
		}
		return properties;
	}

	public static boolean isIncluded(Map<String, String> properties) {
		String[] exclude = { "http://org.softlang.com/ATL", "http://org.softlang.com/Xtext",
				"http://org.softlang.com/QVTo", "http://org.softlang.com/Emfatic", "http://org.softlang.com/Acceleo",
				"http://org.softlang.com/EGL", "http://org.softlang.com/IncQuery", "http://org.softlang.com/Eugenia",
				"http://org.softlang.com/GMF", "http://org.softlang.com/EOL", "http://org.softlang.com/ETL",
				"http://org.softlang.com/EVL", "http://org.softlang.com/OCL", "http://org.softlang.com/Sirius",
				"http://org.softlang.com/Henshin", "http://org.softlang.com/MOFScript",
				"http://org.softlang.com/Kermeta", "http://org.softlang.com/Xcore", "http://org.softlang.com/JET",
				"http://org.softlang.com/EMFText", "http://org.softlang.com/Xpand" };
		for (String ex : exclude) {
			if (properties.containsKey(ex))
				return false;
		}
		String[] include = {"http://org.softlang.com/Ecore","http://org.softlang.com/GeneratorModel"};
		for (String in : include) {
			if (!properties.containsKey(in))
				return false;
		}
		return true;
	}
}
