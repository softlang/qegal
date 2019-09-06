package org.softlang.qegal.buildins.decompose;

import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.dom.DOMAttributePointer;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.apache.commons.jxpath.ri.model.dom.NamespacePointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.softlang.qegal.buildins.Tablings;
import org.softlang.qegal.xml2.Document;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;

/**
 * <pre> <b> Body: StrXml(file, xpath, result)</b></pre>
 * <pre> <b> Head: StrXml(file, sub, pred, obj) </b></pre>
 */
public class StrPropertyXml2 extends DecXml2 {

	@Override
	public List<Node> decompose(String uri, String xpath) {
		String oxpath = xpath.substring(0, xpath.lastIndexOf("/"));
		String property = xpath.substring(xpath.lastIndexOf("/")+1);
		
		String query = deepQuery(uri, oxpath);
		Document contextBean = Tablings.get(filepath(uri), TABLE, x -> parse(x));

		JXPathContext context = JXPathContext.newContext(contextBean);
		Iterator<?> it = context.iteratePointers(query);
		
		List<Object> result = Lists.newArrayList(it);
		
		return result.stream()
					 .map(x -> resolveProperty(filepath(uri), (NodePointer) x, property))
					 .collect(Collectors.toList());
	}
	
    protected Node resolveProperty(String filepath, NodePointer pointer, String property) {
        if (pointer instanceof BeanPointer) {
        	Object value = pointer.getBaseValue();
			Object x = null;
			try {
				x = value.getClass().getMethod(property).invoke(value);
				
			} catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException | SecurityException | NoSuchMethodException e) {
				e.printStackTrace();
			} 
			//Node node = NodeFactory.createLiteralByValue(value,RDFLangString.rdfLangString);
			Node node = NodeFactory.createLiteral(x.toString());
            return node;
        }
        throw new RuntimeException("Type not matching");
    }
}
