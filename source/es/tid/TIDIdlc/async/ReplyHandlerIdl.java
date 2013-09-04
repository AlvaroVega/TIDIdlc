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

/**
 * @author iredondo
 */
public class ReplyHandlerIdl {
	
	/**
	 * The name of the file.
	 */
	private String m_file_name;

	/**
	 * The file.
	 */
	private File m_file;
	
	/**
	 * The list of modules.
	 */
	private ArrayList m_module_list;

	/**
	 * The actual module.
	 */
	private ModuleIdl m_module_act;
	
	/**
	 * Constructor
	 * @throws IOException 
	 */
	public ReplyHandlerIdl (String name) throws IOException{
		this.m_file_name = name;
		this.m_file = new File(m_file_name);
		if (m_file.exists())
			m_file.delete();
		m_file.createNewFile();
		this.m_module_list = new ArrayList();
		this.m_module_act = null;
	}

	public void addModule (String name) {
		m_module_act = new ModuleIdl(name);
		m_module_list.add(m_module_act);
	}
	
	public void addInterface (String name, String abstractx, String local) {
		m_module_act.addDefinition("AMI_" + name + "ExceptionHolder");
		m_module_act.addInterface("AMI_"+name+"Handler", (new Boolean(abstractx)).booleanValue(), (new Boolean(local)).booleanValue());
		m_module_act.addInheritanceInterface("Messaging::ReplyHandler");
		//m_module_act.addInheritance("CORBA::Policy");
	}

	public void addInterfaceForward (String name, String abstractx, String local) {
		m_module_act.addInterfaceForward(name, (new Boolean(abstractx)).booleanValue(), (new Boolean(local)).booleanValue());
		//m_module_act.addValuetypeForward(name, (new Boolean(abstractx)).booleanValue(), (new Boolean(local)).booleanValue());
	}

	public void addAttribute (String name, String type, String readonly) {
		m_module_act.addOperationInterface("get_"+name, "void");
		m_module_act.addParameterOper("ami_return_val", type, "in");
		
		//Operation for exception results
		m_module_act.addOperationInterface("get_"+name+"_excep", "void");
		String ifaceName = m_module_act.getInterfaceAct().getName();
		ifaceName = ifaceName.substring(0, ifaceName.length()-7);
		m_module_act.addParameterOper("excep_holder", "Messaging::ExceptionHolder", "in");
		
		if (readonly.compareTo("true")!=0) {
			m_module_act.addOperationInterface("set_"+name, "void");

			//Operation for exception results
			m_module_act.addOperationInterface("set_"+name+"_excep", "void");
            m_module_act.addParameterOper("excep_holder", "Messaging::ExceptionHolder", "in");
		}
	}

	public void addOperation (String name, String returnType) {
		m_module_act.addOperationInterface(name, "void");
		if (returnType != null)
			m_module_act.addParameterOper("ami_return_val", returnType, "in");
		
		//Operation for exception results
		OperationIdl operAct = m_module_act.getInterfaceAct().getOperationAct(); //Saves actual operation
		
		m_module_act.addOperationInterface(name+"_excep", "void");
		String ifaceName = m_module_act.getInterfaceAct().getName();
		ifaceName = ifaceName.substring(0, ifaceName.length()-7);
		m_module_act.addParameterOper("excep_holder", "Messaging::ExceptionHolder", "in");
		
		m_module_act.getInterfaceAct().setOperationAct(operAct); //Restores actual operation 
	}
	
	public void addParameterOper (String name, String type, String modif) {
		if (modif.compareTo("in")!=0)
			m_module_act.addParameterOper(name, type, "in");
	}
	
	public void addExceptionOper (String name) {
		m_module_act.addExceptionOperInterface(name);
	}

	public void addInheritance (String inheritance) {
		m_module_act.addInheritanceInterface(inheritance);
		m_module_act.addInheritanceValuetype(inheritance);
	}

	/**
	 * The ReplyHandler idl file is created in the file systen
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public String createIdlFile () throws FileNotFoundException, IOException {


		StringBuffer buffer = new StringBuffer();

		XmlIdlHeaderGenerator.generate(buffer, "IDL file",  m_file_name);

		String content_file = buffer.toString() + "#include <Messaging.idl>\n\n";
		//String content_file = "#include <orb.idl>\n\n";
		
		String content_module;
		for (int i=0; i<m_module_list.size(); i++) {
			content_module = ((ModuleIdl)m_module_list.get(i)).getString();
			if (content_module != null)
				content_file = content_file + content_module + "\n"; 
		}
		
		Traces.println("replyHandler idl:\n" + content_file, Traces.USER);
		
		FileOutputStream fos = new FileOutputStream(m_file);
		fos.write(content_file.getBytes());
		fos.flush();
		fos.close();
		return m_file.getPath();
	}
	
}
