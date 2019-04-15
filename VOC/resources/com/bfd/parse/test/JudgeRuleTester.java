package com.bfd.parse.test;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.parse.config.shelf.JudgeRule;
import com.bfd.parse.config.shelf.JudgeStatue;
import com.bfd.parse.util.JsonUtil;

public class JudgeRuleTester implements Tester {

	private static final Log LOG = LogFactory.getLog(JudgeRuleTester.class);

	@Override
	public TestResponse test(TestRequest request) {
		LOG.info("execute judgeruletester");
		Map<String, Object> reqMap = request.getReqMap();
		if (reqMap.containsKey("rule") && StringUtils.isNotEmpty(request.getUrl())) {
			JudgeRule rule = JudgeRule.create((Map) reqMap.get("rule"));
			String status = JudgeStatue.judge(request.getUrl(), request.getData(), rule);
			return TestResponse.create(0).put("onshelf", status);
		}
		LOG.info("Invalid data, rule or url is empty.");
		return TestResponse.create(1);
	}
}
