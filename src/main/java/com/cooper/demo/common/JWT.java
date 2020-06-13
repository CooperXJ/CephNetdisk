package com.cooper.demo.common;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import java.util.Date;


/**
 * JWT  ---  Token
 * */
public class JWT {
    private static long ttl = 30 * 60 * 1000L;
    private static String key = "user_token";
    /**
     * ttl：失效时间
     * user:用户信息
     */
    public static String createToken(String username) {
        long now = System.currentTimeMillis();
        long exp = now + ttl;
        JwtBuilder jwtBuilder = Jwts.builder()
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, key);
        jwtBuilder.setSubject(username);
        jwtBuilder.setExpiration(new Date(exp));
        return jwtBuilder.compact();
    }

    public static String parseToken(String token) {
        try {
            return Jwts.parser().setSigningKey(key).parseClaimsJws(token).getBody().getSubject();
        }catch (Exception e){
            return null;//查询解析异常返回null
        }
    }

}
