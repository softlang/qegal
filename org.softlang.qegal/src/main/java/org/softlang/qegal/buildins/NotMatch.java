package org.softlang.qegal.buildins;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;
import org.apache.jena.reasoner.rulesys.BuiltinException;
import org.apache.jena.reasoner.rulesys.RuleContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Johannes on 11.10.2017. TODO: This builtin is not necessary but I forgot why! I dont like - remove.
 */
public class NotMatch extends QegalBuiltin{

    @Override
    public boolean trackedBodyCall(Node[] args, int length, RuleContext context) {
        if (length != 2)
            throw new BuiltinException(this, context, "Must have 2 arguments to " + getName());
        String text = getString( getArg(0, args, context), context );
        String pattern = getString( getArg(1, args, context), context );
        Matcher m = Pattern.compile(pattern).matcher(text);
        if ( ! m.matches()) return true;
        return false;
    }

    /**
     * Return the lexical form of a literal node, error for other node types
     */
    protected String getString(Node n, RuleContext context) {
        if (n.isLiteral()) {
            return n.getLiteralLexicalForm();
        } else {
            return n.getURI();
        }
    }
}
