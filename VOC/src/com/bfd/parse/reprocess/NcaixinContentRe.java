package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 
 * @author 08
 *
 */
public class NcaixinContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
//		http://china.caixin.com/2017-04-07/101075491.html
		String url = unit.getUrl();
		if (resultData != null) {
			//post_time时间   2017年04月07日 17:36 来源于 财新网 可以听文章啦！
//			来源于 《财新周刊》 2016年第23期 出版日期 2016年06月13日
			if(resultData.containsKey(Constants.POST_TIME) && resultData.get(Constants.POST_TIME) != ""){
				String t_post_time = resultData.get(Constants.POST_TIME).toString();
				String reg1 = "(\\d{4}年\\d{2}月\\d{2}日 \\S+:\\d{2})";
				String reg2 = "(\\d{4}年\\d{2}月\\d{2}日)";
				String post_time = this.getCresult(t_post_time, reg1, reg2);
				SimpleDateFormat df1 = new SimpleDateFormat("yyyy年MM月dd日 hh:mm");
				SimpleDateFormat df3 = new SimpleDateFormat("yyyy年MM月dd日");
				SimpleDateFormat df2 = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
				try {
					if (post_time.matches(reg1)) {
						Date d = df1.parse(post_time);
						post_time = df2.format(d);
					}
					if (post_time.matches(reg2)) {
						Date d = df3.parse(post_time);
						post_time = df2.format(d);
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
				resultData.put(Constants.POST_TIME, post_time.trim());
			}
			//拼接评论链接 http://c2.caixin.com/comment-api-caixin/comment/treelist.do?appid=100&topic_id=101076184&req_type=99&page=1&size=20
			String topic_id = this.getCresult(url, "(\\d+).html", null);
			String sCommUrl = "http://c2.caixin.com/comment-api-caixin/comment/treelist.do?appid=100&topic_id=" + topic_id + "&req_type=99&page=1&size=15";
			Map commentTask = new HashMap();
			commentTask.put(Constants.LINK, sCommUrl);
			commentTask.put(Constants.RAWLINK, sCommUrl);
			commentTask.put(Constants.LINKTYPE, "newscomment");
			if (resultData != null && !resultData.isEmpty()) {
				resultData.put(Constants.COMMENT_URL, sCommUrl);
				List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);	
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	/**
	 * 正则匹配字符串
	 * @param str
	 * @param pattern
	 * @return
	 */
	private String getCresult(String str,String reg1,String reg2){
		Pattern pattern = Pattern.compile(reg1);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}else{
			pattern = Pattern.compile(reg2);
			mch = pattern.matcher(str);
			if(mch.find()){
				return mch.group(1);
			}
		}
		return str;
	}

}
