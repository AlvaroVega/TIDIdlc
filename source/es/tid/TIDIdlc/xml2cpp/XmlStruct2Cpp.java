/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 242 $
* Date: $Date: 2008-03-03 15:29:05 +0100 (Mon, 03 Mar 2008) $
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

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.util.XmlUtil;
import es.tid.TIDIdlc.xmlsemantics.SemanticException;

/**
 * Generates Cpp for structure declarations.
 */
class XmlStruct2Cpp
    implements Idl2XmlNames
{
	
	StringBuffer cabConstructor = null;

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

        // Forward declaration
        if (doc.getAttribute(OMG_fwd).equals(OMG_true))
            return;

        // Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           generateCode);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                           generateCode);
        // Types generation.
        // This warranties that the name of the holder and helper don't crash
        // with IDL types.
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); 

        // Este m�todo es el que genera las sequences
        // an�nimas, es decir, miembros
        // sequence dentro del struct que se definen en el propio struct
        preprocessStruct(doc, sourceDirectory, headerDirectory,
                         nameWithPackage, generateCode, expanded, h_ext, c_ext);

        //FileWriter writer;
        //BufferedWriter buf_writer;
        
        // Gets the FileManager
        FileManager fm = FileManager.getInstance();
        StringBuffer final_buffer;
        
        String fileName, contents;
        // Struct generation SOURCE
        // Struct generation HEADER
        fileName = name + h_ext;
        if (generateCode) {
            Traces.println("XmlStruct2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }    
        String idl_fn = XmlUtil.getIdlFileName(doc);

        if (generateCode) {
        	final_buffer = generateHppStructDef(doc, genPackage);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        	
        	fm.addFile(final_buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
        }

        fileName = name + c_ext;
        if (generateCode) {
            Traces.println("XmlStruct2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        
        if (generateCode) {
        	final_buffer = generateCppStructDef(doc, genPackage);
            //writer = new FileWriter(sourceDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        	
        	fm.addFile(final_buffer, fileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
        }

        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(doc, buffer, OMG_struct,
                                                      name, genPackage);
        //contents = buffer.toString();
        if (generateCode) {
            fileName = name + "_ext" + h_ext;
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();

            fm.addFile(buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER_EXT);
        }

        generateCppSubPackageDef(doc, sourceDirectory, headerDirectory, name,
                                 genPackage, generateCode, expanded, h_ext, c_ext);
    }

    private StringBuffer generateCppStructDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        // Package header
        XmlCppHeaderGenerator.generate(buffer, "struct", name, genPackage);

        // StructHelper generation SOURCE
        buffer.append(generateCppHelperDef(doc, genPackage));

        // constructor por defecto

        Vector arrayParams = new Vector();
        StringBuffer forEmptyConst = new StringBuffer();
        StringBuffer forCopyConst = new StringBuffer();
        StringBuffer forInitConst = new StringBuffer();
        //PRA
        StringBuffer initializerList = new StringBuffer();
        //EPRA
        
        NodeList arrays=null;
        NodeList nodes = doc.getChildNodes();
        String objectName = null, initVal = null, copyVal = null;

        //PRA
        HashMap enumerado = new HashMap();
        Integer enum_index = new Integer(0);
        //ArrayList objectNameEnum = new ArrayList();
        //ArrayList enumerado = new ArrayList();
        //ArrayList objectNameEnumFirstElement = new ArrayList();
        //EPRA

        String arrayName=""; 
        //int j = 0;
        //enumerado.put(j, Boolean.FALSE);
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_simple_declarator)) {
            	String[] enumNames = (String[]) enumerado.get(enum_index);
            	if (enumNames != null /*((Boolean) enumerado.get(j)).booleanValue()*/){
            		//objectNameEnum.add(index, el.getAttribute(OMG_name));
            		int index = enum_index.intValue();
            		enum_index = new Integer(index + 1);
            		enumNames[0] = el.getAttribute(OMG_name);
            	}
                objectName = el.getAttribute(OMG_name);
            } else {
            	if (tag.equals(OMG_scoped_name)){
            		if (XmlType2Cpp.getDefinitionType(el).equals(OMG_enum)){
            			//enumerado.add(j,new Boolean (true));
            			
            			Element root = el.getOwnerDocument().getDocumentElement();
            			
            			NodeList enums = root.getElementsByTagName(OMG_enum);
        
            			for (int k = 0; k < enums.getLength(); k++){
            				Element en = (Element) enums.item(k);
            				if (en.getAttribute(OMG_scoped_name).equals(el.getAttribute(OMG_name))){
            					 NodeList subchild = en.getElementsByTagName(OMG_enumerator);
            					 Element child = (Element) subchild.item(0);
            					 //BUG #45------------------------------------
            					 String fatherModule = en.getAttribute(OMG_scoped_name);
            					 String[] module = fatherModule.split("::");
            					 String fatherGenPackage = "::" + module[1];
            					 String elem = fatherGenPackage + "::" + child.getAttribute(OMG_name);
            					 //PRA
            					 String enumNames[] = { null, elem }; 
            					 enumerado.put(enum_index, enumNames);
            					 //objectNameEnumFirstElement.add (enum_index.intValue(), elem/*child.getAttribute(OMG_name)*/);
            					 //EPRA
            					 //BUG #45------------------------------------
            				}           			
            			} // for
            			
            			// FIX to but # 195: Struct constructor doesn't initializes typedef enum member
            			String[] my_enumNames = (String[]) enumerado.get(enum_index);
                    	if (my_enumNames == null){          			
        					String elem = el.getAttribute(OMG_name) + "(0)";
        					String enumNames[] = { null, elem }; 
       					 	enumerado.put(enum_index, enumNames);
        				}
            			
            		}
            		else {
	        			if (XmlType2Cpp.getDefinitionType(el).equals(OMG_array)){
	        			    
	        			    arrayName = XmlType2Cpp.getTypeTypedef(el);
	        			    arrayParams.add(arrayName);
	        			    
	        			    // caceres: esto solo accede al nombre del tipo
	        				//  buscamos si hay arrays
	        	        	/*Element root = el.getOwnerDocument().getDocumentElement();
	        	        	arrays = root.getElementsByTagName(OMG_array);        	        	        	
	        	        	String sc_name = null;
	        	        	String type_name = null;
	        	        	type_name = el.getAttribute(OMG_name);
	        	        	String sc_scoped_name = 
	        	        	
	        	        	for (int r=0;r<arrays.getLength();r++){
	        	        		Element elem = (Element) arrays.item(r);
	        	        		sc_name =elem.getAttribute(OMG_scoped_name);
	        	        		
	        	        		
	        	        		
	        	        		if (sc_name.equals(type_name)){
	        	        			arrayName = elem.getAttribute(OMG_scoped_name);
	        	        			arrayParams.add(arrayName);
	        	        			break;
	        	        		}
	        	        	}
	        	        	*/
	        			}	
	        		}	
            	}
            	
            	
            	
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
                } else 	if (XmlType2Cpp.getDefinitionType(el).equals(OMG_valuetype)){
                    String obv_name = el.getAttribute(OMG_name);
                    initVal = "new OBV_" + obv_name.substring(2) + "()";
                    // copyVal 
                }

                objectName = null;
            }
            if ((objectName != null)) {
                if (copyVal != null){
                    forCopyConst.append("\t" + objectName + " = " + copyVal
                                        + "(_s." + objectName + ");\n");
                    forInitConst.append("\t" + objectName + " = " + copyVal 
                                        + "(p" + objectName + ");\n");
                }
                else{
                	if (arrayName != "") {
                		forCopyConst.append("\t" + arrayName + "_copy(" + objectName + "," + "_s." + objectName + ");\n");
                		forInitConst.append("\t" + arrayName + "_copy(" + objectName + "," + "p" + objectName + ");\n");
                        arrayName="";
                        
                	}
                	else {
                		forCopyConst.append("\t" + objectName + " = _s."
                				+ objectName + ";\n");                        
                        //PRA
                		initializerList.append((initializerList.length()==0) ? "" : ", ");
                		initializerList.append(objectName + "(p" + objectName + ")");
                        //EPRA

                	}
                }
                if (initVal != null)
                    forEmptyConst.append("\t" + objectName + " = " + initVal
                                         + ";\n");
            }
          
        }

        //if (forEmptyConst.length()>0) {
        buffer.append("/* Empty constructor */\n\n");
        buffer.append(nameWithPackage + "::" + name + "()");

        Iterator it = enumerado.values().iterator();
        String sep = " : ";
        while (it.hasNext()) { 
        	String[] enumNames = (String[]) it.next();
        	buffer.append(sep + enumNames[0] + "(" + enumNames[1] + ")");
        	sep = ", ";
        }

		//PRA
        //int z = 0;
        //while (((Boolean) enumerado.get(z)).booleanValue()){
        //	if (z == 0){
        //		buffer.append(" : " + objectNameEnum.get(z) + " (" + objectNameEnumFirstElement.get(z) +")");
        //	} else {
        //		buffer.append(", " + objectNameEnum.get(z) + " (" + objectNameEnumFirstElement.get(z) +")");
        //	}
        //	z++;
        //}
		//EPRA

        buffer.append( "\n{\n");
        buffer.append(forEmptyConst.toString());
        buffer.append("}\n\n");
        buffer.append("/* Copy constructor */\n\n");
        buffer.append(nameWithPackage + "::" + name + "(const "
                      + nameWithPackage + "& _s)\n{\n");
        buffer.append(forCopyConst.toString());
        buffer.append("}\n\n");
        //}
        

/*
PRA
        StringTokenizer tokenizer = new StringTokenizer(cabConstructor.toString(), "\t\n\r\f :,();", true);
        String token=null;
        StringBuffer initilizationList = new StringBuffer();
        int firstColom = -1; 
        String noArrayName = null;
        int initializerNumber = 0;
        StringBuffer copyConstructorBody = new StringBuffer();
        
        for (;tokenizer.hasMoreElements();token = tokenizer.nextToken()) {
        	
        	if (token!=null&&token.equals(":")) {
        		if (firstColom == -1) {
        			firstColom = 0;
        			continue;
        		}
        		else {
	        		if (firstColom == 0) 
	        		{
						firstColom = 1;
						continue;
	        		}
        		}      		
        	}
        	
        	if (firstColom==1)	{
        		if (!arrayParams.contains(token))  {
        			      			
        			while (tokenizer.hasMoreElements()) {
        				noArrayName = tokenizer.nextToken();
        				
        				if (noArrayName.equals(":")) {
        					break;
        				}
        					
        				if (noArrayName.length() > 1)
        					break;
        			}
        			
        			if (noArrayName.equals(":")) {
        				firstColom=0;
        				continue;
        			}
        			
        			initializerNumber++;
        			
        			if (initializerNumber > 1)
        				initilizationList.append(", ");
        			
        			initilizationList.append(noArrayName.subSequence(1, noArrayName.length()) + "(" + noArrayName + ") ");
        			
        		}
        		else 
        		{
        			while (tokenizer.hasMoreElements()) {
        				noArrayName = tokenizer.nextToken();
        				if (noArrayName.length() > 1)
        					break;
        			}
        			
        			copyConstructorBody.append("\t" + token + "_copy(" + noArrayName.substring(1, noArrayName.length())  + ", " + noArrayName + ");\n");
        			

        		    //if (initializerNumber > 1)
        			//	initilizationList.append(", ");
        			
        		}
        		
        		firstColom=-1;
        	}
        	
        	noArrayName = token;
        }

EPRA
*/        

        
        //PRA
        buffer.append("/* Member constructor */\n\n");
        //EPRA
        
        cabConstructor.insert(0,nameWithPackage + "::" + name + "(");
        buffer.append(cabConstructor.toString());
        
        if (initializerList.length()>1)	buffer.append(" : ");
    	buffer.append(initializerList.toString() + "\n{\n" + forInitConst.toString() + "}\n");

        
/*
PRA
        if (copyConstructorBody.length()!=0){
        	buffer.append(initializerList.toString() + "\n{\n" + copyConstructorBody.toString() + "}\n");
        } else {
        	buffer.append(initializerList.toString() + "\n{\n" + forInitConst.toString() + "}\n");
        }
EPRA
*/

        
        // Moved to External Operations
        // XmlCppFooterGenerator.generate(doc,buffer,"Struct",name,genPackage);
        return buffer;
    }

    private String generateCppHelperDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();

        // Header
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);

        buffer.append(XmlCppHelperGenerator.generateCpp(doc, null, genPackage,false));

        String contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                            holderClass);
        buffer.append(contents);

        return buffer.toString();
    }


    private StringBuffer generateHppStructDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage + "::" + name;
        //String
        // helperClass=TypedefManager.getInstance().getUnrolledHelperType(nameWithPackage);
        //String
        // holderClass=TypedefManager.getInstance().getUnrolledHolderType(nameWithPackage);
        // - Cualquier typedef del IDL tiene asociado un typedef en C++;
        // por eso en C++ no
        // es necesario desenrollar nada como pasa en Java.
        // TypedefManager no tiene sentido.
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        // Package header
        XmlHppHeaderGenerator.generate(doc, buffer, "struct", name, genPackage);
        // _tc_ Type Code Generation.
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        // Items definition
        buffer.append("class  ");
        buffer.append(name);
        buffer.append("\n{\n\n");
        buffer.append("\tpublic:\n");
        buffer.append("\t\ttypedef ");
        buffer.append(name);
        buffer.append("_var  ");
        //buffer.append(name); - DAVV: sobra
        buffer.append("_var_type;");
        buffer.append("\n\n");
        XmlHppHeaderGenerator.includeForwardDeclarations(doc, buffer, "struct",
                                                         name, genPackage);
        XmlHppHeaderGenerator.includeChildrenHeaderFiles(doc, buffer, "struct",
                                                         name, genPackage);
        buffer.append("\n");
        buffer.append("\t\t// Member Attributes.   \n\n");

        NodeList nodes = doc.getChildNodes();
        String objectName = null, classType = null;
        for (int i = 0; i < nodes.getLength(); i++) { // Este codigo va
                                                      // parejo al de
                                                      // XmlException2Cpp.java
            // ClassType is assigned in odd loops
            // objetName is assignes in pair loops.

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_simple_declarator)) {
                objectName = el.getAttribute(OMG_name);
            } else if (tag.equals(OMG_array)) {
                throw new SemanticException(
                                            "Anonymous array members are not supported yet");
                /*
                 * objectName = el.getAttribute(OMG_name); for (int k=0; k
                 * <el.getChildNodes().getLength(); k++) { objectName += "*"; }
                 */
            } else {                
                classType = XmlType2Cpp.getMemberType(el);              
                objectName = null;                
            }
            
            if (objectName != null)
                buffer.append("\t\t" + classType + " " + objectName + ";\n");
        }

        // Constructors
        buffer.append("\n\t\t// Member Functions.   \n\n");
        buffer.append("\t\t" + name + "(); /*Empty Constructor */ \n\n");
        buffer.append("\t\t" + name + "(const " + name + "&);\n\n"); // constructor
                                                                     // de copia
        //buffer.append(name);
        //buffer.append("& source); /*Copy Constructor*/ \n\n");
        //buffer.append("\t\t~" + name + "();/* Destructor */\n\n"); 
        // destructor innecesario
        //buffer.append("\t\t" + name + "& operator=( const "); '='
        // sobrecargado innecesario
        //buffer.append(name + "& s ); /* Assignment Operator */ \n\n");
        StringBuffer forListOfInit = new StringBuffer();
        buffer.append("\t\t" + name + "(");
        
        // 
        cabConstructor= new StringBuffer();
        StringBuffer initArrays = new StringBuffer();
        
        

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
                classType = XmlType2Cpp.getParamType(el,"in");
                objectName = null;
            }            
            
            if (objectName != null) {
                if (i > 1 ) {
                    buffer.append(", ");
                    cabConstructor.append(", ");
                    forListOfInit.append(", ");
                }
                
                buffer.append(classType + " p" + objectName); 
                cabConstructor.append(classType + " p" + objectName); 
                forListOfInit.append(objectName + "(p" + objectName + ")");
            }
        }
        
      buffer.append(");\n");
      cabConstructor.append(")");

        buffer.append("}; // End of class for struct ");
        buffer.append(name);
        buffer.append("\n\n");

        // StructHelper generation HEADER
        //buffer.append(generateHppHelperDef(doc,genPackage));
        buffer.append(XmlCppHelperGenerator.generateHpp(doc, null, genPackage, false));
        // StructHolder generation HEADER
        //if(doc.getAttribute(OMG_variable_size_type).equals("true"))
        //{
        buffer.append(XmlCppHolderGenerator.generateHpp(OMG_Holder_Complex,
                                                        genPackage,
                                                        nameWithPackage,
                                                        holderClass));
        //}
        //else buffer.append(
        // XmlHppHolderGenerator.generate(OMG_Holder_Simple,genPackage,nameWithPackage,holderClass));

        XmlHppHeaderGenerator.generateFoot(buffer, "struct", name, genPackage);
        return buffer;
    }

    private void generateCppSubPackageDef(Element doc, String sourceDirectory,
                                          String headerDirectory,
                                          String interfaceName,
                                          String genPackage, boolean generate,
										  boolean expanded, String h_ext, String c_ext)
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

            /*
             * if (tag.equals(OMG_const_dcl)) 
             * 
             * XmlConst2Cpp gen = new XmlConst2Cpp(); gen.generateCpp(el,
             * sourceDirectory, headerDirectory, newPackage, generate); } else
             */
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
        name = "_" + name + "_seq";
        //Xml2Cpp.generateForwardUtilSimbols(genPackage,name); // DAVV - RELALO
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

    /**
     * Preprocesado de la definicion de struct a la busqueda de sequencia para
     * generar los ficheros correspondientes a la secuencia y sustitucion de la
     * secuencia por el tipo definido.
     */

    private void preprocessStruct(Element doc, String sourceDirectory,
                                  String headerDirectory, String genPackage,
                                  boolean createDir,
								  boolean expanded, 
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
}
