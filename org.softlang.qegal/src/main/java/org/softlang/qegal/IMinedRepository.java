package org.softlang.qegal;

import java.io.File;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.softlang.qegal.io.IOLayer;

public interface IMinedRepository {
	
	Model model();
	
	IOLayer iolayer();
	
	Map<String,String> properties();
	
	File log();

}
