package com.ai.interview.mapper;

import com.ai.interview.entity.PracticeRecord;
import com.ai.interview.vo.PracticeRecordVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PracticeRecordMapper {

    @Insert("INSERT INTO practice_records(user_id, question_id, answer_content) VALUES(#{userId}, #{questionId}, #{answerContent})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(PracticeRecord practiceRecord);

    @Select("SELECT pr.id, u.username AS username, pr.question_id AS questionId, q.title AS questionTitle, pr.answer_content AS answerContent, pr.created_at AS submittedAt FROM practice_records pr INNER JOIN users u ON pr.user_id = u.id INNER JOIN questions q ON pr.question_id = q.id WHERE pr.id = #{id}")
    PracticeRecordVO findById(@Param("id") Long id);

    @Select("SELECT pr.id, u.username AS username, pr.question_id AS questionId, q.title AS questionTitle, pr.answer_content AS answerContent, pr.created_at AS submittedAt FROM practice_records pr INNER JOIN users u ON pr.user_id = u.id INNER JOIN questions q ON pr.question_id = q.id WHERE u.username = #{username} ORDER BY pr.created_at DESC, pr.id DESC")
    List<PracticeRecordVO> findByUsername(@Param("username") String username);
}