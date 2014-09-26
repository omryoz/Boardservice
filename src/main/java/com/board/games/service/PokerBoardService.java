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

package com.board.games.service;

import org.apache.log4j.Logger;

import com.board.games.handler.generic.GenericLoginHandler;
import com.board.games.model.PlayerProfile;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.login.LoginLocator;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.Service;
import com.cubeia.firebase.api.service.ServiceContext;
import com.cubeia.firebase.api.service.ServiceRegistry;
import com.cubeia.firebase.api.service.ServiceRouter;
import com.google.inject.Guice;
import com.google.inject.Injector;

public class PokerBoardService implements LoginLocator, Service, PokerBoardServiceContract {

	private GenericLoginHandler handler = new GenericLoginHandler();

	private BoardService boardService;

	private ServiceRouter router;

	private Injector injector = null;
	private Board board = null;


	private Logger log = Logger.getLogger(this.getClass());


	public PokerBoardService() {
		try {
			log.debug("Inside PokerBoardService: createInjector");
			injector = Guice.createInjector(new BoardModule());
			board = injector.getInstance(Board.class);
		} catch (Exception e) {
			log.error("Exception in init " + e.toString());
		}
	}

	@Override
	public void init(ServiceContext con) throws SystemException {
		try {
			log.debug("Inside PokerBoardService: init");
			// For some reason, constructor is not called in windows
			if (board == null) {
				log.error("Initialize injector of board");
				injector = Guice.createInjector(new BoardModule());
				board = injector.getInstance(Board.class);
			}
			log.debug("******* Initialize board");
			board.initialize(con);
		} catch (Exception e) {
			log.error("Exception occurred in PokerBoardService : " + e.toString());
		}
	}


	@Override
	public void destroy() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRouter(ServiceRouter router) {
		this.router = router;
	}


	@Override
	public void onAction(ServiceAction e) {
		log.debug("Inside onAction");
		ClientServiceAction dataToClient = board.process(e);
		if (dataToClient != null) {
		// dispatch data
		router.dispatchToPlayer(e.getPlayerId(), dataToClient);
		} else {
			log.debug("onAction : dataToclient is null");
		}

	}

	@Override
	public void init(ServiceRegistry serviceRegistry) {
		// TODO Auto-generated method stub

	}

	@Override
	public LoginHandler locateLoginHandler(LoginRequestAction request) {
		log.debug("Locate login handler");
		if (board == null) {
			log.error("Initialize injector of board");
			injector = Guice.createInjector(new BoardModule());
			board = injector.getInstance(Board.class);
		}
		return board.getLoginHandler(request);
		//return handler;
	}

	@Override
	public PlayerProfile getUserProfile(int userId) throws Exception {
		log.debug("Inside getUserProfile on user id #  " + String.valueOf(userId));
		return board.getUserInfo(userId);
	}


}
