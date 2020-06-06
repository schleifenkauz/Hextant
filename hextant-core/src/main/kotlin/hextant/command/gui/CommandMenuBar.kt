/**
 *@author Nikolaus Knop
 */

package hextant.command.gui

import hextant.Context
import hextant.command.Commands
import javafx.scene.control.MenuBar

internal class CommandMenuBar<T : Any>(
    private val target: T,
    commands: Commands,
    private val context: Context
) : MenuBar() {
    //    init {
    //        initialize(commands)
    //        listenForNewCategories(registrar)
    //    }
    //
    //    private fun initialize(commands: Commands) {
    //
    //        val categories = commands.categories
    //        for (c in categories) {
    //            addMenu(registrar, c)
    //        }
    //    }
    //
    //    private fun listenForNewCategories(registrar: CommandRegistrar<T>) {
    //        registrar.addedCategory.observe { _, newCategory ->
    //            addMenu(registrar, newCategory)
    //        }
    //    }
    //
    //    private fun addMenu(
    //        registrar: CommandRegistrar<T>,
    //        newCategory: Category
    //    ) = menus.add(CommandMenu(target, registrar, newCategory, context))
    //
    //    companion object {
    //        internal fun <T : Any> newInstance(
    //            target: T,
    //            registrar: CommandRegistrar<T>,
    //            context: Context
    //        ): MenuBar = CommandMenuBar(target, registrar, context)
    //    }
    //
    //    private class CommandMenu<T : Any>(
    //        private val target: T,
    //        private val commandRegistrar: CommandRegistrar<T>,
    //        private val category: Category,
    //        private val context: Context
    //    ) : Menu() {
    //        init {
    //            text = category.name
    //            update()
    //            setOnShowing { update() }
    //        }
    //
    //        private fun update() {
    //            items.clear()
    //            for (c in commandRegistrar.commands) {
    //                if (c.category == category) {
    //                    val item = CommandMenuItem(target, c, context)
    //                    items.add(item)
    //                }
    //            }
    //        }
    //    }
}