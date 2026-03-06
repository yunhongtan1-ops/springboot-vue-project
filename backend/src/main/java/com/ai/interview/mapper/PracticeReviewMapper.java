package com.ai.interview.mapper;

import com.ai.interview.entity.PracticeReview;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface PracticeReviewMapper {

    @Select("SELECT id, practice_record_id, status, overall_score, summary, highlight_excerpt, strengths_json, improvements_json, follow_ups_json, generator_name, error_message, created_at, updated_at FROM practice_reviews WHERE practice_record_id = #{practiceRecordId} LIMIT 1")
    PracticeReview findByPracticeRecordId(@Param("practiceRecordId") Long practiceRecordId);

    @Insert("INSERT INTO practice_reviews(practice_record_id, status) VALUES(#{practiceRecordId}, 'PENDING') ON DUPLICATE KEY UPDATE practice_record_id = practice_record_id")
    int insertPending(@Param("practiceRecordId") Long practiceRecordId);

    @Update("UPDATE practice_reviews SET status = 'SUCCESS', overall_score = #{overallScore}, summary = #{summary}, highlight_excerpt = #{highlightExcerpt}, strengths_json = #{strengthsJson}, improvements_json = #{improvementsJson}, follow_ups_json = #{followUpsJson}, generator_name = #{generatorName}, error_message = NULL WHERE practice_record_id = #{practiceRecordId}")
    int updateSuccessByPracticeRecordId(
            @Param("practiceRecordId") Long practiceRecordId,
            @Param("overallScore") Integer overallScore,
            @Param("summary") String summary,
            @Param("highlightExcerpt") String highlightExcerpt,
            @Param("strengthsJson") String strengthsJson,
            @Param("improvementsJson") String improvementsJson,
            @Param("followUpsJson") String followUpsJson,
            @Param("generatorName") String generatorName
    );

    @Update("UPDATE practice_reviews SET status = 'FAILED', error_message = #{errorMessage} WHERE practice_record_id = #{practiceRecordId}")
    int updateFailureByPracticeRecordId(@Param("practiceRecordId") Long practiceRecordId, @Param("errorMessage") String errorMessage);
}