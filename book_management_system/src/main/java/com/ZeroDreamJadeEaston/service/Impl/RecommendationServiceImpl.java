package com.ZeroDreamJadeEaston.service.Impl;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.entity.Recommendation;
import com.ZeroDreamJadeEaston.domain.vo.RecommendationVo;
import com.ZeroDreamJadeEaston.mapper.RecommendationMapper;
import com.ZeroDreamJadeEaston.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecommendationServiceImpl implements RecommendationService {

    private final RecommendationMapper recommendationMapper;

    @Override
    public Result<String> addRecommendation(Recommendation recommendation) {
        if (recommendation.getCreateTime() == null) {
            recommendation.setCreateTime(new Date());
        }
        recommendationMapper.insert(recommendation);
        return Result.success("推荐成功");
    }

    @Override
    public Result<String> deleteRecommendation(Integer id) {
        recommendationMapper.deleteById(id);
        return Result.success("删除成功");
    }

    @Override
    public Result<List<RecommendationVo>> getAllRecommendations() {
        return Result.success(recommendationMapper.getRecommendationList());
    }
}