package com.ZeroDreamJadeEaston.controller;

import com.ZeroDreamJadeEaston.domain.Result;
import com.ZeroDreamJadeEaston.domain.entity.Recommendation;
import com.ZeroDreamJadeEaston.domain.vo.RecommendationVo;
import com.ZeroDreamJadeEaston.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendation")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/list")
    public Result<List<RecommendationVo>> list() {
        return recommendationService.getAllRecommendations();
    }

    @PostMapping("/add")
    public Result<String> add(@RequestBody Recommendation recommendation) {
        return recommendationService.addRecommendation(recommendation);
    }

    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Integer id) {
        return recommendationService.deleteRecommendation(id);
    }
}