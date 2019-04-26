package org.softlang.qegal.xml2;

public class UnhandledElement implements Content {
	private String type;
	private String text;

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
}
