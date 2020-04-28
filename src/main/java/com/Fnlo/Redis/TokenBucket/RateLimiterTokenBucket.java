package com.Fnlo.Redis.TokenBucket;

import com.google.common.util.concurrent.RateLimiter;

/**
 * @author ：fenglu<fnlo1995@gmail.com>
 * @version :     $version$
 * @date ：Created in 2020/4/28 14:26
 * @description ：使用RateLimiter实现令牌桶
 */
public class RateLimiterTokenBucket {

    private static final RateLimiter rateLimiter = RateLimiter.create(2);

    public static void main(String[] args) throws Exception{

        for (int i = 0; i < 10; i++) {
//            RunThread thread = new RunThread();
            RunUntilGetPermitesThread thread = new RunUntilGetPermitesThread();
            thread.start();
        }

    }

    /**
     * 没能获得令牌的 退出
     */
    public static class RunThread extends Thread {
        @Override
        public void run() {
            //尝试获取令牌
            if (rateLimiter.tryAcquire()) {//默认为1
                System.out.println("[" + Thread.currentThread().getName() + "] 业务执行完成");
            } else {
                System.out.println("[" + Thread.currentThread().getName() + "] 业务繁忙，请重试");
            }
        }
    }

    /**
     * 获取令牌，并等待执行
     */
    public static class RunUntilGetPermitesThread extends Thread{
        @Override
        public void run() {
            rateLimiter.acquire(2);//一次拿两个
            System.out.println("[" + Thread.currentThread().getName() + "] 业务执行完成");
        }
    }
}
