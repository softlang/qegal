package org.softlang.qegal.modules;

import org.junit.Test;

public class CustomTests extends QegalTest {

	public CustomTests(String path, String address, IModel model, QegalState state) {
		super(path, address, model, state);
	}

	@Test
	public void metalibRequiredTriples() {
		// Repository specific whitelist.
		whitelist("emfMetalib");

		// Assertions.
		assertTriple(path("fsml/org.softlang.metalib.emf.fsml/model/Fsml.ecore"), sl("elementOf"), sl("Ecore"));
	}
}
