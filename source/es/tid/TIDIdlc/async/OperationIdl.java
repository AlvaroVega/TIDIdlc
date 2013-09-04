/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 314 $
* Date: $Date: 2009-07-17 12:50:46 +0200 (Fri, 17 Jul 2009) $
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

package es.tid.TIDIdlc.async;

import java.util.ArrayList;

/**
 * @author iredondo
 */
public class OperationIdl {
	
	/**
	 * The name of the operation.
	 */
	private String m_name;

	/**
	 * The return type.
	 */
	private String m_return_type;
	
	/**
	 * The list of parameters.
	 */
	private ArrayList m_parameters_list;
	
	/**
	 * The list of throwed exceptions
	 */
	private ArrayList m_exceptions_list;
	
	/**
	 * Constructor
	 */
	public OperationIdl (String name, String return_type){
		this.m_name = name;
		this.m_return_type = return_type;
		this.m_parameters_list = new ArrayList();
		this.m_exceptions_list = new ArrayList();
	}

	public String getName() {
		return m_name;
	}

	public void setName(String name) {
		this.m_name = name;
	}

	public String getReturnType() {
		return m_return_type;
	}

	public void setReturnType(String return_type) {
		this.m_return_type = return_type;
	}

	public ArrayList getParameters() {
		return m_parameters_list;
	}

	public void setParameters(ArrayList parameters_list) {
		this.m_parameters_list = parameters_list;
	}
	
	public void addParameter (String name, String type, String modif) {
		ParameterOperIdl param = new ParameterOperIdl(name, type, modif);
		m_parameters_list.add(param);
	}
	
	public ArrayList getExceptions() {
		return m_exceptions_list;
	}

	public void setExceptions(ArrayList exceptions_list) {
            this.m_exceptions_list = exceptions_list;
	}
	
	public void addException (String name) {
			// Exceptions must not appear in Operation idl			
            //m_exceptions_list.add(name);
	}

	public String getString () {
		String contentOper = m_return_type + " " + m_name + " (";
		for (int i=0; i<m_parameters_list.size(); i++) {
			if (i>0)
				contentOper = contentOper + ", ";
			contentOper = contentOper + ((ParameterOperIdl)m_parameters_list.get(i)).getString();
		}
		
		for (int i=0; i<m_exceptions_list.size(); i++) {
			if (i>0)
				contentOper = contentOper + ", ";
			else
				contentOper = contentOper + ") raises (";
			contentOper = contentOper + m_exceptions_list.get(i);
		}
		contentOper = contentOper + ");"; 

		return contentOper;
	}

}
