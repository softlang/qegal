package org.softlang.qegal.buildins.decompose;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.dom.DOMAttributePointer;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.apache.commons.jxpath.ri.model.dom.NamespacePointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * <pre> <b> Body: StrXml(file, xpath, result)</b></pre>
 * <pre> <b> Head: StrXml(file, sub, pred, obj) </b></pre>
 */
public class StrXml extends DecXml {

    @Override
    protected Node resolve(String filepath, NodePointer pointer) {
        if (pointer instanceof DOMAttributePointer) {
        	return NodeFactory.createLiteral(((DOMAttributePointer) pointer).getValue().toString());
        }
        if (pointer instanceof BeanPointer) {
            return NodeFactory.createLiteral(((BeanPointer) pointer).getValue().toString());
        }
        if (pointer instanceof NamespacePointer) {
            return NodeFactory.createLiteral(((NamespacePointer) pointer).getNamespaceURI());
        }
        if(pointer instanceof DOMNodePointer) {
            return NodeFactory.createLiteral(((DOMNodePointer) pointer).getValue().toString());
        }

        throw new RuntimeException("Type not matching");
    }
}
