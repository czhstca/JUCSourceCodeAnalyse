package basicknowledge;


//通过extends Thread的方式创建线程
class Mythread1 extends Thread{
	
	@Override
	public void run() {
		for(int i=0; i<5; i++){
			System.out.println(Thread.currentThread().getName() + " is run......");
		}
	}
}

//通过 implements Runnable的方式创建线程
class Mythread2 implements Runnable{
	
	@Override
	public void run() {
		for(int i=0; i<5; i++){
			System.out.println(Thread.currentThread().getName() + " is run......");
		}
	}
}


/**
 * 本类演示创建线程的两种方式:extends Thread 和  implements Runnable
 * 应该使用第二种  implements Runnable的方式创建线程，这样还可以按需继承别的类，并且代码耦合度较低。
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadCreate {
	
	public static void main(String[] args) {
		// 使用extends Thread的方式创建一个新线程
		Mythread1 th1 = new Mythread1();
		//注意：使用这种创建线程的方式，调用thread的run()和start()方法有什么区别？
		//thread.run() 这个方法只是主线程调用了MyThread1这个类的run()方法而已，主线程会输出5次信息，其实并没有创建新的线程(通过执行结果为  main is run...可以看出)
		//thread.start() 这个方法才是新创建线程并执行新线程的任务应该使用的方法，新创建的线程会输出5次信息(通过执行结果为 Thread-0 is run... 可以看出 )
		th1.run();
		th1.start();
		
		
		//使用implements runnable方式创建一个新线程,效果和上面是一样的
		Mythread2 th2 = new Mythread2();
		Thread t  = new Thread(th2);
		t.start();
		
		//这是使用implements runnable方式创建一个新线程并启动的匿名内部类的写法,效果和上面是一样的
		new Thread(new Runnable() {
			public void run() {
				for(int i=0; i<5; i++){
					System.out.println(Thread.currentThread().getName() + " is run......");
				}
			}
		}).start();
		
		//这里在main线程中打印5次结果，根据输出结果可以看到这里的打印的5条记录并不是一定在最后打印的
		//也就是说线程的启动具有不确定性
		for(int i=0; i<5; i++){
			System.out.println(Thread.currentThread().getName() + " is run......");
		}
	}

}
