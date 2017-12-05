package threadcommunicate;

/**
 * volatile的机制分析
 * 注意：volatile只是保证了可见性
 * 对于单个volatile变量同时也保证原子性，但对于volatile的复合操作(比如i++),volatile无法保证其原子性，必须使用锁或CAS自旋保证原子性
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class ThreadVolatile {

	public static void main(String[] args) {
		//volatile的作用:1.保证可见性  2.禁止指令重排序

		//volatile的用法：private volatile boolean flag;
		
		//volatile底层实现分析：
		//内存语义：处理器在volatile读和写操作的前后都会加入内存屏障，防止这些操作和前面以及后面的指令被重排序
		//从而保证每次读取到的volatile变量都是主内存中最新的值
		//处理器语义：这部分和synchronized的原理是一样的，都是使用Lock指令使缓存行失效并将最新数据写回主内存并禁止指令重排序
		//具体可参考ThreadSynchronized.java中的  "synchronized底层原理"(搭配resources资源中的volatile处理器实现原理图食用更佳).
		
	}

}
