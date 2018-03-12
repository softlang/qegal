package org.softlang.qegal;

import java.io.File;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.softlang.qegal.io.IOLayer;


public class MinedRepositoryInMemory implements IMinedRepository {

	final Model model;
	final IOLayer iolayer;
	final Map<String, String> properties;
	final File log;

	public MinedRepositoryInMemory(Model model, IOLayer iolayer, Map<String, String> properties, File log) {
		this.model = model;
		this.iolayer = iolayer;
		this.properties = properties;
		this.log = log;
	}

	@Override
	public Model model() {
		return model;
	}

	@Override
	public IOLayer iolayer() {
		return iolayer;
	}

	@Override
	public Map<String, String> properties() {
		return properties;
	}

	@Override
	public File log() {
		return log;
	}

}
