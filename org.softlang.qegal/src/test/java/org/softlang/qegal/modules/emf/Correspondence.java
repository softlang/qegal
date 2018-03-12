package org.softlang.qegal.modules.emf;

import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.softlang.qegal.modules.IModel;
import org.softlang.qegal.modules.QegalState;
import org.softlang.qegal.modules.QegalTest;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class Correspondence extends QegalTest{

	public Correspondence(String path, String address, IModel model, QegalState state) {
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
	public void ecoreCountCorrespondencePerPackage() {
		elementsOf(sl("EcoreXmiPackage")).forEach(p -> {
			assertTrue(getObjectsWith(p, sl("correspondsTo")).size()<2);
		});
	}
	
	@Test
	public void ecoreCountCorrespondencePerClassifier() {
		elementsOf(sl("EcoreXmiClassifier")).forEach(c -> {
			assertTrue(getObjectsWith(c, sl("correspondsTo")).size()<2);
		});
	}
	
	@Test
	public void ecorePackageCorrespondenceJavaXMI() {
		Map<String, String> xmiMap = new HashMap<>();
		Map<String, String> javaMap = new HashMap<>();
		
		Set<String> packageXmi = elementsOf(sl("EcorePackageXMI"));
		for(String pxmi : packageXmi) {
			String nsUri = getObjectWith(pxmi, sl("nsUri"));
			xmiMap.put(nsUri, pxmi);
		}
		
		Set<String> packageJava = elementsOf(sl("EcorePackageJava"));
		for(String pjava : packageJava) {
			String nsUri = getObjectWith(pjava, sl("nsUri"));
			javaMap.put(nsUri, pjava);
		}
		
		for(String xmikey : xmiMap.keySet()) {
			if(javaMap.containsKey(xmikey)) {
				assertTriple(xmiMap.get(xmikey), sl("correspondsTo"), javaMap.get(xmikey));
			}
		}
	}
	
	@Test
	public void ecoreClassifierCorrespondenceJavaXMI() {
		Map<String, String> nsUrixmiClassifiers = new HashMap<>();
		Map<String, String> nsUrijavaClassifiers = new HashMap<>();
		
		Set<String> packageXmi = elementsOf(sl("EcoreClassifierXMI"));
		for(String pxmi : packageXmi) {
			String nsUri = getObjectWith(pxmi, sl("uri"));
			nsUrixmiClassifiers.put(nsUri, pxmi);
		}
		
		Set<String> packageJava = elementsOf(sl("EcoreClassifierJava"));
		for(String pjava : packageJava) {
			String nsUri = getObjectWith(pjava, sl("uri"));
			nsUrijavaClassifiers.put(nsUri, pjava);
		}
		
		for(String xmikey : nsUrixmiClassifiers.keySet()) {
			if(nsUrijavaClassifiers.containsKey(xmikey)) {
				assertTriple(nsUrixmiClassifiers.get(xmikey), sl("correspondsTo"), nsUrijavaClassifiers.get(xmikey));
			}
		}
	}

}
