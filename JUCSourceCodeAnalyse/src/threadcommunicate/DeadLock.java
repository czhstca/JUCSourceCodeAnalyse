package threadcommunicate;

/**
 * 演示线程间的死锁现象
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class DeadLock {

	/**
	 * 避免死锁发生的常用方法：
	 * 1.让程序只有一个锁(不现实)
	 * 2.定义获取锁的顺序，每个线程都按相同顺序获取锁
	 * 3.使用trylock方式设置超时获取锁，当超时仍未获取锁时返回失败而不是无限等待
	 */
	
    private final Object left = new Object();
    private final Object right = new Object();
    
    public void leftRight() throws Exception
    {
        synchronized (left)
        {
            Thread.sleep(2000);
            synchronized (right)
            {
                System.out.println("leftRight end!");
            }
        }
    }
    
    public void rightLeft() throws Exception
    {
        synchronized (right)  //两个方法获取锁的顺序不一致，导致死锁发生
        {
            Thread.sleep(2000);
            synchronized (left)
            {
                System.out.println("rightLeft end!");
            }
        }
    }
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		DeadLock dl = new DeadLock();
		DeadThread1 th1 = new DeadThread1(dl);
		DeadThread2 th2 = new DeadThread2(dl);
		th1.start();
		th2.start();
	}

}


class DeadThread1 extends Thread{
	
	private DeadLock dl;
	
	public DeadThread1(DeadLock dl){
		this.dl = dl;
	}
	
	
	@Override
	public void run() {
		try
        {
            dl.leftRight();
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
}

class DeadThread2 extends Thread{
	
	private DeadLock dl;
	
	public DeadThread2(DeadLock dl){
		this.dl = dl;
	}
	
	
	@Override
	public void run() {
		try
        {
            dl.rightLeft();;
        } 
        catch (Exception e)
        {
            e.printStackTrace();
        }
	}
}



