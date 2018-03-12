package org.softlang.qegal.buildins;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.softlang.qegal.io.IOLayer;

import com.google.common.io.Files;

import java.io.File;

/**
 * <pre>
 *  <b> Body: Extension(file, extension)</b>
 * </pre>
 */
public class Extension extends QegalBuiltin {

	@Override
	public int getArgLength() {
		return 2;
	}

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
		BindingEnvironment env = context.getEnv();
		String path = getArg(0, args, context).getURI();
		Node ext = getArg(1, args, context);
		return env.bind(ext, NodeFactory.createLiteral(iolayer.extension(path)));
	}
}
