/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 117 $
* Date: $Date: 2006-02-22 13:25:35 +0100 (Wed, 22 Feb 2006) $
* Last modified by: $Author: iredondo $
*
* (C) Copyright 2004 Telefónica Investigación y Desarrollo
*     S.A.Unipersonal (Telefónica I+D)
*
* Info about members and contributors of the MORFEO project
* is available at:
*
*   http://www.morfeo-project.org/TIDIdlc/CREDITS
*
* This program is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
*
* If you want to use this software an plan to distribute a
* proprietary application in any way, and you are not licensing and
* distributing your source code under GPL, you probably need to
* purchase a commercial license of the product.  More info about
* licensing options is available at:
*
*   http://www.morfeo-project.org/TIDIdlc/Licensing
*/ 

package es.tid.TIDIdlc.async;

import java.util.ArrayList;

/**
 * @author iredondo
 */
public class ExceptionIdl {
	
	/**
	 * The name of the exception.
	 */
	private String m_name;

	/**
	 * The list of members.
	 */
	private ArrayList m_members_list;

	/**
	 * The actual valuetype.
	 */
//	private ValuetypeIdl m_valuetype_act;

	/**
	 * Constructor
	 */
	public ExceptionIdl (String name){
		this.m_name = name;
		this.m_members_list = new ArrayList();
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public ArrayList getMembersList() {
		return m_members_list;
	}

	public void setMembersList(ArrayList members_list) {
		this.m_members_list = members_list;
	}
	
	public void addMember (String member) {
		m_members_list.add(member);
	}

	public String getString() {
		String contentException = "exception " + m_name + "{\n";
		for (int i=0; i<m_members_list.size(); i++) {
			contentException = contentException + m_members_list.get(i) + ";\n";
		}
		contentException = contentException + "};";
		return contentException;
	}
}
