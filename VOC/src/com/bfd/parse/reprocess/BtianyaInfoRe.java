package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 站点名：天涯
 * 
 * 功能：标准化论坛用户页
 * 
 * @author bfd_06
 */
public class BtianyaInfoRe implements ReProcessor {

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();

		// 问答用户页
		if (!resultData.containsKey(Constants.LOGIN_CNT)) {
			// personal_signature
			formatStr(resultData, unit,
					resultData.get(Constants.PERSONAL_SIGNATURE).toString(),
					Constants.PERSONAL_SIGNATURE);
			return new ReProcessResult(SUCCESS, processdata);
		}

		// LOGIN_CNT
		formatStr(resultData, unit, resultData.get(Constants.LOGIN_CNT)
				.toString(), Constants.LOGIN_CNT);
		// REG_TIME
		formatStr(resultData, unit, resultData.get(Constants.REG_TIME)
				.toString(), Constants.REG_TIME);
		// FORUM_SCORE
		formatStr(resultData, unit, resultData.get(Constants.FORUM_SCORE)
				.toString(), Constants.FORUM_SCORE);
		// LASTLOGIN_TIME
		formatStr(resultData, unit, resultData.get(Constants.LASTLOGIN_TIME)
				.toString(), Constants.LASTLOGIN_TIME);

		return new ReProcessResult(SUCCESS, processdata);
	}

	public void formatStr(Map<String, Object> resultData, ParseUnit unit,
			String str, String keyName) {
		if (keyName.equals(Constants.LOGIN_CNT)) {
			resultData.put(keyName, str.substring(str.indexOf("数") + 1));
		} else if (keyName.equals(Constants.REG_TIME)) {
			resultData.put(keyName, str.substring(str.indexOf("期") + 1));
		} else if (keyName.equals(Constants.FORUM_SCORE)) {
			resultData.put(keyName, str.substring(str.indexOf("分") + 1));
		} else if (keyName.equals(Constants.LASTLOGIN_TIME)) {
			resultData.put(keyName, str.substring(str.indexOf("录") + 1));
		} else if (keyName.equals(Constants.PERSONAL_SIGNATURE)) {
			resultData.put(keyName, str.substring(str.indexOf(' ') + 1));
		}
	}
}
