/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 126 $
* Date: $Date: 2006-03-21 10:12:42 +0100 (Tue, 21 Mar 2006) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telef???nica Investigaci???n y Desarrollo
*     S.A.Unipersonal (Telef???nica I+D)
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

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.xmlsemantics.Scope;
import es.tid.TIDIdlc.idl2xml.CPreprocessor;
import es.tid.TIDIdlc.idl2xml.Idl2Xml;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.idl2xml.Preprocessor;
import es.tid.TIDIdlc.util.*;

import org.w3c.dom.Attr;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

//import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileWriter;

/**
 * Generates Cpp for modules.
 */
class XmlModule2Cpp
    implements Idl2XmlNames
{

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean createDir, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {


        // Get the package whole name
        if (genPackage.length() > 0) {
            String s = doc.getAttribute(OMG_name);
            if (!s.equals("") && !s.equals("::")) {
                genPackage = genPackage + "::" + doc.getAttribute(OMG_name);
            }
        } else {
            String s = doc.getAttribute(OMG_name);
            if (!s.equals("") && !s.equals("::")) {
                genPackage = doc.getAttribute(OMG_name);
            }
        }

        // Module generation
        Traces.println("XmlModule2Cpp:->", Traces.DEEP_DEBUG);
        Traces.println("Generating module: " + headerDirectory + "...",
                       Traces.USER);
        generateCppModuleDef(doc, sourceDirectory, headerDirectory, genPackage,
                             createDir, expanded, h_ext, c_ext);
    }

    private void generateCppModuleDef(Element doc, String sourceDirectory,
                                      String headerDirectory,
                                      String genPackage, boolean createDir, boolean expanded,
									  String h_ext, String c_ext)
        throws Exception
    {

    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();

        NodeList definitions = doc.getChildNodes();
        if (genPackage.startsWith("::"))
            genPackage = genPackage.substring(2);

        for (int i = 0; i < definitions.getLength(); i++) {
            Element definition = (Element) definitions.item(i);
            String tag = definition.getTagName();
/*            
        	if (doc.getTagName().equals(OMG_specification)) {
                String lineString = definition.getAttribute("line");
        		if (lineString!=null) {
        			int line = Integer.parseInt(lineString);
        			String idlFile = Preprocessor.getInstance().locateFile(line);
        			String fileName = CompilerConf.getFile();
        			if (!idlFile.equals(fileName)) {
        				continue;
        			}
        		}
        	}
*/

            if (tag.equals(OMG_module)) {
                //FileWriter writer;
                //BufferedWriter buf_writer;
                String fileName, contents;
                StringBuffer buffer = new StringBuffer();
                fileName = definition.getAttribute(OMG_name) + h_ext; //".h";
                Attr not_code = 
                    definition.getAttributeNode(OMG_Do_Not_Generate_Code);
                XmlModule2Cpp gen = new XmlModule2Cpp();
                String name = definition.getAttribute(OMG_name);

                // Header generation.
                // cabecera del archivo
                XmlHppHeaderGenerator.generate(definition, buffer, "module",
                                               name, genPackage);
                // codigo especifico del modulo
                buffer.append("namespace ");
                buffer.append(name);
                buffer.append("\n{\n");
                XmlHppHeaderGenerator.includeForwardDeclarations(definition,
                                                                 buffer,
                                                                 "module",
                                                                 name,
                                                                 genPackage);
                XmlHppHeaderGenerator.includeChildrenHeaderFiles(definition,
                                                                 buffer,
                                                                 "module",
                                                                 name,
                                                                 genPackage);
                buffer.append("}// end of namespace ");
                buffer.append(name);
                // pie de archivo
                XmlHppHeaderGenerator.generateFoot(buffer, "module", name,
                                                   genPackage);
                //contents = buffer.toString();

                if (not_code == null) {
                    String headerDir = Xml2Cpp.getDir(genPackage,
                                                       headerDirectory, true);
                    Traces.println("XmlModule2Cpp:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + headerDir
                                   + File.separatorChar + fileName + "...",
                                   Traces.USER);
                    fileName = definition.getAttribute(OMG_name) + h_ext; //".h";
                    String idl_fn = XmlUtil.getIdlFileName(definition);

                    fm.addFile(buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
                    //writer = new FileWriter(headerDir + File.separatorChar
                    //                        + fileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(contents);
                    //buf_writer.close();

                    // Design of the header files, Any operations outside main
                    // file.
                    buffer = new StringBuffer();
                    XmlHppExternalOperationsGenerator.generateHpp(definition,
                                                                  buffer,
                                                                  "module",
                                                                  name,
                                                                  genPackage);
                    //contents = buffer.toString();
                    fileName = definition.getAttribute(OMG_name) + "_ext" + h_ext;

                    fm.addFile(buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);

                    //writer = new FileWriter(headerDir + File.separatorChar
                    //                        + fileName);
                    //buf_writer = new BufferedWriter(writer);
                    //buf_writer.write(contents);
                    //buf_writer.close();

                    generatePOAHeaderFiles(genPackage, definition, headerDir,
                                           headerDirectory, expanded, h_ext, c_ext);
                    generateOBVHeaderFiles(genPackage, definition, headerDir,
                                           headerDirectory, expanded, h_ext, c_ext);

                    // The attribute OMG_Do_Not_Generate_Code
                    // doesn't exists, so we generate code
                    gen.generateCpp(definition, sourceDirectory,
                                    headerDirectory, genPackage, true, expanded, h_ext, c_ext);
                } else {
                    // The attribute OMG_Do_Not_Generate_Code
                    // exists, so we don't generate code
                    Traces.println("XmlModule2Cpp:->", Traces.DEEP_DEBUG);
                    ///////////////////////////////////////////////////
                    Traces.println(
                        "Atention Generating OMG_Do_Not_Generate_Code is true. ",
                        Traces.USER);
                    //////////////////////////////////////////////////////
                    gen.generateCpp(definition, sourceDirectory,
                                    headerDirectory, genPackage, false, expanded, h_ext, c_ext);
                } // OMG_do_not_generate_code exists so we didn`t generate code.
            } else if (tag.equals(OMG_interface)) {
                XmlInterface2Cpp gen = new XmlInterface2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_const_dcl)) {
                XmlConst2Cpp gen = new XmlConst2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_enum)) {
                XmlEnum2Cpp gen = new XmlEnum2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_struct)) {
                XmlStruct2Cpp gen = new XmlStruct2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_union)) {
                XmlUnion2Cpp gen = new XmlUnion2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_exception)) {
                XmlException2Cpp gen = new XmlException2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_typedef)) {
                XmlTypedef2Cpp gen = new XmlTypedef2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_valuetype)) {
                XmlValuetype2Cpp gen = new XmlValuetype2Cpp();
                gen.generateCpp(definition, sourceDirectory, headerDirectory,
                                genPackage, createDir, expanded, h_ext, c_ext);
            }
        }
    }

    private void generatePOAHeaderFiles(String genPackage, Element definition,
                                        String headerDir, String headerDirectory,
										boolean expanded, 
										String h_ext, 
										String c_ext)
        throws Exception
    {

    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();

    	/** ******** POA File declarator. ************** */
        String name = definition.getAttribute(OMG_name);
        // no queremos estructura de directorios ni POA_headers si no va
        // a generarse ningun skeleton
        NodeList interfaces = definition.getElementsByTagName(OMG_interface);
        boolean thereIsOnePOAInterface = false;
        for (int k = 0; k < interfaces.getLength() && !thereIsOnePOAInterface; k++) {
            Element anInterface = (Element) interfaces.item(k);
            boolean isForward = anInterface
                .getAttribute(OMG_fwd).equals(OMG_true);
            boolean isLocal = anInterface
                .getAttribute(OMG_local).equals(OMG_true);
            thereIsOnePOAInterface = !isForward && !isLocal;
        }
        // eso obliga a comprobar no s???lo interfaces, sino tambi???n
        // valuetypes que soporten interfaces no abstractas:
        NodeList valuetypes = definition.getElementsByTagName(OMG_valuetype);
        boolean thereIsOnePOAValuetype = false;
        for (int j = 0; j < valuetypes.getLength() && !thereIsOnePOAValuetype; j++) {
            Element valuetype = (Element) valuetypes.item(j);
            Element inheritance = (Element) valuetype.getFirstChild();
            if (inheritance.getTagName().equals(OMG_value_inheritance_spec)) {
                NodeList parents = inheritance.getChildNodes();
                for (int k = 0; k < parents.getLength(); k++) {
                    Element parent = (Element) parents.item(k);
                    if (parent.getTagName().equals(OMG_supports)) {
                        NodeList supports = parent.getChildNodes();
                        for (int l = 0; l < supports.getLength(); l++) {
                            Element supported = (Element) supports.item(l);
                            if (supported.getTagName().equals(OMG_scoped_name)) {
                                Scope inhScope = 
                                    Scope.getGlobalScopeInterface(
                                        supported.getAttribute(OMG_name));
                                Element supportedDef = inhScope.getElement();
                                thereIsOnePOAValuetype = 
                                    (supportedDef.getTagName().equals(OMG_interface)
                                    && !supportedDef.getAttribute(OMG_abstract).equals(OMG_true));
                            }
                        }
                    }
                }
            }
        }
        if (!CompilerConf.getNoSkel()
            && (thereIsOnePOAInterface || thereIsOnePOAValuetype)) {
            StringBuffer buffer = new StringBuffer();
            XmlHppExternalPOAGenerator.generateHpp(definition, buffer,
                                                   "module", name, genPackage);
            String contents = buffer.toString();
            String fileName;

            String POAheaderDir;
            if (genPackage != "") { // m???dulo dentro de otro m???dulo
                //POAheaderDir = headerDirectory + File.separatorChar + "POA_" + genPackage; 
                POAheaderDir = Xml2Cpp.getDir("POA_" + genPackage,
                                               headerDirectory, true);
                fileName = definition.getAttribute(OMG_name) + h_ext;
            } else { // m???dulo en ???mbito global
                POAheaderDir = headerDir;
                fileName = "POA_" + definition.getAttribute(OMG_name) + h_ext;
            }
            String idl_fn = XmlUtil.getIdlFileName(definition);

            fm.addFile(buffer, fileName, POAheaderDir, idl_fn, FileManager.TYPE_POA_HEADER);

            //FileWriter writer = new FileWriter(POAheaderDir
            //                                   + File.separatorChar + fileName);
            //BufferedWriter buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        }
        /** ******** end of POA File declarator. ************** */
    }

    private void generateOBVHeaderFiles(String genPackage, Element definition,
                                        String headerDir, String headerDirectory,
										boolean expanded, 
										String h_ext, 
										String c_ext)
        throws Exception
    {

    	// Gets the File Manager
    	FileManager fm = FileManager.getInstance();

        /** ******** OBV File declarator. ************** */
        // hay que construir una jerarqu???a similar a la de los POA para
        // las clases OBV asociadas a los valuetypes
        String name = definition.getAttribute(OMG_name);
        NodeList valuetypes = definition.getElementsByTagName(OMG_valuetype);
        boolean thereIsOneConcreteValuetype = false;
        for (int j = 0; j < valuetypes.getLength()
                        && !thereIsOneConcreteValuetype; j++) {
            Element valuetype = (Element) valuetypes.item(j);
            boolean isAbstract = 
                valuetype.getAttribute(OMG_abstract).equals(OMG_true);
            boolean isBoxed = 
                valuetype.getAttribute(OMG_boxed).equals(OMG_true);
            thereIsOneConcreteValuetype = !isAbstract && !isBoxed;
        }

        if (!CompilerConf.getNoSkel() && thereIsOneConcreteValuetype) {
            StringBuffer buffer = new StringBuffer();
            XmlHppExternalOBVGenerator.generateHpp(definition, buffer,
                                                   "module", name, genPackage);
            //String contents = buffer.toString();
            String fileName;

            String OBVheaderDir;
            if (genPackage != "") { // m???dulo dentro de otro m???dulo
                OBVheaderDir = Xml2Cpp.getDir("OBV_" + genPackage,
                                               headerDirectory, true);
                fileName = definition.getAttribute(OMG_name) + h_ext;
            } else { // m???dulo en ???mbito global
                OBVheaderDir = headerDir;
                fileName = "OBV_" + definition.getAttribute(OMG_name) + h_ext;
            }
            String idl_fn = XmlUtil.getIdlFileName(definition);

            fm.addFile(buffer, fileName, OBVheaderDir, idl_fn, FileManager.TYPE_OBV_HEADER);

            //FileWriter writer = new FileWriter(OBVheaderDir
            //                                   + File.separatorChar + fileName);
            //BufferedWriter buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        }
        /** ******** end of OBV File declarator. ************** */

    }

}