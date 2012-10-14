package baijie.test;

import java.net.MalformedURLException;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.MyHessianURLConnectionFactory;

public class TestHessian {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://baijie1991-hrd.appspot.com/testhessian";
//		String url = "http://localhost:8888/testhessian";

//		System.setProperty("http.proxyHost", "127.0.0.1");
//		System.setProperty("http.proxyPort", "8087");
		
		HessianProxyFactory factory = new HessianProxyFactory();
		MyHessianURLConnectionFactory mHessianURLConnectionFactory =
				new MyHessianURLConnectionFactory();
		mHessianURLConnectionFactory.setHessianProxyFactory(factory);
		factory.setConnectionFactory(mHessianURLConnectionFactory);
		BasicAPI basic;
		try {
			basic = (BasicAPI) factory.create(BasicAPI.class, url);
			basic.setGreeting("Low Low Low.中文哦，hi哈");
			System.out.println("hello(): " + basic.hello());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private interface BasicAPI {
		public void setGreeting(String greeting);
		public String hello();
	}

}
