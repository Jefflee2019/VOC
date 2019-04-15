package com.bfd.parse.test.plugin;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.bfd.parse.ParserFace;
import com.bfd.parse.facade.parseunit.ParseUnit;
import com.bfd.parse.reprocess.EamazonCommentRe;
import com.bfd.parse.util.JsonUtil;

public class EamazonCommentReTest {
	public static void main(String[] args) throws Exception {
		testDemo();
	}

	public static void testDemo() throws Exception {
		// 要抓取链接
		String url1 = "https://www.amazon.cn/Apple-iPhone-X-%E5%85%A8%E7%BD%91%E9%80%9A4G%E6%99%BA%E8%83%BD%E6%89%8B%E6%9C%BA-256GB-%E6%B7%B1%E7%A9%BA%E7%81%B0%E8%89%B2-%E9%A1%BA%E4%B8%B0%E5%8F%91%E8%B4%A7-%E5%8F%AF%E5%BC%80%E4%B8%93%E7%A5%A8/product-reviews/B0763KX27G/ref=cm_cr_dp_d_show_all_top?ie=UTF8&reviewerType=all_reviews";
		// 动态数据
//		 List<String> ajaxlist = new ArrayList<String>();
//		 ajaxlist.add(url1);
		// 生成解析使用的bean
		Map<String, Object> map = CreateParseUnit.createMap("test", "test", 100, 100, url1, "utf8", null, null);
		// 生成解析需要结构
		ParseUnit unit = ParseUnit.fromMap(map, System.currentTimeMillis());
		// 模板
		String tmplStr = FileUtils.readFileToString(new File("tmpl/EamazonCommentRe.txt"));
		// 解析输出字段
		String outputFiled = "";
		ParserFace face = new ParserFace("test");
		// 执行解析流程
		// 参数分别是 预处理插件,json插件,后处理插件,模板,ParseFace,输出字段
		ParsePlugin testPlugin = new ParsePlugin(null, null, new EamazonCommentRe(), tmplStr, face, outputFiled);
		// ParsePlugin testPlugin = new ParsePlugin(null, null, null, tmplStr,
		// face, outputFiled);
		// 输出结果
		System.out.println(JsonUtil.toJSONString(testPlugin.parse(unit)));
	}
}
