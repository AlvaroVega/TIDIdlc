/*
 * MORFEO Project
 * http://www.morfeo-project.org
 *
 * Component: TIDIdlc
 * Programming Language: Java
 *
 * File: $Source$
 * Version: $Revision: 318 $
 * Date: $Date: 2010-01-13 08:47:59 +0100 (Wed, 13 Jan 2010) $
 * Last modified by: $Author: avega $
 *
 * (C) Copyright 2004 Telef�nica Investigaci�n y Desarrollo
 *     S.A.Unipersonal (Telef�nica I+D)
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
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.util.XmlUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.Vector;
//import es.tid.TIDIdlc.xml2cpp.structures.*;
//import es.tid.TIDIdlc.xml2cpp.valuetypes.ValueType;
import es.tid.TIDIdlc.CompilerConf;

import org.w3c.dom.*;

/**
 * Generates Cpp for valuetypes.
 */
class XmlValuetype2Cpp extends XmlValuetypeUtils2Cpp
    implements Idl2XmlNames
{

    private boolean m_generate;

    private Vector m_supported_interfaces = null;

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();
    	
        if (doc.getAttribute(OMG_fwd).equals(OMG_true))
            return; // it is a forward declaration

        m_generate = generateCode;

        String name = doc.getAttribute(OMG_name);
        String OBVname = genPackage.equals("") ? "OBV_" + name : name;
        boolean isBoxed = doc.getAttribute(OMG_boxed).equals(OMG_true);
        boolean isAbstract = doc.getAttribute(OMG_abstract).equals(OMG_true);

        // Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                          generateCode);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                          generateCode);
        String OBVgenPackage = genPackage.equals("") ? "" : "OBV_" + genPackage;
        String OBVheaderDir = "";
        String OBVsourceDir = "";
        if (!isAbstract && !isBoxed) {
            OBVheaderDir = Xml2Cpp.getDir(OBVgenPackage, headerDirectory,
                                          generateCode);
            OBVsourceDir = Xml2Cpp.getDir(OBVgenPackage, sourceDirectory,
                                          generateCode);
        }

        //FileWriter writer;
        StringBuffer buffer, auxBuffer;
        //BufferedWriter buf_writer;
        String sourceFileName, headerFileName;
        StringBuffer sourceContents = new StringBuffer();
        StringBuffer headerContents = new StringBuffer();
        String OBVsourceFileName = "", OBVheaderFileName = "";
        StringBuffer OBVheaderContents = new StringBuffer();
        StringBuffer OBVsourceContents = new StringBuffer();

        /** **** Valuetype generation header ***** */
        headerFileName = name + h_ext;
        if (!isAbstract && !isBoxed)
            OBVheaderFileName = OBVname + h_ext;

        if (generateCode) {
            Traces.println("XmlValuetype2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + headerFileName + "...", Traces.USER);
        }

        // package header
        buffer = new StringBuffer();
        XmlHppHeaderGenerator.generate(doc, buffer, "valuetype", name,
                                       genPackage);
        headerContents = buffer;
        if (!isAbstract && !isBoxed) {
            buffer = new StringBuffer();
            XmlHppHeaderGenerator.generate(doc, buffer, "valuetype", OBVname,
                                           OBVgenPackage);
            OBVheaderContents = buffer;
        }

        boolean thereAreOps = false;

        // the valuetype
        if (!isBoxed) { // valuetype headers
            buffer = new StringBuffer();
            auxBuffer = new StringBuffer();
            thereAreOps = generateHppValuetype(doc, sourceDirectory,
                                               headerDirectory, genPackage,
                                               buffer, auxBuffer, expanded, h_ext, c_ext);
            headerContents.append(buffer);
            if (!isAbstract)
                OBVheaderContents.append(auxBuffer);
        } else
            // valuebox header
            headerContents.append(generateHppBoxedValuetype(doc, genPackage));

        headerContents.append(generateHppHelperDef(doc, genPackage)); // the helper
        headerContents.append(XmlCppHolderGenerator.generateHpp(
                                                                OMG_valuetype, genPackage,
                                                                genPackage.equals("") ? name : genPackage + "::"
                                                                + name, 
                                                                XmlType2Cpp.getHolderName(genPackage + "::" + name))); 
        // the holder

        buffer = new StringBuffer();
        XmlHppHeaderGenerator.generateFoot(buffer, "valuetype", name,
                                           genPackage);
        headerContents.append(buffer);
        if (!isAbstract && !isBoxed) {
            buffer = new StringBuffer();
            XmlHppHeaderGenerator.generateFoot(buffer, "valuetype", OBVname,
                                               OBVgenPackage);
            OBVheaderContents.append(buffer);
        }

        if (generateCode) {
            String idl_fn = XmlUtil.getIdlFileName(doc);

            fm.addFile(headerContents, headerFileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
            //writer = new FileWriter(headerDir + File.separatorChar
            //                        + headerFileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerContents);
            //buf_writer.close();

            if (!isAbstract && !isBoxed) {
            	
            	fm.addFile(OBVheaderContents, OBVheaderFileName, OBVheaderDir, idl_fn, FileManager.TYPE_OBV_HEADER);
                //writer = new FileWriter(OBVheaderDir + File.separatorChar
                //                        + OBVheaderFileName);
                //buf_writer = new BufferedWriter(writer);
                //buf_writer.write(OBVheaderContents);
                //buf_writer.close();
            }
        }

        /** *********** Valuetype generation Source *************** */
        sourceFileName = name + c_ext;
        if (!isAbstract && !isBoxed)
            OBVsourceFileName = OBVname + c_ext;

        if (generateCode) {
            Traces.println("XmlValuetype2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + sourceFileName + "...", Traces.USER);
        }

        // Package header
        buffer = new StringBuffer();
        XmlCppHeaderGenerator.generate(buffer, "valuetype", name, genPackage);
        sourceContents = buffer;
        if (!isAbstract && !isBoxed) {
            buffer = new StringBuffer();
            XmlCppHeaderGenerator.generate(buffer, "valuetype", OBVname,
                                           OBVgenPackage);
            OBVsourceContents.append(buffer);
            String routeName = "";
            StringTokenizer tokenizer = new StringTokenizer(OBVgenPackage, "::");
            while (tokenizer.hasMoreTokens())
                routeName += tokenizer.nextToken() + File.separatorChar;
            sourceContents.append("#include \"" + routeName + OBVheaderFileName
                                  + "\"\n\n");
        }

        sourceContents.append(generateCppHelperDef(doc, genPackage)); // the helper

        // Holder Source Generator
        String holderClass = XmlType2Cpp.getHolderName(genPackage + "::" + name);
        String contents = XmlCppHolderGenerator.generateCpp(genPackage, name, holderClass);
        
        sourceContents.append(contents);


        if (!isBoxed) { // the valuetype
            buffer = new StringBuffer();
            auxBuffer = new StringBuffer();
            generateCppValuetype(doc, genPackage, buffer, auxBuffer,
                                 thereAreOps);
            sourceContents.append(buffer);
            if (!isAbstract) {
                if (auxBuffer.length() > 0)
                    OBVsourceContents.append(auxBuffer);
                else
                    OBVsourceContents = new StringBuffer();
            }
        } else
            // the valuebox
            sourceContents.append(generateCppBoxedValuetype(doc, genPackage));

        if (generateCode) {
            String idl_fn = XmlUtil.getIdlFileName(doc);

            fm.addFile(sourceContents, sourceFileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
            //writer = new FileWriter(sourceDir + File.separatorChar
            //                        + sourceFileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(sourceContents);
            //buf_writer.close();

            if (!isAbstract && !isBoxed && OBVsourceContents.length() > 0) {
            	fm.addFile(OBVsourceContents, OBVsourceFileName, OBVsourceDir, idl_fn, FileManager.TYPE_OBV_SOURCE);
            	
                //writer = new FileWriter(OBVsourceDir + File.separatorChar
                //                        + OBVsourceFileName);
                //buf_writer = new BufferedWriter(writer);
                //buf_writer.write(OBVsourceContents);
                //buf_writer.close();
            }
        }

        /** *********** Valuetype POA *************** */
        if (!CompilerConf.getNoSkel() && !isBoxed
            && m_supported_interfaces.size() > 0) {
            int i = 0;
            boolean nonAbstractSupported = false;
            while (i < m_supported_interfaces.size() && !nonAbstractSupported) {
                Element supported = (Element) m_supported_interfaces.get(i);
                nonAbstractSupported = 
                    (!supported.getAttribute(OMG_abstract).equals(OMG_true));
                i++;
            }
            if (nonAbstractSupported) {
                XmlInterfaceSkeleton2Cpp genSkeleton = new XmlInterfaceSkeleton2Cpp();
                StringBuffer POAHeaderContents = genSkeleton.generateHpp(doc,
                                                                         genPackage);
                StringBuffer POASourceContents = genSkeleton.generateCpp(doc,
                                                                         genPackage);
                if (generateCode) {
                    String POAheaderDir, POAsourceDir, POASourcefileName, POAHeaderfileName;
                    if (genPackage != "") { // valuetypes en un m�dulo
                        POAheaderDir = Xml2Cpp.getDir("POA_" + genPackage,
                                                      headerDirectory,
                                                      generateCode);
                        POAsourceDir = Xml2Cpp.getDir("POA_" + genPackage,
                                                      sourceDirectory,
                                                      generateCode);
                        POASourcefileName = doc.getAttribute(OMG_name) + c_ext;
                        POAHeaderfileName = doc.getAttribute(OMG_name) + h_ext;
                    } else { // DAVV - valuetypes en �mbito global
                        POAheaderDir = Xml2Cpp.getDir(genPackage,
                                                      headerDirectory,
                                                      generateCode);
                        POAsourceDir = Xml2Cpp.getDir(genPackage,
                                                      sourceDirectory,
                                                      generateCode);
                        POASourcefileName = "POA_" + doc.getAttribute(OMG_name)
                            + c_ext;
                        POAHeaderfileName = "POA_" + doc.getAttribute(OMG_name)
                            + h_ext;
                    }
                    String idl_fn = XmlUtil.getIdlFileName(doc);

                    fm.addFile(POAHeaderContents, POAHeaderfileName, POAheaderDir, idl_fn, FileManager.TYPE_POA_HEADER);
                    fm.addFile(POASourceContents, POASourcefileName, POAsourceDir, idl_fn, FileManager.TYPE_POA_SOURCE);
                    //writer = new FileWriter(POAsourceDir + File.separatorChar
                    //                        + POASourcefileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(POASourceContents);
                    //buf_writer.close();
                    //writer = new FileWriter(POAheaderDir + File.separatorChar
                    //                        + POAHeaderfileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(POAHeaderContents);
                    //buf_writer.close();
                }
            }
        }

        /** ************ External any operations ******************* */
        // Design of the header files, Any operations outside main file.
        StringBuffer headerBuff = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(doc, headerBuff,
                                                      OMG_valuetype, name,
                                                      genPackage);

        if (this.m_generate) {
            headerFileName = name + "_ext" + h_ext;
            String idl_fn = XmlUtil.getIdlFileName(doc);

            fm.addFile(headerBuff, headerFileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
            
            //writer = new FileWriter(headerDir + File.separatorChar
            //                        + headerFileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(headerBuff.toString());
            //buf_writer.close();
        }

    }

    private static void generateWriteImplementation(Element doc,
                                                    StringBuffer buffer)
        throws Exception
    {
    	String tagDef = doc.getTagName();
    	if (tagDef.equals(OMG_valuetype)) {
            NodeList preNodes = doc.getChildNodes();
            for (int j = 0; j < preNodes.getLength(); j++) {
                Element preEl = (Element) preNodes.item(j);
                if (preEl.getTagName().equals(OMG_state_member)) {
                    NodeList nodes = preEl.getChildNodes();
                    String name = "";
                    Element type = null;
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element el = (Element) nodes.item(i);
                        String tag = el.getTagName();
                        if (tag.equals(OMG_simple_declarator)) {
                            name = el.getAttribute(OMG_name);
                        } else if (tag.equals(OMG_array)) {
                        } else {
                            type = el;
                        }
                        if (name != "") {
                            buffer.append("\t" + XmlType2Cpp.getTypeWriter(type,
                                                                           "outs", name + "()")+ ";\n\n");
                        }
                    }
                }
            }
        }
    }
    
    private static void generateReadImplementation(Element doc,
                                                   String genPackage,
                                                   StringBuffer buffer)
        throws Exception
    {
    	String tagDef = doc.getTagName();
    	
    	if (tagDef.equals(OMG_valuetype)) {
            NodeList preNodes = doc.getChildNodes();
            for (int j = 0; j < preNodes.getLength(); j++) {
                Element preEl = (Element) preNodes.item(j);
                if (preEl.getTagName().equals(OMG_state_member)) {
                    NodeList nodes = preEl.getChildNodes();
                    String name = "";
                    String typeStr = "";
                    Element type = null;
                    String reader = "";
                    String def = "";
                    for (int i = 0; i < nodes.getLength(); i++) {
                        Element el = (Element) nodes.item(i);
                        String tag = el.getTagName();
                        if (tag.equals(OMG_simple_declarator)) {
                            name = el.getAttribute(OMG_name);
                        } else if (tag.equals(OMG_array)) {
                        } else {
                            def = XmlType2Cpp.getDefinitionType(el);
                            typeStr = XmlType2Cpp.getModifierType(el);
                            if (typeStr.endsWith("&"))
                                typeStr = typeStr.substring(0, typeStr.length() - 1);
                            if (typeStr.startsWith("const "))
                                typeStr = typeStr.substring(6);
                            reader = XmlType2Cpp.getTypeReader(el, "ins");
                            type = el;
                        }

                        if (name != "") {
                        	
                            if (def.equals(OMG_struct) || def.equals(OMG_union) ||
                            	def.equals(OMG_exception)) {
                                    buffer.append("\t" + reader + "" + name + "());\n\n");
                            } else {
                                    if (def.equals(OMG_sequence)) {
                                    	// Performance improvement: avoid call to copy constructor
                                        buffer.append("\t" + reader + name + "()" + ");\n");                                        
                                    } else {
                                    
                                    	if (typeStr.equals("char*"))
                                    		buffer.append("\tCORBA::String_var _" + name + ";\n");
                                    	else
                                    		buffer.append("\t" + typeStr + " _" + name + ";\n");

                                    	if (reader.lastIndexOf("fixed") >= 0) {
                                    		buffer.append("\t"
                                              + reader
                                              + "_"
                                              + name
                                              + ", "
                                              + XmlExpr2Cpp.getIntExpr(type.getFirstChild())
                                              + ", "
                                              + XmlExpr2Cpp.getIntExpr(type.getLastChild()) + ");\n");
                                    	} else {                                
                                    		if (typeStr.equals("char*"))
                                    			buffer.append("\t" + reader + "_" + name + ".out());\n");
                                    		else
                                    			buffer.append("\t" + reader + "_" + name + ");\n");
                                    	}
                                    	buffer.append("\t" + name + "(_" + name + ");\n\n");
                                    	}
                            }                               
                            // END AVG
                                                	                                                                                
                        }
                    }
                }
            }
        }
    }

        
    private void generateCppValuetype(Element doc, String genPackage,
                                      StringBuffer baseBuffer,
                                      StringBuffer OBVBuffer,
                                      boolean thereAreOps)
        throws Exception
    {

        StringBuffer factoryBuffer = new StringBuffer();

        String name = doc.getAttribute(OMG_name);
        
        generateCppValueContents(baseBuffer, OBVBuffer, doc, name, genPackage, thereAreOps);

        String nameWithPackage = genPackage.equals("") ? name : genPackage
            + "::" + name;
        
        baseBuffer.append("::CORBA::TypeCode_ptr " + nameWithPackage
                          + "::_type() const { return CORBA::TypeCode::_duplicate("+genPackage+"::_tc_" + name + "); }\n\n");
        
        baseBuffer.append("void "+nameWithPackage+"::_write(::TIDorb::portable::OutputStream& outs) const {\n");
        generateWriteImplementation(doc, baseBuffer);
        baseBuffer.append("}\n\n");
        
        

        baseBuffer.append("void "+nameWithPackage+"::_read(::TIDorb::portable::InputStream& ins) {\n");
        generateReadImplementation(doc, genPackage, baseBuffer);
        baseBuffer.append("}\n\n");
        
        baseBuffer.append(nameWithPackage + "* " + nameWithPackage
                          + "::_downcast(CORBA::ValueBase* value) {\n");
        baseBuffer.append("\tif (value == NULL)\n");
        baseBuffer.append("\t\treturn NULL;\n");
        baseBuffer.append("\treturn dynamic_cast<" + nameWithPackage
                          + "*>(value);\n");
        baseBuffer.append("}\n");

        generateCppFactoryDef(factoryBuffer, genPackage, doc, thereAreOps);

        baseBuffer.append(factoryBuffer.toString());
    }

    private boolean generateHppValuetype(Element doc, String sourceDirectory,
                                         String headerDirectory,
                                         String genPackage,
                                         StringBuffer baseBuffer,
                                         StringBuffer OBVBuffer,
                                         boolean expanded, 
                                         String h_ext, 
                                         String c_ext)
        throws Exception
    {

        StringBuffer factoryBuffer = new StringBuffer();

        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
            + "::" + name;
        String OBVname = genPackage.equals("") ? "OBV_" + name : name;

        // _tc_ Type Code Generation.
        baseBuffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        baseBuffer.append("const ::CORBA::TypeCode_ptr _tc_");
        baseBuffer.append(name);
        baseBuffer.append(";\n\n");
        
        baseBuffer.append("class " + XmlType2Cpp.getHelperName(name) + ";\n\n"); 
        // declaracion
        // adelantada

        // 'Preparaci�n' de clase base abstracta
        baseBuffer.append("class ");
        baseBuffer.append(name);
        generateHppInheritance(baseBuffer, doc);
        baseBuffer.append(" {\n\n");
        baseBuffer.append("\tfriend class "
                          + XmlType2Cpp.getHelperName(nameWithPackage)
                          + ";\n\n");
        // para poder utilizar accesores y modificadores correspondientes
        // a miembros protected del IDL

        XmlHppHeaderGenerator.includeForwardDeclarations(doc, baseBuffer,
                                                         "valuetype", name,
                                                         genPackage);
        XmlHppHeaderGenerator.includeChildrenHeaderFiles(doc, baseBuffer,
                                                         "valuetype", name,
                                                         genPackage);

        OBVBuffer.append("class " + OBVname + " : public virtual "
                         + nameWithPackage);

        boolean thereAreOps = generateHppValueContents(baseBuffer, OBVBuffer,
                                                       doc, sourceDirectory,
                                                       headerDirectory, name,
                                                       genPackage, expanded, h_ext, c_ext);

        generateHppConstructionMethods(baseBuffer, OBVBuffer, doc, genPackage,
                                       thereAreOps);

        baseBuffer.append("};\n");
        OBVBuffer.append("};\n");

        generateHppFactoryDef(factoryBuffer, doc, thereAreOps);

        String factory = factoryBuffer.toString();
        if (!factory.equals(""))
            baseBuffer.append("\n\n" + factoryBuffer.toString());

        return thereAreOps;
    }

    private String generateCppBoxedValuetype(Element doc, String genPackage)
        throws Exception
    {

        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage + "::" + name;
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        //String thetype = XmlType2Cpp.getTypedefType(typeEl);
        String definition = XmlType2Cpp.getDefinitionType(typeEl);
        String kind = "";
        if (definition.equals(OMG_kind))
            kind = XmlType2Cpp.getDeepKind(typeEl);

        // Casos:

        // 2. Struct
        if (definition.equals(OMG_struct)) {
            
            // Default constructor
            buffer.append("// Default constructor\n");
            buffer.append(nameWithPackage + "::" + name + "() {\n");
            buffer.append("\tm_the_struct = new " + type + "();\n");
            buffer.append("}\n");

            // Valuebox added constructor
            buffer.append("// Valuebox added constructor\n");
            buffer.append(nameWithPackage + "::" + name + "(const " + type + "& init) {\n");
            buffer.append("\tm_the_struct = new " + type + "(init);\n");
            buffer.append("}\n");

        	
            // Copy Constructor
            buffer.append("// Copy Constructor\n");
            buffer.append(nameWithPackage + "::" + name + "(const " + nameWithPackage + "& other) {\n");
            buffer.append("\tm_the_struct = new " + type + "(*other.m_the_struct);\n");
            buffer.append("}\n");

            // Assignment operator
            buffer.append("// Assignment operator\n");
            buffer.append(nameWithPackage + "& " + nameWithPackage + "::operator=(const " + type + "& other) {\n" );
            buffer.append("\t" + nameWithPackage + " _the_auxiliar(other);\n");
            buffer.append("\treturn _the_auxiliar;\n");
            buffer.append("}\n");
        	
            // Accessors and modifier
            buffer.append("// Accessors and modifier\n");
            buffer.append("const " + type + "& " + nameWithPackage + "::_value() const {\n");
            buffer.append("\treturn (const " + type + "&) *m_the_struct;\n");
            buffer.append("}\n");
            buffer.append(type + "& " + nameWithPackage + "::_value() {\n");
            buffer.append("\treturn *m_the_struct;\n");
            buffer.append("}\n");
            buffer.append("void " + nameWithPackage + "::_value(const " + type + "& other) {\n");
            buffer.append("\tdelete m_the_struct;\n");
            buffer.append("\tm_the_struct = new " + type + "(other);\n");
            buffer.append("}\n");
            
            // Explicit argument passing conversions for the underlying struct
            buffer.append("// Explicit argument passing conversions for the underlying struct\n");
            buffer.append(XmlType2Cpp.getParamType(typeEl,"in") + " " + nameWithPackage +"::_boxed_in() const {\n");
            buffer.append("\treturn (const " + type + "&) *m_the_struct;\n");
            buffer.append("}\n");
            buffer.append(XmlType2Cpp.getParamType(typeEl,"inout") + " " + nameWithPackage +"::_boxed_inout() {\n");
            buffer.append("\treturn (" + type + ") *m_the_struct;\n");
            buffer.append("}\n");
            buffer.append(XmlType2Cpp.getParamType(typeEl,"out") + " " + nameWithPackage + "::_boxed_out() {\n");
            buffer.append("\treturn (" + type + "*&) m_the_struct;\n");
            buffer.append("}\n");

            // Static _downcast function
            buffer.append("// Static _downcast function\n");
            buffer.append("static " + nameWithPackage + "* " + nameWithPackage + "::_downcast(CORBA::ValueBase* base) {\n");
            buffer.append("\tif(base == NULL)\n");
            buffer.append("\t\treturn NULL;\n");
            buffer.append("\t" + nameWithPackage + "* _concrete_ref = dynamic_cast<" + nameWithPackage + "*> (base);\n");
            buffer.append("\treturn _concrete_ref;\n");
            buffer.append("}\n");

            //A partir de aqu� se lee el contenido del tipo struct y se procede a su mapping: s�lo
            //los accesores y los modificadores
            Element root = doc.getOwnerDocument().getDocumentElement(); //vamos a la raiz del DOM
        	
            NodeList structs = root.getElementsByTagName(OMG_struct); //Buscamos los structs que 
            //haya en el �rbol
        	
            for (int k = 0; k < structs.getLength(); k++) {		//recorremos los structs buscando 
                //el que corresponde al valuetype
                Element str = (Element) structs.item(k);
                if (str.getAttribute(OMG_scoped_name).equals("::"+type)) {
                    NodeList subchilds = str.getElementsByTagName(OMG_type); 
                    for (int l = 0; l < subchilds.getLength(); l++) { //encontrado el que buscamos,
                        //lo recorremos
                        Element child = (Element) subchilds.item(l);
                        kind = child.getAttribute(OMG_kind);
                        Element sibling = (Element) child.getNextSibling();
                        String childname = sibling.getAttribute(OMG_name);
        				
                        String accesorType = XmlType2Cpp.getAccesorType(child);
                        String modifierType = XmlType2Cpp.getModifierType(child);
                        String referentType = XmlType2Cpp.getReferentType(child);

                        // accesor
                        buffer.append("// Accessor: " + childname + "\n");
                        buffer.append(accesorType + " " + nameWithPackage + "::" + childname + "() const {\n");
                        buffer.append("\treturn m_the_struct->" + childname + "();\n");
                        buffer.append("}\n");


                        // modificador
                        buffer.append("// Modifier: " + childname + "\n");
                        buffer.append("void " + nameWithPackage + "::" + childname + "(" + modifierType + " val) {\n");
                        buffer.append("\treturn m_the_struct->" + childname + "(val);\n");
                        buffer.append("}\n");

                        // modificadores extra para cadenas
                        if (XmlType2Cpp.isAString(child) || XmlType2Cpp.isAWString(child)) {
                            String varType;
                            if (XmlType2Cpp.isAWString(child))
                                varType = "CORBA::WString_var&";
                            else
                                varType = "CORBA::String_var&";
                            buffer.append("void " + nameWithPackage + "::" + childname + "(const " + modifierType
                                          + " val) {\n");
            	            buffer.append("\treturn m_the_struct->" + childname + "(val);\n");
            	            buffer.append("}\n");
                            buffer.append("void " + nameWithPackage + "::" + childname + "(const " + varType + " val) {\n");
            	            buffer.append("\treturn m_the_struct->" + childname + "(val);\n");
            	            buffer.append("}\n");
                        }
                    }

                    NodeList subchildscoped = str.getElementsByTagName(OMG_scoped_name); 
                    for (int l = 0; l < subchildscoped.getLength(); l++) { //encontrado el que buscamos,
                        //lo recorremos
                        Element child = (Element) subchildscoped.item(l);
                        kind = child.getAttribute(OMG_kind);
                        Element sibling = (Element) child.getNextSibling();
                        String childname = sibling.getAttribute(OMG_name);
        				
                        String accesorType = XmlType2Cpp.getAccesorType(child);
                        String modifierType = XmlType2Cpp.getModifierType(child);
                        String referentType = XmlType2Cpp.getReferentType(child);

                        // accesor
                        buffer.append("// Accessor: " + childname + "\n");
                        buffer.append(accesorType + " " + nameWithPackage + "::" + childname + "() const {\n");
                        buffer.append("\treturn m_the_struct->" + childname + "();\n");
                        buffer.append("}\n");


                        // modificador
                        buffer.append("// Modifier: " + childname + "\n");
                        buffer.append("void " + nameWithPackage + "::" + childname + "(" + modifierType + " val) {\n");
                        buffer.append("\treturn m_the_struct->" + childname + "(val);\n");
                        buffer.append("}\n");

                        // modificadores extra para cadenas
                        if (XmlType2Cpp.isAString(child) || XmlType2Cpp.isAWString(child)) {
                            String varType;
                            if (XmlType2Cpp.isAWString(child))
                                varType = "CORBA::WString_var&";
                            else
                                varType = "CORBA::String_var&";
                            buffer.append("void " + nameWithPackage + "::" + childname + "(const " + modifierType
                                          + " val) {\n");
            	            buffer.append("\treturn m_the_struct->" + childname + "(val);\n");
            	            buffer.append("}\n");
                            buffer.append("void " + nameWithPackage + "::" + childname + "(const " + varType + " val) {\n");
            	            buffer.append("\treturn m_the_struct->" + childname + "(val);\n");
            	            buffer.append("}\n");
                        }
                    }

                }
            }

        	
            // Destructor
            buffer.append("// Destructor\n");
            buffer.append(nameWithPackage + "::~" + name + "() {\n");
            buffer.append("\tdelete m_the_struct;\n");
            buffer.append("}\n");
        	        	        	
        } else

            // 3. String y WString
            if (XmlType2Cpp.isAString(typeEl) || XmlType2Cpp.isAWString(typeEl)) {

                String varType = "CORBA::String_var";
                String subType = "char";
                String dup = "CORBA::string_dup";
                String free = "CORBA::string_free";
                String empty = "\"\"";
                if (XmlType2Cpp.isAWString(typeEl)) {
                    varType = "CORBA::WString_var";
                    subType = "CORBA::WChar";
                    dup = "CORBA::wstring_dup";
                    free = "CORBA::wstring_free";
                    empty = "L" + empty;
                }

                buffer.append(nameWithPackage + "::" + name + "() {\n"); 
                // constructor
                // por defecto
                buffer.append("\tm_the_value = " + dup + "(" + empty + ");\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "::" + name + "(" + type
                              + " val) {\n"); // constructor con valor inicial - se
                // apropia
                buffer.append("\tm_the_value = val;\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "::" + name + "(const " + type
                              + " val) {\n"); // constructor con valor inicial -
                // copia
                buffer.append("\tm_the_value = " + dup + "(val);\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "::" + name + "(const " + varType
                              + "& val) {\n"); // constructor con valor inicial -
                // copia de var
                buffer.append("\tm_the_value = " + dup + "(val);\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "::" + name + "(const "
                              + nameWithPackage + "& val) {\n"); // constructor de
                // copia
                buffer.append("\tm_the_value = " + dup + "(val.m_the_value);\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "& " + nameWithPackage
                              + "::operator= (" + type + " val) {\n"); // asignacion
                // - se
                // apropia
                buffer.append("\tm_the_value = val;\n");
                buffer.append("\treturn *this;\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "& " + nameWithPackage
                              + "::operator= (const " + type + " val) {\n"); // asignacion
                // -
                // copia
                buffer.append("\tm_the_value = " + dup + "(val);\n");
                buffer.append("\treturn *this;\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "& " + nameWithPackage
                              + "::operator= (const " + varType + "& val) {\n"); 
                // asignacion
                // - copia de var
                buffer.append("\tm_the_value = " + dup + "(val);\n");
                buffer.append("\treturn *this;\n");
                buffer.append("}\n\n");

                buffer.append("const " + type + " " + nameWithPackage
                              + "::_value() const {\n"); // accesor
                buffer.append("\treturn m_the_value;\n");
                buffer.append("}\n\n");

                buffer.append("void " + nameWithPackage + "::_value(" + type
                              + " val) {\n"); // modificador - se apropia
                buffer.append("\tif (m_the_value != NULL)\n");
                buffer.append("\t\t" + free + "(m_the_value);\n");
                buffer.append("\tm_the_value = val;\n");
                buffer.append("}\n\n");

                buffer.append("void " + nameWithPackage + "::_value(const " + type
                              + " val) {\n"); // modificador - copia
                buffer.append("\tif (m_the_value != NULL)\n");
                buffer.append("\t\t" + free + "(m_the_value);\n");
                buffer.append("\tm_the_value = " + dup + "(val);\n");
                buffer.append("}\n\n");

                buffer.append("void " + nameWithPackage + "::_value(const "
                              + varType + "& val) {\n"); // modificador - copia de
                // var
                buffer.append("\tif (m_the_value != NULL)\n");
                buffer.append("\t\t" + free + "(m_the_value);\n");
                buffer.append("\tm_the_value = " + dup + "(val);\n");
                buffer.append("}\n\n");

                buffer.append(XmlType2Cpp.getParamType(typeEl, "in") + " "
                              + nameWithPackage + "::_boxed_in() const {\n");
                buffer.append("\treturn (" + XmlType2Cpp.getParamType(typeEl, "in")
                              + ") m_the_value;\n");
                buffer.append("}\n\n");

                buffer.append(XmlType2Cpp.getParamType(typeEl, "inout") + " "
                              + nameWithPackage + "::_boxed_inout() const {\n");
                buffer.append("\treturn ("
                              + XmlType2Cpp.getParamType(typeEl, "inout")
                              + ") m_the_value;\n");
                buffer.append("}\n\n");

                buffer.append(XmlType2Cpp.getParamType(typeEl, "inout") + " "
                              + nameWithPackage + "::_boxed_out() const {\n");
                buffer.append("\tif (m_the_value != NULL)\n");
                buffer.append("\t\t" + free + "(m_the_value);\n");
                buffer.append("\treturn ("
                              + XmlType2Cpp.getParamType(typeEl, "inout")
                              + ") m_the_value;\n");
                buffer.append("}\n\n");

                buffer.append(subType + "& " + nameWithPackage
                              + "::operator[] (CORBA::ULong index) {\n");
                buffer.append("\treturn m_the_value[index];\n");
                buffer.append("}\n\n");

                buffer.append(subType + " " + nameWithPackage
                              + "::operator[] (CORBA::ULong index) const {\n");
                buffer.append("\treturn m_the_value[index];\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "* " + nameWithPackage
                              + "::_downcast(CORBA::ValueBase* base) {\n");
                buffer.append("\tif (base == NULL)\n");
                buffer.append("\t\treturn NULL;\n");
                buffer.append("\treturn dynamic_cast<" + nameWithPackage
                              + "*> (base);\n");
                buffer.append("}\n\n");

                buffer.append(nameWithPackage + "::~" + name + "() {\n"); // destructor
                buffer.append("\tif (m_the_value != NULL)\n");
                buffer.append("\t\t" + free + "(m_the_value);\n");
                buffer.append("}\n\n");

            } else
                // 4. Union
                if (definition.equals(OMG_union)) {

                    // Default Constructor
                    buffer.append("// Default Constructor\n");
                    buffer.append(nameWithPackage + "::" + name + "() {\n");
                    buffer.append("\tm_the_union = new " + type + "();\n");
                    buffer.append("}\n");

                    // Copy Constructor
                    buffer.append("\n// Copy Constructor\n");
                    buffer.append(nameWithPackage + "::" + name + "(const " + nameWithPackage + "& other) {\n");
                    buffer.append("\tm_the_union = new " + type + "(*other.m_the_union);\n");
                    buffer.append("}\n");

                    // Valuebox added Constructor
                    buffer.append("\n// Valuebox Added Constructor\n");
                    buffer.append(nameWithPackage + "::" + name + "(const " + type + "& init) {\n");
                    buffer.append("\tm_the_union = new " + type + "(init);\n");
                    buffer.append("}\n");

                    // Assigment Operator
                    buffer.append("\n// Assigment Operator\n");
                    buffer.append(nameWithPackage + "& " + nameWithPackage + "::operator(const " + type + "& other) {\n");
                    buffer.append("\t" + nameWithPackage + " _the_auxiliar(other);\n");
                    buffer.append("\treturn _the_auxiliar;\n");
                    buffer.append("}\n");
 
                    Element root = doc.getOwnerDocument().getDocumentElement(); //vamos a la raiz del DOM
                    NodeList unions = root.getElementsByTagName(OMG_union); //Buscamos los union que 
                    //haya en el �rbol
                    for (int k = 0; k < unions.getLength(); k++) {		//recorremos los unions buscando 
                        //el que corresponde al valuetype
        		Element uni = (Element) unions.item(k);
        		if (uni.getAttribute(OMG_scoped_name).equals("::" + type)) {
                            Union union = UnionManager.getInstance().get(uni);
                            Vector switchBody = union.getSwitchBody();
                            String objectName = null;
                            for (int i = 0; i < switchBody.size(); i++) {
                                UnionCase union_case = (UnionCase) switchBody.elementAt(i);
                                Element type2 = union_case.m_type_spec;
                                Element decl = union_case.m_declarator;
                                String decl_tag = decl.getTagName();
                                if (decl_tag.equals(OMG_simple_declarator)) {
                                    objectName = decl.getAttribute(OMG_name);
                                } else if (decl_tag.equals(OMG_array)) {
                                    throw new SemanticException(
                                                                "Anonymous array members are not supported in unions",
                                                                decl);
                                }

                                String accesorType = XmlType2Cpp.getAccesorType(type2);
                                String modifierType = XmlType2Cpp.getModifierType(type2);
                                String referentType = XmlType2Cpp.getReferentType(type2);

                                // 	accesor
                                buffer.append("\n// Accessor\n");
                                buffer.append(accesorType + " " + nameWithPackage + "::" + objectName + "() const {\n");
                                buffer.append("\treturn m_the_union->" + objectName + "();\n");
                                buffer.append("}\n");
        				
                                // referente
                                if (referentType != null) {
                                    buffer.append("\n// Referent\n");
                                    buffer.append(referentType + " " + nameWithPackage + "::" + objectName + "() {\n");
                                    buffer.append("\treturn m_the_union->" + objectName + "();\n");
                                    buffer.append("}\n");
                                }
                                // modificador
                                buffer.append("\n// Modifier\n");
                                buffer.append("void " + nameWithPackage + "::" + objectName + "(" + modifierType + " val) {\n");
                                buffer.append("\tm_the_union->" + objectName + "(val);\n");
                                buffer.append("}\n");

                                //Calculamos el valor del discriminante
        		        Element discriminator = (Element) uni.getFirstChild().getFirstChild();
        		        String tag = discriminator.getTagName();
        		        String discriminatorType;
        		        if (tag.equals(OMG_enum)) {
        		            String enumName = discriminator.getAttribute(OMG_name);
        		            String newPackage;
        		            if (!genPackage.equals("")) {
        		                newPackage = genPackage + "::" + name;//MACP "Package";
        		            } else {
        		                newPackage = name;//MACP "Package";
        		            }
        		            discriminatorType = newPackage + "::" + enumName;
        		        } else {
        		            discriminatorType = XmlType2Cpp.getType(discriminator);
        		        }
        					
                                // modificador con valor de discriminante
                                if (union_case.m_case_labels.size() > 1){
                                    buffer.append("\nvoid " + nameWithPackage + "::" + objectName + "(" + discriminatorType
                                                  + " discriminant, " + modifierType + " type) {\n");
                                    buffer.append("\tm_the_union->" + objectName + "(discriminant, type);\n");
                                    buffer.append("}\n");
                                }
                                // modificadores extra para cadenas
                                if (XmlType2Cpp.isAString(type2) || XmlType2Cpp.isAWString(type2)) {
                                    String varType;
                                    if (XmlType2Cpp.isAWString(type2))
                                        varType = "CORBA::WString_var&";
                                    else
                                        varType = "CORBA::String_var&";
                                    buffer.append("\nvoid " + nameWithPackage + "::" + objectName + "(const " + modifierType
                                                  + " type) {\n");
                                    buffer.append("\tm_the_union->" + objectName + "(type);\n");
                                    buffer.append("}\n");
                                    buffer.append("\nvoid " + nameWithPackage + "::" + objectName + "(const " + varType + " type) {\n");
                                    buffer.append("\tm_the_union->" + objectName + "(type);\n");
                                    buffer.append("}\n");
                                    if (union_case.m_case_labels.size() > 1) {
                                        buffer.append("\nvoid " + nameWithPackage + "::" + objectName + "(" + discriminatorType
                                                      + " dtype, const " + modifierType + " mtype) {\n");
                                        buffer.append("\tm_the_union->" + objectName + "(dype,mtype);\n");
                                        buffer.append("}\n");

                                        buffer.append("\nvoid " + nameWithPackage + "::" + objectName + "(" + discriminatorType
                                                      + " dtype, const " + varType + " vtype) {\n");
                                        buffer.append("\tm_the_union->" + objectName + "(dtype, vtype);\n");
                                        buffer.append("}\n");

                                    }
                                }	
                                //buffer.append("\n");
                            }
                            // Calculamos el valor del discriminante
                            Element discriminator = (Element) uni.getFirstChild().getFirstChild();
                            String tag = discriminator.getTagName();
                            String discriminatorType;
                            if (tag.equals(OMG_enum)) {
                                String enumName = discriminator.getAttribute(OMG_name);
                                String newPackage;
                                if (!genPackage.equals("")) {
                                    newPackage = genPackage + "::" + name;//MACP "Package";
                                } else {
                                    newPackage = name;//MACP "Package";
                                }
                                discriminatorType = newPackage + "::" + enumName;
                            } else {
                                discriminatorType = XmlType2Cpp.getType(discriminator);
                            }
                            if (!union.getHasDefault() && union.getDefaultAllowed()) {
                                buffer.append("\n// Union's default case modifiers\n"); 
                                buffer.append("void " + nameWithPackage + "::_default() {\n");
                                buffer.append("\tm_the_union->_default();\n");
                                buffer.append("}\n");
                                buffer.append("\nvoid "+ nameWithPackage + "::_default(" + discriminatorType
                                              + " discriminator) {\n");
                                buffer.append("\tm_the_union->_default(discriminator);\n");
                                buffer.append("}\n\n");

                            }
                            buffer.append("// Union accessor\n");
                            buffer.append(discriminatorType + nameWithPackage + "::_d() const {\n");
                            buffer.append("\tm_the_union->_d();\n");
                            buffer.append("}\n");
                            buffer.append("\n// Union modifier\n");
                            buffer.append("void " + nameWithPackage + "::_d(" + discriminatorType + " nd) {\n");
                            buffer.append("\tm_the_union->_d(nd);\n");
                            buffer.append("}\n");
        		}//if
                    }//for
            
                    // Accessor & modifier functions for the underlying boxed value.
                    buffer.append("\n// Accessor & modifier functions for the underlying boxed value.\n");
                    buffer.append("const " + type + "& " + nameWithPackage +"::_value() const {\n");
                    buffer.append("\treturn (const " + type + "&) *m_the_union;\n");
                    buffer.append("}\n");
            
            
                    buffer.append("\n" + type + "& " + nameWithPackage +"::_value() {\n");
                    buffer.append("\treturn *m_the_union;\n");
                    buffer.append("}\n");

                    buffer.append("\nvoid " + nameWithPackage +"::_value(const " + type + "& other) {\n");
                    buffer.append("\tdelete m_the_union;\n");
                    buffer.append("\tm_the_union = new " + type + "(other);\n");
                    buffer.append("}\n");
            
                    // Parameter passing for underlying boxed type
                    buffer.append("\n// Parameter passing for underlying boxed type\n");
                    buffer.append("const " + type + "& " + nameWithPackage + "::_boxed_in() const {\n");
                    buffer.append("\treturn (const " + type + "&) *m_the_union;\n");
                    buffer.append("}\n");
            
                    buffer.append("\n" + type + "& " + nameWithPackage + "::_boxed_inout() {\n");
                    buffer.append("\treturn (" + type + "&) *m_the_union;\n");
                    buffer.append("}\n");

                    buffer.append("\n" + type + "*& " + nameWithPackage + "::_boxed_out() {\n");
                    buffer.append("\treturn (" + type + "*&) m_the_union;\n");
                    buffer.append("}\n");
            
                    // Destructor
                    buffer.append("\n// Destructor\n");
                    buffer.append(nameWithPackage + "::~" + name + "() {\n");
                    buffer.append("\tdelete m_the_union;\n");
                    buffer.append("}\n");


                } else

                    // 5. Sequence
                    if (definition.equals(OMG_sequence)) {
            
                        String internalType = XmlType2Cpp.getSequenceType(doc,type);
                        String bounds = XmlType2Cpp.getSequenceMaximum(doc, type);
            
            
                        // Default Constructor
                        buffer.append("// Default Constructor\n");
                        buffer.append(nameWithPackage + "::" + name + "() {\n");
                        buffer.append("\tm_the_sequence = new " + type + "();\n");
                        buffer.append("}\n");

                        if(bounds.equals("")) {
                            // Maximum Constructor
                            buffer.append("\n// Maximum Constructor\n");
                            buffer.append(nameWithPackage + "::" + name + "(CORBA::ULong max) {\n");
                            buffer.append("\tm_the_sequence = new " + type + "(max);\n");
                            buffer.append("}\n");

                            // T* data Constructor
                            buffer.append("\n// T* data Constructor\n");
                            buffer.append(nameWithPackage + "::" + name + 
                                          "(CORBA::ULong max, CORBA::ULong length, CORBA::Long* data, CORBA::Boolean release = false) {\n");
                            buffer.append("\tm_the_sequence = new " + type + "(max, length, data, release);\n");
                            buffer.append("}\n");
                        } else {
            
                            // T* data Constructor
                            buffer.append("\n// T* data Constructor\n");
                            buffer.append(nameWithPackage + "::" + name + 
                                          "(CORBA::ULong length, CORBA::Long* data, CORBA::Boolean release = false) {\n");
                            buffer.append("\tm_the_sequence = new " + type + "(max, length, data, release);\n");
                            buffer.append("}\n");
                        }
            
                        // Copy Constructor
                        buffer.append("\n// Copy Constructor\n");
                        buffer.append(nameWithPackage + "::" + name + "(const " + nameWithPackage + "& other) {\n");
                        buffer.append("\tm_the_sequence = new " + type + "(*other.m_the_sequence);\n");
                        buffer.append("}\n");

                        // Valuebox added Constructor
                        buffer.append("\n// Valuebox Added Constructor\n");
                        buffer.append(nameWithPackage + "::" + name + "(const " + type + "& init) {\n");
                        buffer.append("\tm_the_sequence = new " + type + "(init);\n");
                        buffer.append("}\n");

                        // Assigment Operator
                        buffer.append("\n// Assigment Operator\n");
                        buffer.append(nameWithPackage + "& " + nameWithPackage + "::operator(const " + type + "& other) {\n");
                        buffer.append("\t" + nameWithPackage + " _the_auxiliar(other);\n");
                        buffer.append("\treturn _the_auxiliar;\n");
                        buffer.append("}\n");
            
                        // Accessor & modifier functions for the underlying boxed value.
                        buffer.append("\n// Accessor & modifier functions for the underlying boxed value.\n");
                        buffer.append("const " + type + "& " + nameWithPackage +"::_value() const {\n");
                        buffer.append("\treturn (const " + type + "&) *m_the_sequence;\n");
                        buffer.append("}\n");
            
            
                        buffer.append("\n" + type + "& " + nameWithPackage +"::_value() {\n");
                        buffer.append("\treturn *m_the_sequence;\n");
                        buffer.append("}\n");

                        buffer.append("\nvoid " + nameWithPackage +"::_value(const " + type + "& other) {\n");
                        buffer.append("\tdelete m_the_sequence;\n");
                        buffer.append("\tm_the_sequence = new " + type + "(other);\n");
                        buffer.append("}\n");
            
                        // Parameter passing for underlying boxed type
                        buffer.append("\n// Parameter passing for underlying boxed type\n");
                        buffer.append("const " + type + "& " + nameWithPackage + "::_boxed_in() const {\n");
                        buffer.append("\treturn (const " + type + "&) *m_the_sequence;\n");
                        buffer.append("}\n");
            
                        buffer.append("\n" + type + "& " + nameWithPackage + "::_boxed_inout() {\n");
                        buffer.append("\treturn (" + type + "&) *m_the_sequence;\n");
                        buffer.append("}\n");

                        buffer.append("\n" + type + "*& " + nameWithPackage + "::_boxed_out() {\n");
                        buffer.append("\treturn (" + type + "*&) m_the_sequence;\n");
                        buffer.append("}\n");

                        buffer.append("\n// These methods came from Sequence Mapping\n");
                        buffer.append("CORBA::ULong " + nameWithPackage + "::maximum() const {\n");
                        buffer.append("\treturn m_the_sequence->maximum();\n");
                        buffer.append("}\n");

                        buffer.append("\nCORBA::ULong " + nameWithPackage + "::length() const {\n");
                        buffer.append("\treturn m_the_sequence->length();\n");
                        buffer.append("}\n");
            
                        buffer.append("\nvoid " + nameWithPackage + "::length(CORBA::ULong len) {\n");
                        buffer.append("\t m_the_sequence->length(len);\n");
                        buffer.append("}\n");

                        buffer.append("\n" + XmlType2Cpp.basicMapping(internalType) + "& " + nameWithPackage + "::operator[](CORBA::ULong index) {\n");
                        buffer.append("\tif (index < m_the_sequence->length())\n");
                        buffer.append("\t\treturn m_the_sequence[index];\n");
                        buffer.append("\telse\n");
                        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n");
                        buffer.append("}\n");
            
                        buffer.append("\n" + XmlType2Cpp.basicMapping(internalType) + " " + nameWithPackage + "::operator[](CORBA::ULong index) const {\n");
                        buffer.append("\tif (index < m_the_sequence->length())\n");
                        buffer.append("\t\treturn (" + XmlType2Cpp.basicMapping(internalType) + ") m_the_sequence[index];\n");
                        buffer.append("\telse\n");
                        buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Current sequence length exceeded\");\n");
                        buffer.append("}\n");
            
                        buffer.append("\nstatic " + nameWithPackage + "* "+ nameWithPackage + "::_downcast(CORBA::ValueBase* base) {\n");
                        buffer.append("\tif(base == NULL)\n");
                        buffer.append("\t\treturn NULL;\n");
                        buffer.append("\t" + nameWithPackage + "* _concrete_ref = dynamic_cast<" + nameWithPackage + "*> (base);\n");
                        buffer.append("\treturn _concrete_ref;\n");
                        buffer.append("}\n");
            
                        // Destructor
                        buffer.append("\n// Destructor\n");
                        buffer.append(nameWithPackage + "::~" + name + "() {\n");
                        buffer.append("\tdelete m_the_sequence;\n");
                        buffer.append("}\n");

                    } else

                        // 6. Fixed
                        if (kind.equals(OMG_fixed)) {

                            // All kind of constructors inherited from fixed class
                            buffer.append("// All kind of constructors inherited from fixed class\n");
                            buffer.append(nameWithPackage+"::"+name + "() {\n");
                            buffer.append("\t m_the_fixed = new " + type + "();\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::Long v);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::ULong v);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::LongLong v);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::ULongLong v);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::Double v);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::LongDouble v);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(const char *s);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(s);\n");
                            buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(CORBA::UShort d, CORBA::Short s);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(d,s);\n");
                            buffer.append("}\n");
                            //buffer.append(nameWithPackage+"::"+name + "(const CORBA::Fixed& f);\n");
                            //buffer.append("\t m_the_fixed = new " + type + "(f);\n");
                            //buffer.append("}\n");
                            buffer.append(nameWithPackage+"::"+name + "(TIDorb::types::BigInt v,CORBA::Short s);\n");
                            buffer.append("\t m_the_fixed = new " + type + "(v,s);\n");
                            buffer.append("}\n");

                            //buffer.append("\t// Copy constructor\n");
                            //buffer.append("\t" + name + "(const " + name + "& other);\n" );
                            buffer.append("\n// Valuebox added constructor\n");
                            buffer.append(nameWithPackage+"::"+name + "(const " + type + "& init) {\n");
                            buffer.append("\t m_the_fixed = new " + type + "(init);\n");
                            buffer.append("}\n");
        	
                            // Assigment operator
                            buffer.append("\n// Assigment operator\n");
                            buffer.append(nameWithPackage + "& " + nameWithPackage + "::operator = (const " + type + "& other) {\n");
                            buffer.append("\t" + nameWithPackage + " _the_auxiliar(other);\n");
                            buffer.append("\treturn _the_auxiliar;\n");
                            buffer.append("}\n");
        	
                            // Accessor & modifier functions for the underlying boxed value
                            buffer.append("\n// Accessor & modifier functions for the underlying boxed value.\n");
                            buffer.append("const " + type + "& " + nameWithPackage +"::_value() const {\n");
                            buffer.append("\treturn (const " + type + "&) *m_the_fixed;\n");
                            buffer.append("}\n");
            
            
                            buffer.append("\n" + type + "& " + nameWithPackage +"::_value() {\n");
                            buffer.append("\treturn *m_the_fixed;\n");
                            buffer.append("}\n");

                            buffer.append("\nvoid " + nameWithPackage +"::_value(const " + type + "& other) {\n");
                            buffer.append("\tdelete m_the_fixed;\n");
                            buffer.append("\tm_the_fixed = new " + type + "(other);\n");
                            buffer.append("}\n");
 
                            // Parameter passing for underlying boxed type
                            buffer.append("\n// Parameter passing for underlying boxed type\n");
                            buffer.append("const " + type + "& " + nameWithPackage + "::_boxed_in() const {\n");
                            buffer.append("\treturn (const " + type + "&) *m_the_fixed;\n");
                            buffer.append("}\n");
            
                            buffer.append("\n" + type + "& " + nameWithPackage + "::_boxed_inout() {\n");
                            buffer.append("\treturn (" + type + "&) *m_the_fixed;\n");
                            buffer.append("}\n");

                            buffer.append("\n" + type + "*& " + nameWithPackage + "::_boxed_out() {\n");
                            buffer.append("\treturn (" + type + "*&) m_the_fixed;\n");
                            buffer.append("}\n");
       	
                            // More from Fixed class...
                            buffer.append("// More from Fixed class\n");
                            //buffer.append("\tCORBA::operator CORBA::LongLong () const;\n");
                            //buffer.append("\tCORBA::operator CORBA::LongDouble () const;\n");

                            buffer.append("CORBA::Fixed " + nameWithPackage + "::round(CORBA::UShort nscale) const {\n");
                            buffer.append("\treturn m_the_fixed->round(nscale);\n");
                            buffer.append("}\n");
                            buffer.append("char* " + nameWithPackage + "::to_string() const {\n");
                            buffer.append("\treturn m_the_fixed->to_string();\n");
                            buffer.append("}\n");
                            buffer.append("CORBA::Fixed& " + nameWithPackage + "::from_string(const char* value) {\n");
                            buffer.append("\treturn m_the_fixed->from_string();\n");
                            buffer.append("}\n");
                            buffer.append("CORBA::UShort " + nameWithPackage + "::fixed_digits() const {\n");
                            buffer.append("\treturn m_the_fixed->fixed_digits();\n");
                            buffer.append("}\n");
                            buffer.append("CORBA::Short " + nameWithPackage + "::fixed_scale() const {\n");
                            buffer.append("\treturn m_the_fixed->fixed_scale();\n");
                            buffer.append("}\n");
                            buffer.append("TIDorb::types::BigInt " + nameWithPackage + "::fixed_value() const {\n");
                            buffer.append("\treturn m_the_fixed->fixed_value();\n");
                            buffer.append("}\n");

                            // �Habr�a que hacer todos estos operadores?
                            //        	buffer.append("CORBA::Fixed&  operator= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("CORBA::Fixed&  operator+= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("CORBA::Fixed&  operator-= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("CORBA::Fixed&  operator*= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("CORBA::Fixed&  operator/= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("CORBA::Fixed& operator++ ();\n");
                            //        	buffer.append("CORBA::Fixed  operator++ (int);\n");
                            //        	buffer.append("CORBA::Fixed& operator-- ();\n");
                            //        	buffer.append("CORBA::Fixed& operator-- ();\n");
                            //        	buffer.append("CORBA::Fixed  operator-- (int);\n");
                            //        	buffer.append("CORBA::Fixed  operator+ () const;\n");
                            //        	buffer.append("CORBA::Fixed  operator- () const;\n");
                            //        	buffer.append("CORBA::Boolean  operator!() const;\n");
                            //        	buffer.append("CORBA::Fixed operator+ (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Fixed operator- (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Fixed operator* (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Fixed operator/ (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Boolean operator> (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Boolean operator< (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Boolean operator>= (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Boolean operator<= (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Boolean operator== (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("CORBA::Boolean operator!= (const CORBA::Fixed &f) const;\n");

                            // Destructor
                            buffer.append("\n// Destructor\n");
                            buffer.append(nameWithPackage + "::~" + name + "() {\n");
                            buffer.append("\tdelete m_the_fixed;\n");
                            buffer.append("}\n");

                        } else

                            // 7. Any
                            if (kind.equals(OMG_any)) {
                                // Default Constructor
                                buffer.append("// Default Constructor\n");
                                buffer.append(nameWithPackage + "::" + name + "() {\n");
                                buffer.append("\tm_any = new " + type + "();\n");
                                buffer.append("}\n");

                                // Copy Constructor
                                buffer.append("\n// Copy Constructor\n");
                                buffer.append(nameWithPackage + "::" + name + "(const " + nameWithPackage + "& other) {\n");
                                buffer.append("\tm_any = new " + type + "(*other.m_any);\n");
                                buffer.append("}\n");

                                // Valuebox added Constructor
                                buffer.append("\n// Valuebox Added Constructor\n");
                                buffer.append(nameWithPackage + "::" + name + "(const " + type + "& init) {\n");
                                buffer.append("\tm_any = new " + type + "(init);\n");
                                buffer.append("}\n");

                                // Assigment Operator
                                buffer.append("\n// Assigment Operator\n");
                                buffer.append(nameWithPackage + "& " + nameWithPackage + "::operator(const " + type + "& other) {\n");
                                buffer.append("\t" + nameWithPackage + " _the_auxiliar(other);\n");
                                buffer.append("\treturn _the_auxiliar;\n");
                                buffer.append("}\n");
            
                                // Accessor & modifier functions for the underlying boxed value.
                                buffer.append("\n// Accessor & modifier functions for the underlying boxed value.\n");
                                buffer.append("const " + type + "& " + nameWithPackage +"::_value() const {\n");
                                buffer.append("\treturn (const " + type + "&) *m_any;\n");
                                buffer.append("}\n");
            
            
                                buffer.append("\n" + type + "& " + nameWithPackage +"::_value() {\n");
                                buffer.append("\treturn *m_any;\n");
                                buffer.append("}\n");

                                buffer.append("\nvoid " + nameWithPackage +"::_value(const " + type + "& other) {\n");
                                buffer.append("\tdelete m_any;\n");
                                buffer.append("\tm_any = new " + type + "(other);\n");
                                buffer.append("}\n");
            
                                // Parameter passing for underlying boxed type
                                buffer.append("\n// Parameter passing for underlying boxed type\n");
                                buffer.append("const " + type + "& " + nameWithPackage + "::_boxed_in() const {\n");
                                buffer.append("\treturn (const " + type + "&) *m_any;\n");
                                buffer.append("}\n");
            
                                buffer.append("\n" + type + "& " + nameWithPackage + "::_boxed_inout() {\n");
                                buffer.append("\treturn (" + type + "&) *m_any;\n");
                                buffer.append("}\n");

                                buffer.append("\n" + type + "*& " + nameWithPackage + "::_boxed_out() {\n");
                                buffer.append("\treturn (" + type + "*&) m_any;\n");
                                buffer.append("}\n");
            
                                //buffer.append("\nstatic " + nameWithPackage + "* "+ nameWithPackage + "::_downcast(CORBA::ValueBase* base) {\n");
                                //buffer.append("\tif(base == NULL)\n");
                                //buffer.append("\t\treturn NULL;\n");
                                //buffer.append("\t" + nameWithPackage + "* _concrete_ref = dynamic_cast<" + nameWithPackage + "*> (base);\n");
                                //buffer.append("\treturn _concrete_ref;\n");
                                //buffer.append("}\n");
            
                                // Destructor
                                buffer.append("\n// Destructor\n");
                                buffer.append(nameWithPackage + "::~" + name + "() {\n");
                                buffer.append("\tdelete m_any;\n");
                                buffer.append("}\n");

                            } else

                                // 8. Array
                                if (definition.equals(OMG_array)) {

                                    // Default Constructor
                                    buffer.append("// Default Constructor\n");
                                    buffer.append(nameWithPackage + "::" + name + "() {\n");
                                    buffer.append("\tm_the_array = new " + type + "_var();\n");
                                    buffer.append("}\n");

                                    // Copy Constructor
                                    buffer.append("\n// Copy Constructor\n");
                                    buffer.append(nameWithPackage + "::" + name + "(const " + nameWithPackage + "& other) {\n");
                                    buffer.append("\tm_the_array = new " + type + "_var(*other.m_the_array);\n");
                                    buffer.append("}\n");

                                    // Valuebox added Constructor
                                    buffer.append("\n// Valuebox added Constructor\n");
                                    buffer.append(nameWithPackage + "::" + name + "(const " + type + "_slice& init) {\n");
                                    buffer.append("\tm_the_array = new " + type + "_var(init);\n");
                                    buffer.append("}\n");

                                    // A public assignment operator that takes a const argument
                                    buffer.append("\n// A public assignment operator that takes a const argument\n");
                                    buffer.append(nameWithPackage + "& " + nameWithPackage + "::operator(const " + type + " other) {\n");
                                    buffer.append("\t" + nameWithPackage + " _the_auxiliar(other);\n");
                                    buffer.append("\treturn _the_auxiliar;\n");
                                    buffer.append("}\n");

                                    // Accessor & modifier functions for the underlying boxed value.
                                    buffer.append("\n// Accessor & modifier functions for the underlying boxed value.\n");
                                    buffer.append("const " + type + "& " + nameWithPackage +"_slice*::_value() const {\n");
                                    buffer.append("\treturn (const " + type + "&) *m_any;\n");
                                    buffer.append("}\n");
            
            
                                    buffer.append("\n" + type + "& " + nameWithPackage +"_slice*::_value() {\n");
                                    buffer.append("\treturn *m_any;\n");
                                    buffer.append("}\n");

                                    buffer.append("\nvoid " + nameWithPackage +"::_value(const " + type + "& other) {\n");
                                    buffer.append("\tdelete m_any;\n");
                                    buffer.append("\tm_any = new " + type + "(other);\n");
                                    buffer.append("}\n");
          	
                                    buffer.append("\t// Accessor and Modifier functions\n");
                                    buffer.append("\tconst " + type + "_slice* _value() const;\n");
                                    buffer.append("\t" + type + "_slice* _value();\n");
                                    buffer.append("\tvoid _value(const " + type + ");\n");

                                    buffer.append("\n\t// Parameter passing for underlying boxed type\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"in") + "_slice* _boxed_in() const;\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"inout") + "_slice* _boxed_inout();\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"out") + "_slice* _boxed_out();\n");
        	
                                    buffer.append("\n\t// Static _downcast function\n");
                                    buffer.append("\tstatic " + name + "* _downcast(CORBA::ValueBase*);\n");
        	
                                    buffer.append("\n\t// Overloaded subscript operators\n");
                                    buffer.append("\tLongArray_slice& operator[] (CORBA::ULong index);\n");
                                    buffer.append("\tconst LongArray_slice& operator[] (CORBA::ULong index) const;\n");
        	
                                    buffer.append("\n  protected:");
        	
                                    buffer.append("\n\t// Destructor\n");
                                    buffer.append("\t~" + name + "();\n\n");

            
                                } else {
                                    // 1. Basicos, Enums y Object Ref.
                                    String accessType = type;
                                    if (definition.equals(OMG_interface))
                                        accessType += "_ptr";
                                    if (kind.equals(OMG_Object))
                                        type = accessType.substring(0, accessType.length() - 4);

                                    buffer.append(nameWithPackage + "::" + name + "() {\n"); 
                                    // constructor
                                    // por defecto
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append("\tm_the_value = " + type + "::_nil();\n");
                                    buffer.append("}\n\n");

                                    buffer.append(nameWithPackage + "::" + name + "(" + accessType
                                                  + " val) {\n"); // constructor con valor inicial
                                    buffer.append("\tm_the_value = ");
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append(type + "::_duplicate(val);\n");
                                    else
                                        buffer.append("val;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(nameWithPackage + "::" + name + "(const "
                                                  + nameWithPackage + "& val) {\n"); // constructor de
                                    // copia
                                    buffer.append("\tm_the_value = ");
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append(type + "::_duplicate(val.m_the_value);\n");
                                    else
                                        buffer.append("val.m_the_value;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(nameWithPackage + "& " + nameWithPackage
                                                  + "::operator= (" + accessType + " val) {\n"); // asignacion
                                    buffer.append("\tm_the_value = ");
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append(type + "::_duplicate(val);\n");
                                    else
                                        buffer.append("val;\n");
                                    buffer.append("\treturn *this;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(accessType + " " + nameWithPackage
                                                  + "::_value() const {\n"); // accesor
                                    buffer.append("\treturn m_the_value;\n");
                                    buffer.append("}\n\n");

                                    buffer.append("void " + nameWithPackage + "::_value(" + accessType
                                                  + " val) {\n"); // modificador
                                    buffer.append("\tm_the_value = ");
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append(type + "::_duplicate(val);\n");
                                    else
                                        buffer.append("val;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(XmlType2Cpp.getParamType(typeEl, "in") + " "
                                                  + nameWithPackage + "::_boxed_in() const {\n");
                                    buffer.append("\treturn (" + XmlType2Cpp.getParamType(typeEl, "in")
                                                  + ") m_the_value;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(XmlType2Cpp.getParamType(typeEl, "inout") + " "
                                                  + nameWithPackage + "::_boxed_inout() const {\n");
                                    buffer.append("\treturn ("
                                                  + XmlType2Cpp.getParamType(typeEl, "inout")
                                                  + ") m_the_value;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(XmlType2Cpp.getParamType(typeEl, "inout") + " "
                                                  + nameWithPackage + "::_boxed_out() const {\n");
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append("\tCORBA::release(m_the_value);\n");
                                    buffer.append("\treturn ("
                                                  + XmlType2Cpp.getParamType(typeEl, "inout")
                                                  + ") m_the_value;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(nameWithPackage + "* " + nameWithPackage
                                                  + "::_downcast(CORBA::ValueBase* base) {\n");
                                    buffer.append("\tif (base == NULL)\n");
                                    buffer.append("\t\treturn NULL;\n");
                                    buffer.append("\t" + nameWithPackage
                                                  + "* _concrete_ref = dynamic_cast<" + nameWithPackage
                                                  + "*> (base);\n");
                                    buffer.append("\treturn _concrete_ref;\n");
                                    buffer.append("}\n\n");

                                    buffer.append(nameWithPackage + "::~" + name + "() {"); // destructor
                                    if (kind.equals(OMG_Object) || definition.equals(OMG_interface))
                                        buffer.append("\n\tCORBA::release(m_the_value);\n");
                                    buffer.append("}\n\n");

                                }

        return buffer.toString();

    }

    private String generateHppBoxedValuetype(Element doc, String genPackage)
        throws Exception
    {

        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        String definition = XmlType2Cpp.getDefinitionType(typeEl);
        String kind = "";
        
        if (definition.equals(OMG_kind))
            kind = XmlType2Cpp.getDeepKind(typeEl);

        
               
        // _tc_ Type Code Generation. //vv Static or Extern
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        // Class header
        //String helperClass = XmlType2Cpp.getHelperName(genPackage + "::" +
        // name);
        //buffer.append("class _" + name + "Helper;\n\n");
        buffer.append("class " + name + " : ");
        buffer.append("public ::CORBA::DefaultValueRefCountBase\n{\n\n");
        //buffer.append("\tfriend class " + helperClass + ";\n\n");

        // Casos:

        // 2. Struct
        if (definition.equals(OMG_struct)) {

            buffer.append("  public:\n");
        	
            // Default constructor
            buffer.append("\t// Default constructor\n");
            buffer.append("\t" + name + "();\n\n");
        	
            // Valuebox added constructor
            buffer.append("\t// Valuebox added constructor\n");
            buffer.append("\t" + name + "(const " + type + "&);\n\n");
        	
            // Copy Constructor
            buffer.append("\t// Copy Constructor\n");
            buffer.append("\t" + name + "(const " + name + "&);\n\n");

            // Assignment operator
            buffer.append("\t// Assignment operator\n");
            buffer.append("\t" + name + "& operator=(const " + type + "&);\n\n" );
        	
            // Accessors and modifier
            buffer.append("\t// Accessors and modifier\n");
            buffer.append("\tconst " + type + "& _value() const;\n");
            buffer.append("\t" + type + "& _value();\n");
            buffer.append("\tvoid _value(const " + type + "&);\n\n");

            // Explicit argument passing conversions for the underlying struct
            buffer.append("\t// Explicit argument passing conversions for the underlying struct\n");
            buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"in") + " _boxed_in() const;\n");
            buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"inout") + " _boxed_inout();\n");
            buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"out") + " _boxed_out();\n\n");
        	
            // Static _downcast function
            buffer.append("\t// Static _downcast function\n");
            buffer.append("\tstatic " + name + "* _downcast(CORBA::ValueBase*);\n\n");
        	
            //A partir de aqu� se lee el contenido del tipo struct y se procede a su mapping: s�lo
            //los accesores y los modificadores
            Element root = doc.getOwnerDocument().getDocumentElement(); //vamos a la raiz del DOM
        	
            NodeList structs = root.getElementsByTagName(OMG_struct); //Buscamos los structs que 
            //haya en el �rbol
        	
            for (int k = 0; k < structs.getLength(); k++) {		//recorremos los structs buscando 
                //el que corresponde al valuetype
                Element str = (Element) structs.item(k);
                if (str.getAttribute(OMG_scoped_name).equals("::"+type)) {
                    NodeList subchilds = str.getElementsByTagName(OMG_type); 
                    for (int l = 0; l < subchilds.getLength(); l++) { //encontrado el que buscamos,
                        //lo recorremos
                        Element child = (Element) subchilds.item(l);
                        kind = child.getAttribute(OMG_kind);
                        Element sibling = (Element) child.getNextSibling();
                        String childname = sibling.getAttribute(OMG_name);
        				
                        String accesorType = XmlType2Cpp.getAccesorType(child);
                        String modifierType = XmlType2Cpp.getModifierType(child);
                        String referentType = XmlType2Cpp.getReferentType(child);

                        // accesor
                        buffer.append("\t// Accessor: " + childname + "\n");
                        buffer.append("\t" + accesorType + " " + childname + "() const;\n");

                        // modificador
                        buffer.append("\t// Modifier: " + childname + "\n");
                        buffer.append("\tvoid " + childname + "(" + modifierType + ");\n");

                        // modificadores extra para cadenas
                        if (XmlType2Cpp.isAString(child) || XmlType2Cpp.isAWString(child)) {
                            String varType;
                            if (XmlType2Cpp.isAWString(child))
                                varType = "CORBA::WString_var&";
                            else
                                varType = "CORBA::String_var&";
                            buffer.append("\tvoid " + childname + "(const " + modifierType
                                          + ");\n");
                            buffer.append("\tvoid " + childname + "(const " + varType + ");\n\n");
                        }
                    }
                    NodeList subchildscope = str.getElementsByTagName(OMG_scoped_name); 
                    for (int l = 0; l < subchildscope.getLength(); l++) { //encontrado el que buscamos,
                        //lo recorremos
                        Element child = (Element) subchildscope.item(l);
                        kind = child.getAttribute(OMG_kind);
                        Element sibling = (Element) child.getNextSibling();
                        String childname = sibling.getAttribute(OMG_name);
        				
                        String accesorType = XmlType2Cpp.getAccesorType(child);
                        String modifierType = XmlType2Cpp.getModifierType(child);
                        String referentType = XmlType2Cpp.getReferentType(child);

                        // accesor
                        buffer.append("\t// Accessor: " + childname + "\n");
                        buffer.append("\t" + accesorType + " " + childname + "() const;\n");

                        // modificador
                        buffer.append("\t// Modifier: " + childname + "\n");
                        buffer.append("\tvoid " + childname + "(" + modifierType + ");\n");

                        // modificadores extra para cadenas
                        if (XmlType2Cpp.isAString(child) || XmlType2Cpp.isAWString(child)) {
                            String varType;
                            if (XmlType2Cpp.isAWString(child))
                                varType = "CORBA::WString_var&";
                            else
                                varType = "CORBA::String_var&";
                            buffer.append("\tvoid " + childname + "(const " + modifierType
                                          + ");\n");
                            buffer.append("\tvoid " + childname + "(const " + varType + ");\n\n");
                        }
                    }
                }
            }
        	
            buffer.append("\n  protected:\n\n");

            // Destructor
            buffer.append("\t// Destructor\n");
            buffer.append("\t~" + name + "();\n\n");
        	
            buffer.append("  private:\n\n");

            // A private and preferably unimplemented default assignment operator
            buffer.append("\t// A private and preferably unimplemented default assignment operator\n");
            buffer.append("\tvoid operator=(const " + name + "& val);\n");
   	        
            // The struct!!
            buffer.append("\n\t// The struct\n");
            buffer.append("\t" + type + "* m_the_struct;\n");
        	
        	
        } else

            // 3. String y WString
            if (XmlType2Cpp.isAString(typeEl) || XmlType2Cpp.isAWString(typeEl)) {

                String varType = "CORBA::String_var";
                String subType = "char";
                if (XmlType2Cpp.isAWString(typeEl)) {
                    varType = "CORBA::WString_var";
                    subType = "CORBA::WChar";
                }
                buffer.append("  public:\n");

                buffer.append("\t" + name + "();\n"); // constructor por defecto
                buffer.append("\t" + name + "(" + type + ");\n"); // constructor con
                // valor inicial
                // (m�ltiple)
                buffer.append("\t" + name + "(const " + type + ");\n");
                buffer.append("\t" + name + "(const " + varType + "&);\n");
                buffer.append("\t" + name + "(const " + name + "&);\n"); // constructor
                // de copia

                buffer.append("\t" + name + "& operator= (" + type + ");\n"); // asignacion
                buffer.append("\t" + name + "& operator= (const " + type + ");\n");
                buffer.append("\t" + name + "& operator= (const " + varType
                              + "&);\n");

                buffer.append("\n");

                buffer.append("\tconst " + type + " _value() const;\n"); // accesor
                buffer.append("\tvoid _value(" + type + ");\n"); // modificadores
                buffer.append("\tvoid _value(const " + type + ");\n"); // modificadores
                buffer.append("\tvoid _value(const " + varType + "&);\n"); // modificadores

                buffer.append("\n");

                buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "in")
                              + " _boxed_in() const;\n");
                buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "inout")
                              + " _boxed_inout() const;\n");
                buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "inout")
                              + " _boxed_out() const;\n");

                buffer.append("\n");

                buffer.append("\t" + subType + "& operator[] (CORBA::ULong);\n");
                buffer.append("\t" + subType
                              + " operator[] (CORBA::ULong) const;\n");

                buffer.append("\n");

                buffer.append("\tstatic " + name
                              + "* _downcast(CORBA::ValueBase*);\n");

                buffer.append("\n");

                buffer.append("  protected:\n");
                buffer.append("\t~" + name + "();\n"); // destructor

                buffer.append("\n");

                buffer.append("  private:\n");
                buffer.append("\t" + type + " m_the_value;\n"); // miembro
                buffer.append("\tvoid operator=(const " + name + "& val);\n"); // asignacion
                // por
                // defecto
                // (sin
                // implementar)

            } else

                // 4. Union
                if (definition.equals(OMG_union)) {
            
                    buffer.append("  public:\n");
                    buffer.append("\t// Default Constructor\n");
                    buffer.append("\t" + name + "();\n");
                    buffer.append("\t// Copy Constructor\n");
                    buffer.append("\t" + name + "(const " + name + "& other);\n");
                    buffer.append("\t// Valuebox added constructor\n");
                    buffer.append("\t" + name + "(const " + type + "& init);\n");
                    buffer.append("\t// Assignment operator\n");
                    buffer.append("\t" + name + "& operator=(const " + type + "& other);\n\n");
        	
                    Element root = doc.getOwnerDocument().getDocumentElement(); //vamos a la raiz del DOM
                    NodeList unions = root.getElementsByTagName(OMG_union); //Buscamos los union que 
                    //haya en el �rbol
                    for (int k = 0; k < unions.getLength(); k++) {		//recorremos los unions buscando 
                        //el que corresponde al valuetype
        		Element uni = (Element) unions.item(k);
        		if (uni.getAttribute(OMG_scoped_name).equals("::"+type)) {
                            Union union = UnionManager.getInstance().get(uni);
                            Vector switchBody = union.getSwitchBody();
                            String objectName = null;
                            for (int i = 0; i < switchBody.size(); i++) {
                                UnionCase union_case = (UnionCase) switchBody.elementAt(i);
                                Element type2 = union_case.m_type_spec;
                                Element decl = union_case.m_declarator;
                                String decl_tag = decl.getTagName();
                                if (decl_tag.equals(OMG_simple_declarator)) {
                                    objectName = decl.getAttribute(OMG_name);
                                } else if (decl_tag.equals(OMG_array)) {
                                    throw new SemanticException(
                                                                "Anonymous array members are not supported in unions",
                                                                decl);
                                }

                                String accesorType = XmlType2Cpp.getAccesorType(type2);
                                String modifierType = XmlType2Cpp.getModifierType(type2);
                                String referentType = XmlType2Cpp.getReferentType(type2);

                                // 	accesor
                                buffer.append("\t// Accessor\n");
                                buffer.append("\t" + accesorType + " " + objectName + "() const;\n");
                                // referente
                                if (referentType != null) {
                                    buffer.append("\t// Accessor\n");
                                    buffer.append("\t" + referentType + " " + objectName + "();\n");
                                }
                                // modificador
                                buffer.append("\t// Modifier\n");
                                buffer.append("\tvoid " + objectName + "(" + modifierType + ");\n");
                                //Calculamos el valor del discriminante
        		        Element discriminator = (Element) uni.getFirstChild().getFirstChild();
        		        String tag = discriminator.getTagName();
        		        String discriminatorType;
        		        if (tag.equals(OMG_enum)) {
        		            String enumName = discriminator.getAttribute(OMG_name);
        		            String newPackage;
        		            if (!genPackage.equals("")) {
        		                newPackage = genPackage + "::" + name;//MACP "Package";
        		            } else {
        		                newPackage = name;//MACP "Package";
        		            }
        		            discriminatorType = newPackage + "::" + enumName;
        		        } else {
        		            discriminatorType = XmlType2Cpp.getType(discriminator);
        		        }
        					
                                // modificador con valor de discriminante
                                if (union_case.m_case_labels.size() > 1)
                                    buffer.append("\tvoid " + objectName + "(" + discriminatorType
                                                  + ", " + modifierType + ");\n");
 
                                // modificadores extra para cadenas
                                if (XmlType2Cpp.isAString(type2) || XmlType2Cpp.isAWString(type2)) {
                                    String varType;
                                    if (XmlType2Cpp.isAWString(type2))
                                        varType = "CORBA::WString_var&";
                                    else
                                        varType = "CORBA::String_var&";
                                    buffer.append("\tvoid " + objectName + "(const " + modifierType
                                                  + ");\n");
                                    buffer.append("\tvoid " + objectName + "(const " + varType + ");\n");
                                    if (union_case.m_case_labels.size() > 1) {
                                        buffer.append("\tvoid " + objectName + "(" + discriminatorType
                                                      + ", const " + modifierType + ");\n");
                                        buffer.append("\tvoid " + objectName + "(" + discriminatorType
                                                      + ", const " + varType + ");\n");
                                    }
                                }	
                                //buffer.append("\n");
                            }
                            //                  Calculamos el valor del discriminante
                            Element discriminator = (Element) uni.getFirstChild().getFirstChild();
                            String tag = discriminator.getTagName();
                            String discriminatorType;
                            if (tag.equals(OMG_enum)) {
                                String enumName = discriminator.getAttribute(OMG_name);
                                String newPackage;
                                if (!genPackage.equals("")) {
                                    newPackage = genPackage + "::" + name;//MACP "Package";
                                } else {
                                    newPackage = name;//MACP "Package";
                                }
                                discriminatorType = newPackage + "::" + enumName;
                            } else {
                                discriminatorType = XmlType2Cpp.getType(discriminator);
                            }
                            if (!union.getHasDefault() && union.getDefaultAllowed()) {
                                buffer.append("\n\t// Default accessor\n"); 
                                buffer.append("\tvoid _default();\n");
                                buffer.append("\t// Default modifier\n");
                                buffer.append("\tvoid _default(" + discriminatorType
                                              + " discriminator);\n\n");
                            }
                            buffer.append("\t// Union accessor\n");
                            buffer.append("\t" + discriminatorType + " _d() const;\n");
                            buffer.append("\t// Union modifier\n");
                            buffer.append("\tvoid _d(" + discriminatorType + " nd);\n");
        		}//if
                    }//for

                    // Accessor & modifier functions for the underlying boxed type
                    buffer.append("\n\t// Accessor & modifier functions for the underlying boxed type\n");
                    buffer.append("\tconst " + type + "& _value() const;\n");
                    buffer.append("\t" + type + "& _value();\n");
                    buffer.append("\tvoid _value(const " + type + "&);\n\n");

                    // Parameter passing for underlying boxed type
                    buffer.append("\t// Parameter passing for underlying boxed type\n");
                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"in") + " _boxed_in() const;\n");
                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"inout") + " _boxed_inout();\n");
                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"out") + " _boxed_out();\n\n");

                    buffer.append("  protected:\n");
        	
                    buffer.append("\t~" + name + "();\n\n");
        	
        	
                    buffer.append("  private:\n");
        	
                    buffer.append("\t// A private and preferably unimplemented default assignment operator\n");
                    buffer.append("\tvoid operator=(const " + name + "& val);\n");
                    // The Union
                    buffer.append("\t// The Union\n");
                    buffer.append("\t" + type + "* m_the_union;\n");
             
                } else

                    // 5. Sequence
                    if (definition.equals(OMG_sequence)) {

                        String internal = XmlType2Cpp.getSequenceType(doc,type);
                        String bounds = XmlType2Cpp.getSequenceMaximum(doc, type);
       	
                        buffer.append("public:\n");
                        buffer.append("\t// Default constructor\n");
                        buffer.append("\t" + name + "();\n");
        	
                        if(bounds.equals("")) {
                            buffer.append("\t// maximum constructor\n");
                            buffer.append("\t" + name + "(CORBA::ULong max);\n");
                            buffer.append("\t// T* data constructor\n");
                            buffer.append("\t" + name + "(CORBA::ULong max, CORBA::ULong length, "
                                          + XmlType2Cpp.basicMapping(internal)
                                          + "* data, CORBA::Boolean release = false);\n");
                        } else {
                            buffer.append("\t// T* data constructor\n");
                            buffer.append("\t" + name + "(CORBA::ULong length, "
                                          + XmlType2Cpp.basicMapping(internal)
                                          + "* data, CORBA::Boolean release = false);\n");
                        }
                        buffer.append("\t// Copy constructor\n");
                        buffer.append("\t" + name + "(const " + name + "& other);\n" );
                        buffer.append("\t// Valuebox added constructor\n");
                        buffer.append("\t" + name + "(const " + type + "& init);\n");
        	
                        buffer.append("\n\t// Assigment operator\n");
                        buffer.append("\t" + name + "& operator = (const " + type + "& other);\n");
                        buffer.append("\n\t// Accessor & modifier functions for the underlying boxed value.\n");
                        buffer.append("\tconst " + type + "& _value() const;\n");
                        buffer.append("\t" + type + "& _value();\n");
                        buffer.append("\tvoid _value(const " + type + "&);\n");
                        buffer.append("\n\t// Parameter passing for underlying boxed type\n");
                        buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "in") + " _boxed_in() const;\n");
                        buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "inout") + " _boxed_inout();\n");
                        buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "out") + " _boxed_out();\n");
        	
                        buffer.append("\tstatic " + name + "* _downcast(ValueBase*);\n" );
        	 
                        buffer.append("\n\tCORBA::ULong maximum() const;\n");
                        buffer.append("\tvoid length(CORBA::ULong v);\n");
                        buffer.append("\tCORBA::ULong length() const; \n");
                        buffer.append("\n\t" + XmlType2Cpp.basicMapping(internal)
                                      + "& operator[](CORBA::ULong index);\n");
                        buffer.append("\tconst " + XmlType2Cpp.basicMapping(internal)
                                      + "& operator[](CORBA::ULong index) const;\n");
                        buffer.append("\nprotected:\n");
                        buffer.append("\t// Destructor\n");
                        buffer.append("\t~" + name + "();");
                        buffer.append("\nprivate:\n");
                        buffer.append("\t// A preferably unimplemented default assignment operator\n");
                        buffer.append("\tvoid operator = (const " + name + "& val);\n\n");
                        buffer.append("\t// The sequence\n");
                        buffer.append("\t" + type + "* m_the_sequence;\n\n");

                    } else

                        // 6. Fixed
                        if (kind.equals(OMG_fixed)) {
            
                            buffer.append("  public:\n");

                            // All kind of constructors inherited from fixed class
                            buffer.append("\t// All kind of constructors inherited from fixed class\n");
                            buffer.append("\t" + name + "();\n");
                            buffer.append("\t" + name + "(CORBA::Long v);\n");
                            buffer.append("\t" + name + "(CORBA::ULong v);\n");
                            buffer.append("\t" + name + "(CORBA::LongLong v);\n");
                            buffer.append("\t" + name + "(CORBA::ULongLong v);\n");
                            buffer.append("\t" + name + "(CORBA::Double v);\n");
                            buffer.append("\t" + name + "(CORBA::LongDouble v);\n");
                            buffer.append("\t" + name + "(const char *s);\n");
                            buffer.append("\t" + name + "(CORBA::UShort d, CORBA::Short s);\n");
                            //buffer.append("\t" + name + "(const CORBA::Fixed& f);\n");
                            buffer.append("\t" + name + "(TIDorb::types::BigInt v,CORBA::Short s);\n");

                            //buffer.append("\t// Copy constructor\n");
                            //buffer.append("\t" + name + "(const " + name + "& other);\n" );
                            buffer.append("\t// Valuebox added constructor\n");
                            buffer.append("\t" + name + "(const " + type + "& init);\n");
        	
                            // Assigment operator
                            buffer.append("\n\t// Assigment operator\n");
                            buffer.append("\t" + name + "& operator = (const " + type + "& other);\n");
        	
                            // Accessor & modifier functions for the underlying boxed value
                            buffer.append("\n\t// Accessor & modifier functions for the underlying boxed value.\n");
                            buffer.append("\tconst " + type + "& _value() const;\n");
                            buffer.append("\t" + type + "& _value();\n");
                            buffer.append("\tvoid _value(const " + type + "&);\n");
        	
                            // Parameter passing for underlying boxed type
                            buffer.append("\n\t// Parameter passing for underlying boxed type\n");
                            buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"in") + " _boxed_in() const;\n");
                            buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"inout") + " _boxed_inout();\n");
                            buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"out") + " _boxed_out();\n\n");

                            // More from Fixed class...
                            buffer.append("\t// More from Fixed class\n");
                            //buffer.append("\tCORBA::operator CORBA::LongLong () const;\n");
                            //buffer.append("\tCORBA::operator CORBA::LongDouble () const;\n");

                            buffer.append("\tCORBA::Fixed round(CORBA::UShort nscale) const;\n");
                            buffer.append("\tchar* to_string() const;\n");
                            buffer.append("\tCORBA::Fixed& from_string(const char* value);\n");
                            buffer.append("\tCORBA::UShort fixed_digits () const;\n");
                            buffer.append("\tCORBA::Short fixed_scale () const;\n");
                            buffer.append("\tTIDorb::types::BigInt fixed_value () const;\n");
        	
                            //        	buffer.append("\tCORBA::Fixed&  operator= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("\tCORBA::Fixed&  operator+= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("\tCORBA::Fixed&  operator-= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("\tCORBA::Fixed&  operator*= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("\tCORBA::Fixed&  operator/= (const CORBA::Fixed &f);\n");
                            //        	buffer.append("\tCORBA::Fixed& operator++ ();\n");
                            //        	buffer.append("\tCORBA::Fixed  operator++ (int);\n");
                            //        	buffer.append("\tCORBA::Fixed& operator-- ();\n");
                            //        	buffer.append("\tCORBA::Fixed& operator-- ();\n");
                            //        	buffer.append("\tCORBA::Fixed  operator-- (int);\n");
                            //        	buffer.append("\tCORBA::Fixed  operator+ () const;\n");
                            //        	buffer.append("\tCORBA::Fixed  operator- () const;\n");
                            //        	buffer.append("\tCORBA::Boolean  operator!() const;\n");
                            //        	buffer.append("\tCORBA::Fixed operator+ (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Fixed operator- (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Fixed operator* (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Fixed operator/ (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Boolean operator> (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Boolean operator< (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Boolean operator>= (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Boolean operator<= (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Boolean operator== (const CORBA::Fixed &f) const;\n");
                            //        	buffer.append("\tCORBA::Boolean operator!= (const CORBA::Fixed &f) const;\n");

                            buffer.append("\n  protected:\n");
                            buffer.append("\t// Destructor\n");
                            buffer.append("\t~" + name + "();\n\n");
        	
                            buffer.append("  private:\n");
                            buffer.append("\t// A preferably unimplemented default assignment operator\n");
                            buffer.append("\tvoid operator=(const " + name + "& val);\n");     
                            buffer.append("\t// The Fixed \n");
                            buffer.append("\t" + type + "* m_the_fixed;\n\n");
                          
                        } else

                            // 7. Any
                            if (kind.equals(OMG_any)) {
                                buffer.append("public:\n");
                                buffer.append("\t// Default constructor\n");
                                buffer.append("\t" + name + "();\n");
        	
                                buffer.append("\t// Copy constructor\n");
                                buffer.append("\t" + name + "(const " + name + "& other);\n" );

                                buffer.append("\t// Valuebox added constructor\n");
                                buffer.append("\t" + name + "(const " + type + "& init);\n");
        	
                                buffer.append("\n\t// Assigment operator\n");
                                buffer.append("\t" + name + "& operator = (const " + type + "& other);\n");

                                buffer.append("\n\t// Accessor & modifier functions for the underlying boxed value.\n");
                                buffer.append("\tconst " + type + "& _value() const;\n");
                                buffer.append("\t" + type + "& _value();\n");
                                buffer.append("\tvoid _value(const " + type + "&);\n");
        	
                                buffer.append("\n\t// Parameter passing for underlying boxed type\n");
                                buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "in") + " _boxed_in() const;\n");
                                buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "inout") + " _boxed_inout();\n");
                                buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "out") + " _boxed_out();\n");
        	
                                //buffer.append("\tstatic " + name + "* _downcast(ValueBase*);\n" );
        	 
                                buffer.append("\nprotected:\n");
                                buffer.append("\t// Destructor\n");
                                buffer.append("\t~" + name + "();");
                                buffer.append("\nprivate:\n");
                                buffer.append("\t// A preferably unimplemented default assignment operator\n");
                                buffer.append("\tvoid operator = (const " + name + "& val);\n\n");
                                buffer.append("\t// The sequence\n");
                                buffer.append("\t" + type + "* m_any;\n\n");

                            } else

                                // 8. Array
                                if (definition.equals(OMG_array)) {
            
                                    buffer.append("  public:\n");
        	
                                    buffer.append("\t// Default Constructor\n");
                                    buffer.append("\t" + name + "();\n");	
                                    buffer.append("\t// Copy Constructor\n");
                                    buffer.append("\t" + name + "(const " + name + "& other);\n");
                                    buffer.append("\t// Valuebox added constructor\n");
                                    buffer.append("\t" + name + "(const " + type + "_slice init);\n\n");
        	
                                    buffer.append("\t// A public assignment operator that takes a const argument\n");
                                    buffer.append("\t" + name + "& operator=(const " + type + " other);\n\n");
        	
                                    buffer.append("\t// Accessor and Modifier functions\n");
                                    buffer.append("\tconst " + type + "_slice* _value() const;\n");
                                    buffer.append("\t" + type + "_slice* _value();\n");
                                    buffer.append("\tvoid _value(const " + type + ");\n");

                                    buffer.append("\n\t// Parameter passing for underlying boxed type\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"in") + "_slice* _boxed_in() const;\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"inout") + "_slice* _boxed_inout();\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl,"out") + "_slice* _boxed_out();\n");
        	
                                    buffer.append("\n\t// Static _downcast function\n");
                                    buffer.append("\tstatic " + name + "* _downcast(CORBA::ValueBase*);\n");
        	
                                    buffer.append("\n\t// Overloaded subscript operators\n");
                                    buffer.append("\tLongArray_slice& operator[] (CORBA::ULong index);\n");
                                    buffer.append("\tconst LongArray_slice& operator[] (CORBA::ULong index) const;\n");
        	
                                    buffer.append("\n  protected:");
        	
                                    buffer.append("\n\t// Destructor\n");
                                    buffer.append("\t~" + name + "();\n\n");
        	
                                    buffer.append("  private:");
        	
                                    buffer.append("\n\t// A preferably unimplemented default assignment operator\n");
                                    buffer.append("\tvoid operator=(const " + name + "& val);\n");
                                    buffer.append("\t// The Array\n");
                                    buffer.append("\t" + type + "_slice* m_the_array;\n\n");
        	
                                } else {
                                    // 1. Basicos, Enums y Object Ref.
                                    if (definition.equals(OMG_interface))
                                        type += "_ptr";

                                    buffer.append("  public:\n");

                                    buffer.append("\t" + name + "();\n"); // constructor por defecto
                                    buffer.append("\t" + name + "(" + type + ");\n"); // constructor con
                                    // valor inicial
                                    buffer.append("\t" + name + "(const " + name + "&);\n"); // constructor
                                    // de copia
                                    buffer.append("\t" + name + "& operator= (" + type + ");\n"); // asignacion

                                    buffer.append("\n");

                                    buffer.append("\t" + type + " _value() const;\n"); // accesor
                                    buffer.append("\tvoid _value(" + type + ");\n"); // modificador

                                    buffer.append("\n");

                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "in")
                                                  + " _boxed_in() const;\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "inout")
                                                  + " _boxed_inout() const;\n");
                                    buffer.append("\t" + XmlType2Cpp.getParamType(typeEl, "inout")
                                                  + " _boxed_out() const;\n");

                                    buffer.append("\n");

                                    buffer.append("\tstatic " + name
                                                  + "* _downcast(CORBA::ValueBase*);\n");

                                    buffer.append("\n");

                                    buffer.append("  protected:\n");
                                    buffer.append("\t~" + name + "();\n"); // destructor

                                    buffer.append("\n");

                                    buffer.append("  private:\n");
                                    buffer.append("\t" + type + " m_the_value;\n"); // miembro
                                    buffer.append("\tvoid operator=(const " + name + "&);\n"); // asignacion
                                    // por
                                    // defecto
                                    // (sin
                                    // implementar)

                                }

        /*
         * buffer.append("\tpublic:\n\t "+type+" value;\nn");
         * buffer.append("\t"+name+"("+type+" initial){ value=initial; }\n\n");
         * buffer.append("\tstatic char** _ids ;\n\n"); buffer.append("\tchar**
         * _truncatable_ids(){ return _ids; }\n\n");
         */

        buffer.append("\n};\n\n");
        return buffer.toString();

    }

    private void generateHppInheritance(StringBuffer buffer, Element doc)
        throws Exception
    {

        int inheritances = 0;

        //String isAbstractS = doc.getAttribute(OMG_abstract);
        //boolean isAbstract = (isAbstractS!=null) &&
        // (isAbstractS.equals(OMG_true));
        //String isCustomS = doc.getAttribute(OMG_custom);
        //boolean isCustom = (isCustomS!=null) && (isCustomS.equals(OMG_true));

        Vector inherit = new Vector();
        m_supported_interfaces = new Vector();

        NodeList list = doc.getElementsByTagName(OMG_value_inheritance_spec);

        Element inheritance = (Element) list.item(0);
        //String isTruncatableS = inheritance.getAttribute(OMG_truncatable);
        //boolean isTruncatable = (isTruncatableS!=null) &&
        // (isTruncatableS.equals(OMG_true));

        NodeList inherits = inheritance.getChildNodes();
        for (int k = 0; k < inherits.getLength(); k++) {
            Element inheritedScopeEl = (Element) inherits.item(k);
            String inherited_tag = inheritedScopeEl.getTagName();
            if (inherited_tag.equals(OMG_scoped_name)) {
                String inheritedScope = inheritedScopeEl.getAttribute(OMG_name);
                Scope inhScope = Scope.getGlobalScopeInterface(inheritedScope);
                Element elfather = inhScope.getElement();
                String father_tag = elfather.getTagName();
                if (father_tag.equals(OMG_valuetype)) {
                    inherit.add(TypeManager.convert(inheritedScope));
                    inheritances++;
                }
            } else if (inherited_tag.equals(OMG_supports)) {
                NodeList supports = inheritedScopeEl.getChildNodes();
                for (int j = 0; j < supports.getLength(); j++) {
                    Element supportedScopeEl = (Element) supports.item(j);
                    String supported_tag = supportedScopeEl.getTagName();
                    if (supported_tag.equals(OMG_scoped_name)) {
                        String supportedScope = supportedScopeEl
                            .getAttribute(OMG_name);
                        Scope inhScope = Scope
                            .getGlobalScopeInterface(supportedScope);
                        Element elfather = inhScope.getElement();
                        String father_tag = elfather.getTagName();
                        if (father_tag.equals(OMG_interface)){
                            //Bug #39
                            m_supported_interfaces.add(elfather);
                            //inheritances++;
                        }
                    }
                }
            }
        }

        if (inheritances == 0) {
            if(new Boolean (doc.getAttribute(OMG_custom)).booleanValue()) {
                inherit.add("CORBA::CustomMarshal");
            }
            else {
                inherit.add("CORBA::ValueBase");
            }
        }
        buffer.append(" :");
        for (int i = 0; i < inherit.size(); i++) {
            if (i == 0)
                buffer.append(" public virtual " + inherit.elementAt(i));
            else
                buffer.append(",\n\t  public virtual " + inherit.elementAt(i));
        }
        
        //        for (int i = 0; i < m_supported_interfaces.size(); i++) {
        //            if (i == 0)
        //                buffer.append(" public virtual " + m_supported_interfaces.elementAt(i));
        //            else
        //                buffer.append(",\n\t  public virtual " + m_supported_interfaces.elementAt(i));
        //        }
    }

    private void generateCppValueContents(StringBuffer baseBuffer,
                                          StringBuffer OBVBuffer, Element doc,
                                          String valuetypeName,
                                          String genPackage,
                                          boolean thereAreOps)
        throws Exception
    {

        StringBuffer OBVstateBuffer = new StringBuffer();
        String OBVname = doc.getAttribute(OMG_name);
        String OBVgenPackage = genPackage;
        
        if (genPackage.equals(""))
            OBVname = "OBV_" + OBVname;
        else
            OBVgenPackage = "OBV_" + OBVgenPackage;
        
        String valueWithPackage = OBVgenPackage.equals("") ? OBVname 
                : OBVgenPackage + "::" + OBVname;

        
        // Default Constructor
        OBVstateBuffer.append("/* Empty constructor */\n\n");
        OBVstateBuffer.append(OBVgenPackage + "::" + OBVname + "::" + OBVname + "()");
        
        
          
        // Initialize enum members
        HashMap enumerado = new HashMap();
        Integer enum_index = new Integer(0);
        
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            NodeList childNodes = el.getChildNodes();
            if (tag.equals(OMG_value_inheritance_spec) && !(childNodes.getLength() == 0)) {
            		Element fatherName = (Element) childNodes.item(0);
            		String name = fatherName.getAttribute(OMG_name);
            		String[] fatherVector = name.split("::");
            		String father = fatherVector[fatherVector.length - 1];
            	
            		Element root = doc.getOwnerDocument().getDocumentElement();
            		NodeList valuetypes = root.getElementsByTagName(OMG_valuetype);
            	
            		for (int k = 0; k < valuetypes.getLength(); k++) {
                    Element value = (Element) valuetypes.item(k);
                    if (value.getAttribute(OMG_name).equals(father)) {
                        NodeList subChilds = value.getElementsByTagName(OMG_state_member);
                        for (int l = 0; l < subChilds.getLength(); l++){
                            Element child = (Element) subChilds.item(l);
                       	    NodeList nodes2 = child.getChildNodes(); 
                            Element type2 = (Element) nodes2.item(0); //      tipo de la declaraci�n
                            String typeTag2 = type2.getTagName();
                            
                            for (int j = 1; j < nodes2.getLength(); j++) {   // recorrido de tantas declaraciones como se
                                												// incluyan
                    				Element decl2 = (Element) nodes2.item(j);
                    				String name2 = decl2.getAttribute(OMG_name);
                    			
                    				String declType2 = decl2.getTagName(); //      simple o array

                    				if (typeTag2.equals(OMG_scoped_name)) {
                    					if (XmlType2Cpp.getDefinitionType(type2).equals(OMG_enum)){
                    						// System.out.println("scoped_name");
                    						Element root2 = type2.getOwnerDocument().getDocumentElement();
                                			NodeList enums = root2.getElementsByTagName(OMG_enum);
                            				for (int m = 0; m < enums.getLength(); m++){
                            					Element en = (Element) enums.item(m);
                            					if (en.getAttribute(OMG_scoped_name).equals(type2.getAttribute(OMG_name))){
                            						NodeList subchild = en.getElementsByTagName(OMG_enumerator);
                            						Element child2 = (Element) subchild.item(0);
                            						String fatherModule = en.getAttribute(OMG_scoped_name);
                            						String[] module = fatherModule.split("::");
                            						String fatherGenPackage = "::" + module[1];
                            						String elem = fatherGenPackage + "::" + child2.getAttribute(OMG_name);
                            						
                            						String enumNames[] = { null, elem }; 
                            						enumerado.put(enum_index, enumNames);
                            					}
                            				}
                            				if (declType2.equals(OMG_simple_declarator)) {
                            					String[] enumNames = (String[]) enumerado.get(enum_index);
                                        		if (enumNames != null /*((Boolean) enumerado.get(j)).booleanValue()*/){
                                        			int index = enum_index.intValue();
                                        			enum_index = new Integer(index + 1);
                                        			enumNames[0] = decl2.getAttribute(OMG_name);
                                        		}
                            					// System.out.println("simple_declarator");
                            				}   	
                   					}
                    				}
                            }                    
                        }
                    }          		
            		}
            } else if (tag.equals(OMG_state_member)) {
                
            	    NodeList nodes2 = el.getChildNodes(); 
                Element type2 = (Element) nodes2.item(0); //      tipo de la declaraci�n
                String typeTag2 = type2.getTagName();
                
                for (int j = 1; j < nodes2.getLength(); j++) {   // recorrido de tantas declaraciones como se
                    												// incluyan
        				Element decl2 = (Element) nodes2.item(j);
        				String name2 = decl2.getAttribute(OMG_name);
        			
        				String declType2 = decl2.getTagName(); //      simple o array
        			        				    				
        				if (typeTag2.equals(OMG_scoped_name)) {
        					if (XmlType2Cpp.getDefinitionType(type2).equals(OMG_enum)){
        						// System.out.println("scoped_name");
        						Element root = type2.getOwnerDocument().getDocumentElement();
                    			NodeList enums = root.getElementsByTagName(OMG_enum);
                				for (int k = 0; k < enums.getLength(); k++){
                					Element en = (Element) enums.item(k);
                					if (en.getAttribute(OMG_scoped_name).equals(type2.getAttribute(OMG_name))){
                						NodeList subchild = en.getElementsByTagName(OMG_enumerator);
                						Element child = (Element) subchild.item(0);	
                						String fatherModule = en.getAttribute(OMG_scoped_name);
                						String[] module = fatherModule.split("::");
                						String fatherGenPackage = "::" + module[1];
                						String elem = fatherGenPackage + "::" + child.getAttribute(OMG_name);			
                						String enumNames[] = { null, elem }; 
                						enumerado.put(enum_index, enumNames);				
                					}
                				}
                				if (declType2.equals(OMG_simple_declarator)) {
                					String[] enumNames = (String[]) enumerado.get(enum_index);
                            		if (enumNames != null /*((Boolean) enumerado.get(j)).booleanValue()*/){                            			
                            			int index = enum_index.intValue();
                            			enum_index = new Integer(index + 1);
                            			enumNames[0] = decl2.getAttribute(OMG_name);
                            		}
                					// System.out.println("simple_declarator");
                				}   		
        					}
        				}
                }            
            }                          
        }
        
        Iterator it = enumerado.values().iterator();
        String sep = " : ";
        while (it.hasNext()) { 
        		String[] enumNames = (String[]) it.next();
        		OBVstateBuffer.append(sep + "_" + enumNames[0] + "(" + enumNames[1] + ")");
        		sep = ", ";
        }
        
        OBVstateBuffer.append( "{\n");
        
        
        
        
        // Inicializar miembros string y Wstring
        nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            NodeList childNodes = el.getChildNodes();
            if (tag.equals(OMG_value_inheritance_spec) && !(childNodes.getLength() == 0)) {
            		Element fatherName = (Element) childNodes.item(0);
            		String name = fatherName.getAttribute(OMG_name);
            		String[] fatherVector = name.split("::");
            		String father = fatherVector[fatherVector.length - 1];
            	
            		Element root = doc.getOwnerDocument().getDocumentElement();
            		NodeList valuetypes = root.getElementsByTagName(OMG_valuetype);
            	
            		for (int k = 0; k < valuetypes.getLength(); k++) {
                    Element value = (Element) valuetypes.item(k);
                    if (value.getAttribute(OMG_name).equals(father)) {
                        NodeList subChilds = value.getElementsByTagName(OMG_state_member);
                        for (int l = 0; l < subChilds.getLength(); l++){
                            Element child = (Element) subChilds.item(l);
                            
                       	    NodeList nodes2 = child.getChildNodes(); 
                            Element type2 = (Element) nodes2.item(0); //      tipo de la declaraci�n
                            String typeTag2 = type2.getTagName();
                            
                            for (int j = 1; j < nodes2.getLength(); j++) {   // recorrido de tantas declaraciones como se
                                												// incluyan
                    				Element decl2 = (Element) nodes2.item(j);
                    				String name2 = decl2.getAttribute(OMG_name);
                    			
                    				String declType2 = decl2.getTagName(); //      simple o array
                    			
                    				if (declType2.equals(OMG_simple_declarator)) {
                    					if (XmlType2Cpp.isAString(type2) || XmlType2Cpp.isAWString(type2)) {						                                        						
                    						OBVstateBuffer.append("\t" + " _" + name2 + " = " + XmlType2Cpp.getDefaultConstructor(type2) + ";\n");
                    					}
                    				
                    				}
                            }
                        
                        }
                    }
            		
            		}
            } else if (tag.equals(OMG_state_member)) {
            	    NodeList nodes2 = el.getChildNodes(); 
                Element type2 = (Element) nodes2.item(0); //      tipo de la declaraci�n
                String typeTag2 = type2.getTagName();
                
                for (int j = 1; j < nodes2.getLength(); j++) {   // recorrido de tantas declaraciones como se
                    												// incluyan
        				Element decl2 = (Element) nodes2.item(j);
        				String name2 = decl2.getAttribute(OMG_name);
        			
        				String declType2 = decl2.getTagName(); //      simple o array
        			
        				if (declType2.equals(OMG_simple_declarator)) {
        					if (XmlType2Cpp.isAString(type2) || XmlType2Cpp.isAWString(type2)) {						                    
        						OBVstateBuffer.append("\t" + " _" + name2 + " = " + XmlType2Cpp.getDefaultConstructor(type2) + ";\n");          						
        					}
        				
        				}
                }
            
            }
        
                   
        }
        
        OBVstateBuffer.append("}\n\n");                     
        
        
        // Items definition
        nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            NodeList childNodes = el.getChildNodes();
            if (tag.equals(OMG_value_inheritance_spec) && !(childNodes.getLength() == 0)) {
                    /* 
	                // It's not neccesary repeat definition code from derived (father)
					// Currently OBV inherits from Value Declaration and OBV derived.
            		Element fatherName = (Element) childNodes.item(0);
            		String name = fatherName.getAttribute(OMG_name);
            		String[] fatherVector = name.split("::");
            		String father = fatherVector[fatherVector.length - 1];
            	
            		Element root = doc.getOwnerDocument().getDocumentElement();
            		NodeList valuetypes = root.getElementsByTagName(OMG_valuetype);
            	
            		for (int k = 0; k < valuetypes.getLength(); k++) {
                    Element value = (Element) valuetypes.item(k);
                    if (value.getAttribute(OMG_name).equals(father)) {
                        NodeList subChilds = value.getElementsByTagName(OMG_state_member);
                        //String OBVname = value.getAttribute(OMG_name);
                        Element parent = (Element) el.getParentNode();
                        //String OBVname = parent.getAttribute(OMG_name);
                        //String OBVgenPackage = "OBV_" + genPackage;
                        for (int l = 0; l < subChilds.getLength(); l++){
                            Element child = (Element) subChilds.item(l);
                            //generateCppStateMemberDecl(OBVstateBuffer, child, OBVgenPackage,
                            // OBVname);
                        }
                    }
            		}*/
            } else if (tag.equals(OMG_state_member)) {
                generateCppStateMemberDecl(OBVstateBuffer, el, OBVgenPackage,
                                           OBVname);

            } else if (tag.equals(OMG_const_dcl)) {
                generateCppConstDecl(baseBuffer, el,
                                     genPackage.equals("") ? valuetypeName
                                     : genPackage + "::" + valuetypeName);
            }
        }


        // _copy_value()   
        
        nodes = doc.getChildNodes();
      
        OBVstateBuffer.append(valueWithPackage + "* " + valueWithPackage + "::" + "_copy_value() {\n\t");

        if (!thereAreOps) {

            OBVstateBuffer.append(valueWithPackage + "* _copy = new " + valueWithPackage + "();\n");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String tag = el.getTagName();
                NodeList childNodes = el.getChildNodes();
                if (tag.equals(OMG_value_inheritance_spec) && !(childNodes.getLength() == 0)) {
                    
                    Element fatherName = (Element) childNodes.item(0);
                    String name = fatherName.getAttribute(OMG_name);
            		String[] fatherVector = name.split("::");
            		String father = fatherVector[fatherVector.length - 1];
            	
            		Element root = doc.getOwnerDocument().getDocumentElement();
            		NodeList valuetypes = root.getElementsByTagName(OMG_valuetype);
            	
            		for (int k = 0; k < valuetypes.getLength(); k++) {
            			Element value = (Element) valuetypes.item(k);
            			if (value.getAttribute(OMG_name).equals(father)) {
            				NodeList subChilds = value.getElementsByTagName(OMG_state_member);
            				//String OBVname = value.getAttribute(OMG_name);
            				Element parent = (Element) el.getParentNode();
            				OBVname = parent.getAttribute(OMG_name);
            				for (int l = 0; l < subChilds.getLength(); l++){
            					Element child = (Element) subChilds.item(l);
            					NodeList nodes2 = child.getChildNodes(); 
            					for (int j = 1; j < nodes2.getLength(); j++) { // recorrido de tantas declaraciones como se incluyan
            						Element decl = (Element) nodes2.item(j);	
            						String name2 = decl.getAttribute(OMG_name);
            						String declType = decl.getTagName(); // simple o array
            						if (declType.equals(OMG_simple_declarator)) 
            							OBVstateBuffer.append("\t_copy->" + name2 + "(this->" + name2 + "());\n");
            						else { // arrays annimos
            						}
            					}
            				}
                                }
                        }
                } else if (tag.equals(OMG_state_member)) {
                    for (int j = 1; j < childNodes.getLength(); j++) { // recorrido de tantas declaraciones como se incluyan
                        Element decl = (Element) childNodes.item(j);	
                        String name2 = decl.getAttribute(OMG_name);
                        String declType = decl.getTagName(); // simple o array
                        if (declType.equals(OMG_simple_declarator)) 
                            OBVstateBuffer.append("\t_copy->" + name2 + "(this->" + name2 + "());\n");
                        else { // arrays annimos
                        }
                    }
                } else if (tag.equals(OMG_const_dcl)) {
                    // nothing to do
                }
            }
            OBVstateBuffer.append("\treturn _copy;\n");		

        } else {
            // For valuetypes which has ops 
            OBVstateBuffer.append("throw CORBA::NO_IMPLEMENT();\n");	
        }

        OBVstateBuffer.append("\n");
        OBVstateBuffer.append("}\n\n");




        if (OBVstateBuffer.length() != 0) {
            OBVBuffer.append("// OBV state members\n\n");
            OBVBuffer.append(OBVstateBuffer);
        }
        baseBuffer.append("\n");
    }

    private boolean generateHppValueContents(StringBuffer baseBuffer,
                                             StringBuffer OBVBuffer,
                                             Element doc,
                                             String sourceDirectory,
                                             String headerDirectory,
                                             String valuetypeName,
                                             String genPackage,
                                             boolean expanded, 
                                             String h_ext, 
                                             String c_ext)
        throws Exception
    {

        boolean thereAreOps = false;
        boolean thereAreFather = false;
        boolean thereAreFatherOps = false;
        String father_Name = new String("");

        StringBuffer stateBuffer = new StringBuffer("");
        StringBuffer opsBuffer = new StringBuffer("");

        StringBuffer OBVstateBuffer = new StringBuffer("");

        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {

            //  Hay que manejar:
            //  <state_member> - V
            //  <init_dcl> -
            //  <export>, que comprende:
            //      <type_dcl>
            //      <const_dcl>
            //      <except_dcl>
            //      <attr_dcl>
            //      <op_dcl>

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            // Look at 2nd level of inheritance: father
            NodeList childNodes = el.getChildNodes();
            if (tag.equals(OMG_value_inheritance_spec) && !(childNodes.getLength() == 0)) {
            	Element fatherName = (Element) childNodes.item(0);
            	String name = fatherName.getAttribute(OMG_name);
            	String[] fatherVector = name.split("::");
                String father = fatherVector[fatherVector.length - 1];

                // All valuetypes defined
            	Element root = doc.getOwnerDocument().getDocumentElement();
            	NodeList valuetypes = root.getElementsByTagName(OMG_valuetype);
            	
            	for (int k = 0; k < valuetypes.getLength(); k++) {
                    Element value = (Element) valuetypes.item(k);
                    // The "derived - derived" are not having in account!
                    if (value.getAttribute(OMG_name).equals(father)) {
                    	
							// Check if father is abstract: then OBV must not be generated
							if (!value.getAttribute(OMG_abstract).equals(OMG_true)) {
								NodeList subChilds = value.getChildNodes();
		                        for (int l = 0; l < subChilds.getLength(); l++){
		                            Element child = (Element) subChilds.item(l);
								    String tag2 = child.getTagName();
								    
								    // Look at 3rd level of inheritance: grandfather
								    NodeList grandchildNodes = child.getChildNodes();
						            if (tag2.equals(OMG_value_inheritance_spec) && !(grandchildNodes.getLength() == 0)) {						            	
						            	Element grandfatherName = (Element) grandchildNodes.item(0);
						            	String name2 = grandfatherName.getAttribute(OMG_name);
						            	String[] grandfatherVector = name2.split("::");
						                String grandfather = grandfatherVector[grandfatherVector.length - 1];
						                
						                for (int k2 = 0; k2 < valuetypes.getLength(); k2++) {
						                    Element value2 = (Element) valuetypes.item(k2);
						                    // The "derived - derived" are not having in account!
						                    if (value2.getAttribute(OMG_name).equals(grandfather)) {
						                    	if (!value2.getAttribute(OMG_abstract).equals(OMG_true)) {
													NodeList subChilds2 = value2.getChildNodes();
							                        for (int l2 = 0; l2 < subChilds2.getLength(); l2++){
							                            Element child2 = (Element) subChilds2.item(l2);
													    String tag3 = child2.getTagName();
													    
													    
													    // Look at 4th level of inheritance
													    NodeList grandgrandchildNodes = child2.getChildNodes();
											            if (tag3.equals(OMG_value_inheritance_spec) && !(grandgrandchildNodes.getLength() == 0)) {						            	
											            	Element grandgrandfatherName = (Element) grandgrandchildNodes.item(0);
											            	String name3 = grandgrandfatherName.getAttribute(OMG_name);
											            	String[] grandgrandfatherVector = name3.split("::");
											                String grandgrandfather = grandgrandfatherVector[grandgrandfatherVector.length - 1];
											                
											                for (int k3 = 0; k3 < valuetypes.getLength(); k3++) {
											                    Element value3 = (Element) valuetypes.item(k3);
											                    // The "derived - derived" are not having in account!
											                    if (value3.getAttribute(OMG_name).equals(grandgrandfather)) {
											                    	if (!value3.getAttribute(OMG_abstract).equals(OMG_true)) {
																		NodeList subChilds3 = value3.getChildNodes();
												                        for (int l3 = 0; l3 < subChilds3.getLength(); l3++){
												                            Element child3 = (Element) subChilds3.item(l3);
																		    String tag4 = child3.getTagName();
																		    
																		    if (tag4.equals(OMG_op_dcl) || tag4.equals(OMG_attr_dcl)) {
																		      	thereAreFatherOps = true;
															                }
												                        }
											                    	}
											                    }
											                }
											            }
													    
													    
													    if (tag3.equals(OMG_op_dcl) || tag3.equals(OMG_attr_dcl)) {
													      	thereAreFatherOps = true;
										                }
							                        }
						                    	}
						                    }
						                }
						            }
						                 
								   								    
								    if (tag2.equals(OMG_op_dcl) || tag2.equals(OMG_attr_dcl)) {
								      	thereAreFatherOps = true;
					                }
		                        }
			                    thereAreFather = true;
            			        father_Name = fatherVector[fatherVector.length - 2] + "::" +
		                        fatherVector[fatherVector.length - 1];						        
			                }
			                else {
            			        thereAreFather = false;
			                    father_Name = "";
            			    }

                    }
            		
            	}
            } else if (tag.equals(OMG_state_member)) {
                generateHppStateMemberDecl(stateBuffer, el, false);
                generateHppStateMemberDecl(OBVstateBuffer, el, true);
                String type = ((Element) el.getFirstChild()).getTagName();
                if (type.equals(OMG_struct) || type.equals(OMG_union)
                    || type.equals(OMG_enum))
                    generateCppSubPackageDef((Element) el.getFirstChild(),
                                             sourceDirectory, headerDirectory,
                                             valuetypeName, genPackage, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_op_dcl)) {
                opsBuffer.append("  public:");
                generateHppMethodHeader(opsBuffer, el, true, true, "\t");
                opsBuffer.append(";\n\n");
                thereAreOps = true;
            } else if (tag.equals(OMG_attr_dcl)) {
                opsBuffer.append("  public:");
                generateHppAttributeDecl(opsBuffer, el, false, "\t");
                thereAreOps = true;
            } else if (tag.equals(OMG_factory)) {
                //generateHppMethodHeader(initBuffer, el, true, "\t");
                // DAVV - la generacion de factorys va en un metodo aparte
            } else if (tag.equals(OMG_const_dcl)) {
                baseBuffer.append("  public:\n");
                generateHppConstDecl(baseBuffer, el);
            } else
                generateCppSubPackageDef(el, sourceDirectory, headerDirectory,
                                         valuetypeName, genPackage, expanded, h_ext, c_ext);
        }

        // operaciones de interfaces soportadas
        for (int i = 0; i < m_supported_interfaces.size(); i++) {
            Element actualInterface = (Element) m_supported_interfaces.get(i);
            NodeList children = actualInterface.getChildNodes();
            for (int j = 0; j < children.getLength(); j++) {
                Element child = (Element) children.item(j);
                String childTag = child.getTagName();
                if (childTag.equals(OMG_op_dcl)) {
                    opsBuffer.append("  public:");
                    generateHppMethodHeader(opsBuffer, child, true, true, "\t");
                    opsBuffer.append(";\n\n");
                    thereAreOps = true;
                } else if (childTag.equals(OMG_attr_dcl)) {
                    opsBuffer.append("  public:");
                    generateHppAttributeDecl(opsBuffer, child, true, "\t");
                    thereAreOps = true;
                } else if (childTag.equals(OMG_inheritance_spec)) {
                    NodeList grandPas = child.getChildNodes();
                    for (int k = 0; k < grandPas.getLength(); k++) {
                        Element grandPa = (Element) grandPas.item(k);
                        if (grandPa.getTagName().equals(OMG_scoped_name)) {
                            String grandPaName = grandPa.getAttribute(OMG_name);
                            Scope grandPaScope = 
                                Scope.getGlobalScopeInterface(grandPaName);
                            Element grandPaDef = grandPaScope.getElement();
                            String grandPaTag = grandPaDef.getTagName();
                            if (grandPaTag.equals(OMG_interface)) {
                                int l = 0;
                                while (l < m_supported_interfaces.size()
                                       && ((Element) m_supported_interfaces.get(l))
                                       != grandPaDef)
                                    l++;
                                if (l >= m_supported_interfaces.size())
                                    m_supported_interfaces.add(grandPaDef);
                            }
                        }
                    }

                }
            }
        }

        if (stateBuffer.length() > 0)
            baseBuffer.append("  // State members\n\n" + stateBuffer);
        if (opsBuffer.length() > 0)
            baseBuffer.append("  // Operations\n\n" + opsBuffer);

        if (thereAreFather)
            OBVBuffer.append(",\n\t public virtual ::OBV_" + father_Name);

        if (!thereAreOps) 
            OBVBuffer.append(",\n\t public virtual CORBA::DefaultValueRefCountBase");
        // aqu�!!: mapping C++, 1.17.2, justo antes del �ltimo p�rrafo
        // del apartado:
        // 'For valuetypes that have no operations other than factory
        // initializers, the same
        // constructors and destructors are generated, but with public access so
        // that THEY
        // CAN BE CALLED DIRECTLY BY APPLICATION CODE' (hablando de clases OBV_)
        // - esto obliga
        // a que la clase OBV_ sea concreta para que puedan instanciarse objetos
        // de la misma,
        // as� que hay que IMPLEMENTAR las operaciones de c�mputo de referencias
        // heredadas
        // desde CORBA::ValueBase

        OBVBuffer.append(" {\n\n");

        if (OBVstateBuffer.length() > 0)
            OBVBuffer.append("  // State members\n\n" + OBVstateBuffer);

        // Add operations to OBV code as virtual methods
        return (thereAreOps || thereAreFatherOps);
    }

    private void generateCppSubPackageDef(Element doc, String sourceDirectory,
                                          String headerDirectory,
                                          String valuetypeName,
                                          String genPackage, 
                                          boolean expanded, 
                                          String h_ext, 
                                          String c_ext)
        throws Exception
    {

        String newPackage;
        if (!genPackage.equals("")) {
            newPackage = genPackage + "::" + valuetypeName;//MACP "Package";
        } else {
            newPackage = valuetypeName;//MACP "Package";
        }

        Element definition = doc;
        String tag = definition.getTagName();
        if (tag.equals(OMG_const_dcl)) {
            XmlConst2Cpp gen = new XmlConst2Cpp();
            gen.generateCpp(definition, sourceDirectory, headerDirectory,
                            newPackage, this.m_generate, expanded, h_ext, c_ext);
        } else if (tag.equals(OMG_enum)) {
            XmlEnum2Cpp gen = new XmlEnum2Cpp();
            gen.generateCpp(definition, sourceDirectory, headerDirectory,
                            newPackage, this.m_generate, expanded, h_ext, c_ext);
        } else if (tag.equals(OMG_struct)) {
            XmlStruct2Cpp gen = new XmlStruct2Cpp();
            gen.generateCpp(definition, sourceDirectory, headerDirectory,
                            newPackage, this.m_generate, expanded, h_ext, c_ext);
        } else if (tag.equals(OMG_union)) {
            XmlUnion2Cpp gen = new XmlUnion2Cpp();
            gen.generateCpp(definition, sourceDirectory, headerDirectory,
                            newPackage, this.m_generate, expanded, h_ext, c_ext);
        } else if (tag.equals(OMG_exception)) {
            XmlException2Cpp gen = new XmlException2Cpp();
            gen.generateCpp(definition, sourceDirectory, headerDirectory,
                            newPackage, this.m_generate, expanded, h_ext, c_ext);
        } else if (tag.equals(OMG_typedef)) {
            XmlTypedef2Cpp gen = new XmlTypedef2Cpp();
            gen.generateCpp(definition, sourceDirectory, headerDirectory,
                            newPackage, this.m_generate, expanded, h_ext, c_ext);
        }
    }

    private void generateHppStateMemberDecl(StringBuffer buffer, Element doc,
                                            boolean isOBVclass)
        throws SemanticException
    {

        NodeList nodes = doc.getChildNodes(); 
        Element type = (Element) nodes.item(0); //      tipo de la declaraci�n
        String typeTag = type.getTagName();
		// Fix to bug #[#335] Compilation error: array into valuetype (C++)
        String accesorType = XmlType2Cpp.getAccesorType(type);
        String modifierType = XmlType2Cpp.getModifierType(type);
        String referentType = XmlType2Cpp.getReferentType(type);

        String kind = doc.getAttribute(OMG_kind); //      permisos de acceso
                                                  // (public/private)
        if (kind.equals(OMG_private))
            buffer.append("  protected:\n");
        else if (kind.equals(OMG_public))
            buffer.append("  public:\n");

        String endOfDeclaration = "";
        if (!isOBVclass)
            endOfDeclaration = " = 0";

        for (int i = 1; i < nodes.getLength(); i++) { //      recorrido de tantas
                                                      // declaraciones como se
                                                      // incluyan
            Element decl = (Element) nodes.item(i);
            String name = decl.getAttribute(OMG_name);

            String declType = decl.getTagName(); //      simple o array
            
            if (declType.equals(OMG_simple_declarator)) {
                
                // DAVV - accesor
                buffer.append("\tvirtual " + accesorType + " " + name
                              + "() const" + endOfDeclaration + ";\n");

                // DAVV - referente
                if (referentType != null)
                    buffer.append("\tvirtual " + referentType + " " + name
                                  + "()" + endOfDeclaration + ";\n");

                // DAVV - modificador
                
                buffer.append("\tvirtual void " + name + "(" + modifierType
                              + ")" + endOfDeclaration + ";\n");

                if (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type)) {
                    String varType;
                    if (XmlType2Cpp.isAWString(type))
                        varType = "CORBA::WString_var&";
                    else
                        varType = "CORBA::String_var&";
                    
                    buffer.append("\tvirtual void " + name + "(const "
                                  + varType + ")" + endOfDeclaration + ";\n");
                }

                if (isOBVclass) {
                    buffer.append("  protected:\n");                    
                    String classType = XmlType2Cpp.getMemberType(type);
                    buffer.append("\t" + classType + " _" + name + ";\n");
                }

            } else { // arrays an�nimos
                NodeList nl = decl.getChildNodes();
                StringBuffer arrayBounds = new StringBuffer();
                StringBuffer sliceBounds = new StringBuffer();
                Element el = null;
                //int dimensions = 0;
                for (int j = 0; j < nl.getLength(); j++) {
                    el = (Element) nl.item(j);
                    if (el != null) {
                        Element expr = (Element) el.getFirstChild();
                        if (expr != null) {
                            arrayBounds.append("["
                                               + XmlExpr2Cpp.getIntExpr(expr)
                                               + "]");
                            if (j > 0)
                                sliceBounds.append("["
                                                   + XmlExpr2Cpp.getIntExpr(expr) 
                                                   + "]");
                            //dimensions++;
                        }
                    }
                }
                String typeStr = null;
                if (typeTag.equals(OMG_scoped_name))
                    typeStr = type.getAttribute(OMG_name);
                else
                    typeStr = XmlType2Cpp.getType(type);
                buffer.append("\ttypedef " + typeStr + " _" + name + "_slice"
                              + sliceBounds + ";\n");
                throw new SemanticException(
                                            "Anonimous array state member not supported yet",
                                            decl);
            }

            buffer.append("\n");
        }
    }

    private void generateCppStateMemberDecl(StringBuffer buffer, Element doc,
                                            String genPackage, String valueName)
    {
        NodeList nodes = doc.getChildNodes(); 
        Element type = (Element) nodes.item(0); // tipo de la declaraci�n        
		// Fix to bug [#335] Compilation error: array into valuetype (C++)
        String accesorType =  XmlType2Cpp.getAccesorType(type);
        String modifierType = XmlType2Cpp.getModifierType(type);
        String referentType = XmlType2Cpp.getReferentType(type);

        String valueWithPackage = genPackage.equals("") ? valueName
            : genPackage + "::" + valueName;

        for (int i = 1; i < nodes.getLength(); i++) { // recorrido de tantas
                                                      // declaraciones como se
                                                      // incluyan
            Element decl = (Element) nodes.item(i);
            String name = decl.getAttribute(OMG_name);

            String declType = decl.getTagName(); // simple o array
            if (declType.equals(OMG_simple_declarator)) {
                // accesor
                buffer.append(accesorType + " " + valueWithPackage + "::"
                              + name + "() const {\n\t");
                
                buffer.append(XmlType2Cpp.getMemberReturnAccesor(type, valueName, "_" + name));
                buffer.append("}\n\n");

                // referente
                if (referentType != null) {
                    
                    buffer.append(referentType + " " + valueWithPackage + "::"
                                  + name + "() {\n");
                    
                    if (XmlType2Cpp.isAString(type) 
                        || XmlType2Cpp.isAString(type)
                        || XmlType2Cpp.isAnValuetype(type)
                        || XmlType2Cpp.isAnInterface(type)) {
                        buffer.append("\treturn _" + name + ".inout();\n");
                    } else { 
                        buffer.append("\treturn _" + name + ";\n");
                    }
                    buffer.append("}\n\n");                  
                }

                // modificador
                buffer.append("void " + valueWithPackage + "::" + name + "("
                              + modifierType + " v) {\n");
                if (XmlType2Cpp.getDefinitionType(type).equals(OMG_array))
               		// Fix to bug [#335] Compilation error: array into valuetype (C++)
                	buffer.append("\t" + XmlType2Cpp.getParamType(type, "inout") 
                			+ "_copy(_"+ name + ", v);\n");
                else
                    buffer.append("\t_" + name + " = v;\n");
                buffer.append("}\n\n");

                if (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type)) {
                    String varType;
                    if (XmlType2Cpp.isAWString(type))
                        varType = "CORBA::WString_var&";
                    else
                        varType = "CORBA::String_var&";
                    
                    buffer.append("void " + valueWithPackage + "::" + name
                                  + "(const " + varType + " v) {\n");
                    buffer.append("\t_" + name + " = v;\n");
                    buffer.append("}\n\n");
                }

            } else { // arrays an�nimos
            }

            buffer.append("\n");
        }

    }

    private void generateHppConstDecl(StringBuffer buffer, Element doc)
    {

        NodeList nodes = doc.getChildNodes();

        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        buffer.append("\tstatic const ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(doc.getAttribute(OMG_name));
        // solo declaracion
        /*
         * buffer.append(" = ("); buffer.append(type); buffer.append(")");
         *  // Expre generation Element exprEl = (Element)nodes.item(1);
         * //Object expr = XmlExpr2Cpp.getExpr(exprEl,type);
         * //IdlConstants.getInstance().add(scopedName, type, expr); Object expr =
         * IdlConstants.getInstance().getValue(scopedName); String typeExpr =
         * IdlConstants.getInstance().getType(scopedName);
         * buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
         */
        buffer.append(";\n\n");
    }

    private void generateCppConstDecl(StringBuffer buffer, Element doc,
                                      String className)
        throws SemanticException
    {
        NodeList nodes = doc.getChildNodes();
        String scopedName = doc.getAttribute(OMG_scoped_name);

        // inicializacion
        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Cpp.getType(typeEl);
        buffer.append("const ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(className);
        buffer.append("::");
        buffer.append(doc.getAttribute(OMG_name));
        buffer.append(" = ");
        /*
         * buffer.append(" = ("); buffer.append(type);
         * //buffer.append(");\n\n"); buffer.append(")");
         */
        // Expre generation
        /*
         * Element exprEl = (Element)nodes.item(1); Object expr =
         * XmlExpr2Cpp.getExpr(exprEl, type); IdlConstants.getInstance()
         * .add(scopedName, type, expr);
         */
        Object expr = IdlConstants.getInstance().getValue(scopedName);
        String typeExpr = IdlConstants.getInstance().getType(scopedName);
        buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
        buffer.append(";\n\n");
    };

    private String generateCppHelperDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String isBoxedS = doc.getAttribute(OMG_boxed);
        boolean isBoxed = (isBoxedS != null) && (isBoxedS.equals(OMG_true));
        if (!isBoxed) {
            buffer.append(XmlCppHelperGenerator.generateCpp(doc, null,
                                                            genPackage,false));
        } else {
            //;
            buffer.append(generateCppBoxedValuetypeHelper(doc,genPackage));
        }
        return buffer.toString();
    }
    


    private String generateHppHelperDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String isBoxedS = doc.getAttribute(OMG_boxed);
        boolean isBoxed = (isBoxedS != null) && (isBoxedS.equals(OMG_true));
        if (!isBoxed) {
            buffer.append(XmlCppHelperGenerator.generateHpp(doc, null,
                                                            genPackage,false));
        } else {
            //;
            buffer.append(generateHppBoxedValuetypeHelper(doc,genPackage));
        }
        return buffer.toString();
    }

    private String generateHppBoxedValuetypeHelper(Element doc, String genPackage){
    	
        StringBuffer buffer = new StringBuffer();
        
        Element discriminant = doc;
        String name = doc.getAttribute(OMG_name);
        
        Element child = (Element) doc.getChildNodes().item(0);
        String type = child.getAttribute(OMG_name);
        
        String helperName = XmlType2Cpp.getHelperName(name);
        String nameWithPackage = genPackage.equals("") ? name : 
            genPackage + "::" + name;

        buffer.append("class " + helperName + " {\n");
        buffer.append("\n\tpublic:\n");

        // TypeCode y RepositoryId
        buffer.append("\t\tstatic ::CORBA::TypeCode_ptr type();\n\n");
        buffer.append("\t\tstatic const char* id() { return \""
                      + RepositoryIdManager.getInstance().get(doc)
                      + "\"; }\n\n");
        
        // Inserci�n y extracci�n en Any - variables en n�mero (mapping
        // C++)
        String insertTypeParamCopy = XmlCppHelperGenerator.getInsertType(discriminant,
                                                                         nameWithPackage, true);
        String insertTypeParamNoCopy = XmlCppHelperGenerator.getInsertType(discriminant,
                                                                           nameWithPackage, false);
        String extractTypeParam = XmlCppHelperGenerator.getExtractType(discriminant, nameWithPackage);
        
        if (!insertTypeParamCopy.equals(""))
            buffer.append("\t\tstatic void insert(::CORBA::Any& any, "
                          + insertTypeParamCopy + " _value);\n\n");
        if (!insertTypeParamNoCopy.equals(""))
            buffer.append("\t\tstatic void insert(::CORBA::Any& any, "
                          + insertTypeParamNoCopy + " _value);\n\n");
        if (!name.equals(""))
            buffer.append("\t\tstatic CORBA::Boolean extract(const ::CORBA::Any& any, const "
                          + extractTypeParam /*nameWithPackage*/ + " _value);\n\n");
        
        String writeTypeParam = XmlCppHelperGenerator.getWriteType(discriminant, nameWithPackage);
        String readTypeParam = XmlCppHelperGenerator.getReadType(discriminant, nameWithPackage);
        buffer.append("\t\tstatic void read(::TIDorb::portable::InputStream& is, "
                      + readTypeParam + " _value);\n\n");
        buffer.append("\t\tstatic void write(::TIDorb::portable::OutputStream& os, "
                      + writeTypeParam + " _value);\n\n");

        buffer.append("};// End of helper definition\n\n");

        return buffer.toString();
        
    }
    
    private String generateCppBoxedValuetypeHelper(Element doc, String genPackage) 
    	throws Exception
    {
    
        StringBuffer buffer = new StringBuffer();
        Element discriminant = doc;
        String name = doc.getAttribute(OMG_name);
        
        Element child = (Element) doc.getChildNodes().item(0);
        String type = child.getAttribute(OMG_name);
        
        String nameWithPackage = genPackage.equals("") ? name : genPackage
            + "::" + name;
        String helperName = XmlType2Cpp.getHelperName(nameWithPackage);
        String holderName = XmlType2Cpp.getHolderName(nameWithPackage);
        
        // Inserci�n y extracci�n en Any - variables en n�mero (mapping
        // C++)
        String insertTypeParamCopy = XmlCppHelperGenerator.getInsertType(discriminant,
                                                                         nameWithPackage, true);
        String insertTypeParamNoCopy =  XmlCppHelperGenerator.getInsertType(discriminant,
                                                                            nameWithPackage, false);
        String extractTypeParam = XmlCppHelperGenerator.getExtractType(discriminant, nameWithPackage);
        if (!insertTypeParamCopy.equals("")) {
            buffer.append("void " + helperName + "::insert(::CORBA::Any& any, "
                          + insertTypeParamCopy + " _value) {\n");
            XmlCppHelperGenerator.generateInsertImplementation(discriminant, nameWithPackage,
                                                               holderName, buffer, true);
            buffer.append("}\n\n");
        }
        if (!insertTypeParamNoCopy.equals("")) {
            buffer.append("void " + helperName + "::insert(::CORBA::Any& any, "
                          + insertTypeParamNoCopy + " _value) {\n");
            XmlCppHelperGenerator.generateInsertImplementation(discriminant, nameWithPackage,
                                                               holderName, buffer, false);
            buffer.append("}\n\n");
        }
        if (!extractTypeParam.equals("")) {
            buffer.append("CORBA::Boolean " + helperName
                          + "::extract(const ::CORBA::Any& any, const "
                          + extractTypeParam /*nameWithPackage*/ + " _value) {\n");
            XmlCppHelperGenerator.generateExtractImplementation(discriminant, buffer,
                                                                nameWithPackage, holderName);
            buffer.append("}\n\n");
        }
        // TypeCode
        buffer.append("CORBA::TypeCode_ptr " + helperName + "::type() {\n");
        //      genera la implementacion del metodo type() de la clase helper
        XmlCppHelperGenerator.generateTypeImplementation(discriminant, buffer, name, genPackage);
        
        buffer.append("}\n\n");

        String tc = genPackage.equals("") ? "_tc_" : genPackage + "::_tc_";
        buffer.append("const ::CORBA::TypeCode_ptr " + tc);
        buffer.append(name);
        buffer.append("=");
        buffer.append(helperName);
        buffer.append("::type();\n\n");

        buffer.append("void " + helperName
                      + "::read(::TIDorb::portable::InputStream& is, "
                      + XmlCppHelperGenerator.getReadType(discriminant, nameWithPackage)
                      + " _value) {\n");
        //XmlCppHelperGenerator.generateReadImplementation(discriminant, nameWithPackage, buffer);
    	Element root = doc.getOwnerDocument().getDocumentElement(); //vamos a la raiz del DOM
    	
    	NodeList enums = root.getElementsByTagName(OMG_struct); //Buscamos los structs que 
        //haya en el �rbol
    	String childName = "";
    	for (int k = 0; k < enums.getLength(); k++) {		//recorremos los structs buscando 
            //el que corresponde al valuetype
            Element str = (Element) enums.item(k);
            if (str.getAttribute(OMG_scoped_name).equals(type)) {
                childName = str.getAttribute(OMG_name);
            }
    	}

        // Struct
        String definition = XmlType2Cpp.getDefinitionType(child);

        if (definition.equals(OMG_struct)) {
            System.out.println("XmlValueType2Cpp.generateCppBoxedValuetypeHelper");
            buffer.append("\t" + genPackage + "::"+ "_" + childName + 
                          "Helper::read(is, _value -> m_the_struct);\n");
        } 
//         else if (definition.equals(OMG_struct)) {

//         }
//         else if (definition.equals(OMG_struct)) {

//         }

        buffer.append(genPackage + "::"+ "_" + childName + 
                      "Helper read(is, _value -> m_the_value);\n");


        buffer.append("}\n\n");
        
        // narrow para interfaces
        if (XmlType2Cpp.getDefinitionType(discriminant).equals(OMG_interface)) {
            buffer.append(nameWithPackage
                          + "_ptr "
                          + helperName
                          + "::narrow(const ::CORBA::Object_ptr obj, bool is_a) {\n");
            if (discriminant.getTagName().equals(OMG_interface)) {
            	XmlCppHelperGenerator.generateNarrowImplementation(discriminant, genPackage, buffer);
            } else {
            	XmlCppHelperGenerator.generateNarrowImplementation(null, genPackage, buffer); 
                // typedef de un interfaz
            }
            buffer.append("}\n\n");
        }

        buffer.append("void " + helperName
                      + "::write(::TIDorb::portable::OutputStream& os, "
                      //+ XmlCppHelperGenerator.getWriteType(discriminant, nameWithPackage)
                      + XmlCppHelperGenerator.getWriteType(discriminant, nameWithPackage)
                      + " _value) {\n");
        //XmlCppHelperGenerator.generateWriteImplementation(discriminant, buffer);
        buffer.append(genPackage + "::"+ "_" + childName + "Helper write(is, _value -> m_the_value);\n");
        buffer.append("}\n\n");
        buffer.append("}\n\n");

        return buffer.toString();
    }
    
    

    
    private void generateHppConstructionMethods(StringBuffer baseBuffer,
                                                StringBuffer OBVBuffer,
                                                Element doc, String genPackage,
                                                boolean thereAreOps)
    {

        String name = doc.getAttribute(OMG_name);
        String OBVname = name;
        if (genPackage.equals(""))
            OBVname = "OBV_" + name;

        baseBuffer.append("  public:\n");
        baseBuffer.append("\tstatic " + name
                          + "* _downcast(CORBA::ValueBase*);\n\n");
        baseBuffer.append("\t::CORBA::TypeCode_ptr _type() const;\n\n");
 
        //buffer.append("\t\t\t" + helperClass + "::write(outs, value); \n");
        
        if (!new Boolean (doc.getAttribute(OMG_custom)).booleanValue())
            {
                baseBuffer.append("\tvoid _write(::TIDorb::portable::OutputStream& outs) const;\n");
                //        	baseBuffer.append("\t\touts.write_Value(this);\n");
                //	        baseBuffer.append("\t}\n\n");
	        baseBuffer.append("\tvoid _read(::TIDorb::portable::InputStream& ins);\n");
                //	        baseBuffer.append("\t{\n");
                //	        baseBuffer.append("\t\tCORBA::ValueBase* val;\n");
                //	        baseBuffer.append("\t\tins.read_Value(val);\n");
                //	        baseBuffer.append("\t\t CORBA::remove_ref(this);\n");
                //	        baseBuffer.append("\t\tthis = " + name + "::_downcast(val);\n");
                //	        baseBuffer.append("\t}\n");
	    }
        
        
        String usage = "  protected:\n";

        baseBuffer.append("  // Constructor and destructor\n");
        baseBuffer.append(usage);
        baseBuffer.append("\t" + name + "() {};\n"); // constructor por
        // defecto
        baseBuffer.append("\tvirtual ~" + name + "() {};\n"); // destructor

        baseBuffer.append("\n");
        //        baseBuffer.append("  // For nobody to use\n");
        //        //buffer.append(" private:\n\t " + name + "(const " + name + "&)
        //        // {}\n");
        //        baseBuffer.append("  private:\n");
        //        baseBuffer.append("\t void operator= (const " + name + "&);\n");

        baseBuffer.append("\n");

        OBVBuffer.append("  // Constructors and destructor\n");
        if (!thereAreOps)
            usage = "  public:\n";
        OBVBuffer.append(usage);
        OBVBuffer.append("\t" + OBVname + "(); \n"); // {};\n"); // constructor por defecto
        StringBuffer listBuffer = new StringBuffer();
        StringBuffer asigBuffer = new StringBuffer();
        StringBuffer sentenceBuffer = new StringBuffer();
        boolean hayUno = false;
        NodeList children = doc.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Element child = (Element) children.item(i);
            if (child.getTagName().equals(OMG_state_member)) {
                Element type = (Element) child.getFirstChild();
                NodeList state_children = child.getChildNodes();
                for (int j = 1; j < state_children.getLength(); j++) {
                    Element decl = (Element) state_children.item(j);
                    if (decl.getTagName().equals(OMG_simple_declarator)) {
                        if (hayUno) {
                            listBuffer.append(",\n\t\t");
                            if (!XmlType2Cpp.getDefinitionType(type).equals(OMG_array))
                            	asigBuffer.append(",\n");
                        } else
                            hayUno = true;
                        listBuffer.append(XmlType2Cpp.getParamType(type, "in")
                                          + " " + decl.getAttribute(OMG_name));
                        if (XmlType2Cpp
                            .getDefinitionType(type).equals(OMG_array))
                        	// Fix to bug [#335] Compilation error: array into valuetype (C++)                            
                             sentenceBuffer.append("\t\t" 
                            		 		  + XmlType2Cpp.getType(type)
                            		 		  + "_copy("
                            		 		  + "_" + decl.getAttribute(OMG_name)
                            		 		  + ", " + decl.getAttribute(OMG_name)
                            		 		  + ");\n");
                              
                        else if (XmlType2Cpp
                                 .getDefinitionType(type).equals(OMG_interface))
                            asigBuffer.append("\t\t\t_"
                                              + decl.getAttribute(OMG_name)
                                              + "(" + XmlType2Cpp.getType(type)
                                              + "::_duplicate("
                                              + decl.getAttribute(OMG_name)
                                              + "))");
                        else
                            asigBuffer.append("\t\t\t_"
                                              + decl.getAttribute(OMG_name)
                                              + "("
                                              + decl.getAttribute(OMG_name)
                                              + ")");
                    } else {}
                }
            }
        }
        if (hayUno) {
            OBVBuffer.append("\t" + OBVname + "(");
        	// Fix to bug [#335] Compilation error: array into valuetype (C++)
            OBVBuffer.append(listBuffer + ")"); 
            if (asigBuffer.length() > 0) 
               OBVBuffer.append(": \n" + asigBuffer);
            if (sentenceBuffer.length() > 0)
               OBVBuffer.append("\n\t{\n " + sentenceBuffer + "\t}\n");
            else 
            	OBVBuffer.append("\n\t{ }\n");
        }
        OBVBuffer.append("\tvirtual ~" + OBVname + "() {};\n"); // destructor

        OBVBuffer.append("\t" + OBVname + "*" + " _copy_value();\n");  // _copy_value()
 
        OBVBuffer.append("\n");
        OBVBuffer.append("  // For nobody to use\n");
        //buffer.append(" private:\n\t " + name + "(const " + name + "&)
        // {}\n");
        OBVBuffer.append("  private:\n");
        OBVBuffer.append("\tvoid operator= (const " + OBVname + "&);\n");

        OBVBuffer.append("\n");

    }

    private void generateHppFactoryDef(StringBuffer buffer, Element doc,
                                       boolean thereAreOps)
    {

        boolean isAbstract = doc.getAttribute(OMG_abstract).equals(OMG_true);
        if (!isAbstract) {
            NodeList children = doc.getChildNodes();
            StringBuffer createBuffer = new StringBuffer();
            for (int i = 0; i < children.getLength(); i++) {
                
                Element child = (Element) children.item(i);
                String tag = child.getTagName();
                if (tag.equals(OMG_factory)) {
                    // generaci�n de operaciones de creacion
                    
                    NodeList nodes = child.getChildNodes();
                    createBuffer.append("\n\tvirtual "
                                        + doc.getAttribute(OMG_name) + "* "); // tipo
                                                                              // de
                                                                              // retorno
                    String nombre = child.getAttribute(OMG_name); // nombre del
                                                                  // metodo
                    createBuffer.append(nombre + "(");
                    int contador = 0;
                    for (int j = 0; j < nodes.getLength(); j++) {
                    
                        Element el = (Element) nodes.item(j);
                        if (el.getTagName().equals(OMG_init_param_decl)) {
                            contador++;
                            Element paramType = 
                                (Element) el.getChildNodes().item(0);
                            String paramName = el.getAttribute(OMG_name);// nombre
                            // de
                            // parametro
                            if (contador > 1)
                                createBuffer.append(", ");
                            String paramTypeS = XmlType2Cpp.getParamType(
                                                                         paramType,
                                                                         el.getAttribute(OMG_kind));
                            createBuffer.append(paramTypeS); // tipo de
                                                             // parametro
                            createBuffer.append(" ");
                            createBuffer.append(paramName);
                        }
                    }
                    createBuffer.append(")");
                    createBuffer.append(" = 0;\n");
                }

            }

            if (createBuffer.length() > 0) {
                // hay 'factory' en el valuetype - generamos clase
                // abstracta
                String name = doc.getAttribute(OMG_name);
                buffer.append("class " + name
                              + "_init : public CORBA::ValueFactoryBase {\n\n");
                buffer.append("  // Initializers\n");
                buffer.append("  public:" + createBuffer + "\n");
                buffer.append("\tvirtual ~" + name + "_init() {}\n");
                buffer.append("\tstatic " + name
                              + "_init* _downcast(CORBA::ValueFactory vf);\n\n");
                buffer.append("  protected:\n");
                buffer.append("\t" + name + "_init() {}\n};\n\n");
            } else if (!thereAreOps) {
                // no hay 'factory' ni operaciones - generamos clase
                // concreta
                String name = doc.getAttribute(OMG_name);
                buffer.append("class " + name
                              + "_init : public CORBA::ValueFactoryBase {\n\n");
                buffer.append("  // Initializers\n");
                buffer.append("  public:" + "\n");
                buffer.append("\tvirtual ~" + name + "_init() {}\n");
                buffer.append("\tstatic " + name
                              + "_init* _downcast(CORBA::ValueFactory vf);\n");
                buffer.append("\t" + name + "_init() {};\n\n");
                buffer.append("  private:\n");
                buffer.append("\tvirtual CORBA::ValueBase* create_for_unmarshal();\n};\n\n");
            }
            // no hay 'factory' pero s� operaciones - no generamos clase,
            // responsabilidad del usuario
        }
    }

    private void generateCppFactoryDef(StringBuffer buffer, String genPackage,
                                       Element doc, boolean thereAreOps)
    {
        // implementacion de las funciones de las clases factory
        boolean isAbstract = doc.getAttribute(OMG_abstract).equals(OMG_true);
        if (!isAbstract) {
            NodeList children = doc.getChildNodes();
            boolean thereAreInits = false;
            for (int i = 0; i < children.getLength(); i++) {
                Element child = (Element) children.item(i);
                String tag = child.getTagName();
                if (tag.equals(OMG_factory)) {
                    thereAreInits = true;
                    // los inicializadores son virtuales puros, pero es
                    // necesario saber si hay alguno
                }
            }

            if (thereAreInits) {
                // hay 'factory' en el valuetype - generamos clase
                // abstracta, hay que implementar _downcast!!
                String name = doc.getAttribute(OMG_name);
                String nameWithPackage = genPackage.equals("") ? name
                    : genPackage + "::" + name;
                buffer.append(nameWithPackage + "_init* " + nameWithPackage
                              + "_init::_downcast(CORBA::ValueFactory vf) {\n");
                buffer.append("\tif (vf == NULL)\n");
                buffer.append("\t\treturn NULL;\n");
                buffer.append("\treturn dynamic_cast<" + nameWithPackage
                              + "_init*>(vf);\n");
                buffer.append("}\n\n");
            } else if (!thereAreOps) {
                // no hay 'factory' ni operaciones - generamos clase
                // concreta
                String name = doc.getAttribute(OMG_name);
                String nameWithPackage = genPackage.equals("") ? name
                    : genPackage + "::" + name;
                buffer.append(nameWithPackage + "_init* " + nameWithPackage
                              + "_init::_downcast(CORBA::ValueFactory vf) {\n");
                buffer.append("\tif (vf == NULL)\n");
                buffer.append("\t\treturn NULL;\n");
                buffer.append("\treturn dynamic_cast<" + nameWithPackage
                              + "_init*>(vf);\n");
                buffer.append("}\n\n");
                buffer.append("CORBA::ValueBase* " + nameWithPackage
                              + "_init::create_for_unmarshal() {\n");
                buffer.append("\treturn new OBV_" + nameWithPackage + "();\n");
                buffer.append("}\n\n");
            }
            // no hay 'factory' pero s� operaciones - no generamos clase,
            // responsabilidad del usuario
        }

    }
}
