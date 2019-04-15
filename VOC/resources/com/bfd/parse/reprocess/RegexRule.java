package com.bfd.parse.reprocess;

import java.util.regex.Pattern;

public class RegexRule {

	public static final Pattern getnum = Pattern.compile("(\\d+)");
	public static final Pattern gettime = Pattern.compile("([0-9]{4}-[0-9]{1,2}-[0-9]{1,2})");
}
