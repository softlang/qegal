package org.softlang.qegal.buildins.decompose;

import com.google.common.collect.Lists;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jxpath.FileNodePointer;
import org.softlang.qegal.jxpath.GenericNodePointerFactory;
import org.softlang.qegal.jxpath.IONode;
import org.softlang.qegal.jxpath.IONodePointer;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <pre>
 *  <b> Body: DecFs(file, xpath, result)</b>
 * </pre>
 * 
 * <pre>
 *  <b> Head: DecFs(file, sub, pred, obj) </b>
 * </pre>
 * 
 * <p>
 * <b>Body usage</b>: Decomposes the file using xpath and binds the first result
 * on result.
 * </p>
 * <p>
 * <b>Head usage</b>: Takes the xpath or uri in sub, pred and obj position and
 * adds all resulting triples where the xpath is replaced by the decomposition
 * of file.
 * </p>
 * s
 */
public class DecFs extends Dec {

	static {
		JXPathContextReferenceImpl
				.addNodePointerFactory(new GenericNodePointerFactory<IONode>(1, IONode.class, IONodePointer.class));

	}

	@Override
	List<Node> decompose(String uri, String xpath) {
		Object contextBean = new IONode(uri, iolayer);
		JXPathContext context = JXPathContext.newContext(contextBean);

		// TODO: Convert this again to file.
		List<IONodePointer> decomposition = Lists.newArrayList(context.iteratePointers(xpath));
		return decomposition.stream().map(x -> NodeFactory.createURI(x.content.uri)).collect(Collectors.toList());
	}

}
