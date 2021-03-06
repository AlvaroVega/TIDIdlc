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
import es.tid.TIDIdlc.xmlsemantics.RepositoryIdManager;
import es.tid.TIDIdlc.CompilerConf;
import java.io.*;

import org.w3c.dom.*;

/**
 * Generates Java for insert/extract operations of helper classes.
 */
class XmlJavaHelperGenerator
    implements Idl2XmlNames
{

    public static void generateInsertExtract(StringBuffer buffer, String type,
                                             String holderType)
    {
        if (CompilerConf.getPortable()) {
            generateInsertPortable(buffer, type, holderType);
            generateExtractPortable(buffer, type, holderType);
        } else {
            generateInsert(buffer, type, holderType);
            generateExtract(buffer, type, holderType);
        }
    }

    public static void generateInsertExtract_Object(StringBuffer buffer,
                                                    String type,
                                                    String holderType)
    {
        generateInsert_Object(buffer, type);
        generateExtract_Object(buffer, type, holderType);
    }

    private static void generateInsert(StringBuffer buffer, String type,
                                       String holderType)
    {
        buffer.append("  public static void insert(org.omg.CORBA.Any any, ");
        buffer.append(type);
        buffer.append(" value) {\n");

        buffer.append("    any.insert_Streamable(new ");
        buffer.append(holderType);
        buffer.append("(value));\n");

        buffer.append("  };\n\n");
    }

    private static void generateExtract(StringBuffer buffer, String type,
                                        String holderType)
    {
        buffer.append("  public static ");
        buffer.append(type);
        buffer.append(" extract(org.omg.CORBA.Any any) {\n");

        buffer.append("    if(any instanceof es.tid.CORBA.Any) {\n");
        buffer.append("      try {\n");
        buffer.append("        org.omg.CORBA.portable.Streamable holder =\n");
        buffer.append("          ((es.tid.CORBA.Any)any).extract_Streamable();\n");
        buffer.append("        if(holder instanceof ");
        buffer.append(holderType);
        buffer.append("){\n");
        buffer.append("          return ((");
        buffer.append(holderType);
        buffer.append(") holder).value;\n");
        buffer.append("        }\n");
        buffer.append("      } catch (Exception e) {}\n");
        buffer.append("    }\n\n");
        buffer.append("    return read(any.create_input_stream());\n");
        buffer.append("  };\n\n");
    }

    private static void generateInsertPortable(StringBuffer buffer,
                                               String type, String holderType)
    {
        buffer.append("  public static void insert(org.omg.CORBA.Any any, ");
        buffer.append(type);
        buffer.append(" value) {\n");

        buffer.append("    any.type(type());\n");
        buffer.append("    org.omg.CORBA.portable.OutputStream os = any.create_output_stream();\n");
        buffer.append("    write(os, value);\n");

        buffer.append("  };\n\n");
    }

    private static void generateExtractPortable(StringBuffer buffer,
                                                String type, String holderType)
    {
        buffer.append("  public static ");
        buffer.append(type);
        buffer.append(" extract(org.omg.CORBA.Any any) {\n");

        buffer.append("    ");
        buffer.append(type);
        buffer.append(" value = read(any.create_input_stream());\n");
        buffer.append("    return value;\n");

        buffer.append("  };\n\n");
    }

    private static void generateInsert_Object(StringBuffer buffer, String type)
    {
        buffer.append("  public static void insert(org.omg.CORBA.Any any, ");
        buffer.append(type);
        buffer.append(" value) {\n");

        buffer.append("    any.insert_Object((org.omg.CORBA.Object)value, type());\n");

        buffer.append("  };\n\n");
    }

    private static void generateExtract_Object(StringBuffer buffer,
                                               String type, String holderType)
    {
        buffer.append("  public static ");
        buffer.append(type);
        buffer.append(" extract(org.omg.CORBA.Any any) {\n");

        buffer.append("    org.omg.CORBA.Object obj = any.extract_Object();\n");
        buffer.append("    ");
        buffer.append(type);
        buffer.append(" value = narrow(obj);\n");
        buffer.append("    return value;\n");

        buffer.append("  };\n\n");
    }

    public static void generateRepositoryId(StringBuffer buffer, Element el)
    {
        String id = RepositoryIdManager.getInstance().get(el);
        buffer.append("  public static String id() {\n");
        if (id != null) {
            buffer.append("    return \"");
            buffer.append(id);
            buffer.append("\";\n");
        } else {
            buffer.append("    return \"repositoryIdNotFound\";  \n");
        }
        buffer.append("  };\n\n");
    }

}

