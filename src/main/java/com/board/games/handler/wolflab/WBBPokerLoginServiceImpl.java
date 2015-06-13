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
package com.board.games.handler.wolflab;


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

import com.board.games.config.ServerConfig;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.board.games.handler.generic.PokerConfigHandler;
import com.board.games.helper.BCrypt;
import com.board.games.helper.PHPBB3Password;

public class WBBPokerLoginServiceImpl extends PokerConfigHandler implements LoginHandler {
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
						Long userId = walletAdapter.checkCreateNewUser(idStr, user,  "UNUSED", new Long(0), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),true,false,0);
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
			connect = DriverManager.getConnection(connectionStr);

			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			log.debug("Execute query: authenticate");
			String selectSQL = "select userID, username, password, avatarID, enableGravatar, rankID, userTitle, userOnlineGroupID, wbbPosts " + 
					" from " + dbPrefix + "user "  //wcf1_
					+ " where username  = " + "\'" + user + "\'";
			log.debug("Executing query : " + selectSQL);
			resultSet = statement.executeQuery(selectSQL);
			String checkPwdHash = null;
			String checkPwdHashNew = null;
			String members_pass_hash = null;
			int member_id = 0;
			int posts = 0;
			if (resultSet != null && resultSet.next()) {
				String members_seo_name = resultSet
						.getString("username");
				member_id = resultSet.getInt("userID");
				String members_display_name = resultSet.getString("username");
				members_pass_hash = resultSet.getString("password");
				
				log.error("DB members_pass_hash = " + members_pass_hash);
				
				posts = resultSet.getInt("wbbPosts");
				log.debug("# of Post " + posts);
				boolean authenticated = false;
				
				if (serverConfig != null) {
					if (serverConfig.getVersion() != null & serverConfig.getVersion().equals("3.x")) {
						log.debug("User: " + user + " Password " + "********");
					} else {
						log.debug("Using new authentication blowfish with password " + "********");
						log.debug("Using salt as " + members_pass_hash);
						String encryptedPassword = members_pass_hash;
						String firstHash = BCrypt.hashpw(password, encryptedPassword);
						authenticated = BCrypt.checkpw(firstHash,encryptedPassword);
					}	
					
				} else {
					log.error("ServerConfig is null.");
					return "-3";
				}					
				if (authenticated) {
					log.debug("Authentication successful");
					
					log.debug("Member id " + String.valueOf(member_id));
					
					if (serverConfig.isUseIntegrations()) {
						
						WalletAdapter walletAdapter = new WalletAdapter();
						log.error("Calling createWalletAccount");
						//walletAdapter.createWalletAccount(new Long(String.valueOf(member_id)));
						Long userId = walletAdapter.checkCreateNewUser(String.valueOf(member_id), members_seo_name,  "UNUSED", new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge, needAgeAgreement,authTypeId);
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


}