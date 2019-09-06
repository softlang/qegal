package org.softlang.qegal.buildins.decompose;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.jena.datatypes.xsd.impl.RDFLangString;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * <pre> <b> Body: StrJava(file, xpath, result)</b></pre>
 * <pre> <b> Head: StrJava(file, xpath, pred, obj) </b></pre>
 */
public class StrJava extends DecJava {

    @Override
    protected Node resolve(String filepath, NodePointer pointer) {
		if (pointer instanceof BeanPointer) {
			Object value = pointer.getValue();
			//Node node = NodeFactory.createLiteralByValue(value,RDFLangString.rdfLangString);
			Node node = NodeFactory.createLiteral(value.toString());
            return node;
		}
        throw new RuntimeException("Type not matching");
    }
}
