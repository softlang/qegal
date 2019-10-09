package org.softlang.qegal.qmodles.model2;

import static org.junit.Assert.*;

import java.io.File;

import org.apache.cxf.helpers.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Before;
import org.junit.Test;

public class DemoAssertion {

	String SLPREFIX = "http://org.softlang.com/";
	Model model;
	
	@Before
	public void setUp(){
		File file = new File("data/qmodles/model2/demo/ttls/PrimerPO.ttl");
		model = RDFDataMgr.loadModel(file.getAbsolutePath());
	}

	@Test
	public void testAssertPos() throws Exception {
		File f = new File("src/main/java/org/softlang/qegal/qmodles/model2/demoassertion+.txt");

		for (String line : FileUtils.readLines(f)) {
			String[] parts = line.trim().split(" ");
			String subj = parts[0];
			String pred = parts[1].replace("sl:", SLPREFIX);
			String obj = parts[2];
			if (obj.contains("sl:"))
				obj = obj.replace("sl:", SLPREFIX);
			Resource subr = model.getResource(subj);
			Property prop = model.getProperty(pred);
			Resource objr = model.getResource(obj);
			
			assertTrue(subr.toString(),model.containsResource(subr));
			assertTrue(objr.toString(),model.containsResource(objr));
			assertTrue(subj +" "+pred+" " +obj,model.contains(subr, prop, objr));
		}
	}
	
	@Test
	public void testAssertNeg() throws Exception {
		File f = new File("src/main/java/org/softlang/qegal/qmodles/model2/demoassertion-.txt");

		for (String line : FileUtils.readLines(f)) {
			String[] parts = line.trim().split(" ");
			String subj = parts[0];
			String pred = parts[1].replace("sl:", SLPREFIX) + ">";
			String obj = parts[2];
			if (obj.contains("sl:"))
				obj = obj.replace("sl:", SLPREFIX) + ">";
			Resource subr = model.getResource(subj);
			Property prop = model.getProperty(pred);
			Resource objr = model.getResource(obj);
			assertFalse(subj +" "+pred+" " +obj,model.contains(subr, prop, objr));
		}
	}

}
