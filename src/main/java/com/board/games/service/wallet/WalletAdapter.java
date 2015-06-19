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
package com.board.games.service.wallet;

import java.math.BigDecimal;
import java.util.UUID;

import org.apache.log4j.Logger;
import static com.cubeia.backoffice.wallet.api.dto.Account.AccountType.STATIC_ACCOUNT;

import com.cubeia.backoffice.accounting.api.Money;
import com.cubeia.backoffice.users.api.dto.CreateUserRequest;
import com.cubeia.backoffice.users.api.dto.User;
import com.cubeia.backoffice.users.client.UserServiceClient;
import com.cubeia.backoffice.users.client.UserServiceClientHTTP;
import com.cubeia.backoffice.wallet.api.dto.CreateAccountResult;
import com.cubeia.backoffice.wallet.api.dto.MetaInformation;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionEntry;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionRequest;
import com.cubeia.backoffice.wallet.api.dto.report.TransactionResult;
import com.cubeia.backoffice.wallet.api.dto.request.CreateAccountRequest;
import com.cubeia.backoffice.wallet.client.WalletServiceClient;
import com.cubeia.backoffice.wallet.client.WalletServiceClientHTTP;
import java.util.HashMap;
import java.util.Map;

public class WalletAdapter {
	private Logger log = Logger.getLogger(getClass());
	
	private  static UserServiceClient userService;
	private WalletServiceClient walletService;

	private static final String EXTERNAL_USERNAME_ATTRIBUTE = "externalUsername";
	private static final String FB_AVATAR_URL = "FB_SOCIAL_AVATAR_URL";
	private static final String GOOGLEPLUS_AVATAR_URL = "GOOGLEPLUS_SOCIAL_AVATAR_URL";
	private static final String CURRENT_SOCIAL_NETWORK_TYPE = "CURRENT_SOCIAL_NETWORK_TYPE";

	private String userServiceUrl = "http://localhost:8080/user-service-rest/rest";
	
	private String walletServiceUrl = "http://localhost:8080/wallet-service-rest/rest";

	public WalletAdapter() {
		log.debug("Instantiate walletservice");
		walletService = new WalletServiceClientHTTP(walletServiceUrl);
		log.debug("instantiate userService");
		userService = new UserServiceClientHTTP(userServiceUrl);
		
	}

	public static int getUserExternalId(String userId) {
		User user = userService.getUserById(new Long(userId));
		if (user!=null) {
			String extId = user.getExternalUserId();
			return new Integer(extId);
		}
		return -1;
	}

	public Long checkCreateNewUser(String externalId, String name, String socialAvatar, Long operatorId, String currency, Long walletBankAccountId, BigDecimal initialAmount, boolean hasClauseAgreed, boolean needAgeAgreement, int authTypeId) {
		log.debug("Check if user exists - extId["+externalId+"] name["+name+"] opId["+operatorId+"]");
		User user =  userService.getUserByExternalId(externalId, operatorId);
		if (user == null) {
			if (needAgeAgreement) {
				if (!hasClauseAgreed) {
					log.debug("User does not exist and has not pass age verification (-9998) .");
					return new Long(-9998);
				}
			}
			log.debug("Migrating user for the first time: extId["+externalId+"] name["+name+"] opId["+operatorId+"]");
			// Username has to be unique for every operator id and the only unique constraint we 
			// enforce on external operators is on externalId. Thus we must use that one as usename in User-Service
			CreateUserRequest createUser = new CreateUserRequest(name, "", externalId, operatorId);
			user = userService.createUser(createUser).getUser();
			if (user!= null) {
				log.debug("Created user: " + user);
				user.getAttributes().put("AGE_ACCEPTANCE_REQUIRED_DONE", "1");
				// Always update attributes
				user.getAttributes().put(EXTERNAL_USERNAME_ATTRIBUTE, name);
				//FIXME: needs to differenicate different social network
				log.debug("Setting social network type " + String.valueOf(authTypeId) );
				user.getAttributes().put(CURRENT_SOCIAL_NETWORK_TYPE, String.valueOf(authTypeId));
				switch (authTypeId) {
					case 5:
						log.debug("Setting fb social network avatar " + socialAvatar );
						user.getAttributes().put(FB_AVATAR_URL, socialAvatar);
						break;
					case 6: 
						log.debug("Setting g+ social network avatar " + socialAvatar );
						user.getAttributes().put(GOOGLEPLUS_AVATAR_URL, socialAvatar);
						break;
					default:
						break;
					
				}
				//TODO ChangeUserStatusRequest
				// Verify age validation if fails, return error code
				userService.updateUser(user);	
				log.debug("Creating main account");
				createMainAccountForUser(user.getUserId(), name, currency, walletBankAccountId, initialAmount);
				return user.getUserId();
			} 
			else{
				log.error("Create user failed (null)");
			}
		} else { 
			String value = user.getAttributes().get("AGE_ACCEPTANCE_REQUIRED_DONE");
			if (value == null || (value != null && !value.equals("1"))) {
				if (hasClauseAgreed) {
					user.getAttributes().put("AGE_ACCEPTANCE_REQUIRED_DONE", "1");
					userService.updateUser(user);	
					return user.getUserId();
				}
				if (needAgeAgreement) {
					log.debug("User found but has not pass age verification (-9999).");
					return new Long(-9999);
				}
			}
			log.debug("User found and pass age check. Will not create new.");
			// Always update user authenticate
			// Always update attributes since user may have change avatar/name or user simply back and forth forum and social network
			// so previously was 1 , now must be updated
			log.debug("Setting social network name " + name );
			user.getAttributes().put(EXTERNAL_USERNAME_ATTRIBUTE, name);
			//FIXME: needs to differenicate different social network
			log.debug("Setting social network type " + String.valueOf(authTypeId) );
			user.getAttributes().put(CURRENT_SOCIAL_NETWORK_TYPE, String.valueOf(authTypeId));
			switch (authTypeId) {
				case 5:
					log.debug("Setting fb social network avatar " + socialAvatar );
					user.getAttributes().put(FB_AVATAR_URL, socialAvatar);
					break;
				case 6: 
					log.debug("Setting g+ social network avatar " + socialAvatar );
					user.getAttributes().put(GOOGLEPLUS_AVATAR_URL, socialAvatar);
					break;
				default:
					break;
				
			}			
			return user.getUserId();
		}
		
		return new Long("-999");
		
	}
	private void createMainAccountForUser(Long userId, String name, String currency, Long walletBankAccountId, BigDecimal initialAmount) {
		log.warn("Will create new account for user " + userId + " with amount as " + initialAmount);
		CreateAccountRequest req = createAccountRequest(userId, name, currency);
		CreateAccountResult result = walletService.createAccount(req);
		log.debug("Play money account for player " + userId + " created with ID: " + result.getAccountId());
		TransactionRequest treq = createInitialTransactionRequest(result.getAccountId(), currency, initialAmount, walletBankAccountId);

		try {
			log.debug("Calling wallet service doTransaction");
			TransactionResult tresult = walletService.doTransaction(treq);
			log.debug("New account " + result.getAccountId() + " topped up with " + initialAmount + " " + currency + " with transaction ID: " + tresult.getTransactionId());
		} catch (Exception e) {
			log.error("System exception " + e.toString());
		}
	}
	
	private TransactionRequest createInitialTransactionRequest(Long accountId, String currency, BigDecimal amount, Long bankId) {
		log.debug("Inside createInitialTransactionRequest");
		TransactionRequest req = new TransactionRequest();
		req.setComment("Initial top-up for new account");
		req.getEntries().add(new TransactionEntry(accountId, new Money(currency, 2, amount)));
		req.getEntries().add(new TransactionEntry(bankId, new Money(currency, 2, amount.negate())));
		return req;
	}

	private CreateAccountRequest createAccountRequest(Long userId, String name, String currency) {
		CreateAccountRequest car = new CreateAccountRequest();
		car.setNegativeBalanceAllowed(false);
		car.setRequestId(UUID.randomUUID());
		car.setUserId(userId);
		car.setType(STATIC_ACCOUNT);
		MetaInformation info = new MetaInformation();
		info.setName(name);
		car.setInformation(info);
		car.setCurrencyCode(currency);
		log.debug("Inside createAccountRequest");
		Map<String, String> attributes = new HashMap<String, String>();
		attributes.put("ROLE","MAIN");
		log.debug("Setting account attributes");
		for (String key : attributes.keySet()) {
			log.debug("Attribute found for " + key + " value " +  attributes.get(key));
		}
		car.setAttributes(attributes);
		return car;
	}
	
	
	

}
