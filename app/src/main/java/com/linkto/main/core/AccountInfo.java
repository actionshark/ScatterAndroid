package com.linkto.main.core;

public class AccountInfo extends AccountThumb {
	public static class Resource {
		public int total;
		public int left;
	}

	public String privateKey;

	public String balance;

	public Resource ram = new Resource();
	public Resource cpu = new Resource();
	public Resource net = new Resource();
}
