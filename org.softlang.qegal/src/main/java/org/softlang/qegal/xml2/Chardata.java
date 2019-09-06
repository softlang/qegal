package org.softlang.qegal.xml2;

public class Chardata implements Content {
	private String text;
	private String parsedText;
	@Override
	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	@Override
	public String getParsedText() {
		return parsedText;
	}

	@Override
	public void setParsedText(String text) {
		parsedText = text;
	}
}
