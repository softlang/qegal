package org.softlang.qegal.buildins.decompose;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.BuiltinException;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.softlang.qegal.buildins.QegalBuiltin;

import java.util.Collections;
import java.util.List;

/**
 * Created by Johannes on 28.09.2017. // TODO: This needs to be checked again
 * (brain damage causes error 3).
 */
abstract public class Dec extends QegalBuiltin {

	abstract List<Node> decompose(String uri, String xpath) throws Exception;

	protected String uriToXpath(String xpath) {
		return xpath.replace("(", "[").replace(")", "]");
	}

	protected String xpathToUri(String xpath) {
		return xpath.replace("[", "(").replace("]", ")");
	}

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext ruleContext) {
		if (length != 3)
			throw new BuiltinException(this, ruleContext, "Wrong parameter count");
		BindingEnvironment env = ruleContext.getEnv();

		// This is the node that will be decomposed.
		Node decomposee = getArg(0, args, ruleContext);

		// Decomposed the decomposee node.
		String query = getArg(1, args, ruleContext).getLiteral().toString();

		List<Node> results;
		try {
			results = decompose(decomposee.getURI(), query);
		} catch (Exception e) {
			String message = "\"" + String.valueOf(e.getMessage()).replace("\n", " ") + "\"";
			submit(args, length, ruleContext, "exception", "message", message);
			return false;
		}

		// TODO: Check. If more that on semantic dependent on node status. Not sure if
		// this works!
		Node assignee = getArg(2, args, ruleContext);

		if (assignee.isConcrete() && !assignee.isVariable()) {
			// Contains semantic. Not sure if this works!
			boolean res = results.contains(assignee);
			return res;
		}
		if (assignee.isVariable() && !assignee.isConcrete()) {
			if (results.size() == 1)
				return env.bind(assignee, results.get(0));
			else
				return false;
		}
		throw new RuntimeException();
	}

	@Override
	public void trackedHeadAction(Node[] args, int length, RuleContext ruleContext) {
		if (length == 4) {

			// This is the node that will be decomposed.
			Node decomposee = getArg(0, args, ruleContext);

			// Determines the position of the xpath.
			int position;
			if (getArg(1, args, ruleContext).isLiteral())
				position = 1;
			else if (getArg(2, args, ruleContext).isLiteral())
				position = 2;
			else if (getArg(3, args, ruleContext).isLiteral())
				position = 3;
			else
				throw new RuntimeException("No xpath in this decomposition");

			// Decomposed the decomposee node.
			String query = getArg(position, args, ruleContext).getLiteral().toString();
			List<Node> results;
			try {
				results = decompose(decomposee.getURI(), query);
			} catch (Exception e) {
				String message = "\"" + String.valueOf(e.getMessage()).replace("\n", " ") + "\"";
				submit(args, length, ruleContext, "exception", "message", message);
				return;
			}

			// Create the output statements.
			for (Node node : results) {
				Node subject = position == 1 ? node : getArg(1, args, ruleContext);
				Node predicate = position == 2 ? node : getArg(2, args, ruleContext);
				Node object = position == 3 ? node : getArg(3, args, ruleContext);

				ruleContext.add(new Triple(subject, predicate, object));
			}
		} else
			throw new BuiltinException(this, ruleContext, "Wrong parameter count");
	}
}
