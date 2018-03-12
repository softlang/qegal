package org.softlang.qegal.buildins;

import com.google.common.io.Files;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;

/**
 * <pre>
 *  <b> Body: Children(uri, rest, part_1, ... part_n)</b>
 * </pre>
 * 
 * <p>
 * Decomposes the uri into fragments separated by slash. The last 1 to n
 * fragments are bound to the parameters part 1 to n. The first fragments are
 * bound to the rest parameter.
 * </p>
 */
public class Children extends QegalBuiltin {

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
		BindingEnvironment env = context.getEnv();
		String path = getArg(0, args, context).getURI();
		String[] fragments = path.split("/");

		if (args.length > fragments.length + 1)
			return false;

		String prefix = fragments[0];
		for (int i = 1; i < fragments.length - args.length + 2; i++)
			prefix = prefix + "/" + fragments[i];

		// I think the prefix is always an uri.
		if (!env.bind(getArg(1, args, context), NodeFactory.createURI(prefix)))
			return false;

		// The rest of the parameters represents path fragments as literals.
		for (int i = 2; i < args.length; i++)
			if (!env.bind(getArg(i, args, context),
					NodeFactory.createLiteral(fragments[fragments.length - args.length + i])))
				return false;

		return true;
	}
}
