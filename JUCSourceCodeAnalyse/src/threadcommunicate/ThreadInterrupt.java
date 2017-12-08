package threadcommunicate;

/**
 * 演示线程的中断机制
 * 注意：中断只是给一个线程设置一下中断标记，至于这个线程是否会中断完全取决于它本身，和中断标记没有直接关系
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadInterrupt {

	/**
	 * 和中断相关的方法共有3个:
	 * 1.interrupt():这个方法会给调用它的线程设置一个中断标识位，但是线程是否真的会中断还是依赖于它本身
	 * 2.isInterrupted():这个方法用来测试线程是否已经中断
	 * 					  注意：这个方法不会清除线程的中断标识位
	 * 3.interrupted():这个方法同样是用来测试线程是否已经中断
	 * 				       和上面的方法区别处在于这个方法会清除线程的中断标识位(即连续两次调用该方法必定返回false)
	 * 
	 * 由于中断的不确定性，如果检测中断太频繁则会消耗过多资源导致效率低，但若间隔过长时间才检测中断则可能导致中断得不到及时的响应
	 * 这种情况下就需要对当前业务做一个测试模型来找到最佳的检测中断点
	 * 
	 * 但是像sleep、wait、notify、join，这些方法遇到中断会自动清除中断标识此时必须有对应的措施，可以直接在catch块中处理，也可以抛给上一层
	 * 
	 */
	
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		
		 Runnable runnable = new Runnable()
		    {
		        public void run()
		        {
		            while (true)   //该线程无限轮询是否被别的线程打上了中断标识
		            {
		                if (Thread.currentThread().isInterrupted())  //若当前检测发现已经被打上中断标识，则结束
		                {
		                    System.out.println("线程被中断了");
		                    return ;
		                }
		                else
		                {
		                    System.out.println("线程没有被中断");  //否则就输出  "线程没有被中断"
		                    //Thread.sleep(500);  //如果这边加上这句，线程就会每隔0.5s检测是否中断，这可能导致"中断得不到及时响应"
  		                }
		            }
		        }
		    };
		    Thread t = new Thread(runnable);  
		    t.start();   //main函数起一个线程t
		    Thread.sleep(3000);   
		    t.interrupt();  //main函数休眠3s后，对t线程发出中断请求
		    System.out.println("线程中断了，程序到这里了");
		
	}

}
