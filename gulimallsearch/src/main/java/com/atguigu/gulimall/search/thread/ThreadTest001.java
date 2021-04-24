package com.atguigu.gulimall.search.thread;

import java.util.concurrent.*;

public class ThreadTest001 {
    //当前系统池只有一两个，每一个异步任务，提交给线程池去执行
    public static ExecutorService service = Executors.newFixedThreadPool(10);
    ThreadPoolExecutor executor = new ThreadPoolExecutor(
            5,
            200,
            10,
            TimeUnit.SECONDS,
            new LinkedBlockingDeque<>(),
            Executors.defaultThreadFactory(),
            new ThreadPoolExecutor.AbortPolicy()
    );

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        CompletableFuture.runAsync(()->{
            System.out.println("当前线程，"+Thread.currentThread().getId());
            int i = 10/5;
            System.out.println("运行结果"+i);
        },service);
        //方法执行完成后的感知
        CompletableFuture<Integer> future = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程，" + Thread.currentThread().getId());
            int i = 10 / 0;
            System.out.println("运行结果" + i);
            return i;
        }, service).whenComplete((result,excption)->{
            //虽然能得到异常信息，但是没法修改返回数据
            System.out.println("异步任务成功完成！"+result+"异常是"+excption);
        }).exceptionally(throwable -> {
            //可以感知异常，同事返回默认值
            return 10;
        });
        //方法完成后的处理
        CompletableFuture<Integer> future1 = CompletableFuture.supplyAsync(() -> {
            System.out.println("当前线程，" + Thread.currentThread().getId());
            int i = 10 / 5;
            System.out.println("运行结果" + i);
            return i;
        }, service).handle((result,throwable)->{
            //如果没有异常
            if (result!=null){
                return result*2;
            }
            //如果有异常
            if(throwable!=null){
                return 0;
            }
            return 0;
        });
        Integer integer = future.get();
        System.out.println("运行结果"+integer);

        CompletableFuture<Integer> future2 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务2线程，" + Thread.currentThread().getId());
            int i = 10 /2;
            System.out.println("任务2结束" + i);
            return i;
        }, service);

        CompletableFuture<Integer> future3 = CompletableFuture.supplyAsync(() -> {
            System.out.println("任务3线程，" + Thread.currentThread().getId());
            int i = 10 / 5;
            System.out.println("任务3结束" + i);
            return i;
        }, service);
        //在两个线程执行完之后获取两个线程的结果
        future2.thenAcceptBothAsync(future3,(f1,f2)->{
            System.out.println("任务3开始的结果。。。"+f1+"--->"+f2);
        },service);

        CompletableFuture<String> combineAsync = future2.thenCombineAsync(future3, (f1, f2) -> {
            return f1 + "--->" + f2;
        }, service);
        Integer integer1 = future.get();
        System.out.println("运行结果"+integer);
       /* FutureTask futureTask = new FutureTask<>(new Callable01());
        new Thread(futureTask).start();

        //等待整个线程执行完成，获取返回结果
        Integer integer = (Integer) futureTask.get();
        System.out.println("运行结果"+integer);*/
        service.execute(new Runable01());
        Executors.newCachedThreadPool();//core是0 可回收
        //Executors.newScheduledThreadPool();//定时任务的线程
        Executors.newSingleThreadExecutor();//单线程线程池，后台从队列里面取任务，挨个执行

    }
    public static class Thread01 extends Thread{
        @Override
        public void run(){
            System.out.println("当前线程，"+Thread.currentThread().getId());
            int i = 5;
            System.out.println("运行结果"+i);
        }

    }
    public static class Runable01 implements Runnable{
        @Override
        public void run(){
            System.out.println("当前线程，"+Thread.currentThread().getId());
            int i = 10/5;
            System.out.println("运行结果"+i);
        }
    }
    public static class Callable01 implements Callable<Integer> {

        @Override
        public Integer call() throws Exception {
            System.out.println("当前线程，" + Thread.currentThread().getId());
            int i = 5;
            System.out.println("运行结果" + i);
            return i;
        }
    }
}
/*class Callable01 implements Callable<Integer> {

    @Override
    public Integer call() throws Exception {
        System.out.println("当前线程，"+Thread.currentThread().getId());
        int i = 5;
        System.out.println("运行结果"+i);
        return i;
    }
}*/

