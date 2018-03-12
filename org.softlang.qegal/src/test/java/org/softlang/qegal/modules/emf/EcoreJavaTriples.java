package org.softlang.qegal.modules.emf;

import org.junit.Before;
import org.junit.Test;
import org.softlang.qegal.modules.IModel;
import org.softlang.qegal.modules.QegalState;
import org.softlang.qegal.modules.QegalTest;

public class EcoreJavaTriples extends QegalTest{
	
	public EcoreJavaTriples(String path, String address, IModel model, QegalState state) {
		super(path, address, model, state);
	}

	@Before
	public void setUp() throws Exception {
		whitelist("Tranxen200/FMUSim-Rodin");
	}
	
	@Test
	public void testEcorePackageJava() {
		String packageuri = "C:/Programmierung/EMFMining/Tranxen200/FMUSim-Rodin/content/"
				+ "ac.soton.fmusim.components/src/ac/soton/fmusim/components/ComponentsPackage.java";
		assertTriple(packageuri,sl("elementOf"),sl("EcorePackageJava"));
	}
	
	

}
