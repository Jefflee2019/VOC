package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.entity.Constants;

/**
 * 站点：天涯博文
 * 
 * 功能：标准化内容页 给出评论页链接
 * 
 * @author bfd_06
 */

public class NtianyaContentReForPE implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();
		/* 为空则返回 */
		if(url==null){
			return new ReProcessResult(SUCCESS, processdata);
		}
//		String subUrl = urlFilter(url); // 去掉 http https 后的链接
		int tmpl_id = (int)resultData.get("tmpl_id");
//		subUrl = subUrl.replace("blog.tianya.cn/", "");
		if (tmpl_id==267||tmpl_id==596) {
			// ARTICLE_RANKING 文章排名
			if (resultData.containsKey(Constants.ARTICLE_RANKING)) {
				formatStr(resultData, unit,
						resultData.get(Constants.ARTICLE_RANKING).toString(),
						Constants.ARTICLE_RANKING);
			}
			// REG_TIME 注册时间
			if (resultData.containsKey(Constants.REG_TIME)) {
				formatStr(resultData, unit, resultData.get(Constants.REG_TIME)
						.toString(), Constants.REG_TIME);
			}
			// POST_TIME 发表时间
			if (resultData.containsKey(Constants.POST_TIME)) {
				formatStr(resultData, unit, resultData.get(Constants.POST_TIME)
						.toString(), Constants.POST_TIME);
			}
			// REPLY_CNT 评论数
			if (resultData.containsKey(Constants.REPLY_CNT)) {
				String replyCnt = resultData.get(Constants.REPLY_CNT)
						.toString();
				replyCnt = replyCnt.substring(replyCnt.indexOf(":") + 1);
				resultData.put(Constants.REPLY_CNT, replyCnt);
			}
			/**
			 * 添加下一页
			 */
			if (resultData.containsKey(Constants.REPLY_CNT)) {
				String replyCnt = resultData.get(Constants.REPLY_CNT)
						.toString();
				/**
				 * 如果有评论则给出评论页链接
				 */
				if (Integer.parseInt(replyCnt) != 0) {
					addCommentUrl(unit, resultData);
					ParseUtils.getIid(unit, result);
				}
			}
		} else {
			/**
			 * 第一种模板类型
			 */
			if (resultData.containsKey(Constants.ARTICLE_CNT)) {
				// MSG_CNT
				formatAttr(resultData,
						(String) resultData.get(Constants.MSG_CNT),
						Constants.MSG_CNT);
				// VISIT_CNT
				formatAttr(resultData,
						(String) resultData.get(Constants.VISIT_CNT),
						Constants.VISIT_CNT);
				// REPLY_CNT
				formatAttr(resultData,
						(String) resultData.get(Constants.REPLY_CNT),
						Constants.REPLY_CNT);
				// ARTICLE_CNT
				formatAttr(resultData,
						(String) resultData.get(Constants.ARTICLE_CNT),
						Constants.ARTICLE_CNT);
				// REG_TIME
				formatAttr(resultData,
						(String) resultData.get(Constants.REG_TIME),
						Constants.REG_TIME);
				// POST_TIME
				formatAttr(resultData,
						(String) resultData.get(Constants.POST_TIME),
						Constants.POST_TIME);
			}
			String author = (String) resultData.get(Constants.AUTHOR);
			/**
			 * 第二种模板类型
			 */
			if (author.contains("发表")) {
				/* AUTHOR */
				formatAttr746(resultData,
						(String) resultData.get(Constants.AUTHOR),
						Constants.AUTHOR);
				/* POST_TIME */
				formatAttr746(resultData,
						(String) resultData.get(Constants.POST_TIME),

						Constants.POST_TIME);
				/**
				 * 第三种模板类型
				 */
			} else {
				// POST_TIME
				formatAttr(resultData,
						(String) resultData.get(Constants.POST_TIME),
						Constants.POST_TIME);
			}
		}

		// CONTENT 去掉内容前后的空格
		if (resultData.containsKey(Constants.CONTENT)) {
			formatAttr(resultData, (String) resultData.get(Constants.CONTENT),
					Constants.CONTENT);
		}

		return new ReProcessResult(SUCCESS, processdata);
	}

	private void formatAttr746(Map<String, Object> resultData, String value,
			String keyName) {
		if (keyName.equals(Constants.AUTHOR)) {
			resultData
					.put(keyName,
							value.substring(value.indexOf("：") + 1,
									value.indexOf("|")));
		} else if (keyName.equals(Constants.POST_TIME)) {
//			value = value.substring(value.indexOf("|") + 2);
//			resultData.put(keyName, value.substring(value.indexOf("：") + 1,
//					value.indexOf("|") - 1));
			value = value.split("星期")[0].trim();
			resultData.put(keyName, value);
		}
	}

	private void formatAttr(Map<String, Object> resultData, String value,
			String keyName) {
		switch (keyName) {
		case Constants.MSG_CNT:
			String strM = value.substring(value.indexOf("留言") + 2);
			resultData.put(keyName, strM.substring(0, strM.indexOf("个")));
			break;
		case Constants.VISIT_CNT:
			value = value.substring(value.indexOf("：") + 1,
					value.indexOf("次") - 1);
			resultData.put(keyName, value);
			break;
		case Constants.REPLY_CNT:
			String strR = value.substring(value.indexOf("评论") + 3);
			resultData.put(keyName, strR.substring(0, strR.indexOf("个") - 1));
			break;
		case Constants.ARTICLE_CNT:
			String strA = value.substring(value.indexOf("日志") + 2);
			resultData.put(keyName, strA.substring(0, strA.indexOf("篇")));
			break;
		case Constants.REG_TIME:
			value = value.substring(value.lastIndexOf("：") + 1);
			resultData.put(keyName, value);
			break;
		case Constants.POST_TIME:
			value = value.substring(value.indexOf("日期") + 3);
			resultData.put(keyName, value);
			break;
		case Constants.CONTENT:
			value = trim(value);
			resultData.put(keyName, value);
			break;
		default:
			break;
		}
	}

	@SuppressWarnings("unchecked")
	public void addCommentUrl(ParseUnit unit, Map<String, Object> resultData) {
		Map<String, Object> commentTask = new HashMap<String, Object>();
		String comUrl = unit.getUrl() + "#allcomments";
		commentTask.put("link", comUrl);
		commentTask.put("rawlink", comUrl);
		commentTask.put("linktype", "newscomment");
		resultData.put("comment_url", comUrl);
		List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
				.get("tasks");
		rtasks.add(commentTask);
	}

	public void formatStr(Map<String, Object> resultData, ParseUnit unit,
			String str, String keyName) {
		switch (keyName) {
		case Constants.ARTICLE_RANKING:
			resultData.put(keyName, str.substring(str.indexOf("：") + 1));
			break;
		case Constants.REG_TIME:
			resultData.put(keyName, str.substring(str.indexOf("：") + 1));
			break;
		case Constants.POST_TIME:
			resultData.put(keyName, str.substring(0, str.lastIndexOf(' ')));
			break;
		default:
			break;
		}
	}

	public String trim(String str) {
		char[] value = str.toCharArray();
		int len = value.length;
		int st = 0;
		while ((st < len) && ((value[st] == '') || (value[st] == ' '))) {
			st++;
		}
		while ((st < len) && ((value[st] == '') || (value[st] == ' '))) {
			len--;
		}
		return (((st > 0) || (len < value.length)) ? str.substring(st, len)
				: str);
	}

	/**
	 * 过滤 http:// https://
	 */
	public String urlFilter(String url) {
		if (url.startsWith("http://")) {
			return url.substring(7);
		} else {
			return url.substring(8);
		}
	}

}
