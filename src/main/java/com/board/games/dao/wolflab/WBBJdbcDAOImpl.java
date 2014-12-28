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

package com.board.games.dao.wolflab;




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
public class WBBJdbcDAOImpl implements GenericDAO {


	private Logger log = Logger.getLogger(this.getClass());
	
	private DataSource dataSource;
	private boolean useEMoney = false;
	
 	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
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
		
		String query = "select ua.avatarID, ua.avatarName, ua.fileHash, ua.avatarExtension, u.username, u.wbbPosts  "  +
		" from " + dbPrefix + "user_avatar ua " +
		" inner join " + dbPrefix + "user u on ua.avatarID=u.avatarID " + 
		" where u.userID = ?";
		log.debug("Query " + query);
		/**
		 * Define the connection, preparedStatement and resultSet parameters
		 */
		
		
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
				String filehash = "";
				String user_avatar = "";
				String avatar_type = "";
				String extension = "";
				extension =  resultSet.getString("avatarExtension");
				filehash = resultSet.getString("fileHash");
				// check if group avatar
				log.debug("fileHash " + filehash);		
				String avatarId =  resultSet.getString("avatarID");
				//String.valueOf(userPhpBB3Id);
				log.debug("id " + avatarId);	
				String user_avatar_path = avatarId + "-" + filehash + "." + extension;
				
	//			if (user_avatar.indexOf("g") == -1) {
				user_avatar_path = user_avatar_path;
	//			} else {
	//				user_avatar_path = avatar_salt+"_g"+String.valueOf(group_id);
	//			}
				
				//C:\xampp\htdocs\bb4\wcf\images\avatars\f5
				log.debug("user_avatar_path" + user_avatar_path);
				String subdir = "";
				subdir= filehash.substring(0,2);
			    log.debug("subdir of avatar " + subdir);          
			      //avatar.driver.upload        
				
		//		if (avatar_type == 1) { //upload
					avatar_location = siteUrl + "/wcf/images/avatars/" + subdir + "/" + user_avatar_path;
					log.debug("avatar file name " + avatar_location);
		//		} else if (avatar_type == 2) {
					// nothing to do
		//		}
				String name = resultSet.getString("username");
				playerProfile.setPosts(resultSet.getInt("wbbPosts"));
				/*				
				playerProfile.setGroupId(resultSet.getInt("group_id"));
				playerProfile.setName(name);
*/				playerProfile.setAvatar_location(avatar_location);
	//			playerProfile.setAvatar_type(avatar_type);
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
