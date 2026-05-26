package com.pss.vertx.common.utils;

//import io.micrometer.core.instrument.Timer;
import io.vertx.core.Future;
//import io.vertx.core.impl.future.Listener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class FutureUtil {

    private static final Logger log = LogManager.getLogger(FutureUtil.class);
    private static final Executor executor = Executors.newVirtualThreadPerTaskExecutor();

    public static  <T> T await(Future<T> future) {
        long l1 = System.currentTimeMillis();
        CountDownLatch latch = new CountDownLatch(1);
        executor.execute(()-> {
            future.onComplete((s)-> latch.countDown());
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        Object result = future.result();
//        if(result instanceof Listener<?> l) {
//            log.error("Instance of listener {}", l);
//        }
        long time = System.currentTimeMillis() - l1;

        return (T) result;
    }
}
