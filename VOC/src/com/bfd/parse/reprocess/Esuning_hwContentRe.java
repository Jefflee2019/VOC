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
 * 站点：苏宁易购荣耀旗舰店 作用：添加评论，咨询url
 * 
 * @author bfd_04
 *
 */
public class Esuning_hwContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Esuning_hwContentRe.class);
	private static final String COMM_URL_TEMP = "https://review.suning.com/ajax/cluster_review_lists/general--temp_id-total-1-default-10-----reviewList.htm";

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			// deal with comment
			Map<String, Object> commentTask = new HashMap<String, Object>();
			Map ma = (Map) resultData.get("comment_url");
			String url = (String) ma.get("link");
			//general--000000010398812072-0000000000
			Pattern p = Pattern.compile("general-(\\d*-\\d+)");
			Matcher m = p.matcher(url);
			if (m.find()) {
				try {
					String itemId = m.group(1);
					String commUrl = COMM_URL_TEMP.replaceAll("temp_id", itemId);
					commentTask.put("link", commUrl);
					commentTask.put("rawlink", commUrl);
					commentTask.put("linktype", "eccomment");
					resultData.put(Constants.COMMENT_URL, commUrl);
					List<Map> tasks = (List<Map>) resultData.get("tasks");
					tasks.clear();
					tasks.add(commentTask);
				} catch (Exception e) {
					// e.printStackTrace();
					LOG.error("regex parse error");
				}
			}

		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
