package com.Fnlo.Redis.Test;

import redis.clients.jedis.Jedis;

import javax.swing.*;
import java.util.List;

public class Test {

    private static final String REDIS_PASSWORD = "highgo123";

    public static void main(String[] args) throws Exception{
        System.out.println("enter");
        //连接本地的Redis服务
        Jedis jedis = new Jedis("localhost");
        System.out.println("连接成功");
        jedis.auth(REDIS_PASSWORD);
        //查看服务是否运行
        System.out.println("服务正在运行：" + jedis.ping());

        Test.connect1();
        Test.encode();
    }

    /**
     * aisdugsia
     */
    public static void connect1(){
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        jedis.auth(REDIS_PASSWORD);
        System.out.println(jedis.ping("nihao"));

        jedis.lpush("site-list", "Runoob", "Runoob2", "Runoob3");
        jedis.lpush("site-list", "Google");
        jedis.lpush("site-list", "Taobao");

        List<String> list = jedis.lrange("site-list", 0, 4);
        for (String node : list){
            System.out.println(node);
        }
    }

    public static void encode(){
        Jedis jedisEncode = new Jedis("localhost");
        jedisEncode.auth(REDIS_PASSWORD);
        jedisEncode.set("key1", "你好");

        String value1 = jedisEncode.get("key1");
        System.out.println(value1);

    }
}
