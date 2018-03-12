package org.softlang.qegal.utils;

import com.google.common.collect.Lists;

/**
 * Created by Johannes on 24.11.2017.
 */
public class Sandbox {

    public static String CONTENT = "org.eclipse.core.runtime,\n" +
            " org.eclipse.core.resources;visibility:=reexport,\n" +
            " org.softlang.metalib.emf.fsml.edit;visibility:=reexport,\n" +
            " org.eclipse.emf.ecore.xmi;visibility:=reexport,\n" +
            " org.eclipse.emf.edit.ui;visibility:=reexport,\n" +
            " org.eclipse.ui.ide;visibility:=reexport";

    public static void main(String[] args) {
        System.out.println(CONTENT);
        String out = CONTENT.replaceAll("(;[^,]*)|\\s", "");
        System.out.println(out);

        String[] split = out.split(",");

        System.out.println(Lists.newArrayList(split));
    }
}
