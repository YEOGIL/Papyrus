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
                transaction.set(counterRef, mapOf("lastIndex" to 1L))
                1L
            } else {
                val currentIndex = snapshot.getLong("lastIndex") ?: 0L
                val updatedIndex = currentIndex + 1
                transaction.update(counterRef, "lastIndex", updatedIndex)
                updatedIndex
            }
        }.await()

        // (2) Firestore 난수 ID를 자동 생성
        val newDocRef = firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .document()

        // (3) 문서에 인덱스 필드 및 테마 목록 저장
        val data = mapOf(
            "index" to newIndex,
            "writtenDate" to archiveItem.writtenDate,
            "author" to archiveItem.author,
            "docType" to archiveItem.docType,
            "detail" to archiveItem.detail,
            "generatedText" to archiveItem.generatedText,
            "themeList" to archiveItem.themeList // ← 추가
        )

        newDocRef.set(data).await()
    }

    suspend fun getAllArchives(): List<ArchiveItem> {
        val user = auth.currentUser ?: return emptyList()
        val snapshot = firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .orderBy("index", Query.Direction.DESCENDING)
            .get()
            .await()

        return snapshot.documents.mapNotNull { doc ->
            val data = doc.data ?: return@mapNotNull null
            val themeList = data["themeList"] as? List<String> ?: emptyList()

            ArchiveItem(
                docId = doc.id,
                writtenDate = data["writtenDate"] as? String ?: "",
                author = data["author"] as? String ?: "",
                docType = data["docType"] as? String ?: "",
                detail = data["detail"] as? String ?: "",
                generatedText = data["generatedText"] as? String ?: "",
                themeList = themeList
            )
        }
    }

    suspend fun getArchiveItem(docId: String): ArchiveItem? {
        val user = auth.currentUser ?: return null
        val doc = firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .document(docId)
            .get()
            .await()

        val data = doc.data ?: return null
        val themeList = data["themeList"] as? List<String> ?: emptyList()

        return ArchiveItem(
            docId = doc.id,
            writtenDate = data["writtenDate"] as? String ?: "",
            author = data["author"] as? String ?: "",
            docType = data["docType"] as? String ?: "",
            detail = data["detail"] as? String ?: "",
            generatedText = data["generatedText"] as? String ?: "",
            themeList = themeList
        )
    }

    suspend fun deleteArchiveItem(docId: String) {
        val user = auth.currentUser ?: return
        firestore.collection("users")
            .document(user.uid)
            .collection("generatedArchives")
            .document(docId)
            .delete()
            .await()
    }
}
