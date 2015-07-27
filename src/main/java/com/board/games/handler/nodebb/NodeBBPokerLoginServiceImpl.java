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
package com.board.games.handler.nodebb;


import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.mindrot.jbcrypt.BCrypt;

import com.board.games.config.ServerConfig;
import com.board.games.handler.generic.PokerConfigHandler;
import com.board.games.service.wallet.WalletAdapter;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.MongoClient;
public class NodeBBPokerLoginServiceImpl extends PokerConfigHandler implements LoginHandler {

  
	private static AtomicInteger pid = new AtomicInteger(0);
	private Logger log = Logger.getLogger(this.getClass());
	private ServiceRouter router;
	private String connectionStr = "";
	private String jdbcDriverClassName = "";
	private String dbPrefix = "";
	private boolean needAgeAgreement = false;
	 private int authTypeId = 1;
	private	DBCursor cursor = null;
	private DBCursor cursor2 = null;


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
			//    log.debug((char)b);
			    char val = (char)b;
			//	if (idx >7 )
			    sb.append(val);
			    
			}		
			
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
								
			//log.debug("count " + count);
			// TO DBG: activate trace of detail array below
			log.debug("sb " + sb.toString());
			 String logindataRequest = 	sb.toString();	
/*			 log.debug("logindataRequest" + logindataRequest);
			 if (logindataRequest.toUpperCase().equals("AGEVERIFICATIONDONE")) {
				 userHasAcceptedAgeclause = true;
			 }*/
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
				userIdStr = authenticateSocialNetwork(req.getUser(), socialNetworkId, socialAvatar, getServerCfg(),userHasAcceptedAgeclause,authTypeId, needAgeAgreement);
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
/*			String userIdStr = authenticate(req.getUser(), req.getPassword(), getServerCfg(),userHasAcceptedAgeclause,authTypeId);
			if (!userIdStr.equals("")) {
				response = new LoginResponseAction(Integer.parseInt(userIdStr) > 0?true:false, (req.getUser().toUpperCase().startsWith("GUESTXDEMO")?req.getUser()+"_"+userIdStr:req.getUser()),
						Integer.parseInt(userIdStr)); // pid.incrementAndGet()
				return response;
			}*/
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
	  		log.debug("Execute query: authenticate");
			String members_pass_hash = null;
			String members_display_name = null;
			boolean authenticated = false;

			int member_id = 0;
			int posts = 0;
			
		    		 // To connect to mongodb server
		    	         MongoClient mongoClient = new MongoClient( "localhost" , 27017 );
		    	         // Now connect to your databases
		    	         DB db = mongoClient.getDB( "nodebb" );
		    //		 log.debug("Connect to database successfully");
		 			log.debug("Execute query: authenticate");
		    		 DBCollection collection = db.getCollection("objects");
		    		 cursor = collection.find(new BasicDBObject("username", user),
		    							   new BasicDBObject("_id", 0));
		    	       
		    		
					members_pass_hash = (String) cursor.next().get("password");
					log.error("DB members_pass_hash = " + members_pass_hash);
		    		
		    		 cursor2 = collection.find(new BasicDBObject("username", user),
		    				   new BasicDBObject("_id", 0));

		    		member_id = (int) cursor2.next().get("uid");
					log.error("DB member_id = " + member_id);
		    		 	
				log.debug("User: " + user + " Password " + "********");
	
				if (members_pass_hash != null) {
		        	   // Check it against database stored hash
					   authenticated = BCrypt.checkpw(password,members_pass_hash);
	           	
		           } else {
		        	   log.debug("failed verification of hashing");
		           }
	
					
				if (authenticated) {
					log.debug("Authentication successful");
					
					log.debug("Member id " + String.valueOf(member_id));
					
					if (serverConfig.isUseIntegrations()) {
						
						WalletAdapter walletAdapter = new WalletAdapter();
						log.error("Calling createWalletAccount");
						//walletAdapter.createWalletAccount(new Long(String.valueOf(member_id)));
						Long userId = walletAdapter.checkCreateNewUser(String.valueOf(member_id), user,  "UNUSED", new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge, needAgeAgreement, authTypeId);
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
			if (cursor != null) {
				cursor.close();
			}

			if (cursor2 != null) {
				cursor2.close();
			}

		} catch (Exception e) {

		}
	}


}