package org.softlang.qegal.buildins;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.RuleContext;

/**
 * Created by Johannes on 21.06.2017.
 */
public class Split extends QegalBuiltin {
    // Todo: merge with fragments.
    @Override
    public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
        BindingEnvironment env = context.getEnv();
        String path = getArg(0, args, context).getURI();
        String split = getArg(1, args, context).getURI();
        String[] fragments = path.split(split);

        if (args.length > fragments.length + 2) return false;

        String prefix = fragments[0];
        for (int i = 2; i < fragments.length - args.length + 3; i++)
            prefix = prefix + "/" + fragments[i];

        if (!env.bind(getArg(2, args, context), NodeFactory.createLiteral(prefix)))
            return false;

        for (int i = 3; i < args.length; i++)
            if (!env.bind(getArg(i, args, context),
                    NodeFactory.createLiteral(fragments[fragments.length - args.length + i])))
                return false;

        return true;
    }
}
