package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.Collections;
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
 * @site:搜狐博客 (Nsohublog)
 * @function 新闻内容页后处理插件,处理来源、标签及评论链接
 * 
 * @author bfd_02
 *
 */

public class NsohublogContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NsohublogContentRe.class);

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

		// 处理来源、标签
		if (resultData.containsKey(Constants.SOURCE) && resultData.containsKey(Constants.TAG)) {
			String source = resultData.get(Constants.SOURCE).toString();
			String tag = null;
			if (!source.contains("分类：")) {
				String tagReg = "标签：([\\S\\s]*)";
				tag = regexMatch(tagReg, source).trim();
				if (tag.contains(" ")) {
					String[] tagarr = tag.split(" ");
					List<String> taglist = new ArrayList<String>();
					Collections.addAll(taglist, tagarr);
					resultData.put(Constants.TAG, taglist);
				}
				resultData.remove(Constants.SOURCE);
			} else if (!source.contains("标签：")) {
				resultData.remove(Constants.TAG);
				String sourReg = "分类： [\\S\\s]*?(|标签)";
				source = regexMatch(sourReg, source).trim();
				resultData.put(Constants.SOURCE, source);
			} else if (source.contains("分类：") && source.contains("标签：")) {
				String tagReg = "标签：([\\S\\s]*)";
				tag = regexMatch(tagReg, source).trim();
				if (tag.contains(" ")) {
					String[] tagarr = tag.split(" ");
					List<String> taglist = new ArrayList<String>();
					Collections.addAll(taglist, tagarr);
					resultData.put(Constants.TAG, taglist);
				}
				String sourReg = "分类：(\\s*\\S*\\s*)(?:\\|\\s*标签)";
				source = regexMatch(sourReg, source).trim();
				resultData.put(Constants.SOURCE, source);
			}
		}

		// 评论链接
		// http://i.sohu.com/a/app/discuss/indexBlogList.htm?_input_encode=UTF-8&ids=blog_322364320_0_em91bGFuMTIzQHNvaHUuY29t&page=1&sz=10
		// 获取页面源码
		// var _xpt = 'd2FuZ3l1a3VuX2Jsb2dAc29odS5jb20='
		try {
			String pageData = unit.getPageData();
			String url = unit.getUrl();
			String urlidReg = "/(\\d+).html";
			String encodeReg = "var\\s*_xpt\\s*=\\s*'(\\S*)=";

			String encode = regexMatch(encodeReg, pageData);
			String urlid = regexMatch(urlidReg, url);
			Map<String, Object> commentTask = new HashMap<String, Object>();
			if (!encode.equals("") && !urlid.equals("")) {
				String commUrl = "http://i.sohu.com/a/app/discuss/indexBlogList.htm?_input_encode=UTF-8&ids=blog_"
						+ urlid + "_0_" + encode + "&page=1&sz=10";
				commentTask.put(Constants.LINK, commUrl);
				commentTask.put(Constants.RAWLINK, commUrl);
				commentTask.put(Constants.LINKTYPE, "newscomment");
				resultData.put(Constants.COMMENT_URL, commUrl);
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get(Constants.TASKS);
				tasks.add(commentTask);
			}
			ParseUtils.getIid(unit, result);
		} catch (Exception e) {
			LOG.error("excuteParse error");
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

	private String regexMatch(String regex, String data) {
		String targetid = "";
		Matcher match = Pattern.compile(regex).matcher(data);
		if (match.find()) {
			targetid = match.group(1);
		}
		return targetid;
	}
}