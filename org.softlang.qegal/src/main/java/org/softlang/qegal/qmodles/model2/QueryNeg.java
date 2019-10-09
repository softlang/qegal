package org.softlang.qegal.qmodles.model2;

import static org.softlang.qegal.utils.QegalUtils.query;

import java.io.File;
import java.util.Set;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

public class QueryNeg {

	/**
	 * absence vs. equality in numbers
	 * @param args
	 */
	public static void main(String[] args) {
		File ttls = new File("data/qmodles/model2/wild/ttls");
		//File ttls = new File("data/qmodles/model2/demo/ttls");
		for(File f:ttls.listFiles(f -> f.getName().endsWith(".ttl"))) {
			Model model = RDFDataMgr.loadModel(f.getAbsolutePath());
			
			Set<QuerySolution> result = query(model, noCreateGeneratorModel());
			if(!result.isEmpty()) {
				System.err.println("There exist generator models without creation relation.");
				result.forEach(r -> System.err.println(r.toString()));
			}
			
			result = query(model, createGeneratorModelNotEcoreModel());
			if(!result.isEmpty()) {
				System.err.println("Left side of CreateGeneratorModel is not Ecore Model.");
				result.forEach(r -> System.err.println(r.toString()));
			}
			
			result = query(model, adapterFactoryNotGenerateJavaCode());
			if(!result.isEmpty()) {
				System.err.println("Adapter Factory without GenerateJavaCode");
				result.forEach(r -> System.err.println(r.toString()));
			}
			
			result = query(model, interfaceNotGenerateJavaCode());
			if(!result.isEmpty()) {
				System.err.println("Interface without GenerateJavaCode");
				result.forEach(r -> System.err.println(r.toString()));
			}
		}
	}
	
	/**
	 * get CreateGeneratorModel
	 * @return SPARQL
	 */
	private static String createGeneratorModel() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?e ?g WHERE {"
				+ "	?e sl:CreateGeneratorModel ?g . "
				+ "}";
		return query;
	}
	
	/**
	 * get CreateGeneratorModel
	 * @return SPARQL
	 */
	private static String sourceAbsence() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?e ?g WHERE {"
				+ "	?e sl:CreateGeneratorModel ?g ."
				+ "FILTER NOT EXISTS{ ?e sl:instanceOf sl:EcoreModel. } "
				+ "}";
		return query;
	}
	
	private static String targetAbsence() {
		String query = "\"PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?e ?g WHERE {"
				+ "	?e sl:CreateGeneratorModel ?g ."
				+ "FILTER NOT EXISTS{ ?g sl:instanceOf sl:GeneratorModel. } "
				+ "}";
		return query;
	}
	
	private static String noCreateGeneratorModel() {
		String query = "PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?g WHERE {"
				+ "	?g sl:instanceOf sl:GeneratorModel ."
				+ "FILTER NOT EXISTS{ ?e sl:CreateGeneratorModel ?g. } "
				+ "}";
		return query;
	}
	
	private static String createGeneratorModelNotEcoreModel() {
		String query = "PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?e ?g WHERE {"
				+ "	?e sl:CreateGeneratorModel ?g ."
				+ "FILTER NOT EXISTS{ ?e sl:instanceOf sl:EcoreModel. } "
				+ "}";
		return query;
	}
	
	private static String adapterFactoryNotGenerateJavaCode() {
		String query = "PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?af WHERE {"
				+ "	?af sl:instanceOf sl:AdapterFactory ."
				+ "FILTER NOT EXISTS{ ?e sl:GenerateJavaCode ?af. } "
				+ "}";
		return query;
	}
	
	private static String interfaceNotGenerateJavaCode() {
		String query = "PREFIX sl: <http://org.softlang.com/> \n"
				+ "SELECT DISTINCT ?i WHERE {"
				+ "	?i sl:instanceOf sl:Interface ."
				+ "FILTER NOT EXISTS{ ?e sl:GenerateJavaCode ?i. } "
				+ "}";
		return query;
	}

}
