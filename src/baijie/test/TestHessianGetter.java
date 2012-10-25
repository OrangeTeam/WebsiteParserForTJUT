package baijie.test;

import java.net.MalformedURLException;
import java.net.SocketTimeoutException;

import util.GetterInterface;
import util.webpage.Post;

import com.caucho.hessian.client.HessianProxyFactory;
import com.caucho.hessian.client.HessianRuntimeException;
import com.caucho.hessian.client.MyHessianSocketConnectionFactory;

public class TestHessianGetter {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String url = "http://schoolwebpageparser.appspot.com/getter";
		int maxAttempts = 10;
		int timeout = 1000;
//		String url = "http://localhost:8888/getter";
		
		HessianProxyFactory factory = new HessianProxyFactory();
		MyHessianSocketConnectionFactory mHessianSocketConnectionFactory =
				new MyHessianSocketConnectionFactory();
		mHessianSocketConnectionFactory.setHessianProxyFactory(factory);
		factory.setConnectionFactory(mHessianSocketConnectionFactory);

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
			} catch (Exception e){
				if(e.getCause() instanceof SocketTimeoutException){
					System.out.println("SocketTimeoutException: "+e.getMessage());
					continue;
				}
				else
					e.printStackTrace();
			}
			break;
		}
		System.out.println("Over");
	}
	
}
