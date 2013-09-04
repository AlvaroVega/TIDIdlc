/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 282 $
* Date: $Date: 2008-09-15 16:27:08 +0200 (Mon, 15 Sep 2008) $
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

//import java.io.BufferedWriter;
import java.io.File;
//import java.io.FileWriter;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.util.XmlUtil;
import es.tid.TIDIdlc.xmlsemantics.Scope;
import es.tid.TIDIdlc.xmlsemantics.SemanticException;
import es.tid.TIDIdlc.xmlsemantics.Union;
import es.tid.TIDIdlc.xmlsemantics.UnionCase;
import es.tid.TIDIdlc.xmlsemantics.UnionManager;

/**
 * Generates Cpp for union declarations.
 */
class XmlUnion2Cpp
    implements Idl2XmlNames
{

    private boolean m_generate;

    /** Generate Cpp */
    public void generateCpp(Element doc, String sourceDirectory,
                            String headerDirectory, String genPackage,
                            boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

        // Forward declaration
        if (doc.getAttribute(OMG_fwd).equals(OMG_true))
            return;

        m_generate = generateCode;

        // Get package components
        String headerDir = Xml2Cpp.getDir(genPackage, headerDirectory,
                                           generateCode);
        String sourceDir = Xml2Cpp.getDir(genPackage, sourceDirectory,
                                           generateCode);

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
        String fileName;
        //String contents;
        // Union generation Header
//      Gets the FileManager
        FileManager fm = FileManager.getInstance();
        StringBuffer final_buffer;

        fileName = doc.getAttribute(OMG_name) + h_ext;
        if (generateCode) {
            Traces.println("XmlUnion2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + headerDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        String idl_fn = XmlUtil.getIdlFileName(doc);

        
        if (generateCode) {
        	final_buffer = generateHppUnionDef(doc, genPackage);
            //writer = new FileWriter(headerDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();
        	
        	fm.addFile(final_buffer, fileName, headerDir, idl_fn, FileManager.TYPE_MAIN_HEADER);
        }
        // Union generation Source
        fileName = doc.getAttribute(OMG_name) + c_ext;
        if (generateCode) {
            Traces.println("XmlUnion2Cpp:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + sourceDir + File.separatorChar
                           + fileName + "...", Traces.USER);
        }

        if (generateCode) {
            final_buffer = generateCppUnionDef(doc, sourceDirectory, headerDirectory,
                    						   genPackage, expanded, h_ext, c_ext);
            //writer = new FileWriter(sourceDir + File.separatorChar + fileName);
            //buf_writer = new BufferedWriter(writer);
            //buf_writer.write(contents);
            //buf_writer.close();           
            fm.addFile(final_buffer, fileName, sourceDir, idl_fn, FileManager.TYPE_MAIN_SOURCE);
        }
        // External any operations
        // Design of the header files, Any operations outside main file.
        StringBuffer buffer = new StringBuffer();
        XmlHppExternalOperationsGenerator.generateHpp(doc, buffer, OMG_union,
                                                      name, genPackage);
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

    private StringBuffer generateCppUnionDef(Element doc, String sourceDirectory,
                                       		 String headerDirectory, String genPackage, 
											 boolean expanded, String h_ext, String c_ext)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String discriminatorType = getDiscriminatorType(doc, name, genPackage);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;

        // Package header
        XmlCppHeaderGenerator.generate(buffer, "union", name, genPackage);
        String helperCppcontents = generateCppHelperDef(doc, genPackage);
        buffer.append(helperCppcontents);
        // Class header

        /*
         * buffer.append(name); buffer.append("\n : public virtual
         * CORBA::portable::IDLEntity {\n\n"); buffer.append(" private:\n " +
         * discriminatorType + " _discriminator;\n"); buffer.append("
         * CORBA::Object_ptr _union_value;\n"); buffer.append(" bool _isDefault =
         * TRUE;\n\n");
         */
        // Items definition
        Union union = UnionManager.getInstance().get(doc);
        Vector switchBody = union.getSwitchBody();
        String objectName = null;
        for (int i = 0; i < switchBody.size(); i++) {
            UnionCase union_case = (UnionCase) switchBody.elementAt(i);
            Element type = union_case.m_type_spec;
            Element decl = union_case.m_declarator;
            String decl_tag = decl.getTagName();
            String type_tag = type.getTagName();
            if (decl_tag.equals(OMG_simple_declarator)) {
                objectName = decl.getAttribute(OMG_name);
            } else if (decl_tag.equals(OMG_array)) {
                objectName = decl.getAttribute(OMG_name) + "[]";
            }
            if (type_tag.equals(OMG_enum)) {
                // Added to support the declaration of enumerations into a Union
                // body
                //String enumName = type.getAttribute(OMG_name);
                String newPackage;
                if (!genPackage.equals("")) {
                    newPackage = genPackage + "::" + name + "::";//MACP
                                                                 // "Package";
                } else {
                    newPackage = name + "::"; //MACP "Package";
                }
                XmlEnum2Cpp gen = new XmlEnum2Cpp();
                gen.generateCpp(type, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);

            } else if (type_tag.equals(OMG_struct)) {
                // Added to support the declaration of structs into a Union body
                String newPackage;
                if (!genPackage.equals("")) {
                    newPackage = genPackage + "::" + name + "::";//MACP
                                                                 // "Package";
                } else {
                    newPackage = name + "::";// MACP "Package";
                }
                XmlStruct2Cpp gen = new XmlStruct2Cpp();
                gen.generateCpp(type, sourceDirectory, headerDirectory,
                                newPackage, this.m_generate, expanded, h_ext, c_ext);
            }

            generateCppUnionAccesorAndMutatorMethods(buffer, type, objectName,
                                                     nameWithPackage,
                                                     union_case,
                                                     discriminatorType, union);

            buffer.append("\n");
        }


        buffer.append(discriminatorType + " " + nameWithPackage
        			  + "::_d() const {\n");
        buffer.append("\treturn _discriminator;\n");
        buffer.append("}\n\n");

        buffer.append("void " + nameWithPackage + "::_d(" + discriminatorType
	                  + " nd) {\n");
        generateCppUnion_DiscMutatorMethod(buffer, union);
        buffer.append("}\n\n");

        
        //PRA
        String defaultInitializer = "";
        if (union.getDefaultAllowed() && !union.getHasDefault()) {
            buffer.append("void " + nameWithPackage + "::_default() {\n");
            generateCppUnion_DefaultMethod(buffer, discriminatorType, union, true);
            buffer.append("void " + nameWithPackage + "::_default("
                          + discriminatorType + " discriminator) {\n");
            generateCppUnion_DefaultMethod(buffer, discriminatorType, union, false);
            defaultInitializer = "\t_default();\n";
        } else {
            buffer.append("void " + nameWithPackage + "::_reset() {\n");
            generateCppUnion_ResetMethod(buffer, discriminatorType, union);
            defaultInitializer = "\t_reset();\n";
        }        
        
        
        // Default constructor

        buffer.append(nameWithPackage + "::" + name + "()\n");
        buffer.append("{\n");

        //PRA
        buffer.append(defaultInitializer);
        //EPRA

        //if (!union.getHasDefault() && union.getDefaultAllowed())
        //    buffer.append("\t_discriminator = ;\n");
        buffer.append("\t_isDefault = true;\n");
        buffer.append("}\n\n");


        // Copy constructor
        
        buffer.append(nameWithPackage + "::" + name + "(const "
                      + nameWithPackage + "& u)\n");
        buffer.append("{\n");
        buffer.append("\t_union_value = u._union_value;\n");
        buffer.append("\t_discriminator = u._discriminator;\n");
        buffer.append("\t_isDefault = u._isDefault;\n");
        buffer.append("}\n\n");

        
        // Assign operator
        
        buffer.append(nameWithPackage + "& " + nameWithPackage
                      + "::operator=(const " + nameWithPackage + "& u)\n");
        buffer.append("{\n");
        buffer.append("\t_union_value = u._union_value;\n");
        buffer.append("\t_discriminator = u._discriminator;\n");
        buffer.append("\t_isDefault = u._isDefault;\n");
        buffer.append("\treturn *this;\n");
        buffer.append("}\n\n");

/*        
        buffer.append(nameWithPackage + "& " + nameWithPackage
                      + "::clone() const\n");
        buffer.append("{\n");
        buffer.append("\treturn  *(new " + name + "(*this));\n");
        buffer.append("}\n");
*/
        
        return buffer;
    }

    private void generateCppUnionAccesorMethod(StringBuffer buffer,
                                               String classType,
                                               UnionCase union_case,
                                               String discriminatorType,
                                               Union union, 
                                               String definition,
                                               String kind)
        throws Exception
    {

        Element doc = union.getUnionElement();
        Element type = union_case.m_type_spec;
        Vector case_labels = union_case.m_case_labels;
        Element discriminator_type = 
            (Element) doc.getFirstChild().getFirstChild();
        String inverseClassType = getInverseType(type, true);

        buffer.append("\tif ");
        int case_size = case_labels.size();
        if (case_size > 1) {
            buffer.append("(");
        }
        for (int i = 0; i < case_size; i++) {
            if (i > 0)
                buffer.append(" || ");
            Element case_label = (Element) case_labels.elementAt(i);
            
            String value = getCaseValue(union, case_label);
            
            if(value != null) {
                
                buffer.append("(_discriminator == " + value + ")");
                
            }            
            
        }
        if (case_size > 1) {
            buffer.append(")");
        }
        if (union_case.m_is_default) {
            buffer.append("(_isDefault)");
        }
        buffer.append("\n\t{\n");
        //////////////////////////
        if (definition.equals(OMG_array)) { // introducido para
                                            // extraccion de arrays
            buffer.append("\t\t" + classType + "_forany _tmp;\n");
            buffer.append("\t\t" + classType + "_slice* _ret;\n");
        } else {
            if (kind != null
                && (kind.equals(OMG_string) || kind.equals(OMG_wstring))) {
                String bound; // DAVV - tratamiento de string y wstring
                if (type.hasChildNodes()) {
                    Element tmp = 
                        (Element) type.getFirstChild().getFirstChild();
                    bound = tmp.getAttribute("value");
                } else
                    bound = "0";
                buffer.append("\t\tCORBA::ULong _bound = " + bound + ";\n");
                if (kind.equals(OMG_string))
                    buffer.append("\t\tconst char * _tmp;\n");
                else
                    buffer.append("\t\tconst CORBA::WChar * _tmp;\n");

            } else {
                buffer.append("\t\t");
                if (definition.equals(OMG_struct)
                    || definition.equals(OMG_union)
                    || definition.equals(OMG_exception)
                    || definition.equals(OMG_sequence) || // incluye referentes
                    (kind != null && kind.equals(OMG_any))) {
                    if (!classType.startsWith("const"))
                        buffer.append("const ");
                    buffer.append(classType + "* _tmp;\n");
                } else if (kind != null && kind.equals(OMG_fixed)) {
                    buffer.append(classType + "* _tmp = new " + classType
                                  + "();\n");
                } else if (definition.equals(OMG_interface)) {
                    buffer.append(classType + "_ptr _tmp;\n");
                } else if (definition.equals(OMG_valuetype)) {
                    buffer.append(classType + "* _tmp;\n");
                } else
                    buffer.append(classType + " _tmp;\n");
            }

        }

        buffer.append("\t\t" + inverseClassType + "\n");
        ////////////////////////
        if (definition.equals(OMG_array)) { // DAVV - introducido para
                                            // extraccion de arrays
            buffer.append("\t\t_ret = _tmp;\n");
            buffer.append("\t\treturn _ret;\n");
        } else if (definition.equals(OMG_struct)
                   || definition.equals(OMG_union)
                   || definition.equals(OMG_exception)
                   || definition.equals(OMG_sequence)
                   || definition.equals(OMG_fixed)
                   || (kind != null && (kind.equals(OMG_any))))

            if (!classType.startsWith("const")) 
                buffer.append("\t\treturn *((" + classType + "*)_tmp);\n"); 
                // cast a 'no-const'
            
            else
                buffer.append("\t\treturn *_tmp;\n");
        else if (kind != null && kind.equals(OMG_fixed))
            buffer.append("\t\treturn *_tmp;\n");
        else if (kind != null
                 && (kind.equals(OMG_string) || kind.equals(OMG_wstring)))
            buffer.append("\t\treturn (" + classType + ")_tmp;\n");
        else
            buffer.append("\t\treturn _tmp;\n");

        buffer.append("\t}\n");
        buffer.append("\tthrow CORBA::BAD_OPERATION();\n");
        buffer.append("}\n\n"); // Cierra el metodo.

    }

    private void generateCppUnionMutatorMethod(StringBuffer buffer,
                                               String classType,
                                               UnionCase union_case,
                                               String discriminatorType,
                                               Union union, String kind,
                                               boolean hasOneCaseLabel)
        //boolean forceSimpleInsertion)
        throws Exception
    {
        Element doc = union.getUnionElement();
        Element type = union_case.m_type_spec;
        Vector case_labels = union_case.m_case_labels;
        Element discriminator_type = 
            (Element) doc.getFirstChild().getFirstChild();
        String inverseClassType = getInverseType(type, false);
        String value = "";
        String defaultValue = "";

        if (union_case.m_is_default) {
            buffer.append("\t_discriminator = ");
            if (!hasOneCaseLabel)
                value = "discriminator";
            else {
                String name;
                if (union.getScopedDiscriminator() == null)
                    name = discriminator_type.getAttribute(OMG_kind);
                else
                    name = union.getScopedDiscrimKind();

                if ((name == null) || name.equals("") || name.equals(OMG_enum)) {
                    // We have an enumeration
                    StringTokenizer token = new StringTokenizer(
                                                    discriminatorType, "::");
                    String scope = "";
                    while (token.countTokens() > 1)
                        scope += token.nextElement() + "::";
                    //value = discriminatorType + "::" +
                    // union.getDefaultValue();
                    value = scope + union.getDefaultValue();
                } else if (name.equals(OMG_char) || name.equals(OMG_wchar)) {
                    // We have a char/wchar discriminator
                    value = "'" + union.getDefaultValue() + "'";
                } else {
                    // We have a basic discriminator
                    value = union.getDefaultValue().toLowerCase();
                    if (name.equals(OMG_long) || name.equals(OMG_longlong))
                        value += "L";
                    else if(name.equals(OMG_unsignedlong)
                        || name.equals(OMG_unsignedlonglong))
                        value += "UL";
                }
            }
            buffer.append(value);
            buffer.append(";\n");
            defaultValue = "true";
        } else {
            Element case_label = (Element) case_labels.elementAt(0);
            
            value = getCaseValue(union, case_label);
            
            buffer.append("\t_discriminator = ");
            if (!hasOneCaseLabel)
                value = "discriminator";
            buffer.append(value);
            buffer.append(";\n");          
        }
        
        String definition = XmlType2Cpp.getDefinitionType(type);
        if (definition.equals(OMG_array)) {
            buffer.append("\t" + classType + "_forany _aux((" + classType
                          + "_slice*)value);\n");
            buffer.append("\t_union_value <<= _aux;\n");
            if (defaultValue.equals(""))
                defaultValue = "false";
        } else {
            //if (!forceSimpleInsertion) {
            if (kind != null
                && (kind.equals(OMG_string) || kind.equals(OMG_wstring))) {
                String bound; // tratamiento de string y wstring
                if (type.hasChildNodes()) {
                    Element tmp = 
                        (Element) type.getFirstChild().getFirstChild();
                    bound = tmp.getAttribute("value");
                } else
                    bound = "0";
                buffer.append("\tCORBA::ULong _bound = " + bound + ";\n");
            }
            buffer.append("\t_union_value <<= " + inverseClassType);
            if (kind != null
                && (kind.equals(OMG_string) || kind.equals(OMG_wstring))) {
                if (classType.startsWith("const"))
                    buffer.append(")");
                else
                    buffer.append(", true)");
            }
            buffer.append(";\n");
            if (defaultValue.equals(""))
                defaultValue = "false";
        }
        /*
         * else { buffer.append("\t_union_value < <= " + inverseClassType +
         * ");\n"); //buffer.append("\t_union_value < <= " + inverseClassType +
         * ");\n"); if (defaultValue.equals("")) defaultValue="false"; }
         */
        buffer.append("\t_isDefault = " + defaultValue + ";\n");
        buffer.append("}\n\n");
    }

    private void generateCppUnion_DefaultMethod(StringBuffer buffer,
                                                String discriminatorType,
                                                Union union, boolean noArgs)
        throws Exception
    {
        Element doc = union.getUnionElement();
        Element discriminator_type = 
            (Element) doc.getFirstChild().getFirstChild();
        String value = "";
        if (noArgs) {
            buffer.append("\t_discriminator = ");
            String name;
            if (union.getScopedDiscriminator() == null)
                name = discriminator_type.getAttribute(OMG_kind);
            else
                name = union.getScopedDiscrimKind();

            if ((name == null) || name.equals("") || name.equals(OMG_enum)) {
                // We have an enumeration
                StringTokenizer token = new StringTokenizer(discriminatorType,
                                                            "::");
                String scope = "";
                while (token.countTokens() > 1)
                    scope += token.nextElement() + "::";
                //value = discriminatorType + "::" + union.getDefaultValue();
                value = scope + union.getDefaultValue();
            } else if (name.equals(OMG_char) || name.equals(OMG_wchar)) {
                // We have a char/wchar discriminator
                // Obtain default union discriminator for default constructor
                Vector switchBody = union.getSwitchBody();
                UnionCase union_case = (UnionCase) switchBody.elementAt(0);
                Vector case_labels = union_case.m_case_labels;
                Element case_label = (Element) case_labels.elementAt(0);
                String value2 = getCaseValue(union, case_label);
                value = value2;
                //value = "'" + union.getDefaultValue() + "'";
            } else {
                // We have a basic discriminator
                value = union.getDefaultValue().toLowerCase();
                if (name.equals(OMG_long) || name.equals(OMG_longlong)
                    || name.equals(OMG_unsignedlong)
                    || name.equals(OMG_unsignedlonglong))
                    value += "L";
            }
            //buffer.append(value.toLowerCase() + ";\n"); 
            buffer.append(value + ";\n");
        } else {
            buffer.append("\tif(");
            Vector switchBody = union.getSwitchBody();
            /*
             * if (case_size > 1) { buffer.append("("); }
             */
            for (int z = 0; z < switchBody.size(); z++) {
                UnionCase union_case = (UnionCase) switchBody.elementAt(z);
                if (union_case.m_is_default)
                    continue; // default no tiene valor
                
                buffer.append("(");
                Vector case_labels = union_case.m_case_labels;
                // Vector case_labels =
                // ((UnionCase)switchBody.elementAt(z)).case_labels;
                /*
                 * if (case_labels_size > 1) buffer.append("(");
                 */
                for (int i = 0; i < case_labels.size(); i++) {
                    /*
                     * if (z > 0) buffer.append("||\n\t");
                     */
                    Element case_label = (Element) case_labels.elementAt(i);
                    value = getCaseValue(union, case_label);
                    
                    if(value != null) {
	                    buffer.append("discriminator == ");
	                    buffer.append(value);                  
                    
	                    if (i < case_labels.size() - 1) {
	                        buffer.append("||");
	                    }
                    }

                }
                /*
                 * if (case_labels_size > 1) buffer.append(")");
                 */
                if (((z < switchBody.size() - 1) && (!union.getHasDefault()))
                    || (z < switchBody.size() - 2))
                    buffer.append(") || \n\t   ");
            }
            /*
             * if (case_size > 1) { buffer.append(")"); }
             */
            buffer.append("))\n\t");
            buffer.append("\t\tthrow CORBA::BAD_OPERATION();\n");
            buffer.append("\t\n");
            buffer.append("\t_discriminator = discriminator;\n");
        }
        //buffer.append("\t//_union_value <<= null;\n"); 
        buffer.append("\t_isDefault = true;\n");
        buffer.append("}\n\n");
    }

    //PRA
    private void generateCppUnion_ResetMethod(StringBuffer buffer,
            String discriminatorType,
            Union union)
    	throws Exception
    {
    	// Default member initialization
    	String defaultInitialization = "";

    	Vector switchBody = union.getSwitchBody();
        for (int i = 0; i < switchBody.size(); i++) {
            UnionCase union_case = (UnionCase) switchBody.elementAt(i);
            Element el = union_case.m_declarator;

            String objectName = null;
            String tag = el.getTagName();
            if (tag.equals(OMG_array)) {
            	throw new SemanticException("Anonymous array members are not supported yet");
            } else if (tag.equals(OMG_simple_declarator)) {
                objectName = el.getAttribute(OMG_name);
                tag = XmlType2Cpp.getDefinitionType(union_case.m_type_spec);
            }
            
            if (union_case.m_is_default || defaultInitialization.equals("")) {
            	String type = XmlType2Cpp.getAccesorType(union_case.m_type_spec);
            	if (tag.equals(OMG_array)) {
                	type = XmlType2Cpp.getMemberType(union_case.m_type_spec);              
                	defaultInitialization = "\t" + type + " _reset_value;\n\t"
					                        + objectName + "(_reset_value);\n";
            	} else if (tag.equals(OMG_struct) 
            	           || tag.equals(OMG_union) 
            	           || tag.equals(OMG_sequence)) {
                	type = XmlType2Cpp.getMemberType(union_case.m_type_spec);              
                	defaultInitialization = "\t" + objectName + "(" + type + "());\n";
                } else {
                	defaultInitialization = "\t" + objectName + "((" + type + ") 0);\n";
                }
            }
        }

        buffer.append(defaultInitialization);
        buffer.append("}\n\n");
    }
    //EPRA
    
    
    private static String getInverseType(Element doc, boolean accesor)
    {
        String tag = doc.getTagName();
        if (tag.equals(OMG_type)) {
            if (accesor)
                return accesorInverseMapping(doc.getAttribute(OMG_kind));
            else
                return mutatorInverseMapping(doc.getAttribute(OMG_kind));
        } else if (tag.equals(OMG_scoped_name)) {
            String type = XmlType2Cpp.getType(doc);
            if (XmlType2Cpp.getDefinitionType(doc).equals(OMG_kind))
                type = XmlType2Cpp.getDeepKind(doc);
            if (accesor)
                return accesorInverseMapping(type); //getUnrolledName(doc));
            else
                return mutatorInverseMapping(type); //getUnrolledName(doc));
        }
        // Added to support the declaration of sequences into a Union Body
        else if (tag.equals(OMG_sequence)) { 
            if (accesor) {
                return "_union_value;";
            } else
                return "";
        }
        // Added to support the declaration of enumerations, structs into a
        // Union body  o unions...
        else if (tag.equals(OMG_enum) || tag.equals(OMG_struct)
                 || tag.equals(OMG_union)) {
            if (accesor)
                return accesorInverseMapping(XmlType2Cpp.getType(doc));
            else
                return mutatorInverseMapping(XmlType2Cpp.getType(doc));
        } else
            return "unknownType";
    }

    private static String accesorInverseMapping(String type)
    {
        if (type.equals(OMG_wchar)) {
            return "_union_value >>= CORBA::Any::to_wchar(_tmp);";
        } else if (type.equals(OMG_char)) {
            return "_union_value >>= CORBA::Any::to_char(_tmp);";
        } else if (type.equals(OMG_octet)) {
            return "_union_value >>= CORBA::Any::to_octet(_tmp);";
        } else if (type.equals(OMG_string)) {
            return "_union_value >>= CORBA::Any::to_string(_tmp, _bound);";
        } else if (type.equals(OMG_wstring)) {
            return "_union_value >>= CORBA::Any::to_wstring(_tmp, _bound);";
        } else if (type.equals(OMG_short)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_unsignedshort)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_long)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_unsignedlong)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_longlong)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_float)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_double)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_fixed)) {
            return "_union_value >>=CORBA::Any::to_fixed(*_tmp, 0, 0);"; 
        } else if (type.equals(OMG_any)) {
            return "_union_value >>= _tmp;";
        } else if (type.equals(OMG_boolean)) {
            return "_union_value >>= CORBA::Any::to_boolean(_tmp);";
        } else if (type.equals(OMG_Object)) {
            return "_union_value >>= CORBA::Any::to_object(_tmp);";
        } else if (type.equals(OMG_TypeCode)) {
            return "_union_value >>= _tmp;";
        }
        return "_union_value >>= _tmp;";
        //return type;
    }

    private static String mutatorInverseMapping(String type)
    {
        if (type.equals(OMG_wchar)) {
            return "CORBA::Any::from_wchar(value)";
        } else if (type.equals(OMG_char)) {
            return "CORBA::Any::from_char(value)";
        } else if (type.equals(OMG_octet)) {
            return "CORBA::Any::from_octet(value)";
        } else if (type.equals(OMG_string)) {
            return "CORBA::Any::from_string(value, _bound";
        } else if (type.equals(OMG_wstring)) {
            return "CORBA::Any::from_wstring(value, _bound";
        } else if (type.equals(OMG_short)) {
            return "(value)";
        } else if (type.equals(OMG_unsignedshort)) {
            return "(value)";
        } else if (type.equals(OMG_long)) {
            return "(value)";
        } else if (type.equals(OMG_unsignedlong)) {
            return "(value)";
        } else if (type.equals(OMG_longlong)) {
            return "(value)";
        } else if (type.equals(OMG_unsignedlonglong)) {
            return "(value)";
        } else if (type.equals(OMG_float)) {
            return "(value)";
        } else if (type.equals(OMG_double)) {
            return "(value)";
        } else if (type.equals(OMG_fixed)) {
            return "CORBA::Any::from_fixed(value, value.fixed_digits(), value.fixed_scale())";
        } else if (type.equals(OMG_any)) {
            return "(value)";
        } else if (type.equals(OMG_boolean)) {
            return "CORBA::Any::from_boolean(value)";
        } else if (type.equals(OMG_Object)) {
            //return "CORBA::Any::from_object(value)";
            return "(value)";
        } else if (type.equals(OMG_TypeCode)) {
            return "(value)";
        }
        //return type;
        return "(value)";
    }

    private String getDiscriminatorType(Element el, String name,
                                        String genPackage)
        throws Exception
    {
        Element discriminator = (Element) el.getFirstChild().getFirstChild();
        String tag = discriminator.getTagName();
        if (tag.equals(OMG_enum)) {
            String enumName = discriminator.getAttribute(OMG_name);
            String newPackage;
            if (!genPackage.equals("")) {
                newPackage = genPackage + "::" + name;//MACP "Package";
            } else {
                newPackage = name;//MACP "Package";
            }
            return newPackage + "::" + enumName;
        } else {
            return XmlType2Cpp.getType(discriminator);
        }
    }
    
    private String getCaseValue(Union union,
                                Element case_label)
	throws Exception
	{

		Element doc = union.getUnionElement();	
		Element discriminator_type = 
		(Element) doc.getFirstChild().getFirstChild();
	
      
        String value = null;
        try {
            Object expr = XmlExpr2Cpp.getExpr(case_label.getParentNode(),
                                              union.getDiscKind());
            value = expr.toString();
        }
        catch (SemanticException e) {}

        if ((value == null) || value.equals("")) {
            // enum or boolean value (comprobar)
            String dis_name = discriminator_type.getTagName();
            
            if (dis_name.equals(OMG_scoped_name)) {
                dis_name = union.getScopedDiscrimKind();
            }
            
            if (dis_name.equals(OMG_enum)) {
                //value = discriminatorType + "._" +
                // removeScope(case_label.getAttribute(OMG_name)); 
                value = 
                    XmlExpr2Cpp.getEnumExpr(case_label.getParentNode()).toString();
                
            } else if (dis_name.equals(OMG_char)) {                
               value =  "'" + case_label.getAttribute(OMG_name) + "'";
            } else if (union.getDiscKind().equals("boolean")) {
                value = case_label.getAttribute(OMG_value).toLowerCase();
            } else {
                value = case_label.getAttribute(OMG_name);                
            }
        } else {
            String case_type = case_label.getTagName();
            if (case_type.equals(OMG_character_literal)) {
                value = "'" + value + "'";
            } else {
                String name;
                
                if (union.getScopedDiscriminator() == null) {
                    name = discriminator_type.getAttribute(OMG_kind);
                } else {
                    name = union.getScopedDiscrimKind();
                }
                
                if (name.equals(OMG_long) || name.equals(OMG_longlong)) {
                    value += "L";
                } else if(name.equals(OMG_unsignedlong)
                    || name.equals(OMG_unsignedlonglong)) {
                    value += "UL";
                } else if (union.getDiscKind().equals("boolean")) {
                    value = case_label.getAttribute(OMG_value).toLowerCase();
                }
            }
        }
        
        return value;
    }

    private String generateCppHelperDef(Element doc, String genPackage)
        throws Exception
    {

        String name = doc.getAttribute(OMG_name);
        StringBuffer buffer = new StringBuffer();
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);

        buffer.append(XmlCppHelperGenerator.generateCpp(doc, null, genPackage,false));
        String contents = XmlCppHolderGenerator.generateCpp(genPackage, name,
                                                            holderClass);
        buffer.append(contents);
        return buffer.toString();
    }

    private String removeScope(String value)
    {
        StringTokenizer tokenizer = new StringTokenizer(value, Scope.SEP);
        String label = null;
        while (tokenizer.hasMoreTokens()) {
            label = tokenizer.nextToken();
        }
        return label;
    }

    private StringBuffer generateHppUnionDef(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage + "::" + name;
        //String
        // helperClass=TypedefManager.getInstance().getUnrolledHelperType(nameWithPackage);
        String helperClass = XmlType2Cpp.getHelperName(nameWithPackage);
        helperClass = helperClass.substring(genPackage.length());
        helperClass = helperClass.startsWith("::") ? helperClass.substring(2)
            : helperClass;
        String discriminatorType = getDiscriminatorType(doc, name, genPackage);

        // Package header
        XmlHppHeaderGenerator.generate(doc, buffer, "union", name, genPackage);
        buffer.append(XmlType2Cpp.getTypeStorageForTypeCode(doc));
        buffer.append("const ::CORBA::TypeCode_ptr _tc_");
        buffer.append(name);
        buffer.append(";\n\n");

        // Class header
        buffer.append("class ");
        buffer.append(name);
        buffer.append("{\n\n");
        buffer.append("\tpublic:\n");
        buffer.append("\ttypedef ");
        buffer.append(name);
        buffer.append("_var  ");
        //buffer.append(name); - DAVV: sobra
        buffer.append("_var_type;");
        buffer.append("\n\n");

        XmlHppHeaderGenerator.includeForwardDeclarations(doc, buffer, "union",
                                                         name, genPackage);
        XmlHppHeaderGenerator.includeChildrenHeaderFiles(doc, buffer, "union",
                                                         name, genPackage);
        //buffer.append("\tpublic:\n");
        //buffer.append("\tfriend class " + helperClass+";\n"); // DAVV - no
        // sirve con unions que se definen dentro de una clase (un strcut, otro
        // union...)
        buffer.append("\n");
        buffer.append("\tprivate:\n\t" + discriminatorType
                      + " _discriminator;\n");
        buffer.append("\tCORBA::Any _union_value;\n");
        buffer.append("\tbool _isDefault;\n\n");
        // Operators.
        buffer.append("\tpublic:\n");
        buffer.append("\t" + name + "();\n");
        buffer.append("\t" + name + "(const " + name + "& _value);\n");
        //buffer.append("\t~"+name+"() {};\n"); // DAVV - innecesario
        // (implementaci�n vac�a)
        buffer.append("\t" + name + "& operator=(const " + name + "&);\n");
        /*
        buffer.append("\t" + name + "& clone() const;\n\n");
        */
        // Items definition
        Union union = UnionManager.getInstance().get(doc);
        Vector switchBody = union.getSwitchBody();
        String objectName = null;
        for (int i = 0; i < switchBody.size(); i++) {
            UnionCase union_case = (UnionCase) switchBody.elementAt(i);
            Element type = union_case.m_type_spec;
            Element decl = union_case.m_declarator;
            String decl_tag = decl.getTagName();
            if (decl_tag.equals(OMG_simple_declarator)) {
                objectName = decl.getAttribute(OMG_name);
            } else if (decl_tag.equals(OMG_array)) {
                //objectName = decl.getAttribute(OMG_name) + "[]"; // DAVV
                throw new SemanticException(
                           "Anonymous array members are not supported in unions",
                           decl);
            }

            // The accesor method for each branch
            generateHppUnionAccesorAndMutatorMethods(buffer, type, objectName,
                                                     union_case,
                                                     discriminatorType);
            buffer.append("\n");
        }

        // Discriminator
        buffer.append("\t" + discriminatorType + " _d() const;\n");
        buffer.append("\tvoid _d(" + discriminatorType + " nd);\n\n");

        // Default
        if (union.getDefaultAllowed() && !union.getHasDefault()) {
        	buffer.append("\tvoid _default();\n");
        	buffer.append("\tvoid _default(" + discriminatorType
        			      + " discriminator);\n\n");
       	} else {
       		//PRA
       		buffer.append("\tprivate:\n");
       		buffer.append("\tvoid _reset();\n");
       		//EPRA
       	}

    
        buffer.append("\n}; //end of class \n\n");
        // Helper Generation.
        //String
        // helperContents=generateHppHelperDef(doc,genPackage,sourceDirectory,headerDirectory);
        buffer.append(XmlCppHelperGenerator.generateHpp(doc, null, genPackage,false));

        String holderClass = XmlType2Cpp.getHolderName(nameWithPackage);
        buffer.append(XmlCppHolderGenerator.generateHpp(OMG_Holder_Complex,
                                                        genPackage,
                                                        nameWithPackage,
                                                        holderClass));

        XmlHppHeaderGenerator.generateFoot(buffer, "union", name, genPackage);
        return buffer;
    }

    private static void generateHppUnionAccesorAndMutatorMethods(
                                                  StringBuffer buffer,
                                                  Element type,
                                                  String objectName,
                                                  UnionCase union_case,
                                                  String discriminatorType)
        throws Exception
    {

        /*
         * String tag = type.getTagName(); if (tag.equals(OMG_sequence)) throw
         * new SemanticException("Anonymous sequence members are not supported
         * in union yet", type);
         */

        String accesorType = XmlType2Cpp.getAccesorType(type);
        String modifierType = XmlType2Cpp.getModifierType(type);
        String referentType = XmlType2Cpp.getReferentType(type);

        // accesor
        buffer.append("\t" + accesorType + " " + objectName + "() const;\n");

        // referente
        if (referentType != null)
            buffer.append("\t" + referentType + " " + objectName + "();\n");

        // modificador
        buffer.append("\tvoid " + objectName + "(" + modifierType + ");\n");

        // modificador con valor de discriminante
        if (union_case.m_case_labels.size() > 1)
            buffer.append("\tvoid " + objectName + "(" + discriminatorType
                          + ", " + modifierType + ");\n");

        // modificadores extra para cadenas
        if (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type)) {
            String varType;
            if (XmlType2Cpp.isAWString(type))
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
    }

    private void generateCppUnionAccesorAndMutatorMethods(
                                             StringBuffer buffer,
                                             Element type,
                                             String objectName,
                                             String genPackage,
                                             UnionCase union_case,
                                             String discriminatorType,
                                             Union union)
        throws Exception
    {

        String accesorType = XmlType2Cpp.getAccesorType(type);
        String modifierType = XmlType2Cpp.getModifierType(type);
        String referentType = XmlType2Cpp.getReferentType(type);

        String nameWithPackage = genPackage.equals("") ? objectName
            : genPackage + "::" + objectName;

        String tag = type.getTagName();
        String typeStr = XmlType2Cpp.getType(type);
        String kind = "";
        if (tag.equals(OMG_scoped_name)) {
            tag = XmlType2Cpp.getDefinitionType(type);
            //typeStr = type.getAttribute(OMG_name); // RELALO
            if (tag.equals(OMG_kind))
                kind = XmlType2Cpp.getDeepKind(type);
        } else if (tag.equals(OMG_type)) {
            kind = type.getAttribute(OMG_kind);
            tag = OMG_kind;
        }/* else if (tag.equals(OMG_sequence)) */

        // accesor
        buffer.append(accesorType + " " + nameWithPackage + "() const {\n");
        generateCppUnionAccesorMethod(buffer, typeStr, union_case,
                                      discriminatorType, union, tag, kind);

        // referente
        if (referentType != null) {
            buffer.append(referentType + " " + nameWithPackage + "() {\n");
            generateCppUnionAccesorMethod(buffer, typeStr, union_case,
                                          discriminatorType, union, tag, kind);
        }

        // modificador
        buffer.append("void " + nameWithPackage + "(" + modifierType
                      + " value) {\n");
        generateCppUnionMutatorMethod(buffer, typeStr, union_case,
                                      discriminatorType, union, kind, true);

        // modificador con valor de discriminante
        if (union_case.m_case_labels.size() > 1) {
            buffer.append("void " + nameWithPackage + "(" + discriminatorType
                          + " discriminator, " + modifierType + " value) {\n");
            generateCppUnionMutatorMethod(buffer, typeStr, union_case,
                                          discriminatorType, union, kind, false);
        }

        // modificadores extra para cadenas
        if (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type)) {
            String varType;
            if (XmlType2Cpp.isAWString(type))
                varType = "CORBA::WString_var&";
            else
                varType = "CORBA::String_var&";
            buffer.append("void " + nameWithPackage + "(const " + modifierType
                          + " value) {\n");
            generateCppUnionMutatorMethod(buffer, "const " + typeStr,
                                          union_case, discriminatorType, union,
                                          kind, true);
            buffer.append("void " + nameWithPackage + "(const " + varType
                          + " value) {\n");
            generateCppUnionMutatorMethod(buffer, "const " + varType,
                                          union_case, discriminatorType, union,
                                          kind, true);
            if (union_case.m_case_labels.size() > 1) {
                buffer.append("void " + nameWithPackage + "("
                              + discriminatorType + " discriminator, const "
                              + modifierType + " value) {\n");
                generateCppUnionMutatorMethod(buffer, "const " + typeStr,
                                              union_case, discriminatorType,
                                              union, kind, false);
                buffer.append("void " + nameWithPackage + "("
                              + discriminatorType + " discriminator, const "
                              + varType + " value) {\n");
                generateCppUnionMutatorMethod(buffer, "const " + varType,
                                              union_case, discriminatorType,
                                              union, kind, false);
            }
        }
    }

    private void generateCppUnion_DiscMutatorMethod(StringBuffer buffer,
                                                    Union union)
        throws Exception
    {

        // METODO QUE GENERA EL MODIFICADOR DEL DISCRIMINANTE
        // ASEGURA QUE NO SE PUEDE MODIFICAR EL MIEMBRO ACTIVO DE LA UNION

        Element union_el = union.getUnionElement();
        Element switch_el = (Element) union_el.getFirstChild();
        Element discriminator_type = (Element) switch_el.getFirstChild();
        Vector switchBody = union.getSwitchBody();

        UnionCase union_case = (UnionCase) switchBody.elementAt(0);

        if (!((switchBody.size() == 1) && (union_case.m_is_default))) {
            // esto es para prevenirse de uniones que solo tienen un miembro,
            // que ademas es 'default' :-)

            // TRATAMIENTO DE '_discriminant' ES DEFAULT

            if (union.getHasDefault() || union.getDefaultAllowed()) {

                buffer.append("\n\tif (_isDefault) {");
                buffer.append("\n\t\tif (");

                for (int i = 0; i < switchBody.size(); i++) { 
                    // recorrido de todos los 'case' para obtener sus valores
                    union_case = (UnionCase) switchBody.elementAt(i);
                    if (union_case.m_is_default)
                        continue; // default no tiene valor
                    buffer.append("(");
                    Vector case_labels = union_case.m_case_labels;
                    for (int j = 0; j < case_labels.size(); j++) { 
                        // recorrido de todos los 'case' de un mismo miembro
                        
                        Element label = (Element) case_labels.elementAt(j);
                        
                        String value = getCaseValue(union, label);
                        
                        if(value != null) {
                            //buffer.append("(");                        
                            buffer.append("nd == " + value); //  condicion
                                                         // generada
                            if (j < case_labels.size() - 1)
                                buffer.append("||");
                        }
                    }
                    if (((i < switchBody.size() - 1) && (!union.getHasDefault()))
                        || (i < switchBody.size() - 2))
                        buffer.append(") || \n\t\t    ");
                }
                buffer.append(")");
                buffer.append(")\n\t\t\tthrow CORBA::BAD_OPERATION();"); 
                // lanzamiento de excepcion
                
                buffer.append("\n\t}\n\telse {");
            }

            // TRATAMIENTO DE '_discriminant' ES CUALQUIERA MENOS DEFAULT
            /*
             * if (dis_name.equals(OMG_enum)) { // esto esta por verse si
             * importa realmente buffer.append("\tswitch (_discriminator){\n"); }
             * else { buffer.append("\tswitch (_discriminator){"); // se trata
             * en un switch }
             */

            StringBuffer realBuffer = buffer;
            buffer = new StringBuffer();

            for (int i = 0; i < switchBody.size(); i++) { // recorrido de todos
                                                          // los valores del
                                                          // discriminante
                union_case = (UnionCase) switchBody.elementAt(i);
                if (union_case.m_is_default) // 'default' no tiene valor
                    continue;
                Vector case_labels = union_case.m_case_labels;
                for (int j = 0; j < case_labels.size(); j++) { // recorrido de
                                                               // todos los
                                                               // 'case' de un
                                                               // mismo miembro
                    Element label = (Element) case_labels.elementAt(j);
                    String value = null;
                    
                    value = getCaseValue(union, label);
                    
                    if (j == 0) {
                        if (!buffer.toString().equals(""))
                            buffer.append("\n\t\telse ");
                        else
                            buffer.append("\n\t\t");
                        buffer.append("if (");
                        //buffer.append("\n\t\tcase " + value + ":");
                        // -- el valor de una caso
                    } else
                        buffer.append("||");
                    buffer.append("(_discriminator ==  " + value + ")"); 
                    // el valor de una caso
                }
                buffer.append(") {\n");
                buffer.append("\t\t\tif ("); // cada caso del 'switch' generado
                                             // evalua un 'if'
                for (int j = 0; j < case_labels.size(); j++) { // recorrido de
                                                               // todos los
                                                               // 'case' del
                                                               // mismo miembro
                    Element label = (Element) case_labels.elementAt(j);
                    String value = null;
                    value = getCaseValue(union, label);
                    
                    if (case_labels.size() > 1)
                        buffer.append("(");
                    //buffer.append("nd != " + value.toLowerCase()); // MEOLLO
                    // -- condici�n generada
                    buffer.append("nd != " + value); // MEOLLO -- condici�n
                                                     // generada
                    if (j < case_labels.size() - 1)
                        buffer.append(")&&");
                }
                if (case_labels.size() > 1)
                    buffer.append(")");
                buffer.append(")\n\t\t\t\tthrow CORBA::BAD_OPERATION();"); 
                // se lanza una excepci�n
                
                //buffer.append("\n\t\t\tbreak;\n");
                buffer.append("\n\t\t}");
            }

            //buffer.append("\n\t}\n");

            if (union.getHasDefault() || union.getDefaultAllowed())
                buffer.append("\n\t}\n");

            realBuffer.append(buffer);
            buffer = realBuffer;
        }
        buffer.append("\t_discriminator = nd;\n"); // se permite
                                                   // modificar el discriminante

    }

    private void generateCppSubPackageDef(Element doc, String sourceDirectory,
                                          String headerDirectory,
                                          String unionName, String genPackage,
                                          boolean generateCode, boolean expanded, String h_ext, String c_ext)
        throws Exception
    {

        String newPackage;
        if (!genPackage.equals("")) {
            newPackage = genPackage + "::" + unionName;
        } else {
            newPackage = unionName;
        }

        NodeList nodes = doc.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {

            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_case)) {

                NodeList nodes2 = el.getChildNodes();
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Element union_el = (Element) nodes2.item(j);
                    String tag2 = union_el.getTagName();
                    if (tag2.equals("value")) {
                        Element type = (Element) union_el.getFirstChild();
                        String typeTag = type.getTagName();

                        if (typeTag.equals(OMG_enum)) {
                            XmlEnum2Cpp gen = new XmlEnum2Cpp();
                            gen.generateCpp(type, sourceDirectory,
                                            headerDirectory, newPackage,
                                            generateCode, expanded, h_ext, c_ext);
                        } else if (typeTag.equals(OMG_struct)) {
                            XmlStruct2Cpp gen = new XmlStruct2Cpp();
                            gen.generateCpp(type, sourceDirectory,
                                            headerDirectory, newPackage,
                                            generateCode, expanded, h_ext, c_ext);
                        } else if (typeTag.equals(OMG_union)) {
                            XmlUnion2Cpp gen = new XmlUnion2Cpp();
                            gen.generateCpp(type, sourceDirectory,
                                            headerDirectory, newPackage,
                                            generateCode, expanded, h_ext, c_ext);
                        }
                    }
                }
            } else if (tag.equals(OMG_switch)) { // discriminante enum definido
                                                 // in situ
                Element type = (Element) el.getFirstChild();
                if (type.getTagName().equals(OMG_enum)) {
                    XmlEnum2Cpp gen = new XmlEnum2Cpp();
                    gen.generateCpp(type, sourceDirectory, headerDirectory,
                                    newPackage, generateCode, expanded, h_ext, c_ext);
                }
            }

        }
    }

    /**
     * Preprocesado de la definicion de union a la busqueda de sequencia para
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
        // Doc continene la definicion de la union
        NodeList nl = doc.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            if (el.getTagName().equals(OMG_case)) {
                NodeList nodes2 = el.getChildNodes();
                for (int j = 0; j < nodes2.getLength(); j++) {
                    Element union_el = (Element) nodes2.item(j);
                    String tag2 = union_el.getTagName();
                    if (tag2.equals("value")) {
                        Element type = (Element) union_el.getFirstChild();
                        if (type.getTagName().equals(OMG_sequence)) {
                            Element newDecl = generateSequenceImplementation(
                                                               type,
                                                               sourceDirectory,
                                                               headerDirectory,
                                                               genPackage,
                                                               createDir, 
															   expanded, 
															   h_ext, 
															   c_ext);
                            String name = 
                                ((Element) newDecl.getLastChild()).getAttribute(OMG_name);
                            Element sup = 
                                union_el.getOwnerDocument().createElement(OMG_scoped_name);
                            
                            sup.setAttribute(OMG_name, genPackage + "::"
                                                        + name);
                            sup.setAttribute(OMG_variable_size_type, "true");
                            union_el.replaceChild(sup, type);
                            union_el.appendChild(newDecl);
                            // 'ajuste' en el UnionManager
                            Union union = UnionManager.getInstance().get(doc);
                            Vector switchBody = union.getSwitchBody();
                            for (int k = 0; k < switchBody.size(); k++) {
                                UnionCase union_case = 
                                    (UnionCase) switchBody.elementAt(k);
                                if (union_case.m_type_spec == type)
                                    union_case.m_type_spec = sup;
                            }
                        }
                    }
                }

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
        // No cumple mapping C++
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

}
