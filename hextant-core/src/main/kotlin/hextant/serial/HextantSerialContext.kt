package hextant.serial

import hextant.*
import kserial.SerialContext
import kserial.SerializationException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters

class HextantSerialContext(
    private val platform: HextantPlatform,
    classLoader: ClassLoader
) : SerialContext(classLoader = classLoader) {
    override fun <T : Any> createInstance(cls: KClass<T>): T {
        return if (cls.isSubclassOf(Editor::class)) createEditor(cls)
        else super.createInstance(cls)
    }

    private fun <T : Any> createEditor(cls: KClass<T>): T {
        val contextConstr = cls.constructors.find {
            val params = it.valueParameters.filter { p -> !p.isOptional }
            params.size == 1 && params[0].type.classifier == Context::class
        } ?: throw SerializationException("Editors must have constructor with context parameter")
        val contextParam = contextConstr.parameters.find { !it.isOptional }!!
        return contextConstr.callBy(mapOf(contextParam to context))
    }

    private val contextStack: Deque<Context> = LinkedList()

    fun pushContext(context: Context) {
        contextStack.push(context)
    }

    fun popContext() {
        contextStack.pop()
    }

    val context get() = contextStack.peek() ?: platform
}