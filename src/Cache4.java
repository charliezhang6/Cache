import computable.Computable;
import computable.ExpensiveFunction;
import computable.MayFail;

import java.io.IOError;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.*;

public class Cache4<A,V> implements Computable<A,V> {
    private final Computable<A,V> c;

    private final Map <A, Future<V>> cache = new ConcurrentHashMap<>();

    public Cache4(Computable<A,V> c){
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

    public static void main(String[] args) {
        Cache4<String,Integer> expensiveComputer =new Cache4<>(new MayFail());
        new Thread(() -> {
            try {
                Integer result = expensiveComputer.compute("666");
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
    }
}
