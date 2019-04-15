package com.bfd.parse.test.plugin;


public class DownloaderParam {
		private String url = "";
		private String data = "";
		private String charset = "";

		public DownloaderParam(String url, String data, String charset) {
			super();
			this.url = url;
			this.data = data;
			this.charset = charset;
		}

		public String getUrl() {
			return url;
		}

		public void setUrl(String url) {
			this.url = url;
		}

		public String getData() {
			return data;
		}

		public void setData(String data) {
			this.data = data;
		}

		public String getCharset() {
			return charset;
		}

		public void setCharset(String charset) {
			this.charset = charset;
		}
}
