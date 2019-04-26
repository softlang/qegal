package org.softlang.qegal.xml2;

public class Document {
	private String prolog;
	
	private Element element;

	public void setProlog(String prolog) {
		this.prolog = prolog;
	}
	
	public String getProlog() {
		return prolog;
	}

	public void setElement(Element element) {
		this.element = element;
	}

	public Element getElement() {
		return element;
	}
}
