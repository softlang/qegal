package org.softlang.qegal.buildins.decompose;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.dom.DOMAttributePointer;
import org.apache.commons.jxpath.ri.model.dom.NamespacePointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * <pre> <b> Body: UriXml(file, xpath, result)</b></pre>
 * <pre> <b> Head: UriXml(file, sub, pred, obj) </b></pre>
 */
public class UriXml extends DecXml {

    @Override
    protected Node resolve(String filepath, NodePointer pointer) {
        if (pointer instanceof DOMAttributePointer)
            return NodeFactory.createURI(((DOMAttributePointer) pointer).getValue().toString());
        if (pointer instanceof BeanPointer)
            return NodeFactory.createURI(((BeanPointer) pointer).getValue().toString());
        if (pointer instanceof NamespacePointer)
            return NodeFactory.createURI(((NamespacePointer) pointer).getNamespaceURI());

        throw new RuntimeException("Type not matching");
    }
}
