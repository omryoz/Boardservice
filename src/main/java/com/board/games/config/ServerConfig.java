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
package com.board.games.config;

import java.math.BigDecimal;

public class ServerConfig {

	
	private String currency = "USD";
	private Long walletBankAccountId; 
	private BigDecimal initialAmount = BigDecimal.ZERO;
	private boolean useIntegrations = false;
	private boolean useSubscriptions = false;
	
	public boolean isUseSubscriptions() {
		return useSubscriptions;
	}
	public void setUseSubscriptions(boolean useSubscriptions) {
		this.useSubscriptions = useSubscriptions;
	}
	private String version = "1.0";
	
	public ServerConfig(String currency, Long walletBankAccountId, BigDecimal initialAmount, boolean useIntegrations, String version) {
		this.currency = currency;
		this.walletBankAccountId = walletBankAccountId;
		this.initialAmount = initialAmount;
		this.useIntegrations = useIntegrations;
		this.version = version;
	}
	public String getCurrency() {
		return currency;
	}
	public void setCurrency(String currency) {
		this.currency = currency;
	}


	public boolean isUseIntegrations() {
		return useIntegrations;
	}
	public void setUseIntegrations(boolean useIntegrations) {
		this.useIntegrations = useIntegrations;
	}
	public BigDecimal getInitialAmount() {
		return initialAmount;
	}
	public void setInitialAmount(BigDecimal initialAmount) {
		this.initialAmount = initialAmount;
	}
	public Long getWalletBankAccountId() {
		return walletBankAccountId;
	}
	public void setWalletBankAccountId(Long walletBankAccountId) {
		this.walletBankAccountId = walletBankAccountId;
	}
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
}
