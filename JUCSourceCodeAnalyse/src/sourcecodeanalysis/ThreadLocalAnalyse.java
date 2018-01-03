package sourcecodeanalysis;

import java.lang.ThreadLocal.ThreadLocalMap;
import java.lang.ThreadLocal.ThreadLocalMap.Entry;
import java.lang.ref.WeakReference;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ThreadLocal源码分析类
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadLocalAnalyse<T> {

	/**
	 * ThreadLocal的特性：
	 * 1.ThreadLocal提供了保存线程独有数据的功能.
	 * 	  相较于普通类中的变量是被多个线程所共享的，在ThreadLocal中保存的变量是当前线程独有的.
	 *   换句话说，如果对某个线程的a变量调用了ThreadLocal的set()方法，那么只有该线程自己的a变量被修改，其余线程若有同样的a变量则不会被修改.
	 * 
	 * 2.每个线程都对它自己的ThreadLocal对象持有一个弱引用(只要线程还存活并且ThreadLocal可访问)
	 *   当一个线程消亡时，它所有的ThreadLocal对象都会被标记为需要垃圾回收，除非还有别的对象在引用它们.
	 * 
	 * 3.ThreadLocal主要依靠它的内部类ThreadLocalMap来存储数据
	 *   对它来说，每一个key就是ThreadLocal对象，value就是对应的值
	 *   key和value组合成一个个Entry存放在map的table数组中
	 *   通过key的ThreadLocalHashCode来查找相应的Entry结点，从而获取value.
	 *   
	 * 4.一个ThreadLocal只能对应一种类型数据
	 *   如果需要存放多种类型的数据，则需要定义多个ThreadLocal
	 *   
	 */
	
	
	/**  ThreadLocal用到的核心属性   */
	
	/**   每一个ThreadLocal对象在ThreadLocalMap中的hashcode   */
	private final int threadLocalHashCode = nextHashCode();
	
	/**   下一个threadLocalHashCode.原子更新，从0开始       */
    private static AtomicInteger nextHashCode =
        new AtomicInteger();
	
    /**   每一次返回的threadLocalHashCode都需要加上这个值(使hash结果更加均匀散列,减少hash碰撞几率)  */
    private static final int HASH_INCREMENT = 0x61c88647;
	
    /**   返回下一个threadLocalHashCode.下一个hashCode的结果为 计算+hash增量.   */
    private static int nextHashCode() {
        return nextHashCode.getAndAdd(HASH_INCREMENT);
    }
	
	
    
	/**  ThreadLocal用到的核心方法  */
	
    /**
     * 初始化ThreadLocal有两种方式（带初始值和不带初始值）:
     * 没有初始值的初始化写法: ThreadLocal<Integer> tl = new ThreadLocal<Integer>();
     * 有初始值的写法:
     * private static ThreadLocal<Integer> tl = new ThreadLocal<Integer>() {
	         @Override 
	         protected Integer initialValue() {
	              return 1;
	         }
       };
     */
    
    
    /**
     * 返回当前线程独有的该对象副本的值
     * 如果当前线程该对象副本还没有值，则会先调用initialValue()方法设置初始值
     */
    public T get() {
        Thread t = Thread.currentThread();  //获取当前线程
        ThreadLocalMap map = getMap(t);  //获取当前线程ThreadLocalMap内部类
        if (map != null) {  //如果map不为空
            ThreadLocalMap.Entry e = map.getEntry(this);  //实际调用的是ThreadLocalMap的getEntry()方法来获取该变量当前线程副本在table中的对应结点
            if (e != null) {  //如果找到了对应结点
                @SuppressWarnings("unchecked")
                T result = (T)e.value;
                return result;  //返回值
            }
        }
        return setInitialValue();  //否则先初始化map并返回初始化的值
    }
    
    
    /**
     * 给当前线程持有的该ThreadLocal对象(key)赋值(value)
     */
    public void set(T value) {
        Thread t = Thread.currentThread();  //获取当前线程
        ThreadLocalMap map = getMap(t);  //获取当前线程ThreadLocalMap内部类
        if (map != null)  //如果map不为空
            map.set(this, value);  //实际调用的是ThreadLocalMap的set()方法来设置值
        else
            createMap(t, value);  //map为空则先初始化ThreadLocalMap
    }
    
    
    /**
     * 删除当前线程独有的该变量副本ThreadLocal对应的值
     *
     * @since 1.5
     */
     public void remove() {
         ThreadLocalMap m = getMap(Thread.currentThread()); //获取当前线程ThreadLocalMap内部类
         if (m != null)  //如果map不为空
             m.remove(this);  //实际调用的是ThreadLocalMap的remove()方法来设置值
     }
    
    
     
     /**
      * ThreadLocal的核心 ：  ThreadLocalMap
      * ThreadLocalMap是专门用于存储线程独有的副本对象的一种map。
      * 为了处理一些较大的、长生命周期的对象，key使用了WeakReferences(弱引用)
      * 但是，因为没有使用弱引用队列，所以只有当Map内部的table容量不够用时才能保证无引用的ThreadLocal对象被GC回收
      */
     static class ThreadLocalMap {

         /**
          * 这个map使用ThreadLocal对象作为key.
          * 如果调用map中Entry的get()方法返回null，说明这个key已经没有被任何对象引用，在table容量耗尽时会被GC回收
          */
         static class Entry extends WeakReference<ThreadLocal<?>> {
             /** 和ThreadLocal关联的值. */
             Object value;

             /**  map中每一个节点对象，key为ThreadLocal对象   */
             Entry(ThreadLocal<?> k, Object v) {
                 super(k);
                 value = v;
             }
         }

         /**
          * table初始容量为16，容量必须为2的次方（方便计算）
          */
         private static final int INITIAL_CAPACITY = 16;

         /**
          * map内部的数组，存放每一个结点对象
          */
         private Entry[] table;

         /**
          * table中实际结点个数
          */
         private int size = 0;

         /**
          * 阙值大小，超过该值则map需要resize.
          */
         private int threshold; // Default to 0

         /**
          * 设置阙值大小为传入的len的三分之二
          */
         private void setThreshold(int len) {
             threshold = len * 2 / 3;
         }

         /**
          * 返回下一个索引位置
          */
         private static int nextIndex(int i, int len) {
             return ((i + 1 < len) ? i + 1 : 0);
         }

         /**
          * 返回上一个索引位置
          */
         private static int prevIndex(int i, int len) {
             return ((i - 1 >= 0) ? i - 1 : len - 1);
         }

         /**
          * 初始化一个ThreadLocalMap
          * map是懒加载的，所以只有当第一个key-value对需要放入map时，才会先初始化这个map
          * 注意：ThreadLocalMap并不是用链表进行存储结点的，而是用开地址法存储结点
          */
         ThreadLocalMap(ThreadLocal<?> firstKey, Object firstValue) {
             table = new Entry[INITIAL_CAPACITY];  //使用初始容量初始化map
             int i = firstKey.threadLocalHashCode & (INITIAL_CAPACITY - 1);  //计算第一个key-value在map的table数组中存放的索引（注意同样 &(INITIAL_CAPACITY - 1),保证数组奇数位也能存放数据）
             table[i] = new Entry(firstKey, firstValue);  //在数组该位置初始化一个Entry
             size = 1;  //初始实际结点个数 =1
             setThreshold(INITIAL_CAPACITY);  //设置初始负载因子为16*2/3=10
         }

         /**
          * 通过传入的key查找map的table中对应的结点对象
          * 这个方法只适用于一次命中结点的情况，用于优化一次命中时的性能表现
          * 如果没有一次命中结点，则需要再调用 getEntryAfterMiss()方法继续查找
          *
          * @param  key threadLocal 对象
          * @return 如果一次命中key对应的结点，则返回结点对象，否则返回调用  getEntryAfterMiss()方法的结果
          */
         private Entry getEntry(ThreadLocal<?> key) {
             int i = key.threadLocalHashCode & (table.length - 1);  //计算当前的key应该在数组中存放的索引位置
             Entry e = table[i];  //获取table数组该索引处的结点对象
             if (e != null && e.get() == key)  //如果结点对象不是空且结点的key和传入的key相同
                 return e;  //直接返回结点
             else
                 return getEntryAfterMiss(key, i, e);  //没有一次命中时，则继续调用该方法
         }

         /**
          * 通过key查找结点时没有一次命中的情况下，需要调用该方法 
          *
          * @param  key threadLocal对象
          * @param  i table数组当前索引位置
          * @param  e table数组i索引处的结点对象
          * @return 如果通过key找到结点对象，则返回结点对象，否则返回null
          */
         private Entry getEntryAfterMiss(ThreadLocal<?> key, int i, Entry e) {
             Entry[] tab = table;   //获取table数组
             int len = tab.length;   //获取table数组当前最大容量

             while (e != null) {  //一次查找时有找到结点对象,继续循环判断
                 ThreadLocal<?> k = e.get();  //获取当前循环遍历到的数组索引处结点的key
                 if (k == key)  //如果此处结点的key和传入的key相同
                     return e;  //那么这个位置的结点就是要查找的结点，直接返回结点对象
                 if (k == null) //如果之前遍历时都没有找到和当前传入key相同的结点key
                     expungeStaleEntry(i);  //如果当前位置的结点key已经被GC回收则需要进一步操作，否则返回null
                 else
                     i = nextIndex(i, len);  //计算这个key对应hash下一次应该存放在数组中的索引位置
                 e = tab[i];  //e设置为当前遍历到的数组索引处的结点
             }
             return null;  //如果第一次查找时没有找到结点对象，则返回null
         }

         /**
          * 给当前线程变量副本设置对应值
          *
          * @param threadLocal对象(key) 
          * @param 需要设置的值(value)
          */
         private void set(ThreadLocal<?> key, Object value) {
        	 //一句话总结set过程:当table的位置上有数据的时候，ThreadLocal采取的办法是找最近的一个空的位置设置数据
             Entry[] tab = table;   //map内部的数组
             int len = tab.length;   //数组当前最大容量
             int i = key.threadLocalHashCode & (len-1);  //计算当前的key应该在数组中存放的索引位置

             for (Entry e = tab[i];
                  e != null;
                  e = tab[i = nextIndex(i, len)]) {  //判断数组当前索引处是否有Entry结点对象，有则取出结点对象，没有则对当前索引取模，计算下一个应该放置的索引位置
                 ThreadLocal<?> k = e.get();  //在最近的位置处有找到结点对象，则取它的key

                 if (k == key) {  //如果该位置的结点的key和当前要设置的key相同，则说明当前结点就是之前保存的结点
                     e.value = value;  //直接将结点的值进行替换并返回
                     return;
                 }

                 if (k == null) {  //如果该位置结点key为null说明该位置原先结点的key已被GC回收
                     replaceStaleEntry(key, value, i);  //把新设置的结点替换到当前位置上，返回
                     return;
                 }
             }
             //如果上面都没有return直接运行到这里，说明在table中找到了空位（即这个值是第一次进行设置）
             tab[i] = new Entry(key, value);  //将当前key和value作为新结点对象存放在数组当前索引位置处
             int sz = ++size;   //table中实际结点个数+1
             if (!cleanSomeSlots(i, sz) && sz >= threshold)  //每次set结束时都要判断table中实际结点个数是否>阙值，若是则需要resize+rehash操作
                 rehash();
         }

         /**
          * 删除当前线程对应变量副本
          */
         private void remove(ThreadLocal<?> key) {
             Entry[] tab = table;  //map内部的数组
             int len = tab.length;  //数组当前最大容量
             int i = key.threadLocalHashCode & (len-1);  //计算当前的key应该在数组中存放的索引位置
             for (Entry e = tab[i];
                  e != null;
                  e = tab[i = nextIndex(i, len)]) {  //判断数组当前遍历到索引处是否有Entry结点对象，有则取出结点对象，没有则对当前索引取模，计算下一个应该放置的索引位置
                 if (e.get() == key) {  //判断table当前索引结点key和传入的key是否相同，如果相同
                     e.clear();  //清空结点对象
                     expungeStaleEntry(i);  //在当前索引位置删除对结点的引用
                     return;
                 }
             }
         }


}
