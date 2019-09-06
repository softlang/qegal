package org.softlang.qegal.xml2;

public class UnhandledElement implements Content {
	private String type;
	private String text;
	private String parsedText;

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

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
