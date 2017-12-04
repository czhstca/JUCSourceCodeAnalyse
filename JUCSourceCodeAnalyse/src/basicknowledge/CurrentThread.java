package basicknowledge;

/**
 * 这是Thread的静态方法currentThread的测试类
 * 注意：该方法的定义是  "对当前正在执行线程对象的引用",可以对CPU当前正在执行的线程操作
 * @author EX_WLJR_CHENZEHUA
 *
 */
public class CurrentThread {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Thread th = new Thread(new MyThread3());
		th.start();
		
		MyThread4 th2 = new MyThread4();
		th2.start();
	}

}

class MyThread3 implements Runnable{

    static
    {
        System.out.println("静态块的打印：" + 
                Thread.currentThread().getName());  //static块是由主线程来执行的，此处打印为 main  
    }
    
    public MyThread3()
    {
        System.out.println("构造方法的打印：" + 
                Thread.currentThread().getName());   //构造方法也是由主线程来执行的，此处打印为main 
    }
    
    @Override
    public void run()
    {
        System.out.println("run()方法的打印：" + 
                Thread.currentThread().getName());   //run方法是由线程本身来执行的，此处打印为 Thread-0
    }
	
}


class MyThread4 extends Thread{
	
    public MyThread4()
    {
        System.out.println("MyThread4----->Begin");
        System.out.println("Thread.currentThread().getName()----->" + 
                Thread.currentThread().getName());  //构造方法由主线程执行，所以这里的 Thread.currentThread()引用的其实是主线程main，输出main
        System.out.println("this.getName()----->" + this.getName());  //this表示的是 "当前线程自己"，和currentThread的 "对当前正在执行线程对象的引用"要注意区分,输出 Thread-1
        System.out.println("MyThread4----->end");
    }
    
    @Override
    public void run()
    {
        System.out.println("run----->Begin");
        System.out.println("Thread.currentThread().getName()----->" + 
                Thread.currentThread().getName());  //run()方法由线程自己执行，这里的 Thread.currentThread()引用的是线程自己  Thread-1,输出 Thread-1
        System.out.println("this.getName()----->" + this.getName()); //同样输出  Thread-1
        System.out.println("run----->end");
    }
	
}