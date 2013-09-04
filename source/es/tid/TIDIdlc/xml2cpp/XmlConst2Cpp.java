/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 78 $
* Date: $Date: 2005-07-07 20:35:07 +0200 (Thu, 07 Jul 2005) $
* Last modified by: $Author: pra $
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

package es.tid.TIDIdlc.xml2cpp;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.IdlConstants;
import es.tid.TIDIdlc.util.*;

import java.io.*;
//import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Cpp for constant declarations.
 * <p>
 */
class XmlConst2Cpp
    implements Idl2XmlNames
{

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {
    	
        // No genero código si tiene Do_Not_Generate_Code
        String doNotGenerateCode = doc.getAttribute(OMG_Do_Not_Generate_Code);
        if (doNotGenerateCode!=null&&doNotGenerateCode.equals("TRUE"))
        	return;
    	
    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();
    	
        // Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           generateCode);

        //FileWriter writer;
        //BufferedWriter buf_writer;
        String fileName;

        // Const generation
        fileName = doc.getAttribute(OMG_name) + h_ext;
        if (generateCode) {
            Traces.println("XmlConst2Cpp->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        //contents = generateCppConstDef(doc,genPackage);
        if (generateCode) {
            StringBuffer final_buffer = generateHppConstDef(doc, genPackage);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        	
            String idl_fn = XmlUtil.getIdlFileName(doc);
        	fm.addFile(final_buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
        	
        }

    }

    private StringBuffer generateHppConstDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String scopedName = doc.getAttribute(OMG_scoped_name);
        // Package header
        XmlHppHeaderGenerator.generate(doc, buffer, "constant", name,
                                       genPackage);

        NodeList nodes = doc.getChildNodes();

        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        buffer.append("static const ");
        String kind = XmlType2Cpp.getDeepKind(typeEl);	//BUG #44
        buffer.append(type);
        buffer.append(" " + name + " = ");
        /*
         * buffer.append(" "+name+" = ("); buffer.append(type);
         * buffer.append(")");
         */

        // Expre generation
        /*
         * Element exprEl = (Element)nodes.item(1); 
         * Object expr = XmlExpr2Cpp.getExpr(exprEl, type);
         * IdlConstants.getInstance().add(scopedName, type, expr);
         */
        Object expr = IdlConstants.getInstance().getValue(scopedName);
        String typeExpr = IdlConstants.getInstance().getType(scopedName);
        //BUG #44
        if (kind.equals(OMG_string)) {
        	buffer.append("\"" + XmlExpr2Cpp.toString(expr, typeExpr) + "\"");
        } else if (kind.equals(OMG_char)) {
           	//buffer.append("'" + XmlExpr2Cpp.toString(expr, typeExpr) + "'");
           	buffer.append(XmlExpr2Cpp.toString(expr, kind));
            //BUG #79------------------------------------------
        } else {
       		buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
        }

        buffer.append(";\n");
        XmlHppHeaderGenerator
            .generateFoot(buffer, "constant", name, genPackage);
        return buffer;
    }

}