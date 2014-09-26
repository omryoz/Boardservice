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

package com.board.games.service.jl;

import java.io.UnsupportedEncodingException;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.board.games.dao.GenericDAO;
import com.board.games.handler.smf.SMFPokerLoginServiceImpl;
import com.board.games.model.PlayerProfile;
import com.board.games.service.BoardService;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.google.inject.Singleton;

@Singleton
public class JLBoardService implements BoardService {

	private SMFPokerLoginServiceImpl loginServiceImpl = new SMFPokerLoginServiceImpl();
	
	private Logger log = Logger.getLogger(this.getClass());
    private static ApplicationContext applicationContext;
    private static boolean useLinux = false;
    
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}	
	
	public void init(ServiceContext con) throws SystemException { 
		log.debug("**** Inside Service Initialized");
		if (useLinux)
		    applicationContext = new FileSystemXmlApplicationContext(LINUX_APP_CTX_CONFIG_FILE);
		else
			applicationContext = new FileSystemXmlApplicationContext(WINDOWS_APP_CTX_CONFIG_FILE);
	
	}
	
	@Override
	public ClientServiceAction onAction(ServiceAction action) {
		log.debug("Inside onAction of SMFBoardService : NEW multiple avatar query");
		try {
			// Assume the client sent an UTF-8 encoded string
			String stringDataFromClient = new String(action.getData(),"UTF-8");
			log.debug("Receiving request data from client " + stringDataFromClient);
			
			 StringTokenizer st=new StringTokenizer(stringDataFromClient,";");
			 int request =Integer.parseInt(st.nextToken());
			 String stringDataToClient = null;
			 PlayerProfile playerProfile = null;
			 String playerId = null;
			 switch (request) {
			 // may need here since client may send directly the current win after each hand or triggered on server in listener
			 case 1 :
				break;
				//FIXME: Refactoring to do
			 case 2 :
				 playerId =(new String(st.nextToken()));
					log.debug("Retrieving avatar info for user " + playerId);
					// need the user id to query player at lobby level to get their avatar if one set
					playerProfile= getUserProfile(Integer.parseInt(playerId));
					stringDataToClient = "2;"+playerId+";"+playerProfile.getAvatar_location(); 
					break;
			 case 3 :
				 String userList = "";
				 stringDataToClient = "3;";
				 //FIXME: to do one sql query later instead of  multiple : quick and dirty to quick test
				 while(st.hasMoreTokens()) {
					 playerId = (String) st.nextToken();
					 log.debug("Processing player #" + playerId);
					 playerProfile = getUserProfile(Integer.parseInt(playerId));
					 userList += playerId+";"+ playerProfile.getAvatar_location()+";"+playerProfile.getName();
					 if (st.hasMoreElements()) {
						 userList += ";";
					 }
				}
				log.debug("userList " + userList);
			    stringDataToClient = "3;"+userList;
				log.debug("stringDataFromClient " + stringDataFromClient);
				break;
			default:
				log.debug("Undefined request");
				break;
				 
			 }
			log.debug("sending data to client: " + stringDataToClient);
			// set up action with data to send to the client
			ClientServiceAction dataToClient;
			dataToClient = new ClientServiceAction(action.getPlayerId(), action.getSeq(), stringDataToClient.getBytes("UTF-8"));
			log.debug("data to client: " + dataToClient);
			return dataToClient;
		} catch (UnsupportedEncodingException use) {
			log.error("Error processing client data", use);
			
		} catch (Exception e) {
			log.error("Error processing client data", e);
		}
		return null;
	}
	
	
	@Override	
    public PlayerProfile getUserProfile(int userId) throws Exception
    {
		if (getApplicationContext() == null) {
			if (useLinux)
			    applicationContext = new FileSystemXmlApplicationContext(LINUX_APP_CTX_CONFIG_FILE);
			else
				applicationContext = new FileSystemXmlApplicationContext(WINDOWS_APP_CTX_CONFIG_FILE);
		}
		if (getApplicationContext() != null) {
	       GenericDAO genericDAO = (GenericDAO) getApplicationContext().getBean("smfDAO");
	       PlayerProfile playerProfile = genericDAO.selectPlayerProfile(userId);
	       log.debug("Avatar location " + playerProfile.getAvatar_location() + " Name " + playerProfile.getName());
	       return playerProfile;
		}
		return null;
    }	
    	

	@Override
	public LoginHandler locateLoginHandler(LoginRequestAction req) {
		return loginServiceImpl;
	}
	    

	
}
