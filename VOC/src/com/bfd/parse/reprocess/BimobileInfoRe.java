package com.bfd.parse.reprocess;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.bfd.parse.ParseResult;
import com.bfd.parse.ParserFace;
import com.bfd.parse.entity.Constants;
import com.bfd.parse.facade.parseunit.ParseUnit;

/**
 * 手机之家论坛用户页 
 * 后处理插件
 * @author bfd_05
 *
 */
public class BimobileInfoRe implements ReProcessor{

//	private static final Log LOG = LogFactory.getLog(BimobileInfoRe.class);
	private static final Pattern pNum = Pattern.compile("\\d+");
	private static final Pattern patTime = Pattern.compile("(?<=\\D)[0-9]{4}.[0-9]{1,2}.[0-9]{1,2}.*[0-9]{2}:[0-9]{2}(?=\\b)");
	
	@SuppressWarnings("unchecked")
	@Override
	public ReProcessResult process(ParseUnit unit, ParseResult result,
			ParserFace arg2) {
		Map<String, Object> processdata = new HashMap<String, Object>();
		Map<String, Object> resultData = result.getParsedata().getData();
		
		if(resultData.containsKey(Constants.ONLINE_HOUR)){//在先小时数
			parseByReg(resultData, Constants.ONLINE_HOUR, pNum);
		}
		if(resultData.containsKey(Constants.USER_GROUP)){
			String userGroup = ((List<Object>)resultData.get(Constants.USER_GROUP)).get(0).toString();
			resultData.put(Constants.USER_GROUP, userGroup);
		}
		if(resultData.containsKey(Constants.REPLY_CNT)){//回复数
			parseByReg(resultData, Constants.REPLY_CNT, pNum);
		}
		if(resultData.containsKey(Constants.REG_TIME)){//注册时间
			parseByReg(resultData, Constants.REG_TIME, patTime);
		}
		if(resultData.containsKey(Constants.FORUM_SCORE)){//论坛积分
			parseByReg(resultData, Constants.FORUM_SCORE, pNum);
		}
		if(resultData.containsKey("miid")){//用户唯一标识
			parseByReg(resultData, "miid", pNum);
		}
		if(resultData.containsKey(Constants.USERID)){//用户id
			parseByReg(resultData, Constants.USERID, pNum);
		}
		if(resultData.containsKey(Constants.TOPICCNT)){//主题数
			parseByReg(resultData, Constants.TOPICCNT, pNum);
		}
		if(resultData.containsKey(Constants.USERNAME_INFO)){//用户名
			String userName = (String) resultData.get(Constants.USERNAME_INFO);
			userName = userName.substring(0, userName.indexOf("("));
			resultData.put(Constants.USERNAME_INFO, userName);
		}
		if(resultData.containsKey(Constants.CONTRIBUTE_CNT)){//贡献值
			parseByReg(resultData, Constants.CONTRIBUTE_CNT, pNum);
		}
		if(resultData.containsKey(Constants.LASTLOGIN_TIME)){//最后访问时间
			parseByReg(resultData, Constants.LASTLOGIN_TIME, patTime);
		}
		if(resultData.containsKey(Constants.GOODFRIEND_NUM)){//好友数
			parseByReg(resultData, Constants.GOODFRIEND_NUM, pNum);
		}
//		LOG.info("url:" + unit.getUrl() + ".after reprocess rs is "
//				+ JsonUtil.toJSONString(resultData));
		return new ReProcessResult(SUCCESS, processdata);
	}
	
	public void parseByReg(Map<String, Object> resultData, String conststr, Pattern p){
		String  resultStr = (String) resultData.get(conststr);
		Matcher mch = p.matcher(resultStr);
		if(mch.find()){
			resultStr = mch.group(0);
		}
		resultData.put(conststr, resultStr);
	}
}
