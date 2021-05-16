package org.egbz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

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

    @GetMapping("/buyGoods")
    public String buyGoods() {
        String value = UUID.randomUUID() + Thread.currentThread().getName();
        Boolean flag = stringRedisTemplate.opsForValue().setIfAbsent(REDIS_LOCK, value); //setNX

        if (flag) {
            // get key     看库存数量够不够
            String res = stringRedisTemplate.opsForValue().get("goods:001");
            int goodsNumber = res == null ? 0 : Integer.parseInt(res);

            if (goodsNumber > 0) {
                int remaining = goodsNumber - 1;
                stringRedisTemplate.opsForValue().set("goods:001", String.valueOf(remaining));
                System.out.println("成功买到商品, 库存剩余: " + remaining + "      serverPort: " + serverPort);
                stringRedisTemplate.delete(REDIS_LOCK);
                return "成功买到商品, 库存剩余: " + remaining + "      serverPort: " + serverPort;
            }
            System.out.println("------------------- [failed]");
            stringRedisTemplate.delete(REDIS_LOCK);
            return "failed";
        } else {
            System.out.println(serverPort + "抢锁失败" );
            return "抢锁失败";
        }
    }

}
