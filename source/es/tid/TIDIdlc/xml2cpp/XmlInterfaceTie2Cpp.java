/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 326 $
* Date: $Date: 2010-01-18 13:12:39 +0100 (Mon, 18 Jan 2010) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telef�nica Investigaci�n y Desarrollo
*     S.A.Unipersonal (Telef�nica I+D)
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

package es.tid.TIDIdlc.xml2cpp;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.Scope;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Enumeration;

/**
 * Generates Cpp for interface ties.
 */
class XmlInterfaceTie2Cpp extends XmlInterfaceUtils2Cpp
    implements Idl2XmlNames
{

    //private Hashtable interface_parentsForHeader = new Hashtable();
    //private Hashtable interface_parentsForCpp = new Hashtable();
    private java.util.Hashtable m_interface_parents = null;
    private int key_cont = 0;

    public StringBuffer generateCpp(Element doc, String genPackage)
        throws Exception
    {
        m_interface_parents = new java.util.Hashtable();
        initInterfaceParents(doc);
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        String delegateName = genPackage.equals("") ? name : genPackage + "::"
                                                             + name;
        String tieWithPackageName;
        String className;
        // hay que tener en cuenta los interfaces con �mbito global
        if (!genPackage.equals("")) {
            tieWithPackageName = "POA_" + genPackage + "::" + name + "_tie";
            className = name;
        } else { // interfaces de �mbito global
            tieWithPackageName = "POA_" + name + "_tie";
            className = "POA_" + name;
        } // tenidos en cuenta los interfaces con �mbito global

        // It is included inside POA file so its not necesary the Cpp header.
        //XmlCppHeaderGenerator.generate(buffer,"tie",name+"_tie","POA_"+genPackage);

        buffer.append(tieWithPackageName + "::" + className + "_tie");
        buffer.append("(");
        buffer.append(delegateName);
        buffer.append("_ptr delegate)\n{\n");
        buffer.append("\t_delegate_tie = " + delegateName
                      + "::_duplicate(delegate);\n}\n\n");

        buffer.append(delegateName);
        buffer.append("_ptr " + tieWithPackageName + "::_delegate()\n{\n");
        buffer.append("\treturn _delegate_tie;\n}\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr " + tieWithPackageName
                      + "::__init_ids(){\n");
        buffer.append("\tCORBA::RepositoryIdSeq_ptr ids = new  CORBA::RepositoryIdSeq();\n");

        StringBuffer bufferTemp = new StringBuffer();
        int num = generateInterfacesSupported(bufferTemp, doc, 0);
        buffer.append("\tids->length(" + (num + 1) + ");\n");
        buffer.append(bufferTemp.toString());
        buffer.append("\treturn ids;\n");
        buffer.append("}\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr " + tieWithPackageName
                      + "::__ids =" + tieWithPackageName
                      + "::__init_ids();\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr ");
        buffer.append(tieWithPackageName);
        buffer.append("::_ids()\n{\n");
        buffer.append("\treturn __ids;\n");
        buffer.append("}\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr "
                    + tieWithPackageName
                    + "::_all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectId)\n{\n");
        buffer.append("\treturn __ids;\n");
        buffer.append("}\n\n");

        generateCppTieExportDef(buffer, doc, tieWithPackageName);
        Enumeration elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            generateCppTieExportDef(buffer, (Element) elements.nextElement(),
                                    tieWithPackageName);

        return buffer;
    }

    private void generateCppTieExportDef(StringBuffer buffer, Element doc,
                                         String className)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("");
                generateCppMethodHeader(buffer, el, className);
                generateCppTieMethodBody(buffer, el);
                buffer.append("\n\n");
            } else if (tag.equals(OMG_attr_dcl)) {
                generateCppTieAttributeDecl(buffer, el, className);
            }
        }

        
    }

    private void generateCppTieMethodBody(StringBuffer buffer, Element doc)
    {
        buffer.append("{\n");
        NodeList nodes = doc.getChildNodes();
        String nombre = doc.getAttribute(OMG_name);
        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        buffer.append("\t");
        if (returnTypeL.getLength() > 0) {
            buffer.append("return ");
        }

        // Method name
        buffer.append("_delegate_tie->");
        buffer.append(nombre);
        buffer.append("(");

        // Parameters
        boolean firstParam = true;
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                //Element paramType = (Element)el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                if (!firstParam)
                    buffer.append(", ");
                buffer.append(paramName);
                firstParam = false;
            }
        }
        //if (!firstParam) buffer.append("\n\t\t\t");
        buffer.append(");\n");
        buffer.append("}");
    }

    private void generateCppTieAttributeDecl(StringBuffer buffer, Element doc,
                                             String className)
    {
        // Get the type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getParamType((Element) nodes.item(0), "in");
        String returntype = XmlType2Cpp.getReturnType(typeEl);
        ;
        String readonly = doc.getAttribute(OMG_readonly);
        // Get & set methods
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);
            // Accessor
            buffer.append(returntype + " " + className + "::" + name + "() {\n");
            //buffer.append("throw (::CORBA::SystemException)\n{\n");; 
            // antes throw()
            
            // Method name
            buffer.append("\treturn _delegate_tie->");
            buffer.append(name);
            buffer.append("();\n}\n\n");
            // Modifier
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("void " + className + "::" + name + "(");
                buffer.append(type);
                buffer.append(" pvalue) {\n");
                //buffer.append("throw (::CORBA::SystemException)\n{\n"); //
                // antes throw()
                buffer.append("\t_delegate_tie->");
                buffer.append(name);
                buffer.append("(pvalue);\n}\n\n");
            }
        }
    }

    public StringBuffer generateHpp(Element doc, String genPackage)
        throws Exception
    {
        m_interface_parents = new java.util.Hashtable();
        initInterfaceParents(doc);
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        String className = genPackage.equals("") ? "POA_" + name : name; 
        //DAVV - por interfaces de �mbito global
        String delegateName = genPackage + "::" + name;

        XmlHppHeaderGenerator.generate(doc, buffer, "tie", name + "_tie",
                                       genPackage);

        // Class header
        buffer.append("class ");
        buffer.append(className); // DAVV - por interfaces de �mbito global
        buffer.append("_tie: public ");
        buffer.append(className); // DAVV - por interfaces de �mbito global
        buffer.append("{\n\n");
        buffer.append("\tpublic:\n\t\t");
        buffer.append(className); // DAVV - por interfaces de �mbito global
        buffer.append("_tie(");
        buffer.append(delegateName);
        buffer.append("_ptr delegate);\n\n");
        buffer.append("\t\t");
        buffer.append(delegateName);
        buffer.append("_ptr _delegate();\n\n");
        buffer.append("\t\tvirtual const CORBA::RepositoryIdSeq_ptr _all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectID);\n\n");
        buffer.append("\t\tvirtual const CORBA::RepositoryIdSeq_ptr _ids();\n\n");

        buffer.append("\t// Operations....\n");
        buffer.append("\tpublic:\n");
        generateHppTieExportDef(buffer, doc);
        Enumeration elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            generateHppTieExportDef(buffer, (Element) elements.nextElement());
        
        buffer.append("\tprivate:\n");
        buffer.append("\t\tstatic const CORBA::RepositoryIdSeq_ptr __ids;\n");
        buffer.append("\t\tstatic const CORBA::RepositoryIdSeq_ptr __init_ids();\n");
        buffer.append("\t\t" + delegateName + "_ptr _delegate_tie;\n");
        buffer.append("}; // end of " + className + "_tie class.\n"); 
        // por interfaces de �mbito global
        XmlHppHeaderGenerator.generateFoot(buffer, "tie", name + "_tie",
                                           genPackage);

        return buffer;
    }

    private void generateHppTieExportDef(StringBuffer buffer, Element doc)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                generateHppMethodHeader(buffer, el, false, false, "\t\t");
                buffer.append(";\n");
            } else if (tag.equals(OMG_attr_dcl)) {
                generateHppAttributeDecl(buffer, el, false, "\t\t");
            }
        }


    }

    private void initInterfaceParents(Element doc)
    {
        NodeList nodes = doc.getChildNodes();
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    String clase = el.getAttribute(OMG_name);
                    Scope scope = Scope.getGlobalScopeInterface(clase);
                    Element inhElement = scope.getElement();
                    // Generate the operation for all the interface parents
                    //if (!interface_parentsForCpp.containsKey(inhElement))
                    if (!m_interface_parents.contains(inhElement)) {
                        // This is to avoid the duplication of the operation
                        // when there's multiple
                        // inheritance and one of the father inherits from the
                        // other
                        //interface_parentsForCpp.put(inhElement,"void");
                        m_interface_parents.put(new java.lang.Integer(key_cont++), inhElement);
                        initInterfaceParents(inhElement);
                    }
                }
            }
        }
    }
}
