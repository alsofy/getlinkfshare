package com.getlinkfshare.services.API;

import java.net.URLEncoder;

public class User {
	private String email;
	private String password;
	
	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}
	
	@SuppressWarnings("deprecation")
	public String getDataLogin(String fs_csrf) {
		String data = "fs_csrf="
				+ fs_csrf
				+ "&LoginForm"
				+ URLEncoder.encode("[email]")
				+ "="
				+ URLEncoder.encode(this.email)
				+ "&LoginForm"
				+ URLEncoder.encode("[password]")
				+ "="
				+ URLEncoder.encode(this.password)
				+ "&LoginForm"
				+ URLEncoder.encode("[checkloginpopup]")
				+ "=0&LoginForm"
				+ URLEncoder.encode("[rememberMe]")
				+ "=0&yt0="
				+ URLEncoder.encode("Đăng nhập");
		return data;
	}
}
