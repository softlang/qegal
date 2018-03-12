package org.softlang.qegal.process;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
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

public class AlphaMiningProcess {
	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		CSVParser records = CSVFormat.DEFAULT.withHeader().parse(new FileReader("data/repository_raw.csv"));

		List<String> header = new ArrayList<String>();
		header.addAll(records.getHeaderMap().keySet());
		header.add("components");
		header.add("duplicates");
		header.add("time");
		header.add("state");
		header.add("http://org.softlang.com/Folder");
		header.add("http://org.softlang.com/File");
		header.add("http://org.softlang.com/Manifest");
		header.add("http://org.softlang.com/Pom");
		header.add("http://org.softlang.com/Gradle");
		header.add("http://org.softlang.com/Ant");

		CSVPrinter printer = CSVFormat.DEFAULT.withHeader(header.toArray(new String[] {}))
				.print(new File("data/repository_alpha.csv"), Charsets.UTF_8);

		for (CSVRecord record : records) {
			// Bare access on GitHub repository.
			Map<String, String> properties = new HashMap<>();
			Repository repository = null;
			try {
				String address = record.get("repository");
				String sha = record.get("sha");
				Git git = Gits.collect(address, true);
				repository = git.getRepository();
				IOLayer iolayer = new IOGitBare(repository, sha);

				// Run mining.
				System.out.println("Starting " + address);
				IMinedRepository mined = QegalProcess2.execute(iolayer,
						new File(JUtils.configuration("temp") + "/mined_alpha_repositories/" + address),
						Collections.singleton(new File("src/main/java/org/softlang/qegal/modules/alpha")),
						1000 * 60 * 10, QegalLogging.EXCEPTIONS, false);

				// Duplicate declaration and component detection.
				Model model = mined.model();
				if (model != null) {
					HashMultimap<RDFNode, Resource> declarations = HashMultimap.create();
					for (Table.Cell<Resource, Property, RDFNode> cell : QegalUtils
							.statements(model, null, QegalUtils.predicate(model, "decOccurs"), null).cellSet())
						declarations.put(cell.getValue(), cell.getRowKey());

					Set<RDFNode> duplicatedDeclarations = declarations.asMap().entrySet().stream()
							.filter(x -> x.getValue().size() > 1).map(x -> x.getKey()).collect(Collectors.toSet());
					Set<RDFNode> nonDuplicatedDeclarations = declarations.asMap().entrySet().stream()
							.filter(x -> x.getValue().size() == 1).map(x -> x.getKey()).collect(Collectors.toSet());

					// Declaration component detection.
					Set<Set<RDFNode>> components = QegalUtils.connectedComponents(model, nonDuplicatedDeclarations,
							model.getProperty(QegalUtils.DEPENDS_ON));

					properties.put("components", String.valueOf(components.size()));
					properties.put("duplicates", String.valueOf(duplicatedDeclarations.size()));
				}
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
