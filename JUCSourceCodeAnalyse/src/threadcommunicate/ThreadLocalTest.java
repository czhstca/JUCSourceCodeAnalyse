package threadcommunicate;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocal测试类
 * 
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadLocalTest {

	/**
	 * ThreadLocal总结:
	 * 1.ThreadLocal只是一个工具，一个往各个线程的ThreadLocal.ThreadLocalMap中table的某一位置set一个值的工具
	 * 2.同步与ThreadLocal是解决多线程中数据访问问题的两种思路，前者是数据共享的思路，后者是数据隔离的思路
	 * 3.threadLocal虽然多数情况都是用来在多线程环境下保护临界资源被多次篡改，但少数情况也可以用来在线程间共享一些数据(比如只需要读取拿来用的数据,如servlet)
	 */
	
	
	public static void main(String[] args) {
		// 可以看到对于所有线程共享的threadLocal,每个线程都可以获取和修改
		// 而对于线程独有的threadLocal,每个线程保存的是自己的副本，如果修改对其他线程的副本没有影响
        ThreadLocalThread a = new ThreadLocalThread("ThreadA","aaaa");
        ThreadLocalThread b = new ThreadLocalThread("ThreadB","bbbb");
        ThreadLocalThread c = new ThreadLocalThread("ThreadC","cccc");
        a.start();
        b.start();
        c.start();
	}

}


class ThreadLocalThread extends Thread
{
	//所有线程共享的ThreadLocal
	private static ThreadLocal<String> t1 = new ThreadLocal<String>();
	
	//每个线程自己独有的ThreadLocal
	private ThreadLocal<String> tl2 = new ThreadLocal<String>();
	private String value;
	
	private static AtomicInteger ai = new AtomicInteger();
    
    public ThreadLocalThread(String name,String value)
    {
        super(name);
        this.value = value;
    }
    
    public void run()
    {
        try
        {
            for (int i = 0; i < 3; i++)
            {
                t1.set(ai.addAndGet(1) + "");
                tl2.set(value);
                System.out.println(this.getName() + " get static value--->" + t1.get());
                System.out.println(this.getName() + " get normal value--->" + tl2.get());
                Thread.sleep(200);
            }
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
    }
}