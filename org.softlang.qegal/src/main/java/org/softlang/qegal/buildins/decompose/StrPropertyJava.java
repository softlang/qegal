package org.softlang.qegal.buildins.decompose;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.softlang.qegal.buildins.Tablings;

import com.github.javaparser.ast.CompilationUnit;
import com.google.common.collect.Lists;

/**
 * xpath ends with the name of a getter for a bean's property
 * <pre> <b> Body: StrPropertyJava(file, xpath, result)</b></pre>
 * <pre> <b> Head: StrPropertyJava(file, sub, pred, obj) </b></pre>
 */
public class StrPropertyJava extends DecJava {

	@Override
	public List<Node> decompose(String uri, String xpath) {
		String oxpath = xpath.substring(0, xpath.lastIndexOf("/"));
		String property = xpath.substring(xpath.lastIndexOf("/")+1);
		
		String query = deepQuery(uri, oxpath);
		CompilationUnit contextBean = Tablings.get(filepath(uri), TABLE, x -> parse(x));

		JXPathContext context = JXPathContext.newContext(contextBean);
		List<Object> result = Lists.newArrayList(context.iteratePointers(query));

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
