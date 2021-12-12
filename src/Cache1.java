import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class Cache1 {
    private final HashMap<String,Integer> cache =new HashMap<>();
    public Integer compute (String userid) throws InterruptedException {
        Integer result = cache.get(userid);
        if(result==null){
            result=doCompute(userid);
            cache.put(userid,result);
        }
        return result;
    }
    private Integer doCompute(String userid) throws InterruptedException {
        TimeUnit.SECONDS.sleep(5);
        return Integer.valueOf(userid);
    }

    public static void main(String[] args) {
        Cache1 cache1=new Cache1();
        System.out.println("开始计算");
        try {
            Integer result=cache1.compute("15");
            System.out.println("第一次计算结果："+result);
            result=cache1.compute("15");
            System.out.println("第二次计算结果："+result);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
