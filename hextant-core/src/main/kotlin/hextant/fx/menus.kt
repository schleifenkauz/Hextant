/**
 * @author Nikolaus Knop
 */

package hextant.fx

import javafx.scene.control.*


inline fun menuBar(builder: MenuBarBuilder.() -> Unit) = MenuBarBuilder().apply(builder).build()

class MenuBarBuilder {
    private val menus = mutableListOf<Menu>()

    fun menu(menu: Menu) {
        menus.add(menu)
    }

    inline fun menu(name: String, block: MenuBuilder.() -> Unit) {
        menu(MenuBuilder(name).apply(block).build())
    }

    fun build() = MenuBar(*menus.toTypedArray())
}

class MenuBuilder(private val name: String) {
    private var items = mutableListOf<MenuItem>()

    fun item(name: String, shortcut: Shortcut? = null, action: () -> Unit) {
        val item = MenuItem(name)
        item.accelerator = shortcut?.toCombination()
        item.setOnAction { action() }
        items.add(item)
    }

    fun build() = Menu(name, null, *items.toTypedArray())
}