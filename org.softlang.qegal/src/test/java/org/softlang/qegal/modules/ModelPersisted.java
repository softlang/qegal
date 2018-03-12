package org.softlang.qegal.modules;

import java.io.File;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;

public class ModelPersisted implements IModel {

	private final String location;

	public ModelPersisted(String location) {
		this.location = location;
	}

	@Override
	public Model delegate() {
		Model model = ModelFactory.createDefaultModel();

		model.read(new File(location).toURI().toString(),"N3");
		return model;
	}

}
