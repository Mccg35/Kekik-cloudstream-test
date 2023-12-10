// ! Bu araç @keyiflerolsun tarafından | @KekikAkademi için yazılmıştır.

package com.keyiflerolsun

import android.util.Log
import com.lagradost.cloudstream3.*
import com.lagradost.cloudstream3.utils.*
import com.fasterxml.jackson.annotation.JsonProperty

open class OkRu : ExtractorApi() {
    override val name            = "OkRu"
    override val mainUrl         = "https://odnoklassniki.ru"
    override val requiresReferer = false

    override suspend fun getUrl(url: String, referer: String?, subtitleCallback: (SubtitleFile) -> Unit, callback: (ExtractorLink) -> Unit) {
        val ext_ref = referer ?: ""
        Log.d("Kekik_${this.name}", "url » ${url}")

        val user_agent = mapOf("User-Agent" to "Mozilla/5.0 (Windows NT 10.0; rv:120.0) Gecko/20100101 Firefox/120.0")

        val video_req  = app.get(url, headers=user_agent).text.replace("\\&quot;", "\"").replace("\\\\", "\\")
            .replace(Regex("\\\\u([0-9A-Fa-f]{4})")) { matchResult ->
                Integer.parseInt(matchResult.groupValues[1], 16).toChar().toString()
            }
        val videos_str = Regex("""\"videos\":(\[[^\]]*\])""").find(video_req)?.groupValues?.get(1) ?: throw ErrorLoadingException("Video not found")
        val videos = AppUtils.tryParseJson<List<OkRuVideo>>(videos_str) ?: throw ErrorLoadingException("Video not found")

        for (video in videos) {
            Log.d("Kekik_${this.name}", "video » ${video}")

            val video_url = if (video.url.startsWith("//")) "https:${video.url}" else video.url

            callback.invoke(
                ExtractorLink(
                    source  = "${this.name} - ${video.name}",
                    name    = "${this.name} - ${video.name}",
                    url     = video_url,
                    referer = url,
                    quality = Qualities.Unknown.value,
                    headers = user_agent,
                    isM3u8  = false
                )
            )
        }
    }

    data class OkRuVideo(
        @JsonProperty("name") val name: String,
        @JsonProperty("url")  val url: String,
    )
}
