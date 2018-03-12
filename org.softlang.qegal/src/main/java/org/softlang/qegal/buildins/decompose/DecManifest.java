package org.softlang.qegal.buildins.decompose;

import com.google.common.collect.Lists;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.ri.JXPathContextReferenceImpl;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.jena.graph.Node;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jxpath.GenericNodePointerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

/**
 * <pre>
 *  <b> Body: DecManifest(file, key, result)</b>
 * </pre>
 * 
 * <pre>
 *  <b> Head: DecManifest(file, sub, pred, obj) </b>
 * </pre>
 */
public class DecManifest extends DecDeep {

	@Override
	List<Node> decompose(String uri, String xpath) {
		String query = deepQuery(uri, xpath);
		Manifest manifest;
		try {
			manifest = new Manifest(iolayer.access(uri));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Map map = new HashMap();
		for (Map.Entry<Object, Object> attribute : manifest.getMainAttributes().entrySet())
			map.put(attribute.getKey().toString(), attribute.getValue().toString());

		JXPathContext context = JXPathContext.newContext(map);
		List<Object> result = Lists.newArrayList(context.iteratePointers(query));

		return result.stream().map(x -> resolve(filepath(uri), (NodePointer) x)).collect(Collectors.toList());

	}

	protected Node resolve(String filepath, NodePointer pointer) {
		return resolveUri(filepath, pointer);
	}
}
