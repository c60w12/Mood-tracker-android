package com.example.newproject30.ui.addentry

/**
 * Utility for cleaning whitespace and enforcing sentence capitalization on speech transcripts.
 */
object SpeechTextFormatter {

    /**
     * Formats incoming voice transcription text by:
     * 1. Cleaning leading/trailing whitespace and replacing multiple spaces with a single space.
     * 2. Capitalizing the first character of the transcription.
     * 3. Capitalizing the first character of any sentence following sentence-ending punctuation (. ! ?).
     */
    fun format(rawText: String): String {
        val cleaned = rawText.trim().replace(Regex("\\s+"), " ")
        if (cleaned.isEmpty()) return ""

        val sb = StringBuilder(cleaned)

        // Capitalize first character
        if (sb.isNotEmpty() && sb[0].isLowerCase()) {
            sb.setCharAt(0, sb[0].uppercaseChar())
        }

        // Capitalize characters after sentence-ending punctuation (. ! ?)
        var capitalizeNext = false
        for (i in sb.indices) {
            val ch = sb[i]
            if (ch == '.' || ch == '!' || ch == '?') {
                capitalizeNext = true
            } else if (capitalizeNext && ch.isLetter()) {
                sb.setCharAt(i, ch.uppercaseChar())
                capitalizeNext = false
            } else if (capitalizeNext && !ch.isWhitespace()) {
                capitalizeNext = false
            }
        }

        return sb.toString()
    }

    /**
     * Appends [newText] to [existingContent] ensuring single space separation
     * and correct sentence capitalization following sentence-ending punctuation.
     */
    fun appendText(existingContent: String, newText: String): String {
        val trimmedExisting = existingContent.trim().replace(Regex("\\s+"), " ")
        val formattedNew = format(newText)

        if (trimmedExisting.isEmpty()) return formattedNew
        if (formattedNew.isEmpty()) return trimmedExisting

        val capitalizedNew = if (trimmedExisting.endsWith(".") || trimmedExisting.endsWith("!") || trimmedExisting.endsWith("?")) {
            if (formattedNew.isNotEmpty() && formattedNew[0].isLowerCase()) {
                formattedNew[0].uppercaseChar() + formattedNew.substring(1)
            } else {
                formattedNew
            }
        } else {
            formattedNew
        }

        return "$trimmedExisting $capitalizedNew".replace(Regex("\\s+"), " ")
    }
}
