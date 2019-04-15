package com.bfd.parse.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Random;

//import com.prnasia.mediawatch.app.crawldata.parser.ParserException;

/**
 * 日期处理工具类 。
 * <p>
 * 
 * Create date:3/11/2011
 * 
 * @author wenchao.fu
 * 
 */

public class MyDateUtil {
	
	/** yyyy-MM-dd */
	public static final String DATE_FORMAT = "yyyy-MM-dd";
	/** yyyy-MM-dd HH:mm:ss */
	public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
	/** yyyy-MM-dd HH:mm:ss */
	public static final String DATE_SHORT_TIME_FORMAT = "yyyy-MM-dd HH:mm";
	/** 1970-1-1 */
	public static final String DEFAULT_DATE = "1970-1-1";

	/**
	 * 获取默认格式（yyyy-MM-dd）的日期日期对象 。<br>
	 * 
	 * Create date:8/30/2012 Last modify:8/30/2012
	 * 
	 * @param strDate
	 *            目标日期字符串
	 * @return 日期对象
	 * 
	 * @author wenchao.fu
	 */
	public static Date getDate(String strDate) {
		return getDate(strDate, DATE_FORMAT);
	}
	
	/**
	 * 用指定的格式的字符串生成日期对象 。<br>
	 * Format eg. yyyy-MM-dd HH:mm:ss<br>
	 * 
	 * Create date:3/11/2011 Last modify:3/11/2011
	 * 
	 * @param strDate
	 *            被判断的字符串
	 * @param format
	 *            格式
	 * @return 日期对象
	 * 
	 * @author wenchao.fu
	 */
	public static Date getDate(String strDate, String format) {
		Date date = null;
		try {
			if (strDate!=null&&strDate.trim().length()!=0) {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				date = sdf.parse(strDate);
			}
		} catch (Exception ex) {
			date = new Date();
		}

		return date;
	}
	
	public static Date getDateDefaultNull(String strDate, String format) {
		Date date = null;
		try {
			if (strDate!=null&&strDate.trim().length()!=0) {
				SimpleDateFormat sdf = new SimpleDateFormat(format);
				date = sdf.parse(strDate);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return date;
	}

	/**
	 * 用指定的格式的字符串生成日期字符串 。<br>
	 * Format eg. yyyy-MM-dd HH:mm:ss<br>
	 * 
	 * Create date:3/23/2011 Last modify:3/231/2011
	 * 
	 * @param strDate
	 *            目标日期对象
	 * @param format
	 *            格式
	 * @return 日期字符串
	 * 
	 * @author wenchao.fu
	 */
	public static String getStr(Date date, String format) {
		if (date != null) {
			SimpleDateFormat dFormat = new SimpleDateFormat(format);
			return dFormat.format(date);

		} else {
			return "";
		}
	}
	
	/**
	 * 获取默认格式（yyyy-MM-dd）的日期字符串 。<br>
	 * 
	 * Create date:7/17/2012 Last modify:7/17/2012
	 * 
	 * @param strDate
	 *            目标日期对象
	 * @return 日期字符串
	 * 
	 * @author wenchao.fu
	 */
	public static String getStr(Date date) {
		return getStr(date, DATE_FORMAT);
	}

	/**
	 * 获取相对时间 。<br>
	 * 
	 * Create date:12/6/2011 Last modify:12/6/2011
	 * 
	 * @param date
	 *            基础日期对象
	 * @param field
	 *            单位，月、日、年等，如：Calendar.MONTH, Calendar.DATE
	 * @param amount
	 *            相差单位的个数，可为负值
	 * @return 日期对象
	 * 
	 * @author wenchao.fu
	 */
	public static Date getRelativeDate(Date date, int field, int amount) {
		if (date == null)
			return null;

		GregorianCalendar gc = new GregorianCalendar();
		gc.setTime(date);
		gc.add(field, amount);

		return gc.getTime();
	}
	
	

	public static int[] getYearMonthDay(String yyyyMMdd) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		if (yyyyMMdd == null
				|| !yyyyMMdd
						.matches("(19|20)\\d\\d(0[1-9]|1[012])(0[1-9]|[12][0-9]|3[01])")) {
			yyyyMMdd = sf.format(new Date());
		}
		int[] values = new int[3];
		values[0] = Integer.parseInt(yyyyMMdd.substring(0, 4));
		values[1] = Integer.parseInt(yyyyMMdd.substring(4, 6));
		values[2] = Integer.parseInt(yyyyMMdd.substring(6, 8));
		return values;
	}

	public static int[] getYearMonthDay() {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		String yyyyMMdd = sf.format(new Date());
		int[] values = new int[3];
		values[0] = Integer.parseInt(yyyyMMdd.substring(0, 4));
		values[1] = Integer.parseInt(yyyyMMdd.substring(4, 6));
		values[2] = Integer.parseInt(yyyyMMdd.substring(6, 8));
		return values;
	}
	public static int[] getYearMonthDay(Date date) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		String yyyyMMdd = sf.format(date);
		int[] values = new int[3];
		values[0] = Integer.parseInt(yyyyMMdd.substring(0, 4));
		values[1] = Integer.parseInt(yyyyMMdd.substring(4, 6));
		values[2] = Integer.parseInt(yyyyMMdd.substring(6, 8));
		return values;
	}

	public static int getDayInThisWeek(Date date) {
		String[] days = { "Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun" };
		SimpleDateFormat f = new SimpleDateFormat("EEE", Locale.US);
		String weekday = f.format(date);
		for (int i = 0; i < days.length; i++) {
			if (days[i].equals(weekday))
				return i;
		}
		return 3;
	}

	public static String[] getMondaySunday(String yyyyMMdd) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		Date today;
		try {
			today = sf.parse(yyyyMMdd);
		} catch (Exception e) {
			today = new Date();
		}
		int week_day = getDayInThisWeek(today);

		Date monday = new Date(today.getTime() - week_day * 3600 * 1000 * 24);
		Date sunday = new Date(today.getTime() + (7 - week_day) * 3600 * 1000
				* 24);

		String[] mon_sun = new String[2];
		mon_sun[0] = sf.format(monday);
		mon_sun[1] = sf.format(sunday);
		return mon_sun;
	}

	public static String[] getRelativeDatesInRange(Date date, int amount,
			String format) {
		int positive = 1;
		if (amount < 0) {
			positive = -1;
			amount = 0 - amount;
		}
		String[] dates = new String[amount];
		SimpleDateFormat sf = new SimpleDateFormat(format);

		long base_timestamp = date.getTime();
		for (int i = 0; i < amount; i++) {
			Date current_date = new Date(base_timestamp - 3600L * 1000 * 24 * i);
			String date_str = sf.format(current_date);
			int index = i;
			if (positive == -1) {
				index = amount - 1 - index;
			}
			dates[index] = date_str;
		}
		return dates;
	}

	public static String[] getRelativeDatesInRange(String base_date,
			int amount, String format) {
		int positive = 1;
		if (amount < 0) {
			positive = -1;
			amount = 0 - amount;
		}
		String[] dates = new String[amount];
		SimpleDateFormat sf = new SimpleDateFormat(format);
		Date date;
		try {
			date = sf.parse(base_date);
		} catch (Exception e) {
			date = new Date();
		}
		long base_timestamp = date.getTime();
		for (int i = 0; i < amount; i++) {
			Date current_date = new Date(base_timestamp + 3600L * 1000 * 24 * i
					* positive);
			String date_str = sf.format(current_date);
			int index = i;
			if (positive == -1) {
				index = amount - 1 - index;
			}
			dates[index] = date_str;
		}
		return dates;
	}

	/**
	 * Get the date of today in other month, the months can be negative
	 * 
	 * @param yyyyMMdd
	 * @param months
	 * @return
	 */
	public static int[] getTodayByMonth(int[] yyyyMMdd, int months) {
		int[] newdate = new int[3];
		int ori_year = yyyyMMdd[0];
		int ori_month = yyyyMMdd[1];
		int ori_day = yyyyMMdd[2];

		int years = months / 12;
		int related_months = months % 12;
		ori_year = ori_year + years;

		if (ori_month + related_months < 1) {
			newdate[0] = ori_year - 1;
			newdate[1] = ori_month + 12 + related_months;
		} else if (ori_month + related_months > 12) {
			newdate[0] = ori_year + 1;
			newdate[1] = ori_month - 12 + related_months;
		} else {
			newdate[0] = ori_year;
			newdate[1] = ori_month + related_months;
		}

		int day = getMaxDaysOfaMonth(newdate[0], newdate[1]);
		if (ori_day > day) {
			newdate[2] = day;
		} else {
			newdate[2] = ori_day;
		}
		return newdate;
	}

	public static String[] getTodayNextDay(String yyyyMMdd) {
		SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
		Date today;
		try {
			today = sf.parse(yyyyMMdd);
		} catch (Exception e) {
			today = new Date();
		}
		Date morrow = new Date(today.getTime() + 3600L * 1000 * 24);
		String[] today_nextday = new String[2];
		today_nextday[0] = sf.format(today);
		today_nextday[1] = sf.format(morrow);
		return today_nextday;
	}

	/**
	 * 获得某个月的天数
	 * 
	 * @param year
	 * @param month
	 * @return
	 */
	public static int getMaxDaysOfaMonth(int year, int month) {
		int days = 0;
		if (month != 2) {
			switch (month) {
			case 1:
			case 3:
			case 5:
			case 7:
			case 8:
			case 10:
			case 12:
				days = 31;
				break;
			case 4:
			case 6:
			case 9:
			case 11:
				days = 30;
			}
		} else {
			if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0)
				days = 29;
			else
				days = 28;
		}
		return days;
	}

	/**
	 * 获得系统当前年份
	 * 
	 * @return
	 */
	public static String getCurrentYear() {
		Date date = new Date();
		String currentyear = getStr(date, "yyyyMMdd").substring(0, 4);

		return currentyear;
	}

	/**
	 * 获得系统当前年份
	 * 
	 * @return
	 */
	public static String getCurrentYear(Date date) {
		String currentyear = getStr(date, "yyyyMMdd").substring(0, 4);
		return currentyear;
	}

	/**
	 * 获得系统当前月份
	 * 
	 * @return
	 */
	public static String getCurrentMonth(Date date) {
		String currentmonth = getStr(date, "yyyyMMdd").substring(4, 6);
		return currentmonth;
	}

	public static String getCurrentYear(String date) {
		String currentyear = date.substring(0, 4);

		return currentyear;
	}

	/**
	 * 获得系统当前月份
	 * 
	 * @return
	 */
	public static String getCurrentMonth() {
		Date date = new Date();
		String currentmonth = getStr(date, "yyyyMMdd").substring(4, 6);
		return currentmonth;
	}

	public static String getCurrentMonth(String date) {
		String currentmonth = date.substring(4, 6);
		return currentmonth;
	}

	/**
	 * 获得系统当前天
	 * 
	 * @return
	 */
	public static String getCurrentDay() {
		Date date = new Date();
		String currentDay = getStr(date, "yyyyMMdd").substring(6, 8);

		return currentDay;
	}

	public static String getCurrentDay(String date) {
		String currentDay = date.substring(6, 8);
		return currentDay;
	}

	public static String getQday(String day, int i) {

		day = String.valueOf(Integer.parseInt(day) + i);
		if (1 == day.length()) {
			day = "0" + day;
		}

		return day;
	}

	/**
	 * 截取日期时间
	 * 
	 * @param date
	 * @return
	 */
	public static String getQdate(String date) {
		String[] str = date.split(":");
		String year = str[0];
		String month = str[1];
		String day = str[2];
		if (1 == month.length()) {
			month = "0" + month;
		}
		if (1 == day.length()) {
			day = "0" + day;
		}
		return year + month + day;
	}

	/**
	 * 截取日期时间
	 * 
	 * @param date
	 * @return
	 */
	public static String getDeQdate(String date) {
		String[] str = date.split(" ")[0].split("-");
		String year = str[0];
		String month = str[1];
		String day = str[2];
		if (1 == month.length()) {
			month = "0" + month;
		}
		if (1 == day.length()) {
			day = "0" + day;
		}
		return year + "-" + month + "-" + day;
	}

	public static String getFormateSomeDate(String format, Date date, int day) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		cal.add(Calendar.DATE, day);
		date = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		return sdf.format(date);
	}

	public static int getIntervalDays(String begin, String end,String format) {
		
		 SimpleDateFormat sf=new SimpleDateFormat(format); 
		 long days=0;
		 try {
			Date dateBegin=sf.parse(begin);
			Date dateEnd=sf.parse(end);
			Calendar calBegin=Calendar.getInstance();  
			calBegin.setTime(dateBegin);  
			Calendar calEnd=Calendar.getInstance();  
			calEnd.setTime(dateEnd);
			
			long  seconds= calEnd.getTimeInMillis()-calBegin.getTimeInMillis();
			
			days =seconds/(24*60*60*1000);
		} catch (ParseException e) {
			e.printStackTrace();
		}  
		
		
		
		return Integer.parseInt(String.valueOf(days));
	}
	
	public static int getIntervalHours(Date dateBegin, Date dateEnd) {
		
		long hours = -1;


			Calendar calBegin=Calendar.getInstance();  
			calBegin.setTime(dateBegin);  
			Calendar calEnd=Calendar.getInstance();  
			calEnd.setTime(dateEnd);
			
			long  seconds= calEnd.getTimeInMillis()-calBegin.getTimeInMillis();
			
			hours =seconds/(60*60*1000);

		
		
		
		return Integer.parseInt(String.valueOf(hours));
	}
	
	/**
	 * 
	 * added by Martin xu,  2012/03/08
	 * 
	 * @param dateString
	 * @param oldFormat
	 * @param newFormat
	 * @return
	 */
	public static String changeFormat(String dateString, String oldFormat , String newFormat){
		SimpleDateFormat sdf = new SimpleDateFormat(oldFormat);
		try {
			Date date=sdf.parse(dateString);
			sdf.applyPattern(newFormat);
			return sdf.format(date);
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return dateString;
	}
	
	 public static  int getDayOfMonth(String dateString,String format){
		  SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		  Date date;
		 
		try {
			date = dateFormat.parse(dateString);
			Calendar now =new GregorianCalendar();		
			now.setTime(date);
			int dayOfMonth=now.get(Calendar.DAY_OF_MONTH);
			return dayOfMonth;
		} catch (ParseException e) {
			e.printStackTrace();
			return 0;
		}
				
	 }
		
	 public static Date getDateFromString(String dateString, String format){
		  if(format==null|| format.trim().equals(""))
			  format="yyyy-MM-dd";
		  SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		  Date date= new Date();
		try {
			date = dateFormat.parse(dateString);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		  
		  return date;
	 }
	 public static String getStringFromDate(Date date, String format){
		  if(format==null|| format.trim().equals(""))
			  format="yyyy-MM-dd";
		  SimpleDateFormat dateFormat = new SimpleDateFormat(format);
		  String datestring= "";
		  datestring = dateFormat.format(date);	  
		  return datestring;
	 }
	 
	 
	 /**
	  * 20120212 返回 20120112, 如果是20120330 则返回20120228 或者20120229
	  * @param dateString
	  * @param dateformat
	  * @param amount 几个月以前的
	  * @return
	  */
	 public static String getSameDayOfPastedMonth(String dateString,String dateformat, int amount){
		 if (dateformat == null)
				dateformat = "yyyy-MM-dd";
		
		SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
			try {
				Date date = dateFormat.parse(dateString);
	
				if (dateformat == null || dateformat.trim().equals(""))
					dateformat = "yyyy-MM-dd";
	
				GregorianCalendar now = new GregorianCalendar();
				now.setTime(date);
				int thisDay = now.get(GregorianCalendar.DAY_OF_MONTH);
				now.add(GregorianCalendar.MONTH, -amount);
	
				// 比如 20120330，则2月份没有30天，最后一天是29或者28
				if (now.getMaximum(GregorianCalendar.DAY_OF_MONTH) < thisDay) {
					thisDay = now.getMaximum(GregorianCalendar.DAY_OF_MONTH);
					now.set(GregorianCalendar.DAY_OF_MONTH, thisDay);
				}
	
				String lastMonthday = dateFormat.format(now.getTime());
				return lastMonthday;
			} catch (ParseException e) {
				e.printStackTrace();
			}
	
			return dateString;
	 }
	 
	 
	 
	 public static String getWeek(String dateformat){
		 
		 GregorianCalendar now = new GregorianCalendar();
		 now.setTime(new Date());
		 now.add(Calendar.DAY_OF_YEAR, -7);
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 return dateFormat.format(now.getTime());
	 }
	 
	 
	 public static String getWeek(String currentDate,String dateformat){
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 GregorianCalendar now = new GregorianCalendar();
		 try {
			now.setTime(dateFormat.parse(currentDate));
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		 now.add(Calendar.DAY_OF_YEAR, -7);
		
		 return dateFormat.format(now.getTime());
	 }
	 
	 
	 public static String getToday(String dateformat){
		 
		 GregorianCalendar now = new GregorianCalendar();
		 now.setTime(new Date());
		
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 return dateFormat.format(now.getTime());
	 }
	 
	 
	 public static String getSeason(String dateformat){
		 
		 GregorianCalendar now = new GregorianCalendar();
		 now.setTime(new Date());
		 now.add(Calendar.MONTH, -3);
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 return dateFormat.format(now.getTime());
	 }
	 
	 
	 public static String getSeason(String currentDate,String dateformat){
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 GregorianCalendar now = new GregorianCalendar();
		 try {
			now.setTime(dateFormat.parse(currentDate));
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		 now.add(Calendar.MONTH, -3);
		
		 return dateFormat.format(now.getTime());
	 }
	 
	 
	 
	 public static String getMonth(String dateformat){
		 
		 GregorianCalendar now = new GregorianCalendar();
		 now.setTime(new Date());
		 now.add(Calendar.MONTH, -1);
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 return dateFormat.format(now.getTime());
	 }
	 
	 public static int getRandom(int num){
		 Random r = new Random();
//		 r.
		 return r.nextInt(num);
	 }
	 	 
	 public static String getMonth(String currentDate,String dateformat){
		 if (dateformat == null || dateformat.trim().equals(""))
				dateformat = "yyyy-MM-dd";
		 SimpleDateFormat dateFormat = new SimpleDateFormat(dateformat);
		 GregorianCalendar now = new GregorianCalendar();
		 try {
			now.setTime(dateFormat.parse(currentDate));
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		 now.add(Calendar.MONTH, -1);
		
		 return dateFormat.format(now.getTime());
	 }
	 
	 
	 
	 
	// 测试主函数
	public static void main(String[] args) {
//		String olddate = "20110431";
//		int[] olddate_numbers = getYearMonthDay(olddate);
//		for (int i = -40; i < 40; i++) {
//			int[] newdate = getTodayByMonth(olddate_numbers, i);
//			System.out.println("" + newdate[0] + "-" + newdate[1] + "-"
//					+ newdate[2]);
//		}
//		olddate = "2011-04-28";
//		String[] dates = getRelativeDatesInRange(olddate, 80, "yyyy-MM-dd");
//		for (int i = 0; i < dates.length; i++) {
//			System.out.println(dates[i]);
//		}
//		for(int i=0;i<10;i++){
//			System.out.println(getRandom(-9));
//		}
//		String a = "abc";
//		System.out.println(a.length());
//		System.out.println(getBeginOfMonth(new Date()));
		Date date = new Date();
		Date date2 = MyDateUtil.getDate("2014-10-17 13:04:04", DATE_TIME_FORMAT);
		System.out.println(MyDateUtil.getIntervalHours(date, date2));
		
	}
	
	/**
	 * 根据Google 网页搜索给出的时间字符串获取时间 。
	 *
	 * @param dateStr 时间字符串
	 * @return 时间对象
	 * @throws ParserException 
	 */	
//	public static Date getTime(String dateStr) throws ParserException {
//		Date sysDate = new Date();
//		if (!MyStringUtil.hasValue(dateStr)) return sysDate;
//		
//		//搜狗bbs里面有1分钟内的情况
//		if(dateStr.indexOf("内")>=0){
//			dateStr = dateStr.replace("内", "前");
//		}
//		// x分钟前
//		String x = MyStringUtil.getRegexGroup(
//				"(^\\d+)\\s?分钟前$", dateStr, 1);
//		if (!x.equals("")) {
//			return MyDateUtil.getRelativeDate(sysDate, Calendar.MINUTE,
//					- Integer.parseInt(x));
//		}
//			
//		// x小时前
//		x = MyStringUtil.getRegexGroup(
//				"(^\\d+)\\s?小时前$", dateStr, 1);
//		if (!x.equals("")) {
//			return MyDateUtil.getRelativeDate(sysDate, Calendar.HOUR,
//					- Integer.parseInt(x));
//		}
//		
//		// x天前
//		x = MyStringUtil.getRegexGroup(
//				"(^\\d+)\\s?天前$", dateStr, 1);
//		if (!x.equals("")) {
//			return MyDateUtil.getRelativeDate(sysDate, Calendar.DATE,
//					- Integer.parseInt(x));
//		}
//		
//		// 确定日期，无小时，分钟数据 2011-4-20
//		return MyDateUtil.getDate(dateStr, "yyyy-MM-dd");
//	}
	
	/**
	 * 根据传入的时间，返回当月的起始时间的Date
	 * 
	 * @param Date 时间对象
	 * @return Date 时间对象
	 */
	public static Date getBeginOfMonth(Date now){
		Date result = new Date();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
		String tp = sdf2.format(now)+"-01 00:00:00";
		try {
			result = sdf.parse(tp);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		return result;
	}
	
	/**
	 * 根据传入的时间，返回当周的其实时间Date
	 * 
	 * @param now 时间对象
	 * @param mondayfirst 如果为true，则monday是第一天，如果为false，则sunday是第一天
	 * @return Date 
	 */
	public static Date getBeginOfWeek(Date now, boolean mondayfirst){
		Date result = new Date();
		
		int n = MyDateUtil.getDayInThisWeek(now);
		if(mondayfirst) {
			n = 0-n;
		}else{
			n = -1-n;
		}
		result = MyDateUtil.getRelativeDate(now, Calendar.DATE, n);
		String tempdate = MyDateUtil.getStr(result, "yyyy-MM-dd")+" 00:00:00";
		result = MyDateUtil.getDate(tempdate, "yyyy-MM-dd HH:mm:ss");
		
		return result;
	}
	
	/**
	 * 根据传入的时间，返回当月的最后时间的Date，精度为1秒
	 * 
	 * @param Date 时间对象
	 * @return Date 时间对象 
	 */
	public static Date getLastOfMonth(Date now){
		Date result = new Date();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		SimpleDateFormat sdf2 = new SimpleDateFormat("yyyy-MM");
		SimpleDateFormat sdf3 = new SimpleDateFormat("MM");
		SimpleDateFormat sdf4 = new SimpleDateFormat("yyyy");
		
		Integer month = new Integer(sdf3.format(now));
		Integer year = new Integer(sdf4.format(now));
		
		//计算最后一天
		int lastday = 28;
		boolean flag = false;
		int[] odd = {1,3,5,7,8,10,12};
		int[] even = {4,6,9,11};
		for(int i=0; i<odd.length; i++){
			if(month.equals(odd[i])){
				flag = true;
			}
		}
		if(flag){
			lastday = 31;
			flag = false;
		}
		for(int i=0; i<even.length; i++){
			if(month.equals(even[i])){
				flag = true;
			}
		}
		if(flag){
			lastday = 30;
			flag = false;
		}
		if(month.equals(2)){
			if((year%4 == 0)&&(year%100!=0)||year%400==0){
				lastday = 29;
			}
		}
		
		String tp = sdf2.format(now)+"-"+lastday+" 23:59:59";
		try {
			result = sdf.parse(tp);
		} catch (ParseException e) {
			
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static Date getLastOfWeek(Date now, boolean mondayfirst) {
		Date result = new Date();
		
		int n = MyDateUtil.getDayInThisWeek(now);
		if(mondayfirst) {
			n = 6-n;
		}else{
			n = 5-n;
		}
		result = MyDateUtil.getRelativeDate(now, Calendar.DATE, n);
		String tempdate = MyDateUtil.getStr(result, "yyyy-MM-dd")+" 23:59:59";
		result = MyDateUtil.getDate(tempdate, "yyyy-MM-dd HH:mm:ss");
		
		
		return result;
	}
	
	public static int getWeekOfYear(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		int weeks = calendar.get(Calendar.WEEK_OF_YEAR);
		return weeks;
		
	}
}
