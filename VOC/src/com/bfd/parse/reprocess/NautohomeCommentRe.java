package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;

/**
 * 站点名：Nautohome
 * 
 * 功能：标准化部分字段 调整数据结构 去掉最后一页的下一页
 * 
 * @author bfd_06
 */
public class NautohomeCommentRe implements ReProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		int processcode = 0;

		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		if (resultData.containsKey(Constants.COMMENTS)) {
			/**
			 * 评论下半部分
			 */
			List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
					.get(Constants.COMMENTS);
			/**
			 * 评论上半部分
			 */
			List<Map<String, Object>> titleitems = (List<Map<String, Object>>) resultData
					.get("titleitems");
			for (int i = 0; i < comments.size(); i++) {
				Map<String, Object> comment = comments.get(i);
				Map<String, Object> titleItem = titleitems.get(i);
				Map<String, Object> referComments = new HashMap<String, Object>();
				/**
				 * 标准化comments中的字段
				 */
				// UP_CNT
				if (comment.containsKey(Constants.UP_CNT)) {
					formatCom(Constants.UP_CNT,
							(String) comment.get(Constants.UP_CNT), comment);
				}
				// REPLYFLOOR
				if (titleItem.containsKey(Constants.REPLYFLOOR)) {
					formatCom(Constants.REPLYFLOOR,
							(String) titleItem.get(Constants.REPLYFLOOR),
							titleItem);
				}
				// REFER_COMM_TIME
				if (comment.containsKey(Constants.REFER_COMM_TIME)) {
					// REFER_REPLYFLOOR
					formatCom(Constants.REFER_REPLYFLOOR,
							(String) comment.get(Constants.REFER_REPLYFLOOR),
							comment);
					String referCommTime = formatRef(Constants.REFER_COMM_TIME,
							(String) comment.get(Constants.REFER_COMM_TIME),
							comment);
					/**
					 * 引用楼层数据结构调整
					 */
					referComments.put(Constants.REFER_COMM_TIME, referCommTime);
					referComments.put(Constants.REFER_COMM_CONTENT,
							comment.get(Constants.REFER_COMM_CONTENT));
					referComments.put(Constants.REFER_COMM_USERNAME,
							comment.get(Constants.REFER_COMM_USERNAME));
					referComments.put(Constants.REFER_REPLYFLOOR,
							comment.get(Constants.REFER_REPLYFLOOR));
					comment.remove(Constants.REFER_COMM_TIME);
					comment.remove(Constants.REFER_COMM_CONTENT);
					comment.remove(Constants.REFER_COMM_USERNAME);
					comment.remove(Constants.REFER_REPLYFLOOR);
					comment.put(Constants.REFER_COMMENTS, referComments);
				}
				/**
				 * 标准化titleitems中的字段
				 */
				// COMMENT_TIME
				if (titleItem.containsKey(Constants.COMMENT_TIME)) {
					formatCom(Constants.COMMENT_TIME,
							(String) titleItem.get(Constants.COMMENT_TIME),
							titleItem);
				}
				/**
				 * 将评论上下部分合并
				 */
				comment.putAll(titleItem);
			}
			resultData.remove("titleitems");
		}
		/**
		 * 删掉下一页为空的最后一页
		 */
		if (resultData.containsKey(Constants.NEXTPAGE)) {
			/**
			 * 此处注意 系统中取出的nextpage为String 测试时为Map<String, Object>
			 */
			// Map<String, Object> nextpage = (Map<String, Object>) resultData
			// .get(Constants.NEXTPAGE);
			// if (nextpage.get(Constants.LINK) == "") {
			// List<Map<String, Object>> tasks = (List<Map<String, Object>>)
			// resultData
			// .get(Constants.TASKS);
			// tasks.remove(0);
			// resultData.remove(Constants.NEXTPAGE);
			// }
			String nextpage = (String) resultData.get(Constants.NEXTPAGE);
			if (nextpage.equals("")) {
				List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
						.get(Constants.TASKS);
				tasks.remove(0);
				resultData.remove(Constants.NEXTPAGE);
			}
		}

		return new ReProcessResult(processcode, processdata);
	}

	public void formatCom(String keyName, String value,
			Map<String, Object> result) {
		switch (keyName) {
		case Constants.UP_CNT:
			int indexA = value.indexOf("(");
			int indexB = value.indexOf(")");
			value = value.substring(indexA + 1, indexB);
			result.put(keyName, value);
			break;
		case Constants.REPLYFLOOR:
		case Constants.REFER_REPLYFLOOR:
			value = value.replace("楼", "");
			result.put(keyName, value);
			break;
		case Constants.COMMENT_TIME:
			value = ConstantFunc.convertTime(value.substring(0, value.indexOf("[") - 1));
			result.put(keyName, value);
			break;
		default:
			break;
		}
	}

	public String formatRef(String keyName, String value,
			Map<String, Object> result) {
		value = match("于([0-9- :]+)发表", value);
		result.put(keyName, value);
		return value;
	}

	public String match(String regular, String matchedStr) {
		Pattern patten = Pattern.compile(regular);
		Matcher matcher = patten.matcher(matchedStr);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return null;
	}

}
