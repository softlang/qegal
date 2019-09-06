package org.softlang.qegal.qmodles;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import javax.swing.JOptionPane;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

public class TestProtocolUI {

	public static void main(String[] args) {
		startTestProtocol("data/qmodles/stage0/", "SimplePO","Marcel");
	}

	public static void startTestProtocol(String stage, String project, String user) {
		File csv = new File(stage+"Testprotocol-" + project + ".ttl.csv");
		File ttl = new File(stage+"ttls/" + project + ".ttl");
		Model model = RDFDataMgr.loadModel(ttl.getAbsolutePath());
		try {
			// execute queries
			String qtext = "PREFIX env: <http://org.softlang.com/env/> \n"
					+ "SELECT DISTINCT ?x ?y WHERE { ?x env:content ?y . }";
			Query query = QueryFactory.create(qtext, Syntax.syntaxARQ);
			QueryExecution qe = QueryExecutionFactory.create(query, model);
			ResultSet resultSet = qe.execSelect();
			HashMap<String, String> contents = new HashMap<>();
			while (resultSet.hasNext()) {
				QuerySolution r = resultSet.next();
				contents.put(r.get("?x").toString(), r.get("?y").toString());
			}
			System.out.println(contents.size());
			File out = new File(stage+"Testprotocol-"+project+"-"+user+".csv");
			FileWriter fw = new FileWriter(out, false);
			
			List<String> lines = Files.readAllLines(csv.toPath());
			int count=0;
			for (String row : lines) {
				String[] rowPs = row.split(",");
				
				System.out.println("========================");
				System.out.println("========================");
				String content = "";
				String uri = rowPs[0];
				if(uri.startsWith("cls:"))
					continue;
				if (contents.containsKey(uri)) {
					content = contents.get(uri);
				}
				
				System.out.println("%%%%%%%%%%%%%%%%%%%%%%%%");
		        System.out.println("Determine the smallest subset that contains the linked artifact.");
		        System.out.println(uri);
		        System.out.println(content);
				
				BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
		        int answer = 0;
		        do{
		        	System.out.print("Enter ");
		        	for(int i=1;i<rowPs.length;i++) {
		        		System.out.print("("+i+"="+rowPs[i]+") ");
		        	}
		        	System.out.println();
		        	String s = br.readLine();
		        	System.out.println();
		        	System.out.println();
		        	try {
		        		answer = Integer.parseInt(s);
		        	}catch (NumberFormatException e) {
						System.out.println("Not a number. Try again.");
						answer = rowPs.length;
					}
		        }while(!(rowPs.length > answer && answer > 0));
				fw.write(row+","+rowPs[answer]);
				count++;
				System.out.println("Progress: "+count+"/"+lines.size());
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
