package org.softlang.qegal.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.jena.ext.com.google.common.collect.Iterables;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.softlang.qegal.IMinedRepository;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.QegalProcess2;
import org.softlang.qegal.io.IOGitBare;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.Gits;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;

public class MavenProcess {

	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		CSVParser records = CSVFormat.DEFAULT.withHeader().parse(new FileReader("data/maven_250.csv"));

		List<String> header = new ArrayList<String>();
		header.addAll(records.getHeaderMap().keySet());
		header.add("http://org.softlang.com/Folder");
		header.add("http://org.softlang.com/File");
		header.add("http://org.softlang.com/groupId");
		header.add("http://org.softlang.com/artifactId");
		header.add("http://org.softlang.com/relativePath");
		header.add("http://org.softlang.com/Pom");
		header.add("http://org.softlang.com/MavenDependency");
		header.add("http://org.softlang.com/Property");
		header.add("http://org.softlang.com/ParentBlock");
		header.add("http://org.softlang.com/ParentPom");
		header.add("http://org.softlang.com/resolvable");
		header.add("http://org.softlang.com/Module");

		CSVPrinter printer = CSVFormat.DEFAULT.withHeader(header.toArray(new String[] {}))
				.print(new File("data/maven_results3_missing.csv"), Charsets.UTF_8);

		List<CSVRecord> repos = new LinkedList<CSVRecord>();

		for (CSVRecord record : repos) {
			// Bare access on GitHub repository.
			Map<String, String> properties = new HashMap<>();
			Repository repository = null;
			try {
				String address = record.get("repo");
				String sha = record.get("hash");
				Git git = Gits.collect(address, true);
				repository = git.getRepository();
				System.out.println(sha);
				IOLayer iolayer = new IOGitBare(repository, sha);

				// Run mining.
				System.out.println("Starting " + address);
				IMinedRepository mined = QegalProcess2.execute(iolayer,
						new File(JUtils.configuration("temp") + "/mined_alpha_repositories/" + address),
						Collections.singleton(new File("src/main/java/org/softlang/qegal/modules/testmaven")),
						1000 * 60 * 10, QegalLogging.EXCEPTIONS, true);

				properties.putAll(mined.properties());
				properties.putAll(record.toMap());

				System.out.println("Ready " + address);
				System.out.println("Properties " + address + " " + properties);
			} catch (Exception e) {
				System.err.println(e);
				properties.put("exception_in_process", e.toString());
			} finally {
				if (repository != null)
					repository.close();

				printer.printRecord(Iterables.transform(header, x -> properties.getOrDefault(x, "NaN")));
				printer.flush();
			}
		}

		printer.close();
		records.close();
	}

}
