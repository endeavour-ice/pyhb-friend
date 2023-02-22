package com.user.py.mode.utils;

import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class IpUtilSealUp {
    private static final Map<String, ExpiringMap<String, Integer>> map= new ConcurrentHashMap<>();

    public static void addIpList(String ip) {
        ExpiringMap<String, Integer> em= map.getOrDefault(ip,
                ExpiringMap.builder().variableExpiration().build());
        em.put(ip, 0,
                ExpirationPolicy.CREATED, 1, TimeUnit.MILLISECONDS);
        map.put(ip, em);
    }

    public static boolean selectByIp(String ips) {
        if (!StringUtils.hasText(ips)) {
            return false;
        }
        if (map.isEmpty()) {
            return false;
        }
        return map.get(ips) != null;
    }

}