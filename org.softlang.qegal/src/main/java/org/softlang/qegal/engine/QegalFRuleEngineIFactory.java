package org.softlang.qegal.engine;

import org.apache.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.impl.FRuleEngine;
import org.apache.jena.reasoner.rulesys.impl.FRuleEngineI;
import org.apache.jena.reasoner.rulesys.impl.FRuleEngineIFactory;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.jutils.CSVSink;
import java.util.List;
import java.util.Map;

/**
 * Created by Johannes on 07.11.2017. Replacement of the regular RETE Enging by
 * a Qegal specific subclass.
 */
public class QegalFRuleEngineIFactory extends FRuleEngineIFactory {

	private final CSVSink debug;

	private final Map<String, String> info;

	private final QegalLogging log;

	public QegalFRuleEngineIFactory(CSVSink debug, Map<String, String> info, QegalLogging log) {
		this.debug = debug;
		this.info = info;
		this.log = log;
	}

	public static void register(CSVSink debug, Map<String, String> info, QegalLogging log) {
		setInstance(new QegalFRuleEngineIFactory(debug, info,log));
	}

	@Override
	public FRuleEngineI createFRuleEngineI(ForwardRuleInfGraphI parent, List<Rule> rules, boolean useRETE) {
		FRuleEngineI engine;
		if (rules != null) {
			if (useRETE) {
				engine = new QegalRETEEngine(parent, rules, debug, info,log);
			} else {
				engine = new FRuleEngine(parent, rules);
			}
		} else {
			if (useRETE) {
				engine = new QegalRETEEngine(parent, debug, info,log);
			} else {
				engine = new FRuleEngine(parent);
			}
		}
		return engine;
	}
}
