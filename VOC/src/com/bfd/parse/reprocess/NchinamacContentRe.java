package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * @site:苹果在线
 * @function 处理编辑字段
 * 
 * @author bfd_01
 */

public class NchinamacContentRe implements ReProcessor {
	private static final Log LOG = LogFactory.getLog(NchinamacContentRe.class);

	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result, ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = new HashMap<String, Object>();

		// 解析数据
		resultData = result.getParsedata().getData();
		if (resultData == null) {
			LOG.warn("未找到解析数据");
			return null;
		}

		/**
		 * @param editor
		 * @function 清洗字段
		 *  
		 */

		if (resultData.containsKey(Constants.EDITOR)) {
			String editor = resultData.get(Constants.EDITOR).toString();
			if (editor.contains("编辑：")) {
				int index = editor.indexOf("编辑：");
				editor = editor.substring(index+3);
			}
			resultData.put(Constants.EDITOR, editor);
		}

		ParseUtils.getIid(unit, result);
		return new ReProcessResult(SUCCESS, processdata);
	}
}