package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.util.ParseUtils;
import com.bfd.parse.util.Unicode2UTF;

/**
 * 站点名：Bsohu
 * 
 * 解析站点字符串源码 给出下一页并提取作者信息
 * 
 * @author bfd_06
 */
public class BsohuPostRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(BsohuPostRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		String pageData = unit.getPageData();
		int[] indexs = match("\"#bbs_postlist\",\"content\":\"(.*?)</script>",
				pageData);
		String htmlContent = pageData.substring(indexs[0], indexs[1]);
		String scriptContent = pageData.substring(indexs[1]);
		/**
		 * 截取评论内容字符串
		 */
		htmlContent = htmlContent.substring(0,
				htmlContent.lastIndexOf("css") - 3);
		/**
		 * 去除多余字符
		 */
		htmlContent = htmlContent.replace("\\\"", "\"").replace("\\/", "/")
				.replace("\\n", "");
		// scriptContent = scriptContent.replace("\\\"", "\"").replace("\\/",
		// "/")
		// .replace("\\n", "").replace("\\t", "");
		/**
		 * 解码
		 */
		htmlContent = Unicode2UTF.decodeUnicode(htmlContent);
		scriptContent = Unicode2UTF.decodeUnicode(scriptContent);
		/**
		 * 封装成HtmlCleaner
		 */
		HtmlCleaner cleaner = new HtmlCleaner();
		TagNode root = cleaner.clean(htmlContent);
		try {
			TagNode div = (TagNode) root
					.evaluateXPath("//div[@id='bbs_postlist']")[0];
			int childsSize = div.getChildTagList().size();
			List<Map<String, Object>> author = new ArrayList<Map<String, Object>>();
			List<Map<String, Object>> replys = new ArrayList<Map<String, Object>>();
			int i = 0;
			for (; i < childsSize - 1; i++) {
				TagNode table = div.getChildTags()[i];
				/**
				 * 如果无评论则方法返回
				 */
				if (i == 0 && table.getName().equals("div")) {
					/**
					 * 标准化部分模板字段
					 */
					// CATE
					if (resultData.containsKey(Constants.CATE)) {
						formatAttr(Constants.CATE,
								(String) resultData.get(Constants.CATE),
								resultData);
					}
					// REPLYCOUNT
					if (resultData.containsKey(Constants.REPLYCOUNT)) {
						formatAttr(Constants.REPLYCOUNT,
								(String) resultData.get(Constants.REPLYCOUNT),
								resultData);
					}
					// VIEW_CNT
					if (resultData.containsKey(Constants.VIEW_CNT)) {
						formatAttr(Constants.VIEW_CNT,
								(String) resultData.get(Constants.VIEW_CNT),
								resultData);
					}

					return new ReProcessResult(processcode, processdata);
				}
				TagNode tr = table.getChildTags()[0].getChildTags()[0];
				// 名称
				TagNode a = tr.getChildTags()[0].getChildTags()[2]
						.getChildTags()[0];
				String name = a.getText().toString();
				// 时间
				TagNode spanNewFormat = tr.getChildTags()[1].getChildTags()[3]
						.getChildTags()[0];
				String date = spanNewFormat.getText().toString();
				// 楼层
				TagNode p = tr.getChildTags()[1].getChildTags()[0];
				String floor = p.getText().toString();
				// 内容
				TagNode divWrap = (TagNode) table
						.evaluateXPath("//div[@class='wrap']")[0];
				String content = divWrap.getText().toString().trim();
				String afterUrl = unit.getUrl();
				afterUrl = afterUrl.substring(afterUrl.lastIndexOf("/"));
				String pageNumMark = matchOne("p(\\d+)", afterUrl);
				// 用户等级
				TagNode spanBbsGrade = tr.getChildTags()[0].getChildTags()[3]
						.getChildTags()[0];
				Map<String, String> levelMap = getLevelMap(
						"bbs_grade_[\\d\\w]+", "等级：(\\d+)级", scriptContent);
				/**
				 * 如果是第一页则需要提取作者相关字段以及NEWSTIME
				 */
				if (i == 0 && (pageNumMark == null || pageNumMark.equals("1"))) {
					Map<String, Object> authorMap = new HashMap<String, Object>();
					authorMap.put(Constants.AUTHORNAME, name);
					authorMap.put(Constants.AUTHOR_LEVEL, levelMap
							.get(spanBbsGrade.getAttributeByName("class")));
					spanNewFormat = tr.getChildTags()[1].getChildTags()[3];
					date = spanNewFormat.getText().toString();
					date = "20" + date.substring(date.indexOf("发表于") + 4);
					resultData.put(Constants.NEWSTIME, date);
					resultData.put(Constants.CONTENTS, content);
					author.add(authorMap);
//					System.out.println(htmlContent);
				} else {
					Map<String, Object> reply = new HashMap<String, Object>();
					reply.put(Constants.REPLYUSERNAME, name);
					reply.put(Constants.REPLYDATE, "20" + date);
					reply.put(Constants.REPLYFLOOR, floor);
					// REPLYFLOOR
					formatAttr(Constants.REPLYFLOOR,
							(String) reply.get(Constants.REPLYFLOOR), reply);
					reply.put(Constants.REPLYCONTENT, content);
					reply.put(Constants.REPLY_LEVEL, levelMap.get(spanBbsGrade
							.getAttributeByName("class")));
					replys.add(reply);
				}
			}
			resultData.put(Constants.AUTHOR, author);
			resultData.put(Constants.REPLYS, replys);
			/**
			 * 给出下一页
			 */
			TagNode form = div.getChildTags()[i];
			if (form.getChildTagList().size() == 2) {
				TagNode nextUrlNode = form.getChildTags()[0].getChildTags()[0];
				String strEnd = nextUrlNode.getAttributeByName("href");
				String url = unit.getUrl();
				// 去掉 http:// https://
				if (url.startsWith("http://")) {
					url = url.substring(7);
				} else if (url.startsWith("https://")) {
					url = url.substring(8);
				}
				String strStart = url.substring(0, url.indexOf("/"));
				String nextUrl = "http://" + strStart + strEnd;
				List<Map<String, Object>> rtasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				Map<String, Object> rtask = new HashMap<String, Object>();
				rtask.put("link", nextUrl);
				rtask.put("rawlink", nextUrl);
				rtask.put("linktype", "bbspost");
				rtasks.add(rtask);
				resultData.put(Constants.NEXTPAGE, nextUrl);
				ParseUtils.getIid(unit, result);
			}
		} catch (XPatherException e) {
			LOG.error("json format conversion error in the process() method", e);
		}
		/**
		 * 标准化部分模板字段
		 */
		// CATE
		if (resultData.containsKey(Constants.CATE)) {
			formatAttr(Constants.CATE, (String) resultData.get(Constants.CATE),
					resultData);
		}
		// REPLYCOUNT
		if (resultData.containsKey(Constants.REPLYCOUNT)) {
			formatAttr(Constants.REPLYCOUNT,
					(String) resultData.get(Constants.REPLYCOUNT), resultData);
		}
		// VIEW_CNT
		if (resultData.containsKey(Constants.VIEW_CNT)) {
			formatAttr(Constants.VIEW_CNT,
					(String) resultData.get(Constants.VIEW_CNT), resultData);
		}

		return new ReProcessResult(processcode, processdata);
	}

	public int[] match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		matcher.find();
		int[] paras = { matcher.start(), matcher.end() };
		return paras;
	}

	public String matchOne(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	public Map<String, String> getLevelMap(String regular1, String regular2,
			String matchedStr) {
		Map<String, String> levelMap = new HashMap<String, String>();
		List<String> keys = new ArrayList<String>();
		List<String> values = new ArrayList<String>();
		Pattern patten1 = Pattern.compile(regular1);
		Matcher matcher1 = patten1.matcher(matchedStr);
		while (matcher1.find()) {
			keys.add(matcher1.group());
		}
		Pattern patten2 = Pattern.compile(regular2);
		Matcher matcher2 = patten2.matcher(matchedStr);
		while (matcher2.find()) {
			values.add(matcher2.group(1));
		}
		for (int i = 0; i < keys.size(); i++) {
			levelMap.put(keys.get(i), values.get(i));
		}

		return levelMap;
	}

	public void formatAttr(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.REPLYFLOOR:
			value = value.replace("楼", "").replace(" ", "");
			if (value.equals("沙发")) {
				value = "1";
			} else if (value.equals("板凳")) {
				value = "2";
			}
			result.put(keyName, value);
			break;
		case Constants.CATE:
			List<String> strList = new ArrayList<String>();
			String[] atrArray = value.split(" -> ");
			for (String str : atrArray) {
				strList.add(str);
			}
			result.put(keyName, strList);
			break;
		case Constants.REPLYCOUNT:
			int indexH = value.indexOf("回复");
			value = value.substring(indexH + 3).replace(" ", "");
			if(value.equals("")){
				value = "0";
			}
			result.put(keyName, value);
			break;
		case Constants.VIEW_CNT:
			int indexF = value.indexOf("回复");
			value = value.substring(3, indexF - 1);
			if(value.equals("")){
				value = "0";
			}
			result.put(keyName, value);
			break;
		default:
			break;
		}
	}
}