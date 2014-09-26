/**
 * This file is part of Boardservice Software package.
 * @copyright (c) 2014 Cuong Pham-Minh
 *
 * Boardservice is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License, version 2 (GPL-2.0)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * A copy of the license can be viewed in the docs/LICENSE.txt file.
 * The same can be viewed at <http://opensource.org/licenses/gpl-2.0.php>
 */
/*BSD license

Copyright (c) 2010, Ralf Ebert
All rights reserved.*/
package jgravatar;

public enum GravatarRating {

	GENERAL_AUDIENCES("g"),

	PARENTAL_GUIDANCE_SUGGESTED("pg"),

	RESTRICTED("r"),

	XPLICIT("x");

	private String code;

	private GravatarRating(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

}