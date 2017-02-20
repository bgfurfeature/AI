package com.bgfurfeature.concurrent.blockingqueue.main;

import java.util.Random;
import java.util.concurrent.BlockingQueue;

public class TestBlockingQueueConsumer implements Runnable{  

    BlockingQueue<String> queue;
    Random random = new Random();
    
    public TestBlockingQueueConsumer(BlockingQueue<String> queue){  
        this.queue = queue;  
    }        

    public void run() {  
        try {  
        	Thread.sleep(random.nextInt(10));
        	System.out.println(Thread.currentThread().getName()+ " trying...");
            // take, poll,remove
            String temp = queue.take();//如果队列为空，会阻塞当前线程

            System.out.println(Thread.currentThread().getName() + " get a job " + temp);
        } catch (InterruptedException e) {  
            e.printStackTrace();  
        }  
    }  
}
