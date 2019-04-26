package org.softlang.qegal.xml2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.lang.CharUtils;
import org.apache.commons.lang.StringUtils;
import org.softlang.qegal.xml2.XMLParser.AttributeContext;
import org.softlang.qegal.xml2.XMLParser.ChardataContext;
import org.softlang.qegal.xml2.XMLParser.DocumentContext;
import org.softlang.qegal.xml2.XMLParser.ElementContext;
import org.softlang.qegal.xml2.XMLParser.PrologContext;

public class XML2Parser {

	public static void main(String[] args) throws FileNotFoundException, IOException {
		parse(new FileInputStream(new File("pom.xml")));
	}

	public static Document parse(InputStream in) throws IOException {

		ANTLRInputStream inputStream = new ANTLRInputStream(in);
		XMLLexer lexer = new XMLLexer(inputStream);
		CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		XMLParser parser = new XMLParser(tokenStream);
		DocumentContext cst = parser.document();

		return transform(cst);
	}

	static Document transform(DocumentContext x) {
		Document result = new Document();
		if (x.prolog() != null)
			result.setProlog(transform(x.prolog()));

		if (x.element() != null)
			result.setElement(transform(x.element()));

		return result;
	}

	static Element transform(ElementContext element) {
		Element result = new Element();
		result.setName(element.Name(0).getText());
		
		for (ParseTree child : element.attribute()) {
			result.getAttributes().add(transform((AttributeContext)child));
		}
		
		if (element.content() != null) {
			for (ParseTree child : element.content().children) {
				if (child instanceof ChardataContext) {
					result.getContent().add(transform((ChardataContext) child));
				} else if (child instanceof ElementContext) {
					result.getContent().add(transform((ElementContext) child));
//				} else if (child instanceof ReferenceContext) {
//					// ...
				} else {
					result.getContent().add(transform(child));
				}
			}
		}

		return result;
	}

	static Attribute transform(AttributeContext child) {
		Attribute result = new Attribute();
		result.setName(child.Name().getText());
		result.setValue(StringUtils.strip(child.value().getText(), "\""));
		return result;
	}
	
	static Chardata transform(ChardataContext child) {
		Chardata result = new Chardata();
		result.setText(child.getText());
		return result;
	}

	static UnhandledElement transform(ParseTree child) {
		UnhandledElement result = new UnhandledElement();
		result.setType(child.getClass().getSimpleName());
		result.setText(child.getText());
		return result;
	}

	static String transform(PrologContext x) {
		return x.getText();
	}
}
