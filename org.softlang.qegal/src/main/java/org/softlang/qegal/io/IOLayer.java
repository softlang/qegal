package org.softlang.qegal.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface IOLayer {

	//public static IOLayer INSTANCE = new IOFilesystem();

	InputStream access(String uri) throws IOException;

	boolean isDirectory(String uri);

	boolean isFile(String uri);

	String extension(String uri);

	// TODO: Think using name to get each name separately.
	List<String> children(String uri);

	String navigate(String uri, String relativePath);

	String parent(String uri);
	
	String root();
}
