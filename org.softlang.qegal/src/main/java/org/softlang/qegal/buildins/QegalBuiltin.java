package org.softlang.qegal.buildins;

import com.google.common.base.CaseFormat;
import com.google.common.collect.ImmutableMap;
import org.apache.jena.graph.Node;
import org.apache.jena.reasoner.rulesys.BuiltinException;
import org.apache.jena.reasoner.rulesys.RuleContext;
import org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin;
import org.softlang.qegal.QegalLogging;
import org.softlang.qegal.io.IOLayer;
import org.softlang.qegal.jutils.CSVSink;

import java.util.Collections;
import java.util.Map;

/**
 * Created by Johannes on 29.09.2017.
 */
abstract public class QegalBuiltin extends BaseBuiltin {

	protected QegalLogging log = QegalLogging.NONE;

	public CSVSink loggingSink = null;

	protected IOLayer iolayer = null;

	public Map<String, String> info = Collections.emptyMap();

	public long initialTime = System.currentTimeMillis();

	public void setInfo(Map<String, String> info) {
		this.info = info;
	}

	public void setIolayer(IOLayer iolayer) {
		this.iolayer = iolayer;
	}

	public void setDebug(CSVSink loggingSink) {
		this.loggingSink = loggingSink;
	}

	@Override
	public String getName() {
		return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, this.getClass().getSimpleName());
	}

	/**
	 * Builtin specific submit mechanism for complex logs.
	 */
	protected void submit(Node[] args, int length, RuleContext context, String messageType, String... messages) {
		if (log != QegalLogging.NONE) {
			ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
			// Builtin class.
			builder.put("builtin", getName());
			// Arguments.
			for (int i = 0; i < args.length; i++)
				builder.put("arg_" + i, args[i].toString());
			// Rule.
			builder.put("rule", context.getRule().getName());
			// Current time.
			builder.put("now_ms", String.valueOf(System.currentTimeMillis() - initialTime));
			// Type.
			builder.put("type", messageType);
			for (int i = 0; i < messages.length / 2; i++)
				builder.put(messages[i * 2], messages[i * 2 + 1]);

			builder.putAll(info);

			loggingSink.write(builder.build());
		}
	}

	@Override
	final public boolean bodyCall(Node[] args, int length, RuleContext context) {
		long start = 0;
		long end = 0;
		// Enter log.
		if (log == QegalLogging.ALL)
			submit(args, length, context, "builtin_enter", "body", "true");

		// Execution
		if (log == QegalLogging.ALL)
			start = System.nanoTime();

		Boolean result = null;
		try {
			result = trackedBodyCall(args, length, context);
		} catch (Exception e) {
			submit(args, length, context, "exception", "message", String.valueOf(e.getMessage()));
			return false;
		}
		if (log == QegalLogging.ALL)
			end = System.nanoTime();

		// Exit log.
		if (log == QegalLogging.ALL)
			submit(args, length, context, "builtin_exit", "body", "true", "time_ns", String.valueOf(end - start));

		return result;
	}

	@Override
	final public void headAction(Node[] args, int length, RuleContext context) {
		long start = 0;
		long end = 0;
		// Enter log.
		if (log == QegalLogging.ALL)
			submit(args, length, context, "builtin_enter", "body", "false");

		// Execution
		if (log == QegalLogging.ALL)
			start = System.nanoTime();
		try {
			trackedHeadAction(args, length, context);
		} catch (Exception e) {
			submit(args, length, context, "exception", "message", String.valueOf(e.getMessage()));
		}
		if (log == QegalLogging.ALL)
			end = System.nanoTime();

		// Exit log.
		if (log == QegalLogging.ALL)
			submit(args, length, context, "builtin_exit", "body", "false", "time_ns", String.valueOf(end - start));
	}

	/**
	 * Stub for being overridden.
	 */
	public boolean trackedBodyCall(Node[] args, int length, RuleContext context) throws Exception {
		throw new BuiltinException(this, context, "builtin " + getName() + " not usable in rule bodies");
	}

	/**
	 * Stub for being overridden.
	 */
	public void trackedHeadAction(Node[] args, int length, RuleContext context) throws Exception {
		throw new BuiltinException(this, context, "builtin " + getName() + " not usable in rule heads");
	}
}