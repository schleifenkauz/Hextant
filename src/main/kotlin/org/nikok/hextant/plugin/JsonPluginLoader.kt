/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.plugin

import javafx.scene.input.KeyCombination
import org.nikok.hextant.Editable
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.*
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.core.inspect.Inspection
import org.nikok.hextant.core.inspect.Inspections
import javax.json.*
import javax.json.stream.JsonParser
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.isSubclassOf

@Suppress("UNCHECKED_CAST")
internal class JsonPluginLoader(
    private val json: JsonParser,
    private val platform: HextantPlatform,
    private val clsLoader: ClassLoader
) {
    private var loaded: Boolean = false

    private var plugin: Plugin? = null

    private val commands = platform[Public, Commands]

    private val inspections = platform[Public, Inspections]

    private val views = platform[Public, EditorControlFactory]

    private val editors = platform[Public, EditorFactory]

    private val editables = platform[Public, EditableFactory]

    fun load(): Plugin {
        check(!loaded)
        val plugin = try {
            doLoad()
        } catch (ex: JsonException) {
            throw error("Json parsing exception", ex)
        }
        loaded = true
        return plugin
    }

    private fun doLoad(): Plugin {
        val objects = json.objectStream
        for ((name, obj) in objects) {
            processValue(name, obj)
        }
        return plugin ?: error("Plugin information not provided")
    }

    private fun processValue(name: String, value: JsonValue) {
        when (name) {
            "init"        -> processInit(value)
            "exit"        -> processExit(value)
            "plugin"      -> processPlugin(value)
            "views"       -> processViews(value)
            "editors"     -> processEditors(value)
            "editables"   -> processEditables(value)
            "inspections" -> processInspections(value)
            "commands"    -> processCommands(value)
            else          -> error("Unknown property $name")
        }
    }

    private fun processEditables(value: JsonValue) {
        val arr = value.asArray("editables")
        for (o in arr) {
            processEditable(o.asObject("editable"))
        }
    }

    private fun processExit(value: JsonValue) {
        val clsName = (value as? JsonString)?.string ?: error("expected string but got $value")
        val cls = loadClass(clsName)
        val exit = cls.instance() as? PluginExit ?: error("$clsName is not of type PluginExit")
        exit.exit(platform)
    }

    private fun processInit(value: JsonValue) {
        val clsName = (value as? JsonString)?.string ?: error("expected string but got $value")
        val cls = loadClass(clsName)
        val init = cls.instance() as? PluginInit ?: error("$clsName is not of type PluginInit")
        init.initialize(platform)
    }

    private fun processCommands(value: JsonValue) {
        val arr = value.asArray("commands")
        for (command in arr) {
            processCommand(command)
        }
    }

    private fun processCommand(value: JsonValue) {
        val obj = value.asObject("command")
        val clsName = obj.getStr("class", "command")
        val cls = loadClass(clsName)
        val shortcut = getShortcut(obj)
        val command = cls.instance() as? Command<*, *> ?: error("$clsName is not a command")
        val registrar = commands.of(command.receiverCls as KClass<Any>)
        registrar.register(command as Command<Any, Any?>)
        if (shortcut != null) {
            registrar.registerShortcut(command, shortcut)
        }
    }

    private fun getShortcut(obj: JsonObject): KeyCombination? =
        obj.getOrNull<JsonObject>("shortcut", "object")?.run {
            TODO()
        }

    private fun processInspections(value: JsonValue) {
        val arr = value.asArray("inspections")
        for (v in arr) {
            processInspection(v.asObject("inspection"))
        }
    }

    private fun processInspection(obj: JsonObject) {
        val clsName = obj.getStr("class", "inspection")
        val cls = loadClass(clsName)
        check(cls.isSubclassOf(Inspection::class), "class $clsName is not an inspection")
        val receiverClsName = obj.getStr("receiver", "inspection")
        val receiverCls = loadClass(receiverClsName)
        val registrar = inspections.of(receiverCls)
        val constructor = cls.constructors.find { it.parameters.size == 1 && it.parameters[0].type.classifier == cls }
            ?: error("No constructor found for inspection $clsName")
        registrar.register { it -> constructor.call(it) as Inspection<Any> }
    }

    private fun processEditable(obj: JsonObject) {
        val clsName = obj.getStr("class", "editable")
        val cls = loadClass(clsName)
        check(cls.isSubclassOf(Editable::class), "class $clsName is not an inspection")
        cls as KClass<Editable<Any>>
        val editedClsName = obj.getStr("edited", "editable")
        val editedCls = loadClass(editedClsName) as KClass<Any>
        registerNoArg(cls, editedCls)
        registerOneArg(cls, editedCls)
    }

    private fun registerOneArg(
        cls: KClass<Editable<Any>>,
        editedCls: KClass<Any>
    ) {
        val oneArgConstructor = cls.constructors.find { c ->
            val required = c.parameters.filterNot { it.isOptional }
            required.size == 1 && required[0].type.classifier == editedCls
        } ?: error("found no one-argument-constructor for $cls")
        val editedParameter = oneArgConstructor.parameters.first { !it.isOptional && it.type.classifier == editedCls }
        editables.register(editedCls) { edited -> oneArgConstructor.callBy(mapOf(editedParameter to edited)) }
    }

    private fun registerNoArg(
        cls: KClass<Editable<Any>>,
        editedCls: KClass<*>
    ) {
        val noArgConstructor = cls.constructors.find { c -> c.parameters.all { p -> p.isOptional } }
            ?: error("found no nullary constructor for $cls")
        editables.register(editedCls as KClass<Any>) { -> noArgConstructor.callBy(emptyMap()) }
    }

    private fun processEditors(value: JsonValue) {
        TODO("not implemented")
    }

    private fun processViews(value: JsonValue) {
        TODO("not implemented")
    }

    private fun processPlugin(value: JsonValue) {
        check(plugin == null, "Duplicated key 'plugin'")
        val obj = value.asObject("plugin")
        val name = obj.getStr("name", "plugin")
        val author = obj.getStr("author", "plugin")
        plugin = Plugin(name, author)
    }

    private fun error(msg: String, cause: Throwable? = null): Nothing {
        val line = json.location.lineNumber
        val column = json.location.columnNumber
        throw PluginException(plugin, "$msg at: $line:$column", cause)
    }

    private fun JsonValue.asArray(name: String): JsonArray {
        check(this is JsonArray, "entry '$name' must be a array")
        return asJsonArray()
    }

    private fun JsonValue.asObject(name: String): JsonObject {
        check(this is JsonObject, "entry '$name' must be a complex type'")
        return asJsonObject()
    }

    private fun check(condition: Boolean, msg: String, cause: Throwable? = null) {
        if (!condition) {
            error(msg, cause)
        }
    }

    private inline fun <reified T> JsonObject.get(name: String, typeName: String, context: String): T {
        val v = get(name) ?: error("entry '$name' is required in '$context'")
        if (v !is T) {
            error("expected entry '$name' of type $typeName but got $v")
        }
        return v
    }

    private inline fun <reified T> JsonObject.getOrNull(name: String, typeName: String): T? {
        val v = get(name) ?: return null
        if (v !is T) {
            error("expected entry '$name' of type $typeName but got $v")
        }
        return v
    }

    private fun JsonObject.getStr(name: String, context: String) =
        get<JsonString>(name, "string", context).string

    private fun loadClass(name: String): KClass<*> = try {
        clsLoader.loadClass(name).kotlin
    } catch (ex: ClassNotFoundException) {
        error("Class '$name' not found", ex)
    } catch (ex: ExceptionInInitializerError) {
        error("Exception while loading class '$name'", ex)
    }

    private fun KClass<*>.instance() = objectInstance ?: try {
        createInstance()
    } catch (noNullaryConstructor: NoSuchElementException) {
        error("Cannot create instance of $this", noNullaryConstructor)
    }
}