package org.softlang.qegal.buildins;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.BuiltinException;
import org.apache.jena.reasoner.rulesys.RuleContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Johannes on 11.10.2017. TODO: I dont like - remove.
 */
public class Match extends QegalBuiltin {

    @Override
    public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
        if (length < 2)
            throw new BuiltinException(this, context, "Must have at least 2 arguments to " + getName());
        String text = getString(getArg(0, args, context), context);
        String pattern = getString(getArg(1, args, context), context);
        Matcher m = Pattern.compile(pattern).matcher(text);
        if (!m.matches()) return false;
        if (length > 2) {
            // bind any capture groups
            BindingEnvironment env = context.getEnv();
            for (int i = 0; i < Math.min(length - 2, m.groupCount()); i++) {
                String gm = m.group(i + 1);
                Node match = (gm != null) ? NodeFactory.createLiteral(gm) : NodeFactory.createLiteral("");
                if (!env.bind(args[i + 2], match)) return false;
            }
        }
        return true;
    }

    /**
     * Return the lexical form of a literal node, error for other node types
     */
    protected String getString(Node n, RuleContext context) {
        if (n.isLiteral())
            return n.getLiteralLexicalForm();
        else
            return n.getURI();
    }
}
