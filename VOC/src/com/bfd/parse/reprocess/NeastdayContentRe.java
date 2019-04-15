package com.bfd.parse.reprocess;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
/**
 * @site：东方网
 * @function：处理作者，发表时间，来源等
 * @author bfd_04
 *
 */
public class NeastdayContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NeastdayContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 取到解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = result.getSpiderdata().get("location").toString();
		if(resultData != null && !resultData.isEmpty()) {
			//collection.eastday.com
			if(url.contains("shzw.eastday.com") || url.contains("finance.eastday.com")) {
				parser01(resultData);
			}
			else if(url.contains("mini.eastday.com")) {
				parser02(resultData);
			}
			else if(url.contains("sh.eastday.com")) {
				parser03(resultData);
			}
			else if(url.contains("news.eastday.com")) {
				parser04(resultData);
			} 
			else if(url.contains("law.eastday.com")) {
				parser01(resultData);
				//针对这个站点作一个判断 20160720
			}else if(url.contains("collection.eastday.com")){
				parser_collection(resultData);
			}
			
			// 处理评论链接
//			String data = unit.getPageData();
//			Map<String, Object> commentTask = new HashMap<String, Object>();
//			String urlHead = "http://changyan.sohu.com/api/3/topic/liteload?callback=jQuery&client_id=";
//			String clientid = appid(data);
//			String title = null;
//			try {
//				if (resultData.containsKey(Constants.TITLE)) {
//					title = resultData.get(Constants.TITLE).toString();
//				}
//				title = java.net.URLEncoder.encode(title,"utf-8");
//			} catch (UnsupportedEncodingException e) {
//				LOG.info(e);
//			}
//			String comUrl = urlHead + clientid + "&topic_url=" + url + "&topic_title=" + title
//					+ "&page_size=30&topic_source_id=" + findIid(url);
//			commentTask.put("link", comUrl);
//			commentTask.put("rawlink", comUrl);
//			commentTask.put("linktype", "newscomment");
//			LOG.info("url:" + url + "taskdata is " + commentTask.get("link") + commentTask.get("rawlink")
//					+ commentTask.get("linktype"));
//			if (!resultData.isEmpty()) {
//				resultData.put("comment_url", comUrl);
//				@SuppressWarnings("unchecked")
//				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
//				tasks.add(commentTask);
//			}
			// 后处理插件加上iid
			ParseUtils.getIid(unit, result);
		}
		return new ReProcessResult(processcode, processdata);
	}
	  //url with "collection.eastday.com" 为了不影响其他站点，新建一个站点 20160720
		public void parser_collection(Map<String,Object> resultData) {
			if(resultData.containsKey(Constants.AUTHOR)) {
				String author = resultData.get(Constants.AUTHOR).toString();
				if(author.contains("作者")) {
					String[] tempStrs = author.split("作者");
					if(tempStrs.length >1) {
						author = tempStrs[1].split(" ")[0].replace(":", "").trim();
						resultData.put(Constants.AUTHOR, author);
					}
				} else {
					resultData.put(Constants.AUTHOR, "");
				}
			}
			if(resultData.containsKey(Constants.CATE)) {
				List cate = (List)resultData.get(Constants.CATE);
				String[] temp = null;
				if (cate.get(0).toString().contains(">>")) {
					temp = cate.get(0).toString().replace("当前位置 |", "").trim().split(">>");
				} else {
					temp = cate.get(0).toString().replace("当前位置 |", "").trim().split(">");
				}
				cate = Arrays.asList(temp);
				resultData.put(Constants.CATE, cate);
			}
			if(resultData.containsKey(Constants.SOURCE)) {
				String source = resultData.get(Constants.SOURCE).toString();
				if(source.contains("来源")) {
					String[] temp = source.split("来源:");
					if(temp.length > 1) {
						source = temp[1].split(" ")[0];
						resultData.put(Constants.SOURCE, source);
					}
				} else {
					resultData.put(Constants.SOURCE, "");
				}
			}
			if (resultData.containsKey(Constants.POST_TIME)) {
				String postTime = resultData.get(Constants.POST_TIME).toString();
				Pattern pattern = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2}( \\d{1,2}:\\d{1,2}:\\d{1,2})?)");
				Matcher match = pattern.matcher(postTime);
				if(match.find()) {
					postTime = match.group(1);
				} else {
					postTime = postTime.split("来源")[0].trim();
				}
				resultData.put(Constants.POST_TIME, formatDate(postTime));
			}
		}	
	//url with "shzw.eastday.com"
	public void parser01(Map<String,Object> resultData) {
		if(resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if(author.contains("作者")) {
				String[] tempStrs = author.split("作者");
				if(tempStrs.length >1) {
					author = tempStrs[1].split(" ")[0].replace(":", "").trim();
					resultData.put(Constants.AUTHOR, author);
				}
			} else {
				resultData.put(Constants.AUTHOR, "");
			}
		}
		if(resultData.containsKey(Constants.CATE)) {
			List cate = (List)resultData.get(Constants.CATE);
			String[] temp = null;
			if (cate.get(0).toString().contains(">>")) {
				temp = cate.get(0).toString().replace("当前位置 |", "").trim().split(">>");
			} else {
				temp = cate.get(0).toString().replace("当前位置 |", "").trim().split(">");
			}
			cate = Arrays.asList(temp);
			resultData.put(Constants.CATE, cate);
		}
		if(resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			if(source.contains("来源")) {
				String[] temp = source.split("来源");
				if(temp.length > 1) {
					source = temp[1].split(" ")[0];
					resultData.put(Constants.SOURCE, source);
				}
			} else {
				resultData.put(Constants.SOURCE, "");
			}
		}
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Pattern pattern = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2}( \\d{1,2}:\\d{1,2}:\\d{1,2})?)");
			Matcher match = pattern.matcher(postTime);
			if(match.find()) {
				postTime = match.group(1);
			} else {
				postTime = postTime.split("来源")[0].trim();
			}
			resultData.put(Constants.POST_TIME, postTime);
		}
	}
	//url with "mini.eastday.com"
	public void parser02(Map<String,Object> resultData) {
		if(resultData.containsKey(Constants.CATE)) {
			List cate = (List)resultData.get(Constants.CATE);
			String[] temp = null;
			if(cate.get(0).toString().contains(">")) {
				temp = cate.get(0).toString().split(">");
				cate = Arrays.asList(temp);
			}
			resultData.put(Constants.CATE, cate);
		}
		if(resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			String[] temp = source.split(" ");
			if(temp.length > 2) {
				source = temp[2];
			} else {
				source = "";
			}
			resultData.put(Constants.SOURCE, source);
		}
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			Pattern pattern = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{2} \\d{2}:\\d{2})");
			Matcher match = pattern.matcher(postTime);
			if(match.find()) {
				postTime = match.group(1);
			} else {
				postTime = "";
			}
			resultData.put(Constants.POST_TIME, postTime);
		}
	}
	//url with "sh.eastday.com"
	public void parser03(Map<String,Object> resultData) {
		if(resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if(author.contains("作者")) {
				String[] tempStrs = author.split("作者");
				if(tempStrs.length >1) {
					author = tempStrs[1].split(" ")[0].replace(":", "").trim();
					resultData.put(Constants.AUTHOR, author);
				}
			} else {
				resultData.put(Constants.AUTHOR, "");
			}
		}
		if(resultData.containsKey(Constants.CATE)) {
			List cate = (List)resultData.get(Constants.CATE);
			String[] temp = cate.get(0).toString().split(">>");
			cate = Arrays.asList(temp);
			resultData.put(Constants.CATE, cate);
		}
	}
	//url with "news.eastday.com"
	public void parser04(Map<String,Object> resultData) {
		if(resultData.containsKey(Constants.AUTHOR)) {
			String author = resultData.get(Constants.AUTHOR).toString();
			if(author.contains("作者")) {
				String[] tempStrs = author.split("作者");
				if(tempStrs.length >1) {
					author = tempStrs[1].split(" ")[0].replace(":", "").trim();
					resultData.put(Constants.AUTHOR, author);
				}
			} else {
				resultData.put(Constants.AUTHOR, "");
			}
		}
		if(resultData.containsKey(Constants.CATE)) {
			List cate = (List)resultData.get(Constants.CATE);
			String[] temp = cate.get(0).toString().split(">>");
			cate = Arrays.asList(temp);
			resultData.put(Constants.CATE, cate);
			LOG.debug("cate: " + cate);
		}
		//"source": "来源:新华网 作者:任沁沁 顾瑞珍 选稿:魏政"
		if(resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			if (source.contains("来源:")) {
				String[] tempArr = source.split("来源:");
				if (tempArr.length > 1) {
					source = tempArr[1].split(" ")[0];
				}
			}
			resultData.put(Constants.SOURCE, source);
		}
	}
//	//url with "law.eastday.com"
//		public void parser05(Map<String,Object> resultData) {
//			
//			if(resultData.containsKey(Constants.CATE)) {
//				List cate = (List)resultData.get(Constants.CATE);
//				String[] temp = cate.get(0).toString().replace("当前位置 |", "").trim().split(">>");
//				cate = Arrays.asList(temp);
//				resultData.put(Constants.CATE, cate);
//			}
//			if(resultData.containsKey(Constants.SOURCE)) {
//				String source = resultData.get(Constants.SOURCE).toString();
//				if(source.contains("来源")) {
//					String[] temp = source.split("来源");
//					if(temp.length > 1) {
//						source = temp[1].split(" ")[0];
//						resultData.put(Constants.SOURCE, source);
//					}
//				} else {
//					resultData.put(Constants.SOURCE, "");
//				}
//			}
//			if (resultData.containsKey(Constants.POST_TIME)) {
//				String postTime = resultData.get(Constants.POST_TIME).toString();
//				Pattern pattern = Pattern.compile("(\\d{4}-\\d{1,2}-\\d{1,2}( \\d{1,2}:\\d{1,2}:\\d{1,2})?)");
//				Matcher match = pattern.matcher(postTime);
//				if(match.find()) {
//					postTime = match.group(1);
//				} else {
//					postTime = "";
//				}
//				resultData.put(Constants.POST_TIME, postTime);
//			}
//		}
	
	private String appid(String data) {
		Pattern iidPatter = Pattern.compile("appid: '(\\w+)'");
		Matcher match = iidPatter.matcher(data);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	
	private String findIid(String url) {
		Pattern iidPatter = Pattern.compile("(\\d+).html");
		Matcher match = iidPatter.matcher(url);
		while (match.find()) {
			return match.group(1);
		}
		return null;
	}
	//格式化日期操作 20160720
	private String formatDate(String date) {
		if (date.contains("年") && date.contains("月") && date.contains("日")) {
			return date.replace("年", "-").replace("月", "-").replace("日", "");
		}
		return date;
	}
}
