package org.softlang.qegal.jxpath;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;

import java.io.File;
import java.util.*;

/**
 * Created by Johannes on 05.10.2017.
 */
public class FileNodePointer extends GenericNodePointer<File, File> {

    public FileNodePointer(Locale locale, File content) {
        super(locale, content);
    }

    public FileNodePointer(NodePointer parent, File content) {
        super(parent, content);
    }

    @Override
    public QName getChildName(File t) {
        return new QName(null, t.getName());
    }

    @Override
    public List<File> children() {
        if (content.isDirectory())
            return Lists.newArrayList(content.listFiles());
        else
            return Collections.emptyList();
    }

    @Override
    public Map<QName, Object> attributes() {
        Map<QName, Object> result = new HashMap<>();
        result.put(new QName(null, "extension"), Files.getFileExtension(content.getName()));
        return result;
    }

    @Override
    public QName getName() {
        return new QName(null, content.getName());
    }
}
