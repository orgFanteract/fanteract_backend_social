package fanteract.social.dto.outer

data class CreateBoardOuterRequest(
    val title: String,
    val content: String,
)

data class UpdateBoardOuterRequest(
    val title: String,
    val content: String,
)

data class CreateCommentOuterRequest(
    val content: String,
)

data class UpdateCommentOuterRequest(
    val content: String,
)
