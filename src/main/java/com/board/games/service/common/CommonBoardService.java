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
package com.board.games.service.common;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.ini4j.Ini;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.board.games.dao.GenericDAO;
import com.board.games.model.PlayerProfile;
import com.board.games.service.BoardService;
import com.cubeia.firebase.api.action.GameDataAction;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.io.ProtocolObject;
import com.cubeia.firebase.io.StyxSerializer;
import com.cubeia.firebase.io.protocol.ProtocolObjectFactory;
import com.cubeia.firebase.io.protocol.ServiceTransportPacket;
import com.cubeia.games.poker.io.protocol.AchievementNotificationPacket;
import com.cubeia.games.poker.routing.service.io.protocol.PokerProtocolMessage;
import com.cubeia.games.poker.util.ProtocolFactory;
import com.google.gson.Gson;
import com.google.inject.Singleton;

@Singleton
public class CommonBoardService implements BoardService {
   StyxSerializer styxDecoder = new StyxSerializer(new ProtocolObjectFactory());

	private Logger log = Logger.getLogger(this.getClass());
    private static ApplicationContext applicationContext;
    //private static boolean useLinux = true;
    private String boardConfigFile = "";
    private String botAvatarUrl = "";
    private String noAvatarUrl = "";
    private String daoConfig = "xDAO";
    
	public ApplicationContext getApplicationContext() {
		return applicationContext;
	}	
		
	public void init(ServiceContext con) throws SystemException { 
		log.debug("**** Inside CommonBoardService Initialized");
		try {
			Ini ini = new Ini(new File("PokerConfig.ini"));
			String boardConfigFile = ini.get("PokerConfig", "boardConfigFile");
			log.debug("**** CommonBoardService Initialized with config file as : " + boardConfigFile + "****");

			if (boardConfigFile == null || boardConfigFile.equals("")) {
				 log.error("boardConfigFile is null or empty");
				 throw(new Exception("BoardConfigFile cannot be null or empty"));
			}
			
			botAvatarUrl = ini.get("PokerConfig", "botAvatarUrl");
			noAvatarUrl = ini.get("PokerConfig", "noAvatarUrl");
			log.debug("**** CommonBoardService Initialized with botAvatarUrl as : " + botAvatarUrl + "****");
			
			applicationContext = new FileSystemXmlApplicationContext(boardConfigFile);
			if (applicationContext == null) {
				 log.error("Application context of board service cannot be null");
				throw(new Exception("Application context of board service cannot be null"));
			}
		} catch (IOException ioe) {
			log.error("Exception in init " + ioe.toString());
			//System.err.println("Inside configure :PokerConfig.ini not  found" + ioe.toString());
		} catch (Exception e) {
			//System.err.println("Inside configure : other exception" + e.toString());
			log.error("Exception in init " + e.toString());
		}	
	}

    public ProtocolObject unpack(ServiceTransportPacket packet) {
        // Create the user packet
        ProtocolObject servicePacket = null;
        try {
        	servicePacket = styxDecoder.unpack(ByteBuffer.wrap(packet.servicedata));
        } catch (IOException e) {
            log.error("Could not unpack gamedata", e);
        }
        return servicePacket;
    }		
    // from client,we receive JSON (var jsonString=FIREBASE.Styx.toJSON(protocolObject);
	@Override
	public ClientServiceAction onAction(ServiceAction action) {
		String stringDataFromClient = "";
		String parsedServiceData = "";
		String clientRequest = "";
		log.debug("Inside onAction of CommonBoardService : tableId " + action.getTableId() + " playerid " + action.getPlayerId());
		try {
			
			
		// Assume the client sent an UTF-8 encoded string
		 stringDataFromClient = new String(action.getData(),"UTF-8");
		 log.debug("Receiving request data from client " + stringDataFromClient);
		try {
		JSONObject json = (JSONObject) JSONSerializer.toJSON(action);
		log.debug("json " + json.toString());
		String data = json.getString( "data" );
		log.debug("data " + data);
		
		
		//String str = "[255,12,87,183,232,34,64,121,182,23]";
		Gson gson = new Gson();
		byte[] parsed = gson.fromJson(data, byte[].class);
		
		
		int count = 0;
		int idx = 0;
		int ref =0;
		StringBuffer sb = new StringBuffer();
		for (byte b : parsed) {
			idx++;
		    //log.debug((char)b);
		    char val = (char)b;
			if (idx >7 )
		    	  sb.append(val);
		    
		}		
		//log.debug("count " + count);
		// TO DBG: activate trace of detail array below
		log.debug("sb " + sb.toString());
		 clientRequest = 	sb.toString();
		for (byte b : parsed) {
		//	log.debug((char)b);
		}		
		
		
		
/*		 parsedServiceData = new String(parsed, "UTF-8");
		 
		 
		 System.out.println("parsedServiceData " + parsedServiceData);
		 */
		 

		} catch (Exception e) {
			log.error("Exception JSOn  " + e.toString());
		}
/*		 StringTokenizer requestSt2=new StringTokenizer(parsedServiceData,"?");
		 String clientRequest2= requestSt2.nextToken();
		 System.out.println("clientRequest2 " + clientRequest2);
		 StringTokenizer requestSt=new StringTokenizer(parsedServiceData,"?");
		 String clientRequest= requestSt.nextToken(); */
		log.debug("clientRequest " + clientRequest);
		 StringTokenizer st=new StringTokenizer(clientRequest,";");
		 String requestData = st.nextToken().trim();
			 
		 int request =Integer.parseInt(requestData);
		 log.debug("received requestData " + request);
		 String stringDataToClient = null;
		 PlayerProfile playerProfile = null;
		 String playerId = null;
		 String tableId = null;
		 String achievementId = null;
		 String avatar_location = "";
		 boolean bonusHandling = false;
		 ClientServiceAction dataToClient = null;
		 
		 switch (request) {

		 case 2 :
			 tableId =(new String(st.nextToken()));
				log.debug("Retrieving table id for table " + tableId);
				if (st.hasMoreTokens()) {
					 playerId =(new String(st.nextToken()));
					 playerId = playerId.trim();
						log.debug("Retrieving table id for table " + tableId + " and playerId " + playerId);
						// need the user id to query player at lobby level to get their avatar if one set
						playerProfile= getUserProfile(Integer.parseInt(playerId));
						
				}
				if (playerProfile != null) {
					log.debug("Found userid " + playerId);
					if (playerProfile.getAvatar_location().equals("")) {
						avatar_location = noAvatarUrl;
					} else {
						avatar_location = playerProfile.getAvatar_location();
					}
				} else {
					avatar_location = botAvatarUrl;
					log.debug("Player profile is null using bot avatar url as : " + botAvatarUrl);
				}
				log.debug("avatar_location " + avatar_location);
				stringDataToClient = "2;"+tableId+";"+playerId+";"+avatar_location; 
				
		
				log.debug("sending data to client: " + stringDataToClient);
				// set up action with data to send to the client
				
		//			log.debug("String from json array"+parsedServiceData);

				
				dataToClient = new ClientServiceAction(action.getPlayerId(), action.getSeq(), stringDataToClient.getBytes("UTF-8"));
				log.debug("data to client: " + dataToClient);
								
				break;
				//FIXME
		 case 3 :
			 log.debug("Processing notification");
			 tableId =(new String(st.nextToken()));
				log.debug("Retrieving table id for table " + tableId);
				if (st.hasMoreTokens()) {
					 playerId =(new String(st.nextToken()));
					 playerId = playerId.trim();
						log.debug("Retrieving table id for table " + tableId + " and playerId " + playerId);
						if (st.hasMoreTokens()) {
							 achievementId =(new String(st.nextToken()));
							 achievementId = achievementId.trim();
								log.debug("Retrieving achievement id for table " + tableId + " and playerId " + playerId + " as #" + achievementId);
// lookup for achievements image url and it's description to send back to player
								bonusHandling = true;
								
								AchievementNotificationPacket notification = new AchievementNotificationPacket();
								notification.playerId = Integer.parseInt(playerId);
								notification.message = "http://localhost/testimg.jpg";
								
								
								ProtocolFactory factory = new ProtocolFactory();
								GameDataAction gameDataAction = factory.createGameAction(notification, Integer.parseInt(playerId), Integer.parseInt(tableId));
			//					if (wrapper.broadcast) {
								log.info("Notify all players at table["+tableId+"] with event ["+notification.message+"] for player["+playerId+"]");
					//			table.getNotifier().notifyAllPlayers(action);
				//				} else {
								log.info("Notify player["+playerId+"] at table["+tableId+"] with event ["+notification.message+"]");
					//			table.getNotifier().notifyPlayer(playerId, gameDataAction);
						//		}
								
								ByteBuffer notificationData = styxDecoder.pack(notification);
					//			log.debug("notificationData buffer" + notificationData);
								PokerProtocolMessage msg = new PokerProtocolMessage(notificationData.array());
								ByteBuffer msgData = styxDecoder.pack(msg);
								log.debug("msgData buffer" + msgData);
								dataToClient = new ClientServiceAction(Integer.parseInt(playerId), action.getSeq(), msgData.array());
								String achivementUrl = "http://localhost/test.gif";
								stringDataToClient = "3;"+tableId+";"+playerId+";"+achivementUrl; 
								
							//	dataToClient = new ClientServiceAction(Integer.parseInt(playerId), action.getSeq(), stringDataToClient.getBytes("UTF-8"));

						//		log.debug("msgData " + msgData.array());
						//	log.debug("data to client: " + stringDataToClient);
								return dataToClient;
								
/*
       StyxSerializer styx = new StyxSerializer(null);
        PongPacket pongPacket = new PongPacket(identifier);
        GameDataAction gameDataAction = new GameDataAction(playerId, table.getId());
        gameDataAction.setData(styx.pack(pongPacket));
        table.getNotifier().sendToClient(pokerPlayer.getId(), gameDataAction);
    private void sendToPlayer(BonusEventWrapper wrapper) throws IOException {
		int playerId = wrapper.playerId;
		
		AchievementNotificationPacket notification = new AchievementNotificationPacket();
		notification.playerId = playerId;
		notification.message = wrapper.event;
		
		ByteBuffer notificationData = serializer.pack(notification);
		PokerProtocolMessage msg = new PokerProtocolMessage(notificationData.array());
		ByteBuffer msgData = serializer.pack(msg);
		
		ClientServiceAction action = new ClientServiceAction(playerId, 0, msgData.array());
		log.debug("Send bonus event as client action: "+action);
		router.getRouter().dispatchToPlayer(playerId, action);

	}

								
 */
						}
						
				}


				break;	
				default:
			log.error("Undefined request");

			break;
			 
		 }
			return dataToClient;
	
//		ServiceTransportPacket stp = (ServiceTransportPacket) Base64.decodeBase64(action.getData()));
		
/*		System.out.println("actiondata decoded =  " + actiondata);	
		try {
			ProtocolObject packet = unpack(actiondata);
			if (packet != null) {
				if (packet instanceof ServiceTransportPacket) {
				System.out.println("Found *********************************** ServiceTransportPacket");
				}
			}*/
/*	
 * 
 * 
case FB_PROTOCOL.ServiceTransportPacket.CLASSID :
            var valueArray =  FIREBASE.ByteArray.fromBase64String(packet.servicedata);
            // wrap in a ByteArray
            var gameData = new FIREBASE.ByteArray(valueArray);
            // read past the length
            var length = gameData.readInt();
            // get classId of gameData
            var classId = gameData.readUnsignedByte();
            var serviceProtocolObject = YOUR_PROTOCOL.ProtocolObjectFactory.create(classId, gameData);
            this.handleServiceData(serviceProtocolObject);
            break; 
 * 
 * 		ProtocolObject protocol = unpack(action);
			if (protocol != null) {
				if (protocol instanceof ServiceTransportPacket) {
					ServiceTransportPacket serviceTransportPacket = (ServiceTransportPacket) protocol;
					String servicedata = new String(Base64.decodeBase64(serviceTransportPacket.servicedata));
					//String decodedBase64 = new String(Base64.decodeBase64(stringDataFromClient));
					System.out.println("servicedata =  " + servicedata);			
				} else {
					System.out.println("protocol is other than ServiceTransportPacket");
				}
			} else {
				System.out.println("protocol is null");	
			}*/
			// Assume the client sent an UTF-8 encoded string
	/*		String stringDataFromClient = new String(action.getData(),"UTF-8");
			log.debug("Receiving request data from client " + stringDataFromClient);
			System.out.println("Receiving request data from client " + stringDataFromClient);
			 StringTokenizer st=new StringTokenizer(stringDataFromClient,";");
			 int request =Integer.parseInt(st.nextToken());
	*/		 
			 // processs only tableId in serveicedata and request code

/*			
			 StringTokenizer st=new StringTokenizer(stringDataFromClient,";");
			 int request =Integer.parseInt(st.nextToken());
			 String stringDataToClient = null;
			 PlayerProfile playerProfile = null;
			 String playerId = null;
			 System.out.println("onAction: request " + String.valueOf(request));
			 switch (request) {
			 // may need here since client may send directly the current win after each hand or triggered on server in listener
			 case 1 :
				 int id =(new Integer(st.nextToken()));
				 int balance =(new Integer(st.nextToken()));
				log.debug("Saving player balance of " + balance + " for player #" + id);
				resetUserBalance(id, balance);
				stringDataToClient = "1;"+String.valueOf(id)+";"+"Saving balance of "+String.valueOf(balance);
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
			 case 4 :
				 playerId =(new String(st.nextToken()));
					log.debug("Set initial amount for user if none found " + playerId);
		    	   long moneyAmount = 0;
		    		   try {
						moneyAmount= getUserBalance(Integer.parseInt(playerId));
						if (moneyAmount == -1) {
							// balance not added , create default balance
							insertUserBalance(Integer.parseInt(playerId));
							moneyAmount= getUserBalance(Integer.parseInt(playerId));
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
					stringDataToClient = "4;"+playerId+";"+Long.toString(moneyAmount); 
					break;
			default:
				log.debug("Undefined request");
				break;
				 
			 }
			 
*/			
				

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
/*		if (getApplicationContext() == null) {
			if (useLinux)
			    applicationContext = new FileSystemXmlApplicationContext(LINUX_APP_CTX_CONFIG_FILE);
			else
				applicationContext = new FileSystemXmlApplicationContext(WINDOWS_APP_CTX_CONFIG_FILE);
		}*/	
		log.debug("getUserProfile " + userId);
		if (getApplicationContext() != null) {		
	       GenericDAO genericDAO = (GenericDAO) getApplicationContext().getBean(daoConfig);
	       PlayerProfile playerProfile = genericDAO.selectPlayerProfile(userId);
	       if (playerProfile != null) {
		       log.debug("Avatar location " + playerProfile.getAvatar_location() + " Name " + playerProfile.getName());
		       return playerProfile;
	       }
		} else {
			log.error("getApplicationContext() is null");
		}
		return null;       
    }	
    	
	@Override
	public LoginHandler locateLoginHandler(LoginRequestAction request) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getDaoConfig() {
		return daoConfig;
	}

	public void setDaoConfig(String daoConfig) {
		this.daoConfig = daoConfig;
	}
}