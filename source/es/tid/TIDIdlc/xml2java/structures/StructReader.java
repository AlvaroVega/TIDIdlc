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
public class StructReader extends StructProcessor
{

    public void generateJava(StringBuffer buffer, String objectName,
                             Element classType, Vector indexes, Vector isArray)
        throws Exception
    {
        if (indexes.size() == 0) {
            // Simple member
            String classTypeName = XmlType2Java.getTypeReader(classType, "is");
            if (objectName != null)
                buffer.append("    result." + objectName + " = "
                              + classTypeName + ";\n");
            else
                buffer.append("    result = " + classTypeName + ";\n");
        } else {
            // Array member
            generateJavaArrayReader(buffer, objectName, classType, indexes,
                                    isArray, 0);
        }
    }

    private void generateJavaArrayReader(StringBuffer buffer,
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
            boolean isBoundedString = ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) ||
            XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring)) && 
			classType.getFirstChild() != null);

            if (isBoundedString && indexes.size() - 1 == index) {
                // We have a bounded string. Do loop
                generateJavaArrayReader(buffer, objectName, classType, indexes,
                                        isArray, index + 1);
            } else {
                boolean isAnArray = ((Boolean) isArray.elementAt(index))
                    .booleanValue();

                // index reading

                // If we have an array we don't have to read it's length,
                // because it's fixed
                if (!isAnArray) {
                    if (!(indexes.elementAt(index) instanceof Long)) {
                        identation(buffer, index);
                        buffer.append("  int " + indexes.elementAt(index)
                                      + " = is.read_ulong();\n");
                    } else {
                        identation(buffer, index);
                        buffer.append("  int length" + index
                                      + " = is.read_ulong();\n");
                        identation(buffer, index);
                        buffer.append("  if (length" + index + " > "
                                      + indexes.elementAt(index) + ")\n");
                        identation(buffer, index);
                        buffer
                            .append("     throw new org.omg.CORBA.BAD_PARAM(\"Invalid sequence length\");\n");
                    }
                }

                // array creation
                String init = null;
                String classTypeName = XmlType2Java.getType(classType);
                String classTypeBasic = basicType(classTypeName);
                int num = numIndexes(classTypeName);
                if (num == 0) {
                    if (objectName != null)
                        init = initializeElement(indexes, "result."
                        		+ objectName,
								classTypeName, index, true,
								isBoundedString);
                    else
                        init = initializeElement(indexes, "result",
                        		classTypeName, index, true,
								isBoundedString);
                } else {
                    classTypeBasic += "[" + indexes.elementAt(index) + "]";
                    for (int k = 1; k < num; k++)
                        classTypeBasic += "[]";
                    if (objectName != null)
                        init = initializeElement(indexes, "result."
                        		+ objectName,
								classTypeBasic, index, false,
								isBoundedString);
                    else
                        init = initializeElement(indexes, "result",
                        		classTypeBasic, index, false,
								isBoundedString);
                }
                identation(buffer, index);
                buffer.append("  " + init + ";\n");

                // loop
                identation(buffer, index);
                if (isAnArray)
                    buffer.append("  for (int i" + index + "=0; i" + index
                                  + "<" + indexes.elementAt(index) + "; i"
                                  + index + "++) {\n");
                else if (!(indexes.elementAt(index) instanceof Long))
                    buffer.append("  for (int i" + index + "=0; i" + index
                                  + "<" + indexes.elementAt(index) + "; i"
                                  + index + "++) {\n");
                else
                    buffer.append("  for (int i" + index + "=0; i" + index
                                  + "< length" + index + "; i" + index
                                  + "++) {\n");
                generateJavaArrayReader(buffer, objectName, classType, indexes,
                                        isArray, index + 1);
                identation(buffer, index);
                buffer.append("  }\n");
            }
        } else {
            // value reading
            String classTypeName;
            if (classType.getTagName().equals(Idl2XmlNames.OMG_scoped_name)) {
                String name = TypeManager.convert(classType
                    .getAttribute(Idl2XmlNames.OMG_name));
                classTypeName = name + "Helper.read(is)";
            } else {
                classTypeName = XmlType2Java.getTypeReader(classType, "is");
            }
            if ((XmlType2Java
                .getTypedefType(classType).equals(Idl2XmlNames.OMG_string) || XmlType2Java
                .getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring))
                && classType.getFirstChild() != null)
                // We have a bounded string
                index--; // Bug temporal fixing
            identation(buffer, index);
            if (objectName != null)
                buffer.append("  result." + objectName + getIndexes(index)
                              + " = " + classTypeName + ";\n");
            else
                buffer.append("  result" + getIndexes(index) + " = "
                              + classTypeName + ";\n");
            if ((XmlType2Java
                .getTypedefType(classType).equals(Idl2XmlNames.OMG_string) || XmlType2Java
                .getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring))
                && classType.getFirstChild() != null) {
                // We have a bounded string
                identation(buffer, index);
                if (objectName != null) {
                    buffer.append("  if (result." + objectName
                                  + getIndexes(index) + ".length() > "
                                  + indexes.lastElement() + ")\n");
                } else {
                    buffer.append("  if (result" + getIndexes(index)
                                  + ".length() > " + indexes.lastElement()
                                  + ")\n");
                }
                identation(buffer, index);
                buffer
                    .append("    throw new org.omg.CORBA.BAD_PARAM(\"Invalid string length\");\n");
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