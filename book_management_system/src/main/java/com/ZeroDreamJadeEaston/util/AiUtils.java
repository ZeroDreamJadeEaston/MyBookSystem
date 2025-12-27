package com.ZeroDreamJadeEaston.util;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class AiUtils {

    // Key (已脱敏，上传 GitHub 前请使用此版本)
    // 注意：本地运行项目时，请将 "YOUR_API_KEY_HERE" 替换回您真实的 sk- 开头的密钥！
    private static final String API_KEY = "YOUR_API_KEY_HERE";

    // 硅基流动 API 地址
    private static final String API_URL = "https://api.siliconflow.cn/v1/chat/completions";

    public static String getBookInfoFromAI(String bookName) {
        // 1. 构造提示词
        String prompt = String.format(
                "你是一个专业的图书管理员。请根据书名《%s》查询相关的图书元数据。" +
                        "要求：\n" +
                        "1. 严格返回纯 JSON 格式数据，不要包含 markdown 标记。\n" +
                        "2. JSON 需包含以下字段：\n" +
                        "   - title: 书名\n" +
                        "   - author: 作者\n" +
                        "   - isbn: 请提供一个真实的或符合格式的13位ISBN\n" +
                        "   - introduction: 200字以内的精简简介\n" +
                        "   - cover: 请尝试提供一个网络公开的封面图片URL，如果找不到明确的，请留空字符串 \"\"。\n",
                bookName
        );

        // 2. 构造请求体
        JSONObject userMsg = JSONUtil.createObj()
                .set("role", "user")
                .set("content", prompt);

        JSONObject jsonBody = JSONUtil.createObj()
                .set("model", "deepseek-ai/DeepSeek-V3")
                .set("messages", JSONUtil.createArray().put(userMsg))
                .set("stream", false)
                .set("temperature", 0.7)
                .set("max_tokens", 512);

        try {
            log.info("正在调用 DeepSeek-V3 查询书籍: {}", bookName);

            // 3. 发送请求
            String result = HttpRequest.post(API_URL)
                    .header("Authorization", "Bearer " + API_KEY)
                    .header("Content-Type", "application/json")
                    .body(jsonBody.toString())
                    .timeout(30000) // 30秒超时
                    .execute()
                    .body();

            // log.info("AI原始返回: {}", result);

            // 4. 解析结果
            JSONObject resJson = JSONUtil.parseObj(result);

            // 检查报错
            if (resJson.containsKey("error")) {
                log.error("AI 接口报错: {}", resJson.getStr("error"));
                return null;
            }

            JSONArray choices = resJson.getJSONArray("choices");
            if (choices == null || choices.isEmpty()) return null;

            String content = choices.getJSONObject(0)
                    .getJSONObject("message")
                    .getStr("content");

            // 清洗 Markdown
            return content.replace("```json", "").replace("```", "").trim();

        } catch (Exception e) {
            log.error("AI 调用异常", e);
            return null;
        }
    }
}
