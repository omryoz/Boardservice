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

/*	public void setServerCfg(ServerConfig serverCfg) {
		this.serverCfg = serverCfg;
	}*/
}
