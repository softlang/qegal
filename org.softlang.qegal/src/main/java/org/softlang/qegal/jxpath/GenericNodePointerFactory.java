package org.softlang.qegal.jxpath;

import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.NodePointerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by Johannes on 10.10.2017.
 */
public class GenericNodePointerFactory<T> implements NodePointerFactory {

    private final int order;

    private final Class<T> cls;

    private final Constructor constructor1;
    private final Constructor constructor2;

    public GenericNodePointerFactory(int order, Class<T> cls, Class<?> nodePointer) {
        this.order = order;
        this.cls = cls;

        try {
            constructor1 = nodePointer.getConstructor(Locale.class, cls);
            constructor2 = nodePointer.getConstructor(NodePointer.class, cls);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public NodePointer createNodePointer(QName name, Object bean, Locale locale) {

        try {
            return bean.getClass().isAssignableFrom(cls) ? (NodePointer) constructor1.newInstance(locale, bean) : null;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException();
        }
    }

    @Override
    public NodePointer createNodePointer(NodePointer parent, QName name,
                                         Object bean) {
        try {
            return bean.getClass().isAssignableFrom(cls) ? (NodePointer) constructor2.newInstance(parent, bean) : null;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException();
        }
    }

}
