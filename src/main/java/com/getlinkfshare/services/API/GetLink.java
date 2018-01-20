package com.getlinkfshare.services.API;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;

import com.getlinkfshare.services.API.Fshare;
import com.getlinkfshare.services.API.User;

@Path("getlinkfs")
public class GetLink {
	private static String email = "duongrom305@gmail.com";
	private static String password = "123456aA@";
	private static String loginUrl = "https://www.fshare.vn/login";
	private static String homeUrl = "https://www.fshare.vn";
	
	public JSONObject isFile(String strUrlObj) {
		JSONObject jsonUrl = new JSONObject(strUrlObj);
		String url = jsonUrl.getString("url");
		Pattern p = Pattern.compile("https://www.fshare.vn/(.*)/(.{12,15})");
		Matcher m = p.matcher(url);
		if(m.find()) {
			String fileType = m.group(1);
			JSONObject obj = new JSONObject();
			obj.put("type", fileType);
			obj.put("url", url);
			return obj;
		}else {
			JSONObject obj = new JSONObject();
			obj.put("type", "error");
			return obj;
		}
	}
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLinkFshare(String strUrlObj) {		
		//
		CookieManager cookie = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
		User user = new User(email,password);
		Fshare fshare = new Fshare();
		JSONObject data = new JSONObject();		
		//Check url
		JSONObject file = this.isFile(strUrlObj);
		//
		if(!((file.getString("type")).equals("error"))) {
			String fileType = file.getString("type");
			String fileUrl = file.getString("url");
			System.out.println("check OK");
			// login
			String fs_csrf = fshare.getFsCsrf(cookie, homeUrl);
			String loginData = user.getDataLogin(fs_csrf);
			boolean status = fshare.postLogin(cookie, loginData, loginUrl);
			if(status && fileType.equals("file")) {
				String dataJson = fshare.getLinkVipFile(cookie, fileUrl);
				if(dataJson != null) {
					data.put("url", fshare.findUrl(dataJson));				
					return Response.status(201).entity(data.toString()).build();
				}else {
					data.put("msg", "getlink fail");
					return Response.status(404).entity(data.toString()).build();
				}				
			}else if(status && fileType.equals("folder")) {
				JSONObject folder = fshare.findUrlFolder(fileUrl);
				int size = folder.length();
				for(int i = 1; i <= size; i++) {
					JSONObject tmp = new JSONObject();
					String dataJson = fshare.getLinkVipFile(cookie, folder.getString(""+i));
					if(dataJson != null) {
						tmp.put("url",fshare.findUrl(dataJson));
						data.put(""+i, tmp);
					}else {
						System.out.println(folder.getString(""+i));
					}
				}
				return Response.status(200).entity(data.toString()).build();
				
			}else {
				data.put("msg", "login fail");
				return Response.status(401).entity(data.toString()).build();
			}
			
		}else {
			data.put("msg", "file or folder does not existt");
			return Response.status(401).entity(data.toString()).build();
		}
	}
	
	
	@GET
	@Path("check/{code}")
	@Produces(MediaType.APPLICATION_JSON)
	public void check(@PathParam("code") String fileCode) {
		ArrayList<Pattern> patterns = new ArrayList <Pattern>();
		patterns.add(Pattern.compile("<title>Fshare - (.*)</title>"));
		patterns.add(Pattern.compile("<i class=\"fa fa-hdd-o\"></i>(.*)(.*)</div>"));
		BufferedReader buf = null;
		ArrayList <String> matches = new ArrayList <String> ();
		try {
			URL url = new URL ("https://www.fshare.vn/file/"+fileCode);
	        InputStream inputStream = (InputStream) url.getContent ();
	        InputStreamReader isr = new InputStreamReader (inputStream);
	        
	        buf = new BufferedReader (isr);
	        String str = null;
	        while ((str = buf.readLine ()) != null) 
	        {
	            for (Pattern p : patterns) 
	            {
	            	
	                Matcher m = p.matcher(str);
	                while (m.find ()) 
	                    matches.add(m.group(1));
	            }
	        }       
		} catch (Exception e) {}
		System.out.println(matches);
	}
}
