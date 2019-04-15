package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:百度知道
 * @function：处理发表时间，回复内容，作者名称
 * 
 * @author bfd_02
 *
 */

public class BbaiduzhidaoPostRe implements ReProcessor {
	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BbaiduzhidaoPostRe.class);

	private static final Pattern PATTERN_NEWSTIME = Pattern.compile("newstime=(.*)");
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		
		if (resultData == null) {
			return null;
		}
		
		if(!resultData.containsKey("newstime")|| "".equals(resultData.get("newstime")) || resultData.get("newstime") == null){
			String url = unit.getUrl();
			Matcher urlM = PATTERN_NEWSTIME.matcher(url);
			if(urlM.find()){
				String newstime = urlM.group(1);
				resultData.put("newstime", newstime);
			}
			
		}
		if (!resultData.containsKey("author_name") || "".equals(resultData.get("author_name")) || resultData.get("author_name") == null) {
			resultData.put("author_name", "匿名");
		}
		if (!resultData.containsKey("contents") || "".equals(resultData.get("contents")) || resultData.get("contents") == null) {
			String contents = resultData.get(Constants.TITLE).toString();
			resultData.put("contents", split(contents));
		}
		//最佳回复
		if (resultData.containsKey("best_content")) {
			Map tmpmap = new HashMap();
			String content = (String) resultData.get("best_content");
			tmpmap.put("replydate", resultData.get("best_time"));
			tmpmap.put("replycontent", split(content));
			tmpmap.put("replyusername", resultData.get("best_author_name"));
			resultData.remove("best_time");
			resultData.remove("best_content");
			resultData.remove("best_author_name");
			if (resultData.containsKey("replys")) {
				((List) resultData.get("replys")).add(tmpmap);
			} else {
				List replys = new ArrayList();
				replys.add(tmpmap);
				resultData.put("replys", replys);
			}
		}
		// 统一于其它论坛站点，将不同回复放在一个集合内
		// 格式化最佳采纳和网友采纳时间
		if (resultData.containsKey("replys")) {
			List generalReplys = (List) resultData.get("replys");
			if (generalReplys.size() > 0) {
				for (Object object : generalReplys) {
					Map map = (Map) object;
					//replyusername
					if (map.get("replyusername") == null || "".equals(resultData.get("replyusername"))) {
						map.put("replyusername", "热心网友");
					}
					//replydate
					if (map.containsKey("replydate")) {
						String replydate = (String) map.get("replydate");
						//[\u4e00-\u9fa5]
						//发布于2018-04-03   发布于今天 09:31   发布于1 小时前  三种时间格式
						replydate = replydate.replaceAll("发布于", "").trim();
						if(!replydate.contains("-")){
							replydate = ConstantFunc.convertTime(replydate);
						}
						map.put("replydate", replydate);
					}
				}
				
			}
		}
		// 格式化newstime
		/*if (resultData.containsKey(Constants.NEWSTIME)) {
			String newstime = resultData.get(Constants.NEWSTIME).toString();
			if (newstime.contains("今天")) {
				// 分享| 今天 08:29
				newstime = newstime.replace("分享|", "").trim();
				newstime = ConstantFunc.convertTime(newstime);
				resultData.put(Constants.NEWSTIME, newstime);
			} else {
				String regex = "\\d+-\\d+-\\d+\\s*\\d+:\\d+";
				Matcher match = Pattern.compile(regex).matcher(newstime);
				if (match.find()) {
					newstime = match.group();
					resultData.put(Constants.NEWSTIME, newstime);
				}
			}
		}*/
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
	
	private String split(String str){
		String[] str1 = str.split("本回答");
		return str1[0];
		
	}

}
