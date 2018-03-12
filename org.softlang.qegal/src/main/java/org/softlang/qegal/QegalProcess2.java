package org.softlang.qegal;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;

import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.rulesys.Builtin;
import org.apache.jena.vocabulary.RDFS;
import org.softlang.qegal.buildins.QegalBuiltin;
import org.softlang.qegal.engine.Miner;
import org.softlang.qegal.engine.QegalFRuleEngineIFactory;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.CSVSink;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.jutils.CSVSink.SinkType;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;

public class QegalProcess2 {

	public static String ANALYSIS_EVALUATION = "http://org.softlang.com/qegal/Evaluation";

	/**
	 * Load a previously mined repository from a location.
	 */
	public static IMinedRepository load(IOLayer iolayer, File source) {
		throw new RuntimeException("not implemented");
	}

	/**
	 * Run the mining process. Returns a version with model and properties in
	 * memory. Logging file are stored in a temporary location.
	 * 
	 * @param iolayer
	 *            corresponding repository access.
	 * @return
	 */
	public static IMinedRepository execute(IOLayer iolayer, File target, Set<File> modules, long timeoutms,
			QegalLogging logging, boolean doPresistence) {
		// Mined Artifacts.
		long startTime = System.currentTimeMillis();
		File loggingFile = new File(target, "log.csv");
		final Map<String, String> properties = new HashMap<String, String>();
		final CSVSink loggingSink = new CSVSink(loggingFile.getAbsolutePath(), Charsets.UTF_8, SinkType.HEADER_FILE);
		Model model = null;

		// Running the mining task with timeout.
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<Model> future = executor.submit(new Callable<Model>() {
			@Override
			public Model call() throws Exception {
				// Register logging at Jena RETE engine.
				QegalFRuleEngineIFactory.register(loggingSink, Collections.emptyMap(), logging);

				// Initialize layered miner.
				Miner miner = new Miner() {
					@Override
					protected void initialize(Builtin builtin) {
						// Add context to qegal builtings.
						if (builtin instanceof QegalBuiltin) {
							((QegalBuiltin) builtin).setDebug(loggingSink);
							((QegalBuiltin) builtin).setIolayer(iolayer);
						}
					}
				};

				// Adding all modules.
				for (File module : modules)
					miner.addFolder(module);

				// Execute the forward mining process.
				Model model = miner.execute();

				// Count and append the process properties. TODO: This has to be renamed.
				for (Entry<String, Integer> x : QegalUtils.countPredicate(model, ANALYSIS_EVALUATION, null, null)
						.entrySet())
					properties.put(x.getKey(), String.valueOf(x.getValue()));
				// Save model.
				if (doPresistence)
					QegalUtils.write(model, new File(target, "model.ttl"));
				return model;
			}

		});

		try {
			model = future.get(timeoutms, TimeUnit.MILLISECONDS);
			properties.put("state", "done");
		} catch (TimeoutException e) {
			future.cancel(true);
			properties.put("state", "timeout");
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
			properties.put("state", "exception");
		}

		executor.shutdownNow();
		long endTime = System.currentTimeMillis();

		properties.put("time", String.valueOf(endTime - startTime));

		loggingSink.close();

		// Save properties and close logging.
		Properties prop = new Properties();
		prop.putAll(properties);
		try {
			Files.createParentDirs(new File(target, "mining.properties"));
			prop.store(new FileWriter(new File(target, "mining.properties")), "Results of this mining process");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new MinedRepositoryInMemory(model, iolayer, properties, loggingFile);
	}

}
