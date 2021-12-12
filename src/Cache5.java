import computable.Computable;
import computable.MayFail;

import java.util.Map;
import java.util.concurrent.*;

public class Cache5<A,V> implements Computable<A,V> {
    private final Computable<A,V> c;

    private final Map <A, Future<V>> cache = new ConcurrentHashMap<>();

    public Cache5(Computable<A,V> c){
        this.c=c;
    }
    @Override
    public V compute(A arg) throws InterruptedException, ExecutionException {
        while (true) {
            Future<V> f = cache.get(arg);
            if (f == null) {
                Callable<V> callable = () -> c.compute(arg);
                FutureTask<V> ft = new FutureTask<>(callable);

                f = cache.putIfAbsent(arg, ft);
                if (f == null) {
                    f = ft;
                    System.out.println("计算");
                    ft.run();
                }
            }

            try {
                return f.get();
            } catch (InterruptedException e) {
                cache.remove(arg);
                throw e;
            } catch (ExecutionException e) {
                System.out.println("计算错误，需要重试");
                cache.remove(arg);
            } catch (CancellationException e) {
                cache.remove(arg);
                System.out.println("被取消了");
            }
        }
    }

    public V compute(A arg,long expire) throws ExecutionException, InterruptedException {
        if(expire>0){
            excutor.schedule((Runnable) () -> expire(arg),expire,TimeUnit.MILLISECONDS);
        }
        return compute(arg);
    }

    public V computeRandomExpire(A arg) throws ExecutionException, InterruptedException {
        long expire=(long)Math.random()*1000;
        return compute(arg,expire);
    }

    private synchronized void expire(A arg) {
        Future<V> future=cache.get(arg);
        if(future!=null){
            if(!future.isDone()){
                System.out.println("任务取消");
                future.cancel(true);
            }
            System.out.println("清除缓存");
            cache.remove(arg);
        }
    }

    public final static ScheduledExecutorService excutor=new ScheduledThreadPoolExecutor(5);

    public static void main(String[] args) throws InterruptedException {
        Cache5<String,Integer> expensiveComputer =new Cache5<>(new MayFail());
        new Thread(() -> {
            try {
                Integer result = expensiveComputer.compute("666",5000L);
                System.out.println("第一次的计算结果：" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                Integer result = expensiveComputer.compute("667");
                System.out.println("第三次的计算结果：" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try {
                Integer result = expensiveComputer.compute("666");
                System.out.println("第二次的计算结果：" + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        Thread.sleep(6000L);
        try {
            Integer result=expensiveComputer.compute("666");
            System.out.println("主线程计算结果"+result);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
}
