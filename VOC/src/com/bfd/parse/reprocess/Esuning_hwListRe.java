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
 * 站点：苏宁易购 作用：处理商品列表页
 * 
 * @author bfd_04
 *
 */
public class Esuning_hwListRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(Esuning_hwListRe.class);
	private static final Pattern IID_PATTERN = Pattern.compile("cp=(\\d+)");

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		int itemCount = 0;
		if (resultData != null) {
			// deal with items
			if (resultData.containsKey("items")) {
				List items = (List) resultData.get("items");
				itemCount = items.size();
			}

			// deal with nextpage
			Map<String, Object> nextpageTask = new HashMap<String, Object>();
			String url = unit.getUrl();
			Matcher match = IID_PATTERN.matcher(url);

			if (match.find()) {
				try {
					String pageNo = match.group(1);

					if (itemCount > 0) {
						int nextpage = Integer.parseInt(pageNo) + 1; // cal
																		// nextpage
						String nextpageUrl = url.replace("cp=" + pageNo, "cp=" + String.valueOf(nextpage)); // form
																											// nextpage
																											// url
						nextpageTask.put("link", nextpageUrl);
						nextpageTask.put("rawlink", nextpageUrl);
						nextpageTask.put("linktype", "eclist");
						resultData.put(Constants.NEXTPAGE, nextpageUrl); // add
																			// nextpage
																			// field
						List<Map> tasks = (List<Map>) resultData.get("tasks");
						tasks.add(nextpageTask);
					}
				} catch (Exception e) {
					LOG.debug(e.toString());
				}
			}
		}

		ParseUtils.getIid(unit, result); // get iid
		return new ReProcessResult(SUCCESS, processdata);
	}

}
