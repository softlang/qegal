package org.softlang.qegal.qmodles.model1;

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

public class WildScope {

	public static final int GIT_INDEXED_MAX_SIZE = 384000;

	public static void main(String[] args) throws IOException {
		File eObjectProjectsCSV = new File("data/qmodles/model1/wild/files_eobject_raw.csv");
		File genModelProjectsCSV = new File("data/qmodles/model1/wild/files_genmodel_raw.csv");
		File ecoreModelProjectsCSV = new File("data/qmodles/model1/wild/files_ecoremodel_raw.csv");
		
		queryEObject(eObjectProjectsCSV);
		queryGenmodel(genModelProjectsCSV);
		queryEcore(ecoreModelProjectsCSV);
	}

	public static void queryEObject(File file) throws IOException {
		String query = "https://api.github.com/search/code?q=%22extends%20EObject%20{%22+language:java";

		int totalCount = collect(query).getAsJsonObject().get("total_count").getAsInt();

		CSVPrinter printer = CSVFormat.DEFAULT.print(file, Charsets.UTF_8);
		printer.printRecord("total", "low", "high", "page", "repository", "url");
		collectAll(printer, query, 0, GIT_INDEXED_MAX_SIZE);
		printer.close();
	}

	public static void queryGenmodel(File file) throws IOException {
		String query = "https://api.github.com/search/code?q=GenModel+extension:genmodel";

		int totalCount = collect(query).getAsJsonObject().get("total_count").getAsInt();

		CSVPrinter printer = CSVFormat.DEFAULT.print(file, Charsets.UTF_8);
		printer.printRecord("total", "low", "high", "page", "repository", "url");
		collectAll(printer, query, 0, GIT_INDEXED_MAX_SIZE);
		printer.close();
	}

	public static void queryEcore(File file) throws IOException {
		String query = "https://api.github.com/search/code?q=EClass+extension:ecore";
		int totalCount = collect(query).getAsJsonObject().get("total_count").getAsInt();

		CSVPrinter printer = CSVFormat.DEFAULT.print(file, Charsets.UTF_8);
		printer.printRecord("total", "low", "high", "page", "repository", "url");
		collectAll(printer, query, 0, GIT_INDEXED_MAX_SIZE);
		printer.close();
	}

	public static void collectAll(CSVPrinter printer, String service, int low, int high) throws IOException {
		for (int page = 1; page <= 10; page++) {
			String query = service + "+size:" + String.valueOf(low) + ".." + String.valueOf(high) + "&page="
					+ String.valueOf(page) + "&per_page=100";
			JsonObject json = collect(query).getAsJsonObject();
			int totalCount = json.get("total_count").getAsInt();

			if (totalCount > 1000 && low != high) {
				System.out.println("Spliting " + totalCount + " @ " + query);
				int middle = low + (int) Math.floor(((double) (high - low)) / 2.0);
				collectAll(printer, service, low, middle);
				collectAll(printer, service, middle + 1, high);
				return;
			}

			System.out.println("Consuming " + totalCount + " @ " + query);

			for (JsonElement item : json.get("items").getAsJsonArray()) {
				String fullName = item.getAsJsonObject().get("repository").getAsJsonObject().get("full_name")
						.getAsString();
				String url = item.getAsJsonObject().get("html_url").getAsString();
				printer.printRecord(totalCount, low, high, page, fullName, url);
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
