package com.bfd.parse.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 执行时间执行任务/延迟执行任务
 * @author Administrator
 *
 */

public class TimeTaskTest {  
    Timer timer;  
   
    /**
     * 指定时间执行定时任务
     * @param time
     */
    /*public TimeTaskTest(){  
        Date time = getTime();  
        System.out.println("指定时间time=" + time);  
        timer = new Timer();  
        timer.schedule(new TimeTaskTest02(), time);  
    }*/  
      
    /**
     * 延迟执行
     * @param time
     */
    public TimeTaskTest(int time){  
        timer = new Timer();  
        timer.schedule(new TimeTaskTest02(), time*1000);  
    }  
    
    public Date getTime(){  
        Calendar calendar = Calendar.getInstance();  
        calendar.set(Calendar.HOUR_OF_DAY, 10);  
        calendar.set(Calendar.MINUTE, 38);  
        calendar.set(Calendar.SECOND, 00);  
        Date time = calendar.getTime();  
          
        return time;  
    }  
      
    public static void main(String[] args) {  
//        new TimeTaskTest();  
        new TimeTaskTest(3);  
    }  
}  

  
 class TimeTaskTest02 extends TimerTask{  
  
    @Override  
    public void run() {  
        System.out.println("指定时间执行线程任务...");  
        System.out.println("执行完成");
    }  
}  

