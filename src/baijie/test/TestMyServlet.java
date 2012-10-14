package baijie.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class TestMyServlet {

	public TestMyServlet() {
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "203.208.46.2";
		int port = 80;
//		String host = "localhost";
//		int port = 8888;
		try {
			Socket socket = new Socket(host, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			StringBuffer sb = new StringBuffer();
			String data = "Hello from 杰!^_^";
			sb.append("\r\n");
			sb.append("POST /myservlet HTTP/1.1\r\n");
			sb.append("Host: baijie1991-hrd.appspot.com\r\n");
			sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
			sb.append("Content-Length: "+(data.getBytes("UTF-8").length));
//			sb.append("Connection: Keep-Alive\r\n");
			sb.append("\r\n\r\n");
			sb.append(data);
			
			out.write(sb.toString());
			out.flush();
			System.out.println(sb.toString());
			System.err.println("\nstart read.");
			String aLine = null;
			do{
				aLine = in.readLine();
				System.out.println(aLine);
			}while(aLine!=null);
//			int contentLength = -1;
//			do{
//				aLine = in.readLine();
//				System.out.println(aLine);
//				if(aLine.matches("^Content-Length: *\\d+$"))
//					contentLength = Integer.parseInt(aLine.substring(15).trim());
//			}while(!aLine.equals(""));
//			System.out.println("contentLength="+contentLength);
//			char[] buffer = new char[contentLength];
//			in.read(buffer, 0, contentLength);
//			System.out.println(buffer);
			System.err.println("end read.");
			in.close();
			socket.close();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
