package com.ai.interview.mapper;

import com.ai.interview.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper {

    @Select("SELECT id, username, password, nickname, role, created_at FROM users WHERE username = #{username} LIMIT 1")
    User findByUsername(@Param("username") String username);

    @Insert("INSERT INTO users(username, password, nickname, role) VALUES(#{username}, #{password}, #{nickname}, #{role})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int save(User user);

    @Update("UPDATE users SET password = #{password} WHERE id = #{id}")
    int updatePasswordById(@Param("id") Long id, @Param("password") String password);

    @Update("UPDATE users SET role = #{role} WHERE id = #{id}")
    int updateRoleById(@Param("id") Long id, @Param("role") String role);
}