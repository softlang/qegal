package org.softlang.qegal.modules.emf;

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.softlang.qegal.modules.IModel;
import org.softlang.qegal.modules.QegalState;
import org.softlang.qegal.modules.QegalTest;

import com.google.common.collect.Sets;
import com.google.common.collect.Sets.SetView;

public class UriTest extends QegalTest{

	public UriTest(String path, String address, IModel model, QegalState state) {
		super(path, address, model, state);
	}

	@Before
	public void setUp() throws Exception {
		// Can only be tested on beta phase where EMF mining has been applied.
		whitelist(QegalState.DONE);
	}

	/**
	 * Valid nsURI subjects:
	 * - Packages for Java and XMI
	 * - Factory for Java
	 * - Classifiers for XMI
	 */
	@Test
	public void nsUriCheck() {
		Set<String> nsUriSubjects = getSubjectsWith(sl("nsUri"));
		SetView<String> nsUriValid = Sets.union(elementsOf(sl("EcorePackageJava")), elementsOf(sl("EcorePackageXMI")));
		nsUriValid = Sets.union(nsUriValid, elementsOf(sl("EcoreFactoryJava")));
		nsUriValid = Sets.union(nsUriValid, elementsOf(sl("EcoreClassifierXMI")));
		assertEqualSize(nsUriValid,nsUriSubjects);
	}
	
	/**
	 * Only classifiers have uri
	 */
	@Test
	public void uriCheck() {
		Set<String> uriSubjects = getSubjectsWith(sl("uri"));
		SetView<String> validUriSubjects = Sets.union(elementsOf(sl("EcoreClassifierJava")), elementsOf(sl("EcoreClassifierXMI")));
		assertEqualSize(validUriSubjects, uriSubjects);
	}
	
	@Test
	public void nameCheck() {
		Set<String> uriSubjects = getSubjectsWith(sl("name"));
		SetView<String> validUriSubjects = Sets.union(elementsOf(sl("EcoreClassifierJava")), elementsOf(sl("EcoreClassifierXMI")));
		assertEqualSize(validUriSubjects, uriSubjects);
	}

}
