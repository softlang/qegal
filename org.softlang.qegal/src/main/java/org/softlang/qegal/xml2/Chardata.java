package org.softlang.qegal.xml2;

public class Chardata implements Content {
	private String text;

	@Override
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
