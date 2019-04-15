package com.bfd.parse.reprocess;

import java.util.Map;

public class ReProcessResult {

	public static final int SUCCESS = 0;
	public static final int FAILED = 1;
//	public static final int PARSE_FAILED = 2;
	public static final int NONESAVE = 3;
	public static final int OFF = 4;

	private int processcode;
	private Map<String, Object> data;

	public ReProcessResult() {
	}

	public ReProcessResult(int processcode, Map<String, Object> data) {
		this.processcode = processcode;
		this.data = data;
	}

	public Map<String, Object> getData() {
		return data;
	}

	public void setData(Map<String, Object> data) {
		this.data = data;
	}

	public int getProcesscode() {
		return processcode;
	}

	public void setProcesscode(int processcode) {
		this.processcode = processcode;
	}
}
