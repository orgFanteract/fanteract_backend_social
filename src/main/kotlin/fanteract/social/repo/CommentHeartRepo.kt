package fanteract.social.repo

import fanteract.social.entity.CommentHeart
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CommentHeartRepo : JpaRepository<CommentHeart, Long> {
    fun findByCommentIdIn(idList: List<Long>): List<CommentHeart>

    fun findByUserIdAndCommentId(
        userId: Long,
        commentId: Long,
    ): CommentHeart?

    fun existsByUserIdAndCommentId(
        userId: Long,
        commentId: Long,
    ): Boolean

    @Modifying
    @Query(
        """
    delete from CommentHeart ch
    where ch.userId = :userId
      and ch.commentId = :commentId
""",
    )
    fun deleteByUserIdAndCommentId(
        @Param("userId") userId: Long,
        @Param("commentId") commentId: Long,
    ): Int

    @Modifying
    @Query(
        """
    delete from CommentHeart ch
    where ch.commentId in :commentIds
""",
    )
    fun deleteByCommentIdIn(
        @Param("commentIds") commentIds: List<Long>,
    ): Int

    @Modifying
    @Query(
        """
    delete from CommentHeart ch
    where ch.commentId = :commentId
""",
    )
    fun deleteByCommentId(
        @Param("commentId") commentId: Long,
    )
}
