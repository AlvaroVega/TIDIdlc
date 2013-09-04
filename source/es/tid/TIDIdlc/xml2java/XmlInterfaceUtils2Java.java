/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 106 $
* Date: $Date: 2006-01-25 12:04:40 +0100 (Wed, 25 Jan 2006) $
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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.util.Traces;
import java.io.*;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for interfaces.
 */
class XmlInterfaceUtils2Java
    implements Idl2XmlNames
{

    private java.util.Hashtable m_util_interface_parents = new java.util.Hashtable();

    protected void generateInterfacesSupported(StringBuffer buffer, Element doc)
    {
        String name = RepositoryIdManager.getInstance().get(doc);
        buffer.append("    \"");
        buffer.append(name);
        buffer.append("\"");
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                NodeList nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    Scope scope = Scope.getGlobalScopeInterface(el.getAttribute(OMG_name));
                    Element inheritedElement = scope.getElement();
                    // Generate the Repository Id of all the interface parents
                    if (!m_util_interface_parents.containsKey(inheritedElement)) {
                        // This is to avoid the duplication of the RepositoryId
                        // when there's multiple
                        // inheritance and one of the father inherits from the
                        // other
                        buffer.append(",\n");
                        m_util_interface_parents.put(inheritedElement, "void");
                        generateInterfacesSupported(buffer, inheritedElement);
                    }
                }
            }
        }
    }

    protected void generateJavaMethodHeader(StringBuffer buffer, Element doc)
    {
        NodeList nodes = doc.getChildNodes();
        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();

        if (returnTypeL.getLength() > 0) {
            Element ret = (Element) returnTypeL.item(0);
            String retType = XmlType2Java.getType(ret);
            buffer.append(retType);
            buffer.append(" ");
        } else {
            buffer.append("void ");
        }

        // Method name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append(nombre + "(");

        // Parameters
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                
                if (i > 1) {
                    buffer.append(", ");
                }
                String paramTypeS = XmlType2Java.getParamType(paramType, !in);
                buffer.append(paramTypeS);
                buffer.append(" ");
                buffer.append(paramName);
            }
        }
        buffer.append(")");

        // Exceptions
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_raises)) {
                buffer.append("\n    throws ");
                NodeList exceps = el.getChildNodes();
                for (int j = 0; j < exceps.getLength(); j++) {
                    Element ex = (Element) exceps.item(j);
                    if (j > 0)
                        buffer.append(", ");
                    String exceptionName = ex.getAttribute(OMG_name);
                    exceptionName = TypeManager.convert(exceptionName);
                    buffer.append(exceptionName);
                }
                break;
            }
        }
    }
    
    // Metodo para la cabecera del stub asincrona
    protected void generateJavaMethodHeaderAsync(StringBuffer buffer, Element doc, String i_name)
    {
        NodeList nodes = doc.getChildNodes();
        
        buffer.append("void ");

        // Method name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append("sendc_" + nombre + "(");

        // Parameters
        buffer.append("AMI_" + i_name + "Handler ami_handler");
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (!out) {
                    String paramTypeS = XmlType2Java.getParamType(paramType, false);//!in);
                    buffer.append(", ");
                    buffer.append(paramTypeS);
                    buffer.append(" ");
                    buffer.append(paramName);
                }
            }
        }
        buffer.append(")");

        // Exceptions
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_raises)) {
                buffer.append("\n    throws ");
                NodeList exceps = el.getChildNodes();
                for (int j = 0; j < exceps.getLength(); j++) {
                    Element ex = (Element) exceps.item(j);
                    if (j > 0)
                        buffer.append(", ");
                    String exceptionName = ex.getAttribute(OMG_name);
                    exceptionName = TypeManager.convert(exceptionName);
                    buffer.append(exceptionName);
                }
                break;
            }
        }
    }
}