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

package com.board.games.helper;

import java.security.MessageDigest;

public class HashHelper {

	public static void main(String[] a) {
		boolean authenticated = authenticate(
				"rememberme",
				"sha256",
				"05f9d50dfca5287de4cd761cd387aba07840050475fdee8587a2fe5500fd31b9",
				"823f7a261dd46435401fa25cc951ef551127f2485b6a37dc9dbd92eac8253dcb");
		System.out.println("Authenticated " + (authenticated ? "YES" : "NO"));
		
		String hashType = "SHA1";
		try {
			MessageDigest md = MessageDigest.getInstance(hashType);
			String authenticateStr = "hd10180rememberme";
			md.update(authenticateStr.getBytes());
			
			byte[] output = md.digest();
	
			String hashPassword = bytesToHex(output);	
			System.out.println("hashPassword =  " + hashPassword);
			
			// 92ff6c5426a23d105af69f49eb9d0210972ecbca
		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		
	}

	public static boolean authenticate(String password, String hashFunc,
			String salt, String dbPassword) {
		try {
			String hashType = "SHA1";
			if (hashFunc.equals("sha256")) {
				hashType = "SHA-256";
			}
			MessageDigest md = MessageDigest.getInstance(hashType);

			md.update(password.getBytes());
			byte[] output = md.digest();

			String hashPassword = bytesToHex(output);

			// sha256(sha256(password) . salt)
			String checkHashedPwd = hashPassword + salt;

			String input = checkHashedPwd;
			md.update(input.getBytes());
			output = md.digest();

			String decodedPassword = bytesToHex(output);
			if (decodedPassword.equals(dbPassword)) {
				return true;
			}

		} catch (Exception e) {
			System.out.println("Exception: " + e);
		}
		return false;
	}

	public static String bytesToHex(byte[] hash) {

		StringBuffer sb = new StringBuffer();
		for (byte b : hash) {
			sb.append(String.format("%02x", b));
		}
		return sb.toString();
	}
	

}
