package org.softlang.qegal.xml2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.antlr.v4.runtime.tree.ParseTree;

public class Element implements Content {

	private List<Content> content;

	private List<Attribute> attributes;

	private Map<String, List<Element>> c;

	private Map<String, String> a;

	private String name;

	public Map getC() {
		if (c == null)
			c = content.stream().filter(x -> x instanceof Element).map(x -> ((Element) x))
					.collect(Collectors.groupingBy(x -> x.name));

		return c;
	}

	public Map getA() {
		if (a == null) {
			Map<String, String> a = new HashMap<>();
			for (Attribute x : attributes)
				a.put(x.getName(), x.getValue());
		}

		return a;
	}

	public List<Content> getContent() {
		if (content == null)
			content = new ArrayList<>();
		return content;
	}

	public List<Attribute> getAttributes() {
		if (attributes == null)
			attributes = new ArrayList<>();
		return attributes;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getText() {
		if (content == null)
			return "";
		StringBuilder result = new StringBuilder();
		for (Content child : content)
			if (child instanceof Chardata)
				result.append(((Chardata) child).getText());

		return result.toString();
	}
}
