package com.linkto.main.core;

public class AccountInfo {
	public static class Resource {
		public int total;
		public int left;
	}

	public boolean enabled = false;

	public String name;
	public String balance;

	public String privateKey;
	public String publicKey;

	public Resource ram = new Resource();
	public Resource cpu = new Resource();
	public Resource net = new Resource();
}
