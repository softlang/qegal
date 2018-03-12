package org.softlang.qegal.process;

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

public class EMFMiningProcess {
	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		// Input arguments.
		boolean bare = true;
		boolean sample = false;
		boolean persist = false;
		boolean logon = false;
		
		if (args.length > 0)
			bare = Boolean.valueOf(args[0]);
		if (args.length > 1)
			sample = Boolean.valueOf(args[1]);
		if (args.length > 2)
			persist = Boolean.valueOf(args[2]);
		if (args.length > 3)
			logon = Boolean.valueOf(args[3]);

		CSVParser records = sample
				? CSVFormat.DEFAULT.withHeader().parse(new FileReader("data/repository_vanilla_sample.csv"))
				: CSVFormat.DEFAULT.withHeader().parse(new FileReader("data/repository_vanilla.csv"));

		CSVSink csvsink = sample
				? new CSVSink(new File("data/repository_emf_sample.csv").getAbsolutePath(), Charsets.UTF_8, SinkType.DYNAMIC)
				: new CSVSink(new File("data/repository_emf.csv").getAbsolutePath(), Charsets.UTF_8, SinkType.DYNAMIC);

		
		System.out.println("Bare: " + String.valueOf(bare));
		System.out.println("Sample: " + String.valueOf(sample));
		System.out.println("Persist " + String.valueOf(persist));
				
		for (CSVRecord record : records) {
			// Bare access on GitHub repository.
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

				System.out.println("IO connected");
				// Run mining.

				IMinedRepository mined = QegalProcess2.execute(iolayer,
						new File(JUtils.configuration("temp") + "/mined_emf_repositories/" + address),
						Collections.singleton(new File("src/main/java/org/softlang/qegal/modules/emf")), 1000 * 60 * 10,
						logon? QegalLogging.ALL : QegalLogging.EXCEPTIONS, persist);

				properties.putAll(mined.properties());
				properties.putAll(record.toMap());

				System.out.println("Mined: " + properties);
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

		csvsink.close();
		records.close();
	}
}
