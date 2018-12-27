package com.fmi.mpr.hw.http;

import java.net.*;
import java.io.*;

public class HTTPServer {
	
	private ServerSocket serv;
	private boolean isRunning;
	
	
	public HTTPServer() throws IOException {
		this.serv = new ServerSocket(8080);
	}
	
	private void run() {	
		while(isRunning) {
			try {
				listen();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void start() {
		if(!isRunning) {
			this.isRunning = true;
			run();
		}
	}
	
	public void listen() throws IOException {
			Socket client = null;
			try {
				client = serv.accept();
				System.out.println(client.getInetAddress() + " connected!");
				processClient(client);
				System.out.println("Connection to " + client.getInetAddress() + " closed!");
			} finally {
				if (client != null) {
					client.close();
				}
			}
		}
	
	private void processClient(Socket client) throws IOException {
			
			try (BufferedInputStream br = new BufferedInputStream(client.getInputStream());
				 PrintStream ps = new PrintStream(client.getOutputStream(), true)) {
				
				String response = read(ps, br);
				write(ps, response);		
			}
		} 
	
	private String read(PrintStream ps, BufferedInputStream bis) throws IOException {
		
		if (bis != null) {
			StringBuilder request = new StringBuilder();
			
			byte[] buffer = new byte[1024];
			int bytesRead = 0;
			
			while ((bytesRead = bis.read(buffer, 0, 1024)) > 0) {
				request.append(new String(buffer, 0, bytesRead));
				
				if (bytesRead < 1024) {
					break;
				}
			}
			return parseRequest(ps, request.toString());
		}
		return "Error";
	}
	
	private void write(PrintStream ps, String res) {  //used in POST-method
		if(ps != null) {
			ps.println("HTTP/1.0 200 OK");
			ps.println();
				ps.println("<!DOCTYPE html>\n" + 
						   "<html>\n" + 
						   "<head>\n" + 
						   "	<title></title>\n" + 
						   "</head>\n" + 
						   "<body>\n" + 
						   "<form action=\"/action_page.php\">\n" + 
						   "			  <input type=\"file\" name=\"pic\" accept=\"image/*\">\n" + 
						   "			  <input type=\"submit\">\n" + 
						   "			</form> " +
						   "</body>\n" + 
						   "</html>");
		}
	}
	
	private String parseRequest(PrintStream ps, String request) throws IOException {
			
			System.out.println(request);
	
			String[] lines = request.split("\n");
			
			String firstHeader = lines[0];
			String requestType = firstHeader.split(" ")[0];
			String extension = firstHeader.split(" ")[1].split("\\.")[1];
			
			if(requestType.equals("GET")) {
				return getRequestMethod(ps, extension, request);
			}
			if(requestType.equals("POST")) {
				return postRequestMethod(ps, lines);
			}
			return null;
		}
	
	public String getRequestMethod(PrintStream ps, String ext, String req) throws IOException {
		String fileName = req.split(" ")[1].substring(1);
		ps.println("HTTP/1.1 200 OK");
		
		if(ext.equals("png") || ext.equals("jpg") || ext.equals("jpeg")) {
			try {
				ps.println();
			
				File file = new File(fileName);
				String path = file.getAbsolutePath();
				FileInputStream fis = new FileInputStream(path);
				
				sendPicture(fis, ps);
				
				fis.close();
			} catch(IOException e) {
				ps.println();
				ps.println("<!DOCTYPE html>\n" + 
						   "<html>\n" + 
						   "<head>\n" + 
						   "	<title></title>\n" + 
						   "</head>\n" + 
						   "<body>\n" + 
						   			"ERROR! Please, try again with different file." +
						   "</body>\n" + 
						   "</html>");
			}
		}
		
		if(ext.equals("mp4") || ext.equals("avi")) {
			try {
				ps.println("Content-Type: video/mp4");
				ps.println();
				
				File file = new File(fileName);
				String path = file.getAbsolutePath();
				FileInputStream fis = new FileInputStream(path);
				
				sendVideo(fis, ps);
				
				fis.close();
			} catch(IOException e) {
				ps.println();
				ps.println("<!DOCTYPE html>\n" + 
						   "<html>\n" + 
						   "<head>\n" + 
						   "	<title></title>\n" + 
						   "</head>\n" + 
						   "<body>\n" + 
						   			"ERROR! Please, try again with different file." +
						   "</body>\n" + 
						   "</html>");
			}
		}
		
		if(ext.equals("txt")) {
			try {
				ps.println();
				
				File file = new File(fileName);
				FileInputStream inFile = new FileInputStream(file);
				
				sendText(inFile, ps);
				
				inFile.close();
			} catch(IOException e) {
				ps.println();
				ps.println("<!DOCTYPE html>\n" + 
						   "<html>\n" + 
						   "<head>\n" + 
						   "	<title></title>\n" + 
						   "</head>\n" + 
						   "<body>\n" + 
						   			"ERROR! Please, try again with different file." +
						   "</body>\n" + 
						   "</html>");
			}
		}
		return null;
	}
	
	private void sendVideo(FileInputStream inFile, PrintStream ps) throws IOException {
		
		byte[] buffer = new byte[8192];
		
		int bytesRead = 0;
		while((bytesRead = inFile.read(buffer, 0, 8192)) > 0) {
			ps.write(buffer, 0, bytesRead);
		}
		ps.flush();
	}

	private void sendText(FileInputStream inFile, PrintStream ps) throws IOException {
		
		byte[] buffer = new byte[8192];
		
		int bytesRead = 0;
		while((bytesRead = inFile.read(buffer, 0, 8192)) > 0) {
			ps.write(buffer, 0, bytesRead);
		}
		ps.flush();
	}

	private void sendPicture(FileInputStream inFile, PrintStream ps) throws IOException {
	
		byte[] buffer = new byte[4096];
		
		int bytesRead = 0;
		while((bytesRead = inFile.read(buffer, 0, 4096)) > 0) {
			ps.write(buffer, 0, bytesRead);
		}
		ps.flush();
	}

	private String postRequestMethod(PrintStream ps, String[] lines)throws IOException {
		StringBuilder body = new StringBuilder();
		boolean readBody = false;
		for (String line : lines) {
			if (readBody) {
				body.append(line);
			}
			if (line.trim().isEmpty()) {
				readBody = true;
			}
		}
		return parseBody(body.toString());	
	}

//TODO
	private String parseBody(String body) {
		
		if (body != null && !body.trim().isEmpty()) {
			
		}
		return null;
}
	
	public static void main(String[] args) throws IOException {
		HTTPServer server = new HTTPServer();
		server.start();
	}
}
