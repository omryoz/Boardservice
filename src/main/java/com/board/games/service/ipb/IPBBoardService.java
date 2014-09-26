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

package com.board.games.service.ipb;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.board.games.handler.ipb.IPBPokerLoginServiceImpl;
import com.board.games.service.common.CommonBoardService;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.google.inject.Singleton;
@Singleton
public class IPBBoardService extends CommonBoardService {

	private IPBPokerLoginServiceImpl loginServiceImpl = new IPBPokerLoginServiceImpl();
 
   
	private Logger log = Logger.getLogger(this.getClass());
   
	public ApplicationContext getApplicationContext() {
		return super.getApplicationContext();
	}	
	
	public void init(ServiceContext con) throws SystemException { 
		log.debug("**** Inside IPBBoardService Initialized");
		setDaoConfig("ipbDAO");
		super.init(con);		
	}
	

	@Override
	public LoginHandler locateLoginHandler(LoginRequestAction req) {
		return loginServiceImpl;
	}
	    

	
}
