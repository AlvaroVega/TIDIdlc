/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 102 $
* Date: $Date: 2006-01-24 17:35:36 +0100 (Tue, 24 Jan 2006) $
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


/**
 * @author iredondo
 */
public class ParameterOperIdl {
	
	/**
	 * The name of the parameter.
	 */
	private String m_name;

	/**
	 * The type of the parameter.
	 */
	private String m_type;
	
	/**
	 * The modifier of the parameter.
	 */
	private String m_modif;
	
	/**
	 * Constructor
	 */
	public ParameterOperIdl (String name, String type, String modif){
		this.m_name = name;
		this.m_type = type;
		this.m_modif = modif;
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public String getType() {
		return m_type;
	}

	public void setType(String type) {
		this.m_type = type;
	}

	public String getModif() {
		return m_modif;
	}

	public void setModif(String modif) {
		this.m_modif = modif;
	}

	public String getString() {
		String contenParam = m_modif + " " + m_type + " " + m_name;
		return contenParam;
	}
}
