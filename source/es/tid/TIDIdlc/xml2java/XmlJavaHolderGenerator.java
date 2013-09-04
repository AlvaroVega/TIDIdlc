/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 198 $
* Date: $Date: 2007-05-10 12:15:43 +0200 (Thu, 10 May 2007) $
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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

import java.io.*;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for all holder classes.
 */
class XmlJavaHolderGenerator
    implements Idl2XmlNames
{

    public static String generate(String genPackage, String className,
                                  String classType)
    {
        StringBuffer buffer = new StringBuffer();

        // Header
        XmlJavaHeaderGenerator.generate(buffer, "holder", className + "Holder",
                                        genPackage);

        // Class header
        buffer.append("final public class ");
        buffer.append(className);
        buffer.append("Holder\n   implements org.omg.CORBA.portable.Streamable {\n\n");

        buffer.append("  public " + classType + " value; \n");
        //buffer.append("  public " + className + "Holder() {} \n");
        // Initialize default constructor of sequence holders to avoid marshall crashes        
        buffer.append("  public " + className + "Holder() {\n");
        if (classType.endsWith("[]")){        	
        	String initType = classType.replaceFirst("]", "0]");
        	buffer.append("    value = new " + initType + ";\n");
        } 
        buffer.append("  }\n\n");
        
        buffer.append("  public " + className + "Holder(" + classType + " initial) {\n");
        buffer.append("    value = initial;\n");
        buffer.append("  }\n\n");

        buffer.append("  public void _read(org.omg.CORBA.portable.InputStream is) {\n");
        buffer.append("    value = ");
        buffer.append(genPackage);
        if (!genPackage.equals(""))
            buffer.append(".");
        buffer.append(className);
        buffer.append("Helper.read(is);\n");
        buffer.append("  };\n\n");

        buffer.append("  public void _write(org.omg.CORBA.portable.OutputStream os) {\n");
        buffer.append("    ");
        buffer.append(genPackage);
        if (!genPackage.equals(""))
            buffer.append(".");
        buffer.append(className);
        buffer.append("Helper.write(os, value);\n");
        buffer.append("  };\n\n");

        buffer.append("  public org.omg.CORBA.TypeCode _type() {\n");
        buffer.append("    return ");
        buffer.append(genPackage);
        if (!genPackage.equals(""))
            buffer.append(".");
        buffer.append(className);
        buffer.append("Helper.type();\n");
        buffer.append("  };\n\n");

        buffer.append("}\n");
        return buffer.toString();
    }

}

