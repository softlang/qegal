package org.softlang.qegal.jxpath;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

import java.util.*;

/**
 * Created by Johannes on 09.10.2017.
 */
public class OptionalNodePointer extends GenericNodePointer<Optional<?>, Object> {

    public OptionalNodePointer(Locale locale, Optional<?> content) {
        super(locale, content);
    }

    public OptionalNodePointer(NodePointer parent, Optional<?> content) {
        super(parent, content);
    }

    @Override
    public QName getChildName(Object t) {
        return new QName(null, "value");
    }

    @Override
    public List<Object> children() {
        return Collections.singletonList(content.get());
    }

    @Override
    public Map<QName, Object> attributes() {
        return Collections.emptyMap();
    }

    @Override
    public QName getName() {
        return null;
    }
}

