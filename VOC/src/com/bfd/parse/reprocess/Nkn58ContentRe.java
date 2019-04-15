package com.bfd.parse.reprocess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @sie:微客网-新闻 (Nkn58)
 * @function 新闻内容页后处理插件
 * 
 * @author bfd_02
 *
 */

public class Nkn58ContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Nkn58ContentRe.class);

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
		 * @function 格式化路径字段 eg:"您的位置： 首页 > 科技资讯频道 > 业界要闻 > 列表"
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
		 *           eg:"2013-12-21 10:10:30 来源：元器件交易网"
		 * 
		 */

		if (resultData.containsKey(Constants.SOURCE)) {
			String source = (String) resultData.get(Constants.SOURCE);
			int index = source.indexOf("来源");
			String postTime = source.substring(0, index).trim();
			source = source.substring(index + 3).trim();
			resultData.put(Constants.POST_TIME, postTime);
			resultData.put(Constants.SOURCE, source);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}