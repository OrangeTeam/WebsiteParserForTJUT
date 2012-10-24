package baijie.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
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
		String host = "74.125.128.113";
		int port = 80;
//		String host = "localhost";
//		int port = 8888;
		try {
			Socket socket = new Socket(host, port);
			System.out.println(socket);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			OutputStreamWriter out = new OutputStreamWriter(socket.getOutputStream());
			StringBuffer sb = new StringBuffer();
			String data = "Hello from 杰!^_^";
			sb.append("\r\n");
			sb.append("GET /fetch.py HTTP/1.1\r\n");
			sb.append("Host: ilagoagent.appspot.com\r\n");
//			sb.append("Content-Type: text/plain; charset=UTF-8\r\n");
//			sb.append("Content-Length: "+(data.getBytes("UTF-8").length)+"\r\n");
			sb.append("Connection: close\r\n");
			sb.append("\r\n");
//			sb.append(data);
			
			sb.trimToSize();
			out.write(sb.toString());
			out.flush();
			System.out.print(sb.toString());
			System.out.println("-------------start read----------------");
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
			System.out.println("----------------end read----------------");
			out.close();
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
