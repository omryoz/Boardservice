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
package com.board.games.dao.modx;


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


public class MODXJdbcDAOImpl implements GenericDAO {

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
  smf_members 
password_salt = 0682
passwd = 92ff6c5426a23d105af69f49eb9d0210972ecbca
id_member
posts
member_name


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
			query = "select id_member, file_hash, fileext, id_attach, posts " + 
			" from " + dbPrefix + "attachments a " + 
			" join " + dbPrefix + "members as b on a.id_member=b.member_id " +
			" and id_member = ?";
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
				playerProfile.setId(resultSet.getInt("id_member"));
				String avatar_location = "";
				String avatar_type = "";
				String fileHash = "";
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
					fileHash = resultSet.getString("fileHash");
					fileext = resultSet.getString("fileext");
					idAttach = resultSet.getInt("id_attach");
					if (fileHash != null && !fileHash.equals("")) {
						avatar_location = siteUrl + "/attachments/" + "/" + String.valueOf(idAttach) + "_" +  fileHash;
					}
				}
				log.debug("url " + avatar_location);
				
				String name = resultSet.getString("name");
				playerProfile.setPosts(resultSet.getInt("posts"));
				playerProfile.setGroupId(resultSet.getInt("member_group_id"));
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
				
				playerProfile.setPosts(resultSet.getInt("posts"));
				playerProfile.setGroupId(resultSet.getInt("member_group_id"));
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

