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
package com.board.games.handler.jl;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.board.games.helper.HashHelper;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.local.LoginResponseAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.service.ServiceRouter;


public class JLPokerLoginServiceImpl implements LoginHandler {

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
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
			String user = ini.get("JDBCConfig", "user");
			String password = ini.get("JDBCConfig", "password");
			jdbcDriverClassName = ini.get("JDBCConfig", "driverClassName");
			connectionStr = "jdbc" + ":" + jdbcDriver + "://" + connectionUrl
					+ "/" + database + "?user=" + user + "&password="
					+ password;
			log.debug("user " + user);
			log.debug("connectionStr " + connectionStr);
		} catch (IOException ioe) {
			log.error("Exception in init " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in init " + e.toString());
		}

		LoginResponseAction response = null;
		try {
			log.debug("Performing authentication on " + req.getUser());
			String userIdStr = authenticate(req.getUser(), req.getPassword());
			if (!userIdStr.equals("")) {
				
				response = new LoginResponseAction(Integer.parseInt(userIdStr) > 0?true:false, (req.getUser().toUpperCase().startsWith("GUESTXDEMO")?req.getUser()+"_"+userIdStr:req.getUser()),
						Integer.parseInt(userIdStr)); // pid.incrementAndGet()
				log.debug(Integer.parseInt(userIdStr) > 0?"Authentication successful":"Authentication failed");
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
			
/*			smf_members 
			password_salt = 0682
			passwd = 92ff6c5426a23d105af69f49eb9d0210972ecbca
			id_member
			posts
			member_name	*/		
			
			String selectSQL = "select member_name, id_member, "
					+ " passwd,  password_salt,  "
					+ " posts from " + dbPrefix + "members "
					+ " where member_name = " + "\'" + user + "\'";
			log.debug("Executing query : " + selectSQL);
			resultSet = statement.executeQuery(selectSQL);

			String members_pass_hash = null;
			int member_id = 0;
			int posts = 0;
			if (resultSet != null && resultSet.next()) {
				member_id = resultSet.getInt("id_member");
				String name = resultSet.getString("member_name");
				members_pass_hash = resultSet.getString("passwd");
				
				log.debug("DB members_pass_hash = " + members_pass_hash);


				posts = resultSet.getInt("posts");
				log.debug("User: " + user + " Password " + password);
				
				String escapePwdHTML = StringEscapeUtils.escapeHtml(password);
				log.debug("escapeHTML = " + escapePwdHTML);
				String pwdSha1 = getSha1(user.toLowerCase()+password);

				log.debug("pwdSha1 = " + pwdSha1);
	
				
				log.debug("members_pass_hash = " + members_pass_hash);
				log.debug("# of Post " + posts);
				
				if (pwdSha1 != null && members_pass_hash != null) {
					if (pwdSha1.equals(members_pass_hash)) {
						if (posts >= 1) {
							return String.valueOf(member_id);
						} else {
							log.debug("Required number of posts not met, denied login");
							return "-2";
						}
					} else {
						log.debug("hash not matched for user " + user + " password " + password);
						return "-1";
					}
				}
				
			} else {
				log.debug("resultset is null " + selectSQL);
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

	private synchronized String getSha1(String input) {
		try {
			
			String hashType = "SHA1";
			MessageDigest md = MessageDigest.getInstance(hashType);
			md.update(input.getBytes());
			
			byte[] output = md.digest();

			String hashPassword = HashHelper.bytesToHex(output);			

			return hashPassword;
		} catch (Exception e) {
			log.error("Exception: " + e);
		}
        return null;
	}


	
}

