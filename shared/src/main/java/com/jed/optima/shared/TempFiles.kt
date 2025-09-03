package com.jed.optima.shared

import java.io.File

object TempFiles {
    @JvmStatic
    @JvmOverloads
    fun createTempFileWithName(name: String, extension: String = ""): File {
        val tmpDir = getTempDir()
        return File(tmpDir, name + extension).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(): File {
        val tmpDir = getTempDir()
        return File(tmpDir, getRandomName(tmpDir)).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(extension: String): File {
        val tmpDir = getTempDir()
        return File(tmpDir, getRandomName(tmpDir) + extension).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(name: String, extension: String): File {
        val tmpDir = getTempDir()
        return File(tmpDir, name + getRandomName(tmpDir) + extension).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(parent: File, name: String): File {
        return File(parent, name + getRandomName(parent)).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(parent: File, name: String, extension: String): File {
        return File(parent, name + extension).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun createTempFile(parent: File): File {
        return File(parent, getRandomName(parent)).also {
            it.createNewFile()
            it.deleteOnExit()
        }
    }

    @JvmStatic
    fun getPathInTempDir(name: String, extension: String): String {
        val tmpDir = getTempDir()
        val file = File(tmpDir, name + extension)
        file.deleteOnExit()
        return file.absolutePath
    }

    @JvmStatic
    fun getPathInTempDir(): String {
        val tmpDir = getTempDir()
        return File(tmpDir, getRandomName(tmpDir)).absolutePath
    }

    @JvmStatic
    @JvmOverloads
    fun createTempDir(parent: File? = null): File {
        val dir = if (parent != null) {
            File(parent, getRandomName(parent))
        } else {
            File(getPathInTempDir())
        }

        dir.mkdir()
        return dir
    }

    private fun getTempDir(): File {
        val tmpDir = File(System.getProperty("java.io.tmpdir", "."), "com.jed.optima.shared.TempFiles")
        if (!tmpDir.exists()) {
            tmpDir.mkdir()
        }

        return tmpDir
    }

    private fun getRandomName(parent: File): String {
        val existing = parent.listFiles()

        var candiate = _root_ide_package_.com.jed.optima.shared.strings.RandomString.randomString(16)
        while (existing!!.any { it.name.contains(candiate) }) {
            candiate = _root_ide_package_.com.jed.optima.shared.strings.RandomString.randomString(16)
        }

        return candiate
    }
}
