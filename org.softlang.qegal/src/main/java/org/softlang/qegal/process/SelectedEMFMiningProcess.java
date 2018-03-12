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

public class SelectedEMFMiningProcess {
	/**
	 * Mines all path as input and output.
	 * 
	 * @param args
	 * @throws MissingObjectException
	 * @throws IncorrectObjectTypeException
	 * @throws IOException
	 */
	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {

		for (int i = 0; i < args.length / 2; i++) {
			String input = args[i * 2];
			String output = args[i * 2 + 1];

			System.out.println("Input " + input);
			System.out.println("Output " + output);

			Map<String, String> properties = new HashMap<>();
			IOLayer iolayer = new IOFilesystem(new File(input));

			System.out.println("IO connected");
			
			// Run mining.
			IMinedRepository mined = QegalProcess2.execute(iolayer, new File(output),
					Collections.singleton(new File("src/main/java/org/softlang/qegal/modules/emf")), 1000 * 60 * 10,
					QegalLogging.ALL, true);

			properties.putAll(mined.properties());

			System.out.println("Mined: " + properties);
		}
	}
}
