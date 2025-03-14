package com.intel.papyrusbaby.flask


import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

object OpenAiServer {
    private val client = OkHttpClient()

    fun sendRequestToServer(
        author: String,
        documentType: String,
        scenario: String,
        callback: (String?, String?) -> Unit
    ) {
        // Flask 서버
        val url = "https://starfish-evolved-molly.ngrok-free.app/generate_letter"

        // JSON 생성
        val json = JSONObject().apply {
            put("author", author)
            put("documentType", documentType)
            put("scenario", scenario)
        }

        // MediaType.parse(...) 대신 확장 함수 사용
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()

        // RequestBody.create(...) 대신 확장 함수 사용
        val requestBody = json.toString().toRequestBody(mediaType)

        // POST 요청 생성
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        // 비동기 호출
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                // 서버 연결 실패 시
                callback(null, e.message)
            }

            override fun onResponse(call: Call, response: Response) {
                // response.body() → response.body (프로퍼티)
                // response.code() → response.code (프로퍼티)
                if (response.isSuccessful) {
                    val responseBody = response.body?.string() ?: ""
                    try {
                        val jsonResponse = JSONObject(responseBody)
                        // 서버가 {"result": "..."} 형태로 보내준다고 가정
                        val result = jsonResponse.optString("result", "")
                        callback(result, null)
                    } catch (e: Exception) {
                        callback(null, e.localizedMessage)
                    }
                } else {
                    callback(null, "Server Error: ${response.code}")
                }
            }
        })
    }
}
