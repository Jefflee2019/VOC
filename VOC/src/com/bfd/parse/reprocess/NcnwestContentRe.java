package com.bfd.parse.reprocess;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.ConstantFunc;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

public class NcnwestContentRe implements ReProcessor {
	private final static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace parseFace) {
		Map<String, Object> resultData = new HashMap<String, Object>();
		Map<String, Object> processdata = new HashMap<String, Object>();
		resultData = result.getParsedata().getData();
		if (resultData != null && !resultData.isEmpty()) {
			if (resultData.containsKey(Constants.POST_TIME)) {
				try {
					Date d = sdf.parse((String)resultData.get(Constants.POST_TIME));
					resultData.put(Constants.POST_TIME, sdf.format(d));
				} catch (ParseException e) { // 再次尝试
					resultData.put(Constants.POST_TIME,
							ConstantFunc.getDate((String)resultData.get(Constants.POST_TIME)));
				}
			}
			// 部分页面的 时间、来源、编辑 是混在一块的，需要拆解
			if (resultData.containsKey(Constants.SOURCE)) {
				String source = (String)resultData.get(Constants.SOURCE);
				int indx = source.indexOf("来源：");
				if(indx >= 0) {
					String[] s = source.substring(indx+3).trim().split(" ");
					resultData.put(Constants.SOURCE, s[0]);
				}
			}
			if (resultData.containsKey(Constants.EDITOR)) {
				String editor = (String)resultData.get(Constants.EDITOR);
				int indx = editor.indexOf("编辑：");
				if(indx >= 0) {
					String[] s = editor.substring(indx+3).trim().split(" ");
					resultData.put(Constants.EDITOR, s[0]);
				}
			}
		}
		return new ReProcessResult(SUCCESS, processdata);
	}
}