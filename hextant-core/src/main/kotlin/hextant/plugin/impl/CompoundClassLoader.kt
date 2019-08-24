package hextant.plugin.impl

internal class CompoundClassLoader : ClassLoader() {
    private val children = mutableSetOf<ClassLoader>()

    init {
        children.add(getSystemClassLoader())
    }

    fun add(classLoader: ClassLoader) {
        children.add(classLoader)
    }

    override fun loadClass(name: String?): Class<*> {
        for (c in children) {
            try {
                return c.loadClass(name)
            } catch (cnf: ClassNotFoundException) {

            }
        }
        throw ClassNotFoundException("Could not find class $name in any of the children class loaders")
    }
}
