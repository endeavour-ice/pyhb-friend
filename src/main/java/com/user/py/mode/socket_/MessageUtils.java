package com.user.py.mode.socket_;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.user.py.designPatten.singleton.GsonUtils;

/**
 * @author ice
 * @date 2022/7/13 20:37
 * 消息工具类
 */

public class MessageUtils {
    public static String getMessage(boolean isSystemMessage, String fromName, Object message) {
        try {
            ResultMessage<Object> result = new ResultMessage<>();
            result.setSystem(isSystemMessage);
            result.setMessage(message);
            if (fromName != null) {
                result.setFromName(fromName);
            }
            ObjectMapper mapper = new ObjectMapper();
            String s = mapper.writeValueAsString(result);
            // 将对象转换成json数据
            System.out.println(s+"=========s======");
            Gson gson = GsonUtils.getGson();
            String toJson = gson.toJson(result);
            System.out.println(toJson+"===toJson============");
            return s;

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return null;
    }
}
