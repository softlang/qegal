package org.softlang.qegal.buildins;

import com.google.common.io.Files;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.Util;
import org.softlang.qegal.io.IOLayer;

import java.io.File;

/**
 * <b> Body: Manifests(file, type)</b>
 * <p>
 * Determines the manifestation type.
 * </p>
 */
public class IsDirectory extends QegalBuiltin {

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
		String uri = getArg(0, args, context).getURI();
		return iolayer.isDirectory(uri);
	}
}
