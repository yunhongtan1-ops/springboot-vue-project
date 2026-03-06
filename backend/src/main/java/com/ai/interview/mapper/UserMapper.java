package com.ai.interview.mapper;

import com.ai.interview.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, nickname, created_at FROM users WHERE username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO users(username, password, nickname) VALUES(#{username}, #{password}, #{nickname})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(User user);
}
