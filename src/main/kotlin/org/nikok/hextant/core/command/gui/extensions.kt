/**
 * @author Nikolaus Knop
 */

package org.nikok.hextant.core.command.gui

import javafx.scene.control.ContextMenu
import javafx.scene.control.MenuBar
import org.nikok.hextant.HextantPlatform
import org.nikok.hextant.core.CorePermissions.Public
import org.nikok.hextant.core.command.*

/**
 * @return a [ContextMenu] with the commands registered for the receiver as items
 * * When a menu item is clicked the associated command will be executed and when it has parameters those will be
 *   get from the user with a argument prompt.
 * * When a command is not applicable (but registered) on the receiver the associated menu item will be disabled
*/
fun <T : Any> T.commandContextMenu(registrar: CommandRegistrar<T> = HextantPlatform[Public, Commands].of(this::class))
        : ContextMenu = CommandContextMenu(this, registrar)

/**
 * @return a [MenuBar] with the commands registered for the receiver as items
 *  * When a menu item is clicked the associated command will be executed and when it has parameters those will be
 *   get from the user with a argument prompt.
 * * When a command is not applicable (but registered) on the receiver the associated menu item will be disabled
 * * The menu items will be ordered in the different menus by their [Command.category]
*/
fun <T : Any> T.commandMenuBar(registrar: CommandRegistrar<T> = HextantPlatform[Public, Commands].of(this::class))
        : MenuBar = CommandMenuBar.newInstance(this, registrar)