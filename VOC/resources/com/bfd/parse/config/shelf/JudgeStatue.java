package com.bfd.parse.config.shelf;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bfd.crawler.utils.FileUtil;
import com.bfd.parse.util.JsonUtil;

/**
 * 判定指定url商品是否在架
 * 
 * @author yanhui.ji
 */
public class JudgeStatue {

	private static final Log log = LogFactory.getLog(JudgeStatue.class);

	public static String judge(String url, String page, JudgeRule rule) {
		List<JudgeRule> rules = new ArrayList<JudgeRule>();
		rules.add(rule);
		return judgeStatus(url, page, rules);
	}

	/**
	 * 判定指定商家url产品的状态 上架 on 下架 off 即将开售 ready 赠品 gift
	 * 
	 * @param url
	 * @param page
	 * @param rules
	 * @return
	 */
	public static String judgeStatus(String url, String page, List<JudgeRule> rules) {
		if (page == null || page.length() == 0 || rules == null || rules.size() == 0) {
			log.info("when judge status, page or rules is null, ");
			return null;
		}
//		log.info("rules is :"+JsonUtil.toJSONString(rules));
		for (int i = 0, size = rules.size(); i < size; i++) {
			JudgeRule rule = (JudgeRule) rules.get(i);
			if (rule.getRegex() == null)
				continue;
			if (rule.getType().equals("1")) {// 通过html标签判定

			} else if (rule.getType().equals("2")) { // 通过正则判定
				String regexV = matchRegex(page, rule.getRegex());
				if (regexV == null) {
					continue;
				} else {
					if (StringUtils.isEmpty(rule.getExtend())) {
						return rule.getStatus();
					} else {
						if (regexV.equals(rule.getExtend())) {
							return rule.getStatus();
						} else if (Integer.parseInt(regexV) > 0) {
							return "on";
						} else if (Integer.parseInt(regexV) <= 0) {
							return "off";
						}
					}
				}
			} else if (rule.getType().equals("21")) {// 通过一个字符匹配判断出上架下架
				String regexV = matchRegex(page, rule.getRegex());
				if (regexV == null) {
					return rule.getExtend();
				} else {
					return rule.getStatus();
				}
			} else if (rule.getType().equals("22")) {// 获取指定字符与getExtend()的值相比，相等的情况下返回其状态
				String regexV = matchRegex(page, rule.getRegex());
				if (regexV != null) {
					if (regexV.equals(rule.getExtend())) {
						return rule.getStatus();
					}
				}
			} else if (rule.getType().equals("3")) {

			} else if (rule.getType().equals("4")) {
				if (page.trim().length() == 0)
					return rule.getStatus();
			}
		}
		return null;
	}

	public static String matchRegex(String content, String regex) {
		String target = null;
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(content);
		if (m.find()) {
			target = m.group(1);
		}
		return target;
	}

	public static void main(String[] args) throws Exception {
		String str = FileUtil.readFromFile2Str("/home/ian/test");
		String ss = "(该商品不存在或者已下架)</html>";

		// JudgeRule rule = new JudgeRule();
		// rule.setRegex(str);
		// List<String> list = new ArrayList<String>();
		// list.add(str);
	}
}