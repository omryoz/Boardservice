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

package com.board.games.handler.ipb;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.board.games.config.ServerConfig;
import com.board.games.handler.generic.PokerConfigHandler;
import com.board.games.helper.BCrypt;
import com.board.games.helper.HashHelper;
import com.board.games.model.PlayerProfile;
import com.board.games.service.wallet.WalletAdapter;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceRouter;

public class IPBPokerLoginServiceImpl extends PokerConfigHandler implements LoginHandler {

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
	  private static boolean newIPB4Version = false;
	  private boolean needAgeAgreement = false;
	 private int authTypeId = 1;

	protected void initialize() {
		super.initialize();
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
			String jdbcDriver = ini.get("JDBCConfig", "jdbcDriver");
			String connectionUrl = ini.get("JDBCConfig", "connectionUrl");
			String database = ini.get("JDBCConfig", "database");
			String ipbVersion = "";
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
			String user = ini.get("JDBCConfig", "user");
			String password = ini.get("JDBCConfig", "password");
	/*			currency = ini.get("JDBCConfig", "currency");
			walletBankAccountId = ini.get("JDBCConfig", "walletBankAccountId");
			initialAmount = ini.get("JDBCConfig", "initialAmount");
			useIntegrations = ini.get("JDBCConfig", "useIntegrations");
			serverCfg = new ServerConfig(currency, new Long(walletBankAccountId), new BigDecimal(initialAmount), useIntegrations.equals("Y")? true:false);
	*/
			ipbVersion = ini.get("JDBCConfig", "ipbVersion");
			if (ipbVersion!= null && !ipbVersion.equals("") && "IPS4".equals(ipbVersion.toUpperCase())) {
				newIPB4Version = true;
				log.debug("Detecting  IPS4 version");
			}
			String forceAgeAgreement = ini.get("JDBCConfig", "forceAgeAgreement");
			if (forceAgeAgreement!= null && !forceAgeAgreement.equals("") && "Y".equals(forceAgeAgreement.toUpperCase())) {
				needAgeAgreement = true;
			}
			String authType = ini.get("JDBCConfig", "authType");
			// Must readjust from admin console do not need to do a query off database for that
			// by default we use authentication by display name
			// 2 for email
			// 3 for either of the 2
			// for 2, 3 need to readjust code in the client if not it will show email at player seat instead 
			// of display name
			if (authType!= null && !authType.equals("") && "2".equals(authType.toUpperCase())) {
				authTypeId = 2;
			} else if (!authType.equals("") && "3".equals(authType.toUpperCase())) {
				authTypeId = 2;
			}
			
			
			jdbcDriverClassName = ini.get("JDBCConfig", "driverClassName");
			log.debug("Using jdbc " + jdbcDriverClassName);
			connectionStr = "jdbc" + ":" + jdbcDriver + "://" + connectionUrl
					+ "/" + database + "?user=" + user + "&password="
					+ password;
			log.debug("User " + user);
			//log.debug("connectionStr " + connectionStr);
		} catch (IOException ioe) {
			log.error("Exception in initialize " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in initialize " + e.toString());
		}
		
	}
	@Override
	public LoginResponseAction handle(LoginRequestAction req) {
		// At this point, we should get the user name and password
		// from the request and verify them, but for this example
		// we'll just assign a dynamic player ID and grant the login
/*		String currency = "USD";
		String walletBankAccountId = "2";
		String initialAmount = "1000";
		String useIntegrations = "Y";
		ServerConfig serverCfg=null;*/
			// Must be the very first call
			initialize();			
			boolean userHasAcceptedAgeclause = false;
		//	boolean useFacebookAuthentication = false;
			//log.debug("Data login " + req.getData());
			
			boolean isBot = false;
			String loginName = req.getUser();
			int idxb = loginName.indexOf("_");
			if (idxb != -1) {
				// let bots through
				String idStr = loginName.substring(idxb+1);
				if (loginName.toUpperCase().startsWith("BOT")) {
					isBot = true;
				}
			}
			
			int count = 0;
			int idx = 0;
			int ref =0;
			StringBuffer sb = new StringBuffer();
			for (byte b : req.getData()) {
				idx++;
		//	    log.debug((char)b);
			    char val = (char)b;
			//	if (idx >7 )
			    sb.append(val);
			    
			}		
			//log.debug("count " + count);
			// TO DBG: activate trace of detail array below
			log.debug("sb " + sb.toString());
			 String logindataRequest = 	sb.toString();	
				 
			 log.debug("datarequest " + logindataRequest);
			 StringTokenizer st=new StringTokenizer(logindataRequest,";");
			 String socialNetworkId = "";
			 String socialAvatar = "";
			 if (!isBot) {
				 String ageClause = st.nextToken().trim();
				 log.debug("ageClause " + ageClause); 
				 if (ageClause.toUpperCase().equals("AGEVERIFICATIONDONE")) {
					 userHasAcceptedAgeclause = true;
				 }
				 log.debug("User has accepted clause = " + (userHasAcceptedAgeclause? "yes" : "no"));
				 int snFlag =new Integer(st.nextToken());
				 log.debug("authId " + (snFlag==5 || snFlag== 6?"use sn":"no sn"));
				  socialNetworkId = (String)st.nextToken();
				 log.debug("socialNetworkId " + socialNetworkId);
				 socialAvatar =new String(st.nextToken());
				 log.debug("socialAvatar " + socialAvatar);
				 if (snFlag==5) {
					 // overwrite default authTypeId
					 authTypeId = 5; //force email with fb
					 log.debug("authTypeId " + authTypeId);
				 } else if (snFlag==6) {
						 // overwrite default authTypeId
					 authTypeId = 6; //force email with google plus
					 log.debug("authTypeId " + authTypeId);
				 } else {
					 authTypeId = 1;
				 }
			 } else {
				 socialNetworkId ="999999";
				 socialAvatar = "";
				 userHasAcceptedAgeclause = true;
				 authTypeId = 1;
			 }
			 LoginResponseAction response = null;
		try {
			log.debug("Performing authentication on " + req.getUser());
			String userIdStr = null;
			if (authTypeId == 5 || authTypeId == 6) {
				log.debug("*** Social Network authentication ***");
				userIdStr = authenticateSocialNetwork(req.getUser(), socialNetworkId, socialAvatar, getServerCfg(),userHasAcceptedAgeclause,authTypeId);
			}
			else {
				log.debug("*** Forum authentication ***");
				userIdStr = authenticate(req.getUser(), req.getPassword(), getServerCfg(),userHasAcceptedAgeclause,authTypeId);
			}
			if (!userIdStr.equals("")) {
				
				response = new LoginResponseAction(Integer.parseInt(userIdStr) > 0?true:false, (req.getUser().toUpperCase().startsWith("GUESTXDEMO")?req.getUser()+"_"+userIdStr:req.getUser()),
						Integer.parseInt(userIdStr)); // pid.incrementAndGet()
				response.setErrorCode(Integer.parseInt(userIdStr));
				String errMsg = "Login failed ";
				switch (Integer.parseInt(userIdStr)) {
					case -3 : errMsg += " User does not exist, please sign up same account type on forum first";
							break;
					case -5 : errMsg += " User must check age requirement to play due to 18+ clause";
						break;
					case -2 : errMsg += " User has not met required posts";
						break;
					case -1 : errMsg += " User password is invalid";
						break;
					default:
						break;
				}
				

				response.setErrorMessage(errMsg);
				log.debug(Integer.parseInt(userIdStr) > 0?"Authentication successful":"Authentication failed with errorCode as " + userIdStr);
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

	private String authenticateSocialNetwork(String user, String uId, String socialAvatar, ServerConfig serverConfig, boolean checkAge, int authTypeId ) throws Exception {
		int member_id = 0;
		if (serverConfig != null) {	
			if (serverConfig.isUseIntegrations()) {
				
				WalletAdapter walletAdapter = new WalletAdapter();
				log.debug("Calling createWalletAccount");
				Long userId = walletAdapter.checkCreateNewUser(uId, user,  socialAvatar, new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge, needAgeAgreement, authTypeId);
				
				if (userId < 0 ) {
					log.debug("Player did did not accept age clause");
					// user did not accept age clauses
					return "-5";
				}
				log.debug("assigned new id as #" + String.valueOf(userId));
				return String.valueOf(userId);
	/*						if (posts >= 1) {
						return String.valueOf(member_id);
					} else {
						log.error("Required number of posts not met, denied login");
						return "-2";
					}
	*/
			} else {
				return String.valueOf(uId);
			}
		
		} else {
			log.error("ServerConfig is null.");
		}						
		return "-3";
	
	}
	
	private String authenticate(String user, String password, ServerConfig serverConfig, boolean checkAge, int authTypeId) throws Exception {
		boolean authenticated = false;
		try {
/*			
 			if (user.toUpperCase().startsWith("SYSFP97")) {
				return "123456789";
			}*/
			int idx = user.indexOf("_");
			if (idx != -1) {
				// let bots through
				String idStr = user.substring(idx+1);
				if (user.toUpperCase().startsWith("BOT")) {
					if (serverConfig.isUseIntegrations()) {
						WalletAdapter walletAdapter = new WalletAdapter();
						log.debug("Calling createWalletAccount");
						//walletAdapter.createWalletAccount(new Long(String.valueOf(member_id)));
						Long userId = walletAdapter.checkCreateNewUser(idStr, user, "UNUSED", new Long(0), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), (serverConfig.getInitialAmount().multiply(new BigDecimal(20))),true,false,0);
						return String.valueOf(userId);
					} else {
						return idStr;
					}

				}
			}
			if (user.toUpperCase().startsWith("GUESTXDEMO")) {
				return String.valueOf(pid.incrementAndGet()+500000);
			}
			
			log.debug("loading class name for database connection" + jdbcDriverClassName);
			// This will load the MySQL driver, each DB has its own driver
			// "com.mysql.jdbc.Driver"
			Class.forName(jdbcDriverClassName);
			// Setup the connection with the DB
			// "jdbc:mysql://localhost/dbName?" + "user=&password=");
			connect = DriverManager.getConnection(connectionStr);

			// Statements allow to issue SQL queries to the database
			statement = connect.createStatement();
			log.debug("Execute query: authenticate");
			// Result set get the result of the SQL query
			// SELECT * FROM ipb3_members WHERE members_seo_name = ''
			String data = newIPB4Version ? "core_members " : "members ";
			String selectSQL = "";
			if (newIPB4Version) {
				selectSQL = "select members_seo_name,  member_id, name, member_group_id, "
					+ " members_pass_hash,  members_pass_salt,  "
					+ " member_title, member_posts from " + dbPrefix + data
					+ " where " 
					+ " name = " + "\'" + user + "\'";
			} else {
				selectSQL = "select members_seo_name,  member_id, name, member_group_id, "
						+ " members_pass_hash,  members_pass_salt,  "
						+ " title, posts from " + dbPrefix + data
						+ " where name = " + "\'" + user + "\'";
			}
				
			log.debug("Executing query : " + selectSQL);
			resultSet = statement.executeQuery(selectSQL);
			String checkPwdHash = null;
			String checkPwdHashNew = null;
			String members_pass_hash = null;
			int member_id = 0;
			int posts = 0;
			int rank = 0;
			if (resultSet != null && resultSet.next()) {
				String members_seo_name = resultSet
						.getString("members_seo_name");
				member_id = resultSet.getInt("member_id");
				String name = resultSet.getString("name");
				members_pass_hash = resultSet.getString("members_pass_hash");
				
				log.debug("DB members_pass_hash = " + members_pass_hash);
				
				String members_pass_salt = resultSet
						.getString("members_pass_salt");
				//String members_display_name = resultSet
				//		.getString("members_display_name");
				String title = resultSet.getString(newIPB4Version?"member_title":"title");
				posts = resultSet.getInt(newIPB4Version?"member_posts":"posts");
				rank = posts = resultSet.getInt("member_group_id");
				log.debug("User: " + user + " Password " + "**********");
				
				String escapePwdHTML = StringEscapeUtils.escapeHtml(password);
				//log.debug("escapeHTML = " + escapePwdHTML);
				
				if (!newIPB4Version) {
					String pwdMD5 = HashHelper.getMD5(password);
	
					log.debug("pwdMD5 = " + pwdMD5);
					
					
					String pwdSaltMD5 = HashHelper.getMD5(members_pass_salt);
					if (pwdSaltMD5 == null)
						log.debug("pwdMD5 is null");
					
	
					log.debug("pwdSaltMD5 = " + pwdSaltMD5);
					
					if (pwdMD5 != null) {
						checkPwdHash = HashHelper.getMD5(pwdSaltMD5 + pwdMD5);
						log.debug("checkPwdHash = " + checkPwdHash);
					}
					else
						log.debug("pwdMD5 is null");
					
				
					if (checkPwdHash != null && members_pass_hash != null) {
						if (checkPwdHash.equals(members_pass_hash)) {
							authenticated = true;
						}
					}
				} else {
					authenticated = BCrypt.checkpw(escapePwdHTML,members_pass_hash);
										
				}
				log.debug("members_pass_hash = " + members_pass_hash);
				log.debug("# of Post " + posts);
				
				if (authenticated) {
					if (serverConfig != null) {	
					if (serverConfig.isUseIntegrations()) {
						
						WalletAdapter walletAdapter = new WalletAdapter();
						log.debug("Calling createWalletAccount");
						//walletAdapter.createWalletAccount(new Long(String.valueOf(member_id)));
						Long userId = walletAdapter.checkCreateNewUser(String.valueOf(member_id), members_seo_name,  "UNUSED", new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge, needAgeAgreement,authTypeId);
						
						if (userId < 0 ) {
							log.debug("Player did did not accept age clause");
							// user did not accept age clauses
							return "-5";
						}
						log.debug("assigned new id as #" + String.valueOf(userId));
						return String.valueOf(userId);
/*						if (posts >= 1) {
								return String.valueOf(member_id);
							} else {
								log.error("Required number of posts not met, denied login");
								return "-2";
							}
*/
					} else {
						return String.valueOf(member_id);
					}
					
				} else {
					log.error("ServerConfig is null.");
				}						
						} else {
					log.error("Authenticated failed: hash not matched for user " + user + " password " + password);
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
