package org.softlang.qegal.jxpath;

import org.softlang.qegal.io.IOLayer;

public class IONode {
	public final String uri;

	public final IOLayer iolayer;

	public IONode(String uri, IOLayer iolayer) {
		this.uri = uri;
		this.iolayer = iolayer;
	}
}
