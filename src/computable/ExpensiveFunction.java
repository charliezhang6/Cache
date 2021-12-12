package computable;

public class ExpensiveFunction implements Computable<String,Integer>{
    @Override
    public Integer compute(String args) throws InterruptedException {
        Thread.sleep(5000);
        return Integer.valueOf(args);
    }
}
