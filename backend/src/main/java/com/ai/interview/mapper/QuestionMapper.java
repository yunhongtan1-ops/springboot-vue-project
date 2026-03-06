package com.ai.interview.mapper;

import com.ai.interview.entity.Question;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface QuestionMapper {

    @Select("SELECT id, title, content, question_type AS type, difficulty FROM questions ORDER BY id ASC")
    List<Question> findAll();

    @Select("SELECT id, title, content, question_type AS type, difficulty FROM questions WHERE id = #{id}")
    Question findById(@Param("id") Long id);

    @Insert("INSERT INTO questions(title, content, question_type, difficulty) VALUES(#{title}, #{content}, #{type}, #{difficulty})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(Question question);

    @Update("UPDATE questions SET title = #{title}, content = #{content}, question_type = #{type}, difficulty = #{difficulty} WHERE id = #{id}")
    int updateById(Question question);

    @Delete("DELETE FROM questions WHERE id = #{id}")
    int deleteById(@Param("id") Long id);
}