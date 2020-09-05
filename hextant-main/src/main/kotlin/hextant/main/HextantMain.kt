package hextant.main

import bundles.SimpleReactiveProperty
import hextant.context.Context
import hextant.core.editor.SimpleStringEditor
import hextant.plugin.*

internal object HextantMain : PluginInitializer({
    commandDelegation { ctx: Context -> if (ctx.hasProperty(ProjectManager)) ctx[ProjectManager] else null }
    configurableProperty(Header, ::SimpleStringEditor)
}) {
    object Header : SimpleReactiveProperty<String>("header") {
        override val default: String
            get() = "Hextant"
    }
}