package org.softlang.qegal.lang.ui

import org.eclipse.xtext.xbase.ui.highlighting.XbaseHighlightingConfiguration
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfigurationAcceptor
import org.eclipse.xtext.ui.editor.utils.TextStyle
import org.eclipse.swt.graphics.RGB
import org.eclipse.swt.SWT

class QegalHighlightingConfiguration extends XbaseHighlightingConfiguration {

	public static val QEGAL_BUILTIN = 'qegal.builtin'
	public static val QEGAL_VARIABLE = 'qegal.variable'

	override configure(IHighlightingConfigurationAcceptor acceptor) {
		acceptor.acceptDefaultHighlighting(QEGAL_BUILTIN, 'Builtins', builtins)
		acceptor.acceptDefaultHighlighting(QEGAL_VARIABLE, 'Variables', variables)
		super.configure([ id, name, style |
			acceptor.acceptDefaultHighlighting(id, name, style)
		])
	}

	def TextStyle builtins() {
		defaultTextStyle.copy => [
			color = new RGB(0, 0, 0)
			style = SWT.UNDERLINE_SINGLE
		]
	}

	def TextStyle variables() {
		defaultTextStyle.copy => [
			color = new RGB(180, 0, 180)
			style = SWT.BOLD
		]
	}
}
