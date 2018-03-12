package org.softlang.qegal.jxpath;

import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;

import java.util.List;

/**
 * Created by Johannes on 05.10.2017.
 */
public class GenericNodeIterator<T> implements NodeIterator {

    private final NodePointer parent;
    private final List<T> nodes;
    int position = 0;

    public GenericNodeIterator(NodePointer parent, List<T> nodes, boolean reverse, NodePointer startWith) {
        this.nodes = nodes;
        this.parent = parent;
    }

    @Override
    public int getPosition() {
        return position;
    }

    @Override
    public boolean setPosition(int position) {
        this.position = position;
        return position > 0 && position <= nodes.size();
    }

    @Override
    public NodePointer getNodePointer() {
        if (nodes.isEmpty()) return null;
        else return NodePointer.newChildNodePointer(parent, null, nodes.get(position - 1));
    }
}
