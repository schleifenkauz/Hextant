package hextant.impl

import java.lang.ref.WeakReference
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*

internal object RefSearch {
    private fun Class<*>.allFields(): Sequence<Field> = sequence {
        yieldAll(declaredFields.asIterable())
        if (superclass != null) {
            yieldAll(superclass.allFields())
        }
    }

    private fun Any.objectToString(): String = javaClass.name + "@" + System.identityHashCode(this)

    fun getRefPath(origin: Any, target: Any) {
        val parent = IdentityHashMap<Any, Edge?>()
        val q: Queue<Any> = LinkedList()
        q.add(origin)
        while (!q.isEmpty()) {
            val o = q.poll()
            if (o === target) break
            val cls: Class<*> = o.javaClass
            if (cls.isPrimitive) continue
            else if (cls == WeakReference::class.java) continue
            else if (cls.isArray) enqueueArrayItems(o, parent, q)
            else enqueueFields(cls, o, parent, q)
        }
        var o = target
        val chain: MutableList<Edge?> = ArrayList()
        while (o != origin && parent.containsKey(o)) {
            val e = parent[o]
            chain.add(e)
            o = e!!.parent
        }
        if (o != origin) {
            println("No path found")
            return
        }
        for (e in chain.reversed()) {
            println(e!!.parent.objectToString() + " -> " + e.field)
        }
        println(target.objectToString())
    }

    private fun enqueueArrayItems(
        o: Any,
        parent: IdentityHashMap<Any, Edge?>,
        q: Queue<Any>
    ) {
        val l = java.lang.reflect.Array.getLength(o)
        for (i in 0 until l) {
            val ref = java.lang.reflect.Array.get(o, i) ?: continue
            if (parent.containsKey(ref)) continue
            q.add(ref)
            parent[ref] = Edge(o, "[$i]", ref)
        }
    }

    private fun enqueueFields(
        cls: Class<*>,
        o: Any,
        parent: IdentityHashMap<Any, Edge?>,
        q: Queue<Any>
    ) {
        for (f in cls.allFields()) {
            if (f.modifiers and Modifier.STATIC != 0) continue
            f.isAccessible = true
            try {
                val ref = f[o] ?: continue
                if (parent.containsKey(ref)) continue
                q.add(ref)
                parent[ref] = Edge(o, f.name, ref)
            } catch (ex: IllegalAccessException) {
                ex.printStackTrace()
            }
        }
    }

    private class Edge(var parent: Any, var field: String, var child: Any)
}