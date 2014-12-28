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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.ini4j.Ini;

import com.board.games.service.discuz.DiscuzBoardService;
import com.board.games.service.dolphin.DolphinBoardService;
import com.board.games.service.generic.GenericBoardService;
import com.board.games.service.ipb.IPBBoardService;
import com.board.games.service.jl.JLBoardService;
import com.board.games.service.modx.MODXBoardService;
import com.board.games.service.phpbb3.PHPBB3BoardService;
import com.board.games.service.smf.SMFBoardService;
import com.board.games.service.vanilla.VanillaBoardService;
import com.board.games.service.wolflab.WBBBoardService;
import com.board.games.service.xbtit.XbtitBoardService;
import com.board.games.service.xf.XFBoardService;
import com.google.inject.AbstractModule;

public class BoardModule extends AbstractModule {
	
	private Logger log = Logger.getLogger(this.getClass());
	
	@Override
	protected void configure() {
		try {
			log.debug("Inside configure : reading PokerConfig.ini: performing binding");
			Ini ini = new Ini(new File("PokerConfig.ini"));
			String boardType = ini.get("PokerConfig", "boardType");
			String osType = ini.get("PokerConfig", "osType");
			
			log.debug("configure for board type " + boardType);
			if (boardType.equals("IPB") || boardType.equals("1")) {
				bind(BoardService.class).to(IPBBoardService.class);
			} else if (boardType.equals("XF") || boardType.equals("2")) {
				bind(BoardService.class).to(XFBoardService.class);
			} else if (boardType.equals("PHPBB3") || boardType.equals("3")) {
				log.debug("Inside configure : phpBB3 found");
				bind(BoardService.class).to(PHPBB3BoardService.class);
			} else if (boardType.equals("SMF") || boardType.equals("4")) {
				log.debug("Inside configure : SMF found");
				bind(BoardService.class).to(SMFBoardService.class);
			} else if (boardType.equals("Discuz") || boardType.equals("5")) {
				log.debug("Inside configure : Discuz found");
				bind(BoardService.class).to(DiscuzBoardService.class);
			} else if (boardType.equals("MODX") || boardType.equals("6")) {
				log.debug("Inside configure : MODX found");
				bind(BoardService.class).to(MODXBoardService.class);
			} else if (boardType.equals("JL") || boardType.equals("7")) {
				log.debug("Inside configure : JL found");
				bind(BoardService.class).to(JLBoardService.class);
			} else if (boardType.equals("Vanilla") || boardType.equals("8")) {
				log.debug("Inside configure : Vanilla found");
				bind(BoardService.class).to(VanillaBoardService.class);
			} else if (boardType.equals("Xbtit") || boardType.equals("9")) {
				log.debug("Inside configure : Xbtit found");
				bind(BoardService.class).to(XbtitBoardService.class);
			} else if (boardType.equals("Dolphin") || boardType.equals("10")) {
				log.debug("Inside configure : Dolphin found");
				bind(BoardService.class).to(DolphinBoardService.class);
			} else if (boardType.equals("Wolflab") || boardType.equals("11")) {
				log.debug("Inside configure : Wolflab found");
				bind(BoardService.class).to(WBBBoardService.class);
			} else {
				log.debug("Inside configure : *** NO BOARD TYPE *** found");
				bind(BoardService.class).to(GenericBoardService.class);
			}
		} catch (IOException ioe) {
			log.error("Exception in init " + ioe.toString());
			//System.err.println("Inside configure :PokerConfig.ini not  found" + ioe.toString());
		} catch (Exception e) {
			//System.err.println("Inside configure : other exception" + e.toString());
			log.error("Exception in init " + e.toString());
		}
	}
}
