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

public class RunOnSample {

	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		File output = new File("src/main/java/org/softlang/qegal/qmodles/output");

		Map<String, String> properties = new HashMap<>();
		IOLayer iolayer = new IOFilesystem(new File("C:/Data/Workspaces/qegal_models/org.softlang.sampleemap"));

		System.out.println("IO connected");

		// Run mining.
		IMinedRepository mined = QegalProcess2.execute(iolayer, output,
				Collections.singleton(new File("src/main/java/org/softlang/qegal/qmodles/process")), 1000 * 60 * 10,
				QegalLogging.ALL, true);

		properties.putAll(mined.properties());

		System.out.println("Mined: " + properties);

	}
}
