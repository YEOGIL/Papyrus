package com.intel.papyrusbaby.ui.theme

import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.intel.papyrusbaby.R
import android.content.Context
import androidx.core.content.res.ResourcesCompat
import android.graphics.Typeface

enum class LetterBackground(val resId: Int) {
    PAPER_1(R.drawable.paper01),
    PAPER_2(R.drawable.paper02),
    PAPER_3(R.drawable.paper03),
    PAPER_4(R.drawable.paper04),
    PAPER_5(R.drawable.paper05)
}

enum class LetterFont(val label: String, val fontResId: Int?) {
    BOLD("기본", R.font.boldandclear),
    CUTE("살랑", R.font.cute),
    HAND("낙엽", R.font.handwriting),
    THIN("매화", R.font.handwritingthin);

    // 폰트 리소스가 있는 경우 FontFamily를 생성, 없으면 기본 폰트 사용
    fun getFontFamily(): FontFamily {
        return if (fontResId != null) FontFamily(Font(fontResId)) else FontFamily.Default
    }

    // Typeface도 context를 이용해 캐싱 가능하도록
    fun getTypeface(context: Context): Typeface {
        return if (fontResId != null) {
            ResourcesCompat.getFont(context, fontResId) ?: Typeface.DEFAULT
        } else {
            Typeface.DEFAULT
        }
    }
}
