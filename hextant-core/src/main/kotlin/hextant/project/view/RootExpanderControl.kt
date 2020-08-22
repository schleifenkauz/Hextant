/**
 *@author Nikolaus Knop
 */

package hextant.project.view

import bundles.Bundle
import hextant.completion.CompletionStrategy
import hextant.core.view.ExpanderControl
import hextant.project.editor.ProjectItemEditor
import hextant.project.editor.RootExpander

internal class RootExpanderControl(expander: RootExpander<*>, args: Bundle) : ExpanderControl(
    expander, args,
    expander.context[ProjectItemEditor.expanderConfig<Any?>()].completer(CompletionStrategy.simple)
)