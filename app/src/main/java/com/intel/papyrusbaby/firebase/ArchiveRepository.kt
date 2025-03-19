package com.intel.papyrusbaby.firebase

import android.annotation.SuppressLint
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

object ArchiveRepository {
    @SuppressLint("StaticFieldLeak")
    private val firestore = FirebaseFirestore.getInstance()
    private val auth get() = FirebaseAuth.getInstance()

    suspend fun addArchiveItem(archiveItem: ArchiveItem) {
        val user = auth.currentUser ?: return

        // (1) 트랜잭션으로 counterDoc의 인덱스 +1
        val newIndex = firestore.runTransaction { transaction ->
            val counterRef = firestore.collection("users")
                .document(user.uid)
                .collection("generatedArchivesMeta")
                .document("counterDoc")

            val snapshot = transaction.get(counterRef)

            if (!snapshot.exists()) {
                // 문서가 없으면 새로 만들고 1부터 시작
                transaction.set(counterRef, mapOf("lastIndex" to 1L))
                return@runTransaction 1L
            } else {
                // 문서가 있으면 +1
                val currentIndex = snapshot.getLong("lastIndex") ?: 0L
                val updatedIndex = currentIndex + 1
                transaction.update(counterRef, "lastIndex", updatedIndex)
                return@runTransaction updatedIndex
            }
        }.await()

        // (2) Firestore 난수 ID를 자동 생성 (add() 또는 push)
        val newDocRef = firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .document()  // 여기서 랜덤 ID 생성

        // (3) 문서에 인덱스 필드 저장
        val data = mapOf(
            "index" to newIndex, // 숫자 인덱스
            "writtenDate" to archiveItem.writtenDate,
            "author" to archiveItem.author,
            "docType" to archiveItem.docType,
            "detail" to archiveItem.detail,
            "generatedText" to archiveItem.generatedText
        )

        newDocRef.set(data).await()
    }

    suspend fun getAllArchives(): List<ArchiveItem> {
        val user = auth.currentUser ?: return emptyList()
        // (4) index 내림차순으로 정렬해서 가져오기
        val snapshot = firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .orderBy("index", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            ArchiveItem(
                docId = doc.id,
                writtenDate = data["writtenDate"] as? String ?: "",
                author = data["author"] as? String ?: "",
                docType = data["docType"] as? String ?: "",
                detail = data["detail"] as? String ?: "",
                generatedText = data["generatedText"] as? String ?: ""
            )
        }
    }

    suspend fun getArchiveItem(docId: String): ArchiveItem? {
        val user = FirebaseAuth.getInstance().currentUser ?: return null
        val doc = firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .document(docId)
            .get()
            .await()

        val data = doc.data ?: return null
        return ArchiveItem(
            writtenDate = data["writtenDate"] as? String ?: "",
            author = data["author"] as? String ?: "",
            docType = data["docType"] as? String ?: "",
            detail = data["detail"] as? String ?: "",
            generatedText = data["generatedText"] as? String ?: "",
            docId = doc.id
        )
    }
}
