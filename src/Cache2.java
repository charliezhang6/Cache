import computable.Computable;
import computable.ExpensiveFunction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Cache2<A,V> implements Computable<A,V> {
    private final Computable<A,V> c;
    private final Map<A,V> cache = new ConcurrentHashMap<>();
    public Cache2(Computable<A,V> c){
        this.c=c;
    }
    @Override
    public V compute(A arg) throws Exception {
        System.out.println("进入缓存");
        V result = cache.get(arg);
        if(result==null){
            result=c.compute(arg);
            cache.put(arg,result);
        }
        return result;
    }

    public static void main(String[] args) {
        Cache2<String,Integer> cacheexpensivecomputer =new Cache2<>(new ExpensiveFunction());
        try {
            Integer result =cacheexpensivecomputer.compute("666");
            System.out.println("第一次计算结果："+result);
            result=cacheexpensivecomputer.compute("666");
            System.out.println("第二次计算结果："+result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
