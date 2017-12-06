package threadcommunicate;

/**
 * 演示线程通信的 wait-notify机制(保证线程间的同步)
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class WaitNotify {

	/**
	 * 关于wait和notify的一些结论和使用注意点:
	 * 1.在调用wait()方法前必须先获取锁，也就是wait()和notify()方法必须在synchronized方法/代码块中被调用
	 *   如果在synchronized方法/代码块之外调用了wait()和notify()方法，则会抛出java.lang.IllegalMonitorStateException异常
	 * 
	 * 2.调用wait()方法后，当前线程或释放共享资源的锁，并从Runnable状态变更为waiting状态进入等待队列
	 *   直到接收到notify信号或是interrupt信号才会变为blocked或terminal状态.
	 * 
	 * 	  调用nofity()方法后，可以随机唤醒等待队列中的一个线程，使其从waiting状态变更为blocked状态(因为还没有获得资源锁).
	 *   注意：notify()方法不会释放锁！！！它只是起到唤醒其他等待线程的作用，当前拥有锁的线程会等执行完后续代码或出异常才会将锁释放出来
	 *   所以被notify唤醒的线程并不会立即获得锁，必须等唤醒它的线程释放锁之后才有机会获得资源锁
	 *   
	 *   调用notifyAll()方法后，会将所有在等待队列中的线程全部唤醒，让它们去争抢资源锁.
	 * 
	 */
	
	
	public static void main(String[] args) throws Exception {
		Object lock = new Object();
		MyThread7 th7 = new MyThread7(lock);
		th7.start();
		Thread.sleep(3000);  //主线程沉睡3s，以看到wait-notify效果
		MyThread8 th8 = new MyThread8(lock);
		th8.start();
		
	}

}


class MyThread7 extends Thread{
	
	private Object lock;
	
	public MyThread7(Object lock){
		this.lock = lock;
	}
	
	@Override
	public void run() {
		
		try {
			synchronized (lock) {
				 System.out.println("开始------wait time = " + System.currentTimeMillis());
			     lock.wait();
			     System.out.println("结束------wait time = " + System.currentTimeMillis());
			     
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}

class MyThread8 extends Thread{
	
	private Object lock;
	
	public MyThread8(Object lock){
		this.lock = lock;
	}
	
	@Override
	public void run() {
		
		synchronized (lock) {
			 System.out.println("开始------notify time = " + System.currentTimeMillis());
		     lock.notify();
		     System.out.println("结束------notify time = " + System.currentTimeMillis());
		}

	}
	
}