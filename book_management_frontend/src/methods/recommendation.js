import request from "@/util/request";

// 获取推荐列表
export function getRecommendationListService() {
    return request.get('/recommendation/list');
}

// 添加推荐
export function addRecommendationService(data) {
    return request.post('/recommendation/add', data);
}

// 删除推荐
export function deleteRecommendationService(id) {
    return request.delete(`/recommendation/delete/${id}`);
}

//修改推荐信息 
export function updateRecommendationService(data) {
    return request.put('/recommendation/update', data);
}

// 调用 AI 生成推荐理由 

export function aiGenerateReasonService(title) {
    return request.get('/book/ai', {
        params: { title: "请为《" + title + "》写一段100字以内的精彩推荐语" } 
    });
}