package org.orange.parser.parser;

/**
* Created by BaiJie on 2014/4/11.
*/
public class ParseException extends Exception{
	private static final long serialVersionUID = 3737828070910029299L;
	public ParseException(String message){
		super(message + " @SchoolWebpageParser");
	}
	public ParseException(){
		super("encounter Exception when parse school page.");
	}
	public ParseException(String message, Throwable cause) {
		super(message + " @SchoolWebpageParser", cause);
	}
	public ParseException(Throwable cause){
		super("encounter Exception when parse school page.", cause);
	}
}
