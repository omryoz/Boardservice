
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

package com.board.games.dao.mybb;




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
public class MyBBJdbcDAOImpl implements GenericDAO {


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
			log.debug("use integrations " + (useIntegrations.equals("Y") ? "YES" : "NO"));
			
		} catch(IOException ioe) {
			log.error("Exception in selectPlayerProfile " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in selectPlayerProfile " + e.toString());
			throw e;
		}
		
		int userMyBBId = id;
		if (useIntegrations.equals("Y")) {
			userMyBBId = WalletAdapter.getUserExternalId(String.valueOf(id));
			log.debug("Inside selectPlayerProfile for userMyBBId #" + String.valueOf(userMyBBId));
		}
		
		String query = "select username, avatar from " + dbPrefix + "users a " +
				" where a.uid = ?";

		


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
			preparedStatement.setInt(1, userMyBBId);
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
				log.debug("id " + String.valueOf(userMyBBId));	
				String user_avatar_path = "";;
				String user_avatar = "";
				user_avatar = resultSet.getString("avatar");
				log.debug("user_avatar " + user_avatar);
				avatar_location = siteUrl + "/" + user_avatar;
					log.debug("avatar file name " + avatar_location);
					String name = resultSet.getString("username");
				

				playerProfile.setName(name);
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
