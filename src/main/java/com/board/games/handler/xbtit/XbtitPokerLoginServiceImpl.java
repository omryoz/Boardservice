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
package com.board.games.handler.xbtit;


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

import com.board.games.service.wallet.WalletAdapter;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.mindrot.jbcrypt.BCrypt;

import com.board.games.config.ServerConfig;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.board.games.handler.generic.PokerConfigHandler;

import org.apache.commons.lang.StringEscapeUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

public class XbtitPokerLoginServiceImpl extends PokerConfigHandler implements LoginHandler {
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
	private boolean needAgeAgreement = false;
	private int authTypeId = 1;
	protected void initialize() {
		super.initialize();
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
			String jdbcDriver = ini.get("JDBCConfig", "jdbcDriver");
			String connectionUrl = ini.get("JDBCConfig", "connectionUrl");
			String database = ini.get("JDBCConfig", "database");
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
			String user = ini.get("JDBCConfig", "user");
			String password = ini.get("JDBCConfig", "password");
			jdbcDriverClassName = ini.get("JDBCConfig", "driverClassName");
			connectionStr = "jdbc" + ":" + jdbcDriver + "://" + connectionUrl
					+ "/" + database + "?user=" + user + "&password="
					+ password;
			String forceAgeAgreement = ini.get("JDBCConfig", "forceAgeAgreement");
			if (!forceAgeAgreement.equals("") && "Y".equals(forceAgeAgreement.toUpperCase())) {
				needAgeAgreement = true;
			}
					
		} catch (IOException ioe) {
			log.error("Exception in initialize " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in initialize " + e.toString());
		}
	}
	@Override
	public LoginResponseAction handle(LoginRequestAction req) {
		// Must be the very first call
		initialize();		
		boolean userHasAcceptedAgeclause = false;
		log.debug("Data login " + req.getData());
		int count = 0;
		int idx = 0;
		int ref =0;
		StringBuffer sb = new StringBuffer();
		for (byte b : req.getData()) {
			idx++;
		    log.debug((char)b);
		    char val = (char)b;
		//	if (idx >7 )
		    sb.append(val);
		    
		}		
		//log.debug("count " + count);
		// TO DBG: activate trace of detail array below
		log.debug("sb " + sb.toString());
		 String logindataRequest = 	sb.toString();	
		 log.debug("logindataRequest" + logindataRequest);
		 if (logindataRequest.toUpperCase().equals("AGEVERIFICATIONDONE")) {
			 userHasAcceptedAgeclause = true;
		 }
		LoginResponseAction response = null;
		try {

			String userIdStr = authenticate(req.getUser(), req.getPassword(), getServerCfg(),userHasAcceptedAgeclause,authTypeId);
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

	private String authenticate(String user, String password, ServerConfig serverConfig, boolean checkAge, int authTypeId) throws Exception {
		try {
			if (serverConfig == null) {
				log.error("ServerConfig is null.");
				return "-3";
			}			
			int idx = user.indexOf("_");
			if (idx != -1) {
				// let bots through
				String idStr = user.substring(idx+1);
				if (user.toUpperCase().startsWith("BOT")) {
					if (serverConfig.isUseIntegrations()) {
						WalletAdapter walletAdapter = new WalletAdapter();
						log.debug("Calling createWalletAccount");
						//walletAdapter.createWalletAccount(new Long(String.valueOf(member_id)));
						Long userId = walletAdapter.checkCreateNewUser(idStr, idStr, "UNUSED", new Long(0), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), (serverConfig.getInitialAmount().multiply(new BigDecimal(20))),true,false,0);
						return String.valueOf(userId);
					} else {
						return idStr;
					}

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
			//log.debug("connectionStr " + connectionStr);
			connect = DriverManager.getConnection(connectionStr);

			// Statements allow to issue SQL queries to the database
			log.debug("calling createstatement");
			statement = connect.createStatement();
			log.debug("Execute query: authenticate");

			
			String selectSQL = "select id, username, salt, id_level, "
					+ " password, pass_type,  "
					+ " random, dupe_hash from " + dbPrefix + "users "
					+ " where username  = " + "\'" + user + "\'";
			log.debug("Executing query : " + selectSQL);
			resultSet = statement.executeQuery(selectSQL);
			String checkPwdHash = null;
			String checkPwdHashNew = null;
			String members_pass_hash = null;
			int posts = 0;
			int member_id = 0;
			if (resultSet != null && resultSet.next()) {
				member_id = resultSet.getInt("id");

				String members_display_name = resultSet.getString("username");
				members_pass_hash = resultSet.getString("password");
				
				log.error("DB members_pass_hash = " + members_pass_hash);
				String escapePwdHTML = StringEscapeUtils.escapeHtml(password);
		//		log.debug("escapeHTML = " + escapePwdHTML);
				String pwdMD5 = getMD5(escapePwdHTML);
				log.debug("pwdMD5 = " + pwdMD5);
	//			posts = resultSet.getInt("user_posts");
	//			log.debug("# of Post " + posts);
				boolean authenticated = false;
				
				if (serverConfig != null) {
				
					log.debug("User: " + user + " Password " + "********");
					
					// whoelse = $H$9G7SmcSl2EYeV3n49s9ronMpYGayL61
						if (pwdMD5 != null && members_pass_hash != null) {
							authenticated = true;
						}
 
					
				} else {
					log.error("ServerConfig is null.");
					return "-3";
				}					
				if (authenticated) {
					log.debug("Authentication successful");
					
		//			log.debug("Member id " + String.valueOf(member_id));
					
					if (serverConfig.isUseIntegrations()) {
						
						WalletAdapter walletAdapter = new WalletAdapter();
						log.error("Calling createWalletAccount");
						Long userId = walletAdapter.checkCreateNewUser(String.valueOf(member_id), members_display_name,  "UNUSED", new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge,needAgeAgreement,authTypeId);
						if (userId < 0 ) {
							// user did not accept age clauses
							return "-5";
						}
						log.debug("assigned new id as #" + String.valueOf(userId));
						return String.valueOf(userId);	
					} else {
						return String.valueOf(member_id);
					}
					
/*						if (posts >= 1) {
							return String.valueOf(member_id);
						} else {
							log.error("Required number of posts not met, denied login");
							return "-2";
						}*/
				} else {
					log.error("hash not matched for user " + user + " password " + password);
					return "-1";
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
 * Port of Xbtit password handling to Java. 

 * See Xbtit/includes/functions.php
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
  

	private synchronized String getMD5(String input) {
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
  
}