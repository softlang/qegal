package org.softlang.qegal.xml2;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.softlang.qegal.xml2.XMLParser.DocumentContext;

public class XML2Parser {
	 public static void parse(InputStream in) throws IOException {
		 
		 ANTLRInputStream  inputStream = new ANTLRInputStream(in);
		 XMLLexer lexer = new XMLLexer(inputStream);
		 CommonTokenStream tokenStream = new CommonTokenStream(lexer);
		 XMLParser parser = new XMLParser(tokenStream);
		 
		 DocumentContext cst = parser.document();
		 
		 // TODO: Continue this.
//	     var ast = new BuildAstVisitor().VisitCompileUnit(cst);
//	     var value = new EvaluateExpressionVisitor().Visit(ast);

	 }
}
