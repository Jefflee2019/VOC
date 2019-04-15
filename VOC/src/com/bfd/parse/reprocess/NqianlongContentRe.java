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
 * @sie:千龙网-新闻 (Nqianlong)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class NqianlongContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NqianlongContentRe.class);

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
		 * @function 模板路径同级多块取不到最后一个值，因为最后一个值与其他的结构不一样
		 *           eg:"[千龙科技 >> 手机 >> 要闻 直播 | 滚动 | 专题]"
		 * 
		 */
		if (resultData.containsKey(Constants.CATE)) {
			Object oldCate = resultData.get(Constants.CATE);
			if (oldCate instanceof List) {
				ArrayList oldCateList = (ArrayList) oldCate;
				String oldCatestr = oldCateList.get(0).toString();
				ArrayList newCateList = new ArrayList<String>();
				if (!oldCatestr.equals("") && oldCatestr.contains(">>")) {
					String[] newCate = oldCatestr.split(">>");
					for (int i = 0; i < newCate.length; i++) {
						newCateList.add(newCate[i].trim());
					}
					resultData.put(Constants.CATE, newCateList);
				}
			}
		}

		/**
		 * @param editor编辑
		 * @function 格式化editor的值 eg1:"编辑：黄朝晖" eg2:"责任编辑：陈群(QN054)"
		 * 
		 */

		if (resultData.containsKey("editor")) {
			String editor = resultData.get("editor").toString();
			if (editor.contains("：")) {
				int index = editor.indexOf("：");
				editor = editor.substring(index + 1).trim();
				resultData.put("editor", editor);
			}
		}

		/**
		 * @param source
		 *            来源
		 * @function 格式化source的值 eg1:"2015-12-19 19:41:58 | | 打印 | 字体：大 中 小 千龙网"
		 *           eg2:"京华时报" eg3:"来源：中关村在线"
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);
			if (!source.equals("") && source.contains("来源")) {
				int index = source.indexOf("来源");
				source = source.substring(index + 2);
			} else if (!source.equals("") && source.contains("大 中 小")) {
				String[] sourceArr = source.split("大 中 小");
				source = sourceArr[1].trim();
			}
			resultData.put(Constants.SOURCE, source);
		}
		
		/**
		 * @param post_time发表时间
		 * @function 格式化post_time的值
		 *           eg1:"2015-12-19 19:41:58 | | 打印 | 字体：大 中 小 千龙网"
		 *           eg2:"2016-02-24 08:27" eg3:"2014-02-14 08:26:56"
		 */

		if (resultData.containsKey(Constants.POST_TIME)) {
			String oldPostTime = (String) resultData.get(Constants.POST_TIME);
			if (!oldPostTime.equals("")) {
				String rex = "\\d{4}-\\d{2}-\\d{2}\\s+\\d{2}:\\d{2}(:\\d{2})?";
				Pattern pattern = Pattern.compile(rex, Pattern.DOTALL);
				Matcher matcher = pattern.matcher(oldPostTime);
				if (matcher.find()) {
					String newPostTime = matcher.group();
					resultData.put(Constants.POST_TIME, newPostTime);
				}
			}
		}

		/**
		 * @param brief简介
		 * @function 处理个别模板 content包括其它无关字段的情况
		 */

		if (resultData.containsKey(Constants.BRIEF)) {
			String brief = (String) resultData.get(Constants.BRIEF);
			// 文章标题
			String title = resultData.get(Constants.TITLE).toString();
			String content = resultData.get(Constants.CONTENT).toString();
			content = content.replace(title, "").replace(brief, "");
			resultData.put(Constants.CONTENT, content);
			resultData.remove(Constants.BRIEF);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}