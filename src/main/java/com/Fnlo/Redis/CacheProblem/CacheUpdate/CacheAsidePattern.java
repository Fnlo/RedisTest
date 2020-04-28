package com.Fnlo.Redis.CacheProblem.CacheUpdate;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.params.SetParams;

/**
 * @author ：fenglu<fnlo1995@gmail.com>
 * @date ：Created in 2020/4/28 10:08
 * @description : 本类使用CacheAsidePattern的方式进行缓存更新
 * @version : $version$
 */
public class CacheAsidePattern {
    public static final String KEY = "CacheAsidePattern";
    public static final String VALUE_OLD_IN_DB = "oldValue";
    public static final String VALUE_CHANGE_IN_DB = "newValue";
    public static final int TIMEOUT = 1; //seconds
    public static SetParams params;

    /**
     * 模拟数据库中存储的数据
     */
    public static String dbValue = VALUE_OLD_IN_DB;

    public static void main(String[] args) throws Exception {
        Jedis jedis = new Jedis("localhost");
        jedis.auth("highgo123");
        if (jedis.exists(KEY)) {
            jedis.del(KEY);
        }
        params = new SetParams();
        params.ex(TIMEOUT);
        jedis.set(KEY, dbValue, params);

        for (int i = 0; i < 10; i++) {
            RunThread runThread = new RunThread();
            runThread.start();
        }

        Thread.sleep(200);
        update();
//        updateError();

    }

    public static class RunThread extends  Thread {

        @Override
        public void run() {
            try {
                int i = 0;
                while(i < 50){
                    i++;
                    select();
//                    Thread.sleep(100);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public static void select() {
            Jedis jedis = new Jedis("localhost");
            jedis.auth("highgo123");
            boolean isFromDB = false;
            String res;
            if (jedis.exists(KEY)) {
                res = jedis.get(KEY);
            } else {
                //从数据库中读取数据，并刷新缓存
                res = dbValue;
                jedis.set(KEY, res, params);
                isFromDB = true;
            }

            System.out.println("[" + Thread.currentThread().getName() + "] get res :[" + res + "] from " + (isFromDB ? "DB" : "Cache"));
        }

    }

    /**
     * 使用先修改数据库再另缓存失效的方式实现缓存更新
     */
    public static void update() {
        Jedis jedis = new Jedis("localhost");
        jedis.auth("highgo123");
        String res;
        //修改数据库
        dbValue = VALUE_CHANGE_IN_DB;
        System.out.println("[" + Thread.currentThread().getName() + "] UPDATE dbValue");
        res = dbValue;
        //更新缓存
        jedis.set(KEY, res, params);
        System.out.println("[" + Thread.currentThread().getName() + "] UPDATE cacheValue");
        //令缓存失效
//        jedis.del(KEY);
//        System.out.println("[" + Thread.currentThread().getName() + "] DELETE cacheValue");
    }

    /**
     * 使用先删除缓存再修改数据库的方式实现缓存更新。
     * 存在的问题是：
     *      当Update执行在“删除缓存”与“修改数据库”之间时，存在另外一个读取线程读取了数据库的旧值并读入缓存，
     *     导致本次KEY的生存时间内都为脏数据，若此时再有对脏数据的写操作，会导致将该脏数据再次入库。
     *
     */
    public static void updateError(){
        Jedis jedis = new Jedis("localhost");
        jedis.auth("highgo123");
        String res;
        //删除缓存
        jedis.del(KEY);
        System.out.println("[" + Thread.currentThread().getName() + "] DELETE cacheValue");
        //修改数据库
        dbValue = VALUE_CHANGE_IN_DB;
        System.out.println("[" + Thread.currentThread().getName() + "] UPDATE dbValue");
    }
}
