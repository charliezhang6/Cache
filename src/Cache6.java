import computable.ExpensiveFunction;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.*;

public class Cache6 {
    static Cache5<String,Integer> expensiveComputer = new Cache5<>(new ExpensiveFunction());
    public static CountDownLatch countDownLatch=new CountDownLatch(1);
    public static void main(String[] args) {
        ExecutorService service= Executors.newFixedThreadPool(100);
        long start=System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            service.submit(() -> {
                Integer result = null;
                try {
                    System.out.println(Thread.currentThread().getName()+"开始等待");
                    countDownLatch.await();
                    SimpleDateFormat dateFormat=ThreadSafeFormatter.dateFormatter.get();
                    String time=dateFormat.format(new Date());
                    System.out.println(Thread.currentThread().getName()+"  "+time+"被放行");
                    result = expensiveComputer.compute("666");
                    countDownLatch.countDown();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.println(result);
            });
        }
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        countDownLatch.countDown();
        service.shutdown();
    }

}
class ThreadSafeFormatter {

    public static ThreadLocal<SimpleDateFormat> dateFormatter = new ThreadLocal<SimpleDateFormat>() {

        //每个线程会调用本方法一次，用于初始化
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("mm:ss");
        }

        //首次调用本方法时，会调用initialValue()；后面的调用会返回第一次创建的值
        @Override
        public SimpleDateFormat get() {
            return super.get();
        }
    };
}