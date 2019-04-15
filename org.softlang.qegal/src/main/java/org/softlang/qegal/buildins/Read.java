package org.softlang.qegal.buildins;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;

/**
 * <pre>
 *  <b> Body: Extension(file, extension)</b>
 * </pre>
 */
public class Read extends QegalBuiltin {

	@Override
	public int getArgLength() {
		return 2;
	}

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
		BindingEnvironment env = context.getEnv();
		String path = getArg(0, args, context).getURI();
		Node ext = getArg(1, args, context);
		try {
			return env.bind(ext, NodeFactory.createLiteral(IOUtils.toString(iolayer.access(path),Charsets.UTF_8)));
		} catch (IOException e) {
			throw new RuntimeException("Things in Read content method");
		}
	}
}
