package org.softlang.qegal.buildins.string;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.softlang.qegal.buildins.QegalBuiltin;

import java.io.*;

public class PathCombine extends QegalBuiltin {

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
		String first = getArg(0, args, context).getURI();
		first = first.replace("repository:", "").replace(">", "");
		String second = getArg(1, args, context).getLiteralLexicalForm();
		File a = new File(first);
		File parentFolder = new File(a.getParent());
		File b = new File(parentFolder, second);
		String absolute = null;
		try {
			absolute = b.getCanonicalPath();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		BindingEnvironment env = context.getEnv();
		return env.bind(getArg(2, args, context), NodeFactory.createLiteral(absolute));
	}
}
