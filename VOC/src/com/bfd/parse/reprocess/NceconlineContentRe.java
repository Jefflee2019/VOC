package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * @site：世界经理人 Nceconline
 * @function：新闻内容页后处理
 * @author bfd_02
 *
 */
public class NceconlineContentRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			/**
			 * @param author post_time source
			 * @function format 
			 * eg:作者：康斯坦丁 来源： 最佳管理智囊 发表时间：2016-06-06
			 */
			if (resultData.containsKey("tag")) {
				String tem = resultData.get("tag").toString();
				String authorReg = "作者：(\\S*)\\s*";
				String author = Regex(authorReg, tem);
				String sourceReg = "来源：\\s*(\\S*)\\s*";
				String source = Regex(sourceReg, tem);
				String posttimeReg = "发表时间：(\\S*)\\s*";
				String posttime = Regex(posttimeReg, tem);
				resultData.put(Constants.AUTHOR, author);
				resultData.put(Constants.SOURCE, source);
				resultData.put(Constants.POST_TIME, posttime);
			}

			/**
			 * @param content
			 * @function format
			 * @description The content is not put in a label.Need to delete the
			 *              others
			 */
			if (resultData.containsKey(Constants.CONTENT)) {
				String tem = resultData.get(Constants.CONTENT).toString();
				String content = resultData.containsKey("reply_cnt") ? tem.replace(
						(String) resultData.get("reply_cnt"), "") : tem;
				content = resultData.containsKey("tag") ? content.replace((String) resultData.get("tag"), "") : content;
				content = resultData.containsKey("keyword") ? content.replace((String) resultData.get("keyword"), "") : content;
				content = resultData.containsKey("ads") ? content.replace((String) resultData.get("ads"), "")
						: content;
				content = resultData.containsKey("collector") ? content.replace((String) resultData.get("collector"), "")
						: content;
				content = resultData.containsKey("editor") ? content.replace((String) resultData.get("editor"), "") : content;
				content = resultData.containsKey("play_cnt") ? content.replace((String) resultData.get("play_cnt"), "")
						: content;
				content = resultData.containsKey("brief") ? content.replace((String) resultData.get("brief"), "") : content;
				content = resultData.containsKey("favor_cnt") ? content.replace((String) resultData.get("favor_cnt"), "")
						: content;
				content = resultData.containsKey("authorname") ? content.replace((String) resultData.get("authorname"), "")
						: content;
				content = content.trim();

				resultData.put(Constants.CONTENT, content);
				resultData.remove("reply_cnt");
				resultData.remove("tag");
				resultData.remove("keyword");
				resultData.remove("ads");
				resultData.remove("collector");
				resultData.remove("editor");
				resultData.remove("play_cnt");
				resultData.remove("brief");
				resultData.remove("favor_cnt");
				resultData.remove("authorname");
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}

	public String Regex(String regex, String matcher) {
		Matcher match = Pattern.compile(regex).matcher(matcher);
		String result = null;
		if (match.find()) {
			result = match.group(1);
		}
		return result;

	}
}