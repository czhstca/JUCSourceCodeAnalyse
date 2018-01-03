package locks;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 自定义锁的基本使用方式（以AQS作为基础框架）
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class MyLock implements Lock{   //自定义独占锁

	//定义一个静态内部类，实现了AbstractQueuedSynchronizer
	//这个类中的方法就是自定义锁MyLock实际调用的方法
    private static class Sync extends AbstractQueuedSynchronizer {
	      //返回当前独占锁是否为被获取状态  1为被获取  0为未获取
    	  //实际判断的就是AQS内部的int state变量的值
	      protected boolean isHeldExclusively() {
	        return getState() == 1;
	      }
	 
	      // 如果state为0，则尝试获取独占锁
	      public boolean tryAcquire(int acquires) {
	        assert acquires == 1; // Otherwise unused
	        if (compareAndSetState(0, 1)) {  //CAS设置state变量状态
	          setExclusiveOwnerThread(Thread.currentThread());  //将拥有锁的线程设置为当前线程
	          return true;
	        }
	        return false;
	      }
	 
	      // 需要释放独占锁，则将state设置为0
	      protected boolean tryRelease(int releases) {
	        assert releases == 1; // Otherwise unused
	        if (getState() == 0) throw new IllegalMonitorStateException();
	        setExclusiveOwnerThread(null);  //将拥有锁的线程设置为null，即当前没有线程拥有锁
	        setState(0);   //state变量设置为0
	         return true;
	      }
	 
	      // 提供一个Condition对象
	      Condition newCondition() { return new ConditionObject(); }
	      
	}
	
    private final Sync sync = new Sync();
    
	//只需要将MyLock提供给用户操作的方法代理到静态内部类Sync的相应方法即可
	@Override
	public void lock() {
		// TODO Auto-generated method stub
		sync.acquire(1);
	}

	@Override
	public void lockInterruptibly() throws InterruptedException {
		// TODO Auto-generated method stub
		sync.acquireInterruptibly(1);
	}

	@Override
	public boolean tryLock() {
		// TODO Auto-generated method stub
		return sync.tryAcquire(1);
	}

	@Override
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return sync.tryAcquireNanos(1, unit.toNanos(time));
	}

	@Override
	public void unlock() {
		// TODO Auto-generated method stub
		sync.release(1);
	}

	@Override
	public Condition newCondition() {
		// TODO Auto-generated method stub
		return sync.newCondition();
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		MyLock lock = new MyLock();
		lock.lock();
		lock.unlock();
		
	}

}
