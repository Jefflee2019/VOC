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
 * @site:央广网-新闻 (Ncnr)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NcnrContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NcnrContentRe.class);

	@SuppressWarnings({ "unchecked", "rawtypes" })
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
		 * @param editor编辑
		 * @function 格式化editor值 eg:"编辑：王家乐"
		 */

		if (resultData.containsKey("editor")) {
			String editor = (String) resultData.get("editor");
			if (editor.contains("编辑")) {
				editor = editor.replace("编辑：", "").trim();
			}
			if (editor.contains("责编")) {
				editor = editor.replace("责编：", "").trim();
			}
			resultData.put("editor", editor);
		}

		/**
		 * @param post_time发表时间
		 * @function 格式化post_time值 eg1:"post_time: 2016-01-05 06:32:00 来源：新浪科技"
		 *           eg2:"2016-03-18 09:19:00"
		 */
		if (resultData.containsKey(Constants.POST_TIME)) {
			String postTime = resultData.get(Constants.POST_TIME).toString();
			String regex = "(\\d+-\\d+-\\d+\\s+\\d+:\\d+(:\\d+)?)";
			Matcher match = Pattern.compile(regex).matcher(postTime);
			if (match.find()) {
				postTime = match.group();
				resultData.put(Constants.POST_TIME, postTime);
			}
		}

		/**
		 * @param author作者
		 * @function 对于有作者的页面，取出该字段 eg1:"author: 每经记者 赵娜"
		 */
		// 作者包含在内容中
		String content = resultData.get(Constants.CONTENT).toString();
		// 正则匹配作者字段
		String regex = "每经记者\\s*(\\S*)\\s+";
		Matcher match = Pattern.compile(regex).matcher(content);
		if (match.find()) {
			String author = match.group(1);
			resultData.put(Constants.AUTHOR, author);
		}

		/**
		 * @param source
		 *            来源
		 * @function 格式化source值 eg1:"source: 2016-04-05 15:32 来源：央广网 打印本页 关闭"
		 *           eg2:"央广网"
		 */
		if (resultData.containsKey(Constants.SOURCE)) {
			String source = resultData.get(Constants.SOURCE).toString();
			if (source.contains("来源")) {
				Matcher ma = Pattern.compile("来源：(\\S*)\\s*").matcher(source);
				if (ma.find()) {
					source = ma.group(1);
				}
			}
			resultData.put(Constants.SOURCE, source);
		}

		/**
		 * @param cate
		 *            路径
		 * @function 格式化cate值 eg1:"[ 首页 > 名胜 > 正文]" eg2:"[新闻中心, 央广网国内, 国内图片]"
		 */
		if (resultData.containsKey(Constants.CATE)) {
			Object cate = resultData.get(Constants.CATE);
			if (cate instanceof List) {
				ArrayList cateList = (ArrayList) cate;
				String cateStr = cateList.get(0).toString();
				ArrayList newCateList = new ArrayList<String>();
				if (!cateStr.equals("") && cateStr.contains(">")) {
					String[] newCate = cateStr.split(">");
					for (int i = 0; i < newCate.length; i++) {
						newCateList.add(newCate[i].trim());
					}
					resultData.put(Constants.CATE, newCateList);
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}