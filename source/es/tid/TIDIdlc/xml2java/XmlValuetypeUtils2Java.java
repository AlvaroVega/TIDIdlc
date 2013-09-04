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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.io.*;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for interfaces.
 */
class XmlValuetypeUtils2Java
    implements Idl2XmlNames
{

    protected void generateJavaMethodHeader(StringBuffer buffer, Element doc)
    {
        NodeList nodes = doc.getChildNodes();

        // public abstract
        buffer.append("public abstract ");

        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        if (returnTypeL.getLength() > 0) {
            Element ret = (Element) returnTypeL.item(0);
            buffer.append(XmlType2Java.getType(ret));
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
                if (i > 1)
                    buffer.append(", ");
                buffer.append(XmlType2Java.getParamType(paramType, !in));
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
                    buffer.append(TypeManager.convert(exceptionName));
                }
                break;
            }
        }
    }

    protected void generateJavaFactoryHeader(StringBuffer buffer, Element doc,
                                             String returnType)
    {

        // Return type
        buffer.append(returnType + " ");

        // Factory name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append(nombre + "(");

        // Parameters
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_init_param_decl)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                if (i > 0)
                    buffer.append(", ");
                buffer.append(XmlType2Java.getParamType(paramType, false));
                buffer.append(" ");
                buffer.append(paramName);
            }
        }
        buffer.append(")");
    }

    protected void generateHelperJavaFactory(StringBuffer buffer, Element doc,
                                             String returnType)
    {

        // Return type
        buffer.append("  public static " + returnType + " ");

        // Factory name
        String name = doc.getAttribute(OMG_name);
        buffer.append(name + "(");
        buffer.append("org.omg.CORBA.ORB orb");
        // Parameters
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_init_param_decl)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                buffer.append(", ");
                buffer.append(XmlType2Java.getParamType(paramType, false));
                buffer.append(" ");
                buffer.append(paramName);
            }
        }
        buffer.append("){\n");
        buffer.append("    try {\n");
        buffer.append("      "
                    + returnType
                    + "ValueFactory _factory = ("
                    + returnType
                    + "ValueFactory)((org.omg.CORBA_2_3.ORB) orb).lookup_value_factory(id());\n");
        buffer.append("      return _factory." + name + "(");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_init_param_decl)) {
                String paramName = el.getAttribute(OMG_name);
                if (i > 0)
                    buffer.append(", ");
                buffer.append(paramName);
            }
        }
        buffer.append(");\n");
        buffer.append("    } catch (ClassCastException _ex) {\n");
        buffer.append("      throw new org.omg.CORBA.BAD_PARAM ();\n");
        buffer.append("    }\n");
        buffer.append("  }\n\n");
    }
}