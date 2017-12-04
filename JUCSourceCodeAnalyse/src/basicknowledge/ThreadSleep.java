package basicknowledge;

/**
 * Thread的sleep方法示例
 * sleep方法能够让 当前"正在执行的线程"休眠,并且不会释放锁，但是会让出CPU执行资源给其他线程
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadSleep {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyThread5 th5 = new MyThread5();
		System.out.println("begin = " + System.currentTimeMillis());
		th5.start();
		System.out.println("end = " + System.currentTimeMillis());
	}

}

class MyThread5 extends Thread{
	
	@Override
	public void run() {
        try
        {
            System.out.println("run threadName = " + 
                    this.getName() + " begin");
            Thread.sleep(2000);  //在这里让当前线程休眠2s
            System.out.println("run threadName = " + 
                    this.getName() + " end");
        } 
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
	}
	
}
