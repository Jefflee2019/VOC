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
import com.bfd.parse.util.ParseUtils;

/**
 * @site:新浪手机/数码(Esinamobile)
 * @function 商品详情页后处理插件，处理价格数据格式及拼接评论链接
 * 
 * @author bfd_02
 *
 */

public class EsinamobileContentRe implements ReProcessor {
	private static final Log LOG = LogFactory
			.getLog(EsinamobileContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();
		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			return null;
		}
		// 处理价格
		if (resultData.containsKey(Constants.PRICE)) {
			String price = resultData.get("price").toString();
			Matcher m = Pattern.compile("(\\d+)").matcher(price);
			if (m.find()) {
				price = m.group();
			} else {
				price = "0";
			}
			resultData.put(Constants.PRICE, Integer.parseInt(price));
		}

		/**
		 * @param comment_url评论链接
		 * 
		 * @function 需要在页面拼接出评论链接，将与rawlink,linktype放入tasks中
		 */
		// 评论页3部分链接：总评 缺点 优点
		// http://comment5.news.sina.com.cn/page/info?channel=kj&newsid=sj-18266&page_size=10&page=1
		// http://comment5.news.sina.com.cn/page/info?channel=kj&newsid=sj-18266a&page_size=10&page=1
		// http://comment5.news.sina.com.cn/page/info?channel=kj&newsid=sj-18266b&page_size=10&page=1
		Pattern idPattern = Pattern.compile("(\\d+).html");
		String commUrlHead = "http://comment5.news.sina.com.cn/page/info?channel=kj&newsid=sj-";
		String commUrlEnd = "&page_size=10&page=1";
		String itemid = null;
		// 获取内容页的url
		String url = unit.getUrl();
		// 总评task
		Map<String, Object> allcommentTask = new HashMap<String, Object>();
		// 缺点评论task
		Map<String, Object> badcommentTask = new HashMap<String, Object>();
		// 优点评论task
		Map<String, Object> goodcommentTask = new HashMap<String, Object>();
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData
				.get(Constants.TASKS);
		Matcher commMatch = idPattern.matcher(url);
		if (commMatch.find()) {
			try {
				itemid = commMatch.group(1);
				// 总评链接
				String allcommUrl = commUrlHead + itemid + commUrlEnd;
				allcommentTask.put(Constants.LINK, allcommUrl);
				allcommentTask.put(Constants.RAWLINK, allcommUrl);
				allcommentTask.put(Constants.LINKTYPE, "eccomment");
				LOG.info("url:" + unit.getUrl() + "taskdata is "
						+ allcommentTask.get(Constants.LINK)
						+ allcommentTask.get(Constants.RAWLINK)
						+ allcommentTask.get(Constants.LINKTYPE));
				resultData.put(Constants.COMMENT_URL, allcommUrl);

				// 优点评论链接
				String goodcommUrl = commUrlHead + itemid + "a"
						+ commUrlEnd;
				goodcommentTask.put(Constants.LINK, goodcommUrl);
				goodcommentTask.put(Constants.RAWLINK, goodcommUrl);
				goodcommentTask.put(Constants.LINKTYPE, "eccomment");
				LOG.info("url:" + unit.getUrl() + "taskdata is "
						+ goodcommentTask.get(Constants.LINK)
						+ goodcommentTask.get(Constants.RAWLINK)
						+ goodcommentTask.get(Constants.LINKTYPE));
				// resultData.put(Constants.COMMENT_URL, goodcomm_url);

				// 缺点评论链接
				String badcommUrl = commUrlHead + itemid + "b"
						+ commUrlEnd;
				badcommentTask.put(Constants.LINK, badcommUrl);
				badcommentTask.put(Constants.RAWLINK, badcommUrl);
				badcommentTask.put(Constants.LINKTYPE, "eccomment");
				LOG.info("url:" + unit.getUrl() + "taskdata is "
						+ badcommentTask.get(Constants.LINK)
						+ badcommentTask.get(Constants.RAWLINK)
						+ badcommentTask.get(Constants.LINKTYPE));
				// resultData.put(Constants.COMMENT_URL, badcomm_url);

				tasks.add(allcommentTask);
				tasks.add(goodcommentTask);
				tasks.add(badcommentTask);
			} catch (Exception e) {
//				e.printStackTrace();
				LOG.error("regex parse error");
			}
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
