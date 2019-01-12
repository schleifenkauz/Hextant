package hextant.undo

class AnEdit : AbstractEdit() {
    override val actionDescription: String
        get() = "An Edit"

    var undone: Boolean = false
        private set

    override fun doRedo() {
        undone = false
    }

    override fun doUndo() {
        undone = true
    }
}