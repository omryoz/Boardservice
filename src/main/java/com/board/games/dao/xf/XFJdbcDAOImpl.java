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

package com.board.games.dao.xf;



import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jgravatar.Gravatar;
import jgravatar.GravatarDefaultImage;
import jgravatar.GravatarRating;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.board.games.dao.GenericDAO;
import com.board.games.model.PlayerProfile;
import com.board.games.service.wallet.WalletAdapter;


public class XFJdbcDAOImpl implements GenericDAO {

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
		    log.debug("siterurl " + siteUrl);
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
			useIntegrations = ini.get("JDBCConfig", "useIntegrations");
		} catch(IOException ioe) {
			log.error("Exception in selectPlayerProfile " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in selectPlayerProfile " + e.toString());
			throw e;
		}
/*
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
 * */
		int userXFId = id;
		if (useIntegrations.equals("Y")) {
			userXFId = WalletAdapter.getUserExternalId(String.valueOf(id));
			log.debug("Inside selectPlayerProfile for userXFId #" + String.valueOf(userXFId));
		}

		Gravatar gravatar = new Gravatar();
		gravatar.setSize(50);
		gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
		gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);
		
		String query = "select username, user_group_id, gravatar, avatar_date, message_count,"
			+ " avatar_width, avatar_height from xf_user "
			+ " where user_id = ?";
		log.debug("selectPlayerProfile Query " + query);
	
		log.debug("User id to query " + userXFId);
		
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
			preparedStatement.setInt(1, userXFId);
			/**
			 * Execute the statement
			 */
			resultSet = preparedStatement.executeQuery();
			PlayerProfile playerProfile = null;
			/**
			 * Extract data from the result set
			 */
			String avatar_location = siteUrl + "/noavatar.png";
			
			playerProfile = new PlayerProfile();
			if(resultSet.next()) {
				playerProfile.setId(id);
				//String avatar_type = "";
				
				int avw = resultSet.getInt("avatar_width");
				int avh = resultSet.getInt("avatar_height");
				int avd = resultSet.getInt("avatar_date");
				
				String gravatarEmail = resultSet.getString("gravatar");
				if (gravatarEmail != null && !gravatarEmail.equals("")) {
					String url = gravatar.getUrl(gravatarEmail);
					log.debug("Detected gravatar email: " + gravatarEmail + " url " + url);
					avatar_location = url.substring(0, url.indexOf('?'));
				} else {
					if (avw != 0 && avh != 0 && avd != 0) {
						avatar_location = siteUrl + "/data/avatars/m/0/" + id + ".jpg";
					}
				}

				
				String name = resultSet.getString("username");
				log.debug("url " + avatar_location + " for " + name);
				playerProfile.setPosts(resultSet.getInt("message_count"));
				playerProfile.setGroupId(resultSet.getInt("user_group_id"));
				playerProfile.setName(name);
				playerProfile.setAvatar_location(avatar_location);
				
			} else {
				playerProfile.setName("#" + id);
				playerProfile.setAvatar_location(avatar_location);
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
