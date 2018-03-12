package org.softlang.qegal.buildins;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.BuiltinException;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.util.URIref;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Johannes on 24.11.2017.
 */
public class EscapeUri extends QegalBuiltin {

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
		if (length != 2)
			throw new BuiltinException(this, context, "Must have 2 arguments " + getName());

		Node left = getArg(0, args, context);
		Node right = getArg(1, args, context);
		
		BindingEnvironment env = context.getEnv();
		if (left.isConcrete() && !left.isVariable())
			return env.bind(right, NodeFactory.createURI(decode(getString(left))));
		if (right.isConcrete() && !right.isVariable())
			return env.bind(left, NodeFactory.createURI(encode(getString(left))));
		
		return false;
	}

	private String getString(Node n) {
		if (n.isLiteral())
			return n.getLiteralLexicalForm();
		else
			return n.getURI();
	}
	
	private String decode(String s) {
		return s.replace("-","%45");
	}
	
	private String encode(String s) {
		return s.replace("%45","-");
	}
}
