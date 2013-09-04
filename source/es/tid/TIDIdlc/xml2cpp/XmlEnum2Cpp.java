/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 302 $
* Date: $Date: 2009-04-14 07:45:36 +0200 (Tue, 14 Apr 2009) $
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

package es.tid.TIDIdlc.xml2cpp;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.util.XmlUtil;

import java.io.*;
import org.w3c.dom.*;

/**
 * Generates Cpp for enumeration declarations.
 */
class XmlEnum2Cpp
    implements Idl2XmlNames
{

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();
    	
    	// Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           generateCode);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                           generateCode);

        // Types generation.
        // This warranties that the name of the holder and helper don't crash
        // with IDL types.
        String name = doc.getAttribute(OMG_name);
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name);

        //FileWriter writer;
        //BufferedWriter buf_writer;
        String fileName;

        //Header generation.
        fileName = name + h_ext;
        String idl_fn = XmlUtil.getIdlFileName(doc);

        if (generateCode) {
            Traces.println("XmlEnum2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        
        if (generateCode) {
        	StringBuffer contents = generateHppEnumDefHeader(doc, genPackage);
        	fm.addFile(contents, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
        	//writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        	
        }
        //Source generation.
        fileName = name + c_ext;
        if (generateCode) {
            Traces.println("XmlEnum2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }

        if (generateCode) {
            StringBuffer contents = generateCppEnumDefHeader(doc, genPackage);
            fm.addFile(contents, fileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
            
        	//writer = new FileWriter(sourceDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
            
        }

        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(doc, buffer, "enum",
                                                      name, genPackage);
        //contents = buffer.toString();
        if (generateCode) {
            fileName = name + "_ext" + h_ext;
            fm.addFile(buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        }
    }

    // Fills the buffer with the content of the header File.
    private StringBuffer generateHppEnumDefHeader(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : 
            genPackage+ "::" + name;
        // Generation of the header of the header file.
        XmlHppHeaderGenerator.generate(doc, buffer, "enum", name, genPackage);

        // Class header
        buffer.append("enum  ");
        buffer.append(name);
        buffer.append("\n{\n\t");
        // Items definition --> enum Color {red, green, yellow} ;
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String item = el.getAttribute(OMG_name);
            buffer.append(" " + item);

//             if (i == 0)
//             	buffer.append(" = 0");
            
            if (i < nodes.getLength() - 1) {
                buffer.append(",");
                //if ((i % 3) == 0)
                buffer.append("\n\t");
            } else
                buffer.append("\n}; // end of enum definition \n");
        }
        buffer.append("\n");

        // C++ out. --> typedef Color& Color_out;
        buffer.append("typedef ");
        buffer.append(name);
        buffer.append("& ");
        buffer.append(name);
        buffer.append("_out;");
        buffer.append("\n\n");

        // C++ ptr. --> typedef Color& Color_ptr;
        buffer.append("typedef ");
        buffer.append(name);
        buffer.append("* ");
        buffer.append(name);
        buffer.append("_ptr;");
        buffer.append("\n\n");

        // _tc_ Type Code Generation.
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append(" const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        buffer.append(XmlCppHelperGenerator.generateHpp(doc, null, genPackage, false));

        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        StringBuffer contents_buffer = XmlCppHolderGenerator.generateHpp(OMG_Holder_Simple,
                                                            genPackage,
                                                            nameWithPackage,
                                                            holderClass);
        buffer.append(contents_buffer);

        // Generation of the footer of the header class.
        XmlHppHeaderGenerator.generateFoot(buffer, "enum", name, genPackage);
        return buffer;
    }

    // Fills the buffer with the content of the ->source<- File.
    private StringBuffer generateCppEnumDefHeader(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("::") ? genPackage
            : genPackage + "::" + name;
        // Generation of the header of the Source File.
        XmlCppHeaderGenerator.generate(buffer, "enum", name, genPackage);

        // Helper Source Generator
        //XmlCppHelperGenerator.generateInsertExtract("basic",buffer,
        // nameWithPackage, helperClass);
        buffer.append(XmlCppHelperGenerator.generateCpp(doc, null, genPackage,false));

        // Holder Source Generator
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        String contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                            holderClass);
        buffer.append(contents);

        // footer.
        // Moved to External Operations.
        // XmlCppFooterGenerator.generate(doc,buffer,"enum",name,genPackage);
        return buffer;
    }
}
