package org.softlang.qegal.qmodles;

import java.io.File;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import static org.softlang.qegal.utils.QegalUtils.query;

public class QModelConsistency {
	
	public static void main(String stagepath) {
		String[] existence = {ecoreFiles(),generatorFiles(),eClassInterfaces(),eClassImpls()};
		String[] negative = {};
		String[][] countequal = {{ecoreFiles(),generatorFiles()},{eClassInterfaces(),eClassImpls()}};
		
		// load .ttl
		File ttls = new File(stagepath+"/ttls/");
		for(File f:ttls.listFiles(f -> f.getName().endsWith(".ttl"))) {
			Model model = RDFDataMgr.loadModel(f.getAbsolutePath());
			
			for(String querytext : existence) {
				Set<String> result = query(model, querytext);
				if(result.isEmpty()) {
					System.err.println("Existence Error");
					System.err.println(querytext);
				}
			}
			for(String[] querytext : countequal) {				
				String query1 = querytext[0];
				String query2 = querytext[1];
				if(query(model, query1).size()!=query(model,query2).size()) {
					System.err.println("Count error");
					System.err.println(query1);
					System.err.println(query2);
				};
			}
		}
	}
	
	/**
	 * get Ecore files
	 * @return SPARQL
	 */
	private static String ecoreFiles() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?x WHERE {"
				+ "	?x sl:elementOf sl:EcoreXMI . "
				+ "}";
		return query;
	}
	
	/**
	 * get Generator model
	 * @return SPARQL
	 */
	private static String generatorFiles() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?x WHERE {"
				+ "	?x sl:elementOf sl:GeneratorXMI . "
				+ "}";
		return query;
	}
	
	/**
	 * get EClass Interface
	 * realized as: there 
	 * @return
	 */
	private static String eClassInterfaces() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?x WHERE {"
				+ "	?x sl:elementOf sl:EClassInterfaceJava . "
				+ "}";
		return query;
	}

	/**
	 * get EClass Impl
	 * realized as: there 
	 * @return
	 */
	private static String eClassImpls() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?x WHERE {"
				+ "	?x sl:elementOf sl:EClassImplJava . "
				+ "}";
		return query;
	}
}
