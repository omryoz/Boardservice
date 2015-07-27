/**
 * This file is part of Boardservice.
 * Boardservice is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * Boardservice is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Boardservice. If not, see <http://www.gnu.org/licenses/>.
 */
package com.board.games.handler.generic;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.board.games.config.ServerConfig;
import com.board.games.service.wallet.WalletAdapter;

public class PokerConfigHandler {
	private ServerConfig serverCfg=null;
	private Logger log = Logger.getLogger(this.getClass());

	protected void initialize() {
		String currency = "USD";
		String walletBankAccountId = "2";
		String initialAmount = "5000";
		String useIntegrations = "Y";
		String version = "1.0";
		
		try {
			Ini ini = new Ini(new File("JDBCConfig.ini"));
			currency = ini.get("JDBCConfig", "currency");
			walletBankAccountId = ini.get("JDBCConfig", "walletBankAccountId");
			initialAmount = ini.get("JDBCConfig", "initialAmount");
			useIntegrations = ini.get("JDBCConfig", "useIntegrations");
			version = ini.get("JDBCConfig", "version");	
			log.warn("Initial amount : " + initialAmount);
			log.warn("UseIntegrations : " + useIntegrations);
			log.warn("currency : " + currency);
			serverCfg = new ServerConfig(currency, new Long(walletBankAccountId), new BigDecimal(initialAmount), useIntegrations.equals("Y")? true:false,version);
		} catch (IOException ioe) {
			log.error("Exception in init " + ioe.toString());
		} catch (Exception e) {
			log.error("Exception in init " + e.toString());
		}
	}


	
	public ServerConfig getServerCfg() {
		return serverCfg;
	}

	protected String authenticateBot(String user, String uId, String socialAvatar, ServerConfig serverConfig, boolean checkAge, int authTypeId, boolean needAgeAgreement ) throws Exception {
		if (serverConfig != null) {	
			if (serverConfig.isUseIntegrations()) {
				
				WalletAdapter walletAdapter = new WalletAdapter();
				log.debug("Calling createWalletAccount");
				Long userId = walletAdapter.checkCreateNewUser(uId, user,  socialAvatar, new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge, needAgeAgreement, authTypeId);
				
				if (userId < 0 ) {
					log.debug("Player did did not accept age clause");
					// user did not accept age clauses
					return "-5";
				}
				log.debug("assigned new id as #" + String.valueOf(userId));
				return String.valueOf(userId);
	/*						if (posts >= 1) {
						return String.valueOf(member_id);
					} else {
						log.error("Required number of posts not met, denied login");
						return "-2";
					}
	*/
			} else {
				return String.valueOf(uId);
			}
		
		} else {
			log.error("ServerConfig is null.");
		}						
		return "-3";
	
	}	
	
	protected String authenticateSocialNetwork(String user, String uId, String socialAvatar, ServerConfig serverConfig, boolean checkAge, int authTypeId, boolean needAgeAgreement ) throws Exception {
		if (serverConfig != null) {	
			if (serverConfig.isUseIntegrations()) {
				
				WalletAdapter walletAdapter = new WalletAdapter();
				log.debug("Calling createWalletAccount");
				Long userId = walletAdapter.checkCreateNewUser(uId, user,  socialAvatar, new Long(1), serverConfig.getCurrency(), serverConfig.getWalletBankAccountId(), serverConfig.getInitialAmount(),checkAge, needAgeAgreement, authTypeId);
				
				if (userId < 0 ) {
					log.debug("Player did did not accept age clause");
					// user did not accept age clauses
					return "-5";
				}
				log.debug("assigned new id as #" + String.valueOf(userId));
				return String.valueOf(userId);
	/*						if (posts >= 1) {
						return String.valueOf(member_id);
					} else {
						log.error("Required number of posts not met, denied login");
						return "-2";
					}
	*/
			} else {
				return String.valueOf(uId);
			}
		
		} else {
			log.error("ServerConfig is null.");
		}						
		return "-3";
	
	}
	

	
}
