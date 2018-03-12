package org.softlang.qegal.buildins.string;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.BuiltinException;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.softlang.qegal.buildins.QegalBuiltin;

/**
 * Created by Johannes on 24.11.2017.
 */
public class ReplaceAllToUri extends QegalBuiltin {

    @Override
    public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
        if (length != 4)
            throw new BuiltinException(this, context, "Must have 4 arguments " + getName());

        String in = getString(getArg(0, args, context));
        String pattern = getString(getArg(1, args, context));
        String replace = getString(getArg(2, args, context));

        BindingEnvironment env = context.getEnv();
        return env.bind(getArg(3, args, context), NodeFactory.createURI(in.replaceAll(pattern, replace)));
    }

    private String getString(Node n) {
        if (n.isLiteral())
            return n.getLiteralLexicalForm();
        else
            return n.getURI();
    }
}
