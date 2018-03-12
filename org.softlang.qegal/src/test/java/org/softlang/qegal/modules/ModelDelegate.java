package org.softlang.qegal.modules;

import org.apache.jena.rdf.model.Model;

public class ModelDelegate implements IModel {

	final Model model;

	public ModelDelegate(Model model) {
		this.model = model;
	}

	@Override
	public Model delegate() {
		return model;
	}

}
