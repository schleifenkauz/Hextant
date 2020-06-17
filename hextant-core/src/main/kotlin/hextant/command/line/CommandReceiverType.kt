/**
 * @author Nikolaus Knop
 */

package hextant.command.line

/**
 * The type of command receiver determines whether the commands applicable on editors, expander or views can be used.
 */
enum class CommandReceiverType {
    /**
     * Indicates that the commands applicable on view targets can be used.
     */
    Targets,

    /**
     * Indicates that the commands applicable on expanders can be used.
     */
    Expanders,

    /**
     * Indicates that the commands applicable on editor views can be used.
     */
    Views
}