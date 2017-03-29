/**
 * 
 */
package dev.sidney.crawler.simplecrawler.exceptions;

/**
 * @author 杨丰光 2017年3月29日09:26:44
 *
 */
public class BusinessException extends Exception {

	/**
	 * uid
	 */
	private static final long serialVersionUID = 7498512595199150795L;

	public BusinessException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public BusinessException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
		// TODO Auto-generated constructor stub
	}

	public BusinessException(String arg0, Throwable arg1) {
		super(arg0, arg1);
		// TODO Auto-generated constructor stub
	}

	public BusinessException(String arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

	public BusinessException(Throwable arg0) {
		super(arg0);
		// TODO Auto-generated constructor stub
	}

}
