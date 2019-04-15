package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.entity.Constants;
import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.ReProcessResult;
import com.bfd.parse.reprocess.ReProcessor;
import com.bfd.parse.util.ParseUtils;

/**
 * 站点名：牛车网
 * 功能：处理作者字段
 * @author bfd01
 *
 */
public class NniucheContentRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NniucheContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (!resultData.isEmpty() && resultData.size() > 0) {
				if (resultData.containsKey(Constants.AUTHOR)) {
					String author = resultData.get(Constants.AUTHOR).toString();
					author = author.replace("@", "");
					resultData.put(Constants.AUTHOR, author);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.info( this.getClass().getName() + "reprocess exception...");
			processcode = 1;
		}
		ParseUtils.getIid(unit, result);
		// 解析结果返回值 0代表成功
		return new ReProcessResult(processcode, processdata);
	}
}
