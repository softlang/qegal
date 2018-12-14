package org.softlang.qegal.process;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.IOUtils;
import org.softlang.qegal.jutils.JUtils;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class Poms {

	private static final int MAX_RESULTS = 280;
	
	public static final int GIT_INDEXED_MAX_SIZE = 384000;
	
	private static int resultsCount = 0;
	private static int totalSeenCount = 0;
	
	//public static final int GIT_INDEXED_MAX_SIZE = 10000;

	public static void main(String[] args) throws Exception {
		queryPoms();
	}

	public static void queryPoms() throws Exception {
		String query = "https://api.github.com/search/repositories?sort=stars&order=desc&q=language:java";

		int totalCount = collect(query).getAsJsonObject().get("total_count").getAsInt();

		File file = new File("data/maven_with_hashes.csv");

		CSVPrinter printer = CSVFormat.DEFAULT.print(file, Charsets.UTF_8);
		//printer.printRecord("total", "low", "high", "page", "repository", "url");
		printer.printRecord("repo", "hash", "url");
		collectAll(printer, query, 25, GIT_INDEXED_MAX_SIZE);
		//collectTop(printer, query);
		printer.close();
	}
	
	public static void collectTop(CSVPrinter printer, String service) throws Exception {
		int totalCount = 0;
		int resultsCount = 0;
		int page = 1;
		while (resultsCount < MAX_RESULTS) {
			String query = service + "&page="+page+"&per_page=100";
			JsonObject json = collect(query).getAsJsonObject();
			//System.out.println(json);
			System.out.println("Consuming " + query);
			for (JsonElement item : json.get("items").getAsJsonArray()) {
				if(resultsCount >= MAX_RESULTS) {
					break;
				}
				totalCount += 1;
				String fullName = item.getAsJsonObject().get("full_name")
						.getAsString();
				String url = item.getAsJsonObject().get("html_url").getAsString();
				System.out.println("Getting " + fullName);
				String subQuery = "https://api.github.com/search/code?q=filename:pom.xml+extension:xml+repo:" + fullName;
				JsonObject subJson = collect(subQuery).getAsJsonObject();
				//System.out.println(subJson);
				int subCount = subJson.get("total_count").getAsInt();
				if(subCount == 0) {
					System.out.println(resultsCount + "/" + totalCount);
					System.out.println("Skipping");
					continue;
				}
				System.out.println("Found " + subCount + " file(s)");
				/*for(JsonElement jsonFile : subJson.get("items").getAsJsonArray()) {
					String file = jsonFile.getAsJsonObject().get("html_url").getAsString();
					printer.printRecord(fullName, url, file);
				}*/
				String branch = item.getAsJsonObject().get("default_branch").getAsString();
				String subQuery2 = "https://api.github.com/repos/" + fullName + "/commits/" + branch;
				String hash = collect(subQuery2).getAsJsonObject().get("sha").getAsString();
				System.out.println(hash);
				printer.printRecord(fullName, hash, url);
				//String file = (subJson.get("items").getAsJsonArray().get(0)).getAsJsonObject().get("html_url").getAsString();
				resultsCount += 1;
				System.out.println(resultsCount + "/" + totalCount);
			}
			page += 1;
		}
		printer.flush();
		
	}

	public static void collectAll(CSVPrinter printer, String service, int low, int high) throws Exception {
		for (int page = 1; page <= 10; page++) {
			String query = service + "+stars:" + String.valueOf(low) + ".." + String.valueOf(high) + "&page="+page+"&per_page=100";
			JsonObject json = collect(query).getAsJsonObject();
			int totalCount = json.get("total_count").getAsInt();

			if (totalCount > 1000 && low != high) {
				System.out.println("Spliting " + totalCount + " @ " + query);
				int middle = low + (int) Math.floor(((double) (high - low)) / 2.0);
				collectAll(printer, service, low, middle);
				collectAll(printer, service, middle + 1, high);
				return;
			}
			//System.out.println(json);
			System.out.println("Consuming " + query);
			for (JsonElement item : json.get("items").getAsJsonArray()) {
				/*if(resultsCount >= MAX_RESULTS) {
					break;
				}*/
				totalSeenCount += 1;
				String fullName = item.getAsJsonObject().get("full_name")
						.getAsString();
				String url = item.getAsJsonObject().get("html_url").getAsString();
				System.out.println("Getting " + fullName);
				String subQuery = "https://api.github.com/search/code?q=filename:pom.xml+extension:xml+repo:" + fullName;
				JsonObject subJson = collect(subQuery).getAsJsonObject();
				//System.out.println(subJson);
				int subCount = subJson.get("total_count").getAsInt();
				if(subCount == 0) {
					System.out.println(resultsCount + "/" + totalSeenCount);
					System.out.println("Skipping");
					continue;
				}
				System.out.println("Found " + subCount + " file(s)");
				/*for(JsonElement jsonFile : subJson.get("items").getAsJsonArray()) {
					String file = jsonFile.getAsJsonObject().get("html_url").getAsString();
					printer.printRecord(fullName, url, file);
				}*/
				String branch = item.getAsJsonObject().get("default_branch").getAsString();
				String subQuery2 = "https://api.github.com/repos/" + fullName + "/commits/" + branch;
				String hash = collect(subQuery2).getAsJsonObject().get("sha").getAsString();
				System.out.println(hash);
				printer.printRecord(fullName, hash, url);
				//String file = (subJson.get("items").getAsJsonArray().get(0)).getAsJsonObject().get("html_url").getAsString();
				resultsCount += 1;
				System.out.println(resultsCount + "/" + totalSeenCount);
			}
			
			printer.flush();

			// No results left here.
			if ((page) * 100 > totalCount)
				return;
		}
	}

	public static JsonPrimitive rateLimit() {
		return collect("https://api.github.com/rate_limit").getAsJsonPrimitive();
	}

	public static JsonElement collect(String service) {
		try {
			URL myURL = new URL(service);
			HttpURLConnection myURLConnection = (HttpURLConnection) myURL.openConnection();

			String userCredentials = JUtils.configuration("login_git") + ":" + JUtils.configuration("password_git");
			String encodedAuthorization = "Basic " + new String(org.apache.commons.codec.binary.Base64
					.encodeBase64(userCredentials.getBytes(Charsets.UTF_8.name())), Charsets.UTF_8.name());

			myURLConnection.setRequestProperty("Authorization", encodedAuthorization);
			myURLConnection.connect();

			int response = myURLConnection.getResponseCode();

			if (200 == response) {
				JsonElement jelement = new JsonParser().parse(new InputStreamReader(myURLConnection.getInputStream()));

				// Process this.
				myURLConnection.disconnect();
				return jelement;
			} else if (403 == response) {
				System.out.println("403 GiThUb");
				Thread.sleep(30000l);
				return collect(service);
			} else {
				throw new RuntimeException(
						"GiThUb: " + IOUtils.toString(myURLConnection.getErrorStream(), Charsets.UTF_8));
			}
		} catch (Exception e) {
			System.out.println("exception and retrying" + e);
			try {
				Thread.sleep(30000l);
			} catch (InterruptedException e1) {
				throw new RuntimeException(e1);
			}
			return collect(service);
		}

	}

}
