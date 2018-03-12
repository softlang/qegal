package org.softlang.qegal.jxpath;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.jxpath.ri.QName;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.softlang.qegal.io.IOLayer;
import java.io.File;
import java.util.*;

/**
 * Created by Johannes on 05.10.2017.
 */
public class IONodePointer extends GenericNodePointer<IONode, IONode> {

	public IONodePointer(Locale locale, IONode content) {
		super(locale, content);
	}

	public IONodePointer(NodePointer parent, IONode content) {
		super(parent, content);
	}

	@Override
	public QName getChildName(IONode t) {
		return new QName(null, t.uri.substring(t.uri.lastIndexOf("/") + 1));
	}

	@Override
	public List<IONode> children() {
		if (content.iolayer.isDirectory(content.uri))
			return Lists.newArrayList(
					Iterables.transform(content.iolayer.children(content.uri), x -> new IONode(x, content.iolayer)));
		else
			return Collections.emptyList();
	}

	@Override
	public Map<QName, Object> attributes() {
		Map<QName, Object> result = new HashMap<>();
		result.put(new QName(null, "extension"), Files.getFileExtension(content.iolayer.extension(content.uri)));
		return result;
	}

	@Override
	public QName getName() {
		return new QName(null, content.uri.substring(content.uri.lastIndexOf("/") + 1));
	}

}
