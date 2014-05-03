package org.orange.parser.parser;

/**
 * 解析监听器适配器，若无说明，默认方法是空方法。您可以继承此类，只重写需要定义的方法。<br />
 * <strong>注：</strong>本适配器的onError调用System.err.println返回错误信息，方便调试。您可以重写此方法（如用空方法替代）<br />
 * <strong>注：</strong>本适配器的clone()方法仅调用默认的super.clone()方法，如果您有非安全对象字段，这并不适合您，请重写clone
 * @author Bai Jie
 */
public class ParseAdapter implements ParseListener {
    public ParseAdapter() {
        super();
    }
    @Override
    public void onError(int code, String message) {
        System.err.println("Error "+code+": "+message);
    }
    @Override
    public void onWarn(int code, String message) {
    }
    @Override
    public void onInformation(int code, String message) {
    }
    @Override
    public void onProgressChange(int current, int total) {
    }
    @Override
    public ParseListener clone() throws CloneNotSupportedException{
        return (ParseListener) super.clone();
    }
}
