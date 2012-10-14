package baijie.test;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import util.GetterInterface;
import util.webpage.Post;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.client.MyHessianURLConnectionFactory;

public class TestHessianGetter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://baijie1991-hrd.appspot.com/getter";
		int maxAttempts = 7;
		int timeout = 2000;
//		String url = "http://localhost:8888/getter";
		
		HessianProxyFactory factory = new HessianProxyFactory();
		MyHessianURLConnectionFactory mHessianURLConnectionFactory =
				new MyHessianURLConnectionFactory();
		mHessianURLConnectionFactory.setHessianProxyFactory(factory);
		factory.setConnectionFactory(mHessianURLConnectionFactory);
		factory.setConnectTimeout(timeout);
		GetterInterface getter;
		for(int counter = 1;counter <= maxAttempts;counter++){
			System.out.println("网络更新第"+counter+"次尝试");
			try {
				getter = (GetterInterface) factory.create(GetterInterface.class, url);
				System.out.println(getter.echo("Low Low Low.中文哦，hi哈"));
				System.out.println(getter.getPosts(Post.convertToDate(2012, 10, 1), null, -1));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (HessianRuntimeException e){
				if(e.getCause() instanceof SocketTimeoutException)
					continue;
				else
					e.printStackTrace();
			}
			break;
		}
		System.out.println("Over");
	}
	
}
