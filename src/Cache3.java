import computable.Computable;
import computable.ExpensiveFunction;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class Cache3<A,V> implements Computable<A,V> {
    private final Computable<A,V> c;

    private final Map <A, Future<V>> cache = new ConcurrentHashMap<>();

    public Cache3(Computable<A,V> c){
        this.c=c;
    }
    @Override
    public V compute(A arg) throws Exception {
        Future<V> f = cache.get(arg);
        if (f == null ) {
            Callable<V> callable= () -> c.compute(arg);
            FutureTask<V> ft=new FutureTask<>(callable);

            f=cache.putIfAbsent(arg, ft);
            if(f==null){
                f=ft;
                System.out.println("计算");
                ft.run();
            }
        }

        return f.get();
    }

    public static void main(String[] args) {
        Cache3<String,Integer> expensiveComputer =new Cache3<>(new ExpensiveFunction());
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
