package com.ZeroDreamJadeEaston.service;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.entity.Recommendation;
import com.ZeroDreamJadeEaston.domain.vo.RecommendationVo;
import java.util.List;

public interface RecommendationService {
    Result<String> addRecommendation(Recommendation recommendation);
    Result<String> deleteRecommendation(Integer id);
    Result<List<RecommendationVo>> getAllRecommendations();
}