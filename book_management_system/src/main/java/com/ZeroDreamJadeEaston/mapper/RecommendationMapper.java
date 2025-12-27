package com.ZeroDreamJadeEaston.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ZeroDreamJadeEaston.domain.entity.Recommendation;
import com.ZeroDreamJadeEaston.domain.vo.RecommendationVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface RecommendationMapper extends BaseMapper<Recommendation> {

    // 联表查询，获取推荐信息+书籍详情
    @Select("SELECT r.*, b.title as bookTitle, b.cover as bookCover, b.author as bookAuthor, b.introduction as bookIntro " +
            "FROM recommendation r " +
            "LEFT JOIN book b ON r.isbn = b.isbn " +
            "ORDER BY r.create_time DESC")
    List<RecommendationVo> getRecommendationList();
}