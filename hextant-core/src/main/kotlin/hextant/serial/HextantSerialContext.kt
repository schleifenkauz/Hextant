package hextant.serial

import hextant.Context
import hextant.Editor
import kserial.SerialContext
import kserial.SerializationException
import java.util.*
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.valueParameters
import kotlin.reflect.jvm.javaConstructor

/**
 * Extends the [SerialContext] by providing a stack of Hextant [Context]'s for serializing and deserializing editors
 */
class HextantSerialContext(
    private val platform: Context,
    classLoader: ClassLoader
) : SerialContext(classLoader = classLoader) {
    /**
     * Create an instance of the specified class.
     * If the class is a sub-class of [Editor] the constructor with the [Context]-parameter is called with the stack's top context.
     * Otherwise the implementation of [SerialContext] is used.
     */
    override fun <T : Any> createInstance(cls: KClass<T>): T {
        return if (cls.isSubclassOf(Editor::class)) createEditor(cls)
        else super.createInstance(cls)
    }

    private fun <T : Any> createEditor(cls: KClass<T>): T {
        val contextConstr = cls.constructors.find {
            val params = it.valueParameters.filter { p -> !p.isOptional }
            params.size == 1 && params[0].type.classifier == Context::class
        } ?: throw SerializationException("$cls has no constructor with context parameter")
        val contextParam = contextConstr.parameters.find { !it.isOptional }!!
        contextConstr.javaConstructor!!.isAccessible = true
        return contextConstr.callBy(mapOf(contextParam to context))
    }

    private val contextStack: Deque<Context> = LinkedList()

    /**
     * Push the given [context] on top of the context stack
     */
    fun pushContext(context: Context) {
        contextStack.push(context)
    }

    /**
     * Pop the current [context] of the context stack
     */
    fun popContext() {
        contextStack.pop()
    }

    /**
     * Return the top of the context stack
     */
    val context get() = contextStack.peek() ?: platform
}