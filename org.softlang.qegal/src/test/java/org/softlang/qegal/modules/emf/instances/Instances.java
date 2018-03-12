package org.softlang.qegal.modules.emf.instances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.jena.ext.com.google.common.collect.Sets;
import org.apache.jena.ext.com.google.common.collect.Sets.SetView;
import org.apache.jena.graph.Node;
import org.junit.Before;
import org.junit.Test;
import org.softlang.qegal.buildins.decompose.DecXml;
import org.softlang.qegal.buildins.decompose.UriXml;
import org.softlang.qegal.modules.IModel;
import org.softlang.qegal.modules.QegalState;
import org.softlang.qegal.modules.QegalTest;

public class Instances extends QegalTest {

	public Instances(String path, String address, IModel model, QegalState state) {
		super(path, address, model, state);
	}

	public static void main(String[] args) {
		String sf = "//eStructuralFeatures[@containment='true']";
		List<Node> nodes = new DecXml().decompose("D:/Programming/EMFMining/BenRhouma/AcceleoStandAlone/content/model/MetaModel.ecore", sf);
		nodes.forEach(n -> System.out.println(n.getURI()));
	}

	@Before
	public void setUp() throws Exception {
		// Can only be tested on beta phase where EMF mining has been applied.
		whitelist(QegalState.DONE);

		// Only simple Manifest build.
		blacklist(elementsOf(sl("Ant")).size() > 0);
		blacklist(elementsOf(sl("Pom")).size() > 0);
		whitelist(elementsOf(sl("Manifest")).size() > 0);

		whitelist(elementsOf(sl("EcoreClassifierXMI")).size() > 0);
	}

	@Test
	public void ecoreStructuralFeatureXMI() {
		for (String model : elementsOf(sl("Ecore"))) {
			Set<String> features = new DecXml().decompose(model, "//eStructuralFeatures[@containment='true']").stream()
											   .map(n -> n.getURI()).collect(Collectors.toSet());
			assertEqualSize(features, elementsOf(sl("EcoreStructuralFeatureXMI")));
			Set<String> classifiers = new DecXml().decompose(model, "//eClassifier").stream()
												  .map(n -> n.getURI()).collect(Collectors.toSet());
			features.stream().map(sf -> getObjectWith(sf, sl("partOf")))
						     .forEach(c -> assertTrue(classifiers.contains(c)));
		}
	}

	@Test
	public void featureReferences() {
		for (String sf : elementsOf(sl("EcoreStructuralFeatureXMI"))) {
			String classifier = getObjectWith(sf, sl("partOf"));
			String nsURI = getObjectWith(classifier, sl("nsUri"));
			String eType = new DecXml().decompose(sf, "/@eType").get(0).getURI();
			String reference = getObjectWith(sf, sl("references"));
			assertEquals(nsURI+eType,reference);
		}
	}

	@Test
	public void featureName() {
		for (String sf : elementsOf(sl("EcoreStructuralFeatureXMI"))) {
			String actualname = getObjectWith(sf, sl("name"));
			String expectedName = new DecXml().decompose(sf, "/@name").get(0).getURI();
			assertEquals(expectedName,actualname);
		}
	}

	@Test
	public void xmlNS() {
		for(String xml : elementsOf(sl("XML"))) {
			String actual = getObjectWith(xml,sl("xmlns"));
			String expected = new UriXml().decompose(xml, "/namespace::*").get(0).getURI();
			assertEquals(expected,actual);
		}
	}

	@Test
	public void elementOfXMI() {
		Set<String> xmls = elementsOf(sl("XML"));
		Set<String> omgs = getSubjectsWith(sl("xmlns"), "http://www.omg.org/XMI");
		SetView<String> expected = Sets.intersection(xmls, omgs);
		Set<String> actual = elementsOf(sl("XMI"));
		assertEqualSize(expected, actual);
	}

	@Test
	public void xmiFragmentConformance() {
		for(String xmi : elementsOf(sl("XMI"))) {
			String nsUri = getObjectWith(xmi, sl("xmlns"));
			SetView<String> xmiPackages = Sets.intersection(getSubjectsWith(sl("nsUri"),nsUri),elementsOf(sl("EcorePackageXMI")));
			assertEquals(1,xmiPackages.size());
			String xmiPackage = xmiPackages.iterator().next();
			String nsPrefix = getObjectWith(xmiPackage,sl("nsPrefix"));
			SetView<String> xmiClassifiers = Sets.intersection(getSubjectsWith(sl("partOf"),xmiPackage), elementsOf(sl("EcoreClassifierXMI")));
			for(String xmiClassifier : xmiClassifiers) {
				String classifierName = getObjectWith(xmiClassifier,sl("name"));
				String xpath = "/" + nsPrefix + ":" + classifierName;
				List<Node> temp_XMIfragments = new DecXml().decompose(xmi, xpath);
				assertEquals(1,temp_XMIfragments.size());
				String temp_XmiFragment = temp_XMIfragments.get(0).getURI();
				assertTriple(temp_XmiFragment, sl("conformsTo"), xmiClassifier);
				assertTriple(temp_XmiFragment,sl("elementOf"),sl("XMIFragment"));
			}
		}
	}

	@Test
	public void nestedXmiFragmentConformance() {
		for(String xmiFragment : elementsOf(sl("XMIFragment"))) {
			String xmiClassifier = getObjectWith(xmiFragment,sl("conformsTo"));
			SetView<String> xmiStructuralFeatures = Sets.intersection(getSubjectsWith(sl("partOf"),xmiClassifier),elementsOf(sl("EcoreStructuralFeatureXMI")));
			for(String xmiStructuralFeature : xmiStructuralFeatures) {
				String sfname = getObjectWith(xmiStructuralFeature,sl("name"));
				String xmiClassifierUri = getObjectWith(xmiStructuralFeature,sl("references"));
				String referencesXmiClassifier = getSubjectWith(sl("uri"),xmiClassifierUri);
				assertTriple(referencesXmiClassifier, sl("elementOf"), sl("EcoreClassifierXMI"));
				String xpath = "/" + sfname;
				List<Node> temp_XMIfragments = new DecXml().decompose(xmiFragment, xpath);
				assertEquals(1,temp_XMIfragments.size());
				String temp_XMIfragment = temp_XMIfragments.get(0).getURI();
				assertTriple(temp_XMIfragment, sl("conformsTo"), referencesXmiClassifier);
				assertTriple(temp_XMIfragment,sl("elementOf"),sl("XMIFragment"));
			}
		}
	}
}
