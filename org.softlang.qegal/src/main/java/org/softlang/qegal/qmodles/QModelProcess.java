package org.softlang.qegal.qmodles;

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

public class QModelProcess {
	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		// Input arguments.
		boolean bare = false;

		CSVParser records = CSVFormat.DEFAULT.withHeader()
				.parse(new FileReader("src/main/java/org/softlang/qegal/qmodles/target.csv"));

		CSVSink csvsink = new CSVSink(
				new File("src/main/java/org/softlang/qegal/qmodles/out.csv").getAbsolutePath(),
				Charsets.UTF_8, SinkType.DYNAMIC);

		for (CSVRecord record : records) {
		
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
						Collections.singleton(new File("src/main/java/org/softlang/qegal/qmodles/process")), 1000 * 60 * 10,
						QegalLogging.EXCEPTIONS, true);

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
		}
		records.close();
	}
}
