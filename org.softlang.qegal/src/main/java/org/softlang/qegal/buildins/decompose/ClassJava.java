package org.softlang.qegal.buildins.decompose;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * <pre> <b> Body: DecJava(file, xpath, result)</b></pre>
 * <pre> <b> Head: DecJava(file, sub, pred, obj) </b></pre>
 */
public class ClassJava extends DecJava {

    @Override
    protected Node resolve(String filepath, NodePointer pointer) {
        if (pointer instanceof BeanPointer)
            return NodeFactory.createLiteral(((BeanPointer) pointer).getValue().getClass().getName().toString());

        throw new RuntimeException("Type not matching");
    }
}
