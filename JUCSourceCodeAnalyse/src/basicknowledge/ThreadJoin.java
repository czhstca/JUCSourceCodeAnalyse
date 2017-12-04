package basicknowledge;

/**
 * Thread的 join方法，表示其余线程必须等待调用join()的线程执行完或执行了指定时间后，才能继续执行
 * 注意：通过join底层代码可以看到其实调用的就是wait()方法，所以线程调用了join()方法会释放当前该线程获取的锁
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadJoin {

	public static void main(String[] args) throws InterruptedException {
		// TODO Auto-generated method stub

		Thread th = new Thread(new MyThread());
		th.start();
		
		th.join();  //自定义线程调用了join方法,主线程必须等待该线程执行结束后才会继续执行(换言之该线程若无法执行完成，则其余线程会无限等待下去)
		th.join(2000);  //自定义线程调用了超时join方法，主线程必须等待该线程执行了指定时间之后才会继续执行
		System.out.println("我想等待自定义线程MyThread执行完之后才执行....");
		
	}

}

class MyThread implements Runnable{

	@Override
	public void run() {
       try
        {
            int secondValue = 5000;
            Thread.sleep(secondValue);   //该线程启动后会沉睡5s
        } 
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
	}
	
}