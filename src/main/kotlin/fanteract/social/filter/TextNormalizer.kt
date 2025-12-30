package fanteract.social.filter

import org.springframework.stereotype.Component

@Component
class TextNormalizer {
    fun normalize(raw: String): String {
        // 1) 소문자
        var text = raw.lowercase()

        // 2) 양쪽 공백 제거
        text = text.trim()

        // 3) 반복 공백 하나로
        text = text.replace("\\s+".toRegex(), " ")

        // 4) 특수문자 제거(또는 공백으로 치환)
        text = text.replace("[^0-9a-zA-Z가-힣ㄱ-ㅎㅏ-ㅣ ]".toRegex(), "")

        return text
    }
}
