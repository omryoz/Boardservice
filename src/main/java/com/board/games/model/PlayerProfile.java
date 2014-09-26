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
package com.board.games.model;


public class PlayerProfile {
	private int id = 0;
	private String avatar_location = "";
	private String avatar_type = "";
	private String name="";
	private int posts = 0;
	private int groupId = 0;
	
	
	public int getPosts() {
		return posts;
	}
	public void setPosts(int posts) {
		this.posts = posts;
	}
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getAvatar_location() {
		return avatar_location;
	}
	public void setAvatar_location(String avatar_location) {
		this.avatar_location = avatar_location;
	}
	public String getAvatar_type() {
		return avatar_type;
	}
	public void setAvatar_type(String avatar_type) {
		this.avatar_type = avatar_type;
	}
}
