package threadcommunicate;

/**
 * Thread类的synchronize机制分析
 * synchronize既保证了原子性，也保证了可见性（注意和volatile的区别）
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadSynchronize {

	public static void main(String[] args) {
		//synchronize的用处：对一段代码加锁，保证这段代码的多个操作在多线程环境下的原子性以及执行结果对其他线程的可见性
		
		//synchronize的用法：基于对象锁的synchronize 以及 基于类锁的synchronize
		//				       上面的每种锁又分别有  锁方法 以及 锁代码块 两种书写形式
		//使用时一定要注意synchronize是对象锁还是类级锁，若是对象锁还要注意多个线程是否使用的是同一个类的实例对象作为锁（多个对象会有多个锁，此时使用synchronized是无效的）
		
		
		//基于对象锁的synchronize:  锁方法的书写形式: public synchronized void add(){...}
		//						   锁代码块的书写形式: 以当前类的实例对象作为锁的书写方式:  synchronized(this){....}
		//										  以自定义成员变量作为锁的书写方式: public String aaa = new String();
		//																   synchronized(aaa){...}
		
		//基于类锁的synchronize:   锁方法的书写形式: public synchronized static void add(){...}
		//                       锁代码块的书写形式: 只有以当前类作为锁的一种书写方式:  synchronized(ThreadSynchronize.class){....}
		
		
		//上述基于对象锁的synchronize使用方法有以下结论：
		//同一时间只能有一个线程执行类实例对象的synchronized方法/代码块
		//任何其他试图在同一时间访问该类实例对象其他的synchronized方法/代码块的线程都将被阻塞
		//任何其他试图在同一时间访问该类实例对象其他的非synchronized方法/代码块的线程可以执行
		//使用同步块的好处是 被同步块锁住的代码与该类中其他synchronized方法/代码块争抢的不是同一个锁，可以大大提高程序执行效率
		
		
		//synchronize底层实现分析:
		//synchronize底层其实依旧使用的是volatile的读写内存语义以及CAS附带的LOCK前缀信号提供的处理器级别的读写语义
		//在执行到 #monitorenter指令后会使用CAS自旋获取锁（除了偏向锁是看对象头存储的字节中是否为当前线程）
		//获取到锁后才进入被锁的方法/代码块继续执行，并且如果发现有使用到的数据所在缓存行已失效，则会从主内存中加载最新的数据再往下执行
		//当锁住的代码全部执行完毕后会执行 #monitorexit指令，在执行该指令前当前CPU线程会将最新修改过的数据回写到主内存
		//其他CPU通过总线嗅探到该数据在自己缓存中的副本与在主内存中不一致则将该数据所在的缓存行失效(以便下次读取该变量之前能够先从主内存中加载该变量最新的值)
		
		
		//JDK1.6之后引入的锁升级机制(可参考resources资源包下的各种锁的对象头存储数据实例.jpg)：
		//JDK1.6之后，synchronized会根据当前线程竞争的情况，从无竞争到竞争最激烈有以下4种状态：
		//1.无锁状态，即没有任何线程访问synchronized的代码.
		
		//2.偏向锁：当有一条线程使用到了synchronized的代码，并且很长一段时间下只有这个线程在访问synchronized的代码,此时就会将锁升级为偏向锁
		//  这种锁在对象头中存储了上次获取该锁的线程ID，下次再有线程尝试获取相同的锁时先判断该线程的ID是否和对象头中保存的线程ID一样
		//  如果是一样的，说明这次获取该锁的线程和上次的是同一个线程，则直接把锁给线程即可.
		//  这个锁在无竞争的情况下效率比重量级锁要高的多.
		
		//3.轻量级锁：当第一次出现两条线程同时访问synchronized代码块/方法，就会立即将锁升级为轻量级锁.
		//  这种锁会尝试用CAS将对象头的MarkWord替换为指向锁记录的指针,如果成功则表示获取了锁.
		//  如果获取锁失败，这种锁会不断CAS自旋继续尝试获取锁.
		
		//4.重量级锁(对象锁):当第一次出现两条以上线程同时访问synchronized代码块/方法，就会立即将锁升级为重量级锁.
		//  线程需要获取当前锁的monitor对象，才能成功获取到锁，并且其他尝试获取该锁的线程全部进入阻塞状态(同时也在CAS自选尝试获取锁)
		//  只有当前线程执行完#monitorexit指令并释放锁时，其他线程才能获取到锁从而执行synchronized修饰的代码块/方法.
		
	}

}
