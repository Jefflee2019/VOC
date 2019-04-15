package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.Ejd_wlmListRe;
import com.bfd.parse.util.JsonUtil;

public class Ejd_wlmListReTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "https://search.jd.com/Search?keyword=%E7%AB%A5%E4%B9%90%E6%82%A0%E6%82%A0+%E7%9B%8A%E6%99%BA%E8%B6%A3%E5%91%B3%E7%B3%BB%E5%88%97%E7%AB%A5%E8%B6%A3%E9%92%93%E9%B1%BC+11%E4%B8%AA%E9%85%8D%E4%BB%B6&enc=utf-8&page=1&qrst=1&psort=4";
		// 动态数据
		// List<String> ajaxlist = new ArrayList<String>();
		// ajaxlist.add(null);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf8", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/Ejd_wlm_list_1.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, null, new Ejd_wlmListRe(), tmplStr, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
