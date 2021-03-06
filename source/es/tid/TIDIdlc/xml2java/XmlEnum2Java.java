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
import es.tid.TIDIdlc.util.Traces;

import java.io.*;
import java.util.StringTokenizer;
import es.tid.TIDIdlc.xmlsemantics.Scope;

import org.w3c.dom.*;

/**
 * Generates Java for enumeration declarations.
 */
class XmlEnum2Java
    implements Idl2XmlNames
{

    /** Generate Java */
    public void generateJava(Element doc, String outputDirectory,
                             String genPackage, boolean generateCode)
        throws Exception
    {
        // Get package components
        String targetDirName = outputDirectory;
        if (targetDirName.charAt(targetDirName.length() - 1) == File.separatorChar) {
            targetDirName = targetDirName.substring(0,
            		targetDirName.length() - 1);
        }
        StringTokenizer tok = new StringTokenizer(genPackage, ".");
        while (tok.hasMoreTokens()) {
            targetDirName = targetDirName + File.separatorChar
                            + tok.nextToken();
        }

        if (generateCode) {
            // Make target directory
            File targetDir = new File(targetDirName);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }

        FileWriter writer;
        BufferedWriter buf_writer;
        String fileName, contents;

        // Enum generation
        fileName = doc.getAttribute(OMG_name) + ".java";
        if (generateCode) {
            Traces.println("XmlEnum2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        contents = generateJavaEnumDef(doc, genPackage);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // EnumHolder generation
        fileName = doc.getAttribute(OMG_name) + "Holder" + ".java";
        if (generateCode) {
            Traces.println("XmlEnum2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        String name = doc.getAttribute(OMG_name);
        contents = XmlJavaHolderGenerator.generate(genPackage, name, name);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
            		+ fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // EnumHelper generation
        fileName = doc.getAttribute(OMG_name) + "Helper" + ".java";
        if (generateCode) {
            Traces.println("XmlEnum2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
            		+ fileName + "...", Traces.USER);
        }
        contents = generateJavaHelperDef(doc, genPackage);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
            		+ fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }
    }

    private String generateJavaEnumDef(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        // Package header
        XmlJavaHeaderGenerator.generate(buffer, "enum", name, genPackage);

        // Class header
        buffer.append("public class ");
        buffer.append(name);
        buffer.append("\n   implements org.omg.CORBA.portable.IDLEntity {\n\n");

        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String item = el.getAttribute(OMG_name);
            buffer.append("  public static final int _" + item + " = " + i + ";\n");
            buffer.append("  public static final " + name + " " + item);
            buffer.append(" = new " + name + "(_" + item + ");\n");
        }
        buffer.append("\n");

        // Constructors
        buffer.append("  public int value() { return _value; }\n");

        buffer.append("  public static " + name + " from_int(int value) {\n");
        buffer.append("    switch (value) {\n");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String item = el.getAttribute(OMG_name);
            buffer.append("      case " + i + ": return " + item + ";\n");
        }
        buffer.append("    };\n");
        buffer.append("    return null;\n");
        buffer.append("  };\n");

        buffer.append("  protected ");
        buffer.append(name);
        buffer.append(" (int value) { _value = value; };\n");

        buffer.append("  public java.lang.Object readResolve()\n"); 
        // DAVV -añadido en revision 02-08-05 mapping de CORBA 2.6
        buffer.append("    throws java.io.ObjectStreamException\n");
        buffer.append("  {\n");
        buffer.append("    return from_int(value());\n");
        buffer.append("  }\n");

        buffer.append("  private int _value;\n");

        buffer.append("}\n");

        return buffer.toString();
    }

    private String generateJavaHelperDef(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        NodeList nodes = doc.getChildNodes();

        // Header
        String name = doc.getAttribute(OMG_name);
        XmlJavaHeaderGenerator.generate(buffer, "helper", name + "Helper",
                                        genPackage);

        // Class header
        buffer.append("abstract public class ");
        buffer.append(name);
        buffer.append("Helper {\n\n");
        buffer.append("  private static org.omg.CORBA.ORB _orb() {\n");
        buffer.append("    return org.omg.CORBA.ORB.init();\n");
        buffer.append("  }\n\n");

        XmlJavaHelperGenerator.generateInsertExtract(buffer, name, name + "Holder");

        buffer.append("  private static org.omg.CORBA.TypeCode _type = null;\n");
        buffer.append("  public static org.omg.CORBA.TypeCode type() {\n");
        buffer.append("    if (_type == null) {\n");
        buffer.append("      java.lang.String[] members = new java.lang.String[" + nodes.getLength() + "];\n");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String item = el.getAttribute(OMG_name);
            buffer.append("      members[" + i + "] = \"" + item + "\";\n");
        }
        buffer.append("      _type = _orb().create_enum_tc(id(), \"");
        buffer.append(name);
        buffer.append("\", members);\n");
        buffer.append("    }\n");
        buffer.append("    return _type;\n");
        buffer.append("  };\n\n");

        XmlJavaHelperGenerator.generateRepositoryId(buffer, doc);

        buffer.append("  public static ");
        buffer.append(name);
        buffer.append(" read(org.omg.CORBA.portable.InputStream is) {\n");
        buffer.append("    return " + name + ".from_int(is.read_long());\n");
        buffer.append("  };\n\n");

        buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, ");
        buffer.append(name);
        buffer.append(" val) {\n");
        buffer.append("    os.write_long(val.value());\n");
        buffer.append("  };\n\n");

        buffer.append("}\n");

        return buffer.toString();
    }

}