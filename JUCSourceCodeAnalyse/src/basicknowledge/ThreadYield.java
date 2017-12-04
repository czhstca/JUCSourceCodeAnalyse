package basicknowledge;

/**
 * Thread类的 yield方法示例
 * yield方法表示当前线程让出CPU执行资源，但是让出的时间不确定，即有可能刚让出资源马上又抢占到资源，也有可能好长一段时间后才又抢占到资源（这一点注意和join区分开来）
 * yield同样不会释放锁
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadYield {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyThread6 th6 = new MyThread6();
		th6.start();
		
	}

}


class MyThread6 extends Thread{
	
	@Override
	public void run() {
		
		long beginTime = System.currentTimeMillis();
        int count = 0;
        for (int i = 0; i < 50000000; i++)
        {
            Thread.yield();
            count = count + i + 1;
        }
        long endTime = System.currentTimeMillis();
        System.out.println("用时：" + (endTime - beginTime) + "毫秒！");  //每次执行结果都不同，说明yield()让步的时间是不确定的
		
	}
	
}
