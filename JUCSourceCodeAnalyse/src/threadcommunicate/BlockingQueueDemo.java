package threadcommunicate;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 使用阻塞队列实现生产者-消费者模型
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class BlockingQueueDemo {

	/**
	 * BlockingQueue有很多个子类，比较常用的有:
	 * ArrayBlockingQueue:基于数组的阻塞队列，只有一个ReentrantLock锁，所以生产者/消费者无法并行运行
	 * LinkedBlockingQueue:基于链表的阻塞队列，生产者、消费者有各自的ReentrantLock锁,所以生产者/消费者能够并行运行
	 * SynchornousQueue:无缓冲区设计，所以一个插入操作必须等待一个删除操作完成后才能执行
	 * 
	 * 注意锁分为公平锁与非公平锁:
	 * 公平锁,使用一个先进先出的队列来管理多余的生产者消费者
	 * 非公平锁，使用一个后进先出的栈来管理多余的生产者消费者
	 */
	
	public static void main(String[] args) {
		
		//此处使用ArrayBlockingQueue作为子类来实现缓冲区的设计，默认设定大小为10
		//并且此例中设置了生产者的生产速度大于消费者的消费速度，所以在缓冲区满后必须消费完一个才能继续生产一个资源
		
		BlockingQueue<String> bq = new ArrayBlockingQueue<String>(10);  //默认为非公平锁
		
		Runnable producer = new Runnable() {
			int i = 0;
			
			@Override
			public void run() {
				// TODO Auto-generated method stub
				while(!Thread.interrupted()){
					try {
						System.out.println(Thread.currentThread().getName() +" 生产了一个 " + i++);
						bq.put(i + "");
						Thread.sleep(1000);  //这里比消费者睡眠时间短，则生产者线程能够获得更多的执行机会
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} 
				}
			}
		};
		
		Runnable consumer = new Runnable() {
			
			@Override
			public void run() {
				while(!Thread.interrupted()){
					try {
						System.out.println(Thread.currentThread().getName() +" 消费了一个 " + bq.take());
						Thread.sleep(3000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
			}
		};
		
		Thread producerThread = new Thread(producer);
		producerThread.setName("producer-1");
		Thread consumerThread = new Thread(consumer);
		consumerThread.setName("consumer-1");
		producerThread.start();
		consumerThread.start();
	}

}
