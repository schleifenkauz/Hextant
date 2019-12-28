/**
 * @author Nikolaus Knop
 */

package hextant.serial

/**
 * A [HextantFile] works as a wrapper around some object of type [T]. It can be in one of three states:
 * 1. The *in-memory-state*, where the content object is in the working memory
 * 2. The *file-state*, where the content object was serialized to the physical file associated and garbage collected
 * 3. The *deleted-state*, where the associated file was deleted and the object was garbage collected
 */
interface HextantFile<T : Any> {
    /**
     * Serializes the content object to the associated file
     * @throws IllegalStateException if the content object was garbage collected or the file has been deleted
     */
    fun write()

    /**
     * If the content object is in working memory just return it, otherwise deserialize it from the file
     * @throws IllegalStateException if the file has been deleted
     */
    fun get(): T

    /**
     * Deletes the associated file.
     * After this method has been called all invocation on this object throw an [IllegalStateException]
     * @throws IllegalStateException if the file has been deleted
     */
    fun delete()
}