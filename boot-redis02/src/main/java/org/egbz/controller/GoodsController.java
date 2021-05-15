package org.egbz.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author egbz
 * @date 2021/5/15
 */
@RestController
public class GoodsController {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Value("${server.port}")
    private String serverPort;

    @GetMapping("/buyGoods")
    public String buyGoods() {
        // get key     看库存数量够不够
        String res = stringRedisTemplate.opsForValue().get("goods:001");
        int goodsNumber = res == null ? 0 : Integer.parseInt(res);

        if (goodsNumber > 0) {
            int remaining = goodsNumber -1;
            stringRedisTemplate.opsForValue().set("goods:001", String.valueOf(remaining));
            System.out.println("成功买到商品, 库存剩余: " + remaining + "      serverPort: " + serverPort);
            return "成功买到商品, 库存剩余: " + remaining + "      serverPort: " + serverPort;
        }
        System.out.println("------------------- [failed]");
        return "failed";

    }

}
