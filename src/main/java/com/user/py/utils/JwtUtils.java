package com.user.py.utils;

import com.google.gson.Gson;
import com.user.py.common.ErrorCode;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.exception.GlobalException;
import com.user.py.mode.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static com.user.py.mode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 对session数据进行加密防止破解
 */
@Slf4j
public class JwtUtils {

    private static final Gson gson = GsonUtils.getGson();
    private static final String APP_SECRET = "ukc8BDbRigUDaY6pZFfWus2jZWLPHO"; // 随机密钥



    // 生成token字符串
    public static String getJwtToken(User user) {
        String json = gson.toJson(user);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .claim("user",json)
                .setSubject("ice")
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compact();
    }

    /**
     * 根据token获取用户
     *
     * @return
     */
    public static User getMemberIdByJwtToken(HttpServletRequest request) {
        try {
            String jwtToken = (String) request.getSession().getAttribute(USER_LOGIN_STATE);
            if (!StringUtils.hasText(jwtToken)) {
                return null;
            }
            return getMemberIdByJwtToken(jwtToken);
        } catch (Exception e) {
            return null;
        }
    }
    public static User getMemberIdByJwtToken(String token) {
        try {
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(token);
            Claims claims = claimsJws.getBody();
            String userString = (String) claims.get("user");
            System.out.println(userString);
            if (!StringUtils.hasText(userString)) {
                throw new GlobalException(ErrorCode.NO_LOGIN);
            }
            return gson.fromJson(userString, User.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

}