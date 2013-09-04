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

package es.tid.TIDIdlc.xml2cpp;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.util.XmlUtil;

import java.io.*;

import org.w3c.dom.*;

/**
 * Generates Cpp for exception declarations.
 */
class XmlException2Cpp
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
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        // Este método es el que genera las sequences
        // anónimas, es decir, miembros
        // sequence dentro del struct que se definen en el propio struct
        preprocessException(doc, sourceDirectory, headerDirectory,
                            nameWithPackage, generateCode, expanded, h_ext, c_ext);

        //FileWriter writer;
        //BufferedWriter buf_writer;
        String fileName;
        String idl_fn = XmlUtil.getIdlFileName(doc);
        
        // Exception generation
        fileName = name + c_ext;
        if (generateCode) {
            Traces.println("XmlException2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        if (generateCode) {
            StringBuffer contents = generateCppExceptionDef(doc, genPackage);
        	fm.addFile(contents, fileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
            //writer = new FileWriter(sourceDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        }

        fileName = name + h_ext;
        if (generateCode) {
            Traces.println("XmlException2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        if (generateCode) {
            StringBuffer contents = generateHppExceptionDef(doc, genPackage);
            fm.addFile(contents, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        }
        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(doc, buffer,
                                                      OMG_exception, name,
                                                      genPackage);
        //contents = buffer.toString();
        if (generateCode) {
            fileName = name + "_ext" + h_ext;
            fm.addFile(buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        }
        generateCppSubPackageDef(doc, sourceDirectory, headerDirectory, name,
                                 genPackage, generateCode, expanded, h_ext, c_ext);
    }

    private StringBuffer generateCppExceptionDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        // Package header
        XmlCppHeaderGenerator.generate(buffer, "exception", name, genPackage);
        //String
        // helperClass=TypedefManager.getInstance().getUnrolledHelperType(nameWithPackage);
        String helperClass = XmlType2Cpp.getHelperName(nameWithPackage);

        // ExceptionHolder generation
        // Moved to header file.
        // buffer.append(XmlCppHolderGenerator.generate(genPackage,name,holderClass));

        // ExceptionHelper generation
        buffer.append(generateCppHelperDef(doc, genPackage));

        // constructor por defecto

        StringBuffer forEmptyConst = new StringBuffer();
        StringBuffer forCopyConst = new StringBuffer();

        NodeList nodes = doc.getChildNodes();
        String objectName = null, initVal = null, copyVal = null;
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_simple_declarator)) {
                objectName = el.getAttribute(OMG_name);
            } else {
                initVal = null;
                copyVal = null;
                if (XmlType2Cpp.isAString(el)) {
                    initVal = XmlType2Cpp.getDefaultConstructor(el); // string
                                                                     // vacio
                    copyVal = "CORBA::string_dup";
                } else if (XmlType2Cpp.isAWString(el)) {
                    initVal = XmlType2Cpp.getDefaultConstructor(el); // wstring
                                                                     // vacio
                    copyVal = "CORBA::wstring_dup";
                }
                //else if(XmlType2Cpp.isAnInterface(el)) 
                //initVal = XmlType2Cpp.getDefaultConstructor(el);
                objectName = null;
            }
            if (objectName != null) {
                if (copyVal != null)
                    forCopyConst.append("\t" + objectName + " = " + copyVal
                                        + "(_s." + objectName + ");\n");
                else
                    forCopyConst.append("\t" + objectName + " = _s."
                                        + objectName + ";\n");
                if (initVal != null)
                    forEmptyConst.append("\t" + objectName + " = " + initVal
                                         + ";\n");
            }
        }

        //        	if (forEmptyConst.length()>0) {
        buffer.append("/* Empty constructor */\n\n");
        buffer.append(nameWithPackage + "::" + name + "()\n{\n");
        buffer.append(forEmptyConst.toString());
        buffer.append("}\n\n");
        buffer.append("/* Copy constructor */\n\n");
        buffer.append(nameWithPackage + "::" + name + "(const "
                      + nameWithPackage + "& _s)\n{\n");
        buffer.append(forCopyConst.toString());
        buffer.append("}\n\n");
        //        	}

        buffer.append("void " + nameWithPackage + "::_raise() const\n{\n");
        buffer.append("\tthrow " + nameWithPackage + "(*this);\n}\n");
        buffer.append("const char* " + nameWithPackage
                      + "::_name() const \n{\n");
        buffer.append("\treturn \"");
        buffer.append(name + "\";\n}\n");
        buffer.append("const char* " + nameWithPackage
                      + "::_rep_id() const \n{\n");
        buffer.append("\treturn ");
        buffer.append(helperClass);
        buffer.append("::id();\n}\n");
        buffer.append(nameWithPackage + "* " + nameWithPackage
                      + "::_downcast(CORBA::Exception* e)\n{\n");
        buffer.append("\treturn dynamic_cast<" + nameWithPackage + "*>(e);\n");
        buffer.append("}\n");
        buffer.append("const " + nameWithPackage + "* " + nameWithPackage
                      + "::_downcast(const CORBA::Exception* e)\n{\n");
        buffer.append("\treturn dynamic_cast<" + nameWithPackage
                      + "*>(const_cast<Exception*>(e));\n");
        buffer.append("}\n");
        // Moved to External Operations
        // XmlCppFooterGenerator.generate(doc,buffer,"Struct",name,genPackage);
        return buffer;
    }

    // Copy & paste de XmlStruct2Cpp
    private String generateCppHelperDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();

        // Header
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        // Class header
        buffer.append(XmlCppHelperGenerator.generateCpp(doc, null, genPackage,false));

        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        String contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                            holderClass);
        buffer.append(contents);

        return buffer.toString();

    }


    private StringBuffer generateHppExceptionDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage + "::" + name;
        // Package header
        XmlHppHeaderGenerator.generate(doc, buffer, "exception", name,
                                       genPackage);
        // _tc_ Type Code Generation.
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        // Class header
        buffer.append("class ");
        buffer.append(name);
        buffer.append(": public virtual CORBA::UserException {\n\n");
        buffer.append("\t\tpublic:\n");
        buffer.append("\t\ttypedef ");
        buffer.append(name);
        buffer.append("_var  ");
        //buffer.append(name); 
        buffer.append("_var_type;");
        buffer.append("\n\n");
        XmlHppHeaderGenerator.includeForwardDeclarations(doc, buffer,
                                                         "exception", name,
                                                         genPackage);
        XmlHppHeaderGenerator.includeChildrenHeaderFiles(doc, buffer,
                                                         "exception", name,
                                                         genPackage);
        // Items definition
        buffer.append("\n");
        NodeList nodes = doc.getChildNodes();
        String objectName = null, classType = null;
        // Memeber definitions.
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_simple_declarator)) {
                objectName = el.getAttribute(OMG_name);
            } else if (tag.equals(OMG_array)) {
                objectName = el.getAttribute(OMG_name);
                for (int k = 0; k < el.getChildNodes().getLength(); k++) {
                    objectName += "*";
                }
            } else {
                if (((Element) nodes.item(i)).getTagName().equals(OMG_type))
                    classType = XmlType2Cpp.getType(el);
                else if (tag.equals(OMG_scoped_name)) {
                    /*
                     * if(XmlType2Cpp.isABasicType(el)) 
                     * classType=el.getAttribute(OMG_name); else
                     */
                    if (XmlType2Cpp.isAString(el))
                        classType = "CORBA::String_var";
                    else if (XmlType2Cpp.isAWString(el))
                        classType = "CORBA::WString_var";
                    else if (XmlType2Cpp.isAnInterface(el)
                             || XmlType2Cpp
                                 .getDefinitionType(el).equals(OMG_array)
                             || XmlType2Cpp
                                 .getDefinitionType(el).equals(OMG_valuetype)) // DAVV
                        classType = XmlType2Cpp.getType(el) + "_var";
                    else
                        classType = XmlType2Cpp.getType(el);
                    /*
                     * if(XmlType2Cpp.isAString(el)) DAVV classType="string";
                     * else
                     * classType=XmlType2Cpp.getPointerType(el.getAttribute(OMG_name
                     * ));
                     */
                }

                else
                    classType = XmlType2Cpp.getType(el);

                /*
                 * else classType =
                 * XmlType2Cpp.getPointerType(XmlType2Cpp.getType(el));
                 * 
                 * if(classType.equals("char *")) classType="string";
                 * if(classType.equals("wchar *")) classType="string";
                 */

                if (classType.equals("char*"))
                    classType = "CORBA::String_var"; // DAVV
                if (classType.equals("CORBA::WChar*"))
                    classType = "CORBA::WString_var";
                if (classType.equals("CORBA::Object_ptr"))
                    classType = "CORBA::Object_var";
                objectName = null;
            }

            if (objectName != null)
                buffer.append("\t\t" + classType + " " + objectName + ";\n");
        }
        buffer.append("\n\t\tpublic:\n");
        // Constructors
        // Empty constructor
        buffer.append("\t\t" + name + "();\n");
        /*
         * else buffer.append("\t\t" + name + "() {}\n");
         */
        // Constructor with parameters
        if (nodes.getLength() > 0) {
            StringBuffer assignationBuffer = new StringBuffer();
            buffer.append("\t\t" + name + "(");
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String tag = el.getTagName();
                if (tag.equals(OMG_simple_declarator)) {
                    objectName = el.getAttribute(OMG_name);
                } else if (tag.equals(OMG_array)) {
                    objectName = el.getAttribute(OMG_name);
                    for (int k = 0; k < el.getChildNodes().getLength(); k++) {
                        objectName += "*";
                    }
                } else {
                    if (((Element) nodes.item(i)).getTagName().equals(OMG_type))
                        classType = XmlType2Cpp.getType(el);
                    else if (tag.equals(OMG_scoped_name)) {
                        if (XmlType2Cpp.isAString(el))
                            classType = "CORBA::String_var";
                        else if (XmlType2Cpp.isAWString(el))
                            classType = "CORBA::WString_var";
                        //classType="string";
                        else if (XmlType2Cpp.isAnInterface(el)
                                 || XmlType2Cpp.getDefinitionType(el).equals(OMG_array)
                                 || XmlType2Cpp.getDefinitionType(el).equals(OMG_valuetype)) 
                            classType = XmlType2Cpp.getType(el) + "_var";
                        else
                            classType = XmlType2Cpp.getType(el);
                        //classType=XmlType2Cpp.getPointerType(el.getAttribute(OMG_name
                        // ));
                    } else
                        classType = XmlType2Cpp.getType(el);
                    //classType =
                    // XmlType2Cpp.getPointerType(XmlType2Cpp.getType(el));

                    if (classType.equals("char*"))
                        classType = "CORBA::String_var";
                    if (classType.equals("CORBA::WChar*"))
                        classType = "CORBA::WString_var";
                    if (classType.equals("CORBA::Object_ptr"))
                        classType = "CORBA::Object_var";
                    objectName = null;
                }
                if (objectName != null) {
                    if (i > 1) {
                        buffer.append(", ");
                        assignationBuffer.append(", ");
                    }
                    buffer.append(classType + " p" + objectName);
                    assignationBuffer.append(objectName + "(p" + objectName
                                             + ")"); // DAVV
                }
            }
            buffer.append(") : " + assignationBuffer.toString() + "{}\n"); // DAVV
        }

        // Constructor de copia
        buffer.append("\t\t" + name + "(const " + name + "&);\n");

        buffer.append("\n");
        buffer.append("\t\tvoid _raise() const;\n");
        buffer.append("\t\tconst char* _name() const;\n");
        buffer.append("\t\tconst char* _rep_id() const;\n");
        buffer.append("\n");
        buffer.append("\t\tstatic " + name
                      + "* _downcast(CORBA::Exception* e);\n");
        buffer.append("\t\tstatic const " + name
                      + "* _downcast(const CORBA::Exception* e);\n");

        buffer.append("};// End of Exception Class \n");
        // ExceptionHelper generation
        buffer.append(XmlCppHelperGenerator.generateHpp(doc, null, genPackage,false));

        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        buffer.append(XmlCppHolderGenerator.generateHpp(OMG_Holder_Complex,
                                                        genPackage,
                                                        nameWithPackage,
                                                        holderClass));

        XmlHppHeaderGenerator.generateFoot(buffer, "exception", name,
                                           genPackage);

        return buffer;
    }

    private void generateCppSubPackageDef(Element doc, String sourceDirectory,
                                          String headerDirectory,
                                          String interfaceName,
                                          String genPackage, boolean generate,
										  boolean expanded, 
										  String h_ext, 
										  String c_ext)
        throws Exception
    {

        String newPackage;

        if (!genPackage.equals("")) {
            newPackage = genPackage + "::" + interfaceName + ""; //MACP
                                                                 // "Package";
        } else {
            newPackage = interfaceName + ""; // MACP"Package";
        }

        // Items definition
        NodeList nodes = doc.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();

            //*
            // * if (tag.equals(OMG_const_dcl)) {
            // * 
            // * XmlConst2Cpp gen = new XmlConst2Cpp(); gen.generateCpp(el,
            // * sourceDirectory, headerDirectory, newPackage, generate); } else
            // */
            if (tag.equals(OMG_enum)) {

                XmlEnum2Cpp gen = new XmlEnum2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_struct)) {

                XmlStruct2Cpp gen = new XmlStruct2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, generate, expanded, h_ext, c_ext);
            } else if (tag.equals(OMG_union)) {

                XmlUnion2Cpp gen = new XmlUnion2Cpp();
                gen.generateCpp(el, sourceDirectory, headerDirectory,
                                newPackage, generate, expanded, h_ext, c_ext);
            }
        } // end of loop for.
    }

    /**
     * Preprocesado de la definicion de exception a la busqueda de sequences
     * para generar los ficheros correspondientes a la secuencia y sustitucion
     * de la secuencia por el tipo definido.
     */

    private void preprocessException(Element doc, String sourceDirectory,
                                     String headerDirectory, String genPackage,
                                     boolean createDir, boolean expanded, 
									 String h_ext, 
									 String c_ext)
        throws Exception
    {
        // Doc continene la definicion de la estructura.
        NodeList nl = doc.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getTagName().equals(OMG_sequence)) {
                Element newDecl = generateSequenceImplementation(
                                      el, sourceDirectory, headerDirectory,
                                      genPackage, createDir, expanded, h_ext, c_ext);
                String name = 
                    ((Element) newDecl.getLastChild()).getAttribute(OMG_name);
                Element sup = 
                    doc.getOwnerDocument().createElement(OMG_scoped_name);
                sup.setAttribute(OMG_name, genPackage + "::" + name);
                sup.setAttribute(OMG_variable_size_type, "true");
                doc.replaceChild(sup, el);
                doc.appendChild(newDecl);

            }
        }
    }

    private Element generateSequenceImplementation(Element doc,
                                                   String sourceDirectory,
                                                   String headerDirectory,
                                                   String genPackage,
                                                   boolean createDir,
												   boolean expanded, 
												   String h_ext, 
												   String c_ext)
        throws Exception
    {
        // El objetivo de este metodo es desarrollar un subArbol XML para el
        // nodo Sequence que desarolle
        // XmlTypedef2Cpp y generar el nombre para esta secuencia.
        // Esto es doc,
        //<sequence>
        //      <type kind="string" VL_Type="true"/>
        //</sequence>
        //----
        //<simple name="Viar" line="4" column="21" scopedName="::A::L::Viar"
        // VL_Type="false"/>
        // Y esto de arriba lo que viene despues que utilizara el metodo que sea
        // para generar el nombre del tipo.
        //
        // Lo que espera un TYPEDEF para Sequence es
        //<typedef VL_Type="true">
        //   <sequence>
        //      <type kind="string" VL_Type="true"/>
        //   </sequence>
        //   <simple name="aR" line="2" column="29" scoped_name="::A::aR"
        // VL_Type="true" scopedName="::A::aR"/>
        //</typedef>
        Element decl = (Element) doc.cloneNode(true);
        Element el = doc.getOwnerDocument().createElement(OMG_typedef);
        doc.appendChild(el);
        el.setAttribute(OMG_variable_size_type, "true");// PORQUE ES UNA
                                                        // SECUENCIA

        el.appendChild(decl);

        Element def = (Element) doc.getNextSibling(); // Sacamos el nombre del
                                                      // attributo
        String name = def.getAttribute(OMG_name); // para convertirlo en el
                                                  // nombre del tipo
        //name+="_Internal"; // que se define con la coletilla internal. //

        name = "_" + name + "_seq";
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 
        Element internalDefinition = (Element) def.cloneNode(true);
        internalDefinition.setAttribute(OMG_name, name);
        internalDefinition.setAttribute(OMG_scoped_name, genPackage + "::"
                                                         + name);
        el.appendChild(internalDefinition);
        XmlTypedef2Cpp gen = new XmlTypedef2Cpp();
        gen.generateCpp(el, sourceDirectory, headerDirectory, genPackage,
                        createDir, expanded, h_ext, c_ext);
        //def.setAttribute(OMG_name,name);

        return el;
    }

}