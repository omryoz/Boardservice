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

import com.board.games.model.PlayerProfile;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;
import com.google.inject.Inject;
import com.google.inject.Singleton;
@Singleton
public class Board {

	private Logger log = Logger.getLogger(this.getClass());
	protected BoardService boardService;
	@Inject
	public Board(BoardService boardService) {
		log.debug("**** Board:  Inside inject");
		this.boardService = boardService;
	}	

	public void initialize(ServiceContext con) throws SystemException {
		log.debug("***** Board: Inside initialize");
		boardService.init(con);
	}

	public ClientServiceAction process(ServiceAction action) {
		log.debug("Inside process");
		return boardService.onAction(action);
	}

	public PlayerProfile getUserInfo(int userId) throws Exception {
		return boardService.getUserProfile(userId);
	}


	public LoginHandler getLoginHandler(LoginRequestAction request) {
		return boardService.locateLoginHandler(request);
	}
}