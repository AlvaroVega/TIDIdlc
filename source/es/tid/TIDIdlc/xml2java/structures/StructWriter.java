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
public class StructWriter extends StructProcessor
{

    public void generateJava(StringBuffer buffer, String objectName,
                             Element classType, Vector indexes, Vector isArray)
        throws Exception
    {
        if (indexes.size() == 0) {
            // Simple member
            String classTypeName;
            if (objectName != null)
                classTypeName = XmlType2Java.getTypeWriter(classType, "os",
                		"val." + objectName);
            else
                classTypeName = XmlType2Java.getTypeWriter(classType, "os",
                		"val");
            buffer.append("    " + classTypeName + ";\n");
        } else {
            // Array member
            generateJavaArrayWriter(buffer, objectName, classType, indexes, isArray, 0);
        }
    }

    private void generateJavaArrayWriter(StringBuffer buffer,
                                         String objectName, Element classType,
                                         Vector indexes, Vector isArray,
                                         int index)
        throws Exception
    {
        if (indexes.size() > index) {
    		// DAVV
    		// -
    		// bounded
    		// string
            if ((indexes.size() - 1 == index)  &&
            			((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) || 
            				XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring))  &&
							classType.getFirstChild() != null)) {
                // We have a bounded string
                identation(buffer, index);
                if (objectName != null)
                    buffer.append("  if (val." + objectName + getIndexes(index)
                                  + ".length() > " + indexes.lastElement()
                                  + ")\n");
                else
                    buffer.append("  if (val" + getIndexes(index)
                                  + ".length() > " + indexes.lastElement()
                                  + ")\n");
                identation(buffer, index);
                buffer.append("    throw new org.omg.CORBA.BAD_PARAM(\"Invalid string length\");\n");
                generateJavaArrayWriter(buffer, objectName, classType, indexes, isArray, index + 1);
            } else {
                boolean isAnArray = ((Boolean) isArray.elementAt(index)).booleanValue();

                // index writing
                String init = null;
                String classTypeName = XmlType2Java.getType(classType);
                String bounds;
                if (objectName != null)
                    bounds = "val." + objectName + getIndexes(index) + ".length";
                else
                    bounds = "val" + getIndexes(index) + ".length";

                if (indexes.elementAt(index) instanceof Long) {
                    identation(buffer, index);
                    if (isAnArray) {
                        buffer.append("  if (" + bounds + " != "
                                      + indexes.elementAt(index) + ") \n");
                        identation(buffer, index);
                        buffer
                            .append("    throw new org.omg.CORBA.BAD_PARAM(\"Invalid array length\");\n");
                    } else {
                        buffer.append("  if (" + bounds + " > " + indexes.elementAt(index) + ") \n");
                        identation(buffer, index);
                        buffer
                            .append("    throw new org.omg.CORBA.BAD_PARAM(\"Invalid sequence length\");\n");
                    }
                }
                if (isAnArray) {
                    identation(buffer, index);
                    buffer.append("  for (int i" + index + "=0; i" + index
                                  + "<" + indexes.elementAt(index) + "; i"
                                  + index + "++) {\n");
                } else {
                    identation(buffer, index);
                    buffer.append("  os.write_ulong(");
                    buffer.append(bounds);
                    buffer.append(");\n");
                    identation(buffer, index);
                    buffer.append("  for (int i" + index + "=0; i" + index
                                  + "<" + bounds + "; i" + index + "++) {\n");
                }
                // loop
                generateJavaArrayWriter(buffer, objectName, classType, indexes,
                                        isArray, index + 1);
                identation(buffer, index);
                buffer.append("  }\n");
            }
        } else {
            // value writing
			// DAVV
			// -
			// bounded
			// string
            if ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) || 
                XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring)) &&
				classType.getFirstChild() != null)
            		index--;

            String classTypeName;
            if (classType.getTagName().equals(Idl2XmlNames.OMG_scoped_name)) {
                String name = TypeManager.convert(classType
                    .getAttribute(Idl2XmlNames.OMG_name));
                if (objectName != null)
                    classTypeName = name + "Helper.write(os,val." + objectName
                                    + getIndexes(index) + ")";
                else
                    classTypeName = name + "Helper.write(os,val"
                                    + getIndexes(index) + ")";
            } else if (objectName != null)
                classTypeName = XmlType2Java
                    .getTypeWriter(classType, "os", "val." + objectName
                                                    + getIndexes(index));
            else
                classTypeName = XmlType2Java
                    .getTypeWriter(classType, "os", "val" + getIndexes(index));

			// DAVV
			// -
			// bounded
			// string
            if ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) || 
                XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring))  &&
				classType.getFirstChild() != null) {
                // We have a bounded string
                if (objectName != null)
                    buffer
                        .append("\t" + XmlType2Java.getTypeWriter(
                        		classType,
								"os",
								"val."
								+ objectName
								+ getIndexes(index))
								+ ";\n");
                else
                    buffer.append("\t" + XmlType2Java.getTypeWriter(classType, "os",
                    			"val" + getIndexes(index))
								+ ";\n");
            } else {
                identation(buffer, index);
                buffer.append("  " + classTypeName + ";\n");
            }
        }
    }

    public void generateJava(String structName, StringBuffer buffer,
                             String objectName, Element classType,
                             Vector indexes, Vector isArray)
        throws Exception
    {

        this.m_struct_name = structName;
        this.generateJava(buffer, objectName, classType, indexes, isArray);
    }

    private String m_struct_name = null;

}