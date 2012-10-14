package baijie.test;

import java.net.MalformedURLException;

import util.GetterInterface;
import util.webpage.Post;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.MyHessianURLConnectionFactory;

public class TestHessianGetter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://baijie1991-hrd.appspot.com/getter";
//		String url = "http://localhost:8888/getter";
		
		HessianProxyFactory factory = new HessianProxyFactory();
		MyHessianURLConnectionFactory mHessianURLConnectionFactory =
				new MyHessianURLConnectionFactory();
		mHessianURLConnectionFactory.setHessianProxyFactory(factory);
		factory.setConnectionFactory(mHessianURLConnectionFactory);
		GetterInterface getter;
		try {
			getter = (GetterInterface) factory.create(GetterInterface.class, url);
			System.out.println(getter.echo("Low Low Low.中文哦，hi哈"));
			System.out.println(getter.getPosts(Post.convertToDate(2012, 10, 1), null, -1));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
	}
	
}
