package com.bfd.parse.reprocess;

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
import com.bfd.parse.util.JsonUtil;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点：华为商城 作用：商品详情页后处理
 * 
 * @author bfd_04
 *
 */
public class EvmallContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(EvmallContentRe.class);
	private static final Pattern IID_PATTERN = Pattern.compile("(\\d+).html");
	private static final String URL_HEAD = "http://remark.vmall.com/remark/" + "queryEvaluate.json?pid=";
	private static final String URL_END = "&pageNumber=1";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			// deal with cate
			if (resultData.containsKey(Constants.CATE)) {
				List cate = (List) resultData.get("cate");
				String[] cateArr = cate.get(0).toString().split(" > ");
				cate.clear(); // clear old cate
				for (String temp : cateArr) {
					cate.add(temp.trim());
				}
				resultData.put(Constants.CATE, cate);
			}
			// deal with price
			if (resultData.containsKey(Constants.PRICE)) {
				String price = resultData.get("price").toString();
				Matcher m = Pattern.compile("(\\d+.\\d+)").matcher(price);
				if (m.find()) {
					price = m.group(1);
				}
				resultData.put(Constants.PRICE, price);
			}
			// deal with comment
			Map<String, Object> commentTask = new HashMap<String, Object>();
			String url = unit.getUrl();
			Matcher match = IID_PATTERN.matcher(url);

			if (match.find()) {
				try {
					String itemId = match.group(1);
					String commUrl = URL_HEAD + itemId + URL_END;
					commentTask.put("link", commUrl);
					commentTask.put("rawlink", commUrl);
					commentTask.put("linktype", "eccomment");
					LOG.info("url:" + unit.getUrl() + "taskdata is " + commentTask.get("link")
							+ commentTask.get("rawlink") + commentTask.get("linktype"));
					if (resultData != null && !resultData.isEmpty()) {
						resultData.put(Constants.COMMENT_URL, commUrl);
						List<Map> tasks = (List<Map>) resultData.get("tasks");
						tasks.add(commentTask);
					}
					ParseUtils.getIid(unit, result);
				} catch (Exception e) {
					// e.printStackTrace();
					LOG.error(e);
				}
			}
		}
		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is " + JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}

}
