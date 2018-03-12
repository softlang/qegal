package org.softlang.qegal.buildins.decompose;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * Created by Johannes on 06.10.2017. Super class for decomposition types that
 * dive into one file.
 */
abstract public class DecDeep extends Dec {

	protected String filepath(String uri) {
		return uri.split("#")[0];
	}

	protected String deepQuery(String uri, String xpath) {
		// String filepath = uri.split("#")[0];
		return (uri.contains("#") ? uriToXpath(uri.split("#")[1]) : "") + xpath;
	}

	protected Node resolveUri(String filepath, NodePointer pointer) {
		return NodeFactory.createURI(filepath + "#" + xpathToUri(pointer.asPath()));
	}

}
