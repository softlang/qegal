package org.softlang.qegal.jutils;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang.StringEscapeUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Johannes on 26.10.2017. For complex logging. TODO: This may be
 * replaced by commons csv.
 */
public class CSVSink {

	public enum SinkType {
		HEADER_FILE, FIRST_LINE, STATIC, NONE, DYNAMIC
	}

	private LinkedHashSet<String> header = new LinkedHashSet();
	private CSVPrinter writer = null;

	private final Charset charset;
	private final SinkType type;
	private final String path;

	private int lineNumber = 0;

	public CSVSink(String path, Charset charset, SinkType type, String... staticHeader) {
		this.type = type;
		this.charset = charset;
		this.path = path;

		initializeWriter();

		switch (type) {
		case STATIC: {
			for (String x : staticHeader)
				header.add(x);
			write(header.toArray(new String[0]));
			break;
		}
		case FIRST_LINE: {
			break;
		}
		case HEADER_FILE: {
			break;
		}
		case NONE: {
			break;
		}
		}
	}

	private void initializeWriter() {
		try {
			Files.createParentDirs(new File(path));
			writer = CSVFormat.DEFAULT.print(new File(path), charset);// new CSVWriter(new FileWriter(new File(path)),
																		// ',', CSVWriter.NO_QUOTE_CHARACTER);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		try {
			writer.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(String... row) {

		try {
			writer.printRecord(row);
		} catch (IOException e) {
			throw new RuntimeException();
		}
		lineNumber++;
	}

	public void write(Iterable<String> iterable) {
		write(Lists.newArrayList(iterable).toArray(new String[0]));
	}

	public void flush() {
		try {
			writer.flush();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void write(Map<String, String> row) {
		switch (type) {
		case STATIC: {
			write(Iterables.transform(header, x -> row.getOrDefault(x, "")));
			break;
		}
		case FIRST_LINE: {
			if (lineNumber == 0) {
				header.addAll(row.keySet());
				write(header);
			} else if (header.addAll(row.keySet()))
				throw new RuntimeException("Header changed during writing process");

			write(Iterables.transform(header, x -> row.getOrDefault(x, "")));
			break;
		}
		case HEADER_FILE: {
			if (header.addAll(row.keySet())) {
				try {
					Files.asCharSink(new File(path.substring(0, path.lastIndexOf(".")) + ".header"), charset)
							.write(Joiner.on(",").join(header));
				} catch (IOException e) {
					System.out.println("Cannot write new header file");
				}
			}
			write(Iterables.transform(header, x -> row.getOrDefault(x, "")));
			break;
		}
		case DYNAMIC: {
			if (header.addAll(row.keySet())) {
				File temp = new File(path + "temp");
				File current = new File(path);
				try {
					writer.flush();
					writer.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				try {
					Files.move(current, temp);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				// Initialize new file and header.
				initializeWriter();
				write(header);

				try {
					Iterable<CSVRecord> records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(temp));
					for (CSVRecord record : records)
						write(Iterables.transform(header, x -> record.toMap().getOrDefault(x, "")));
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				// Delete temp file .
				temp.delete();
			}
			write(Iterables.transform(header, x -> row.getOrDefault(x, "")));

			break;
		}
		case NONE:
			throw new RuntimeException();
		}
	}
}

// public CSVSink(String filePath, Charset charset, boolean separateHeader,
// String... header) {
// this.charset = charset;
// csvFile = new File(filePath);
// headerFile = separateHeader ? new File(filePath.substring(0,
// filePath.lastIndexOf(".")) + ".header") : null;
//
// // Delete original content.
// if (csvFile.exists()) csvFile.delete();
// if (headerFile != null && headerFile.exists()) headerFile.delete();
//
// // Add header if existing.
// for (String x : header) this.header.add(x);
// // Initialize writer.
// try {
// Files.createParentDirs(csvFile);
// csvFile.createNewFile();
// writer = new OutputStreamWriter(new FileOutputStream(csvFile), charset);
//
// // Set first line of csv if header is defined.
// if (this.header.size() > 0)
// internalWrite(this.header);
//
// } catch (IOException e) {
// throw new RuntimeException(e);
// }
// }

// protected void updateHeader(Map<String, String> row) {
// if (header.addAll(row.keySet())) {
// if (header != null) {
// try {
// Files.asCharSink(headerFile, charset).write(line(header));
// } catch (IOException e) {
// System.out.println("Cannot write new header file");
// }
// }
// }
// }