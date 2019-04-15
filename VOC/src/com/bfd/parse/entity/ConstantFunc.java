package com.bfd.parse.entity;

import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * @function:此类里都是些公共方法，一些常见的处理
 * @author BFD_499
 *
 */
public class ConstantFunc {
	private static final long DAY = 86400000;
	private static final long HOUR = 3600000;
	private static final long MIN = 60000;
	
	/**
	 * @function:将时间戳转换为标准时间格式，单位是秒
	 * @param time
	 * @return
	 */
	public static String normalTime(String time)
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  
		if(time.length()==13) {
			 return sdf.format(new Date(Long.valueOf(time)));
		}else {
        return sdf.format(new Date(Long.valueOf(time+"000")));
		}
	}

	/**
	 * 
	 * @param 将几天前、几小时前、几分钟前格式化成标准时间处理格式
	 * @return
	 */
	public static String convertTime(String time)
	{
		long nowTimeL = new Date().getTime();
		Pattern iPattern = Pattern.compile("(\\d+)\\s*(年|个月|天|小时|分钟|秒)前");
		Matcher iM = iPattern.matcher(time.trim());
		//不带年份的时间正则
		Pattern p = Pattern.compile("(?:(\\d{2,4}).)?(\\d+).(\\d+).\\s*(\\d+):(\\d+)(?:\\:(\\d+))?");
		Matcher mch = p.matcher(time);
		long day = 86400000;
		long hour = 3600000;
		long min = 60000;
		long second = 1000;
	    if(time.contains("前天")) {
			nowTimeL = (nowTimeL - 2*day)/1000;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	        String tempDate = sdf.format(new Date(Long.valueOf(nowTimeL + "000")));
	        time = time.replace("前天", "").trim();
	        String finalDate = tempDate + " " + time;
	        return finalDate;
		}
	    else if(time.contains("昨天")) {
			nowTimeL = (nowTimeL - day)/1000;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	        String tempDate = sdf.format(new Date(Long.valueOf(nowTimeL + "000")));
	        time = time.replace("昨天","").trim();
	        String finalDate = tempDate + " " + time;
	        return finalDate;
		}
	    else if(time.contains("今天")) {
			nowTimeL = nowTimeL/1000;
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");  
	        String tempDate = sdf.format(new Date(Long.valueOf(nowTimeL + "000")));
	        time = time.replace("今天","").trim();
	        String finalDate = tempDate + " " + time;
	        return finalDate;
		}
		else if(time.contains("半天前"))
		{
			nowTimeL = (nowTimeL - 43200000)/1000;
		}
		else if(time.contains("半小时前"))
		{
			nowTimeL = (nowTimeL - 1800000)/1000;
		}
		else if(iM.find())
		{
			int i = Integer.parseInt(iM.group(1));
			if(time.contains("年前")){
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.YEAR,-i);
				SimpleDateFormat matter = new SimpleDateFormat("yyyy-MM-dd"); 
				return matter.format(calendar.getTime());
			} 
			else if(time.contains("个月前")){
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.MONTH,-i);
				SimpleDateFormat matter = new SimpleDateFormat("yyyy-MM-dd"); 
				return matter.format(calendar.getTime());
			}
			else if(time.contains("天前"))
			{
				Calendar calendar = Calendar.getInstance();
				calendar.add(Calendar.DATE,-i);
				SimpleDateFormat matter = new SimpleDateFormat("yyyy-MM-dd"); 
				return matter.format(calendar.getTime());
			}
			else if(time.contains("小时前"))
			{
				nowTimeL = (nowTimeL - i*hour)/1000;
			}
		    else if(time.contains("分钟前"))
			{
				nowTimeL = (nowTimeL - i*min)/1000;
			}
		    else if(time.contains("秒前"))
			{
				nowTimeL = (nowTimeL - i*second)/1000;
			}
		}else if(mch.find()){
			nowTimeL = convertMonthTime(mch);
		}
		else {
			nowTimeL = nowTimeL/1000;
		}
		return normalTime(Long.toString(nowTimeL));
		
	}
	
	
	/**
	 * 对于可能没有年份的时间字符串标准化为标准时间
	 * @param time
	 * @return
	 * 
	 * 2013年11月01号  11:11
	 */
	private static long convertMonthTime(Matcher mch){
		Calendar c = Calendar.getInstance();
		//c.setTimeInMillis(nowTimeL);
		if(mch.group(1) != null){
			String year = mch.group(1);
			if(year.length() == 2){
				year = "20" + year;
			}
			c.set(Calendar.YEAR, Integer.valueOf(year));
		}
		c.set(Calendar.MONTH, Integer.valueOf(mch.group(2)) - 1);
		c.set(Calendar.DAY_OF_MONTH, Integer.valueOf(mch.group(3)));
		c.set(Calendar.HOUR_OF_DAY, Integer.valueOf(mch.group(4)));
		c.set(Calendar.MINUTE, Integer.valueOf(mch.group(5)));
		if(mch.group(6) != null){				
			c.set(Calendar.SECOND, Integer.valueOf(mch.group(6)));
		}
		return c.getTimeInMillis()/1000;
	}
	/**
	 * 更广泛的处理天前（昨天、前天）、小时前、分钟前、秒前
	 * @num 
	 * @t d、h、m、s分别代表天、小时、分、秒
	 */
	public static String convertTime(int num, String t)
	{
		long nowTimeL = new Date().getTime();
		if(t.equals("d"))
		{
			nowTimeL = (nowTimeL - num*DAY)/1000;
		}
		else if(t.equals("h"))
		{
			nowTimeL = (nowTimeL - num*HOUR)/1000;
		}
		else if(t.equals("m"))
		{
			nowTimeL = (nowTimeL - num*MIN)/1000;
		}
		else if(t.equals("s"))
		{
			nowTimeL = (nowTimeL - num)/1000;
		}
		return normalTime(Long.toString(nowTimeL));
	}
	
    /** 
     * 方法名称:transMapToString 
     * 传入参数:map 
     * 返回值:String 形如 username'chenziwen^password'1234 
    */  
    @SuppressWarnings("rawtypes")
	public static String transMapToString(Map map){  
      java.util.Map.Entry entry;  
      StringBuffer sb = new StringBuffer();  
      int count = 0;
      for(Iterator iterator = map.entrySet().iterator(); iterator.hasNext();)  {  
    	  count ++;
    	  entry = (java.util.Map.Entry)iterator.next();
    	  if(null!=entry.getValue()){
    		  sb.append(entry.getValue().toString());
    	  }
          //sb.append(null==entry.getValue()?"":entry.getValue().toString());  
          if(count%2!=0) {
        	  sb.append(":");
          }
      }  
      return sb.toString();  
    }  
    
    /**
     * 处理替换\t\r\n等符号
     * 
     * @param str
     * @return
     */
    public static String replaceBlank(String str) {
    	/*\n 回车(\u000a)
    	  \t 水平制表符(\u0009)
    	  \s 空格(\u0008)
    	  \r 换行(\u000d)*/
        String dest = "";
        if (str!=null) {
            Pattern p = Pattern.compile("\t|\r|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }
    /**

     * 把毫秒转化成日期

     * @param dateFormat(日期格式，例如：MM/ dd/yyyy HH:mm:ss)

     * @param millSec(毫秒数)

     * @return

     */

    public static String transferLongToDate(String dateFormat,Long millSec) {
	     SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
	     Date date= new Date(millSec);
            return sdf.format(date);
    }
    
 public static String getDate(String date) {
    	
    	if(null!=date){
    		
		date = date.replace("年", "-").replace("月", "-").replace("日", " ")
				.replace(".", "-").replace("/", "-").trim();
		// 日期格式1：2015-11-05 17:49:15
		Matcher match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+:\\d+")
				.matcher(date);
		if (match.find()) {
			return match.group(0);
		}
		// 日期格式2：2015-11-05 17:49
		match = Pattern.compile("\\d+-\\d+-\\d+\\s*\\d+:\\d+").matcher(date);
		if (match.find()) {
			return match.group(0);
		}
		// 日期格式3：2015-11-05
		match = Pattern.compile("\\d+-\\d+-\\d+").matcher(date);
		if (match.find()) {
			return match.group(0);
		}
    	}
		return date;
	}

	public static String getSource(String source) {

		if(null!=source){
		// 以下用区分是不是来源开头,还是来源位于字符串中间
		// 来源： 中国经济网—《经济日报》 注意来源字段后面有空格
		Matcher match = Pattern.compile("^来源").matcher(source);
		if (match.find()) {
			source = source.replace(" ", "");
		}

		int flag = 0;// 标记位，用来作判断
		if (source.contains("来源")) {
			flag = 1;
			// 发布时间： 2016-3-09 来源：中国财经网 作者：周小宁
			// 2016年11月24日 来源：天极网 编辑：豆豆 【我要评论】
			match = Pattern.compile("来源.*\\s+").matcher(source);
			if (match.find()) {
				source = match.group(0).replace("来源：", "").trim();
				flag = 0;
				if (source.contains(" ")) {
					String s[] = source.split(" ");
					if (s.length > 1) {
						source = s[0];
					}
				}
			}
			if (flag == 1) {
				// 来源：中国经济网—《经济日报》
				match = Pattern.compile("来源.*").matcher(source);
				if (match.find()) {
					source = match.group(0).replace("来源：", "").trim();
				}
			}
		}
		}
		return source;
	}

	public static String getEditor(String editor) {
		
		if(null!=editor) {
		Matcher match = Pattern.compile("^编辑").matcher(editor);
		if (match.find()) {
			editor = editor.replace(" ", "");
		}

		int flag = 0;
		if (editor.contains("编辑")) {
			flag = 1;
			match = Pattern.compile("编辑.*\\s+").matcher(editor);
			if (match.find()) {
				editor = match.group(0).replace("编辑：", "").trim();
				flag = 0;
				if (editor.contains(" ")) {
					String s[] = editor.split(" ");
					if (s.length > 1) {
						editor = s[0];
					}
				}
			}
			if (flag == 1) {
				match = Pattern.compile("编辑.*").matcher(editor);
				if (match.find()) {
					editor = match.group(0).replace("编辑：", "").trim();
				}
			}
		}
		}
		return editor;
	}

	public static String getAuthor(String author) {

		if(null!=author) {
			
		Matcher match = Pattern.compile("^作者").matcher(author);
		if (match.find()) {
			author = author.replace(" ", "");
		}

		int flag = 0;
		if (author.contains("作者")) {
			flag = 1;
			match = Pattern.compile("作者.*\\s+").matcher(author);
			if (match.find()) {
				author = match.group(0).replace("作者：", "").trim();
				flag = 0;
				if (author.contains(" ")) {
					String s[] = author.split(" ");
					if (s.length > 1) {
						author = s[0];
					}
				}
			}
			if (flag == 1) {
				match = Pattern.compile("作者.*").matcher(author);
				if (match.find()) {
					author = match.group(0).replace("作者：", "").trim();
				}
			}
		}
		}
		return author;
	}
	
	   /**
     * 生成32位md5值
     * @param plainText
     * @return
     */
	public static String getMD5(String plainText) {
		try {
			// 生成实现指定摘要算法的 MessageDigest 对象。
			MessageDigest md = MessageDigest.getInstance("MD5");
			// 使用指定的字节数组更新摘要。
			md.update(plainText.getBytes());
			// 通过执行诸如填充之类的最终操作完成哈希计算。
			byte b[] = md.digest();
			// 生成具体的md5密码到buf数组
			int i;
			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
//			System.out.println("32位: " + buf.toString());// 32位的加密
			// System.out.println("16位: " + buf.toString().substring(8, 24));//
			// 16位的加密，其实就是32位加密后的截取
			return buf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	public static void getNextpage(Map<String, Object> resultData, String url, List<Map<String, Object>> tasks) {
		String nextpage = null;
		Map<String, Object> nextpageTask = new HashMap<String, Object>();
		String oldPageNum = getPage(url);
		if (oldPageNum.equals("0")) {
			nextpage = url + "&pn=2";
		} else {
			int pageNum = Integer.valueOf(oldPageNum) + 1;
			nextpage = url.replace("&pn=" + oldPageNum, "&pn=" + pageNum);
		}

		nextpageTask.put("link", nextpage);
		nextpageTask.put("rawlink", nextpage);
		nextpageTask.put("linktype", "newslist");
		resultData.put("nextpage", nextpage);
		tasks.add(nextpageTask);
	}

	/**
	 * 处理生成新闻内容页的链接,针对使用360搜索的列表页
	 * 
	 * @param tasks
	 * @param pagedata
	 */
	public static void decodeLink(List<Map<String, Object>> tasks, String pagedata) {
		for (int i = 0; i < tasks.size(); i++) {
			if (tasks.get(i).containsKey(Constants.LINK)
					&& "newscontent".equals(tasks.get(i).get("linktype").toString())) {
				tasks.remove(i);
				i--;
			}
		}

		List<String> dataurl = new ArrayList<String>();
		try {
			Pattern p = Pattern.compile("data-url=\"(\\S+)\"");
			Matcher m = p.matcher(pagedata);
			while (m.find()) {
				if (dataurl.contains(m.group(1))) {
					continue;
				} else {
					dataurl.add(m.group(1));
				}
			}
			for (int i = 0; i < dataurl.size(); i++) {
				if (dataurl.get(i).toString().contains("360") || dataurl.get(i).toString().contains("so.com")) {
					dataurl.remove(i);
					i--;
				}
			}

			for (int i = 0; i < dataurl.size(); i++) {
				Map<String, Object> content = new HashMap<String, Object>();
				content.put(Constants.LINK, dataurl.get(i));
				content.put(Constants.RAWLINK, dataurl.get(i));
				content.put(Constants.LINKTYPE, "newscontent");
				tasks.add(content);
			}

		} catch (Exception e) {
		}
	}

	// 获取页数
	public static String getPage(String url) {
		Pattern iidPatter = Pattern.compile("&pn=(\\d+)");
		Matcher match = iidPatter.matcher(url);
		if (match.find()) {
			return match.group(1);
		} else {
			return "0";
		}
	}

	/**
	 * 将11 January 2018格式的时间转换为2000年01月01日格式
	 * @param commentTime
	 * @return
	 */
	public static String convertUKTime(String commentTime) {
		// TODO Auto-generated method stub
		String[] strArray = commentTime.split(" ");
		String month = strArray[1];
		switch (month) {
		case "January"   : strArray[1] = "1月"; break;
		case "February"  : strArray[1] = "2月"; break;
		case "March"     : strArray[1] = "3月"; break;
		case "April"     : strArray[1] = "4月"; break;
		case "May"       : strArray[1] = "5月"; break;
		case "June"      : strArray[1] = "6月"; break;
		case "July"      : strArray[1] = "7月"; break;
		case "Aguest"    : strArray[1] = "8月"; break;
		case "September" : strArray[1] = "9月"; break;
		case "October"   : strArray[1] = "10月"; break;
		case "November"  : strArray[1] = "11月"; break;
		case "December"  : strArray[1] = "12月"; break;
		}
		strArray[0] = strArray[0] + "日";
		strArray[2] = strArray[2] + "年";
		commentTime = strArray[2] + strArray[1] + strArray[0]; 
		return commentTime;
	}
}