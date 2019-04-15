package com.bfd.parse.test.plugin;

import java.util.Map;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.preprocess.EwartmFlagship_wlmListPre;
import com.bfd.parse.util.JsonUtil;

public class EwartmFlagship_wlmListPreTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "https://mall.jd.com/advance_search-674031-663284-659016-0-0-0-1-1-60.html?other=";
		// 动态数据
		// List<String> ajaxlist = new ArrayList<String>();
		// ajaxlist.add(null);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf8", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
//		String tmplStr = FileUtils.readFileToString(new File("tmpl/Efeiniu_wlm_list_10.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(new EwartmFlagship_wlmListPre(), null, null, null, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
