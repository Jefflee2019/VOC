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
 * 站点名：太和网
 * 功能：清洗字段
 * @author bfd01
 *
 */
public class NtaiheContentRe implements ReProcessor {

	private static final Log log = LogFactory.getLog(NtaiheContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace parseFace) {
		int processcode = 0;
		
		Map<String, Object> processdata = new HashMap<String, Object>();
		// 得到模板解析结果
		Map<String, Object> resultData = result.getParsedata().getData();
		try {
			if (!resultData.isEmpty() && resultData.size() > 0) {
				String[] tem = null;
				if (resultData.containsKey(Constants.AUTHOR)) {
					String temp = resultData.get(Constants.AUTHOR).toString();
					tem = temp.split(" ");
				}
				if (4 == tem.length) {
					String author = tem[0].replace("作者：", "");
					String source = tem[3].replace("来源：", "");
					String post_time = tem[1].replace("时间：", "");
					resultData.put(Constants.AUTHOR, author.trim());
					resultData.put(Constants.SOURCE, source);
					resultData.put(Constants.POST_TIME, post_time);
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
