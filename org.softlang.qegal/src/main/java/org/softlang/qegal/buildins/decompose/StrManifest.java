package org.softlang.qegal.buildins.decompose;

import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

/**
 * <pre>
 *  <b> Body: StrManifest(file, key, result)</b>
 * </pre>
 * 
 * <pre>
 *  <b> Head: StrManifest(file, sub, pred, obj) </b>
 * </pre>
 */
public class StrManifest extends DecManifest {

	@Override
	protected Node resolve(String filepath, NodePointer pointer) {
		if (pointer instanceof BeanPointer)
			return NodeFactory.createLiteral(((BeanPointer) pointer).getValue().toString());

		throw new RuntimeException("Type not matching");
	}
}
