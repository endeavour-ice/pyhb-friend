package com.user.py.utils;

import com.theokanning.openai.OpenAiService;
import com.theokanning.openai.completion.CompletionChoice;
import com.theokanning.openai.completion.CompletionRequest;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Pattern;

/**
 * @Author ice
 * @Date 2023/2/12 14:04
 * @Description: TODO
 */
@Slf4j
public class ChatGptUtils {
    private static final OpenAiService service;

    static {
        try {
            service = new OpenAiService(ConstantPropertiesUtils.CG_TOKEN);
        } catch (Exception e) {
            log.error("token 失效 Error={}", e.getMessage());
            throw new GlobalException(ErrorCode.SYSTEM_EXCEPTION);
        }
    }

    public static String sendChatG(String sendTex) throws Exception {
        String txt = "系统繁忙，请稍后!";
        CompletionRequest completionRequest = CompletionRequest.builder()
                .model("text-davinci-003")
                .prompt(sendTex)
                .temperature(0.9D)
                .maxTokens(2048)
                .topP(1.0D)
                .frequencyPenalty(0.0D)
                .presencePenalty(0.6D)
                .build();
        List<CompletionChoice> choices = service.createCompletion(completionRequest).getChoices();
        CompletionChoice completionChoice = choices.get(0);
        if (completionChoice != null) {
            String test = completionChoice.getText();
            if (!StringUtils.hasText(test)) {
                return txt;
            }
            txt = Pattern.compile("\\s*|\t|\r|\n").matcher(test).replaceAll("");
        }
        return txt;
    }
}
