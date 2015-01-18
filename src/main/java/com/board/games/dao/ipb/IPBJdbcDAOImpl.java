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

package com.board.games.dao.ipb;



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


public class IPBJdbcDAOImpl implements GenericDAO {

	private Logger log = Logger.getLogger(this.getClass());
	
	private DataSource dataSource;
	private boolean useEMoney = false;
    private static boolean oldIPBVersion = false;
    private static boolean newIPB4Version = false;
	
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}


	public PlayerProfile selectPlayerProfile(int id) throws Exception{
		
    	String siteUrl = "";
		String dbPrefix = "";
		String ipbVersion = "";
		String useIntegrations = "Y";		
		log.debug("Inside selectPlayerProfile for id #" + String.valueOf(id));
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
		    siteUrl = ini.get("JDBCConfig", "siteUrl");
			dbPrefix = ini.get("JDBCConfig", "dbPrefix");
			useIntegrations = ini.get("JDBCConfig", "useIntegrations");
			ipbVersion = ini.get("JDBCConfig", "ipbVersion");
			if (!ipbVersion.equals("") && "IPS4".equals(ipbVersion.toUpperCase())) {
				newIPB4Version = true;
				log.debug("Detecting  IPS4 versionx");
			}
			
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

		
		int userIPBId = id;
		if (useIntegrations.equals("Y")) {
			userIPBId = WalletAdapter.getUserExternalId(String.valueOf(id));
			log.debug("Inside selectPlayerProfile for userIPBId #" + String.valueOf(userIPBId));
		}

		if (!ipbVersion.equals("") && "IPB3.1".equals(ipbVersion.toUpperCase())) {
			oldIPBVersion = true;
			log.debug("Detecting old IPB version 3.1.x");
		} 

		String query = "";
		if (oldIPBVersion) {
			query = "select pp_member_id,  avatar_location, avatar_type, " + 
			"name, member_group_id, posts from " + dbPrefix + "profile_portal a " + 
			" join " + dbPrefix + "members as b on a.pp_member_id=b.member_id " +
			" and b.member_id = ?";
		}
		else { 	
			if (!newIPB4Version) {
				query = "select pp_photo_type, pp_gravatar, pp_main_photo, pp_thumb_photo, pp_member_id,  avatar_location, avatar_type, " + 
				"name, member_group_id, posts, pfc.field_6 from " + dbPrefix + "profile_portal a " + 
				" join " + dbPrefix + "members as b on a.pp_member_id=b.member_id " +
				" join " + dbPrefix + "pfields_content pfc on pfc.member_id = b.member_id" + 
				" where b.member_id = ? " ;
	
			} else {// phototype = custom
				query = "select members_seo_name, pp_cover_photo, member_id, name, " +
			    " member_group_id, member_posts, pp_main_photo, pp_thumb_photo, pp_gravatar, " + 
				" pp_photo_type, tc_photo, fb_photo, fb_photo_thumb " + 
				" from " + dbPrefix + "core_members cm " +
				" where cm.member_id = ?";				
			}
		}
		
	log.debug("Query " + query);
	log.debug("User id to query " + userIPBId);
		//log.debug("Inside selectPlayerProfile : query " + query);
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
			preparedStatement.setInt(1, userIPBId);
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
				String avatar_location = "";
				String avatar_type = "";
				if (oldIPBVersion) {
					playerProfile.setId(resultSet.getInt("pp_member_id"));
					avatar_location = resultSet.getString("avatar_location");
					avatar_type = resultSet.getString("avatar_type");
					log.debug("avatar_type = "  + avatar_type);
					
					if (avatar_type != null && avatar_type.equals("upload")) {
						avatar_location = siteUrl + "/uploads/" + avatar_location;
	//					log.debug("avatar_location = "  + avatar_location);
					} else if (avatar_type != null && avatar_type.equals("url")) {
						// nothing to do
					}
					playerProfile.setPosts(resultSet.getInt("posts"));
					
				} else {
					if (!newIPB4Version) {
						playerProfile.setId(resultSet.getInt("pp_member_id"));
						avatar_type = resultSet.getString("pp_photo_type");
						String imageUrl = resultSet.getString("pp_main_photo");
						String gravatarEmail = resultSet.getString("pp_gravatar");
						String location = resultSet.getString("pfc.field_6");
						log.debug("avatar_type : " + avatar_type);
						log.debug("imageUrl : " + imageUrl);
						log.debug("gravatarEmail : " + gravatarEmail);
						log.debug("location : " + location);
						playerProfile.setLocation(location);
						if (avatar_type != null && !avatar_type.equals("") && avatar_type.equals("gravatar")) {
//							if (imageUrl != null && !imageUrl.equals("")) {
								// no need to use avatar api since ipb saves the work by giving it
								//String url = gravatar.getUrl(gravatarEmail);
		//						avatar_location = imageUrl.substring(0, imageUrl.indexOf('?'));
								// no longer trues
								Gravatar gravatar = new Gravatar();
								gravatar.setSize(50);
								gravatar.setRating(GravatarRating.GENERAL_AUDIENCES);
								gravatar.setDefaultImage(GravatarDefaultImage.IDENTICON);

								if (gravatarEmail != null && !gravatarEmail.equals("")) {
									String url = gravatar.getUrl(gravatarEmail);
									avatar_location = url.substring(0, url.indexOf('?'));
								} 							
//							}
						} else {
							if (imageUrl != null && !imageUrl.equals("")) {
								avatar_location = siteUrl + "/uploads" + "/" + imageUrl;
							} else {
								avatar_location = siteUrl + "/uploads" + "/" + "novatar.png";
							}
						}	
						playerProfile.setPosts(resultSet.getInt("posts"));
						
					} else {
						playerProfile.setId(resultSet.getInt("member_id"));
						String imageUrl = resultSet.getString("pp_main_photo");
						avatar_location = imageUrl;
						playerProfile.setPosts(resultSet.getInt("member_posts"));
						
					}
				}

				
				log.debug("url " + avatar_location);
				
				String name = resultSet.getString("name");
				int groupId = resultSet.getInt("member_group_id");
				playerProfile.setGroupId(groupId);
				playerProfile.setVip((groupId == 7) ? true : false);
				
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
