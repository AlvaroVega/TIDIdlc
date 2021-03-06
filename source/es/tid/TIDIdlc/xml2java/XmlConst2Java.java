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
import es.tid.TIDIdlc.xmlsemantics.IdlConstants;
import es.tid.TIDIdlc.util.Traces;

import java.io.*;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for constant declarations.
 */
class XmlConst2Java
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
            targetDirName = targetDirName.substring(0, targetDirName.length() - 1);
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

        // Const generation
        fileName = doc.getAttribute(OMG_name) + ".java";
        if (generateCode) {
            Traces.println("XmlConst2Java->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        if (generateCode) {
            contents = generateJavaConstDef(doc, genPackage);
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

    }

    private String generateJavaConstDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String scopedName = doc.getAttribute(OMG_scoped_name);
        // Package header
        XmlJavaHeaderGenerator.generate(buffer, "const", name, genPackage);

        // Class header
        buffer.append("public interface ");
        buffer.append(name);
        buffer.append(" {\n");

        NodeList nodes = doc.getChildNodes();

        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        buffer.append("  ");
        buffer.append(type);
        buffer.append(" value = (");
        buffer.append(type);
        buffer.append(")");

        Object expr = IdlConstants.getInstance().getValue(scopedName);
        String typeExpr = IdlConstants.getInstance().getType(scopedName);
        buffer.append(XmlExpr2Java.toString(expr, typeExpr));
        buffer.append(";\n");

        buffer.append("}\n");

        return buffer.toString();
    }

}