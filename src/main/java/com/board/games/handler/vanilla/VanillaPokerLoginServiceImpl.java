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
package com.board.games.handler.vanilla;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceRouter;

public class VanillaPokerLoginServiceImpl implements LoginHandler {
  private static final int PHP_VERSION = 4;
  private String itoa64 = 
"./0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";


  
	private static AtomicInteger pid = new AtomicInteger(0);
	private Logger log = Logger.getLogger(this.getClass());
	private ServiceRouter router;
	private static Connection connect = null;
	private static Statement statement = null;
	// private static PreparedStatement preparedStatement = null;
	private static ResultSet resultSet = null;
	private String connectionStr = "";
	private String jdbcDriverClassName = "";
	private String dbPrefix = "";

	@Override
	public LoginResponseAction handle(LoginRequestAction req) {
		// At this point, we should get the user name and password
		// from the request and verify them, but for this example
		// we'll just assign a dynamic player ID and grant the login
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
			String jdbcDriver = ini.get("JDBCConfig", "jdbcDriver");
			String connectionUrl = ini.get("JDBCConfig", "connectionUrl");
			String database = ini.get("JDBCConfig", "database");
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");  //phpbb_
			String user = ini.get("JDBCConfig", "user");
			String password = ini.get("JDBCConfig", "password");
			jdbcDriverClassName = ini.get("JDBCConfig", "driverClassName");
			connectionStr = "jdbc" + ":" + jdbcDriver + "://" + connectionUrl
					+ "/" + database + "?user=" + user + "&password="
					+ password;
		} catch (IOException ioe) {
			log.error("Exception in init " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in init " + e.toString());
		}

		LoginResponseAction response = null;
		try {

			String userIdStr = authenticate(req.getUser(), req.getPassword());
			if (!userIdStr.equals("")) {
				response = new LoginResponseAction(Integer.parseInt(userIdStr) > 0?true:false, (req.getUser().toUpperCase().startsWith("GUESTXDEMO")?req.getUser()+"_"+userIdStr:req.getUser()),
						Integer.parseInt(userIdStr)); // pid.incrementAndGet()
				return response;
			}
		} catch (SQLException sqle) {
			log.error("Error authenticate", sqle);
			response = new LoginResponseAction(false, -1);
			response.setErrorMessage(getSystemErrorMessage(sqle));
			response.setErrorCode(getSystemErrorCode(sqle));
			log.error(sqle);
		} catch (Exception e) {
			log.error("Error system", e);
		}

		response = new LoginResponseAction(false, -1);
		response.setErrorMessage(getNotFoundErrorMessage());
		response.setErrorCode(getNotFoundErrorCode());
		return response;
	}

	/**
	 * This method should return the error code to send back if the sql query
	 * fails. Default msg is 0.
	 * 
	 * @param e
	 *            The sql exception, never null
	 * @return The system error code
	 */
	protected int getSystemErrorCode(SQLException e) {
		return 0;
	}

	/**
	 * This method should return the error message to send back if the sql query
	 * fails. Default msg is "System error."
	 * 
	 * @param e
	 *            The sql exception, never null
	 * @return The system error message
	 */
	protected String getSystemErrorMessage(SQLException e) {
		return "System error.";
	}

	/**
	 * This method should return the error code to send back if the sql query
	 * does not get any results. Default msg is 0.
	 * 
	 * @return The "user not found" error code
	 */
	protected int getNotFoundErrorCode() {
		return 0;
	}

	/**
	 * This method should return the message to send back if the sql query does
	 * not get any results. Default msg is "User not found."
	 * 
	 * @return The "user not found" error message, may be null
	 */
	protected String getNotFoundErrorMessage() {
		return "User not found or registered but at least 1 post is required to play.";
	}

	private String authenticate(String user, String password) throws Exception {
		try {
			int idx = user.indexOf("_");
			if (idx != -1) {
				// let bots through
				String idStr = user.substring(idx+1);
				if (user.toUpperCase().startsWith("BOT")) {
					return idStr;
				}
			}
			if (user.toUpperCase().startsWith("GUESTXDEMO")) {
				return String.valueOf(pid.incrementAndGet());
			}			
			log.debug("loading class name " + jdbcDriverClassName);
			// This will load the MySQL driver, each DB has its own driver
			// "com.mysql.jdbc.Driver"
			Class.forName(jdbcDriverClassName);
			// Setup the connection with the DB
			// "jdbc:mysql://localhost/dbName?" + "user=&password=");
			connect = DriverManager.getConnection(connectionStr);

			// Statements allow to issue SQL queries to the database
			//phpbb_users
			statement = connect.createStatement();
			log.debug("Execute query: authenticate");
			// Result set get the result of the SQL query
			// SELECT * FROM ipb3_members WHERE members_seo_name = ''
			// user_avatar user_avatar_type
			//phpbb_hash
			String selectSQL = "select user_id, username, "
					+ " user_password, user_rank,  "
					+ " username_clean, user_posts  from " + dbPrefix + "users "
					+ " where name = " + "\'" + user + "\'";
			log.debug("Executing query : " + selectSQL);
			resultSet = statement.executeQuery(selectSQL);
			String checkPwdHash = null;
			String checkPwdHashNew = null;
			String members_pass_hash = null;
			int member_id = 0;
			int posts = 0;
			if (resultSet != null && resultSet.next()) {
				String members_seo_name = resultSet
						.getString("username_clean");
				member_id = resultSet.getInt("user_id");
				String name = resultSet.getString("username");
				members_pass_hash = resultSet.getString("user_password");
				
				log.error("DB members_pass_hash = " + members_pass_hash);
				
				posts = resultSet.getInt("user_posts");
				log.debug("# of Post " + posts);
				
				log.debug("User: " + user + " Password " + password);
				
				String hashedPwd = phpbb_hash(password);
				log.error("hashedPwd = " + hashedPwd);
				
				// whoelse = $H$9G7SmcSl2EYeV3n49s9ronMpYGayL61
				if (hashedPwd != null && members_pass_hash != null) {
					boolean result = phpbb_check_hash(user, hashedPwd);

					if (result) {
						log.debug("Authentication successful");
						if (posts >= 1) {
							return String.valueOf(member_id);
						} else {
							log.error("Required number of posts not met, denied login");
							return "-2";
						}
					} else {
						log.error("hash not matched for user " + user + " password " + password);
						return "-1";
					}
				}
				
			} else {
				log.error("resultset is null " + selectSQL);
			}
			

		} catch (Exception e) {
			log.error("Error : " + e.toString());
			// throw e;
		} finally {
			close();
		}
		return "-3";
	}



	private void close() {
		try {
			if (resultSet != null) {
				resultSet.close();
			}

			if (statement != null) {
				statement.close();
			}

			if (connect != null) {
				connect.close();
			}
		} catch (Exception e) {

		}
	}

  
  
/**
 * Port of phpBB3 password handling to Java. 

 * See phpBB3/includes/functions.php
 * 
 * @author lars
 */
  
  
  
  
  public String phpbb_hash(String password) {
    String random_state = unique_id();
    String random = "";
    int count = 6;

    if (random.length() < count) {
      random = "";


      for (int i = 0; i < count; i += 16) {
        random_state = md5(unique_id() + random_state);
        random += pack(md5(random_state));
      }
      random = random.substring(0, count);
    }


    String hash = _hash_crypt_private(
      password, _hash_gensalt_private(random, itoa64));
    if (hash.length() == 34)
      return hash;

    return md5(password);
  }

  private String unique_id() {

    return unique_id("c");
  }

  // global $config;
  // private boolean dss_seeded = false;

  private String unique_id(String extra) {
    // TODO Generate something random here.
    return "1234567890abcdef";

  }

  private String _hash_gensalt_private(String input, String itoa64) {
    return _hash_gensalt_private(input, itoa64, 6);
  }

  private String _hash_gensalt_private(
    String input, String itoa64, int iteration_count_log2) {

    if (iteration_count_log2 < 4 || iteration_count_log2 > 31) {
      iteration_count_log2 = 8;
    }

    String output = "$H$";
    output += itoa64.charAt(
      Math.min(iteration_count_log2 +

      ((PHP_VERSION >= 5) ? 5 : 3), 30));
    output += _hash_encode64(input, 6);

    return output;
  }

  /**
   * Encode hash
   */
  private String _hash_encode64(String input, int count) {

    String output = "";
    int i = 0;

    do {
      int value = input.charAt(i++);
      output += itoa64.charAt(value & 0x3f);
 
      if (i < count)
        value |= input.charAt(i) << 8;

 
      output += itoa64.charAt((value >> 6) & 0x3f);
 
      if (i++ >= count)
        break;
 
      if (i < count)
        value |= input.charAt(i) << 16;
 
      output += itoa64.charAt((value >> 12) & 0x3f);

 
      if (i++ >= count)
        break;
 
      output += itoa64.charAt((value >> 18) & 0x3f);
    } while (i < count);

    return output;
  }

  String _hash_crypt_private(String password, String setting) {

      String output = "*";

      // Check for correct hash
      if (!setting.substring(0, 3).equals("$H$"))
        return output;

      int count_log2 = itoa64.indexOf(setting.charAt(3));

      if (count_log2 < 7 || count_log2 > 30)
        return output;

      int count = 1 << count_log2;
      String salt = setting.substring(4, 12);
      if (salt.length() != 8)
        return output;


      String m1 = md5(salt + password);
      String hash = pack(m1);
      do {
        hash = pack(md5(hash + password));
      } while (--count > 0);

      output = setting.substring(0, 12);

      output += _hash_encode64(hash, 16);

      return output;
  }

  public boolean phpbb_check_hash(
    String password, String hash) {
      if (hash.length() == 34)
        return _hash_crypt_private(password, hash).equals(hash);

      else
        return md5(password).equals(hash);
      }

  public static String md5(String data) {
    try {
      byte[] bytes = data.getBytes("ISO-8859-1");
      MessageDigest md5er = MessageDigest.getInstance("MD5");

      byte[] hash = md5er.digest(bytes);
      return bytes2hex(hash);
    } catch (GeneralSecurityException e) {
      throw new RuntimeException(e);
    } catch (UnsupportedEncodingException e) {
      throw new RuntimeException(e);

    }
  }
  
  static int hexToInt(char ch) {
    if(ch >= '0' && ch <= '9')
      return ch - '0';
  
    ch = Character.toUpperCase(ch);
    if(ch >= 'A' && ch <= 'F')

      return ch - 'A' + 0xA;
  
    throw new IllegalArgumentException("Not a hex character: " + ch);
  }
 
  private static String bytes2hex(byte[] bytes) {
    StringBuffer r = new StringBuffer(32);

    for (int i = 0; i < bytes.length; i++) {
      String x = Integer.toHexString(bytes[i] & 0xff);
      if (x.length() < 2)
        r.append("0");
      r.append(x);
    }
    return r.toString();

  }

  static String pack(String hex) {
    StringBuffer buf = new StringBuffer();
    for(int i = 0; i < hex.length(); i += 2) {
      char c1 = hex.charAt(i);
      char c2 = hex.charAt(i+1);
      char packed = (char) (hexToInt(c1) * 16 + hexToInt(c2));

      buf.append(packed);
    }
    return buf.toString();
  }
}