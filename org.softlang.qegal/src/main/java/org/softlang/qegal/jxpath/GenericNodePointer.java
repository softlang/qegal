package org.softlang.qegal.jxpath;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.compiler.NodeNameTest;
import org.apache.commons.jxpath.ri.compiler.NodeTest;
import org.apache.commons.jxpath.ri.compiler.NodeTypeTest;
import org.apache.commons.jxpath.ri.model.NodeIterator;
import org.apache.commons.jxpath.ri.model.NodePointer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by Johannes on 05.10.2017.
 */
public abstract class GenericNodePointer<T, C> extends NodePointer {

    public T content;

    public GenericNodePointer(Locale locale, T content) {
        super(null, locale);
        this.content = content;
    }

    public GenericNodePointer(NodePointer parent, T content) {
        super(parent);
        this.content = content;
    }

    public boolean testNode(NodeTest test) {
        return testNode(test, getName());
    }

    public boolean testNode(NodeTest test, QName name) {
        if (test == null) {
            return true;
        }
        if (test instanceof NodeNameTest) {
            NodeNameTest nodeNameTest = (NodeNameTest) test;
            if (nodeNameTest.getNamespaceURI() != null)
                throw new RuntimeException();

            boolean wildcard = nodeNameTest.isWildcard();

            QName testName = nodeNameTest.getNodeName();
            String testPrefix = testName.getPrefix();
            if (wildcard && testPrefix == null) {
                return true;
            }
            if (wildcard || testName.getName().equals(name.getName()))
                return true;

            return false;
        }

        if (test instanceof NodeTypeTest) {
            switch (((NodeTypeTest) test).getNodeType()) {
                case Compiler.NODE_TYPE_NODE:
                    return true;
                default:
                    return false;
            }
        }
        return false;
    }

    public NodeIterator childIterator(NodeTest test, boolean reverse, NodePointer startWith) {
        List<C> filteredChildren = children().stream().filter(x -> testNode(test, getChildName(x))).collect(Collectors.toList());
        return new GenericNodeIterator<C>(this, filteredChildren, reverse, startWith);
    }

    public NodeIterator attributeIterator(QName query) {
        List<Object> filteredAttributes = attributes().entrySet().stream().filter(entry ->
                "*".equals(query.getName()) || query.getName().equals(entry.getKey().getName())
        ).map(x -> x.getValue()).collect(Collectors.toList());

        return new GenericNodeIterator<Object>(this, filteredAttributes, false, null);
    }

    public abstract QName getChildName(C t);

    public abstract List<C> children();

    public abstract Map<QName, Object> attributes();

    public int compareChildNodePointers(NodePointer pointer1, NodePointer pointer2) {
        int index1 = children().indexOf(pointer1.getBaseValue());
        int index2 = children().indexOf(pointer2.getBaseValue());

        return Math.min(-1, Math.max(1, Integer.compare(index1, index2)));
    }

    public Object getBaseValue() {
        return content;
    }

    public Object getImmediateNode() {
        return content;
    }

    public boolean isActual() {
        return true;
    }

    public boolean isCollection() {
        return false;
    }

    public int getLength() {
        return 1;
    }

    public boolean isLeaf() {
        return children().size() == 0;
    }

    @Override
    abstract public QName getName();

    public void setValue(Object value) {
        throw new RuntimeException("This is not intended");
    }

    public NodePointer createChild(JXPathContext context, QName name, int index) {
        throw new RuntimeException("This is not intended");
    }

    public NodePointer createChild(JXPathContext context, QName name,
                                   int index, Object value) {
        throw new RuntimeException("This is not intended");
    }

    public NodePointer createAttribute(JXPathContext context, QName name) {
        throw new RuntimeException("This is not intended");
    }

    public void remove() {
        throw new RuntimeException("This is not intended");
    }

    public String asPath() {

        StringBuffer buffer = new StringBuffer();
        if (parent != null) {
            buffer.append(parent.asPath());
        }
        buffer.append("/");
        buffer.append(getName().getName());

        return buffer.toString();
    }


    public int hashCode() {
        return content.hashCode();
    }

    public boolean equals(Object object) {
        return object == this || object instanceof GenericNodePointer && content == ((GenericNodePointer) object).content;
    }

    public Object getValue() {
        return content.toString();
    }

    public String getNamespaceURI() {
        throw new NotImplementedException();
    }

    public NodePointer namespacePointer(String prefix) {
        throw new NotImplementedException();
    }

    public NodeIterator namespaceIterator() {
        throw new NotImplementedException();
    }

    public String getNamespaceURI(String prefix) {
        throw new NotImplementedException();
    }

    public String getDefaultNamespaceURI() {
        throw new NotImplementedException();
    }

    public Pointer getPointerByID(JXPathContext context, String id) {
        throw new NotImplementedException();
    }
}
