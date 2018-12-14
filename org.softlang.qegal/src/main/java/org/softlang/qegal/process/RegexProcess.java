package org.softlang.qegal.process;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
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
import org.softlang.qegal.io.IOFilesystem;
import org.softlang.qegal.io.IOGitBare;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.Gits;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.process.regex.Dependency;
import org.softlang.qegal.process.regex.POM;
import org.softlang.qegal.process.regex.RegexMiner;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Table;

public class RegexProcess {

	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		String input = "temp/input2";
		IOLayer iolayer = new IOFilesystem(new File(input));
		RegexMiner miner = new RegexMiner(iolayer);
		List<POM> results = miner.mine();
		List<Dependency> dependencies = new ArrayList<>();
		for(POM p : results) {
			for(Dependency d : p.getDependencies()) {
				if(!dependencies.contains(d)) {
					dependencies.add(d);
				}
			}
		}
		
		FileOutputStream fos = new FileOutputStream("temp/output/t.tmp");
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(dependencies);
		oos.close();

	}

}
