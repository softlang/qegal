package org.softlang.qegal.buildins.decompose;

import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.jena.graph.Node;
import org.softlang.qegal.buildins.Tablings;
import org.softlang.qegal.xml2.Document;
import org.softlang.qegal.xml2.XML2Parser;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;

/**
 * <pre>
 *  <b> Body: DecXml(file, xpath, result)</b>
 * </pre>
 * 
 * <pre>
 *  <b> Head: DecXml(file, sub, pred, obj) </b>
 * </pre>
 */
public class DecXml2 extends DecDeep {

	public static WeakHashMap<String, Document> TABLE = new WeakHashMap<>();

	public Document parse(String path) {
		try {
			return XML2Parser.parse(iolayer.access(path));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Node> decompose(String uri, String xpath) {
		String query = deepQuery(uri, xpath);

		Document contextBean = Tablings.get(filepath(uri), TABLE, x -> parse(x));

		JXPathContext context = JXPathContext.newContext(contextBean);

		// context.setFunctions(new ClassFunctions(ExtensionFunctionsXml.class,"sl"));
		// if(xpath.endsWith("namespace::*"))
		// System.out.println("asdf");

		List<Object> result = Lists.newArrayList(context.iteratePointers(query));
		return result.stream().map(x -> resolve(filepath(uri), (NodePointer) x)).collect(Collectors.toList());
	}

	protected Node resolve(String filepath, NodePointer pointer) {
		return resolveUri(filepath, pointer);
	}

}
