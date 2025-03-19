package com.intel.papyrusbaby.firebase

import android.annotation.SuppressLint
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// JSON 구조에 맞춘 Author 데이터 클래스
data class Author(
    val name: String,               // 작가 이름
    val occupation: List<String>,   // 작가 직업
    val quotes: List<String>,       // 작가 명언 -> 자세히 다이얼 로그
    val works: List<String>,        // 작가 작품 -> 자세히 다이얼 로그
    val genres: List<String>,       // 작가 장르 -> 해시 태그
    val imageUrl: String            // 이미지 URL
)

// AuthorRepository 객체를 통해 한 번의 요청으로 데이터를 불러와 캐싱
object AuthorRepository {

    private var cachedAuthors: List<Author>? = null

    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()

    // 기존 콜백 방식 함수 대신에 suspend 함수를 사용하여 비동기 처리
    suspend fun fetchAuthors(): List<Author> {
        // 이미 불러온 데이터가 있다면 캐시된 데이터를 반환
        if (cachedAuthors != null) {
            return cachedAuthors!!
        }

        // Firestore에서 데이터를 가져오는 비동기 작업
        return try {
            // authors 컬렉션의 모든 문서를 가져옴
            val snapshot = firestore.collection("authors")
                .get() // 필요 시 Source.SERVER 지정 가능
                .await()

            // snapshot.documents: author01, author02, ... 문서 목록
            val authorsList = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null

                val name = data["name"] as? String ?: return@mapNotNull null
                val occupation = data["occupation"] as? List<String> ?: emptyList()
                val quotes = data["quotes"] as? List<String> ?: emptyList()
                val works = data["works"] as? List<String> ?: emptyList()
                val genres = data["genres"] as? List<String> ?: emptyList()
                val imageUrl = data["image_url"] as? String ?: ""

                Author(name, occupation, quotes, works, genres, imageUrl)
            }

            cachedAuthors = authorsList
            authorsList
        } catch (e: Exception) {
            emptyList()
        }
    }
}