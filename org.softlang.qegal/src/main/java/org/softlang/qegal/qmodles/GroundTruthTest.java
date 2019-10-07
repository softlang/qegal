package org.softlang.qegal.qmodles;

import static org.softlang.qegal.utils.QegalUtils.query;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

public class GroundTruthTest {

	public static void main(String[] args) {
		errorLog("data/qmodles/stage0/", "SimplePO", "Marcel");
	}
	
	public static void errorLog(String stage, String project, String user) {
		File userProtocol = new File(stage + "Testprotocol-" + project + "-" + user + ".csv");
		File ttl = new File(stage + "ttls/" + project + ".ttl");
		File errorF = new File(stage + "Testprotocol-"+project+"-"+user+"-errors.txt");
		
		Model model = RDFDataMgr.loadModel(ttl.getAbsolutePath());

		try {
			List<String> lines = Files.readAllLines(userProtocol.toPath());
			FileWriter fw = new FileWriter(errorF, false);
			for (String line : lines) {
				String[] linePs = line.split(",");
				String uri = linePs[0].trim();
				String choice = linePs[linePs.length - 1].trim();
				String overestimate = linePs[linePs.length - 2].trim();
				Set<String> underestimates = new HashSet<>();
				for (int i = 1; i < linePs.length - 2; i++) {
					underestimates.add(linePs[i].trim());
				}

				String qtext = "PREFIX sl: <http://org.softlang.com/> \n" + "SELECT DISTINCT ?x WHERE { <" + uri
						+ "> sl:elementOf ?x. }";
				Set<String> languages = query(model, qtext).stream()
														   .map(l -> l.toString().replace("http://org.softlang.com/", ""))
														   .collect(Collectors.toSet());
				
				languages.retainAll(underestimates);
				
				// fp if choice is overestimate but languages contains underestimate
				if(choice.equals(overestimate) && languages.contains(overestimate) && languages.size()>1)
					fw.write(uri+" is element of "+choice+", but .ttl contains "+languages.toString());
				// fn if choice is in underestimate but languages only contains overestimate
				if(underestimates.contains(choice) && languages.contains(overestimate) && languages.size() == 1)
					fw.write(uri+" is element of"+choice+", but is not detected.");
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
