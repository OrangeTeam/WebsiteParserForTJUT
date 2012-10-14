package baijie.test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Scanner;

public class TestMyServlet2 {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("203.208.46.2", 80));
			URL url = new URL("http://baijie1991-hrd.appspot.com/myservlet");
			URLConnection con = url.openConnection(proxy);
			con.connect();
			Scanner scan = new Scanner( con.getInputStream() );
			while(scan.hasNext())
				System.out.println(scan.next());
			
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
