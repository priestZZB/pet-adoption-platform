package com.pet.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * 应用启动时清除缓存，确保数据一致性（如商品库存）
 */
@Slf4j
@Component
public class CacheClearRunner implements ApplicationRunner {

    @Autowired
    private CacheManager cacheManager;

    @Override
    public void run(ApplicationArguments args) {
        String[] cacheNames = {"product", "pet"};
        for (String name : cacheNames) {
            org.springframework.cache.Cache cache = cacheManager.getCache(name);
            if (cache != null) {
                cache.clear();
                log.info("启动时已清除缓存: {}", name);
            }
        }
    }
}
