package org.softlang.qegal.buildins.decompose;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.jena.graph.Node;
import org.softlang.qegal.buildins.Tablings;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jxpath.GenericNodePointerFactory;
import org.softlang.qegal.jxpath.OptionalNodePointer;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;

/**
 * <pre>
 *  <b> Body: DecJava(file, xpath, result)</b>
 * </pre>
 * 
 * <pre>
 *  <b> Head: DecJava(file, sub, pred, obj) </b>
 * </pre>
 */
public class DecJava extends DecDeep {

	static {
		JXPathContextReferenceImpl.addNodePointerFactory(
				new GenericNodePointerFactory<Optional>(1, Optional.class, OptionalNodePointer.class));
	}

	public static Map<String, CompilationUnit> TABLE = new WeakHashMap<>();

	protected CompilationUnit parse(String uri) {
		try {
			return JavaParser.parse(iolayer.access(uri));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Node> decompose(String uri, String xpath) {
		String query = deepQuery(uri, xpath);
		CompilationUnit contextBean = Tablings.get(filepath(uri), TABLE, x -> parse(x));

		JXPathContext context = JXPathContext.newContext(contextBean);
		List<Object> result = Lists.newArrayList(context.iteratePointers(query));

		return result.stream()
					 .map(x -> resolve(filepath(uri), (NodePointer) x))
					 .collect(Collectors.toList());
	}

	protected Node resolve(String filepath, NodePointer pointer) {
		return resolveUri(filepath, pointer);
	}
}
