/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 326 $
* Date: $Date: 2010-01-18 13:12:39 +0100 (Mon, 18 Jan 2010) $
* Last modified by: $Author: avega $
*
* (C) Copyright 2004 Telef?nica Investigaci?n y Desarrollo
*     S.A.Unipersonal (Telef?nica I+D)
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
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.Enumeration;
import es.tid.TIDIdlc.CompilerConf;
/**
 * Generates Cpp for interface and valuetype skeletons.
 */
class XmlInterfaceSkeleton2Cpp extends XmlInterfaceUtils2Cpp
    implements Idl2XmlNames
{

    //private java.util.Hashtable interface_parentsForCpp = new
    // java.util.Hashtable();
    //private java.util.Hashtable interface_parentsForHeader = new
    // java.util.Hashtable();
    private java.util.Hashtable m_interface_parents = null;
    private int key_cont = 0;

    public StringBuffer generateCpp(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        m_interface_parents = new java.util.Hashtable();
        initInterfaceParents(doc);
        String name = doc.getAttribute(OMG_name);
        String POA_genPackage;
        String POAWithPackage;
        String POAName;
        if (!genPackage.equals("")) {
            POAName = name;
            POA_genPackage = "POA_" + genPackage;
            POAWithPackage = POA_genPackage + "::" + POAName;
        } else { // DAVV - interfaces de ?mbito global
            POAName = "POA_" + name;
            POA_genPackage = genPackage;
            POAWithPackage = POAName;
        } 

        //XmlCppHeaderGenerator.generate(buffer,"skeleton",name,"POA_"+genPackage);
        XmlCppHeaderGenerator.generate(buffer, "skeleton", POAName,
                                       POA_genPackage);
        String interfaceNameWithPackage = genPackage + "::" + name;
        //String
        // helperClass=TypedefManager.getInstance().getUnrolledHelperType(name);
        //String interfaceHelperNameWithPackage=genPackage+"::"+helperClass;

        buffer.append("#include \"TIDorb" + File.separator + "portable"
                      + File.separator + "ORB.h\"\n\n");

        // TOOLS DEFINITION
        String ltstr = POA_genPackage.equals("") ? "__my_" + name + "__ltstr"
            : POA_genPackage + "::__my_" + name + "__ltstr";
        String map = POA_genPackage.equals("") ? "__POA_" + name + "MAP"
            : POA_genPackage + "::__POA_" + name + "MAP";
        buffer.append("bool " + ltstr
                    + "::operator()(const char* s1, const char* s2) const\n{\n");
        buffer.append("\treturn (strcmp(s1, s2) < 0);\n");
        buffer.append("}\n\n");
        buffer.append(map + "::__POA_" + name + "MAP()\n// Constructor\n{\n");
        int index = 1;
        if (!doc.getTagName().equals(OMG_valuetype)) // POA para
                                                     // valuetypes no incluyen
                                                     // metodos del propio
                                                     // valuetype, s?lo de
                                                     // interfaces soportadas
            index = generateCppSkeletonMethodArray(buffer, doc, 1);
        Enumeration elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            index = generateCppSkeletonMethodArray(buffer, 
                        (Element) elements.nextElement(), index);
        buffer.append("}\n\n");

        // constant definitions
        generateCppConstDefs(buffer, doc, POAWithPackage);
        elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            generateCppConstDefs(buffer, (Element) elements.nextElement(),
                                 POAWithPackage);

        String pointer = interfaceNameWithPackage + "_ptr";

        if (!doc.getTagName().equals(OMG_valuetype)) { // el valuetype tb
                                                       // lo tendra, pero
                                                       // heredado
            buffer.append(pointer + " " + POAWithPackage + "::_this() {\n");
            buffer.append("\tCORBA::Object_var _ref = PortableServer::DynamicImplementation::_this();\n");
            buffer.append("\treturn ");
            buffer.append(interfaceNameWithPackage);
            buffer.append("::_narrow(_ref);\n");
            buffer.append("}\n\n");
        }

        buffer.append("const CORBA::RepositoryIdSeq_ptr " + POAWithPackage
                      + "::__init_ids(){\n");
        buffer.append("\tCORBA::RepositoryIdSeq_ptr ids = new  CORBA::RepositoryIdSeq();\n");
        StringBuffer bufferTemp = new StringBuffer();
        int num = generateInterfacesSupported(bufferTemp, doc, 0);
        buffer.append("\tids->length(" + (num + 1) + ");\n");
        buffer.append(bufferTemp.toString());
        buffer.append("\treturn ids;\n");
        buffer.append("}\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr " + POAWithPackage
                      + "::__ids = " + POAWithPackage + "::__init_ids();\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr ");
        buffer.append(POAWithPackage);
        buffer.append("::_ids() {\n");
        buffer.append("\treturn __ids;\n");
        buffer.append("}\n\n");

        buffer.append("const CORBA::RepositoryIdSeq_ptr "
                      + POAWithPackage
                      + "::_all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectId) \n{\n");
        buffer.append("\treturn _ids();\n");
        buffer.append("}\n\n");

        buffer.append("CORBA::RepositoryId "
                      + POAWithPackage
                      + "::_primary_interface(const PortableServer::ObjectId& oid,PortableServer::POA_ptr poa)\n{\n");
        buffer.append("\t return CORBA::string_dup(\""
                      + RepositoryIdManager.getInstance().get(doc)
                      + "\");\n}\n\n");

        //buffer.append(POA_genPackage+"::__POA_"+name+"MAP
        // "+POAWithPackage+"::_mapped_methods;\n\n");
        buffer.append(map + " " + POAWithPackage + "::_mapped_methods;\n\n");

        buffer.append("void " + POAWithPackage
                      + "::invoke(::CORBA::ServerRequest_ptr _request)\n{\n");
        buffer.append("\tint _method_id = _mapped_methods._methods[_request->operation()];\n");
        buffer.append("\tif (_method_id == 0)\n\t{// Undefined Operation \n");
        buffer.append("\t\tthrow ::CORBA::BAD_OPERATION();\n");
        buffer.append("\t}\n");
        //buffer.append("\tTIDorb::portable::ORB* __orb=
        // get_delegate()->_orb();\n\n");
        buffer
            .append("\tTIDorb::portable::ORB* __orb= dynamic_cast< TIDorb::portable::ORB* > (get_delegate()->orb(this));\n\n");
        //buffer.append("\tswitch(_method_id)\n\t{\n");
        // el 'switch' se cambia por una secuencia de 'if ... else' por las
        // limitaciones encontradas en aCC para compilar 'switch .. case' de
        // gran tama?o
        index = 1;
        if (!doc.getTagName().equals(OMG_valuetype)) // DAVV - POA para
                                                     // valuetypes no incluyen
                                                     // metodos del propio
                                                     // valuetype, s?lo de
                                                     // interfaces soportadas
            index = generateCppSkeletonExportDef(buffer, doc, index, true);
        elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            index = generateCppSkeletonExportDef(
                        buffer, (Element) elements.nextElement(), index, true);
        buffer.append("\t\t");
        if (index > 1)
            buffer.append("else ");
        buffer.append("throw ::CORBA::BAD_OPERATION();\n");
        //buffer.append("\t} // switch\n");
        buffer.append("}// end of method invoke.\n");
        return buffer;
    }

    private int generateCppSkeletonExportDef(StringBuffer buffer, Element doc,
                                             int index, boolean poa)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                generateCppSkeletonMethod(buffer, el, index, poa);
                index++;
            } else if (tag.equals(OMG_attr_dcl))
                index = generateCppSkeletonAttribute(buffer, el, index);
        }
        return index;
    }

    /**
     * Defines the members og the MAP corresponding to the methods of the
     * interfaces..
     */
    private int generateCppSkeletonMethodArray(StringBuffer buffer,
                                               Element doc, int index)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("\t_methods[\"");
                buffer.append(el.getAttribute(OMG_name));
                buffer.append("\"]=" + index);
                buffer.append(";\n");
                index++;
            } else if (tag.equals(OMG_attr_dcl)) {
                index = generateCppSkeletonMethodArrayAttr(buffer, el, index);
            }
        }

        return index;
    }

    /**
     * Defines the members og the MAP corresponding to the accesors for the
     * attributes.
     */
    private int generateCppSkeletonMethodArrayAttr(StringBuffer buffer,
                                                   Element doc, int index)
    {
        NodeList nodes = doc.getChildNodes();
        String readonly = doc.getAttribute(OMG_readonly);
        // Get & set methods
        for (int i = 1; i < nodes.getLength(); i++) {
            Element att = (Element) nodes.item(i);
            String name = att.getAttribute(OMG_name);
            buffer.append("\t_methods[\"_get_");
            buffer.append(name);
            buffer.append("\"]=" + index);
            buffer.append(";\n");
            index++;
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("\t_methods[\"_set_");
                buffer.append(name);
                buffer.append("\"]=" + index);
                buffer.append(";\n");
                index++;
            }
        }
        return index;
    }

    private void generateCppSkeletonMethod(StringBuffer buffer, Element doc,
                                           int index, boolean poa)
        throws Exception
    {
        NodeList nodes = doc.getChildNodes();
        NodeList exceps = null;
        int numParams = nodes.getLength() - 1;
        // para cambiar indentacion
        String ident = "\t\t";
        // buffer.append(ident + "case " + index + ":{ \n");
        buffer.append(ident);
        if (index > 1)
            buffer.append("else ");
        buffer.append("if (_method_id == " + index + ") {\n");
        ident += "\t";

        // Exceptions
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_raises)) {
                exceps = el.getChildNodes();
                numParams -= 1;
                break;
            }
        }

        boolean exceptsExist = ((exceps != null) && (exceps.getLength() > 0));

        if (exceptsExist) {
            buffer.append(ident + "try {\n");
            ident += "\t";
        }
        buffer.append(ident + "::CORBA::NVList_var _params;\n");       
        buffer.append(ident + "__orb->create_list(");
        buffer.append(numParams);
        buffer.append(", _params);\n\n");

        // Parameters 
        // rellenamos _params
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                buffer.append(ident + "::CORBA::NamedValue_var __my_" + paramName
                              + " = _params->add_item(\"" + paramName + "\"");
                if (in)
                    buffer.append(", ::CORBA::ARG_IN);\n");
                else if (inout)
                    buffer.append(", ::CORBA::ARG_INOUT);\n");
                else
                    buffer.append(", ::CORBA::ARG_OUT);\n");
                // Fix bug [#394] Change all internal calls of TIDorb::portable::Any from type(tc) 
                // to set_type(tc) 
                //buffer.append(ident + "__" + paramName + "->value()->type(");
                buffer.append(ident + "__my_" + paramName + "->value()->delegate().set_type(");
                buffer.append(XmlType2Cpp.getTypecodeName(paramType));
                buffer.append(");\n\n");
                //buffer.append("\t\t\t_params->add_value(\""+paramName+"\",
                // __"+paramName);
            }
        }
        buffer.append(ident + "_request->arguments(_params);\n\n");

        // Parameters - DAVV - insercion/extraccion Any
        boolean thereIsParam = false;
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                thereIsParam = true;
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (out)
                    generateOutArgumentDefinition(buffer, paramType, paramName,
                                                  ident);
                else if (in)
                    generateInArgumentExtraction(buffer,
                                                 paramType,
                                                 paramName,
                                                 "(*(__my_"
                                                 + paramName
                                                 + "->value()))",
                                                 ident);
                else if (inout) {
                    generateInoutArgumentDefinition(buffer, paramType,
                                                    paramName, 
                                                    "(*(__my_" + paramName
                                                    + "->value()))",
                                                    ident);
                    generateInoutArgumentExtraction(buffer, paramType,
                                                    paramName, 
                                                    "(*(__my_" + paramName
                                                    + "->value()))",
                                                    ident, true, poa);
                }
            }
        }

        if (thereIsParam)
            buffer.append("\n");
        buffer.append(ident);
        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        if (returnTypeL.getLength() > 0) {
            Element ret = (Element) returnTypeL.item(0);
            
            	String aux = XmlType2Cpp.getReturnType(ret);
            	//System.out.println("ReturnType: " + aux);
            	//System.out.println("DeepKind: " + XmlType2Cpp.getDeepKind(ret));
            	//AVG: Tener en cuenta tambien los Typedefs del objeto IDL: bug #327
            	//if (aux.endsWith("CORBA::Object_ptr")){            	 
            	//	buffer.append(XmlType2Cpp.getReturnType(ret));
            	//}
            	if ((XmlType2Cpp.getDeepKind(ret).equals("Object"))) {            		            		            		
            		buffer.append(XmlType2Cpp.getReturnType(ret));
            	}
            	// AVG END
            	else if (aux.endsWith("_ptr")){
            		String aux2 = aux.substring(0,aux.length()-4);
            		buffer.append(aux2 + "_var");
            	}
            	else 
                	buffer.append(XmlType2Cpp.getReturnType(ret));
            buffer.append(" _result = ");
        }

        // Method name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append("this->");
        buffer.append(nombre);
        buffer.append("(");

        // Parameters (passing)
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (i > 1)
                    buffer.append(", ");
                if (in)
                    generateInArgumentParameterUsing(buffer, paramType,
                                                     paramName);
                else if (out)
                    generateOutArgumentParameterUsing(buffer, paramType,
                                                      paramName);
                else if (inout)
                    generateInoutArgumentParameterUsing(buffer, paramType,
                                                        paramName);
                else
                    buffer.append(paramName);
            }
        }
        buffer.append(");\n\n");

        // Get the result
        if (returnTypeL.getLength() > 0) {
            Element ret = (Element) returnTypeL.item(0);
            buffer.append(ident + "::CORBA::Any _resultAny;\n");
            buffer.append(ident + "_resultAny <<=");
            buffer.append(XmlType2Cpp.getAnyInsertion(ret, "_result", true)
                          + ";\n");
            // FIX bug #270
            if (XmlType2Cpp.getDefinitionType(ret).equals(OMG_valuetype))
        		buffer.append(ident + "_result->_remove_ref();\n");
            buffer.append(ident + "_request->set_result(_resultAny);\n\n");
            
            // Fix bug #383	Memory leak in operations with Object and References as 'in' and 'ret' arguments
            if (XmlType2Cpp.getDeepKind(ret).equals(OMG_Object))
                buffer.append(ident + "CORBA::release(_result);\n");

        }

        // Parameters (returning out parameters)
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                //boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (!in)
                    // inout or out parameters.
                    generateOutArgumentInsertion(buffer,
                                                 paramType,
                                                 paramName,
                                                 "(*(__my_"
                                                 + paramName
                                                 + "->value()))");
                // FIX bug #270
                if (out && XmlType2Cpp.getDefinitionType(paramType).equals(OMG_valuetype))
            		buffer.append(ident + paramName + "->_remove_ref();\n");
            }
        }

        // AVG: delete in params
        if(CompilerConf.getNonCopyingOperators()){
        
	        for (int i = 1; i < nodes.getLength(); i++) {
            	Element el = (Element) nodes.item(i);
            	if (el.getTagName().equals(OMG_parameter)) {
                	Element paramType = (Element) el.getChildNodes().item(0);
                	String paramName = el.getAttribute(OMG_name);
                	boolean in = el.getAttribute(OMG_kind).equals("in");
                	if (in){
                		String definition = XmlType2Cpp.getDefinitionType(paramType);
                    	if (definition.equals(OMG_struct)){
                    		buffer.append(ident + "delete " + paramName + ";\n");
                    	} else if (definition.equals(OMG_union)){
                    		buffer.append(ident + "delete " + paramName + ";\n");
                    	} else if (definition.equals(OMG_sequence)){
                    		buffer.append(ident + "delete " + paramName + ";\n");
                        // Fix bug #383	Memory leak in operations with Object and References as 'in' and 'ret' arguments
                    	} else if ((XmlType2Cpp.getDeepKind(paramType).equals(OMG_Object))) {  
                                buffer.append(ident + "CORBA::release(" + paramName + ");\n");
                    	} else if ((XmlType2Cpp.getDefinitionType(paramType).equals(OMG_interface))) {  
                                buffer.append(ident + "CORBA::release(" + paramName + ");\n");
                        }
                	}
            	}
        	}
           
        }
        
        buffer.append("\n");
        if (exceptsExist) {
            ident = ident.substring(1);
            buffer.append(ident + "}\n");
            for (int j = 0; j < exceps.getLength(); j++) {
                Element ex = (Element) exceps.item(j);
                buffer.append(ident + "catch(");
                buffer.append(XmlType2Cpp.getType(ex));
                buffer.append(" _exception)\n");
                buffer.append(ident + "{\n");
                ident += "\t";
                buffer.append(ident
                            + "::CORBA::Any_var _exceptionAny = __orb->create_any();\n");
                buffer.append(ident + XmlType2Cpp.getHelperType(ex));
                buffer.append("::insert(*_exceptionAny, _exception);\n"); 
                // es un metodo estatico!!
                buffer.append(ident
                              + "_request->set_exception(* _exceptionAny);\n"); 
                // incluido el puntero
                ident = ident.substring(1);
                buffer.append(ident + "}\n");
            }
            //buffer.append("}// end of method\n");
        }
        buffer.append(ident + "return;\n");
        ident = ident.substring(1);
        buffer.append(ident + "}\n");
    }

    private int generateCppSkeletonAttribute(StringBuffer buffer, Element doc,
                                             int index)
        throws Exception
    {
        // Get the type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        //String type = XmlType2Cpp.getType(typeEl);
        String readonly = doc.getAttribute(OMG_readonly);

        for (int i = 1; i < nodes.getLength(); i++) {
            // Accessors generation
            String ident = "\t\t";
            buffer.append(ident); //+ "case " + index + ":{\n");
            if (index > 1)
                buffer.append("else ");
            buffer.append("if (_method_id == " + index + ") {\n");
            ident += "\t";
            buffer.append(ident + "::CORBA::NVList_var _params ;\n");
            buffer.append(ident + "__orb->create_list(0,_params);\n");
            buffer.append(ident + "_request->arguments(_params);\n");

            buffer.append(ident);
            // Return type
            
           	String aux = XmlType2Cpp.getReturnType(typeEl);
         	if (aux.endsWith("CORBA::Object_ptr")){
         		buffer.append(XmlType2Cpp.getReturnType(typeEl));
         	}
         	else if (aux.endsWith("_ptr")){
           		String aux2 = aux.substring(0,aux.length()-4);
           		buffer.append(aux2 + "_var");
           	}
           	else          
           		buffer.append(XmlType2Cpp.getReturnType(typeEl));
            //buffer.append(XmlType2Cpp.getReturnType(typeEl));
            buffer.append(" _result = ");

            // Method name
            Element nameEl = (Element) nodes.item(i);
            String name = nameEl.getAttribute(OMG_name);
            buffer.append("this->");
            buffer.append(name);
            buffer.append("();\n");

            // Get the result
            buffer.append(ident + "::CORBA::Any _resultAny;\n");
            buffer.append(ident + "_resultAny <<=");
            buffer.append(XmlType2Cpp.getAnyInsertion(typeEl, "_result", true));
            buffer.append(";\n");
            buffer.append(ident + "_request->set_result(_resultAny);\n");

            buffer.append(ident + "return;\n");
            ident = ident.substring(1);
            buffer.append(ident + "}\n");
            index++;

            // Modifiers generation

            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append(ident); // + "case " + index + ":{\n");
                if (index > 1)
                    buffer.append("else ");
                buffer.append("if (_method_id == " + index + ") {\n");
                ident += "\t";
                buffer.append(ident + "::CORBA::NVList_var _params;\n");
                buffer.append(ident + "__orb->create_list(1,_params);\n");
                // Parameter (any value declaration)
                buffer.append(ident
                              + "::CORBA::NamedValue_var __value = _params->add_item(\"value\", ::CORBA::ARG_IN);\n");
                
                // Fix bug [#394] Change all internal calls of TIDorb::portable::Any from type(tc) 
                // to set_type(tc)
                //buffer.append(ident + "__value->value()->type(");
                buffer.append(ident + "__value->value()->delegate().set_type(");

                //String intermediateTk_Type = XmlType2Cpp.getTypecode(typeEl);    
                String intermediateTk_Type = XmlType2Cpp.getTypecodeName(typeEl);
                String tk_type=null;
                tk_type=intermediateTk_Type;
                
/*                if (intermediateTk_Type.indexOf("Marshalling::")==0||intermediateTk_Type.indexOf("CORBA::")==0) {
                	// Is a complex type
                	
                	tk_type = intermediateTk_Type.replaceFirst("_", "_tc_");
                	tk_type = tk_type.replaceFirst("Helper.*", "");
                	
                }
                else {
                	// Is a simple type
                	if (!intermediateTk_Type.startsWith("CORBA::")) {
                		// If starts with CORBA::, there is nothing to do!! 
                        int leftBracket = intermediateTk_Type.indexOf("(");
                        tk_type = intermediateTk_Type.substring(leftBracket+1, intermediateTk_Type.length()-1);
                        tk_type = tk_type.replaceFirst("tk","_tc");
                		
                	} 

                }*/
                
                buffer.append(tk_type);
                buffer.append(");\n");
                
                //buffer.append("\t\t\t_params->add_value(\"value\", __value,
                // ::CORBA::ARG_IN);\n");
                buffer.append(ident + "_request->arguments(_params);\n");

                generateInArgumentExtraction(buffer, typeEl, "value",
                                             "(*(__value->value()))", ident);
                // Method name
                buffer.append(ident + "this->");
                buffer.append(name);
                buffer.append("(");
                generateInArgumentParameterUsing(buffer, typeEl, "value");
                buffer.append(");\n");
                buffer.append(ident + "return;\n");
                ident = ident.substring(1);
                buffer.append(ident + "}\n");
                index++;
            }
        }

        return index;
    }

    public StringBuffer generateHpp(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        m_interface_parents = new java.util.Hashtable();
        initInterfaceParents(doc);
        // Header
        String name = doc.getAttribute(OMG_name);
        String className = name;
        if (genPackage.equals("")) // DAVV - interfaces con ?mbito global
            className = "POA_" + className;
        //XmlHppHeaderGenerator.generate(doc,buffer,"skeleton","POA_"+name,genPackage);
        XmlHppHeaderGenerator.generate(doc, buffer, "skeleton", name,
                                       genPackage);
        // Tools headers
        buffer.append("struct __my_" + name + "__ltstr{\n");
        buffer
            .append("\t\tbool operator()(const char* s1, const char* s2) const;\n");
        buffer.append("};//end __my_" + name + "__ltstr struct\n\n");
        buffer.append("struct __POA_" + name + "MAP {\n");
        //buffer.append("\t\tstd::map<const char*,int,__"+name+"__ltstr>
        // _methods;\n");
        buffer.append("\t\tmap<const char*,int,__my_" + name
                      + "__ltstr> _methods;\n");
        buffer.append("\t\t__POA_" + name + "MAP();\n");
        buffer.append("};// end of __POA_" + name + "MAP struct \n\n");

        // Class header
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        buffer.append("class ");
        buffer.append(className);

        String pointer = nameWithPackage + "_ptr";

        if (doc.getTagName().equals(OMG_valuetype))
            generatePOAValueTypeInheritance(buffer, doc, nameWithPackage);
        else
            buffer.append(": public virtual PortableServer::DynamicImplementation"); 
        // hereda de PortableServer::ServantBase

        buffer.append("\n{\n\n");
        buffer.append("\tpublic:\n");
        if (!doc.getTagName().equals(OMG_valuetype)) // el valuetype tb
                                                     // lo tendra, pero heredado
            buffer.append("\t\t" + pointer + " _this();\n\n");
        buffer.append("\t\tvoid invoke(::CORBA::ServerRequest_ptr _request);\n");
        buffer.append("\t\tconst CORBA::RepositoryIdSeq_ptr _all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectId);\n");
        buffer.append("\t\tCORBA::RepositoryId _primary_interface(const PortableServer::ObjectId& oid,PortableServer::POA_ptr poa);\n\n");

        // constant definitions
        generateHppConstDefs(buffer, doc);
        Enumeration elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            generateHppConstDefs(buffer, (Element) elements.nextElement());

        if (!doc.getTagName().equals(OMG_valuetype)) // POA para
                                                     // valuetypes no incluyen
                                                     // metodos del propio
                                                     // valuetype, s?lo de
                                                     // interfaces soportadas
            generateHppExportDef(buffer, doc);
        elements = m_interface_parents.elements();
        while (elements.hasMoreElements())
            generateHppExportDef(buffer, (Element) elements.nextElement());
        buffer.append("\tprivate:\n\n");
        buffer.append("\t\tvirtual const CORBA::RepositoryIdSeq_ptr _ids();\n");
        buffer.append("\t\tstatic const CORBA::RepositoryIdSeq_ptr __ids;\n");
        buffer.append("\t\tstatic const CORBA::RepositoryIdSeq_ptr __init_ids();\n");
        buffer.append("\t\tstatic __POA_" + name + "MAP _mapped_methods; \n");

        //buffer.append("}; // end of POA"+name+"\n");
        buffer.append("}; // end of " + className + "\n"); // por interfaces de
                                                           // ?mbito global

        // POA File declarator.
        XmlHppHeaderGenerator.generateFoot(buffer, "skeleton", 
                                           name, genPackage);
        return buffer;
    }

    private void generateHppExportDef(StringBuffer buffer, Element doc)
        throws Exception
    {

        // Items definition
        NodeList nodes = doc.getChildNodes();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) { // operation declaration
                buffer.append("\t\t");
                generateHppMethodHeader(buffer, el, true, true, "\t\t");
                buffer.append(";\n\n");
            } else if (tag.equals(OMG_attr_dcl)) { // attribute declaration
                generateHppAttributeDecl(buffer, el, true, "\t\t");
            }
        }
    }


    private void generateCppConstDefs(StringBuffer buffer, Element doc,
                                      String interfaceName)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_const_dcl)) { // constant declaration
                NodeList nodes2 = el.getChildNodes();
                String scopedName = el.getAttribute(OMG_scoped_name);
                // DAVV - inicializacion
                // Value generation
                Element typeEl = (Element) nodes2.item(0);
                String type = XmlType2Cpp.getType(typeEl);
                buffer.append("const ");
                buffer.append(type);
                buffer.append(" ");
                buffer.append(interfaceName);
                buffer.append("::");
                buffer.append(el.getAttribute(OMG_name));
                buffer.append(" = ");
                Object expr = IdlConstants.getInstance().getValue(scopedName);
                String typeExpr = IdlConstants.getInstance().getType(scopedName);
                String kind = XmlType2Cpp.getDeepKind(typeEl);
                if (typeExpr == "char*") {
                	buffer.append("\"");
                	buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
                	buffer.append("\"");
                } else if (typeExpr =="CORBA::Char" /*PRA*/ || kind.equals("char")) {
                    buffer.append("'");
                    buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
                    buffer.append("'");
                } else {
                    buffer.append(XmlExpr2Cpp.toString(expr, typeExpr));
                }
                buffer.append(";\n\n");
            }
        }
    }

    private void generateHppConstDefs(StringBuffer buffer, Element doc)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_const_dcl)) { // constant declaration
                NodeList nodes2 = el.getChildNodes();
                // Value generation
                Element typeEl = (Element) nodes2.item(0);
                String type = XmlType2Cpp.getType(typeEl);
                buffer.append("\t\t");
                buffer.append("static const ");
                buffer.append(type);
                buffer.append(" ");
                buffer.append(el.getAttribute(OMG_name));
                buffer.append(";\n\n");
            }
        }
    }

    private void initInterfaceParents(Element doc)
    {
        NodeList nodes = doc.getChildNodes();
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    String clase = el.getAttribute(OMG_name);
                    Scope scope = Scope.getGlobalScopeInterface(clase);
                    Element inhElement = scope.getElement();
                    // Generate the operation for all the interface parents
                    //if (!interface_parentsForCpp.containsKey(inhElement))
                    if (!m_interface_parents.contains(inhElement)) {
                        // This is to avoid the duplication of the operation
                        // when there's multiple
                        // inheritance and one of the father inherits from the
                        // other
                        //interface_parentsForCpp.put(inhElement,"void");
                        m_interface_parents.put(new java.lang.Integer(key_cont++), inhElement);
                        initInterfaceParents(inhElement);
                    }
                }
            } else if (el1.getTagName().equals(OMG_value_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el2 = (Element) nodes.item(i);
                    if (el2.getTagName().equals(OMG_supports)) {
                        NodeList supports = el2.getChildNodes();
                        for (int j = 0; j < supports.getLength(); j++) {
                            Element supportedScopeEl = 
                                (Element) supports.item(j);
                            String supported_tag = supportedScopeEl
                                .getTagName();
                            if (supported_tag.equals(OMG_scoped_name)) {
                                String supportedScope = 
                                    supportedScopeEl.getAttribute(OMG_name);
                                Scope inhScope = 
                                    Scope.getGlobalScopeInterface(supportedScope);
                                Element elfather = inhScope.getElement();
                                if (!m_interface_parents.contains(elfather)) {
                                    m_interface_parents.put(new java.lang.Integer(key_cont++), elfather);
                                    initInterfaceParents(elfather);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void generatePOAValueTypeInheritance(StringBuffer buffer,
                                                 Element doc,
                                                 String nameWithPackage)
    {
        // generando el POA correspondiente a un valuetype que
        // soporta una interfaz
        buffer.append(": public virtual " + nameWithPackage);
        Element inheritance = (Element) doc.getFirstChild();
        if (inheritance.getTagName().equals(OMG_value_inheritance_spec)) {
            NodeList parents = inheritance.getChildNodes();
            for (int i = 0; i < parents.getLength(); i++) {
                Element parent = (Element) parents.item(i);
                if (parent.getTagName().equals(OMG_supports)) {
                    NodeList supports = parent.getChildNodes();
                    for (int j = 0; j < supports.getLength(); j++) {
                        Element supportedScopeEl = (Element) supports.item(j);
                        String supported_tag = supportedScopeEl.getTagName();
                        if (supported_tag.equals(OMG_scoped_name)) {
                            String supportedScope = 
                                supportedScopeEl.getAttribute(OMG_name);
                            Scope inhScope = 
                                Scope.getGlobalScopeInterface(supportedScope);
                            Element elfather = inhScope.getElement();
                            String father_tag = elfather.getTagName();
                            if (father_tag.equals(OMG_interface)) {
                                String isAbstractS = 
                                    elfather.getAttribute(OMG_abstract);
                                boolean isAbstract = (isAbstractS != null)
                                                     && (isAbstractS.equals(OMG_true));
                                if (!isAbstract) {
                                    buffer.append(",\n\tpublic virtual POA_"
                                                  + TypeManager.convert(supportedScope));
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}
