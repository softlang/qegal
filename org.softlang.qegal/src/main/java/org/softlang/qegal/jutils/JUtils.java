package org.softlang.qegal.jutils;

import com.google.common.base.Charsets;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.CharSink;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Created by Johannes on 12.10.2017. General Utils.
 */
public class JUtils {
	private static Properties CONFIGURATION = new Properties();

	static {
		try {
			CONFIGURATION.load(new FileInputStream("config.properties"));
		} catch (IOException e) {
			System.out.println("WARNING - There is no 'config.properties' file in the projects root folder");
		}
	}

	public static String configuration(String key) {
		String result = CONFIGURATION.getProperty(key);
		if (result != null)
			return result;

		throw new RuntimeException("Missing configuration key: " + key);
	}

	public static String date() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/**
	 * Reads csv as a list of maps. Returns an empty list if file not found.
	 *
	 * @param file
	 *            .csv
	 * @return
	 */
	public static List<Map<String, String>> readCsv(File file) {
		List<String> rows = null;
		try {
			rows = Files.readLines(file, Charsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return Collections.emptyList();
		}
		List<String> header = Lists.newArrayList(Splitter.on(",").split(rows.remove(0)));

		List<Map<String, String>> results = new ArrayList<>();
		for (String row : rows) {
			List<String> cells = Lists.newArrayList(Splitter.on(",").split(row));
			Map<String, String> cs = new HashMap<>();
			for (int i = 0; i < cells.size(); i++)
				cs.put(header.get(i), cells.get(i));
			results.add(cs);
		}
		return results;
	}

	

	public static void writeList(File file, Charset charset, Iterable<String> items) {
		try {
			Files.asCharSink(file, charset).write(Joiner.on("\n").join(items));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void write(File file, Charset charset, String text) {
		try {
			Files.asCharSink(file, charset).write(text);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static String read(File file, Charset charset) {
		try {
			return FileUtils.readFileToString(file, charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static List<String> readList(File file, Charset charset) {
		try {
			return FileUtils.readLines(file, charset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean writeCsv(File file, List<Map<String, String>> data) {
		try {
			FileWriter output = new FileWriter(file);

			List<String> header = Lists
					.newArrayList(data.stream().flatMap(x -> x.keySet().stream()).collect(Collectors.toSet()));

			output.write(Joiner.on(",").join(header) + "\n");
			for (Map<String, String> row : data) {
				List<String> list = header.stream().map(x -> row.getOrDefault(x, "")).collect(Collectors.toList());
				output.write(Joiner.on(",").join(list) + "\n");
				output.flush();
			}

			output.close();
			return true;
		} catch (IOException e) {
			System.out.println("Can not flush csv results caused by IOException");
			return false;
		}
	}

	public static <T, F> Optional<F> runWithTimeout(Function<T, F> function, T input, long timeoutms) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Optional<F> result = Optional.empty();
		Future<F> future = executor.submit(() -> function.apply(input));

		try {
			result = Optional.of(future.get(timeoutms, TimeUnit.MILLISECONDS));
		} catch (TimeoutException e) {
			future.cancel(true);
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		executor.shutdownNow();
		return result;
	}

	public static Long countFiles(File file) {
		return Files.fileTreeTraverser().postOrderTraversal(file).stream().count();
	}
}
