package org.egbz.bootRedis01.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author egbz
 * @date 2021/5/15
 */
@RestController
public class GoodsController {
    public static final String REDIS_LOCK = "sellLock";

    @Autowired
    private StringRedisTemplate  stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buyGoods")
    public String buyGoods() {
        String value = UUID.randomUUID() + Thread.currentThread().getName();
        try {
            // 加锁 加过期时间,  此操作具备原子性
            Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, value, 10L, TimeUnit.SECONDS); //setNX

            if (flag) {
                // get key     看库存数量够不够
                String res = stringRedisTemplate.opsForValue().get("goods:001");
                int goodsNumber = res == null ? 0 : Integer.parseInt(res);

                if (goodsNumber > 0) {
                    int remaining = goodsNumber - 1;
                    stringRedisTemplate.opsForValue().set("goods:001", String.valueOf(remaining));
                    System.out.println("成功买到商品, 库存剩余: " + remaining + "      serverPort: " + serverPort);
                    return "成功买到商品, 库存剩余: " + remaining + "      serverPort: " + serverPort;
                }
                System.out.println("------------------- [failed]");

                return "failed";
            } else {
                System.out.println(serverPort + "抢锁失败");
                return "抢锁失败";
            }
        } finally {
            // 解锁. 不使用lua脚本, 使用redis事务的版本
            for (;;) {
                stringRedisTemplate.watch(REDIS_LOCK);
                if (stringRedisTemplate.opsForValue().get(REDIS_LOCK).equalsIgnoreCase(value)) {
                    stringRedisTemplate.setEnableTransactionSupport(true);
                    stringRedisTemplate.multi();
                    stringRedisTemplate.delete(REDIS_LOCK);
                    List<Object> list = stringRedisTemplate.exec();
                    if (list == null) {
                        continue;
                    }
                }
                stringRedisTemplate.unwatch();
                break;
            }

        }
    }
}
