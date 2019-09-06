package org.softlang.qegal.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.Syntax;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.softlang.qegal.jutils.JUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.io.Files;

/**
 * Created by Johannes on 22.11.2017.
 * Everything that can be used for measuring and inspecting jena models.
 */
public class QegalUtils {

    public static String NAMESPACE = "http://org.softlang.com/";

    public static String ELEMENT_OF = NAMESPACE + "elementOf";

    public static String DEPENDS_ON = NAMESPACE + "dependsOn";

    public static String DEC_OCCURS = NAMESPACE + "decOccurs";

   
    // TODO: Replace somehow.
    public static Map<String, Integer> countPredicate(Model model, String subject, String predicate, String object) {
        Multiset<String> result = HashMultiset.create();

        Resource s = subject == null ? null : model.getResource(subject);
        Property p = predicate == null ? null : model.getProperty(predicate);
        RDFNode o = object == null ? null : model.getResource(predicate);

        for (Statement statement : Lists.newArrayList(model.listStatements(s, p, o)))
            result.add(statement.getPredicate().getURI().toString());

        return result.entrySet().stream().collect(Collectors.toMap(x -> x.getElement(), x -> x.getCount()));
    }

    public static void print(Model model, String subject, String predicate, String object) {
        Resource s = subject == null ? null : model.getResource(subject);
        Property p = predicate == null ? null : model.getProperty(predicate);
        RDFNode o = object == null ? null : model.getResource(object);

        for (Statement statement : Lists.newArrayList(model.listStatements(s, p, o)))
            System.out.println(statement);
    }

    public static Property predicate(Model model, String localName) {
        return model.createProperty(NAMESPACE, localName);
    }

    public static Table<Resource, Property, RDFNode> statements(Model model, Resource subject, Property predicate, RDFNode object) {
        ImmutableTable.Builder<Resource, Property, RDFNode> builder = ImmutableTable.builder();

        for (Statement statement : model.listStatements(subject, predicate, object).toList())
            builder.put(statement.getSubject(), statement.getPredicate(), statement.getObject());

        return builder.build();
    }

    public static Set<Set<RDFNode>> connectedComponents(Model model, Set<RDFNode> nodes, Property edge) {
        Map<RDFNode, Set<RDFNode>> edges = new HashMap<>();

        for (Statement statement : model.listStatements(null, edge, (RDFNode) null).toList())
            // Adding connections between subjects and objects.
            if (nodes.contains(statement.getSubject()) && nodes.contains(statement.getObject())) {
                edges.computeIfAbsent(statement.getSubject(), x -> new HashSet<>()).add(statement.getObject());
                edges.computeIfAbsent(statement.getObject(), x -> new HashSet<>()).add(statement.getSubject());
            }

        Set<RDFNode> notVisited = new HashSet<>(nodes);
        Set<Set<RDFNode>> results = new HashSet<>();

        while (!notVisited.isEmpty()) {
            // To search and add to result.
            Set<RDFNode> border = Sets.newHashSet(notVisited.iterator().next());
            Set<RDFNode> component = new HashSet<>();
            while (!border.isEmpty()) {
                RDFNode node = border.iterator().next();
                border.remove(node);
                Set<RDFNode> connected = edges.getOrDefault(node, Collections.emptySet());
                component.add(node);
                // Add all elements to border that are not already contained in the component.
                border.addAll(Sets.difference(connected, component));
            }
            // Adding result and updating visited.
            results.add(component);
            notVisited.removeAll(component);
        }

        return results;
    }
    
    public static Set<String> getSubjectsWith(Model model, String property){
    	Property p = model.getProperty(property);
    	return model.listSubjectsWithProperty(p).mapWith(x -> x.getURI()).toSet();
    }
    
    public static Set<String> getSubjectsWith(Model model, String property, String object){
    	Property p = model.getProperty(property);
        RDFNode o = model.getResource(object);

        return model.listStatements(null, p, o).mapWith(x -> x.getSubject().getURI()).toSet();
    }
    
    public static String getSubjectWith(Model model, String property, String object){
    	Property p = model.getProperty(property);
        Resource o = model.getResource(object);
        List<String> results = model.listStatements(null, p, o).mapWith(x -> x.getSubject().getURI()).toList();
        if(results.size()>1)
        	throw new RuntimeException("More than one result found!");
        return results.get(0);
    }
    
    public static Set<String> getObjectsWith(Model model, String subject, String property){
    	Property p = model.getProperty(property);
        Resource s = model.getResource(subject);
        return model.listObjectsOfProperty(s, p).mapWith(o -> o.asResource().getURI()).toSet();
    }
    
    public static String getObjectWith(Model model, String subject, String property){
    	Property p = model.getProperty(property);
        Resource s = model.getResource(subject);
        List<String> results = model.listObjectsOfProperty(s, p).mapWith(o -> o.asResource().getURI()).toList();
        if(results.size()>1)
        	throw new RuntimeException("More than one result found!");
        return results.get(0);
    }

    public static Set<String> elementsOf(Model model, String language) {
        Property p = model.getProperty(ELEMENT_OF);
        RDFNode o = model.getResource(language);

        return model.listStatements(null, p, o).mapWith(x -> x.getSubject().getURI()).toSet();
    }

    public static void write(Model model, File file) {
        try {
            model.write(new FileWriter(file), "TTL");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void addTriple(Model model, String subject, String predicate, String object) {
        model.add(model.createResource(subject), model.createProperty(predicate), model.getResource(object));
    }

    public static String uri(File file) {
        return file.getAbsolutePath().replace("\\", "/");
    }
    
    public static Set<String> query(Model model, String querytext){
		Set<String> results = new HashSet<>();
		Query query = QueryFactory.create(querytext, Syntax.syntaxARQ);
		try (QueryExecution qe = QueryExecutionFactory.create(query, model)) {
			qe.execSelect().forEachRemaining(r -> results.add(r.get("?x").toString()));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return results;
	}

}
