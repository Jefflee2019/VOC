package com.bfd.parse.reprocess;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
public class TiebalistReprocess implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(TiebalistReprocess.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (resultData != null && resultData.size() > 0) {
					//处理帖子列表页时间
					List<Map<String, Object>> items = (List<Map<String, Object>>)resultData.get("items");
					for(Map<String, Object> author : items ){
						String posttime = (String) author.get("posttime");
						if(posttime!=null){
							String last_reply_time = getTime(posttime.trim());
//							System.out.println(last_reply_time);
//							author.put("last_reply_time",  last_reply_time);
							author.put("posttime",  last_reply_time);
						}
					}
			}
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new ReProcessResult(processcode, processdata);
		
	}
	
		/**
		 * 时间统一格式
		 * @param date
		 * @return
		 */
		public static String getTime(String date){
			String result = null;
			if(date.contains(":") && date.length() < 6){ //判断是12:39这样的时间
				 Calendar now =Calendar.getInstance();  
				 now.setTime(new Date());  
				 SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
				 result = df.format(now.getTime())+" "+date;
			}
			if(date.contains("-") && date.length() <= 5){ //判断是7-17这样的时间10-11
				 Calendar now =Calendar.getInstance();  
				 now.setTime(new Date());  
				 SimpleDateFormat df = new SimpleDateFormat("yyyy");
				 if(Integer.valueOf(date.substring(0, date.indexOf("-")))<10){
					 result = df.format(now.getTime())+"-0"+date;
					 if(Integer.valueOf(date.substring(date.indexOf("-")+1, date.length()))<10){
						 result = result.substring(0,result.lastIndexOf("-")+1)+"0"+date.substring(date.lastIndexOf("-")+1, date.length());
					 }
				 }else{
					 result = df.format(now.getTime())+"-"+date;
					 if(Integer.valueOf(date.substring(date.indexOf("-")+1, date.length()))<10){
						 result = result.substring(0,result.lastIndexOf("-")+1)+"0"+date.substring(date.lastIndexOf("-")+1, date.length());
					 }
				 }
			}
			return result;
			
		}
		 /** 
		   * 得到几天前的时间 
		   * @param d 
		   * @param day 
		   * @return 
		   */  
		  public static String getDateBefore(Date d,int day){  
		   Calendar now =Calendar.getInstance();  
		   now.setTime(d);  
		   now.set(Calendar.DATE,now.get(Calendar.DATE)-day);
		   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		   //System.out.println(df.format(now.getTime()));
		   return df.format(now.getTime());  
		  } 
		  
		  /** 
		   * 得到几小时前的时间 
		   * @param d 
		   * @param hour 
		   * @return 
		   */  
		  public static String getHourBefore(Date d,int hour ){  
		   Calendar now =Calendar.getInstance();  
		   now.setTime(d);  
		   now.set(Calendar.HOUR_OF_DAY,now.get(Calendar.HOUR_OF_DAY)-hour);
		   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		   //System.out.println(df.format(now.getTime()));
		   return df.format(now.getTime());  
		  }
		  
		  /** 
		   * 得到几分钟前的时间 
		   * @param d 
		   * @param minute 
		   * @return 
		   */  
		  public static String getMinuteBefore(Date d,int minute ){  
		   Calendar now =Calendar.getInstance();  
		   now.setTime(d);  
		   now.set(Calendar.MINUTE,now.get(Calendar.MINUTE)-minute);
		   SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		   //System.out.println(df.format(now.getTime()));
		   return df.format(now.getTime());  
		  } 
	
}
