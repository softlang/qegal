package org.softlang.qegal.buildins.string;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.softlang.qegal.buildins.QegalBuiltin;

/**
 * Created by Johannes on 24.11.2017.
 */
public class SplitToUri extends QegalBuiltin {

    @Override
    public void trackedHeadAction(Node[] args, int length, RuleContext context) {
        // This is the node that will be decomposed.
        String decomposee = getString(getArg(0, args, context));

        // Determines the position of the xpath.
        int position;
        if (getArg(1, args, context).isLiteral()) position = 1;
        else if (getArg(2, args, context).isLiteral()) position = 2;
        else if (getArg(3, args, context).isLiteral()) position = 3;
        else throw new RuntimeException("No regex in this split");

        // Decomposed the decomposee node.
        String regex = getArg(position, args, context).getLiteral().toString();

        for (String node : decomposee.split(regex)) {
            Node subject = position == 1 ? NodeFactory.createURI(node) : getArg(1, args, context);
            Node predicate = position == 2 ? NodeFactory.createURI(node) : getArg(2, args, context);
            Node object = position == 3 ? NodeFactory.createURI(node) : getArg(3, args, context);

            context.add(new Triple(subject, predicate, object));
        }
    }

    private String getString(Node n) {
        if (n.isLiteral())
            return n.getLiteralLexicalForm();
        else
            return n.getURI();
    }
}
