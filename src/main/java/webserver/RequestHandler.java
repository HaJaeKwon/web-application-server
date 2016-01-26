package webserver;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import db.DataBase;
import model.User;
import util.HttpRequestUtils;
import util.IOUtils;

public class RequestHandler extends Thread {
	private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);
	
	private Socket connection;
	
	public static List<User> userList= new ArrayList<User>();

	public RequestHandler(Socket connectionSocket) {
		this.connection = connectionSocket;
	}

	public void run() {
		log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(), connection.getPort());
		
		try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {
			// TODO 사용자 요청에 대한 처리는 이 곳에 구현하면 된다.
			
			DataOutputStream dos = new DataOutputStream(out);
			byte[] body;
			
			BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
			
			int content_length=0;
			String statusCode = "200 OK";
			boolean login = false;
			
			String line = br.readLine();
			log.debug("header : {}", line);
			String[] tokens = line.split(" ");
			log.debug("tokens[0] : {}", tokens[0]);
			log.debug("tokens[1] : {}", tokens[1]);
			while(!line.equals("")) {
				
				if(line.contains("Length")) content_length = Integer.parseInt(line.split(" ")[1]);
				
				
				line = br.readLine();
				log.debug("header : {}", line);
			}
			
			String url = getDefaultUrl(tokens);
			log.debug("url : {}", url);
			

			if(tokens[0].contains("GET") && url.contains("create")) {
				log.debug("GET");
				
				url = url.split("\\?")[1];
				User user = getUserInUrl(url);
				DataBase.addUser(user);
				userList.add(user);
				
				printUserlist();
				
				url = "/index.html";
				
			} else if(tokens[0].contains("POST")) {
				log.debug("POST");
				
				url = URLDecoder.decode(IOUtils.readData(br, content_length), "UTF-8");
				User user = getUserInUrl(url);
				DataBase.addUser(user);
				userList.add(user);
				
				printUserlist();
				
				response302Header(dos);
				return;
				
			} else if(tokens[0].contains("GET") && url.contains("login?")) {
				url = url.split("\\?")[1];
				log.debug("url login : {}", URLDecoder.decode(url, "UTF-8"));
				Map<String, String> loginInfo = HttpRequestUtils.parseQueryString(URLDecoder.decode(url, "UTF-8"));
				log.debug("LoginInfo : {}", loginInfo.get("userId"));
				User user = DataBase.findUserById(loginInfo.get("userId"));
				if(user != null) {
					if(loginInfo.get("password").equals(user.getPassword())) {
						login = true;
						log.debug("    [LOGIN]");
						response302Header_loginSuccess(dos);
						return;
					} else log.debug("    [INCORRECT PASSWORD]");
				}  else log.debug("    [INCORRECT ID]");

				response302Header_loginfail(dos);
				return;
			}
			
			body = Files.readAllBytes(new File("./webapp" + url).toPath());
			response200Header(dos, body.length);
			responseBody(dos, body);
			
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
	}

	private void printUserlist() {
		//Map<String, User> List = DataBase.findAll();
		for(int i=0; i<DataBase.findAll().size(); i++) {
			log.debug("[UserList] userId : {}, password : {}, name : {}, email : {}", userList.get(i).getUserId(), userList.get(i).getPassword(), userList.get(i).getName(), userList.get(i).getEmail());
		}
	}

	private User getUserInUrl(String url) throws UnsupportedEncodingException {
		log.debug("create? : {}", URLDecoder.decode(url, "UTF-8"));
		Map<String, String> userProfile = HttpRequestUtils.parseQueryString(URLDecoder.decode(url, "UTF-8"));
		User user = new User(userProfile.get("userId"), userProfile.get("password"), userProfile.get("name"), userProfile.get("email"));
		return user;
	}

	private String getDefaultUrl(String[] tokens) {
		String url = tokens[1];
		if(url.equals("/")) url = "/index.html";
		return url;
	}

	private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
		try {
			dos.writeBytes("HTTP/1.1 200 OK \r\n");
			dos.writeBytes("Content-Type: text/css;charset=utf-8\r\n");
			dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
			dos.writeBytes("\r\n");
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void response302Header(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void response302Header_loginSuccess(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: /index.html \r\n");
			dos.writeBytes("Set-Cookie: logined=true \r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	private void response302Header_loginfail(DataOutputStream dos) {
		try {
			dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
			dos.writeBytes("Location: /login.html \r\n");
			dos.writeBytes("Set-Cookie: logined=false \r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
	
	private void responseBody(DataOutputStream dos, byte[] body) {
		try {
			dos.write(body, 0, body.length);
			dos.writeBytes("\r\n");
			dos.flush();
		} catch (IOException e) {
			log.error(e.getMessage());
		}
	}
}
