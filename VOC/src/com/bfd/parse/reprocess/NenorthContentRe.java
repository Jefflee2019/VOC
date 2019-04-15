package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site：北方网(Nenorth)
 * @function：新闻内容页后处理
 * @author bfd_04
 *
 */
public class NenorthContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		/**
		 * 兼容多个模板，处理author,post_time,source 多字段标定在post_time中，再清洗
		 */
		if (resultData.containsKey(Constants.POST_TIME)) {
			String resource = resultData.get(Constants.POST_TIME).toString();
			// 处理来源 source
			// 来源： 中关村在线 2018-05-04 14:28:27
			// 来源： 网易科技 作者：晗冰 编辑：张思政 2018-05-29 15:43:20
			if (resource.contains("来源")) {
				String sourceRex = "来源\\S\\s*(\\S*)";
				String source = getRex(resource, sourceRex);
				resultData.put(Constants.SOURCE, source);
			}

			// 处理作者 author 编辑与作者二选一，作者优先
			if (resource.contains("作者") || resource.contains("编辑")) {
				String author = null;
				String editor = null;
				if (resource.contains("作者")) {
					String authorRex = "作者\\S\\s*(\\S*)";
					author = getRex(resource, authorRex);
					// 处理作者为空时匹配到编辑的情况
					if (author != null && author.contains("编辑")) {
						author = author.replaceAll("编辑\\S", "");
					}
				}
				if (resource.contains("编辑")) {
					String editorRex = "编辑\\S\\s*(\\S*)";
					editor = getRex(resource, editorRex);
				}
				if (author == null || author.equals("")) {
					author = editor;
				}
				resultData.put(Constants.AUTHOR, author);
			}

			// 处理发表时间 post_time
			String postTimeRex = "(\\d{4}\\S\\d{1,2}\\S\\d{1,2}\\s*\\d{1,2}\\S\\d{1,2}?(\\S\\d{1,2}))";
			String postTime = getRex(resource, postTimeRex);
			resultData.put(Constants.POST_TIME, postTime);
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

	public String getRex(String resource, String rex) {
		String result = null;
		Matcher match = Pattern.compile(rex).matcher(resource);
		if (match.find()) {
			result = match.group(1);
		}
		return result;
	}
}
