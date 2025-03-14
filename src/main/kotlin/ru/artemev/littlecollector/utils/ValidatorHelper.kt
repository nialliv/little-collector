package ru.artemev.littlecollector.utils

import java.io.File

object ValidatorHelper {

    fun validateFilePath(file: File) {
        if (!(file.exists() && file.canRead() && file.extension == "json" && file.length() != 0L)) {
            throw IllegalArgumentException("File is not valid!")
        }
    }

    fun validateRange(text: String) {
        if (!text.matches(Regex("^\\d{1,4}-\\d{1,4}$"))) {
            throw IllegalArgumentException("Range is invalid")
        }

        val splitText = text.split("-")
        if (splitText[0] > splitText[1]) {
            throw IllegalArgumentException("First greater then second")
        }

    }

    fun checkChapterExistsInExport(
        requiredChapters: Set<Int>,
        chapterMap: Map<Int, String?>
    ) {
        if (!chapterMap.keys.containsAll(requiredChapters)) {
            throw RuntimeException(
                "Range is not all contains chapters - not founded - ${
                    requiredChapters.minus(
                        chapterMap.keys
                    )
                }"
            )
        }

        requiredChapters
            .forEach {
                val href = chapterMap[it]
                if (href.isNullOrBlank() || !validateHrefs(href)) {
                    throw RuntimeException("All links for $it not valid...")
                }
            }

    }

    fun validateTargetFolder(targetFolder: String) {
        if (targetFolder.isNotBlank()) {
            val file = File(targetFolder)
            if (file.exists() && file.canWrite() && file.isDirectory) {
                return
            }
        }
        throw IllegalArgumentException("Target folder is not valid")
    }


    private fun validateHrefs(href: String): Boolean = Regex("([A-Za-z]*://)?\\S*").containsMatchIn(href)

}