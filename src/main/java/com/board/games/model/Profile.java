/**
 * This file is part of PokerClientService.
 * @copyright (c) 2015 Cuong Pham-Minh
 *
 * PokerClientService is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PokerClientService is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PokerClientService.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.board.games.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "Profile")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
	"externalAvatarUrl",
	"level",
    "name",
    "screenName",
    "externalUsername",
    "userName"
})

public class Profile {

	
	private String externalAvatarUrl = "";
	private int level = 0;
	private String name = "";
	private String screenName = "";
	private String externalUsername = "";
	private String userName = "";
	
	/*	 items.award.imageUrl
	 items.award.description
	 items.inventory
	 items.inventory.description	*/
	public String getExternalAvatarUrl() {
		return externalAvatarUrl;
	}
	public void setExternalAvatarUrl(String externalAvatarUrl) {
		this.externalAvatarUrl = externalAvatarUrl;
	}
	public int getLevel() {
		return level;
	}
	public void setLevel(int level) {
		this.level = level;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	public String getScreenName() {
		return screenName;
	}
	public void setScreenName(String screenName) {
		this.screenName = screenName;
	}
	public String getExternalUsername() {
		return externalUsername;
	}
	public void setExternalUsername(String externalUsername) {
		this.externalUsername = externalUsername;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}

}
