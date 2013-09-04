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

package es.tid.TIDIdlc.xml2java.structures;

import es.tid.TIDIdlc.xml2java.*;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

import java.io.*;
import java.util.Vector;

import org.w3c.dom.*;

/**
 * Generates Java for structure declarations.
 */
public class StructType extends StructProcessor
{

    public void generateJava(StringBuffer buffer, String objectName,
                             Element classType, Vector indexes, Vector isArray)
        throws Exception
    {
        if (objectName != null) {
            buffer.append("      members[");
            buffer.append(m_actual);
            buffer.append("] = new org.omg.CORBA.StructMember(\"");
            buffer.append(objectName);
            buffer.append("\", ");
        } else {
            buffer.append("      org.omg.CORBA.TypeCode original_type = ");
        }
        if (indexes.size() == 0) {
            String elmType = XmlType2Java.getUnrolledName(classType);
            if ((this.m_struct_name != null)
                && (this.m_struct_name.equals(elmType))) {
                buffer.append("_orb().create_recursive_tc(id())");
            } else {
                buffer.append(XmlType2Java.getTypecode(classType));
            }
        } else {
            for (int i = 0; i < indexes.size(); i++) {
                if (indexes.elementAt(i) instanceof Long) {
                    if ((XmlType2Java
                        .getTypedefType(classType)
                        .equals(Idl2XmlNames.OMG_string) || XmlType2Java
                        .getTypedefType(classType)
                        .equals(Idl2XmlNames.OMG_wstring))
                        && !((Element) classType.getParentNode())
                            .getTagName().equals(Idl2XmlNames.OMG_sequence)
                        && !((Element) classType.getNextSibling())
                            .getTagName().equals(Idl2XmlNames.OMG_array)) {
                        // We have a bounded string
                        buffer.append("_orb().create_string_tc(");
                        buffer.append(indexes.elementAt(i));
                        if (objectName != null)
                            buffer.append("),null);\n");
                        else
                            buffer.append(");\n");
                        m_actual++;
                        return;
                    } else {
                        Boolean isThisAnArray = (Boolean) isArray.elementAt(i);
                        if (isThisAnArray.booleanValue())
                            buffer.append("_orb().create_array_tc(");
                        else
                            buffer.append("_orb().create_sequence_tc(");

                        buffer.append(indexes.elementAt(i));
                        buffer.append(", ");
                    }
                } else {
                    buffer.append("_orb().create_sequence_tc(0 , ");
                }
            }
            String elmType = XmlType2Java.getUnrolledName(classType);
            if ((this.m_struct_name != null)
                && (this.m_struct_name.equals(elmType))) {
                buffer.append("_orb().create_recursive_tc(id())");
            } else if (classType
                .getTagName().equals(Idl2XmlNames.OMG_scoped_name)) {
                String name = TypeManager.convert(classType
                    .getAttribute(Idl2XmlNames.OMG_name));
                buffer.append(name + "Helper.type()");
            } else {
                buffer.append(XmlType2Java.getTypecode(classType));
            }
            for (int i = 0; i < indexes.size(); i++) {
                buffer.append(")");
            }
        }
        if (objectName != null) {
            buffer.append(", null);\n");
        } else {
            buffer.append(";\n");
        }

        m_actual++;
    }

    public void generateJava(String structName, StringBuffer buffer,
                             String objectName, Element classType,
                             Vector indexes, Vector isArray)
        throws Exception
    {

        this.m_struct_name = structName;
        this.generateJava(buffer, objectName, classType, indexes, isArray);
    }

    private int m_actual = 0;

    private String m_struct_name = null;
}