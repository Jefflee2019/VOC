package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class Esuning_hwConmmentRe implements ReProcessor {

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null) {
			if (resultData.containsKey(Constants.COMMENTS)) {
				List<Map<String, Object>> comments = (List<Map<String, Object>>) resultData
						.get(Constants.COMMENTS);
				for (Map<String, Object> map : comments) {
					String reply_cnt = map.get("reply_cnt").toString().replace("(", "").replace(")", "").trim();
					String favor_cnt = map.get("favor_cnt").toString().replace("(", "").replace(")", "").trim();
					map.put("reply_cnt", reply_cnt);
					map.put("favor_cnt", favor_cnt);
					
				}
			}
		}
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}

}
