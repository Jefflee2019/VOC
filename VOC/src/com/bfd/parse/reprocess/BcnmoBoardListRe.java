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
 * @site Bcnmo
 * @function 版块列表页后处理
 * @author bfd_04
 * 
 */
public class BcnmoBoardListRe implements ReProcessor {

	@SuppressWarnings("unused")
	private static final Log LOG = LogFactory.getLog(BcnmoBoardListRe.class);

	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		List<Object> boardlinkList = new ArrayList<Object>();
		
		if (resultData.containsKey("boardlink1")) {
			List tempList1 = (List) resultData.get("boardlink1");
			boardlinkList.addAll(tempList1);
			resultData.remove("boardlink1");
		}
		if (resultData.containsKey("boardlink2")) {
			List tempList2 = (List) resultData.get("boardlink2");
			boardlinkList.addAll(tempList2);
			resultData.remove("boardlink2");
		}
		if (resultData.containsKey("boardlink3")) {
			List tempList3 = (List) resultData.get("boardlink3");
			boardlinkList.addAll(tempList3);
			resultData.remove("boardlink3");
		}
		if (resultData.containsKey("boardlink4")) {
			List tempList4 = (List) resultData.get("boardlink4");
			boardlinkList.addAll(tempList4);
			resultData.remove("boardlink4");
		}
		if (resultData.containsKey("boardlink5")) {
			List tempList5 = (List) resultData.get("boardlink5");
			boardlinkList.addAll(tempList5);
			resultData.remove("boardlink5");
		}
		resultData.put("boardlink", boardlinkList);
		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
	
}
