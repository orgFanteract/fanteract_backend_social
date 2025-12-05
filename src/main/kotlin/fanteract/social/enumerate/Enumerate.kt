package fanteract.social.enumerate

enum class Status {
    ACTIVATED,
    DELETED,
}

enum class AlarmStatus {
    CREATED,
    DELETED,
    UPDATED,
}

enum class ChatroomJoinStatus{
    JOIN,
    LEAVE,
}

enum class RiskLevel {
    ALLOW,
    WARN,
    BLOCK,
}

enum class ActivePoint(
    val point: Int,
) {
    BOARD(10),
    COMMENT(3),
    CHAT(1),
    HEART(1),
}

enum class Balance(
    val cost: Int,
) {
    BOARD(3),
    COMMENT(1),
    CHAT(1),
    HEART(1),
}

enum class ContentType{
    BOARD,
    COMMENT,
    CHAT,
    BOARD_HEART,
    COMMENT_HEART,
}