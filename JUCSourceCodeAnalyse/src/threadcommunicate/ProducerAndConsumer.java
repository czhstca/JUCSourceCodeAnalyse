package threadcommunicate;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 基本的生产者-消费者模型(基于condition的await/signal)
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ProducerAndConsumer extends ReentrantLock{

	/**
	 * 生产者-消费者的模型优点主要有以下两点：
	 * 1.解耦。生产者、消费者并不直接对对方负责，而是都变成直接对缓冲区负责。
	 *   这样如果单方面发生了变化，则不会影响对方，从互相强耦合变为了同时对第三方的弱耦合
	 * 
	 * 2.通过平衡生产者和消费者的处理能力来提高整体处理数据的速度，这是生产者/消费者模型最重要的一个优点。
	 *   消费者若直接从生产者这里拿数据，如果生产者生产的速度很慢，但消费者消费的速度很快，那消费者就得占用CPU的时间片白白等在那边.
	 *   有了生产者/消费者模型，生产者和消费者就是两个独立的并发体，生产者把生产出来的数据往缓冲区一丢就好了，不必管消费者；
	 *   消费者也是，从缓冲区去拿数据就好了，也不必管生产者，缓冲区满了就不生产，缓冲区空了就不消费，使生产者/消费者的处理能力达到一个动态的平衡
	 * 
	 * 注意:小心假死!!!
	 * 如果在一个生产者对多个消费者  或者  多个生产者对多个消费者的情况下，系统有可能出现假死情况
	 * 比如有两个生产者A、B，有一个消费者C，现在A处于waiting状态，B生产完了一个产品，接下来要唤醒C对该产品消费 
	 * 但是因为notify()方法唤醒的线程具有随机性，如果此时唤醒的是另一个生产者A，那么A判断缓冲区里有产品（B刚生产完的），A就会继续Waiting
	 * 此时因为A、B、C都在waiting,且没有能唤醒它们的线程存在，整个系统就处于假死状态了。
	 * 
	 * 那么如何避免发生假死状态呢?有以下两个方法:
	 * 1.使用notifyAll()代替notify(),这样能够唤醒所有在等待的线程(包括生产者和消费者)，消费者迟早是能够得到锁去消费的，所以不会出现假死状态;
	 * 2.ReetrantLock创建两个condition,一个是消费者condition,一个是生产者condition,唤醒时调用相对应的condition进行signal()就可以了
	 * 
	 */
	
	
	private Condition condition = newCondition();   //消费者、生产者使用同一个锁，则它们不能并行运行
	
	//生产者生产的方法
	public void produce(){
		try {
			lock();
			while(!"".equals(ValueObject.value)){  //不满足生产者的条件(缓冲区有东西)
				condition.await();
			}
			//满足条件,则继续生产
			ValueObject.value= String.valueOf((int)(Math.random()*1000));
			System.out.println(Thread.currentThread().getName() + "生产了value,value当前值为 " + ValueObject.value);
			condition.signal();  //如果此时有线程在等待，则唤醒
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			unlock();  //注意必须要unlock
		}
	}
	
	
	//消费者消费的方法
	public void consume(){
		try {
			lock();
			while("".equals(ValueObject.value)){  //不满足消费者的条件(缓冲区为空)
				condition.await();
			}
			//满足条件，则继续消费
			ValueObject.value = "";
			System.out.println(Thread.currentThread().getName() + "消费了value,value当前值为 " + ValueObject.value);
			condition.signal();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			unlock();
		}
		
	}
	
	public static void main(String[] args) {
		// 测试生产者-消费者模型
		ProducerAndConsumer pac = new ProducerAndConsumer();
		Runnable producer = new Runnable() {
			
			@Override
			public void run() {
				for(int i=0; i<5; i++){
					pac.produce();
				}
				
			}
		};
		
		Runnable consumer = new Runnable() {
			
			@Override
			public void run() {
				for(int i=0; i<5; i++){
					pac.consume();
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
