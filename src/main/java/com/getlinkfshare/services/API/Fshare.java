package com.getlinkfshare.services.API;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONObject;

public class Fshare {
	
	
	public String getFsCsrf(CookieManager cookie, String url) {
		CookieHandler.setDefault(cookie);
		String fs_csrf = "";
		Pattern p = Pattern.compile("<input type=\"hidden\" value=\"(\\w*)\" name=\"fs_csrf\" />", Pattern.CASE_INSENSITIVE);
		Matcher m;		
		try {
			URL tmpUrl = new URL(url);
			InputStream inputStream = (InputStream) tmpUrl.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String tmp = "";
			while((tmp = br.readLine()) != null) {
				m = p.matcher(tmp);
				if(m.find()) {
					fs_csrf = m.group(1);
					break;
				}
			}			
		} catch (Exception e) {}
		return fs_csrf;
	}
	
	public boolean postLogin(CookieManager cookie, String loginData, String loginUrl) {
		CookieHandler.setDefault(cookie);
		boolean result = false;
		String tmp;
		Pattern p = Pattern.compile("<a (.*) title=\"(\\w*)\" data-toggle=\"dropdown\">(\\w*)</a>", Pattern.CASE_INSENSITIVE);
		Matcher m;
		try {
			// set header request
			URL url = new URL(loginUrl);
			HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
			conn.setDoOutput(true);
			conn.connect();
			// write data login and flush
			OutputStream os = conn.getOutputStream();
			os.write(loginData.getBytes());
			os.flush();
			os.close();
			// read data response
			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			while((tmp = br.readLine()) != null) {
				m = p.matcher(tmp);
				if(m.find()) {
					result = true;
					break;
				}
			}			
		} catch (Exception e) {}
		return result;
	}	
	
	public String getPage(CookieManager cookie, String pageUrl) {
	
		CookieHandler.setDefault(cookie);
		String result = "";
		String tmp;				
		try {
			URL url = new URL(pageUrl);
			url.openConnection();
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			while((tmp = br.readLine()) != null) {
				result = result.concat(tmp);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return result;
		
	}
	
	
	public String getLinkVipFile(CookieManager cookie,String fileUrl) {
		CookieHandler.setDefault(cookie);
		String result = "";
		String linkCode = "";
		BufferedReader br;
		String fs_csrf = this.getFsCsrf(cookie, fileUrl);
		Pattern p = Pattern.compile("https://www.fshare.vn/file/(\\w*)");
		Matcher m = p.matcher(fileUrl);
		if(m.find()) {
			linkCode = m.group(1);
			String postData = "fs_csrf="
					+ fs_csrf
					+ "&DownloadForm%5Bpwd%5D=&DownloadForm%5Blinkcode%5D="
					+ linkCode
					+ "&ajax=download-form&undefined=undefined";
			try {
				HttpsURLConnection conn = (HttpsURLConnection) new URL("https://www.fshare.vn/download/get").openConnection();
				
				conn.setRequestMethod("POST");
				conn.setRequestProperty("User-Agent", "	Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:56.0) Gecko/20100101 Firefox/56.0");
				conn.setDoOutput(true);
				conn.connect();
				
				OutputStream os =  conn.getOutputStream();
				os.write(postData.getBytes());
				os.flush();
				os.close();
				String tmp;
				br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				while((tmp = br.readLine()) != null) {
					result = result.concat(tmp);
				}
			} catch (Exception e) {}
		}		
		return result;
	}
	
	public String findUrl(String dataJson) {
		JSONObject obj = new JSONObject(dataJson);
		return obj.getString("url");
	}

	public int findQuantityFolder(String folderUrl) {
		String strQuantity = "";
		int quantity = 0;
		Matcher m;
		Pattern p = Pattern.compile("S(.*)g: (\\d*)(.*)</div>",Pattern.CASE_INSENSITIVE);
		
		try {
			URL tmpUrl = new URL(folderUrl);
			InputStream inputStream = (InputStream) tmpUrl.getContent();
			BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
			String tmp = "";
			while((tmp = br.readLine()) != null) {
				m = p.matcher(tmp);
				if(m.find()) {
					strQuantity = m.group(2);
					quantity = Integer.parseInt(strQuantity);
					break;
				}
			}
		} catch (Exception e) {}
		return quantity;
	}
	
	public JSONObject findUrlFolder(String folderUrl) {
		JSONObject obj = new JSONObject();
		Matcher m;
		int count = 1;
		int quantity = this.findQuantityFolder(folderUrl);
		String tmp = "";
		BufferedReader br = null;
		String str = "<div class=\"pull-left file_name\">(.*) data-id=\"(\\w*)\"(.*)</div>";
		Pattern p = Pattern.compile(str, Pattern.CASE_INSENSITIVE);
		if(quantity != 0) {
			try {
				URL tmpUrl = new URL(folderUrl);
				InputStream inputStream = (InputStream) tmpUrl.getContent();
				br = new BufferedReader(new InputStreamReader(inputStream));			
				while((tmp = br.readLine()) != null) {
					m = p.matcher(tmp);
					if(m.find()) {
						obj.put(""+count, "https://www.fshare.vn/file/"+m.group(2));
						count++;
						if(count > quantity) {
							break;
						}
					}
				}
			} catch (Exception e) {}
		}
		return obj;
	}
}
