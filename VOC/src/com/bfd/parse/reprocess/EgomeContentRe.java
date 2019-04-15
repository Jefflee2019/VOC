package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：国美在线 主要功能：生成评论链接
 * 
 * @author bfd_02
 */

public class EgomeContentRe implements ReProcessor {

	private static final Log LOG = LogFactory.getLog(EgomeContentRe.class);

	@SuppressWarnings({ "unchecked" })
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		LOG.info("this is EgomeContentRe  >>>" + result);

		Map<String, Object> processdata = new HashMap<String, Object>();

		Map<String, Object> resultData = result.getParsedata().getData();
		String url = unit.getUrl();

		/**
		 * 拼接评论链接
		 * 商品页url：http://item.gome.com.cn/9134320732-1123230588.html
		 * 评论页链接：http://ss.gome.com.cn/item/v1/prdevajsonp/appraiseNew/9134320732/1/all/0/10/flag/appraise
		 */

		String comUrlHead = "http://ss.gome.com.cn/item/v1/prdevajsonp/appraiseNew/";
		String urls[] = url.split("http://item.gome.com.cn/");
		String id[] = urls[1].split("-");
		String commenturl = comUrlHead + id[0] + "/1/all/0/flag/appraise/all?callback=all";
		List<Map<String, Object>> tasks = (List<Map<String, Object>>) resultData.get("tasks");
		Map<String, Object> task = new HashMap<String, Object>();
		task.put("link", commenturl);
		task.put("rawlink", commenturl);
		task.put("linktype", "eccomment");
		tasks.add(task);

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}
