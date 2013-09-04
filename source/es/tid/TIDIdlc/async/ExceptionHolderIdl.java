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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import es.tid.TIDIdlc.util.Traces;

import org.omg.CORBA.OMGVMCID;

import es.tid.TIDIdlc.CompilerConf;

/**
 * @author iredondo
 */
public class ExceptionHolderIdl {
	
	/**
	 * The name of the file.
	 */
	private String m_file_name;

	/**
	 * The file.
	 */
	private File m_file;
	
	/**
	 * The list of exceptions.
	 */
//	private ArrayList m_exceptions_list;

	/**
	 * The actual exception.
	 */
//	private ExceptionIdl m_exception_act;

	/**
	 * The list of modules.
	 */
	private ArrayList m_modules_list;

	/**
	 * The actual module.
	 */
	private ModuleIdl m_module_act;
	
	/**
	 * Constructor
	 * @throws IOException 
	 */
	public ExceptionHolderIdl (String name) throws IOException{
		this.m_file_name = name;
		this.m_file = new File(m_file_name);
		if (m_file.exists())
			m_file.delete();
		m_file.createNewFile();
//		this.m_exceptions_list = new ArrayList();
//		this.m_exception_act = null;
		this.m_modules_list = new ArrayList();
		this.m_module_act = null;
	}

	/*public void addException (String name) {
		m_exception_act = new ExceptionIdl(name);
		m_exceptions_list.add(m_exception_act);
	}
	
	public void addExceptionModule (String name) {
		m_module_act.addException(name);
		m_exception_act = null;
	}

	public void addMemberException (String member) {
		if (m_exception_act != null)
			m_exception_act.addMember(member);
		else
			m_module_act.addMemberException(member);
	}*/

	public void addModule (String name) {
		m_module_act = new ModuleIdl(name);
		m_modules_list.add(m_module_act);
	}
	
	public void addInterface (String name, String abstractx, String local) {
                m_module_act.addValuetype("AMI_"+name+"ExceptionHolder",
                                          name, (new Boolean(abstractx)).booleanValue(), 
                                          (new Boolean(local)).booleanValue());
		m_module_act.addInheritanceValuetype("Messaging::ExceptionHolder");
	}

	public void addInterfaceForward (String name, String abstractx, String local) {
		m_module_act.addValuetypeForward(name, name, 
                                                 (new Boolean(abstractx)).booleanValue(), 
                                                 (new Boolean(local)).booleanValue());
	}

	public void addAttribute (String name, String type, String readonly) {
		m_module_act.addOperationValuetype("raise_get_"+name, "void");

		if (readonly.compareTo("true")!=0) 
			m_module_act.addOperationValuetype("raise_set_"+name, "void");
	}

	public void addOperation (String name, String returnType) {
		m_module_act.addOperationValuetype("raise_" + name, "void");
	}

	public void addExceptionDclInterface (String name) {
		m_module_act.addExceptionDclValuetype (name);
	}

	public void addExceptionOper (String name) {
		if (m_module_act.getValuetypeAct().containsExceptionDcl(name))
			m_module_act.addExceptionOperValuetype(m_module_act.getValuetypeAct().getInterfaceName() + 
                                                               "::" + name);
		else
			m_module_act.addExceptionOperValuetype(name);
	}

	public void addInheritance (String inheritance) {
            m_module_act.addInheritanceValuetype(inheritance);
	}

	/**
	 * The ReplyHandler idl file is created in the file system
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public String createIdlFile () throws FileNotFoundException, IOException {

		StringBuffer buffer = new StringBuffer();

		XmlIdlHeaderGenerator.generate(buffer, "IDL file",  m_file_name);

		String content_file = buffer.toString() + "#include <Messaging.idl>\n\n";
		//content_file = content_file + "#include <" + CompilerConf.getFileName() + ">\n\n";
		//String content_file = "#include <orb.idl>\n\n";
		
		/*for (int i=0; i<m_exceptions_list.size(); i++) {
			if (i>0)
				content_file = content_file + "\n";
			content_file = content_file + ((ExceptionIdl)m_exceptions_list.get(i)).getString(); 
		}*/

		String content_module;
		for (int i=0; i<m_modules_list.size(); i++) {
			content_module = ((ModuleIdl)m_modules_list.get(i)).getString();
			if (content_module != null)
				content_file = content_file + content_module + "\n";
		}
		
		Traces.println("ExceptionHolder idl:\n" + content_file, Traces.USER);
		
		FileOutputStream fos = new FileOutputStream(m_file);
		fos.write(content_file.getBytes());
		fos.flush();
		fos.close();
		return m_file.getPath();
	}
	
}
