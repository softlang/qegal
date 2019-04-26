package org.softlang.qegal.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.reasoner.rulesys.BindingEnvironment;

import com.google.common.collect.Lists;

/**
 * Created by Johannes on 24.11.2017.
 */
public class Sandbox {

//    public static String CONTENT = "org.eclipse.core.runtime,\n" +
//            " org.eclipse.core.resources;visibility:=reexport,\n" +
//            " org.softlang.metalib.emf.fsml.edit;visibility:=reexport,\n" +
//            " org.eclipse.emf.ecore.xmi;visibility:=reexport,\n" +
//            " org.eclipse.emf.edit.ui;visibility:=reexport,\n" +
//            " org.eclipse.ui.ide;visibility:=reexport";
//
//    public static void main(String[] args) {
//        System.out.println(CONTENT);
//        String out = CONTENT.replaceAll("(;[^,]*)|\\s", "");
//        System.out.println(out);
//
//        String[] split = out.split(",");
//
//        System.out.println(Lists.newArrayList(split));
//    }

	public static void main(String[] args) {
		//
//		Matcher m = Pattern.compile("(.*?)(QT3000)(.*)").matcher("This order was placed for QT3000! OK?");
		Matcher m = Pattern.compile("(.*?)(#//)(.*)").matcher("Fsml.ecore#//FSM");

		// bind any capture groups
		System.out.println(m.groupCount());

		m.find();
		for (int i = 0; i < m.groupCount(); i++) {
			String gm = m.group(i + 1);
			System.out.println(gm);
		}

	}

}
