package org.softlang.qegal.qmodles;

public class QModelConsistency {

	public static void main(String[] args) {
		// load .ttl
		
		//rule1: there exists at least 1 Ecore file
		//rule2: there exists at least 1 Genmodel
		//rule3: for every Ecore file, there exists a Genmodel
		//rule4: for every genmodel, there exists generated code
		//rule5: for every interface, there exists an implementation
		//rule6: for every genclass, there exists an interface
		//rule7: for every structural feature, there exists a variable, getter and setter
	}

}
