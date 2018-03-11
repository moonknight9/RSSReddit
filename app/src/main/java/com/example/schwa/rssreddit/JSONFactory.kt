package com.example.schwa.rssreddit

class JSONFactory {

    companion object {
        var reader : JSONReader? = null
        fun getJSONReader() : JSONReader? {
            var jsonReader : JSONReader? = null
            if (reader != null) {
                jsonReader = JSONReader((reader as JSONReader).listView, (reader as JSONReader).appContext)
                jsonReader.seenSubRedditList = (reader as JSONReader).seenSubRedditList
            }
            return jsonReader
        }
    }
}
