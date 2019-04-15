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
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @sie:站长网-新闻 (Nadmin5)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class Nadmin5ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nadmin5ContentRe.class);

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @parem cate 路径
		 * @function 格式化路径字段 eg:"当前位置：A5站长网首页 > 科技 > 互联网 > 正文"
		 * 
		 */
		if (resultData.containsKey(Constants.CATE)) {
			Object oldCate = resultData.get(Constants.CATE);
			if (oldCate instanceof List) {
				ArrayList oldCateList = (ArrayList) oldCate;
				String oldCatestr = oldCateList.get(0).toString();
				ArrayList newCateList = new ArrayList<String>();
				if (!oldCatestr.equals("") && oldCatestr.contains(">")) {
					String[] newCate = oldCatestr.split(">");
					for (int i = 0; i < newCate.length; i++) {
						/**
						 * 为了去除有的模板中首页前多余的内容 eg:您所在的位置： 首页 > MWC2015 >
						 * MWC2015现场评测 > 巨屏跨界手机 华为MediaPad X2上手评测]
						 */
						if (newCate[i].contains("：")) {
							int index = newCate[i].indexOf("：");
							newCate[i] = newCate[i].substring(index + 1, newCate[i].length());
						}
						newCateList.add(newCate[i].trim());
					}
					resultData.put(Constants.CATE, newCateList);
				}
			}
		}

		/**
		 * @param source来源
		 * @param post_time发表时间
		 * @function 格式化source和post_time的值
		 *           eg:"2008-06-27 13:42 来源： 吴亚洲 我来投稿 我要评论"
		 * 
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);
			int index = source.indexOf("我来投稿");
			source = source.substring(0, index);
			int indexFront = source.indexOf("来源");
			String postTime = source.substring(0, indexFront).trim();
			source = source.substring(indexFront + 3, index).trim();
			resultData.put(Constants.POST_TIME, postTime);
			resultData.put(Constants.SOURCE, source);
		}

		/**
		 * @param commUrl评论链接
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 * 
		 */

		Map<String, Object> commentTask = new HashMap<String, Object>();
		String reg = null;
		String commUrl = null;
		String client_id = null;
		String title = null;
		String pageData = unit.getPageData();
		String currentUrl = unit.getUrl();
		String commUrlHead = "http://changyan.sohu.com/node/html?";
		if (pageData.contains("var appid =")) {
			// var appid = 'cyqRGufpG',
			reg = "var\\s*appid\\s*=\\s*\'(\\w*)\',";
			Matcher match = Pattern.compile(reg, Pattern.DOTALL).matcher(pageData);
			if (match.find()) {
				client_id = match.group(1);
			}
		}

		if (pageData.contains("</title>")) {
			// <title>没有什么不同---我在华为的日子 - A5站长网</title>
			// reg = <title>(.*)</title>
			reg = "<title>([-\\s[\u4E00-\u9FA5]\\w]+)</title>";
			Matcher match = Pattern.compile(reg, Pattern.DOTALL).matcher(pageData);
			if (match.find()) {
				title = match.group(1).trim();
			}
		}
		
		commUrl = commUrlHead + "client_id=" + client_id + "&title=" + title + "&topicurl=" + currentUrl;
		commentTask.put(Constants.LINK, commUrl);
		commentTask.put(Constants.RAWLINK, commUrl);
		commentTask.put(Constants.LINKTYPE, "newscomment");
		LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get(Constants.LINK)
				+ commentTask.get(Constants.RAWLINK) + commentTask.get(Constants.LINKTYPE));
		if (resultData != null && resultData.size() > 0) {
			resultData.put(Constants.COMMENT_URL, commUrl);
			List<Map> tasks = (List<Map>) resultData.get(Constants.TASKS);
			tasks.add(commentTask);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}