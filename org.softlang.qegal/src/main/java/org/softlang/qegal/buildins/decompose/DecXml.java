package org.softlang.qegal.buildins.decompose;

import java.io.IOException;
import java.util.List;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.apache.commons.jxpath.ri.QName;
import org.softlang.qegal.buildins.Tablings;
import org.softlang.qegal.io.IOLayer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.apache.jena.graph.*;

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
public class DecXml extends DecDeep {

	public static WeakHashMap<String, Document> TABLE = new WeakHashMap<>();

	public Document parse(String path) {
		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(iolayer.access(path));
			String s = doc.getDocumentElement().getAttribute("xmlns");
			return doc;
		} catch (SAXException | IOException | ParserConfigurationException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public List<Node> decompose(String uri, String xpath) {
		String query = deepQuery(uri, xpath);
		//System.out.println(query);
		Document contextBean = Tablings.get(filepath(uri), TABLE, x -> parse(x));
		JXPathContext context = JXPathContext.newContext(contextBean);
		context.registerNamespace("ns", "http://maven.apache.org/POM/4.0.0");
		//context.registerNamespace("xmlns", "http://maven.apache.org/POM/4.0.0");
		//context.registerNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");

		// context.setFunctions(new ClassFunctions(ExtensionFunctionsXml.class,"sl"));
		// if(xpath.endsWith("namespace::*"))
		//System.out.println("asdf");

		List<Object> result = Lists.newArrayList(context.iteratePointers(query));
		List<Node> nodes = result.stream().map(x -> resolve(filepath(uri), (NodePointer) x)).collect(Collectors.toList());
		return nodes;
	}

	protected Node resolve(String filepath, NodePointer pointer) {
		return resolveUri(filepath, pointer);
	}

}
