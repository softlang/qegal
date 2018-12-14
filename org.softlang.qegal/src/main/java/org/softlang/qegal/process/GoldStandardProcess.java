package org.softlang.qegal.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.apache.maven.model.building.ModelBuildingException;
import org.apache.maven.model.building.ModelSource;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.softlang.qegal.IMinedRepository;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.QegalProcess2;
import org.softlang.qegal.io.IOFilesystem;
import org.softlang.qegal.io.IOGitBare;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.Gits;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.process.maven.DefaultModelResolver;
import org.softlang.qegal.process.maven.GitModelSource;
import org.softlang.qegal.process.regex.Dependency;
import org.softlang.qegal.process.regex.POM;
import org.softlang.qegal.process.regex.RegexMiner;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;

public class GoldStandardProcess {

	public static void main(String[] args)
			throws MissingObjectException, IncorrectObjectTypeException, IOException, ModelBuildingException {
		String input = "temp/input2";
		

		Git git = Gits.collect("libgdx/libgdx", true);
		Repository repository = git.getRepository();
		IOLayer iolayer =  new IOGitBare(repository, "3e071eb3ba1ca39cc3f2538598b72bd61572aa67");// new IOFilesystem(new File(input));

		DefaultModelResolver dmr = new DefaultModelResolver();

		final Set<String> context = new HashSet<>();
		final Map<String, String> contextMap = new HashMap<>();

		GitModelSource gms = new GitModelSource() {
			@Override
			public ModelSource accessGit(String objectid) {
				return new ModelSource() {
					@Override
					public InputStream getInputStream() throws IOException {
						return iolayer.access("repository:/" + objectid);
					}

					@Override
					public String getLocation() {
						return objectid;
					}
				};
			}
		};

		for (String path : tree(iolayer.root(), iolayer))
			if (path.endsWith("pom.xml")) {
				String witoutRepository = path.replace("repository:/", "");
				context.add(witoutRepository);
				contextMap.put(witoutRepository, witoutRepository);
			}

		for (String maven : context) {
			System.out.println(maven);
			Optional<org.apache.maven.model.Model> m = dmr.resolveModel(gms, maven, maven, contextMap);

			if (m.isPresent())
				System.out.println(m.get().getDependencies());
		}

	}

	public static Set<String> tree(String path, IOLayer iolayser) {
		Set<String> set = new HashSet<>();

		if (iolayser.isDirectory(path)) {
			for (String child : iolayser.children(path))
				set.addAll(tree(child, iolayser));
		} else
			set.add(path);

		return set;
	}

}
