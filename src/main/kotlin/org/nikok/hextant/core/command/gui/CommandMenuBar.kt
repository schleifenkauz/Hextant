/**
 *@author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import org.nikok.hextant.Context
import org.nikok.hextant.core.command.Command.Category
import org.nikok.hextant.core.command.CommandRegistrar
import org.nikok.hextant.core.impl.myLogger
import org.nikok.reaktive.event.subscribe

internal class CommandMenuBar<T : Any> private constructor(
    private val target: T,
    registrar: CommandRegistrar<T>,
    private val context: Context
) : MenuBar() {
    init {
        initialize(registrar)
        listenForNewCategories(registrar)
    }

    private fun initialize(registrar: CommandRegistrar<T>) {
        logger.info("Initializing command menu bar")
        val categories = registrar.categories
        for (c in categories) {
            addMenu(registrar, c)
        }
    }

    private fun listenForNewCategories(registrar: CommandRegistrar<T>) {
        registrar.addedCategory.subscribe("Listen for new Categories") { newCategory ->
            addMenu(registrar, newCategory)
        }
    }

    private fun addMenu(
        registrar: CommandRegistrar<T>,
        newCategory: Category
    ) = menus.add(CommandMenu(target, registrar, newCategory, context)).also {
        logger.fine { "added menu $newCategory" }
    }

    companion object {
        internal fun <T : Any> newInstance(
            target: T,
            registrar: CommandRegistrar<T>,
            context: Context
        ): MenuBar =
            CommandMenuBar(target, registrar, context)

        val logger by myLogger()
    }

    private class CommandMenu<T : Any>(
        private val target: T,
        private val commandRegistrar: CommandRegistrar<T>,
        private val category: Category,
        private val context: Context
    ) : Menu() {
        init {
            text = category.name
            update()
            setOnShowing { update() }
        }

        private fun update() {
            logger.info("Updating $this")
            items.clear()
            for (c in commandRegistrar.commands) {
                if (c.category == category) {
                    val item = CommandMenuItem(target, c, commandRegistrar, context)
                    logger.fine { "Add menu item for $c" }
                    items.add(item)
                }
            }
        }
    }
}