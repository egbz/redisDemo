package org.egbz.controller;

import org.egbz.until.RedisUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.Jedis;

import java.util.Collections;
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
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    @Autowired
    private Redisson redisson;

    @GetMapping("/buyGoods")
    public String buyGoods() throws Exception {
        String value = UUID.randomUUID() + Thread.currentThread().getName();

        RLock rLock = redisson.getLock(REDIS_LOCK);
        rLock.lock();
        try {
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
        } finally {
            rLock.unlock();



//            // 解锁. 使用lua脚本的版本(推荐)
//            Jedis jedis = RedisUtils.getJedis();
//            String script = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then\n" +
//                    "    return redis.call(\"del\",KEYS[1])\n" +
//                    "else\n" +
//                    "    return 0\n" +
//                    "end";
//            try {
//                Object o = jedis.eval(script, Collections.singletonList(REDIS_LOCK), Collections.singletonList(value));
//                if ("1".equals(o.toString())) {
//                    System.out.println("------del redis lock success");
//                } else {
//                    System.out.println("------del redis lock failed");
//                }
//            } finally {
//                if (null != jedis) {
//                    jedis.close();
//                }
//            }
        }
    }

}
