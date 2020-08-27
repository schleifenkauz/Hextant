package hextant.plugins

import kotlin.reflect.KType
import kotlin.reflect.typeOf

class PluginProperty<T : Any> private constructor(val name: String, val type: KType) {
    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        private inline fun <reified T : Any> get(name: String) = PluginProperty<T>(name, typeOf<T>())

        val aspects = get<List<Aspect>>("aspects")
        val features = get<List<Feature>>("features")
        val implementations = get<List<Implementation>>("implementations")
        val info = get<PluginInfo>("plugin")
    }
}