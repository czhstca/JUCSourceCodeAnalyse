package basicknowledge;

/**
 * 线程的六种状态
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadStates {

	public static void main(String[] args) {
		//六种状态的定义:
		//1.NEW:新建状态，比如"Thread t = new Thread()"，t就是一个处于NEW状态的线程
		//2.RUNNABLE:可运行状态，这个状态其实又包括两种子状态：等待处理器资源(READY) 和  获取资源并执行中(RUNNING)
		//2-1.READY:线程已经获得锁，正在等待CPU分配时间切片执行权，此时线程在就绪队列中
		//2-2.RUNNING:线程已经获取CPU分配的时间切片执行权，正在执行线程
		
		//3.BLOCKED:阻塞状态，此时线程正在等待获取监视器锁，以便进入一个同步块/方法
		//          此时线程会处于阻塞队列中，并且会不断重新请求执行资源，若请求资源成功，则会转入就绪队列中。
		//4.WAITING:等待状态，某一线程因为调用不带超时的Object的wait()方法、不带超时的Thread的join()方法、LockSupport的park()方法，就会处于等待WAITING状态
		//			此时线程处于等待队列中，它必须等待其他线程的指示(notify)后会继续进入BLOCKED状态，并且处于WAINTING状态的线程会释放CPU执行权和资源（如锁）.
		//5.TIMED_WAITING:超时等待状态，某一线程因为调用带有指定正等待时间的Object的wait()方法、Thread的join()方法、Thread的sleep()方法、LockSupport的parkNanos()方法、LockSupport的parkUntil()方法，就会处于超时等待TIMED_WAITING状态
		//6.TERMINATED：终止状态，线程调用终止或者run()方法执行完成后，线程会处于该状态

	}

}
