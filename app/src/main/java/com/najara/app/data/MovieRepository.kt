package com.najara.app.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class MovieRepository {

    // ⚠️ यहाँ अपना असली CSV API endpoint डालें
    private val csvUrl = "https://my-csv-api.sandip945235.workers.dev/"

    private val client = OkHttpClient()

    suspend fun fetchMovies(): List<Movie> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(csvUrl).build()
            val response = client.newCall(request).execute()
            val csv = response.body?.string() ?: return@withContext emptyList()
            parseCsv(csv)
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun parseCsv(csv: String): List<Movie> {
        val lines = csv.split("\n").filter { it.isNotBlank() }
        if (lines.size < 2) return emptyList()

        val header = lines[0].split(",").map { it.trim().lowercase() }
        val titleIdx = header.indexOf("title")
        val posterIdx = header.indexOf("poster")
        val catIdx = header.indexOf("category")
        val embedIdx = header.indexOf("embedlink")
        val dlIdx = header.indexOf("downloadlink")

        // Optional columns (अगर नहीं हैं तो -1 रहेंगे)
        val trailerIdx = header.indexOf("trailer")
        val ratingIdx = header.indexOf("rating")
        val printIdx = header.indexOf("print")
        val industryIdx = header.indexOf("industry")
        val languageIdx = header.indexOf("language")
        val qualityIdx = header.indexOf("quality")

        if (titleIdx == -1 || posterIdx == -1 || catIdx == -1 ||
            embedIdx == -1 || dlIdx == -1) return emptyList()

        return lines.drop(1).mapNotNull { line ->
            val cols = splitCsvLine(line)
            if (cols.size <= maxOf(titleIdx, posterIdx, catIdx, embedIdx, dlIdx))
                return@mapNotNull null

            // Safe getter — अगर column नहीं है तो खाली string
            fun get(idx: Int): String =
                if (idx >= 0 && idx < cols.size) cols[idx] else ""

            Movie(
                title = cols[titleIdx],
                poster = cols[posterIdx],
                category = cols[catIdx],
                embedLink = cols[embedIdx],
                downloadLink = cols[dlIdx],
                trailer = get(trailerIdx),
                rating = get(ratingIdx),
                print = get(printIdx),
                industry = get(industryIdx),
                language = get(languageIdx),
                quality = get(qualityIdx)
            )
        }
    }

    private fun splitCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false
        for (c in line) {
            when {
                c == '"' -> inQuotes = !inQuotes
                c == ',' && !inQuotes -> { result.add(sb.toString().trim()); sb.clear() }
                else -> sb.append(c)
            }
        }
        result.add(sb.toString().trim())
        return result
    }
}
