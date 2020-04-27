package com.Fnlo.Redis.CacheProblem.CacheAvalanche;

import redis.clients.jedis.Jedis;

/**
 * @author ：fenglu<fnlo1995@gmail.com>
 * @date ：Created in 2020/4/27 14:12
 * @description：本类介绍了对Redis雪崩时的一种处理方式，通过使用SETNX加锁避免雪崩
 * @modified By：
 * @version: $version$
 */
public class SolveCacheAvalanche {

    /**
     * SETNX用到的锁对象
     */
    private static final String LOCK = "serviceLock";
    /**
     * SETNX时设置锁的超时时间
     */
    private static final long TIMEOUT = 2000L;
    /**
     * 业务所需的Key
     */
    private static String key = "fnlo";

    public static void start(){
        //模拟多并发业务逻辑
        for (int i = 0; i < 10; i++){
            Thread thread = new ServiceThread();
            thread.start();
        }
    }

    public static class ServiceThread extends Thread{

        @Override
        public void run() {
            try {
                Jedis jedis = new Jedis("localhost");
                jedis.auth("highgo123");

                String res = jedis.get(key);
                //判断是否已经过期
                if (res == null || res.length() <= 0) {
//                synchronized (LOCK){
                    System.out.println(Thread.currentThread().getName() + "the res is null/invalid.");
                    //等待Lock释放
                    while (jedis.setnx(LOCK, (System.currentTimeMillis() + TIMEOUT) + "") == 0){
                        long now = System.currentTimeMillis();
                        String lockTimeStamp = jedis.get(LOCK);
                        String temp;
                        //如果某个线程异常退出，但是未能del(Lock)，导致死锁，所以为Lock对象添加超时的时间戳
                        if ((now >  Long.parseLong((lockTimeStamp == null || lockTimeStamp.length() <= 0 ) ? "0" : lockTimeStamp)
                                && now > Long.parseLong(((temp = jedis.getSet(LOCK, (now + TIMEOUT) + "")) == null || temp.length() <= 0) ? "0" : temp))){
                            System.out.println(Thread.currentThread().getName() + "超时，getset锁，并添加新的时间戳");
                            break;
                        } else {//一二三四 再来一次
                            Thread.sleep(100);
                        }
                    }

                    //重新尝试从缓存中获取数据
                    res = jedis.get(key);
                    if (res == null || res.length() <= 0){
                        //模拟从数据库中获取数据，业务逻辑
                        res = "res";
                        System.out.println(Thread.currentThread().getName() + "读取数据库数据");

                        jedis.set(key, res);
                        System.out.println(Thread.currentThread().getName() + "业务完成，刷新缓存");
                    } else {
                        System.out.println(Thread.currentThread().getName() + "读取缓存数据成功");
                    }

                    //释放锁
                    jedis.del(LOCK);
                    System.out.println(Thread.currentThread().getName() + "释放锁");
//                }
                }

                //获得结果
                System.out.println(Thread.currentThread().getName() + ": get the res : "  + res);
            } catch (Exception e) {
                System.out.println(Thread.currentThread().getName() + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) throws Exception{
        SolveCacheAvalanche.start();
    }

}
