package computable;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class MayFail implements Computable<String ,Integer>{

    @Override
    public Integer compute(String args) throws Exception {

        Double random=Math.random();
        if(random>0.5){
            throw new IOException("读取文件出错");
        }
        Thread.sleep(3000);
        return Integer.valueOf(args);
    }
}
