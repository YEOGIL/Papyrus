package com.intel.papyrusbaby.firebase

data class ArchiveItem(
    val writtenDate: String = "",    // 작성일
    val author: String = "",         // 작가
    val docType: String = "",        // 글 형식
    val detail: String = "",         // 상세 내용
    val generatedText: String = "",  // 생성된 텍스트
    val themeList: List<String> = emptyList(), // ← 추가된 필드
    val docId: String = ""           // Firestore 문서 ID
)
