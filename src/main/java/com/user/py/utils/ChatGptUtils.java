package com.user.py.utils;

import com.unfbx.chatgpt.OpenAiClient;
import com.unfbx.chatgpt.entity.common.Choice;
import com.unfbx.chatgpt.entity.completions.CompletionResponse;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import lombok.extern.slf4j.Slf4j;

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

    public static String sendChatG(String sendTex) throws Exception {
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
