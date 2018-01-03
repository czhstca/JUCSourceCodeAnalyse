package sourcecodeanalysis;

import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.AbstractQueuedSynchronizer.Node;

/**
 * AQS源码分析类
 * @author EX_WLJR_CHENZEHUA
 *
 */
public abstract class AbstractQueuedSynchronizerAnalyse extends AbstractOwnableSynchronizer 
														implements java.io.Serializable {

	/**
	 * AbstractQueuedSynchronizer(以下简称AQS)的主要特性:
	 * 1.AQS被设计成一种框架，用于支持内部为FIFO的队列以及通过一个原子的int变量来维护状态的一些并发类。
	 * 	  这些子类必须定义一些能够更改这个int变量的方法，并且定义当这个变量处于某个值时对应的含义是什么（获取锁、释放锁等）
	 * 
	 * 2.AQS有两种模式：独占模式和共享模式。
	 *   当处于独占模式时，当一个线程获取了同步状态后，任何其它线程都无法获取同步状态
	 *   当处于共享模式时，多个线程获取同步状态可能会成功（但不是一定成功）
	 *   AQS在使用共享模式时，当一个线程获取了同步状态后，队列中的下一个等待线程也必须确认自己是否能获取这个同步状态
	 *   处于不同模式的等待线程可以共享同一个FIFO队列
	 *   通常来讲，AQS的子类一般都只支持独占和共享其中一种模式，这样做的好处是AQS没有抽象方法（注意和普通模版模式的区别），所以子类不必强行实现不需要的方法
	 *   但是ReadWriteLock（读写锁）是一个例外，它通常用于读操作远远大于写操作的场景下，此时读操作使用的是共享锁，而写操作使用的是独占锁
	 *   
	 * 3.AQS为内部队列提供了检查、检测和监视方法，还为 condition 对象提供了类似方法
	 * 
	 * 4.为了将AQS作为基础框架，子类需要实现以下的方法,并通过 getState()、setState()、以及 compareAndSetState()方法来更改表示状态的int变量
	 * 	 tryAcquire,tryRelease,tryAcquireShared,tryReleaseShared,isHeldExclusively
	 *   这5个方法在AQS中默认是没有实现的（抛出 UnsupportedOperationException）
	 *   这些方法的实现在内部必须是线程安全的，通常应该很短并且不被阻塞
	 * 
	 * 5.独占模式的核心代码如下:
	 * Acquire:  //获取独占锁
	 *     while (!tryAcquire(arg)) {  //当未获取到同步状态时
	 *        //当前线程入队列末尾
	 *        //入队列操作可能被其他线程所打断，所以该操作需要使用CAS循环检测    
	 *     }
	 *
	 * Release:  //释放独占锁
	 *     if (tryRelease(arg))  //如果释放成功
	 *        //将队列头部的第一个线程解除block状态，使其能够获取锁
	 * 
	 * 6.自定义锁的使用方法：
	 * 	  详见 locks包下的 MyLock.java类
	 *
	 */
	
	
	
	/**
	 * AQS内部的核心数据结构：Node
	 * Node内部数据结构图见 Resources包下的locks包下的  "AQS同步队列Node数据结构图"
	 */
    static final class Node {
        /** 标识位，表示节点使用共享模式进行等待  */
        static final Node SHARED = new Node();
        /** 标识位，表示节点使用独占模式进行等待  */
        static final Node EXCLUSIVE = null;

        /** waitStatus属性值之一：指示线程已经被取消 */
        static final int CANCELLED =  1;
        /** waitStatus属性值之一：指示后继节点仍处于等待状态，当前节点的线程释放或取消同步状态时需要通知后继节点 */
        static final int SIGNAL    = -1;
        /** waitStatus属性值之一：当前线程在等待队列中，并且等待在Condition上 */
        static final int CONDITION = -2;
        /** waitStatus属性值之一：指示下一次共享式的获取同步状态将会无条件往下传递下去 */
        static final int PROPAGATE = -3;

        /**
         * 状态变量，只能取如下的值:
         *   SIGNAL:     当前节点的后继节点即将或已经被阻塞，那么当前节点的线程完成任务或因取消释放同步状态时
         *   			  需要同步唤醒它的后继节点，这样才能保证后继节点可以获取同步状态后继续执行任务.
         *   			  为了避免竞争，acquire方法必须指示需要先获取signal信号后，再尝试非阻塞获取同步状态.
         *   			  获取同步状态成功则继续执行，获取同步状态失败则进入阻塞状态.
         *   
         *   CANCELLED:  节点因为超时或中断被取消，一个节点不应该持有这种状态，取消的节点持有的线程不会被阻塞.
         *   
         *   CONDITION:  节点当前处于condition等待队列中.
         *   			  只有当节点的status转化为0之后才会被作为同步队列中的一个节点来使用.
         *   
         *   PROPAGATE:  共享式的释放同步状态动作应该逐步向后传播到同步队列中的所有节点上.
         *   			  这个状态的设置会在doReleaseShared方法中的头节点上设置来确保传播的持续.
         *   
         *   0:          正常的节点状态
         *
         * 以数字来表示这些状态是为了简化编程中的使用.
         * 非负值表示一个节点不需要被signal.所以大多数代码都不需要去检查这个值，只是用作一个标记.
         * 正常节点该属性值默认为0，condition节点该属性值默认为-2
         * 使用CAS更新该属性值
         */
        volatile int waitStatus;

        /**
         * 指向当前节点的前驱节点
         */
        volatile Node prev;

        /**
         * 指向当前节点的后驱节点
         */
        volatile Node next;

        /**
         * Node持有的线程
         * 构造方法中初始化，使用完毕后置为null
         */
        volatile Thread thread;

        /**
         * 指向下一个在Condition等待队列中等待的节点
         */
        Node nextWaiter;

        /**
         * 返回当前节点是否以共享模式等待.
         */
        final boolean isShared() {
            return nextWaiter == SHARED;
        }

        /**
         * 返回当前节点的前驱节点，如果没有前驱节点则抛出NullPointerException
         * p==null的判断是可省略的，此处用于帮助虚拟机进行判断.
         * 
         * @return the predecessor of this node
         */
        final Node predecessor() throws NullPointerException {
            Node p = prev;
            if (p == null)
                throw new NullPointerException();
            else
                return p;
        }

        Node() {    // 用于创建初始头节点或共享模式标识
        }

        Node(Thread thread, Node mode) {     // 由普通同步队列入队方法 addWaiter所使用
            this.nextWaiter = mode;
            this.thread = thread;
        }

        Node(Thread thread, int waitStatus) { // 由等待队列Condition所使用
            this.waitStatus = waitStatus;
            this.thread = thread;
        }
    }
	
	
	/**   AQS用到的核心属性       */
    
    /**
     * 同步队列的头节点，懒加载。
     * 除了初始化，该节点只能通过setHead()方法来进行修改。
     * 注意：如果头节点存在，那么头节点的waitStatus保证不为CANCELLED（1）
     */
    private transient volatile Node head;

    /**
     * 同步队列的尾节点，懒加载。
     * 该节点只能通过enq()方法在新节点入队列尾时将其设置为尾节点
     */
    private transient volatile Node tail;

    /**
     * 同步状态,acquire需要先根据这个状态值来判断是否有继续执行的资格.
     */
    private volatile int state;

    /**
     * 返回当前同步状态的值
     * 该方法和volatile读具有相同的内存语义
     * @return  当前同步状态的值
     */
    protected final int getState() {
        return state;
    }

    /**
     * 设置当前同步状态的值
     * 该方法和volatile写具有相同的内存语义
     * @param newState 要设置的同步状态值
     */
    protected final void setState(int newState) {
        state = newState;
    }

    /**
     * CAS原子更新状态值.
     * 如果当前同步状态值与期望当前状态的值相同，则将当前同步状态属性值设置为新值；否则不做任何操作.
     * 该操作和volatile读和写具有相同的内存语义
     *
     * @param expect 当前同步状态的期望值
     * @param update 要更新成指定的值
     * @return true,则说明当前状态值和期望的状态值相同，则将状态值更新为新值；
     * 		   false，则说明当前状态值和期望的状态值不同，则不做任何更新操作.
     */
    protected final boolean compareAndSetState(int expect, int update) {
        // See below for intrinsics setup to support this
        return unsafe.compareAndSwapInt(this, stateOffset, expect, update);
    }
    
    
    
	/**   AQS用到的核心方法      */
    
    
    /**   独占模式获取和释放同步状态相关方法     */
    
    /**
     * 节点以独占方式获取同步状态，忽略中断(线程不会因中断信号从同步队列中移除).
     */
    public final void acquire(int arg) {
    	//如果调用AQS子类实现的tryAcquire方法获取同步状态成功，则继续执行节点持有线程的任务
    	//否则将当前线程封装成一个Node对象，并CAS插入到队列尾部后自旋获取同步状态.
    	//如果无法获取同步状态则线程进入阻塞状态，后续只能依靠前驱节点释放同步状态的同时唤醒当前节点或当前节点持有的线程因被中断而不再自旋获取同步状态.
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();
    }
    
    
    /**
     * 将当前线程封装为Node并入队列尾部.
     *
     * @param mode 独占/共享模式
     * @return the new node  新节点
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);
        // Try the fast path of enq; backup to full enq on failure
        Node pred = tail;
        if (pred != null) {
            node.prev = pred;
            if (compareAndSetTail(pred, node)) {
                pred.next = node;
                return node;
            }
        }
        enq(node);
        return node;
    }
    
    /**
     * Inserts node into queue, initializing if necessary. See picture above.
     * @param node the node to insert
     * @return node's predecessor
     */
    private Node enq(final Node node) {
        for (;;) {
            Node t = tail;
            if (t == null) { // Must initialize
                if (compareAndSetHead(new Node()))
                    tail = head;
            } else {
                node.prev = t;
                if (compareAndSetTail(t, node)) {
                    t.next = node;
                    return t;
                }
            }
        }
    }
    
    /**
     * Acquires in exclusive uninterruptible mode for thread already in
     * queue. Used by condition wait methods as well as acquire.
     *
     * @param node the node
     * @param arg the acquire argument
     * @return {@code true} if interrupted while waiting
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;
        try {
            boolean interrupted = false;
            for (;;) {
                final Node p = node.predecessor();
                if (p == head && tryAcquire(arg)) {
                    setHead(node);
                    p.next = null; // help GC
                    failed = false;
                    return interrupted;
                }
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt())
                    interrupted = true;
            }
        } finally {
            if (failed)
                cancelAcquire(node);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
	
	
}
