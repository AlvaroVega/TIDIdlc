/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 322 $
* Date: $Date: 2010-01-14 13:50:44 +0100 (Thu, 14 Jan 2010) $
* Last modified by: $Author: avega $
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

/*
 * Created on 11-abr-2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package es.tid.TIDIdlc.util;

import java.util.ArrayList;

/**
 * @author rafa
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class IdlFile {
	
	/**
	 * The name of the file.
	 */
	private String m_file_name;

	/**
	 * The list of includes.
	 */
	private ArrayList m_include_list;
	
	/**
	 * The list of modules.
	 */
	private ArrayList m_module_list;
	

	public IdlFile (String name){
		this.m_file_name = name;
		this.m_include_list = new ArrayList();
		this.m_module_list = new ArrayList();
	}
	
	public void AddIncludeToFile(String include) {
		this.m_include_list.add(include);
	}

	public void AddModuleToFile(String module) {
		this.m_module_list.add(module);
	}

	public ArrayList getIncludesFromIdlFile() {
		return this.m_include_list;
	}

	public ArrayList getModulesFromIdlFile() {
		return this.m_module_list;
	}

}
