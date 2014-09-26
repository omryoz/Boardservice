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

import org.springframework.context.support.FileSystemXmlApplicationContext;

import com.board.games.model.PlayerProfile;
import com.cubeia.firebase.api.action.local.LoginRequestAction;
import com.cubeia.firebase.api.action.service.ClientServiceAction;
import com.cubeia.firebase.api.action.service.ServiceAction;
import com.cubeia.firebase.api.login.LoginHandler;
import com.cubeia.firebase.api.server.SystemException;
import com.cubeia.firebase.api.service.ServiceContext;

public interface BoardService {
	
	public final static String LINUX_APP_CTX_CONFIG_FILE = "file:/usr/local/poker/beans.xml";
	public final static String WINDOWS_APP_CTX_CONFIG_FILE = "file:D:\\Dev\\beans.xml";
	 public void init(ServiceContext con) throws SystemException;
	 public ClientServiceAction onAction(ServiceAction action);
	 
	 public PlayerProfile getUserProfile(int userId) throws Exception;
	 
	 public LoginHandler locateLoginHandler(LoginRequestAction request);
	 
}
