/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuBar
import org.nikok.hextant.Context
import org.nikok.hextant.core.command.Command
import org.nikok.hextant.core.command.Commands
import org.nikok.hextant.get

/**
 * @return a [ContextMenu] with the commands registered for the receiver as items
 * * When a menu item is clicked the associated command will be executed and when it has parameters those will be
 *   get from the user with a argument prompt.
 * * When a command is not applicable (but registered) on the receiver the associated menu item will be disabled
 */
fun <T : Any> T.commandContextMenu(context: Context)
        : ContextMenu = CommandContextMenu(this, context[Commands].of(this::class), context)

/**
 * @return a [MenuBar] with the commands registered for the receiver as items
 *  * When a menu item is clicked the associated command will be executed and when it has parameters those will be
 *   get from the user with a argument prompt.
 * * When a command is not applicable (but registered) on the receiver the associated menu item will be disabled
 * * The menu items will be ordered in the different menus by their [Command.category]
 */
fun <T : Any> T.commandMenuBar(context: Context)
        : MenuBar = CommandMenuBar.newInstance(this, context[Commands].of(this::class), context)