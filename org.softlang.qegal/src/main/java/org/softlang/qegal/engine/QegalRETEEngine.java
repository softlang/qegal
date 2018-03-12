package org.softlang.qegal.engine;

import com.google.common.collect.ImmutableMap;
import org.apache.jena.graph.Triple;
import org.apache.jena.reasoner.rulesys.ForwardRuleInfGraphI;
import org.apache.jena.reasoner.rulesys.Rule;
import org.apache.jena.reasoner.rulesys.impl.RETEEngine;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.jutils.CSVSink;

import java.util.List;
import java.util.Map;

/**
 * Created by Johannes on 07.11.2017.
 */
public class QegalRETEEngine extends RETEEngine {
	final CSVSink debug;

	private QegalLogging log = QegalLogging.NONE;

	public Map<String, String> info;

	public long initialTime = System.currentTimeMillis();

	public QegalRETEEngine(ForwardRuleInfGraphI parent, CSVSink debug, Map<String, String> info, QegalLogging log) {
		super(parent);
		this.debug = debug;
		this.log = log;
		this.info = info;
	}

	public QegalRETEEngine(ForwardRuleInfGraphI parent, List<Rule> rules, CSVSink debug, Map<String, String> info,
			QegalLogging log) {
		super(parent, rules);
		this.debug = debug;
		this.info = info;
		this.log = log;
	}

	/**
	 * Builtin specific submit mechanism for complex logs.
	 */
	protected void submit(String messageType, String... messages) {
		ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
		// Current time.
		builder.put("now_ms", String.valueOf(System.currentTimeMillis() - initialTime));
		// Type.
		builder.put("type", messageType);
		for (int i = 0; i < messages.length / 2; i++)
			builder.put(messages[i * 2], messages[i * 2 + 1]);

		builder.putAll(info);

		debug.write(builder.build());
	}

	@Override
	public synchronized void addTriple(Triple triple, boolean deduction) {
		if (log == QegalLogging.ALL)
			submit("engine_add_triple", "sub", triple.getSubject().toString(), "pred", triple.getPredicate().toString(),
					"obj", triple.getObject().toString());

		super.addTriple(triple, deduction);
	}
}
