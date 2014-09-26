/*BSD license

Copyright (c) 2010, Ralf Ebert
All rights reserved.*/
package jgravatar;

public class GravatarDownloadException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public GravatarDownloadException(Throwable cause) {
		super("Gravatar could not be downloaded: " + cause.getMessage(), cause);
	}

}
