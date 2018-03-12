package org.softlang.qegal.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.jena.ext.com.google.common.io.Files;

import com.google.common.collect.ImmutableList;

public class IOFilesystem implements IOLayer {

	public static final String ROOT = "repository:";
	private final String workingdir;

	public IOFilesystem(File workingdir) {
		this.workingdir = workingdir.getAbsolutePath();
	}

	public String pathToUri(String path) {
		return path.replace(workingdir, ROOT).replace(" ", "%20").replace("\\", "/");
	}

	public String pathToUri(File file) {
		return pathToUri(file.getAbsolutePath());
	}

	public String uriToPath(String uri) {
		return uri.replace("%20", " ").replace(ROOT, workingdir).replace("/", File.separator);
	}

	@Override
	public InputStream access(String uri) throws IOException {
		try {
			return new FileInputStream(new File(uriToPath(uri)));
		} catch (FileNotFoundException e) {
			throw new IOException(e);
		}
	}

	@Override
	public boolean isDirectory(String uri) {
		return new File(uriToPath(uri)).isDirectory();
	}

	@Override
	public boolean isFile(String uri) {
		return new File(uriToPath(uri)).isFile();
	}

	@Override
	public String extension(String uri) {
		return Files.getFileExtension(uriToPath(uri));
	}

	@Override
	public List<String> children(String uri) {
		ImmutableList.Builder<String> builder = ImmutableList.builder();
		for (File file : new File(uriToPath(uri)).listFiles())
			builder.add(pathToUri(file));
		return builder.build();
	}

	@Override
	public String navigate(String uri, String relativePath) {
		return pathToUri(new File(new File(uriToPath(uri)), relativePath));
	}

	@Override
	public String parent(String uri) {
		return pathToUri(new File(uriToPath(uri)).getParentFile());
	}

	@Override
	public String root() {
		return ROOT;
	}

}
