package org.softlang.qegal.modules.emf;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.jena.graph.Node;
import org.junit.Before;
import org.junit.Test;
import org.softlang.qegal.buildins.decompose.StrJava;
import org.softlang.qegal.buildins.decompose.UriJava;
import org.softlang.qegal.modules.IModel;
import org.softlang.qegal.modules.QegalState;
import org.softlang.qegal.modules.QegalTest;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

/**
 * Tests EcoreJava.qegal
 */
public class EcoreJava extends QegalTest {

	public EcoreJava(String path, String address, IModel model, QegalState state) {
		super(path, address, model, state);
	}

	@Before
	public void setUp() throws Exception {
		// Can only be tested on beta phase where EMF mining has been applied.
		whitelist(QegalState.DONE);

		// Only simple Manifest build.
		blacklist(elementsOf(sl("Ant")).size() > 0);
		blacklist(elementsOf(sl("Pom")).size() > 0);
		whitelist(elementsOf(sl("Manifest")).size() > 0);
	}
	
	@Test
	public void countEcorePackageJava() {
		Set<String> packModels = search("java", "@model kind=\"package\"");
		packModels.removeIf(g -> !g.endsWith("_0"));
		assertEqualSize(packModels, elementsOf(sl("EcorePackageJava")));
	}
	
	@Test
	public void countEcoreFactoryJava() {
		Set<String> factoryFiles = search("java", "extends EFactory");
		factoryFiles.removeIf(g -> g.endsWith("_0"));
		Set<String> factoryInterfaces = search("java","public interface");
		Set<String> factoryModels = Sets.intersection(factoryFiles, factoryInterfaces);
		assertEqualSize(factoryModels, elementsOf(sl("EcoreFactoryJava")));
	}
	
	@Test
	public void countEcoreJavaClassifierInEcoreModels() {
		Set<String> generateds = search("java", "@generated");
		generateds.removeIf(g -> !g.endsWith("_0"));
		Set<String> atmodels = search("java", "@model");
		atmodels.removeIf(g -> !g.endsWith("_0"));
		Set<String> packModels = search("java", "@model kind=\"package\"");
		packModels.removeIf(g -> !g.endsWith("_0"));
		SetView<String> matches = Sets.intersection(generateds, atmodels);
		matches = Sets.difference(matches, packModels);
		assertEqualSize(matches, elementsOf(sl("EcoreClassifierJava")));
	}
	
	@Test
	public void ecorePackageJava() {
		Set<String> javas = elementsOf(sl("Java"));
		javas.removeIf(f -> !f.endsWith("Package.java"));
		
		for(String epackage : javas) {
			// (1) Assert that there exists one nsUri.
			
			// (2) Assert that no other in javas defining this nsUri.
			
			// (3) Assert triple element EcorePackageJava.
			
			search("java", epackage);
			List<Node> nsURIs = new UriJava().decompose(epackage, "type[1]/members/variables[name/identifier='eNS_URI']/initializer/value/value");
			if(1==nsURIs.size()) { //assumption
				assertTriple(epackage,sl("elementOf"),sl("EcorePackageJava"));
				//TODO: Need a fix for a function for UriJava
				assertEquals(nsURIs.get(0).getURI(),getObjectWith(epackage, sl("nsUri")));
				assertTriple(epackage,sl("nsUri"),nsURIs.get(0).getURI());
			}
		}
	}
	
	@Test
	public void ecoreFactoryJava() {
		Set<String> factories = elementsOf(sl("Java"));
		factories.removeIf(f -> !f.endsWith("Factory.java"));
		factories.removeIf(f -> {
			List<Node> names = new StrJava().decompose(f, "type[1]/extendedTypes[1]/name");
			return names.isEmpty() || !names.iterator().next().getLiteralLexicalForm().equals("EFactory");
		});
											  
		for(String factory : factories) {
			String folder = getObjectWith(factory, sl("partOf"));
			Set<String> epackage1 = getSubjectsWith(sl("partOf"), folder);
			Set<String> epackage2 = elementsOf(sl("EcorePackageJava"));
			SetView<String> epackages = Sets.intersection(epackage1, epackage2);
			assertEquals(1,epackages.size()); //assumption
			String epackage = epackages.iterator().next();
			String nsUri = getObjectWith(epackage, sl("nsUri"));
			assertTriple(factory,sl("elementOf"),sl("EcoreFactoryJava"));
			assertTriple(factory,sl("nsUri"),nsUri);
		}
	}
	
	@Test
	public void ecoreClassifierJava() {
		Set<String> epackages = elementsOf(sl("EcorePackageJava"));
		for(String epackage : epackages) {
			String nsUri = getObjectWith(epackage, sl("nsUri"));
			Set<String> efactories1 = elementsOf(sl("EcoreFactoryJava"));
			Set<String> efactories2 = getSubjectsWith(sl("nsUri"),nsUri);
			SetView<String> factories = Sets.intersection(efactories1, efactories2);
			assertEquals(1,factories.size()); //assumption
			String factory = factories.iterator().next();
			String folder = getObjectWith(epackage, sl("partOf"));
			Set<String> files = getSubjectsWith(sl("partOf"), folder);
			files.retainAll(elementsOf(sl("Java")));
			files.removeIf(f -> f.equals(epackage) || f.equals(factory));
			files.removeIf(f -> {
				String comment = new StrJava().decompose(f, "comments[2]").get(0).getLiteralLexicalForm();
				boolean p1 = Pattern.matches("(?s).*[\n\r].*@model.*",comment);
				boolean p2 = Pattern.matches("(?s).*[\n\r].*@generated.*",comment);
				return !(p1 && p2);
			});
			for(String file : files) {
				String classifierName = new StrJava().decompose(file, "type[1]/name/identifier").get(0).getLiteralLexicalForm();
				String uri = nsUri + "#//" + classifierName;
				assertTriple(file,sl("elementOf"),sl("EcoreClassifierJava"));
				assertTriple(file,sl("uri"),uri);
			}
		}
	}

}
