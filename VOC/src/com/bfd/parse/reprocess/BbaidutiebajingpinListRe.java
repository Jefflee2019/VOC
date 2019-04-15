package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
public class BbaidutiebajingpinListRe implements ReProcessor {
	
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
							author.put("posttime",  last_reply_time);
						}
					}
					//处理下一页  控制翻页
					if (resultData.containsKey("nextpage")) {
						int limitNo = 1500; //限制翻页1500
						String link = (String) resultData.get("nextpage");
						String reg = "pn=(\\d+)";
						Pattern pattern = Pattern.compile(reg);
						Matcher matcher = pattern.matcher(link);
						if (matcher.find()) {
							int pageNo = Integer.parseInt(matcher.group(1));
							if (pageNo >= limitNo * 50) {
								List<Map<String, Object>> tasks = (List<Map<String, Object>>)resultData.get("tasks");
								for (Map<String, Object> map2 : tasks) {
									if (map2.containsValue(link)) {
										tasks.remove(map2);
										break;
									}
								}
								resultData.remove("nextpage");
							}
						}
						
					}
			}
						
		} catch (Exception e) {
			e.printStackTrace();
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(processcode, processdata);
		
	}
	
	
	public static void main(String[] args) {
		String time = "2014-03";
		BbaidutiebajingpinListRe re = new BbaidutiebajingpinListRe();
		try {
			System.err.println(re.getTime(time));;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
		/**
		 * 时间统一格式
		 * @param date
		 * @return
		 * @throws ParseException 
		 */
		public String getTime(String date) throws ParseException{
//			10-30  5-3
//			10:20
//			2016-07
			// 正则表达式规则
		    String regEx1 = "\\d{1,2}-\\d{1,2}";
		    String regEx2 = "\\d{1,2}:\\d{1,2}";
		    String regEx3 = "\\d{4}-\\d{1,2}";
		    Date nowTime = new Date();
		    String result = null;
		    SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		    SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd");
		    SimpleDateFormat df3 = new SimpleDateFormat("yyyy");
		    SimpleDateFormat df4 = new SimpleDateFormat("yyyy-MM");
		    SimpleDateFormat df5 = new SimpleDateFormat("yyyy-M-d");
			if (date.matches(regEx1)) {
				String time = df3.format(nowTime) + "-" + date;
				result = df1.format(df5.parse(time));
			} else if (date.matches(regEx2)) {
				result = df2.format(nowTime) + " " + date;
			} else if (date.matches(regEx3)){
				result = df1.format(df4.parse(date));
			} else {
				result = df1.format(nowTime);
			}
			
			return result;
			
		}
		
}
