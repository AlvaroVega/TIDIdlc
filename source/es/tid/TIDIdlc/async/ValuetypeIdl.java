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
public class ValuetypeIdl {
	
	/**
	 * The name of the valuetype.
	 */
	private String m_name;

	/**
	 * Valuetype isn't declared here.
	 */
	private boolean m_forward;

	/**
	 * Valuetype is abstract.
	 */
	private boolean m_abstract;

	/**
	 * Valuetype is local.
	 */
	private boolean m_local;

	/**
	 * Valuetype inheritance.
	 */
	private String m_inheritance;

	/**
	 * The list of attributes.
	 */
	//private ArrayList m_attrs_list;

	/**
	 * The name of the associated interface.
	 */
	private String m_interface_name;

	/**
	 * The list of exception declarations within the associated interface.
	 */
	private ArrayList m_exceptions_list;

	/**
	 * The list of operations.
	 */
	private ArrayList m_operations_list;

	/**
	 * The actual operation.
	 */
	private OperationIdl m_operation_act;

	/**
	 * Constructor
	 */
	public ValuetypeIdl (String name, String ifaceName, boolean forward, boolean abstractx, boolean local){
		this.m_name = name;
		this.m_interface_name = ifaceName;
		this.m_forward = forward;
		this.m_abstract = abstractx;
		this.m_local = local;
		this.m_inheritance = null;
		this.m_exceptions_list = new ArrayList();
		this.m_operations_list = new ArrayList();
		this.m_operation_act = null;
	}

	public String getName() {
		return m_name;
	}

	public String getInterfaceName() {
		return m_interface_name;
	}

	public void addExceptionDcl(String name) {
		m_exceptions_list.add(name);
	}

	public boolean containsExceptionDcl(String name) {
		return m_exceptions_list.contains(name);
	}

	public void addOperation(String name, String returnType) {
		m_operation_act = new OperationIdl(name, returnType);
		m_operations_list.add(m_operation_act);
	}

	public void addParameterOper(String name, String type, String modif) {
		m_operation_act.addParameter(name, type, modif);
	}

	public void addExceptionOper(String name) {
		m_operation_act.addException(name);
	}

	public void addInheritance (String inheritance) {
		m_inheritance = inheritance;
	}
	
	public String getString () {
		String contentValue = "\t";
		
		if (m_abstract)
			contentValue = contentValue + "abstract ";
		else if (m_local)
			contentValue = contentValue + "local ";
		
		if (m_forward) {
			contentValue = contentValue + "valuetype " + m_name + ";";
			return contentValue;
		}
		
		if (m_inheritance != null)
			contentValue = contentValue + "valuetype " + m_name + " : "+ m_inheritance + "{\n";
		else
			contentValue = contentValue + "valuetype " + m_name + "{\n";
			
		for (int i=0; i<m_operations_list.size(); i++) {
			contentValue = contentValue + "\t\t" + ((OperationIdl)m_operations_list.get(i)).getString() + "\n";
		}
		contentValue = contentValue + "\t};";
		return contentValue;
	}
}
