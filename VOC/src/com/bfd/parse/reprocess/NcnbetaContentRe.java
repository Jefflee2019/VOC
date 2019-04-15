package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.Map;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.util.ParseUtils;

/**
 * 中文业界资讯站帖子页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class NcnbetaContentRe implements ReProcessor{

//	private static final Log LOG = LogFactory.getLog(NcnbetaContentRe.class);
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		if(resultData.containsKey("editor")){
			String editor = resultData.get("editor").toString();
			editor = editor.substring(editor.indexOf("编辑") + 3);
			editor = editor.replace("[", "")
					.replace("]", "")
					.replace(":", "")
					.replace("：", "");
			resultData.put("editor", editor.trim());
		}
		//稿源：xxx（文章中有些xxx是链接，有些不是），
		//避免某些不能取到链接的会丢失source,统一取稿源：xxx
		if(resultData.containsKey(Constants.SOURCE)){
			String source = resultData.get(Constants.SOURCE).toString();
			source = source.replace("稿源：", "").replace("稿源:", "");
			resultData.put(Constants.SOURCE, source.trim());
		}
		ParseUtils.getIid(unit, result);
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
}
