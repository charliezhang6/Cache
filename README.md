## 高性能缓存

### 项目介绍

做这个项目主要是因为我们实验室每天做实验要产生大量的数据，而存放数据的数据库已经很久没有维护过了，导致访问速度比较慢，于是就想着给实验室的数据库写一个高性能支持并发访问的缓存

### 实现逻辑

实验数据是键值对的形式，选择用HashMap的数据结构

1. 给缓存加上final关键字，使得其不会被改变，**这里的不会被改变指的是这个引用只会指向这个HashMap而不是说HashMap无法被改变（弄明白Java里面引用和对象的概念）**，也可以让别人知道这是一个缓存

2. 给缓存的实现代码加上synchornized关键字，实现并发安全。但这也会导致两个问题，首先就是性能差，粗暴地直接给整段代码加上synchornized固然可以保证并发安全，但是这个锁的粒度太大了，完全没法并发访问，大大降低了性能，其次就是，每次如果都要在需要用到缓存的地方插入这样一段代码用起来不方便，并且如果缓存的代码需要改动时，所有用到该缓存的代码都要同时改动，代码的可复用性和可维护性都比较差

3. 基于代码复用能力差这个问题，用**装饰者模式**实现缓存，把缓存和计算两个逻辑分开来实现。

   计算方面，先定义一个接口，只有实现了这个接口才能调用这个缓存，同时把计算的数据类型改为泛型，可以兼容更多类型

   ```java
   public interface Computable<A,V> {
       V compute(A args) throws Exception;
   }
   ```

   缓存方面，同样也要实现这个接口，把计算类通过构造函数传入缓存类，缓存类只需要在实现缓存的逻辑里面调用计算类的compute方法即可

   `Cache<String,Integer> cacheexpensivecomputer =new Cache<>(new ExpensiveFunction());`

   装饰者模式就是这样一个通过嵌套来增强功能的模式，装饰者和被装饰者都有相同的超类型，可以用一个或者多个装饰器装饰一个对象，因为都继承于同一个对象，所以在需要原对象的场合可以用新对象替代它

4. 基于性能差，主要是因为并发安全导致的，使用并发安全的ConcurrentHashMap代替原本的HashMap即可，但是，这样无法避免重复计算的问题

5. 使用Future类和Callable接口、以及putIfAbsent避免重复计算

6. 计算过程中有可能出现异常，把Future的get方法用try-catch包裹起来，根据异常种类不同进行处理，人为取消的话就中止，计算错误就重试，同时不要忘记在出错时把对应的值从缓存中移除，不然就会一直出错

7. 出于安全性考虑，要给缓存添加一个过期时间。具体实现方法是重写一个方法，此方法传入的参数除了key之外还有过期时间，使用ScheduledExecutorService线程池来维护，该线程池会在设定时间到了时候执行对应的操作也就是把过期缓存删除，但在高并发访问下，如果许多缓存同时过期，会造成缓存雪崩

8. 为防止缓存雪崩，把缓存过期时间设成随机值

9. 使用线程池测试缓存效果，但这样还是一个线程一个线程创建，没法模拟同时高并发的访问

10. 使用CountDownLatch，在访问前调用await()方法阻塞，等到所有线程都创建完毕时调用countDown()方法进行倒数，这样所有线程就可以同时运行

11. 在每个线程运行的同时，使用ThreadLocal打印每个线程的执行时间，确保其统一执行