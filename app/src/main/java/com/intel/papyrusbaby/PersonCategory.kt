package com.intel.papyrusbaby

sealed class PersonCategory(
    val name: String,
    val thumbnail: Int,
    val description: String,
    val tags: List<String>
) {
    data object YoonDongJu : PersonCategory("윤동주", R.drawable.yoondongju, "잎새에 이는 바람에도\n" +
            "나는 괴로워했다.", tags = listOf("시인"))
    data object KimSoweol : PersonCategory("김소월", R.drawable.yoondongju, "Greek philosopher", tags = listOf("정치인", "비즈니스", "격식있는"))
    data object Obama : PersonCategory("오바마", R.drawable.yoondongju, "44th US President", tags = listOf("Politics", "Business", "Media"))

    companion object {
        fun getAll(): List<PersonCategory> = listOf(YoonDongJu, KimSoweol, Obama)
    }
}