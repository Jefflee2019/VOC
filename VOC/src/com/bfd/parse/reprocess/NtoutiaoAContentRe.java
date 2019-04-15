package com.bfd.parse.reprocess;

import java.util.ArrayList;
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
public class NtoutiaoAContentRe implements ReProcessor{

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Map<String, Object>> _tasks = new ArrayList<Map<String, Object>>();
		resultData.put("tasks", _tasks);
		String getPageData = unit.getPageData();
		String url = unit.getUrl();
		String tempurl = "http://www.toutiao.com/api/comment/list/?group_id=t_group_id&item_id=t_item_id&offset=0&count=10";
		if (getPageData.contains("articleInfo")) {
			//从页面源码获取数据
			String content = this.getCresult(getPageData, "content: '(.*)'");
			/**
			 * content源码标签处理
			 */
			StringBuffer buffer = new StringBuffer();
			String reg1 = "&lt;p&gt;.*?&lt;/p&gt;";
			Pattern pattern1 = Pattern.compile(reg1);
			Matcher mch1 = pattern1.matcher(content);
			while(mch1.find()){
				buffer.append(mch1.group());
			}
			content = buffer.toString();
			
			String reg2 = "&lt;style&gt;.*?&lt;/style&gt;";
			Pattern pattern2 = Pattern.compile(reg2);
			Matcher mch2 = pattern2.matcher(content);
			while(mch2.find()){
//				System.err.println(mch2.group());
				content = content.replaceAll("&lt;style&gt;.*?&lt;/style&gt;", "").trim();
			}
			
			String reg = "&lt;.*?gt;";
			Pattern pattern = Pattern.compile(reg);
			Matcher mch = pattern.matcher(content);
			while(mch.find()){
				System.err.println(mch.group());
				content = content.replaceAll("&lt;.*?gt;", "").trim();
			}
			
			String title = this.getCresult(getPageData, "title: '(.*)'");
			String source = this.getCresult(getPageData, "source: '(.*)'");
			String time = this.getCresult(getPageData, "time: '(.*)'");
			resultData.put(Constants.POST_TIME, time);
			resultData.put(Constants.SOURCE, source);
			resultData.put(Constants.AUTHOR, source);
			resultData.put(Constants.TITLE, title);
			resultData.put(Constants.CONTENT, content);
			//拼接评论页链接
			String group_id = this.getCresult(getPageData, "groupId: '(\\d+)");
			String item_id = this.getCresult(getPageData, "itemId: '(\\d+)");
			String commenturl = tempurl.replaceAll("t_group_id", group_id).replaceAll("t_item_id", item_id);
			List tasks = (List) resultData.get("tasks");
			Map<String, Object> task = new HashMap<String, Object>();
			task.put("link", commenturl);
			task.put("rawlink", commenturl);
			task.put("linktype", "newscomment");
			resultData.put("comment_url", commenturl);
			tasks.add(task);
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
	private String getCresult(String str,String reg){
		Pattern pattern = Pattern.compile(reg);
		Matcher mch = pattern.matcher(str);
		if(mch.find()){
			return mch.group(1);
		}
		return str;
	}

}
