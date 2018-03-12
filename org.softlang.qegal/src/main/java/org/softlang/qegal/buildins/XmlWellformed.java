package org.softlang.qegal.buildins;

import com.google.common.io.Files;
import com.ximpleware.*;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.softlang.qegal.io.IOLayer;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * <pre>
 *  <b> Body: XmlWellformed(file)</b>
 * </pre>
 * 
 * Ref to improve parsing: https://vtd-xml.sourceforge.io/codeSample/cs1.html
 */
public class XmlWellformed extends QegalBuiltin {
	// TODO: ReplaceAll:
	@Override
	public int getArgLength() {
		return 1;
	}

	@Override
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) throws Exception{
		try {
			String url = getArg(0, args, context).getURI();
			byte[] bytes = IOUtils.toByteArray(iolayer.access(url));

			VTDGen vg = new VTDGen();
			vg.setDoc(bytes);
			vg.parse(true);
		} catch (ParseException e) {
			return false;
		}

		return true;
	}

}
