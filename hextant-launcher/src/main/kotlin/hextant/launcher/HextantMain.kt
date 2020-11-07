package hextant.launcher

import bundles.PublicProperty
import bundles.property
import hextant.context.Context
import hextant.core.editor.SimpleStringEditor
import hextant.plugin.*
import hextant.plugins.PluginInfo

internal object HextantMain : PluginInitializer({
    commandDelegation { ctx: Context -> if (ctx.hasProperty(ProjectManager)) ctx[ProjectManager] else null }
    configurableProperty(Header, ::SimpleStringEditor)
    registerCommand(showPluginManager(setOf(PluginInfo.Type.Global)))
    registerCommand(enablePlugin(setOf(PluginInfo.Type.Global)))
    registerCommand(disablePlugin)
}) {
    object Header : PublicProperty<String> by property("header", default = "Hextant")
}