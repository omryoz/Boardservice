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
package com.board.games.dao.discuz;


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


public class DiscuzJdbcDAOImpl implements GenericDAO {

	private Logger log = Logger.getLogger(this.getClass());
	
	private DataSource dataSource;
	private boolean useEMoney = false;
   private static boolean oldSMFVersion = false;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	public PlayerProfile selectPlayerProfile(int id) throws Exception{
		
   	String siteUrl = "";
		String dbPrefix = "";
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
		    siteUrl = ini.get("JDBCConfig", "siteUrl");
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
		} catch(IOException ioe) {
			log.error("Exception in selectPlayerProfile " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in selectPlayerProfile " + e.toString());
			throw e;
		}
/*
  engine4_users 
  engine4_storage_files
salt = 1118652
password = 81fd4e410898be1ae669b9fee1879f4f
email

posts
username
user_id


select pp_member_id,  avatar_location, avatar_type, 
b.name
from ipb_profile_portal a
join ipb_members as b
on
a.pp_member_id=b.member_id
and b.member_id = 1
*
		String query = "select pp_member_id,  avatar_location, avatar_type, " +
		" avatar_size from " + dbPrefix + "profile_portal " +
		" where pp_member_id =?";
		
		select id_member, file_hash, fileext, id_attach from smf_attachments
		
* */


		String query = "";
		if (oldSMFVersion) {
/*			query = "select pp_member_id,  avatar_location, avatar_type, " + 
			"name, member_group_id, posts from " + dbPrefix + "profile_portal a " + 
			" join " + dbPrefix + "members as b on a.pp_member_id=b.member_id " +
			" and b.member_id = ?";*/
		}
		else { 	
			query = "select user_id, type, storage_path, extension, name " + 
			" from " + dbPrefix + "engine4_storage_files a " + 
			" join " + dbPrefix + "engine4_users as b on a.user_id=b.user_id " +
			" and user_id = ? and b.type is null";
		}
		
		log.error("Query " + query);
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
			preparedStatement.setInt(1, id);
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
				playerProfile.setId(resultSet.getInt("user_id"));
				String avatar_location = "";
				String avatar_type = "";
				String storage_path = "";
				int idAttach = -1;
				String fileext = "";
				if (oldSMFVersion) {
/*					avatar_location = resultSet.getString("avatar_location");
					avatar_type = resultSet.getString("avatar_type");
					log.debug("avatar_location = "  + avatar_location);
					log.debug("avatar_type = "  + avatar_type);
					
					if (avatar_type.equals("upload")) {
						avatar_location = siteUrl + "/uploads/" + avatar_location;
					} else if (avatar_type.equals("url")) {
						// nothing to do
					}*/
				} else {
					storage_path = resultSet.getString("storage_path");
				//	fileext = resultSet.getString("fileext");
				//	idAttach = resultSet.getInt("id_attach");
					if (storage_path != null && !storage_path.equals("")) {
						avatar_location = siteUrl + "/" + storage_path;
					}
				}
				log.debug("url " + avatar_location);
				
				String name = resultSet.getString("name");
				//playerProfile.setPosts(resultSet.getInt("posts"));
				//playerProfile.setGroupId(resultSet.getInt("member_group_id"));
				playerProfile.setName(name);
				
				playerProfile.setAvatar_location(avatar_location);
				
/*				
				avatar_location = resultSet.getString("avatar_location");
				avatar_type = resultSet.getString("avatar_type");
				log.debug("avatar_location = "  + avatar_location);
				log.debug("avatar_type = "  + avatar_type);
				
				if (avatar_type.equals("upload")) {
					avatar_location = siteUrl + "/uploads/" + avatar_location;
				} else if (avatar_type.equals("url")) {
					// nothing to do
				}
*/				
				
				//playerProfile.setPosts(resultSet.getInt("posts"));
				//playerProfile.setGroupId(resultSet.getInt("member_group_id"));
				playerProfile.setName(name);
				playerProfile.setAvatar_location(avatar_location);
				//playerProfile.setAvatar_type(avatar_type);
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

