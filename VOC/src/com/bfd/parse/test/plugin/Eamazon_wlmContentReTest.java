package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.Eamazon_wlmContentRe;
import com.bfd.parse.util.JsonUtil;

public class Eamazon_wlmContentReTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "https://www.amazon.cn/MEXICAN-%E7%A8%BB%E8%8D%89%E4%BA%BA-%E6%A0%BC%E7%BA%B9%E5%8A%A0%E7%BB%92%E8%A1%AC%E8%A1%AB%E7%94%B7%E9%95%BF%E8%A2%96%E8%A1%AC%E8%A1%A3-%E4%BC%91%E9%97%B2%E8%A1%AC%E8%A1%AB-%E7%94%B7%E5%A3%AB%E4%B8%8A%E8%A1%A3-%E5%8A%A0%E5%8E%9A%E4%BF%AE%E8%BA%AB%E8%A1%AC%E8%A1%A3%E7%94%B7%E8%A1%AC%E8%A1%AB-%E6%97%B6%E5%B0%9A%E6%89%93%E5%BA%95%E8%A1%AB/dp/B076BC21NP?ie=UTF8";
		// 动态数据
		// List<String> ajaxlist = new ArrayList<String>();
		// ajaxlist.add(null);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf8", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/Eamazon_wlm_con_9.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, null, new Eamazon_wlmContentRe(), tmplStr, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
