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

package com.board.games.dao.phpbb3;




import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.board.games.dao.GenericDAO;
import com.board.games.model.PlayerProfile;
import com.board.games.service.wallet.WalletAdapter;
public class PHPBB3JdbcDAOImpl implements GenericDAO {


	private Logger log = Logger.getLogger(this.getClass());
	
	private DataSource dataSource;
	private boolean useEMoney = false;
	
 	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	//avatar_salt
	public String retrieveAvatarInfo() throws Exception{
    	String siteUrl = "";
		String dbPrefix = "";
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
		    siteUrl = ini.get("JDBCConfig", "siteUrl");
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
		} catch(IOException ioe) {
			log.error("Exception in retrieveAvatarInfo " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in retrieveAvatarInfo " + e.toString());
			throw e;
		}
		//String query = "select avatar_salt, avatar_gallery_path, avatar_path " + 
		String query = "SELECT config_value " + 
		"from " + dbPrefix + "config " +
		"WHERE config_name = \'avatar_salt\'";
		log.debug("Query " + query);
		/**
		 * Define the connection, preparedStatement and resultSet parameters
		 */
		String avatar_salt = "";
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			/**
			 * Open the connection
			 */
			connection = dataSource.getConnection();
			/**
			 * Prepare the statement
			 */
			preparedStatement = connection.prepareStatement(query);
			/**
			 * Execute the statement
			 */
			resultSet = preparedStatement.executeQuery();
			/**
			 * Extract data from the result set
			 */
			if(resultSet.next())
			{
				avatar_salt = resultSet.getString("config_value");
				log.debug("config_value = "  + avatar_salt);
			}
			return avatar_salt;
		} catch (SQLException e) {
			e.printStackTrace();
			log.error("SQLException : " + e.toString());
		} catch (Exception e) {
			log.error("Exception in retrieveAvatarInfo " + e.toString());
			throw e;
		}
		finally {
			try {
				/**
				 * Close the resultSet
				 */
				if (resultSet != null) {
					resultSet.close();
				}
				/**
				 * Close the preparedStatement
				 */
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				/**
				 * Close the connection
				 */
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				/**
				 * Handle any exception
				 */
				e.printStackTrace();
			}
		}
		return null;
	}

	public PlayerProfile selectPlayerProfile(int id) throws Exception{
		
    	String siteUrl = "";
		String dbPrefix = "";
		String useIntegrations = "Y";
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
		    siteUrl = ini.get("JDBCConfig", "siteUrl");
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
			useIntegrations = ini.get("JDBCConfig", "useIntegrations");
		} catch(IOException ioe) {
			log.error("Exception in selectPlayerProfile " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in selectPlayerProfile " + e.toString());
			throw e;
		}
		
		int userPhpBB3Id = id;
		if (useIntegrations.equals("Y")) {
			userPhpBB3Id = WalletAdapter.getUserExternalId(String.valueOf(id));
			log.debug("Inside selectPlayerProfile for userPhpBB3Id #" + String.valueOf(userPhpBB3Id));
		}
		
		String query = "select user_id,  group_id , user_avatar, user_avatar_type, " + 
		"username, user_rank, user_posts from " + dbPrefix + "users " +
		" where user_id = ?";
		log.debug("Query " + query);
		/**
		 * Define the connection, preparedStatement and resultSet parameters
		 */
		
		String avatar_salt = retrieveAvatarInfo();
		
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		ResultSet resultSet = null;
		try {
			/**
			 * Open the connection
			 */
			connection = dataSource.getConnection();
			/**
			 * Prepare the statement
			 */
			preparedStatement = connection.prepareStatement(query);
			/**
			 * Bind the parameters to the PreparedStatement
			 */
			preparedStatement.setInt(1, userPhpBB3Id);
			/**
			 * Execute the statement
			 */
			resultSet = preparedStatement.executeQuery();
			PlayerProfile playerProfile = null;
			/**
			 * Extract data from the result set
			 */
			if(resultSet.next())
			{
				playerProfile = new PlayerProfile();
				
				playerProfile.setId(id);
				String avatar_location = "";
				String user_avatar = "";
				String avatar_type = "";
				int group_id = 0;
				group_id = resultSet.getInt("group_id"); 
				user_avatar = resultSet.getString("user_avatar");
				// check if group avatar
				log.debug("user_avatar " + user_avatar);		
				log.debug("id " + String.valueOf(userPhpBB3Id));	
				String user_avatar_path = "";;
	//			if (user_avatar.indexOf("g") == -1) {
				user_avatar_path = avatar_salt+"_"+String.valueOf(userPhpBB3Id);
	//			} else {
	//				user_avatar_path = avatar_salt+"_g"+String.valueOf(group_id);
	//			}
				log.debug("user_avatar_path" + user_avatar_path);
				String fileExtension = "";
			      int dotInd = user_avatar.lastIndexOf('.');

			      // if dot is in the first position,
			      // we are dealing with a hidden file rather than an extension
			      fileExtension =  (dotInd > 0 && dotInd < user_avatar.length()) ? user_avatar
			              .substring(dotInd + 1) : null;
			    log.debug("File Extension of avatar " + fileExtension);          
			      //avatar.driver.upload        
				avatar_type = resultSet.getString("user_avatar_type");
				log.debug("avatar_type = "  + avatar_type);
				
		//		if (avatar_type == 1) { //upload
					avatar_location = siteUrl + "/images/avatars/upload/" + user_avatar_path+"."+fileExtension;
					log.debug("avatar file name " + avatar_location);
		//		} else if (avatar_type == 2) {
					// nothing to do
		//		}
				String name = resultSet.getString("username");
				
				playerProfile.setPosts(resultSet.getInt("user_posts"));
				playerProfile.setGroupId(resultSet.getInt("group_id"));
				playerProfile.setName(name);
				playerProfile.setAvatar_location(avatar_location);
				playerProfile.setAvatar_type(avatar_type);
			}
			return playerProfile;
		} catch (SQLException e) {
			e.printStackTrace();
			log.error("SQLException : " + e.toString());
		} catch (Exception e) {
			log.error("Exception in selectPlayerProfile " + e.toString());
			throw e;
		}
		finally {
			try {
				/**
				 * Close the resultSet
				 */
				if (resultSet != null) {
					resultSet.close();
				}
				/**
				 * Close the preparedStatement
				 */
				if (preparedStatement != null) {
					preparedStatement.close();
				}
				/**
				 * Close the connection
				 */
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {
				/**
				 * Handle any exception
				 */
				e.printStackTrace();
			}
		}
		return null;
	}


}
