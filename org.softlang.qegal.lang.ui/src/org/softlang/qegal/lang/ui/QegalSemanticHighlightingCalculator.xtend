package org.softlang.qegal.lang.ui

import org.eclipse.xtext.xbase.ide.highlighting.XbaseHighlightingCalculator
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.ide.editor.syntaxcoloring.IHighlightedPositionAcceptor
import org.eclipse.xtext.util.CancelIndicator
import org.eclipse.xtext.EcoreUtil2
import org.softlang.qegal.lang.qegal.Builtin
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.nodemodel.INode
import org.softlang.qegal.lang.qegal.QegalPackage
import org.softlang.qegal.lang.qegal.Variable

/**
 * Good reference:
 * https://github.com/xtext/seven-languages-xtext/blob/master/languages/org.xtext.template.ui/src/org/xtext/template/ui/highlighting/TemplateHighlightingCalculator.xtend
 * 
 */
class QegalSemanticHighlightingCalculator extends XbaseHighlightingCalculator {
	override doProvideHighlightingFor(XtextResource resource, IHighlightedPositionAcceptor acceptor,
		CancelIndicator cancelIndicator) {
		super.doProvideHighlightingFor(resource, acceptor, cancelIndicator)

		val root = resource.getParseResult().getRootASTElement();
		for (builtin : EcoreUtil2.getAllContentsOfType(root, Builtin))
			for (INode node : NodeModelUtils.findNodesForFeature(builtin, QegalPackage.Literals.BUILTIN__BUILIN))
				acceptor.addPosition(node.getOffset(), node.getLength(), QegalHighlightingConfiguration.QEGAL_BUILTIN);

		for (variable : EcoreUtil2.getAllContentsOfType(root, Variable)) {
			val node = NodeModelUtils.findActualNodeFor(variable)
			acceptor.addPosition(node.getOffset(), node.getLength(), QegalHighlightingConfiguration.QEGAL_VARIABLE);
		}
	}
}
