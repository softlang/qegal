package org.softlang.qegal.engine;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;
import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;

import org.apache.commons.lang.ClassUtils;
import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.FileUtils;
import org.apache.jena.vocabulary.ReasonerVocabulary;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * Created by Johannes on 10.10.2017.
 */
public class Miner {

	public Map<Integer, List<Rule>> rules = new HashMap<Integer, List<Rule>>();
	public Map<Integer, Model> initialModels = new HashMap<Integer, Model>();

	protected void initialize(Builtin builtin) {
		// Nothing here.
	}

	private static String plain(String x, Map<String, String> namespaces) {
		if (x.startsWith("<") && x.endsWith(">"))
			return x.substring(1, x.length() - 1);
		else
			return namespaces.get(x.split(":")[0].trim()) + x.split(":")[1].trim();
	}
	

	private static String literal(String x, Map<String, String> namespaces) {
			return x.substring(1, x.length() - 1);
	}
	
	


	public final static String PREFIX = "@prefix";
	public final static String IMPORT = "import";
	public final static String INSTANCE = "instance";

	public final static String COMMENT_1 = "//";
	public final static String COMMENT_2 = "/*";
	public final static String LAYER = "layer";

	/*
	 * Special method for adding qegal files. TODO: Think of using xtext and
	 * generating code producing rule objects.
	 */
	public void addQegalFile(File file) {

		try {
			int layer = 0;
			ClassPath classpath = ClassPath.from(this.getClass().getClassLoader());
			StringBuilder content = new StringBuilder();
			BuiltinRegistry registry = new MapBuiltinRegistry();
			Map<String, String> namespaces = new HashMap<>();
			// Map imports into a builting registy and instance definitions into triples.
			for (String line : Files.readLines(file, Charsets.UTF_8)) {
				// Comments are ignored. TODO: This is currently not correct and needs to be
				// ecaped propery.
				if (line.startsWith(LAYER)) {
					layer = Integer.valueOf(line.substring(6).trim());
					continue;
				}
				if (line.startsWith(COMMENT_1) || line.startsWith(COMMENT_2))
					continue;
				// Namespaces are added to namespace and passed on to jena.
				else if (line.startsWith(PREFIX)) {
					String prefixAndNs = line.substring(PREFIX.length(), line.length()).trim();
					String prefixAndNsWitoutDot = prefixAndNs.substring(0, prefixAndNs.length() - 1).trim();
					String prefix = prefixAndNsWitoutDot.split(":", 2)[0].trim();
					String ns = prefixAndNs.split(":", 2)[1].trim();
					String nsWithoutEdges = ns.substring(1, ns.length() - 2);

					namespaces.put(prefix, nsWithoutEdges);
					content.append(line + "\n");
				} else if (line.startsWith(IMPORT)) {
					String importStmt = line.substring(IMPORT.length(), line.length()).trim();

					if (importStmt.endsWith("*")) {
						// Adding all classes of this package to the namespace.
						String packageName = importStmt.substring(0, importStmt.length() - 2);
						ImmutableSet<ClassInfo> classes = classpath.getTopLevelClasses(packageName);
						for (ClassInfo classInfo : classes) {
							Class<?> cls = Class.forName(classInfo.getName());
							if (ClassUtils.isAssignable(cls, Builtin.class)
									&& !Modifier.isAbstract(cls.getModifiers())) {
								Builtin builtin = (Builtin) cls.getConstructor().newInstance();
								initialize(builtin);
								registry.register(builtin.getClass().getSimpleName(), builtin);
							}
						}

					} else {
						Class<?> cls = Class.forName(importStmt);
						if (ClassUtils.isAssignable(cls, Builtin.class) && !Modifier.isAbstract(cls.getModifiers())) {
							Builtin builtin = (Builtin) cls.getConstructor().newInstance();
							initialize(builtin);
							registry.register(builtin.getClass().getSimpleName(), builtin);
						}
					}
				}
				// Instances are added and not passed to jena.
				else if (line.startsWith(INSTANCE)) {
					String triple = line.substring(INSTANCE.length(), line.length()).trim();
					String tripleWithoutDot = triple.substring(0, triple.length() - 1).trim();
					String tripleWithoutBraces = tripleWithoutDot.substring(1, tripleWithoutDot.length() - 1).trim();
					String subject = tripleWithoutBraces.split(",")[0].trim();
					String predicate = tripleWithoutBraces.split(",")[1].trim();
					String object = tripleWithoutBraces.split(",")[2].trim();

					if (!object.contains("\""))
						addTriple(layer, plain(subject, namespaces), plain(predicate, namespaces),
								plain(object, namespaces));
					else 
						addTripleWithLiteral(layer, plain(subject, namespaces), plain(predicate, namespaces),
								literal(object,namespaces));
				} else
					content.append(line + "\n");
			}

			// Parse rest of the file using default jena parser.
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new ByteArrayInputStream(content.toString().getBytes())));

			// Naming and adding unnamed rules.
			int index = 0;
			String fileid = file.getName().substring(0, file.getName().length() - 3);
			for (Rule rule : Rule.parseRules(Rule.rulesParserFromReader(br, registry)))
				if (rule.getName() == null)
					addRule(layer, new Rule(fileid + "_" + String.valueOf(index++), rule.getHead(), rule.getBody()));
				else
					addRule(layer, rule);

		} catch (IOException | ClassNotFoundException | NoSuchMethodException | IllegalAccessException
				| InvocationTargetException | InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	public void addTriple(int layer, String subject, String predicate, String object) {
		Model model = initialModels.computeIfAbsent(layer, x -> ModelFactory.createDefaultModel());
		model.add(model.createResource(subject), model.createProperty(predicate), model.getResource(object));
	}

	public void addTripleWithLiteral(int layer, String subject, String predicate, String object) {
		Model model = initialModels.computeIfAbsent(layer, x -> ModelFactory.createDefaultModel());
		model.add(model.createResource(subject), model.createProperty(predicate), model.createLiteral(object));
	}

	public void addRule(int layer, Rule rule) {
		List<Rule> list = rules.computeIfAbsent(layer, ArrayList::new);
		list.add(rule);
	}

	public void addFile(File file) {
		try {
			switch (Files.getFileExtension(file.getName())) {
			case "qegal":
				addQegalFile(file);
				break;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Adds all qegal files in a folder.
	 * 
	 * @param folder
	 */
	public void addFolder(File folder) {
		for (File file : folder.listFiles())
			addFile(file);
	}

	public Model execute() {
		Model current = ModelFactory.createDefaultModel();
		TreeSet<Integer> sorted = new TreeSet<Integer>(Sets.union(initialModels.keySet(), rules.keySet()));
		for (Integer layer : sorted) {
			// Add initial.
			if (initialModels.containsKey(layer))
				current.add(initialModels.get(layer));

			if (rules.containsKey(layer)) {
				Reasoner reasoner = new GenericRuleReasoner(rules.get(layer));
				reasoner.setParameter(ReasonerVocabulary.PROPderivationLogging, false);
				reasoner.setParameter(ReasonerVocabulary.PROPtraceOn, false);

				current = ModelFactory.createInfModel(reasoner, current);
			}
		}
		return current;
	}
}
