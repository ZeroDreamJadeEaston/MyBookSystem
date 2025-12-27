package com.ZeroDreamJadeEaston.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ZeroDreamJadeEaston.domain.entity.Reader;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReaderMapper extends BaseMapper<Reader> {
    @Select("SELECT * FROM reader WHERE username = #{username};")
    Reader selectByName(String username);

    void register(Reader reader);

}
