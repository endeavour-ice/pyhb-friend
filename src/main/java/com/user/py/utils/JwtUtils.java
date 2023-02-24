package com.user.py.utils;

import com.google.gson.Gson;
import com.user.py.designPatten.singleton.GsonUtils;
import com.user.py.mode.domain.User;
import com.user.py.common.ErrorCode;
import com.user.py.exception.GlobalException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;

import static com.user.py.mode.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 对session数据进行加密防止破解
 */
public class JwtUtils {

    private static final Gson gson = GsonUtils.getGson();
    private static final String APP_SECRET = "ukc8BDbRigUDaY6pZFfWus2jZWLPHO"; // 随机密钥


    // 生成token字符串
    public static String getJwtToken(User user) {
        String userString = gson.toJson(user);
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                .setSubject("ice")
                .claim("user", userString)
                .signWith(SignatureAlgorithm.HS256, APP_SECRET)
                .compact();
    }


    /**
     * 根据token获取用户
     *
     * @param request
     * @return
     */
    public static User getMemberIdByJwtToken(HttpServletRequest request) {
        try {
            String jwtToken = (String) request.getSession().getAttribute(USER_LOGIN_STATE);
            if (!StringUtils.hasText(jwtToken)) {
                return null;
            }
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(APP_SECRET).parseClaimsJws(jwtToken);
            Claims claims = claimsJws.getBody();
            String userString = (String) claims.get("user");
            if (!StringUtils.hasText(userString)) {
                throw new GlobalException(ErrorCode.NO_LOGIN);
            }
            return gson.fromJson(userString, User.class);
        } catch (Exception e) {
            throw new GlobalException(ErrorCode.NO_LOGIN);
        }
    }

}