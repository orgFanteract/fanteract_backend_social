package fanteract.social.filter


import org.springframework.stereotype.Component

@Component
class MlToxicityClient {

    /**
     * 0.0 ~ 1.0 사이의 점수를 반환한다고 가정
     * - 0.0: 전혀 공격적이지 않음
     * - 1.0: 매우 공격적
     */
    fun getToxicityScore(text: String): Double {
        // TODO: 실제로는 외부 ML 서비스 호출 or 내장 모델 호출
        // 여기서는 임시로 항상 0.2(안전) 반환
        return 0.2
    }
}