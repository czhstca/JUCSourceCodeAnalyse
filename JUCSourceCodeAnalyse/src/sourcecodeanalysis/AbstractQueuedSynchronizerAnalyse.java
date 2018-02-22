package sourcecodeanalysis;

import java.util.concurrent.locks.AbstractOwnableSynchronizer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.LockSupport;
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
	 * 7.关于AQS独占模式获取同步状态的两个小问题：
	 * 	（1）为什么对应的Node构建完毕入队列尾部时不直接调用LockSupport的park方法对Node持有的线程进行阻塞？
	 *  （2）为什么不直接把head的waitStatus设置为Signal而要从0设置为Signal（即为什么节点默认的waitStatus=0）？
	 *   
	 *   答案：其实这两个问题的答案是建立在同一个需求上的，即  宁愿让节点多tryAcquire一次，也要避免因某些操作耗时非常短但无脑加锁而导致线程频繁上下文切换所带来的性能消耗!
	 *   问题一：如果在Node构建完毕入队列尾部时就直接调用LockSupport的park方法对Node持有的线程进行阻塞，那么节点没有经过tryAcquire就直接被阻塞（无论其操作耗时是否很短）,性能很差;
	 *   问题二：如果直接把head的waitStatus设置为Signal，那么在节点tryAcquire失败一次后会调用 shouldParkAfterFailedAcquire方法，若节点默认设置waitStatus为SIGNAL
	 *   则直接会跳出该方法并接着调用 parkAndCheckInterrupt方法去阻塞线程，这样虽然性能上比第一种方法好一些（至少tryAcquire了一次），但是实际情况下同时会有许多线程入队列
	 *   可能还是会出现许多线程同时被阻塞的情况;
	 *   所以AQS的设计是 至少让节点 tryAcquire两次，只有两次都失败时才阻塞节点的线程，以尽量减少因不需要的加锁而导致的性能消耗。
	 *   从AQS的源码中可以看出，当一个节点tryAcquire失败时，调用shouldParkAfterFailedAcquire方法，此时因为AQS设计节点默认的waitStatus=0,所以并不会直接返回true
	 *   而是先把前驱节点的waitStatus设置为SIGNAL(-1)并返回false，然后因为Acquire是自旋操作（死循环），所以此时又会进行一次tryAcquire操作，如果此时再次失败，
	 *   那么又会调用shouldParkAfterFailedAcquire方法,而此时前驱节点的waitStatus已经被设置成了SIGNAL(-1),所以方法返回true并执行调用 parkAndCheckInterrupt方法去阻塞线程
	 *   这样虽然还是无法完全避免因无故加锁导致的性能消耗，但至少tryAcquire了两次，可以尽量降低一些性能的消耗。
	 *   
	 * 8.  
	 *   
	 *   
	 *   
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
         * 等待状态变量，只能取如下的值:
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
     * CAS原子更新同步状态值.
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
    
    
    /**   独占模式获取和释放同步状态相关方法 (可参考resources包下的locks包中的 "AQS独占模式获取同步状态流程图")   */
    
    /**
     * 节点以独占方式获取同步状态，忽略中断(线程不会因中断信号从同步队列中移除).
     */
    public final void acquire(int arg) {
    	//如果调用AQS子类实现的tryAcquire方法获取同步状态成功，则继续执行节点持有线程的任务
    	//否则将当前线程封装成一个Node对象，并CAS插入到队列尾部后自旋获取同步状态.
    	//如果无法获取同步状态则线程进入阻塞状态，后续只能依靠前驱节点释放同步状态的同时唤醒当前节点或当前节点持有的线程因被中断而不再自旋获取同步状态.
        if (!tryAcquire(arg) &&
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg))
            selfInterrupt();  //线程进入阻塞状态
    }
    
    
    /**
     * 将当前线程封装为Node并入队列尾部.
     *
     * @param mode 独占/共享模式
     * @return the new node  新节点
     */
    private Node addWaiter(Node mode) {
        Node node = new Node(Thread.currentThread(), mode);  //将当前线程封装为新的NODE对象
        //先尝试以最短路径O(1)的效果完成快速入队列,减少开销(有可能当前线程一次就能够入队列)
        Node pred = tail;  //获取当前队列中的尾节点
        if (pred != null) {  //如果当前队列中有尾节点，才尝试快速入队列
            node.prev = pred;   //将当前传入新节点的前驱节点指向原先队列中的尾节点
            if (compareAndSetTail(pred, node)) {  //如果使用CAS将当前节点更新为队列尾节点成功(并发环境下同一时间只有一条线程能够执行CAS操作成功)
                pred.next = node;  //将原先队列中尾节点的后驱指向当前传入新节点，完成一次入队列操作
                return node;  //返回该新构建的节点
            }
        }
        enq(node);   //如果一次入队列尾部操作失败，则调用enq()方法循环不断尝试入队列操作
        return node;  //返回该新构建的节点
    }
    
    /**
     * 将一个新的节点入队列尾部，如有必要会先初始化
     * 
     * @param node 要入队列的新节点
     * @return 原先队列中的尾节点
     */
    private Node enq(final Node node) {
        for (;;) {  //这里用了死循环，表示节点会不断尝试入队列的操作直至成功为止(并发环境下可能有许多线程同时因tryAcquire失败需要入队列)
            Node t = tail;  //先获取当前队列的尾节点
            if (t == null) { // 如果尾节点为空，必须先初始化队列
                if (compareAndSetHead(new Node()))  //使用CAS设置一个新的头结点
                    tail = head;  //初始化队列时，尾节点和头节点为同一个节点
            } else {   //如果尾节点存在，则需要将当前节点更新为新的尾节点
                node.prev = t; //将当前传入新节点的前驱节点指向原先队列中的尾节点
                if (compareAndSetTail(t, node)) { //如果使用CAS将当前节点更新为队列尾节点成功
                    t.next = node; //将原先队列中尾节点的后驱指向当前传入新节点，完成一次入队列操作
                    return t;  //返回原先队列中的尾节点,结束循环
                }
            }
        }
    }
    
    /**
     * 已经在队列中的节点调用该方法尝试获取同步状态
     *
     * @param node 队列中的节点
     * @param arg the acquire参数
     * @return 如果在等待过程中被打断，则返回true
     */
    final boolean acquireQueued(final Node node, int arg) {
        boolean failed = true;  //acquire失败标记
        try {
            boolean interrupted = false;  //线程被打断标记
            for (;;) {  //死循环，只有当前节点为头节点的第一个后驱节点才可能Acquire成功，否则不断尝试acquire
                final Node p = node.predecessor();  //获取当前节点的前驱节点
                if (p == head && tryAcquire(arg)) {  //如果当前节点的前驱节点为头节点并且tryAcquire成功
                    setHead(node);  //将当前节点设置为头节点,节点中持有的线程和前驱节点都置空
                    p.next = null; //原先头节点置空，帮助GC回收
                    failed = false;  //acquire失败标记改为false
                    return interrupted;  //跳出循环，返回线程被打断标记
                }
                //当前节点的前驱节点不是头节点或者tryAcquire失败
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt()) //如果当前线程需要被阻塞并且被打上中断标识
                    interrupted = true;  //将线程被打断标记更新为true
            }
        } finally {
            if (failed)  //acquire失败
                cancelAcquire(node);  //调用取消Acquire方法,保证异常节点不会阻塞后续节点acquire
        }
    }
    
    
    /**
     * 检查并更新acquire失败节点的前驱节点的等待状态变量
     * 变量需要满足  pred == node.prev.
     *
     * @param pred 当前acquire失败节点的前驱节点
     * @param node 当前acquire失败节点
     * @return 如果线程被阻塞，返回true
     */
    private static boolean shouldParkAfterFailedAcquire(Node pred, Node node) {
        int ws = pred.waitStatus;  //获取当前节点的前驱节点的waitStatus属性值
        if (ws == Node.SIGNAL)  //如果waitStatus值为SIGNAL
            /*
             * 前驱节点已经设置了释放同步状态时同步唤醒后继节点的状态，所以当前节点中的线程可以安全阻塞，返回true
             */
            return true;
        if (ws > 0) {   //如果waitStatus值为CANCELLED
            /*
             * CANCELLED的节点是无效的需要作废，当前节点不断往前寻找直到找到一个状态不是CANCELLED的节点并与其连接
             */
            do {
                node.prev = pred = pred.prev;
            } while (pred.waitStatus > 0);
            pred.next = node;
        } else {  //如果waitStatus为0或者PROPAGATE
            /*
             * 使用CAS设置前驱节点的waitStatus值为SIGNAL,以便之后能唤醒当前节点
             */
            compareAndSetWaitStatus(pred, ws, Node.SIGNAL);
        }
        return false;
    }
    
    
    /**
     * 该方法用来阻塞节点中的线程并检查节点持有的线程是否被中断
     *
     * @return 如果线程被中断，返回true
     */
    private final boolean parkAndCheckInterrupt() {
        LockSupport.park(this);  //阻塞节点中的线程
        return Thread.interrupted();  //返回节点中线程是否被中断
    }
    
    
    
    /**
	 * 节点以独占方式释放同步状态.
	 */
    public final boolean release(int arg) {
        if (tryRelease(arg)) {   //如果AQS子类实现的tryRelease方法执行成功
            Node h = head;  //获取此时队列中的头节点
            if (h != null && h.waitStatus != 0)  //如果头节点存在并且waitStatus属性值不为0
                unparkSuccessor(h);  //唤醒后继节点
            return true;   //返回true
        }
        return false;   //如果调用tryRelease方法失败，则直接返回false
    }
    
    
    /**
     * 唤醒当前节点的后继节点.
     *
     * @param node 当前节点（头节点）
     */
    private void unparkSuccessor(Node node) {

        int ws = node.waitStatus;  //获取头节点的waitStatus状态值
        if (ws < 0)   //如果状态值 < 0
            compareAndSetWaitStatus(node, ws, 0);  //使用CAS更新头节点的waitStatus值为0

        Node s = node.next;  //获取头节点的后继节点
        if (s == null || s.waitStatus > 0) {  //如果后继节点为null或后继节点的waitStatus状态值为CANCELLED
            s = null;
            for (Node t = tail; t != null && t != node; t = t.prev)  //从队列尾部开始向前循环查找
                if (t.waitStatus <= 0)  //查找到第一个状态值不为CANCELLED的节点，则记录下来作为头节点的后继节点
                    s = t;
        }
        if (s != null)  //如果头节点的后继节点不为null
            LockSupport.unpark(s.thread);  //唤醒头节点的后继节点所持有的线程,继续执行任务
    }
    
    
    
    /**   共享模式获取和释放同步状态相关方法     */
    
    
    /**
     * 节点以共享模式获取同步状态，忽略中断响应
     */
    public final void acquireShared(int arg) {
    	//注意此处和独占模式获取同步状态的区别，独占模式返回的是Boolean变量，而共享模式返回的是int变量
        if (tryAcquireShared(arg) < 0)  //如果调用子类实现的共享获取同步状态方法成功，则方法结束；如果获取同步状态失败，则继续调用doAcquireShared(arg)方法获取同步状态直至成功
            doAcquireShared(arg);
    }
    
    
    /**
     * 共享模式获取同步状态(无法响应中断，无超时自动取消等待机制)
     * @param arg acquire参数(可以理解为同步资源的数量)
     */
    private void doAcquireShared(int arg) {
        final Node node = addWaiter(Node.SHARED); //获取以共享模式入队列尾部的节点
        boolean failed = true;  //tryAcquire失败标记
        try {
            boolean interrupted = false;  //节点持有的线程是否被中断的标记
            for (;;) {  //死循环，不断尝试以共享模式tryAcquire同步资源
                final Node p = node.predecessor();  //获取当前入队列尾部的前一个节点
                if (p == head) {  //如果当前入队列尾部的前一个节点为头节点
                    int r = tryAcquireShared(arg);  //以共享模式tryAcquire同步资源,注意与独占模式相比获取成功条件的不同之处
                    if (r >= 0) {  //如果该方法返回值>=0表示以共享模式获取同步资源成功
                        setHeadAndPropagate(node, r);  //共享模式tryAcquire独有方法，将当前节点设置为头节点并尝试将共享状态往后传播
                        p.next = null; //原先头节点置空，帮助GC回收
                        if (interrupted)  //如果此时线程中断标识为true
                            selfInterrupt();  //中断当前节点所持有的线程
                        failed = false; //acquire失败标记改为false
                        return;
                    }
                }
                //以共享模式获取同步资源失败,与独占模式操作相同
                if (shouldParkAfterFailedAcquire(p, node) &&
                    parkAndCheckInterrupt()) //如果当前线程需要被阻塞并且被打上中断标识
                    interrupted = true;   //线程中断标记置为true
            }
        } finally {
            if (failed)  //共享模式acquire失败
                cancelAcquire(node); //调用取消Acquire方法,保证异常节点不会阻塞后续节点acquire
        }
    }
    
    
    /**
     * 设置当前节点为队列新的头节点，如果后继节点是以共享模式等待的，就将通过doReleaseShared方法尝试继续往后唤醒节点，实现了共享状态的向后传播
     *
     * @param node 新入队列尾部的节点（此时应该是头节点的下一个节点）
     * @param propagate tryAcquireShared方法的返回值
     */
    private void setHeadAndPropagate(Node node, int propagate) {
        Node h = head; // 记录原来队列的头节点
        setHead(node);  //将当前队列头节点设置为原头节点的后一个节点

        if (propagate > 0 || h == null || h.waitStatus < 0 ||
            (h = head) == null || h.waitStatus < 0) {  //如果原先头节点或当前节点为null或状态CANCELLED
            Node s = node.next;  //获取当前节点的下一个节点
            if (s == null || s.isShared())  //如果该节点为null或者是以共享模式等待的节点
                doReleaseShared();  //则调用释放节点同步状态的方法，以唤醒下一个节点，使其可能tryAcquireShared成功
        }
    }
    
    
    /**
	 * 节点以共享方式释放同步状态.
	 */
    public final boolean releaseShared(int arg) {
        if (tryReleaseShared(arg)) {  //如果调用子类实现的tryReleaseShared()成功
            doReleaseShared();   //调用doReleaseShared()方法以共享模式释放同步状态
            return true;
        }
        return false;
    }
    
    
    /**
     * 共享模式的释放同步状态方法，唤醒后继节点并保证唤醒的动作向后传播
     * 注：相对的如果是独占模式释放同步状态，仅仅唤醒头节点的后继节点（如果该节点需要被唤醒的话）
     */
    private void doReleaseShared() {
        
        for (;;) {   //死循环，不断尝试释放动作
            Node h = head;  //获取当前队列的头节点
            if (h != null && h != tail) {
                int ws = h.waitStatus;
                if (ws == Node.SIGNAL) {
                    if (!compareAndSetWaitStatus(h, Node.SIGNAL, 0))
                        continue;            // loop to recheck cases
                    unparkSuccessor(h);
                }
                else if (ws == 0 &&
                         !compareAndSetWaitStatus(h, 0, Node.PROPAGATE))
                    continue;                // loop on failed CAS
            }
            if (h == head)                   // loop if head changed
                break;
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
	
	
}
