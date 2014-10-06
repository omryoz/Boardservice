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

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.log4j.Logger;

public class HashHelper {

	private static Logger log = Logger.getLogger(HashHelper.class);
	
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
	

	public static synchronized String getSha1(String input) {
		try {
			
			String hashType = "SHA1";
			MessageDigest md = MessageDigest.getInstance(hashType);
			md.update(input.getBytes());
			
			byte[] output = md.digest();

			String hashPassword = bytesToHex(output);			

			return hashPassword;
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
        return null;
	}	
	
	public static synchronized String getMD5(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] messageDigest = md.digest(input.getBytes("UTF-8"));
			BigInteger number = new BigInteger(1, messageDigest);
			String hashtext = number.toString(16);
			// Now we need to zero pad it if you actually want the full 32
			// chars.
			while (hashtext.length() < 32) {
				hashtext = "0" + hashtext;
			}
			return hashtext;
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        return null;
	}	
	
	
	public static  synchronized String getAlternativeMD5(String input) {
        // please note that we dont use digest, because if we
        // cannot get digest, then the second time we have to call it
        // again, which will fail again
        MessageDigest digest = null;

        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (digest == null)
            return input;

        // now everything is ok, go ahead
        try {
            digest.update(input.getBytes("UTF-8"));
        } catch (java.io.UnsupportedEncodingException ex) {
            ex.printStackTrace();
        }
        
        
        final StringBuilder sbMd5Hash = new StringBuilder();

        final byte data[] = digest.digest();

/*	        for (byte element : data) {
        sbMd5Hash.append(Character.forDigit((element >> 4) & 0xf, 16));
        sbMd5Hash.append(Character.forDigit(element & 0xf, 16));
        }
$%6e!df fek@&^$345M
pkrGlr1Test
        
        return sbMd5Hash.toString();
*/	        //final byte[] md5Digest = md.digest(password.getBytes());

        final BigInteger md5Number = new BigInteger(1, data);
        final String md5String = md5Number.toString(16);
        return md5String;
        
	}
	
}
