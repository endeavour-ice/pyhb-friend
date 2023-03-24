package com.user.py.utils;

import cn.hutool.http.HttpRequest;
import com.google.gson.Gson;
import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.common.Choice;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.vo.ChatGptMessageVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @Author ice
 * @Date 2023/2/12 14:04
 * @Description: TODO
 */
@Slf4j
public class ChatGptUtils {
    private static final OpenAiClient openAiClient;

    static {
        try {
            openAiClient = new OpenAiClient(ConstantPropertiesUtils.CG_TOKEN);
        } catch (Exception e) {
            log.error("token 失效 Error={}", e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }

    public static String sendChatGpt(String id, String content) {
        String message = "服务器限流，请稍后再试 | Server was limited, please try again later";
        if (!StringUtils.hasText(id) || !StringUtils.hasText(content)) {
            message = "请输入内容!";
            return message;
        }
        Map<String, String> map = new HashMap<>(3);
        map.put("apiKey", ConstantPropertiesUtils.CG_TOKEN);
        map.put("sessionId", id);
        map.put("content", content);
        try {
            Gson gson = GsonUtils.getGson();
            String json = gson.toJson(map);
            String resp = HttpRequest.post("https://api.openai-proxy.com/v1/chat/completions")
                    .body(json)
                    .execute().body();
            System.out.println(resp);
            ChatGptMessageVo messageVo = gson.fromJson(resp, ChatGptMessageVo.class);
            System.out.println(messageVo);
            Integer code = messageVo.getCode();
            if (code == 200 || "执行成功".equals(messageVo.getMessage())) {
                message = messageVo.getData();
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return message;
    }

    public static String sendChatG(String sendTex) {
        StringBuilder stringBuilder = new StringBuilder();
        CompletionResponse completions = openAiClient.completions(sendTex);
        if (completions == null) {
            return stringBuilder.toString();
        }
        Choice[] choices = completions.getChoices();
        for (Choice choice : choices) {
            String choiceText = choice.getText();
            stringBuilder.append(choiceText);
        }
        String txt = stringBuilder.toString();
        txt = Pattern.compile("\\s*|\t|\r|\n").matcher(txt).replaceAll("");
        return txt;
    }
}
