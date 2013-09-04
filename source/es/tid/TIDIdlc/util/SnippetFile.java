/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 2 $
* Date: $Date: 2005-04-15 14:20:45 +0200 (Fri, 15 Apr 2005) $
* Last modified by: $Author: rafa $
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

package es.tid.TIDIdlc.util;


/**
 * @author rafa
 */
public class SnippetFile {

	/**
	 * The buffer where the code is stored. 
	 */
	private StringBuffer m_buffer;
	
	/**
	 * The name of the file in expanded format.
	 */
	private String m_file_name;
	
	/**
	 * The path where the file is used in expanded format.
	 */
	private String m_path;
	
	/**
	 * The type of the file. Its posible values are FileManager.TYPE_XXX_XXX
	 */
	private int m_type;
	
	/**
	 * 
	 */
	private String m_idl_file;
	
	/**
	 * Public constructor of the class.
	 * @param file_name The file name of the piece of code.
	 * @param path The path where the file is.
	 * @param type The type of the file.
	 */
	public SnippetFile (String file_name,
						String path,
						String idl_file_name,
						int type) {
		this.m_buffer = new StringBuffer();
		this.m_file_name = file_name;
		this.m_path = path;
		this.m_idl_file = idl_file_name;
		this.m_type = type;
	}
	
	/**
	 * Adds code to the snippet.
	 * @param code Code to add.
	 */
	public void addCode(StringBuffer code) {
		this.m_buffer.append(code);
	}
	
	/**
	 * Gets the source code from the buffer.
	 * @return the source code from the buffer
	 */
	public String getCode() {
		return this.m_buffer.toString();
	}

	/**
	 * Adds code to the snippet.
	 * @param code Code to add.
	 */
	public void addIdlFileName(String idl) {
		this.m_idl_file = idl;
	}
	
	/**
	 * Gets the source code from the buffer.
	 * @return the source code from the buffer
	 */
	public String getIdlFileName() {
		return this.m_idl_file;
	}

	/**
	 * Gets the file name of the snippet.
	 * @return The file name of the snippet
	 */
	public String getFileName() {
		return this.m_file_name;
	}

	/**
	 * Gets the type of the snippet.
	 * @return The type of the snippet
	 */
	public int getType() {
		return this.m_type;
	}
	
	/**
	 * Gets the path where the code should be stored.
	 * @return The relative path where the code should be stored
	 */
	public String getPath() {
		return this.m_path;
	}
	
}
