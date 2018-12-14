package org.softlang.qegal.process;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.softlang.qegal.IMinedRepository;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.QegalProcess2;
import org.softlang.qegal.io.IOFilesystem;
import org.softlang.qegal.io.IOLayer;

public class TestProcess {

	public static void main(String[] args) throws MissingObjectException, IncorrectObjectTypeException, IOException {
		String input = "temp/input";
		String output = "temp/output";

		System.out.println("Input " + input);
		System.out.println("Output " + output);

		Map<String, String> properties = new HashMap<>();
		IOLayer iolayer = new IOFilesystem(new File(input));

		System.out.println("IO connected");

		// Run mining.
		IMinedRepository mined = QegalProcess2.execute(iolayer, new File(output),
				Collections.singleton(new File("src/main/java/org/softlang/qegal/modules/testmaven")), 1000 * 60 * 10,
				QegalLogging.ALL, true);

		properties.putAll(mined.properties());

		System.out.println("Mined: " + properties);

	}
}
