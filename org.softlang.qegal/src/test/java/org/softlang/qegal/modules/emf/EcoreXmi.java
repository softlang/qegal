package org.softlang.qegal.modules.emf;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.List;
import java.util.Set;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.graph.Node;
import org.junit.Before;
import org.junit.Test;
import org.softlang.qegal.buildins.decompose.DecXml;
import org.softlang.qegal.buildins.decompose.StrXml;
import org.softlang.qegal.buildins.decompose.UriXml;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.modules.IModel;
import org.softlang.qegal.modules.QegalState;
import org.softlang.qegal.modules.QegalTest;

public class EcoreXmi extends QegalTest {

	public EcoreXmi(String path, String address, IModel model, QegalState state) {
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
	public void countEcoreFiles() {
		assertEqualSize(search("ecore",""), elementsOf(sl("Ecore")));
	}
	
	@Test
	public void countEcoreClassifierXMI() {
		assertEqualSize(search("ecore", "<eClassifiers"), elementsOf(sl("EcoreClassifierXMI")));
	}
	
	@Test
	public void countEcoreXMIPackageInEcoreModels() {
		Set<String> parentPackages = search("ecore", "<ecore:EPackage");
		Set<String> subPackages = search("ecore", "<eSubpackages");
		assertEqualSize(Sets.union(parentPackages, subPackages), elementsOf(sl("EcorePackageXMI")));
	}
	
	@Test
	public void ecoreFiles() {
		Set<String> xs = getSubjectsWith(sl("manifestsAs"), sl("File"));
		xs.removeIf(x -> !new File(x).getAbsolutePath().endsWith(".ecore"));
		xs.retainAll(elementsOf(sl("XML")));
		for(String x : xs) {
			assertTriple(x,sl("elementOf"),sl("Ecore"));
		}
	}
	
	@Test
	public void ecorePackageXMIParent() {
		for(String x : elementsOf(sl("Ecore"))) {
			List<Node> epackages = new DecXml().decompose(x, "/ecore:EPackage");
			assertEquals(1,epackages.size());
			String epackage = epackages.get(0).getURI();
			assertTriple(epackage,sl("partOf"),x);
			assertTriple(epackage,sl("elementOf"),sl("EcorePackageXMI"));
		}
	}

	@Test
	public void ecorePackageXMISub() {
		for(String x : elementsOf(sl("EcorePackageXMI"))) {
			new DecXml().decompose(x, "/eSubpackages").forEach(subpackage -> {
				assertTriple(subpackage.getURI(),sl("partOf"),x);
				assertTriple(subpackage.getURI(),sl("elementOf"),sl("EcorePackageXMI"));
			});
		}
	}
	
	@Test
	public void ecorePackageXMINSUriPrefix() {
		for(String x : elementsOf(sl("EcorePackageXMI"))) {
			List<Node> nsURIs = new UriXml().decompose(x, "/@nsURI");
			assertEquals(1,nsURIs.size());
			String nsUri = nsURIs.get(0).getURI();
			assertTriple(x,sl("nsUri"),nsUri);
			List<Node> nsPrefixes = new StrXml().decompose(x, "/@nsPrefix");
			assertEquals(1,nsPrefixes.size());
			String nsPrefix = nsPrefixes.get(0).getURI();
			assertTriple(x,sl("nsPrefix"),nsPrefix);
		}
	}
	
	@Test
	public void ecoreClassifierXMI() {
		for(String x : elementsOf(sl("EcorePackageXMI"))) {
			new DecXml().decompose(x, "/eClassifiers").forEach(classifier -> {
				assertTriple(classifier.getURI(),sl("partOf"),x);
				assertTriple(classifier.getURI(),sl("elementOf"),sl("EcoreClassifierXMI"));
			});
		}
	}
	
	@Test
	public void ecoreClassifierUris() {
		for(String classifier : elementsOf("EcoreClassifierXMI")) {
			String epackage = getObjectWith(classifier, sl("partOf"));
			String nsUri = getObjectWith(epackage, sl("nsUri"));
			List<Node> classifiernames = new StrXml().decompose(classifier, "/@name");
			assertEquals(1,classifiernames.size());
			String classifiername = classifiernames.get(0).getLiteralLexicalForm();
			String uri = nsUri + "#//" + classifiername;
			assertTriple(classifier,sl("uri"),uri);
			assertTriple(classifier,sl("nsUri"),nsUri);
			assertTriple(classifier,sl("name"),classifiername);
		}
	}

}
