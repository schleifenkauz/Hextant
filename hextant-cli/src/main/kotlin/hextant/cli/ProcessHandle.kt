package hextant.cli

class ProcessHandle(private val command: String, private val proc: Process) {
    var isJoined = false
        private set

    fun join() {
        if (isJoined) error("Command $command already joined")
        val exitCode = proc.waitFor()
        if (exitCode != 0) {
            val cmd = command
            error("Command $cmd finished with non-zero exit code")
        }
        isJoined = true
    }
}