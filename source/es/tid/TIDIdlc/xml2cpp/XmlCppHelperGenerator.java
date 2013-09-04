/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 308 $
* Date: $Date: 2009-06-02 10:48:34 +0200 (Tue, 02 Jun 2009) $
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
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;

import org.w3c.dom.*;

import java.util.Vector;

/**
 * Generates helper classes for IDL types
 */
class XmlCppHelperGenerator
    implements Idl2XmlNames
{

    public static String generateHpp(Element type, Element decl,
                                     String genPackage, boolean noHolder)
    {
        // genera header de la clase helper correspondiente a 'type' y 'decl'
        //      - 'decl' sera null, salvo que la llamada al m???todo proceda de
        // XmlTypedef2Cpp
        StringBuffer buffer = new StringBuffer();

        String name = "";
        Element discriminant = type;
        if (decl == null)
            name = type.getAttribute(OMG_name);
        else {
            name = decl.getAttribute(OMG_name);
            if (decl.getTagName().equals(OMG_array))
                discriminant = decl;
        }

        String helperName = XmlType2Cpp.getHelperName(name);
        String nameWithPackage = genPackage.equals("") ? name : 
            genPackage + "::" + name;

        buffer.append("class " + helperName + " {\n");
        buffer.append("\n\tpublic:\n");

        // TypeCode y RepositoryId
        buffer.append("\t\tstatic ::CORBA::TypeCode_ptr type();\n\n");
        buffer.append("\t\tstatic const char* id() { return \""
                + RepositoryIdManager.getInstance().get(decl == null ? type : decl)
                + "\"; }\n\n");

        // Inserci???n y extracci???n en Any - variables en n???mero (mapping
        // C++)
        String insertTypeParamCopy = getInsertType(discriminant,
                                                   nameWithPackage, true);
        String insertTypeParamNoCopy = getInsertType(discriminant,
                                                     nameWithPackage, false);
        String extractTypeParam = getExtractType(discriminant, nameWithPackage);
        String def = XmlType2Cpp.getDefinitionType(discriminant);

        //Bug #42------------------------------------------------------
        if (!noHolder) {
        	//BUG #42-----------------------------------------------------------------
        	if (!insertTypeParamCopy.equals(""))
        		buffer.append("\t\tstatic void insert(::CORBA::Any& any, "
        				+ insertTypeParamCopy + " _value);\n\n");
	        if (!insertTypeParamNoCopy.equals(""))
	            buffer.append("\t\tstatic void insert(::CORBA::Any& any, "
	                          + insertTypeParamNoCopy + " _value, bool must_free=true);\n\n");
	        if (!extractTypeParam.equals("")) {
	        	// structs, exceptions, sequences, unions and valuetypes uses a const _value
	        	// Workaround "const const"
	        	if ((def.equals(OMG_struct)
	        	     || def.equals(OMG_exception)
					 || def.equals(OMG_sequence)
					 || def.equals(OMG_union)
					 || def.equals(OMG_valuetype))
					 && !extractTypeParam.startsWith("const "))
	        	{
		            buffer.append("\t\tstatic CORBA::Boolean extract(const ::CORBA::Any& any, const "
	                        + extractTypeParam + " _value);\n\n");
	        	} else {
		            buffer.append("\t\tstatic CORBA::Boolean extract(const ::CORBA::Any& any, "
	                        + extractTypeParam + " _value);\n\n");
                    //jagd se anaden mas tipos para optimizar
                
                      if (def.equals(OMG_struct)
                          || def.equals(OMG_exception)
                          || def.equals(OMG_sequence)
                          || def.equals(OMG_union))
                      {
                          buffer.append("\t\tstatic CORBA::Boolean extract(const ::CORBA::Any& any, "
                          +nameWithPackage+"*&"+ " _value);\n\n");
                          
                          buffer.append("\t\tstatic CORBA::Boolean extract(const ::CORBA::Any& any, "
                          +nameWithPackage+"&"+ " _value);\n\n");
                      }
                    //fin jagd
	        	}
	        }
        }//Bug #42-----------------------------------------------------------------------
        // Lectura y escritura en streams
        String readTypeParam = getReadType(discriminant, nameWithPackage);
        String writeTypeParam = getWriteType(discriminant, nameWithPackage);
        buffer.append("\t\tstatic void read(::TIDorb::portable::InputStream& is, "
                      + readTypeParam + " _value);\n\n");
        buffer.append("\t\tstatic void write(::TIDorb::portable::OutputStream& os, "
                      + writeTypeParam + " _value);\n\n");

        // narrow para interfaces
        if (def.equals(OMG_interface))
            buffer.append("\t\tstatic "
                          + nameWithPackage
                          + "_ptr narrow(const ::CORBA::Object_ptr obj, bool is_a);\n\n");

        buffer.append("};// End of helper definition\n\n");

        return buffer.toString();
    }

    public static String generateCpp(Element type, Element decl,
                                     String genPackage, boolean noHolder)
        throws Exception
    {
        // genera implementacion de la clase helper correspondiente a
        // 'type' y 'decl'
        //      - 'decl' sera null, salvo que la llamada al m???todo proceda de
        // XmlTypedef2Cpp
        StringBuffer buffer = new StringBuffer();
        String name = "";
        Element discriminant = type;
        if (decl == null)
            name = type.getAttribute(OMG_name);
        else {
            name = decl.getAttribute(OMG_name);
            if (decl.getTagName().equals(OMG_array))
                discriminant = decl;
        }
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        String helperName = XmlType2Cpp.getHelperName(nameWithPackage);
        String holderName = XmlType2Cpp.getHolderName(nameWithPackage);

        // Inserci???n y extracci???n en Any - variables en n???mero (mapping
        // C++)
        String insertTypeParamCopy = getInsertType(discriminant,
                                                   nameWithPackage, true);
        String insertTypeParamNoCopy = getInsertType(discriminant,
                                                     nameWithPackage, false);
        String extractTypeParam = getExtractType(discriminant, nameWithPackage);
        String def = XmlType2Cpp.getDefinitionType(discriminant);
        //BUG #42-----------------------------------------------------------------
        if (!noHolder) {
            //BUG #42-----------------------------------------------------------------
	        if (!insertTypeParamCopy.equals("")) {
	            buffer.append("void " + helperName + "::insert(::CORBA::Any& any, "
	                          + insertTypeParamCopy + " _value) {\n");
	            generateInsertImplementation(discriminant, nameWithPackage,
	                                         holderName, buffer, true);
	            buffer.append("}\n\n");
	        }
	        if (!insertTypeParamNoCopy.equals("")) {
	            buffer.append("void " + helperName + "::insert(::CORBA::Any& any, "
	                          + insertTypeParamNoCopy + " _value, bool must_free) {\n");
	                          //jagd + insertTypeParamNoCopy + " _value) {\n");
	            generateInsertImplementation(discriminant, nameWithPackage,
	                                         holderName, buffer, false);
	            buffer.append("}\n\n");
	        }
	        if (!extractTypeParam.equals("")) {
	        	buffer.append("CORBA::Boolean " + helperName
        				+ "::extract(const ::CORBA::Any& any, ");
	        	// structs, exceptions, sequences, unions and valuetypes uses a const _value
	        	// Workaround "const const"
	        	if ((def.equals(OMG_struct)
	        	     || def.equals(OMG_exception)
					 || def.equals(OMG_sequence)
					 || def.equals(OMG_union)
					 || def.equals(OMG_valuetype))
					 && !extractTypeParam.startsWith("const "))
	        	{
		            buffer.append("const " + extractTypeParam + " _value) {\n");
	        	} else {
		            buffer.append(extractTypeParam + " _value) {\n");
	        	}

	            generateExtractImplementation(discriminant, buffer,
	                                          nameWithPackage, holderName);
	            buffer.append("}\n\n");
              
              //jagd optimizacion anadiendo extractores 
              if (def.equals(OMG_struct)
                          || def.equals(OMG_exception)
                          || def.equals(OMG_sequence)
                          || def.equals(OMG_union))
                      {
                          buffer.append("CORBA::Boolean "+ helperName+ "::extract(const ::CORBA::Any& any, "
                          +nameWithPackage+"*&"+ " _value){\n\n");
                          generateExtractImplementation_no_borra(discriminant,buffer, nameWithPackage,holderName);
	                        buffer.append("}\n\n");
 
                          buffer.append("CORBA::Boolean "+ helperName+ "::extract(const ::CORBA::Any& any, "
                          +nameWithPackage+"&"+ " _value){\n\n");
                          generateExtractImplementation_buffer(discriminant,buffer, nameWithPackage,holderName);
	                        buffer.append("}\n\n");
                      }
              //fin jagd

	        }
        }       //BUG #42-----------------------------------------------------------------
        // TypeCode
        buffer.append("CORBA::TypeCode_ptr " + helperName + "::type() {\n");
        generateTypeImplementation(discriminant, buffer, name, genPackage);
        buffer.append("}\n\n");

        String tc = genPackage.equals("") ? "_tc_" : genPackage + "::_tc_";
        buffer.append("const ::CORBA::TypeCode_ptr " + tc);
        buffer.append(name);
        buffer.append("=");
        buffer.append(helperName);
        buffer.append("::type();\n\n");

        // Lectura y escritura en streams
        buffer.append("void " + helperName
                      + "::read(::TIDorb::portable::InputStream& is, "
                      + getReadType(discriminant, nameWithPackage)
                      + " _value) {\n");
        generateReadImplementation(discriminant, genPackage, buffer);
        
        buffer.append("}\n\n");

        // narrow para interfaces
        if (def.equals(OMG_interface)) {
            buffer.append(nameWithPackage
                        + "_ptr "
                        + helperName
                        + "::narrow(const ::CORBA::Object_ptr obj, bool is_a) {\n");
            if (discriminant.getTagName().equals(OMG_interface)) {
                generateNarrowImplementation(discriminant, genPackage, buffer);
            } else {
                generateNarrowImplementation(decl, genPackage, buffer); 
                // typedef de un interfaz
            }
            buffer.append("}\n\n");
        }

        buffer.append("void " + helperName
                      + "::write(::TIDorb::portable::OutputStream& os, "
                      + getWriteType(discriminant, nameWithPackage)
                      + " _value) {\n");
        generateWriteImplementation(discriminant, buffer);
        buffer.append("}\n\n");

        return buffer.toString();
    }

    protected static String getInsertType(Element doc, String typeStr,
                                          boolean copy)
    {
        // devuelve el tipo asociado a doc como par???metro de un m???todo
        // insert de un helper
        // copy indica si es la inserci???n de copia, o no
        String ret = "";
        //String def = doc.getTagName();
        String def = XmlType2Cpp.getDefinitionType(doc); 
        // cubre tanto definiciones directas como typedefs
        
        if (def.equals(OMG_struct) || def.equals(OMG_union)
            || def.equals(OMG_exception) || def.equals(OMG_sequence)) {
            if (copy) {
                ret = "const " + typeStr + "&";
            } else {
                ret = typeStr + "*";
            }
        } else if (def.equals(OMG_interface)) {
            if (!doc.getAttribute(OMG_local).equals(OMG_true))
                if (copy) {
                    ret = typeStr + "_ptr";
                } else {
                    ret = typeStr + "_ptr*";
                }
        } else if (def.equals(OMG_valuetype)) {
            if (copy) {
                ret = typeStr + "*";
            } else {
                ret = typeStr + "**";
            }
        } else if (def.equals(OMG_array)) {
            if (copy)
                ret = "const " + typeStr + "_forany&";
        } else if (def.equals(OMG_kind)) {
            String kind = XmlType2Cpp.getDeepKind(doc);
            if (kind.equals(OMG_any)) {
                if (copy)
                    ret = "const " + typeStr + "&";
                else
                    ret = typeStr + "*";
            } else if (kind.equals(OMG_Object)) {
                if (copy)
                    ret = typeStr + "_ptr";
                else
                    ret = typeStr + "_ptr*";
            } else if (kind.equals(OMG_string)) {
                if (copy)
                    ret = "const " + XmlType2Cpp.basicMapping(OMG_string);
            } else if (kind.equals(OMG_wstring)) {
                if (copy)
                    ret = "const " + XmlType2Cpp.basicMapping(OMG_wstring);
            } else if (copy)
                ret = typeStr;

        } else if (def.equals(OMG_enum)) {
            if (copy)
                ret = typeStr;
        }
        return ret;
    }

    protected static String getExtractType(Element doc, String typeStr)
    {
        // devuelve el tipo asociado a doc como par???metro de un m???todo
        // extract de un helper
        String ret = "";
        //String def = doc.getTagName();
        String def = XmlType2Cpp.getDefinitionType(doc); 
        // cubre tanto definiciones directas como typedefs
        
        if (def.equals(OMG_struct) || def.equals(OMG_union)
            || def.equals(OMG_exception) || def.equals(OMG_sequence)) {
            ret = "const " + typeStr + "*&";
        } else if (def.equals(OMG_interface)) {
            if (!doc.getAttribute(OMG_local).equals(OMG_true))
                ret = typeStr + "_ptr&";
        } else if (def.equals(OMG_valuetype)) {
            ret = typeStr + "*&";
        } else if (def.equals(OMG_array)) {
            ret = typeStr + "_forany&";
        } else if (def.equals(OMG_kind)) {
            String kind = XmlType2Cpp.getDeepKind(doc);
            if (kind.equals(OMG_any))
                ret = "const " + typeStr + "*&";
            else if (kind.equals(OMG_Object))
                ret = typeStr + "_ptr&";
            else if (kind.equals(OMG_string))
                ret = XmlType2Cpp.basicMapping(OMG_string) + "&";
            else if (kind.equals(OMG_wstring))
                ret = XmlType2Cpp.basicMapping(OMG_wstring) + "&";
            else
                ret = typeStr + "&";

        } else if (def.equals(OMG_enum))
            ret = typeStr + "&";

        return ret;
    }

    protected static String getReadType(Element doc, String typeStr)
    {
        // devuelve el tipo asociado a doc como parametro del metodo de
        // lectura desde streams
        // ubicado en su helper
        String ret = "";
        //String def = doc.getTagName();
        String def = XmlType2Cpp.getDefinitionType(doc); 
        // cubre tanto definiciones directas como typedefs
        
        if (def.equals(OMG_struct) || def.equals(OMG_union)
            || def.equals(OMG_exception) || def.equals(OMG_sequence))
            ret = typeStr + "&"; // *&
        else if(def.equals(OMG_valuetype) )
            ret = typeStr + "*&"; 
        else if(doc.getAttribute(OMG_kind).equals(OMG_Object))
            ret = typeStr + "_ptr&"; 
        else if (def.equals(OMG_interface) )
            ret = typeStr + "_ptr&";
        else if (def.equals(OMG_array))
            ret = typeStr + "_slice*";
        else if (XmlType2Cpp.isAString(doc))
            ret = XmlType2Cpp.basicMapping(OMG_string) + "&";
        else if (XmlType2Cpp.isAWString(doc))
            ret = XmlType2Cpp.basicMapping(OMG_wstring) + "&";
        else
            // enums,...
            ret = typeStr + "&";
        return ret;
    }

    protected static String getWriteType(Element doc, String typeStr)
    {
        // devuelve el tipo asociado a doc como parametro del metodo de
        // escritura a streams ubicado en su helper
        String ret = "";
        String def = XmlType2Cpp.getDefinitionType(doc); 
        // cubre tanto definiciones directas como typedefs
        
        if (def.equals(OMG_struct) || def.equals(OMG_union)
            || def.equals(OMG_exception) || def.equals(OMG_sequence))
            ret = "const " + typeStr + "&";
        else if (def.equals(OMG_valuetype))
            ret = typeStr + "*";
        else if (def.equals(OMG_interface))
            ret = "const " + typeStr + "_ptr";
        else if (def.equals(OMG_array))
            ret = "const " + typeStr + "_slice*";
        else if (XmlType2Cpp.isAString(doc))
            ret = "const " + XmlType2Cpp.basicMapping(OMG_string);
        else if (XmlType2Cpp.isAWString(doc))
            ret = "const " + XmlType2Cpp.basicMapping(OMG_wstring);
        else if(doc.getAttribute(OMG_kind).equals(OMG_Object))
            ret = "const " + typeStr + "_ptr";
        else
            // enums, OMG_kind, ...
            ret = "const " + typeStr;
        return ret;
    }

    protected static void generateInsertImplementation(Element doc,
                                                       String nameWithPackage,
                                                       String holderName,
                                                       StringBuffer buffer,
                                                       boolean copy)
    throws Exception
    {
        // genera la implementacion de los m???todos insert de la clase
        // helper
        String def = XmlType2Cpp.getDefinitionType(doc);
        //jagd 
        boolean holder_ptr=false;
        if (def.equals(OMG_struct)
                          || def.equals(OMG_exception)
                          || def.equals(OMG_sequence)
                          || def.equals(OMG_union))
        {
          holder_ptr=true;
        } 
        //fin jagd

        if (def.equals(OMG_interface)) {
            buffer.append("\tTIDorb::portable::Any& delegate = any.delegate();\n");
            if (copy)
                buffer.append("\tdelegate.insert_Object((CORBA::Object_ptr)_value,"
                              + XmlType2Cpp.getTypecodeName(doc) +");\n");
            else {
                buffer.append("\tdelegate.insert_Object((CORBA::Object_ptr)(*_value), "
                              + XmlType2Cpp.getTypecodeName(doc) +");\n");
                buffer.append("\tCORBA::release(*_value);\n");
            }
        } else {
            buffer.append("\t::TIDorb::portable::Any& delegate=any.delegate(); \n");
            buffer.append("\tdelegate.insert_Streamable(new " + holderName
                          + "(");
            if (!copy&&!holder_ptr) //jagd
                buffer.append("*");
            buffer.append("_value");
            if (holder_ptr&&!copy)
            {
              buffer.append(", must_free"); 
            }

            buffer.append(" ));\n");
            if (!copy) {
                if (def.equals(OMG_valuetype))
                    buffer.append("\t(*_value)->_remove_ref();\n");
                else
                    if (!holder_ptr)
                      buffer.append("\tdelete _value;\n");
            } else if (def.equals(OMG_array))
                buffer.append("\tif (_value.nocopy()) " + nameWithPackage
                              + "_free((" + nameWithPackage
                              + "_forany)_value);\n");
        }
    }

    protected static void generateExtractImplementation(Element doc,
                                                        StringBuffer buffer,
                                                        String typeStr,
                                                        String holderName)
    {
        // genera la implementacion del m???todo extract de la clase helper
        String def = XmlType2Cpp.getDefinitionType(doc);
        String cast = "";

        if (def.equals(OMG_struct)
                || def.equals(OMG_union)
                || def.equals(OMG_exception)
                || def.equals(OMG_sequence)
				|| def.equals(OMG_valuetype)
				|| (def.equals(OMG_kind)
                && XmlType2Cpp.getDeepKind(doc).equals(OMG_any)))
            cast = "(" + typeStr + "*&)";
        else if (def.equals(OMG_interface))
        	cast = "(" + typeStr + "_ptr&)";
        else if (def.equals(OMG_enum) || XmlType2Cpp.isAString(doc) || XmlType2Cpp.isAWString(doc))
            cast = "(" + typeStr + "&)";

        if (def.equals(OMG_interface)) {
            buffer.append("\t::TIDorb::portable::Any& delegate = any.delegate();\n");
            buffer.append("\tCORBA::Object_var obj;\n");
            buffer.append("\tbool ret = delegate.extract_Object(obj);\n");
            
            buffer.append("\tif (ret){\n");
            buffer.append("\t\tif(CORBA::is_nil(obj)) {\n");
            buffer.append("\t\t\t" + cast + "_value = NULL;\n");
            buffer.append("\t\t} else {\n");
            buffer.append("\t\t\t" + cast + "_value = " + XmlType2Cpp.getHelperName(typeStr)
                          + "::narrow(obj,false);\n");
            buffer.append("\t\t\tret = ! CORBA::is_nil(_value);\n");
            buffer.append("\t\t}\n");
            buffer.append("\t}\n");
            buffer.append("\treturn ret;\n");
        } else {
            buffer.append("\t::TIDorb::portable::Any& delegate=any.delegate();\n");
            buffer.append("\tconst TIDorb::portable::Streamable* _holder;\n");
            buffer.append("\tif(delegate.extract_Streamable(_holder))\n\t{\n");
            buffer.append("\t\tconst " + holderName
                          + "* _hld=dynamic_cast< const " + holderName
                          + "*>(_holder);\n");
            buffer.append("\t\tif(_hld){\n");
// Bug #75
/*            if (def.equals(OMG_valuetype)){
            	buffer.append("\t\t\t_value = ((" + holderName + " *)_hld)->value;\n");
            	buffer.append("\t\t\tCORBA::add_ref(_value);\n");
            } else {*/
                buffer.append("\t\t\t" + cast + "_value = _hld->value;\n");                
/*            }*/
            
            buffer.append("\t\t\treturn true;\n");
            buffer.append("\t\t} else { \n");
            buffer.append("\t\t\treturn false;\n");
            buffer.append("\t\t}\n");
            
// Bug #75
/*            if (!def.equals(OMG_valuetype)) {*/
                buffer.append("\t} // Has streamable\n");
                buffer.append("\t");
                buffer.append(holderName);
                buffer.append("* _hld = new " + holderName + "();\n");
                buffer.append("\ttry {\n");
                buffer.append("\t\tCORBA::Boolean ret = delegate.set_Streamable(_hld);\n");
                buffer.append("\t\t" + cast + "_value = _hld->value;\n");  
                buffer.append("\t\tif (!ret) delete _hld;\n");               
                buffer.append("\t\treturn ret;\n");
                buffer.append("\t} catch (CORBA::BAD_OPERATION _e) {\n");
/*            } else {
            	buffer.append("\t\t}\n");
            	buffer.append("\t\t" + holderName + "* _hld = new " + holderName + "();\n");
            	buffer.append("\t\ttry {\n");
            	buffer.append("\t\t\tCORBA::Boolean ret = delegate.set_Streamable(_hld);\n");
            	buffer.append("\t\t\t_value = _hld->value;\n");
            	buffer.append("\t\t\tCORBA::add_ref(_value);\n");
            	buffer.append("\t\t\treturn ret;\n");
            	buffer.append("\t\t}catch (CORBA::BAD_OPERATION _e) {\n");
            	buffer.append("\t\t\treturn false;\n");
            	buffer.append("\t\t}\n");
            }
            if (!def.equals(OMG_valuetype)){*/
                buffer.append("\t\tdelete _hld;\n");
            	buffer.append("\t\treturn false;\n");
            	buffer.append("\t}\n");
/*            }*/
        }
    }
    
    //jagd 
    protected static void generateExtractImplementation_no_borra(Element doc,
                                                        StringBuffer buffer,
                                                        String typeStr,
                                                        String holderName)
    {
        // genera la implementacion del m???todo extract de la clase helper
        String def = XmlType2Cpp.getDefinitionType(doc);
        String cast = "";

        if (def.equals(OMG_struct)
                || def.equals(OMG_union)
                || def.equals(OMG_exception)
                || def.equals(OMG_sequence)
				|| def.equals(OMG_valuetype)
				|| (def.equals(OMG_kind)
                && XmlType2Cpp.getDeepKind(doc).equals(OMG_any)))
            cast = "(" + typeStr + "*&)";
        else if (def.equals(OMG_interface))
        	cast = "(" + typeStr + "_ptr&)";
        else if (def.equals(OMG_enum) || XmlType2Cpp.isAString(doc) || XmlType2Cpp.isAWString(doc))
            cast = "(" + typeStr + "&)";

        if (def.equals(OMG_interface)) {
            buffer.append("\t::TIDorb::portable::Any& delegate = any.delegate();\n");
            buffer.append("\tCORBA::Object_var obj;\n");
            buffer.append("\tbool ret = delegate.extract_Object(obj);\n");
            
            buffer.append("\tif (ret){\n");
            buffer.append("\t\tif(CORBA::is_nil(obj)) {\n");
            buffer.append("\t\t\t" + cast + "_value = NULL;\n");
            buffer.append("\t\t} else {\n");
            buffer.append("\t\t\t" + cast + "_value = " + XmlType2Cpp.getHelperName(typeStr)
                          + "::narrow(obj,false);\n");
            buffer.append("\t\t\tret = ! CORBA::is_nil(_value);\n");
            buffer.append("\t\t}\n");
            buffer.append("\t}\n");
            buffer.append("\treturn ret;\n");
        } else {
            buffer.append("\t::TIDorb::portable::Any& delegate=any.delegate();\n");
            buffer.append("\tTIDorb::portable::Streamable* _holder;\n");
            //arregla chapuza del GCC jagd
            //buffer.append("\tif(delegate.extract_Streamable((const TIDorb::portable::Streamable*)_holder))\n\t{\n");
            buffer.append("\tif(delegate.extract_Streamable((const TIDorb::portable::Streamable*&)_holder))\n\t{\n");
            buffer.append("\t\t" + holderName
                          + "* _hld=dynamic_cast< " + holderName
                          + "*>(_holder);\n");
            buffer.append("\t\tif(_hld){\n");
// Bug #75
/*            if (def.equals(OMG_valuetype)){
            	buffer.append("\t\t\t_value = ((" + holderName + " *)_hld)->value;\n");
            	buffer.append("\t\t\tCORBA::add_ref(_value);\n");
            } else {*/
                buffer.append("\t\t\t" + cast + "_value = _hld->value;\n");          
                buffer.append("\t\t\t _hld->must_free=0;\n");  // No borra    
/*            }*/
            
            buffer.append("\t\t\treturn true;\n");
            buffer.append("\t\t} else { \n");
            buffer.append("\t\t\treturn false;\n");
            buffer.append("\t\t}\n");
            
// Bug #75
/*            if (!def.equals(OMG_valuetype)) {*/
                buffer.append("\t} // Has streamable\n");
                buffer.append("\t");
                buffer.append(holderName);
                buffer.append("* _hld = new " + holderName + "();\n");
                buffer.append("\ttry {\n");
                buffer.append("\t\tCORBA::Boolean ret = delegate.set_Streamable(_hld);\n");
                buffer.append("\t\t" + cast + "_value = _hld->value;\n");                                     
                buffer.append("\t\tif (!ret) delete _hld;\n");
                buffer.append("\t\telse _hld->must_free=0;\n"); // no borra
                buffer.append("\t\treturn ret;\n");
                buffer.append("\t} catch (CORBA::BAD_OPERATION _e) {\n");
/*            } else {
            	buffer.append("\t\t}\n");
            	buffer.append("\t\t" + holderName + "* _hld = new " + holderName + "();\n");
            	buffer.append("\t\ttry {\n");
            	buffer.append("\t\t\tCORBA::Boolean ret = delegate.set_Streamable(_hld);\n");
            	buffer.append("\t\t\t_value = _hld->value;\n");
            	buffer.append("\t\t\tCORBA::add_ref(_value);\n");
            	buffer.append("\t\t\treturn ret;\n");
            	buffer.append("\t\t}catch (CORBA::BAD_OPERATION _e) {\n");
            	buffer.append("\t\t\treturn false;\n");
            	buffer.append("\t\t}\n");
            }
            if (!def.equals(OMG_valuetype)){*/
                buffer.append("\t\tdelete _hld;\n");
            	buffer.append("\t\treturn false;\n");
            	buffer.append("\t}\n");
/*            }*/
        }
    }
    
    protected static void generateExtractImplementation_buffer(Element doc,
                                                        StringBuffer buffer,
                                                        String typeStr,
                                                        String holderName)
    {
        // genera la implementacion del m???todo extract de la clase helper
        String def = XmlType2Cpp.getDefinitionType(doc);
        String cast = "";

        if (def.equals(OMG_struct)
                || def.equals(OMG_union)
                || def.equals(OMG_exception)
                || def.equals(OMG_sequence)
				|| def.equals(OMG_valuetype)
				|| (def.equals(OMG_kind)
                && XmlType2Cpp.getDeepKind(doc).equals(OMG_any)))
            cast = "(" + typeStr + "*&)";
        else if (def.equals(OMG_interface))
        	cast = "(" + typeStr + "_ptr&)";
        else if (def.equals(OMG_enum) || XmlType2Cpp.isAString(doc) || XmlType2Cpp.isAWString(doc))
            cast = "(" + typeStr + "&)";

        if (def.equals(OMG_interface)) {
            buffer.append("\t::TIDorb::portable::Any& delegate = any.delegate();\n");
            buffer.append("\tCORBA::Object_var obj;\n");
            buffer.append("\tbool ret = delegate.extract_Object(obj);\n");
            
            buffer.append("\tif (ret){\n");
            buffer.append("\t\tif(CORBA::is_nil(obj)) {\n");
            buffer.append("\t\t\t" + cast + "_value = NULL;\n");
            buffer.append("\t\t} else {\n");
            buffer.append("\t\t\t" + cast + "_value = " + XmlType2Cpp.getHelperName(typeStr)
                          + "::narrow(obj,false);\n");
            buffer.append("\t\t\tret = ! CORBA::is_nil(_value);\n");
            buffer.append("\t\t}\n");
            buffer.append("\t}\n");
            buffer.append("\treturn ret;\n");
        } else {
            buffer.append("\t::TIDorb::portable::Any& delegate=any.delegate();\n");
            buffer.append("\tconst TIDorb::portable::Streamable* _holder;\n");
            buffer.append("\tif(delegate.extract_Streamable(_holder))\n\t{\n");
            buffer.append("\t\tconst " + holderName
                          + "* _hld=dynamic_cast< const " + holderName
                          + "*>(_holder);\n");
            buffer.append("\t\tif(_hld){\n");
// Bug #75
/*            if (def.equals(OMG_valuetype)){
            	buffer.append("\t\t\t_value = ((" + holderName + " *)_hld)->value;\n");
            	buffer.append("\t\t\tCORBA::add_ref(_value);\n");
            } else {*/
                buffer.append("\t\t\t" + "_value = *_hld->value;\n");                
/*            }*/
            
            buffer.append("\t\t\treturn true;\n");
            buffer.append("\t\t} else { \n");
            buffer.append("\t\t\treturn false;\n");
            buffer.append("\t\t}\n");
            
// Bug #75
/*            if (!def.equals(OMG_valuetype)) {*/
                buffer.append("\t} // Has streamable\n");
                buffer.append("\t");
                buffer.append(holderName);
                buffer.append("* _hld = new " + holderName + "(&_value);\n");
                buffer.append("\ttry {\n");
                buffer.append("\t\tCORBA::Boolean ret = delegate.set_Streamable(_hld);\n");
                //jagd buffer.append("\t\t" + cast + "_value = _hld->value;\n");
                buffer.append("\t\tif (!ret) delete _hld;\n");
                buffer.append("\t\treturn ret;\n");
                buffer.append("\t} catch (CORBA::BAD_OPERATION _e) {\n");
/*            } else {
            	buffer.append("\t\t}\n");
            	buffer.append("\t\t" + holderName + "* _hld = new " + holderName + "();\n");
            	buffer.append("\t\ttry {\n");
            	buffer.append("\t\t\tCORBA::Boolean ret = delegate.set_Streamable(_hld);\n");
            	buffer.append("\t\t\t_value = _hld->value;\n");
            	buffer.append("\t\t\tCORBA::add_ref(_value);\n");
            	buffer.append("\t\t\treturn ret;\n");
            	buffer.append("\t\t}catch (CORBA::BAD_OPERATION _e) {\n");
            	buffer.append("\t\t\treturn false;\n");
            	buffer.append("\t\t}\n");
            }
            if (!def.equals(OMG_valuetype)){*/
                buffer.append("\t\tdelete _hld;\n");
            	buffer.append("\t\treturn false;\n");
            	buffer.append("\t}\n");
/*            }*/
        }
    }
    protected static void generateTypeImplementation(Element doc,
                                                     StringBuffer buffer,
                                                    String name,
													 String genPackage)
        throws Exception
    {
    	// PRA: checks whether or not _tc_* is already initialized
    	String tc = genPackage + "::_tc_" + name;
    	buffer.append("\tif (" + tc + ") {\n");
    	buffer.append("\t\treturn " + tc + ";\n");
    	buffer.append("\t}\n\n");
    	
    	// genera la implementacion del metodo type() de la clase helper
        String tagDef = doc.getTagName();
        if (tagDef.equals(OMG_enum)) {
            int numMembers = XmlType2Cpp.countMembers(doc);
            buffer.append("\tCORBA::EnumMemberSeq* members = new CORBA::EnumMemberSeq();\n");
            buffer.append("\tmembers->length(" + numMembers + ");\n");

            NodeList children = doc.getChildNodes();
            for (int i = 0; i < numMembers; i++) {
                Element el = (Element) children.item(i);
                String item = el.getAttribute(OMG_name);
         		buffer.append("\t(*members)[" + i + "] =  CORBA::string_dup(\""
         						+ item + "\");\n");                
            }
            buffer.append("\treturn TIDorb::portable::TypeCodeFactory::create_enum_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId )id(),");
            buffer.append("\n\t\t(const ::CORBA::Identifier ) \"" + name
                          + "\", members);\n");
        } else if (tagDef.equals(OMG_struct) || tagDef.equals(OMG_exception)) {
            int numMembers = XmlType2Cpp.countMembers(doc);
            buffer.append("\tCORBA::StructMemberSeq_ptr members = new ::CORBA::StructMemberSeq();\n");
            buffer.append("\tmembers->length(" + numMembers + ");\n");

            String objectName = "";
            Element type = null;
            int index = 0;
            NodeList children = doc.getChildNodes();

            for (int i = 0; i < children.getLength(); i++) {
                Element el = (Element) children.item(i);
                String tag = el.getTagName();
                if (tag.equals(OMG_simple_declarator) || tag.equals(OMG_array))
                    objectName = el.getAttribute(OMG_name);
                else {
                    type = el;
                    objectName = null;
                }
                if (objectName != null) {
                    buffer.append("\t(*members)["
                                  + index
                                  + "].name = (::CORBA::Identifier) CORBA::string_dup(\""
                                  + objectName + "\");\n");
                    buffer.append("\t(*members)[" + index + "].type = "                    			  
                                  + XmlType2Cpp.getTypecode(type) + ";\n");                    			  
                    buffer.append("\t(*members)[" + index
                                  + "].type_def = CORBA::IDLType::_nil();\n");
                    index++;
                }
            }

            buffer.append("\treturn TIDorb::portable::TypeCodeFactory::create_"
                          + tagDef + "_tc(\n");
            // 'def' vale "struct" (OMG_struct) o "exception"
            // (OMG_exception) 
            buffer.append("\t\t(const ::CORBA::RepositoryId ) id(),(const ::CORBA::Identifier ) \""
                        + name + "\", members);\n");

        } else if (tagDef.equals(OMG_union)) {
            int numMembers = XmlType2Cpp.countMembers(doc);
            buffer.append("\tCORBA::UnionMemberSeq_ptr members = new CORBA::UnionMemberSeq();\n");
            buffer.append("\tmembers->length(" + numMembers + ");\n\n");

            int index = 0;
            Union union = UnionManager.getInstance().get(doc);
            Vector switchBody = union.getSwitchBody();

            Element switch_el = (Element) doc.getFirstChild();
            Element discriminator_type = (Element) switch_el.getFirstChild();
            String discriminatorTypeCode = 
                XmlType2Cpp.getTypecode(discriminator_type);

            for (int i = 0; i < switchBody.size(); i++) {
                UnionCase union_case = (UnionCase) switchBody.elementAt(i);
                Element el = union_case.m_declarator;
                String objectName = el.getAttribute(OMG_name);
                Element type = union_case.m_type_spec;

                if (union_case.m_is_default) {
                    buffer.append("\t(*members)["
                                  + index
                                  + "].label <<= CORBA::Any::from_octet((CORBA::Octet)0);\n");
                    buffer.append("\t(*members)["
                                  + index
                                  + "].name = (::CORBA::Identifier) CORBA::string_dup(\""
                                  + objectName + "\");\n ");
                    buffer.append("\t(*members)[" + index + "].type = "                                  
                    			  + XmlType2Cpp.getTypecode(type) + ";\n");                    			  
                    buffer.append("\t(*members)[" + index
                                  + "].type_def = CORBA::IDLType::_nil();\n");
                    buffer.append("\n");
                    index++;
                } else {
                    Vector case_labels = union_case.m_case_labels;
                    for (int numCaseLabels = 0; numCaseLabels < case_labels
                        .size(); numCaseLabels++) {
                        Element expr = (Element) case_labels
                            .elementAt(numCaseLabels);
                        String value, cppType, helperType, insertion = "";
                        String anyVar = "(*members)[" + index + "].label";

                        if (discriminator_type.getTagName().equals(OMG_scoped_name)
                        		&& XmlType2Cpp.getDefinitionType(discriminator_type).equals(OMG_enum)) {
                            // typedef de tipo entero, caracter u octeto,
                            // o enum definido con anterioridad
                            value = expr.getAttribute(OMG_name);
                            if ((value == null) || value.equals("")) { 
                                // Tenemos un discriminator con scope
                                // pero asociado a un tipo b?sico
                                cppType = XmlType2Cpp.basicMapping(
                                      XmlType2Cpp.getDeepKind(discriminator_type));
                                Object obj_expr = XmlExpr2Cpp.getExpr(
                                      expr.getParentNode(), union.getDiscKind());
                                value = XmlExpr2Cpp.toString(obj_expr, cppType);
                            } else {
                                value = TypeManager.convert(value);
                            }

                            /*
                        	 *  Miramos si el tipo discriminante es un alias (typedef) en cuyo
                        	 *  caso buscamos el tipo "original"
                        	 */
                    		String typeToSearch = discriminator_type.getAttribute(OMG_name);
                            Element root = doc.getOwnerDocument().getDocumentElement();
                        	NodeList types = root.getElementsByTagName(OMG_typedef);

                    		for (int k = (types.getLength() - 1);k >= 0; k--){
                    			Element en = (Element) types.item(k);

                    			NodeList child = en.getChildNodes();    				    
                    			    
                    			Element firstNode = (Element) child.item(0);
                    			Element secondNode = (Element) child.item(1);

                    			if (secondNode.getAttribute(OMG_scoped_name).equals(typeToSearch)) {
                    				discriminator_type = firstNode;
                    				//typeToSearch = firstNode.getAttribute(OMG_scoped_name);
                    				typeToSearch = firstNode.getAttribute(OMG_name);
                    			}
                    		}
                    		//

                            helperType = XmlType2Cpp.getHelperType(discriminator_type);
                            insertion = helperType + "::insert("
                            + anyVar
                            + ", "
                            + value
                            + ")";
                        } else if (discriminator_type.getTagName().equals(OMG_enum)) {
                            // enum definido en el 'switch'
                            value = TypeManager.convert(expr.getAttribute(OMG_name));
                            helperType = XmlType2Cpp.getHelperType(discriminator_type);
                            insertion = helperType + "::insert("
                                        + anyVar
                                        + ", "
                                        + value
                                        + ")";
                        } else {
                            // DAVV - tipo entero, caracter u octeto
                            cppType = XmlType2Cpp.getType(discriminator_type);
                            String kind = XmlType2Cpp.getDeepKind(discriminator_type);
                            Object obj_expr = XmlExpr2Cpp.getExpr(expr
                                .getParentNode(), union.getDiscKind());
                            value = XmlExpr2Cpp.toString(obj_expr, kind /*cppType*/);
                            insertion = anyVar
                                        + " <<= "
                                        + XmlType2Cpp.getAnyInsertion(
                                                           discriminator_type,
                                                           "("
                                                           + cppType
                                                           + ")"
                                                           + value,
                                                           false);
                        }

                        buffer.append("\t" + insertion + ";\n");
                        buffer.append("\t(*members)["
                                      + index
                                      + "].name = (::CORBA::Identifier) CORBA::string_dup(\""
                                      + objectName + "\");\n");
                        buffer.append("\t(*members)[" + index + "].type = "
                                      + XmlType2Cpp.getTypecode(type) + ";\n");
                        buffer.append("\t(*members)[" + index
                                      + "].type_def = CORBA::IDLType::_nil();\n");
                        buffer.append("\n");
                        index++;
                    }
                }
            }

            buffer.append("\treturn TIDorb::portable::TypeCodeFactory::create_union_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId ) id(),"
            		      + "(const ::CORBA::Identifier ) \"" + name + "\", "
                          + discriminatorTypeCode
                          + ", members);\n");

        } else if (tagDef.equals(OMG_valuetype)) {
            int numMembers = XmlType2Cpp.countMembers(doc);
            String isAbstractS = doc.getAttribute(OMG_abstract);
            boolean isAbstract = (isAbstractS != null)
                                 && (isAbstractS.equals(OMG_true));
            String isCustomS = doc.getAttribute(OMG_custom);
            boolean isCustom = (isCustomS != null)
                               && (isCustomS.equals(OMG_true));
            String isTruncatableS = doc.getAttribute(OMG_truncatable);
            boolean isTruncatable = (isTruncatableS != null)
                                    && (isTruncatableS.equals(OMG_true));

            buffer.append("\tCORBA::ValueMemberSeq* members = new ::CORBA::ValueMemberSeq;\n");
            buffer.append("\tmembers->length(" + numMembers + ");\n");

            int index = 0;
            NodeList preChildren = doc.getChildNodes();

            for (int i = 0; i < preChildren.getLength(); i++) {
                Element preEl = (Element) preChildren.item(i);
                String preTag = preEl.getTagName();
                if (preTag.equals(OMG_state_member)) {
                    Element type = (Element) preEl.getFirstChild();
                    NodeList children = preEl.getChildNodes();
                    for (int j = 1; j < children.getLength(); j++) {
                        // Element decl = (Element) preEl.getLastChild();
                        Element decl = (Element) children.item(j);
                        String objectName = null;
                        objectName = decl.getAttribute(OMG_name);

                        buffer.append("\t(*members)["
                                      + index
                                      + "].name = (::CORBA::Identifier) CORBA::string_dup(\""
                                      + objectName + "\");\n");
                        buffer.append("\t(*members)[" + index + "].type = "                        			  
                        		      + XmlType2Cpp.getTypecode(type) + ";\n");                        			  
                        buffer.append("\t(*members)[" + index
                                      + "].type_def = CORBA::IDLType::_nil();\n"); 
                        // cambiado NULL por _nil()
                        
                        buffer.append("\t(*members)[" + index
                                      + "].access = (CORBA::Visibility) ");
                        boolean isPublic = preEl
                            .getAttribute(Idl2XmlNames.OMG_kind)
                            .equals(Idl2XmlNames.OMG_public);
                        if (isPublic)
                            buffer.append("CORBA::PUBLIC_MEMBER");
                        else
                            buffer.append("CORBA::PRIVATE_MEMBER");
                        buffer.append(";\n");
                        index++;
                    }
                }
            }

            buffer.append("\treturn TIDorb::portable::TypeCodeFactory::create_value_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId) id()," +
            		      "(const ::CORBA::Identifier) \"" + name + "\"");
            buffer.append(",(::CORBA::ValueModifier) ");
            if (isAbstract)
                buffer.append("CORBA::VM_ABSTRACT");
            else if (isCustom)
                buffer.append("CORBA::VM_CUSTOM");
            else if (isTruncatable)
                buffer.append("CORBA::VM_TRUNCATABLE");
            else
                buffer.append("CORBA::VM_NONE");
            Element inheritance = (Element) doc.getFirstChild();
            String concrete_base = "CORBA::TypeCode::_nil()";
            if (!isAbstract
                && inheritance.getTagName().equals(OMG_value_inheritance_spec)) {
                NodeList parents = inheritance.getChildNodes();
                for (int i = 0; i < parents.getLength(); i++) {
                    Element parent = (Element) parents.item(i);
                    if (parent.getTagName().equals(OMG_scoped_name)) {
                        String inheritedScope = parent.getAttribute(OMG_name);
                        Scope inhScope = Scope.getGlobalScopeInterface(
                                           inheritedScope);
                        Element elfather = inhScope.getElement();
                        if (elfather.getTagName().equals(OMG_valuetype)
                            && !elfather.getAttribute(OMG_abstract).equals(OMG_true)) {
                            concrete_base = XmlType2Cpp.getHelperName(
                                parent.getAttribute(OMG_name)) + "::type()";
                            break;
                        }
                    }
                }
            }
            buffer.append(", " + concrete_base);
            buffer.append(", members);\n");

        } else if (tagDef.equals(OMG_interface)) {
            buffer.append("\treturn TIDorb::portable::TypeCodeFactory::create_interface_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId) id()," +
            		           "(const ::CORBA::Identifier) \"" + name + "\");\n");            
        } else if (tagDef.equals(OMG_array)) {
            buffer.append("\tCORBA::TypeCode_ptr original_type = ");
            NodeList indexChilds = doc.getChildNodes();
            for (int k = 0; k < indexChilds.getLength(); k++) {
                Element indexChild = (Element) indexChilds.item(k);
                buffer.append("TIDorb::portable::TypeCodeFactory::create_array_tc(");
                buffer.append(XmlExpr2Cpp.getIntExpr(indexChild) + ",\n\t\t");
            }
            buffer.append(XmlType2Cpp.getTypecode(
                (Element) doc.getParentNode().getFirstChild()));
            for (int k = 0; k < indexChilds.getLength(); k++) {
                buffer.append(")");
            }
            buffer.append(";\n");
            buffer.append("\treturn  TIDorb::portable::TypeCodeFactory::create_alias_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId )id(),\n");
            buffer.append("\t\t(const ::CORBA::Identifier ) \"" + name
                          + "\", original_type);\n");
        } else if (tagDef.equals(OMG_sequence)) {
            //          Comprobamos si es una Bounded Sequence..
            String bounds = "";
            if (doc.getChildNodes().getLength() > 1) {
                Element el = (Element) doc.getLastChild();
                if (el != null) {
                    Element expr = (Element) el.getFirstChild();
                    if (expr != null)
                        bounds = "" + XmlExpr2Cpp.getIntExpr(expr);
                }
            }
            
            buffer.append("\tCORBA::TypeCode_ptr original_type = \n");
            
            Element child = (Element) doc.getFirstChild();
            buffer.append("\t\tTIDorb::portable::TypeCodeFactory::create_sequence_tc(");
            
            if(bounds.equals("")) {
                buffer.append("0,\n");               
            } else {
                buffer.append(bounds +",\n" );
            }
            
        	buffer.append("\t\t\t" +XmlType2Cpp.getTypecode(child) + ");\n");       	   
            
            buffer.append("\treturn  TIDorb::portable::TypeCodeFactory::create_alias_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId )id(),\n");
            buffer.append("\t\t(const ::CORBA::Identifier ) \"" + name
                          + "\", original_type);\n");
        } else /*
                * if (tagDef.equals(OMG_sequence) ||
                * tagDef.equals(OMG_scoped_name)) - DAVV - o cualquier otra cosa
                * (typedef)
                */{
        	
            buffer.append("\treturn  TIDorb::portable::TypeCodeFactory::create_alias_tc(\n");
            buffer.append("\t\t(const ::CORBA::RepositoryId )id(),\n");
            buffer.append("\t\t(const ::CORBA::Identifier ) \"" + name + "\", "
            			  + XmlType2Cpp.getTypecode(doc) + ");\n");
             

        }
    }

    protected static void generateNarrowImplementation(Element doc,
                                                       String genPackage,
                                                       StringBuffer buffer)
    {
        String name = doc.getAttribute(OMG_name);
        String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                + "::" + name;
        String stub = "_" + name + "Stub";
        stub = genPackage.equals("") ? stub : genPackage + "::" + stub;
        boolean isLocal = doc.getAttribute(OMG_local).equals(OMG_true);

        buffer.append("\tif (CORBA::is_nil(obj))\n");
        buffer.append("\t\treturn NULL;\n");
        buffer.append("\t" + nameWithPackage
                      + "_ptr _concrete_ref = dynamic_cast<" + nameWithPackage
                      + "_ptr> (obj);\n ");
        if (!isLocal)
            buffer.append("\tif (!CORBA::is_nil(_concrete_ref))\n\t");
        buffer.append("\treturn " + nameWithPackage
                      + "::_duplicate(_concrete_ref);\n");
        if (!isLocal) { // los objetos locales no tienen stubs, ni
                        // necesitan de unchecked_narrow (is_a == TRUE) puesto
                        // que no han de conectar con ning???n servidor
            buffer.append("\tif (is_a || obj->_is_a(id())) {\n");
            buffer.append("\t\t" + stub + "* result = new " + stub + "();\n");
            buffer.append("\t\tif(result==NULL) \n");
            buffer.append("\t\t\tthrow ::CORBA::NO_MEMORY();\n");
            buffer.append("\t\t::TIDorb::portable::Stub* __aux= dynamic_cast< ::TIDorb::portable::Stub*>(obj);\n ");
            buffer.append("\t\tif (__aux!=NULL) {\n");
            buffer.append("\t\t\tresult->_set_delegate(__aux->_get_delegate());\n");
            buffer.append("\t\t\treturn (" + nameWithPackage + "_ptr)result;\n");
            buffer.append("\t\t}\n");
            buffer.append("\t}\n");
        }
        //buffer.append("\tif (!is_a) throw ::CORBA::BAD_PARAM();\n");
        buffer.append("\treturn " + nameWithPackage + "::_nil();\n");
    }


    protected static void generateReadImplementation(Element doc,
                                                     String genPackage,
                                                     StringBuffer buffer)
        throws Exception

    {
      generateReadImplementation(doc,genPackage,buffer,"_value");
    }

    protected static void generateReadImplementation(Element doc,
                                                     String genPackage,
                                                     StringBuffer buffer,
                                                     String elvalue)
        throws Exception
    {
        String tagDef = doc.getTagName();

        if (tagDef.equals(OMG_enum)) {
            if (CompilerConf.getEnumCheck()) {
                String small = 
                    ((Element) doc.getFirstChild()).getAttribute(OMG_name);
                Object big = ((Element) doc.getLastChild()).getAttribute(OMG_name);
                if (!genPackage.equals("")) {
                    small = genPackage + "::" + small;
                    big = genPackage + "::" + big;
                }
                buffer.append("\tCORBA::ULong _tmp;\n");
                buffer.append("\tis.read_ulong(_tmp);\n");
                buffer.append("\tif (_tmp <= " + big+ ")\n");
                buffer.append("\t\treinterpret_cast< ::CORBA::ULong& >("+elvalue+") = _tmp;\n");
                buffer.append("\telse\n");
                buffer.append("\t\tthrow CORBA::MARSHAL();\n");
            } else {
                buffer.append("\tCORBA::ULong _tmp;\n");
                buffer.append("\tis.read_ulong(_tmp);\n");
                buffer.append("\t\treinterpret_cast< ::CORBA::ULong& >("+elvalue+") = _tmp;\n");
            }

        } else if (tagDef.equals(OMG_struct) || tagDef.equals(OMG_exception)) {

            if(tagDef.equals(OMG_exception)) {
                buffer.append("\tCORBA::String_var ex_id;\n");
                buffer.append("\tis.read_string(ex_id.out());\n");
                buffer.append("\tif(strcmp(ex_id.in(), id())) {\n");
                buffer.append("\t\tthrow CORBA::MARSHAL();\n");
                buffer.append("\t}\n");                    
            }
            
            NodeList children = doc.getChildNodes();
            String reader = "", def = "", kind = "", typename = "";
            Element type = null;
            for (int i = 0; i < children.getLength(); i++) {
                Element el = (Element) children.item(i);
                String elTag = el.getTagName();
                String member = "";
                if (!elTag.equals(OMG_simple_declarator)
                    && !elTag.equals(OMG_array)) {
                    reader = XmlType2Cpp.getTypeReader(el, "is");
                    def = XmlType2Cpp.getDefinitionType(el);
                    typename = el.getAttribute(OMG_name);
                    if (def.equals(OMG_kind))
                        kind = XmlType2Cpp.getDeepKind(el);
                    else
                        kind = "";
                    type = el;
                } else { // declaracion: lectura de un miembro
                    member = el.getAttribute(OMG_name);
                   
                      if (kind.equals(OMG_string)
                             || kind.equals(OMG_wstring)) {
                        // liberaremos la memoria del interior del String_var antes
                        // de pasarselo a read_string porque es mas eficiente
                        // que crear un puntero temporal, darselo a read_string
                        // copiarlo y liberarlo
                        if (type.getTagName().equals(OMG_type))
                            buffer.append("\tCORBA::" + kind + "_free("+elvalue+"."
                                          + member + ");\n");
                        	buffer.append("\t" + reader + elvalue+"." + member
                        					+ ");\n");
                        	if (type.getFirstChild() != null) {
                        		buffer.append("\tCORBA::ULong _length_" + member
                        					  	+ " = 0;\n");
                        		buffer.append("\twhile("+elvalue+"." + member
                        						+ "[_length_" + member
                        						+ "] != NULL)\n");
                        		buffer.append("\t\t_length_" + member + "++;\n");
                        		buffer.append("\tif(_length_"
                        						+ member
                        						+ " > "
                        						+ XmlExpr2Cpp.getIntExpr(type
                        								.getFirstChild()) + ")\n");
                        		buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                        	}
                      	} else {
                      		 if (def.equals(OMG_valuetype)){
                      			 /*
                      			 buffer.append("//alvaro: TagName: " + type.getTagName() + " \n");
                      			 buffer.append("// Definition type: " + XmlType2Cpp.getDefinitionType(el) + "\n");
                      			 buffer.append("// TypeReader: " + XmlType2Cpp.getTypeReader(el, "is") + "\n");
                      			 buffer.append("// Atribute: " + el.getAttribute(OMG_name) + "\n");
                      			 buffer.append("// Helpertype " + XmlType2Cpp.getHelperType(el) +  
                      				el.getAttribute(OMG_type) + " " + 
                      				XmlType2Cpp.getTypeName(el) + " " +
                      				XmlType2Cpp.isAnValuetype(el) + " " +
                      				XmlType2Cpp.getMemberType(el) + " " +
                      				genPackage + " " + 
                      				XmlType2Cpp.getTypeReader(el, "is") + " " + i +
                      				//el.getChildNodes().item(0).getNodeType() + //.equalsIgnoreCase(Idl2XmlNames.OMG_valuetype) +
                      				"\n");
                      			  */
                      			buffer.append("\t" + reader + "(" + typename + "*&)*(_value." + member
                                        + "));\n");
                      			 
                      		 }
                      		 else
                                     if (kind.equals(OMG_fixed)) {
                                         // Anonymous types are not supported (Deprecated) 
                                         // long digits = XmlExpr2Cpp.getIntExpr(children.getFirstChild());
                                         // long scale = XmlExpr2Cpp.getIntExpr(doc.getLastChild());
                                         buffer.append("\t" + reader + "_value." + member + ");\n");
                                         //member + ", " + digits + ", " + scale + ");\n");
                                     } else {
                                         buffer.append("\t" + reader + "_value." + member + ");\n");
                                     }
                      	}
                }
            }
        } else if (tagDef.equals(OMG_union)) {
            Union union = UnionManager.getInstance().get(doc);
            Vector switchBody = union.getSwitchBody();

            Element switch_el = (Element) doc.getFirstChild();
            Element discriminator_type = (Element) switch_el.getFirstChild();

            // Read the discriminator from the InputStream
            String read_discrim = XmlType2Cpp.getTypeReader(discriminator_type, "is");
            String discriminatorType = XmlType2Cpp.getType(discriminator_type);
            
            buffer.append("\n\t" + discriminatorType + " disc;\n");

            buffer.append("\t" + read_discrim + "disc);\n\n");
            
            // Read the selected member from the InputStream, according with the
            // discriminator value
            UnionCase union_case = 
                (UnionCase) switchBody.elementAt(switchBody.size() - 1);
            StringBuffer realBuffer = buffer;
            StringBuffer accumBuffer = new StringBuffer();
            StringBuffer defaultBuffer = new StringBuffer();

            buffer.append("\t");
            for (int i = 0; i < switchBody.size(); i++) {
                // bucle de elementos miembro
                buffer = new StringBuffer();
                union_case = (UnionCase) switchBody.elementAt(i);
                Vector case_labels = union_case.m_case_labels;
                int j; // fuera del bucle, se usa mas abajo...
                for (j = 0; j < case_labels.size(); j++) {
                    // bucle de etiquetas 'case' para cada elemento
                    Element label = (Element) case_labels.elementAt(j);

                    // lectura de la etiqueta 
                    String value = null, cppType = "";
                    if (discriminator_type.getTagName().equals(OMG_scoped_name)) {
                        // typedef de tipo entero, caracter u octeto, o
                        // enum definido con anterioridad
                        value = label.getAttribute(OMG_name);
                        if ((value == null) || value.equals("")) { 
                            // Tenemos un discriminator con scope
                            // pero asociado a un tipo b?sico
                            cppType = XmlType2Cpp.basicMapping(
                                XmlType2Cpp.getDeepKind(discriminator_type));
                            Object obj_expr = XmlExpr2Cpp.getExpr(
                                label.getParentNode(), union.getDiscKind());
                            value = XmlExpr2Cpp.toString(obj_expr, cppType);
                        } else {
                            value = TypeManager.convert(value);
                        }
                    } else if (discriminator_type.getTagName().equals(OMG_enum)) {
                        // enum definido en el 'switch'
                        value = TypeManager.convert(
                                    label.getAttribute(OMG_name));
                    } else {
                        // DAVV - tipo entero, caracter u octeto
                        cppType = XmlType2Cpp.getType(discriminator_type);
                        Object obj_expr = XmlExpr2Cpp.getExpr(
                                                 label.getParentNode(),
                                                 union.getDiscKind());
                        value = XmlExpr2Cpp.toString(obj_expr, cppType);
                    }

                    // condicion para cada 'case'
                    if (j == 0) {
                        //buffer.append("\n\t");
                        if (!accumBuffer.toString().equals(""))
                            buffer.append(" else ");
                        buffer.append("if (");
                    } else
                        buffer.append("||");
                    buffer.append("(disc == " + value + ")");
                    if (j == case_labels.size() - 1)
                        buffer.append(") {\n");
                }

                // condicion 'else' para el default (si lo hay)
                if (union_case.m_is_default)
                    buffer.append(" else {\n");

                // Lectura del miembro
                Element type = union_case.m_type_spec;
                Element decl = union_case.m_declarator;
                String typeStr = XmlType2Cpp.getType(type);
                String defName = decl.getAttribute(OMG_name);
                String def = XmlType2Cpp.getDefinitionType(type);
                String read_def = XmlType2Cpp.getTypeReader(type, "is");

                // variable temporal para lectura
                if (def.equals(OMG_array)) {
                    //PRA buffer.append("\t\t" + typeStr + "_forany _tmp;\n");
                    buffer.append("\t\t" + typeStr + " _tmp;\n");
                } else if ((def.equals(OMG_interface)) 
                           || (def.equals(OMG_valuetype))) {
                    buffer.append("\t\t" + typeStr + "_var _tmp;\n");                                               
                } else {
                	if (typeStr.equals("char*"))
                		buffer.append("\t\t" + typeStr + " _tmp = NULL;\n");
                	else
                		buffer.append("\t\t" + typeStr + " _tmp;\n");
                }
                
                if ((def.equals(OMG_interface)) 
                    || (def.equals(OMG_valuetype))) {                    
                    buffer.append("\t\t" + read_def + "_tmp.out());\n");                              
                }  else {
                    buffer.append("\t\t" + read_def + "_tmp);\n");
                }
                // DAVV - control de longitud en cadenas limitadas
                if (def.equals(OMG_kind)
                    && (XmlType2Cpp.isAString(type) 
                    || XmlType2Cpp.isAWString(type)) 
                    && type.getFirstChild() != null) {
                    buffer.append("\t\tCORBA::ULong _length = 0;\n");
                    buffer.append("\t\twhile(_tmp[_length] != NULL)\n");
                    buffer.append("\t\t\t_length++;\n");
                    buffer.append("\t\tif(_length > "
                                  + XmlExpr2Cpp.getIntExpr(type.getFirstChild())
                                  + ")\n");
                    buffer.append("\t\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                }

                // DAVV - uso de los modificadores de la uni???n para asignarle el
                // valor leido
                if (j > 1) { // DAVV - miembro con varios posibles valores para
                             // el discriminante
                    if ((def.equals(OMG_interface)) 
                        || (def.equals(OMG_valuetype))) {
                        buffer.append("\t\t"+elvalue+"." + defName
                                      + "(disc, _tmp.in());\n");        
                    } else
                        buffer.append("\t\t"+elvalue+"." + defName
                                      + "(disc,_tmp);\n");
                } else { // miembro con un unico valor para el
                         // discriminante
                    if ((def.equals(OMG_interface)) 
                        || (def.equals(OMG_valuetype))) {
                        buffer.append("\t\t"+elvalue+"." + defName + "(_tmp.in());\n"); 
                    } else
                        buffer.append("\t\t"+elvalue+"." + defName + "(_tmp);\n");
                }

                if (union_case.m_is_default) 
                    buffer.append("\t\t_value._d(disc);\n");
                
                buffer.append("\t}");

                if (union_case.m_is_default) {
                	buffer.append("\n");
                	defaultBuffer = buffer; 
                	// se esta 'guardando' para el final
                } else {
                    accumBuffer.append(buffer); // se acumula el c???digo
                                                // correspondiente a cada
                                                // miembro, dejando aparte el de
                                                // 'default' (para el final)
                }
            }

            // default 'automatico'
            if (!union.getHasDefault() && union.getDefaultAllowed()) {
                defaultBuffer.append("\n\telse {\n");
                defaultBuffer.append("\t\t"+elvalue+"._default(disc);\n");
                defaultBuffer.append("\t}\n");
            }
            realBuffer.append(accumBuffer);
            realBuffer.append(defaultBuffer);
            buffer = realBuffer;

        } else if (tagDef.equals(OMG_valuetype)) {
        	buffer.append("\tCORBA::ValueBase_var aux_value;\n");
        	buffer.append("\tis.read_Value(aux_value.out());\n");
        	buffer.append("\tif(aux_value.in() != NULL) {\n");
        	buffer.append("\t\t"+elvalue+" = " +genPackage + "::" + doc.getAttribute(OMG_name)+ "::_downcast(aux_value);\n");
        	buffer.append("\t\tif("+elvalue+" == NULL) {\n");
        	buffer.append("\t\t\tthrow CORBA::MARSHAL();\n");
        	buffer.append("\t\t}\n");
        	buffer.append("\t} else {\n");
        	buffer.append("\t\t"+elvalue+" = NULL;\n");
        	buffer.append("\t}\n");            
        } else if (tagDef.equals(OMG_interface)) {
            if (!doc.getAttribute(OMG_local).equals(OMG_true)) {
                buffer.append("\t::CORBA::Object_ptr obj;\n");
                buffer.append("\tis.read_Object(obj);\n");
                buffer.append("\t"+elvalue+" = " + XmlType2Cpp.getHelperType(doc)
                              + "::narrow(obj, true); \n");
            } else
                buffer.append("\tthrow CORBA::MARSHAL(4,CORBA::COMPLETED_NO);\n");
        } else if (tagDef.equals(OMG_array)) {
            NodeList indexes = doc.getChildNodes();
            String ident = "\t";
            String indexesStr = "";
            
            // DAVV - lectura de elemento
            Element type = (Element) doc.getParentNode().getFirstChild();
            String reader = XmlType2Cpp.getTypeReader(type, "is");
 
            if (XmlType2Cpp.isAString(type)
                || XmlType2Cpp.isAWString(type)) {
                // liberaremos la
                // memoria del interior del String_var antes
                // de pasarselo a read_string porque es mas eficiente que crear
                // un puntero temporal, darselo a read_string
                // copiarlo y liberarlo
            		//            	 bucle para recorrido de elementos
                for (int i = 0; i < indexes.getLength(); i++) {
                    buffer.append(ident + "for (CORBA::ULong i" + i + "=0; i" + i
                                  + "<" + XmlExpr2Cpp.getIntExpr(indexes.item(i))
                                  + "; i" + i + "++) {\n");
                    ident += "\t";
                    indexesStr += "[i" + i + "]";
                }	
            	
                if (type.getTagName().equals(OMG_type))
                    buffer.append(ident + "CORBA::"
                                  + XmlType2Cpp.getDeepKind(type)
                                  + "_free(("+elvalue+")" + indexesStr + ");\n");
                // FIX bug [#389] Incorrect implementation of 'read' helper method for string arrays
                //buffer.append(ident + reader + "("+elvalue+")" + indexesStr + ");\n");
                if (type.getFirstChild() != null) {
                    buffer.append(ident + "CORBA::ULong _strLength = 0;\n");
                    buffer.append(ident + "while(("+elvalue+")" + indexesStr
                                  + "[_strLength] != NULL)\n");
                    buffer.append(ident + "\t_strLength++;\n");
                    buffer.append(ident + "if(_strLength > "
                                  + XmlExpr2Cpp.getIntExpr(type.getFirstChild())
                                  + ")\n");
                    buffer.append(ident
                                  + "\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                                       
                }
                buffer.append(ident + reader + "("+elvalue+")" + indexesStr + ");\n");
                
                //              	cierre de los bucles
                for (int i = 0; i < indexes.getLength(); i++) {
                    ident = ident.substring(1);
                    buffer.append(ident + "}\n");
                }
            } else{
            		if (reader.equals("is.read_octet(")){	
            			for (int i = 0; i < indexes.getLength(); i++) {
            				buffer.append("\tis.read_octet_array(_value, 0," +
            								XmlExpr2Cpp.getIntExpr(indexes.item(i)) + ");\n");
            			}
            		} else {
            			// bucle para recorrido de elementos
                    for (int i = 0; i < indexes.getLength(); i++) {
                    	    buffer.append(ident + "for (CORBA::ULong i" + i + "=0; i" + i
                                      + "<" + XmlExpr2Cpp.getIntExpr(indexes.item(i))
                                      + "; i" + i + "++) {\n");
                        ident += "\t";
                        indexesStr += "[i" + i + "]";
                    }	
            			            			
                    buffer.append(ident + reader + "(_value)" + indexesStr + ");\n");
            
                    // cierre de los bucles
                    for (int i = 0; i < indexes.getLength(); i++) {
                    		ident = ident.substring(1);
                    		buffer.append(ident + "}\n");
                    }            
            		}
            }
        } else if (tagDef.equals(OMG_sequence)) {
            // longitud y control de limites
            buffer.append("\tCORBA::ULong _length;\n");
            buffer.append("\tis.read_ulong(_length);\n");
            Element bound = (Element) doc.getFirstChild().getNextSibling();
            if (bound != null && bound.getTagName().equals(OMG_expr)) {
                buffer.append("\tif (_length > "
                              + XmlExpr2Cpp.getIntExpr(bound) + ")\n");
                buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Invalid sequence length\");\n");
            }
            
            buffer.append("\t"+elvalue+".length(_length);\n");
        
            Element type = (Element) doc.getFirstChild(); // s???lo ser???
                                                          // posible tener
                                                          // valores de etiqueta
                                                          // 'OMG_type' o
                                                          // 'OMG_scoped'

            //jagd 
            if (!XmlType2Cpp.isAString(type)
                     &&! XmlType2Cpp.isAWString(type)) 
            {
              String tagDef2 = XmlType2Cpp.getDefinitionType(type);
              if(tagDef2.equals(OMG_struct))
              {
                String type_pointer = XmlType2Cpp.getTypeHelper(type);
                buffer.append("\t" + type_pointer + "*" + " _buffer="+elvalue+".get_buffer();\n");
              }  
            }

            
            // DAVV - lectura de elementos
                        
            String reader = XmlType2Cpp.getTypeReader(type, "is");
  
            if (XmlType2Cpp.isAString(type)
                     || XmlType2Cpp.isAWString(type)) {
                // DAVV - aunque no sea de buena educacion, liberaremos la
                // memoria del interior del String_var antes
                // de pasarselo a read_string porque es mas eficiente que crear
                // un puntero temporal, darselo a read_string
                // copiarlo y liberarlo
            	
            	    buffer.append("\tfor (CORBA::ULong i=0; i<_length; i++) {\n");	
            	
                if (type.getTagName().equals(OMG_type)) {
                    buffer.append("\t\tCORBA::" + XmlType2Cpp.getDeepKind(type)
                                  + "_free("+elvalue+"[i]);\n");
                }
                if (reader.endsWith("is.read_Object(")){    				
    				reader = reader + "(CORBA::Object_ptr&)";
    			}
                buffer.append("\t\t" + reader + elvalue+"[i]);\n");
                if (type.getFirstChild() != null) {
                    buffer.append("\t\tCORBA::ULong _strLength = 0;\n");
                    buffer.append("\t\twhile("+elvalue+"[i][_strLength] != NULL)\n");
                    buffer.append("\t\t\t_strLength++;\n");
                    buffer.append("\t\tif(_strLength > "
                                  + XmlExpr2Cpp.getIntExpr(type.getFirstChild())
                                  + ")\n");
                    buffer.append("\t\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                }
                buffer.append("\t}\n");
            } else {
            		if (reader.equals("is.read_octet("))
            			buffer.append("\tis.read_octet_array(_value.get_buffer(false), 0, _length);\n");
            		else{
            			buffer.append("\tfor (CORBA::ULong i=0; i<_length; i++) {\n");
              		    //jagd 
	                    String tagDef2 = XmlType2Cpp.getDefinitionType(type);
	                    if(tagDef2.equals(OMG_struct))
                 		{
                   			buffer.append("\t\t" + reader + "*_buffer);\n");
                   			buffer.append("\t\t_buffer++;\n");
                 		}
                 		else {
            				if (reader.endsWith("is.read_Object(")){
                    			reader = reader + "(CORBA::Object_ptr&)";
            				}
            				buffer.append("\t\t" + reader + elvalue+"[i]);\n");
            			}
						buffer.append("\t}\n");
            	    }
            }
            //buffer.append("\t}\n");
        } else /*
                * || tagDef.equals(OMG_scoped_name))
                */{
            //jagd
            String kind="";
            String type=""; 
            type = XmlType2Cpp.getDefinitionType(doc);
            if (type.equals(OMG_kind))
              kind = XmlType2Cpp.getDeepKind(doc);
            else
              kind = "";
        

            String reader = XmlType2Cpp.getTypeReader(doc, "is");
            if (!XmlType2Cpp.isABasicType(doc)
                && !XmlType2Cpp.isAnInterface(doc)
                && !XmlType2Cpp.isAnArray(doc)) // DAVV - otro helper de por
                                                // medio; quiere un *
                buffer.append("\t" + reader + elvalue+");\n");
            else if (XmlType2Cpp.isAString(doc) || XmlType2Cpp.isAWString(doc)) {
                // liberaremos la
                // memoria del interior del String_var antes
                // de pasarselo a read_string porque es mas eficiente que crear
                // un puntero temporal, darselo a read_string
                // copiarlo y liberarlo
                if (doc.getTagName().equals(OMG_type))
                    buffer.append("\tCORBA::" + XmlType2Cpp.getDeepKind(doc)
                                  + "_free("+elvalue+");\n");
                buffer.append("\t" + reader + elvalue+");\n");
                if (doc.getFirstChild() != null) {
                    buffer.append("\tCORBA::ULong _strLength = 0;\n");
                    buffer.append("\twhile(("+elvalue+")[_strLength] != NULL)\n");
                    buffer.append("\t\t_strLength++;\n");
                    buffer.append("\tif(_strLength > "
                                  + XmlExpr2Cpp.getIntExpr(doc.getFirstChild())
                                  + ")\n");
                    buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                }
            } 
            else if (doc.getTagName().equals(OMG_type)
                       && doc.getAttribute(OMG_kind).equals(OMG_fixed)) {
                long digits = XmlExpr2Cpp.getIntExpr(doc.getFirstChild());
                long scale = XmlExpr2Cpp.getIntExpr(doc.getLastChild());
                buffer.append("\t" + reader + elvalue+", " + digits + ", "
                              + scale + ");\n");
            } else
                //jagd desenrolamos el typedef
              {
                if (!kind.equals(""))
                  reader= "is" + ".read_" + XmlType2Cpp.basicORBTypeMapping(kind) +  "(";
 

            
                buffer.append("\t" + reader + elvalue+");\n");
              }
        }
    }
    
     protected static void generateWriteImplementation(Element doc,
                                                      StringBuffer buffer)
        throws Exception
     {
        generateWriteImplementation(doc,buffer,"_value");
     }

    protected static void generateWriteImplementation(Element doc,
                                                      StringBuffer buffer,
                                                      String   elvalue)
        throws Exception
    {
        String tagDef = doc.getTagName();

        if (tagDef.equals(OMG_enum)) {
            buffer.append("\tos.write_ulong("+elvalue+");\n");
        } else if (tagDef.equals(OMG_struct) || tagDef.equals(OMG_exception)) {
            
            if(tagDef.equals(OMG_exception)) {
                buffer.append("\tos.write_string(id());\n");
            }
            
            NodeList children = doc.getChildNodes();
            Element type = null;
            String kind="",def="",typename="";
            for (int i = 0; i < children.getLength(); i++) {
                Element el = (Element) children.item(i);
                String elTag = el.getTagName();
                String member = "";
                if (!elTag.equals(OMG_simple_declarator)
                    && !elTag.equals(OMG_array)) {
                    type = el;
                    //jagd 
                    def = XmlType2Cpp.getDefinitionType(el);
                    typename = el.getAttribute(OMG_name);                    
                    if (def.equals(OMG_kind))
                        kind = XmlType2Cpp.getDeepKind(el);
                    else
                        kind = "";
                } else { // DAVV - declaracion: lectura de un miembro
                    member = el.getAttribute(OMG_name);
                    String data = elvalue+"." + member;
                    if (XmlType2Cpp.isAString(type)
                        || XmlType2Cpp.isAWString(type)) {
                        if (type.getTagName().equals(OMG_type)) {
                            if (type.getFirstChild() != null) {
                                buffer.append("\tCORBA::ULong _length_"
                                              + member + " = 0;\n");
                                buffer.append("\twhile("+elvalue+"." + member
                                              + "[_length_" + member
                                              + "] != NULL)\n");
                                buffer.append("\t\t_length_" + member + "++;\n");
                                buffer.append("\tif(_length_"
                                              + member
                                              + " > "
                                              + XmlExpr2Cpp.getIntExpr(
                                                  type.getFirstChild()) + ")\n");
                                buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                            }
                        } else if (XmlType2Cpp.isAString(type)) { // tag es
                                                                  // OMG_scoped_name
                            data = "(char*const)((CORBA::String_var)" + data
                                   + ")";
                        } else { // wstring y OMG_scoped_name
                            data = "(CORBA::WChar*const)((CORBA::WString_var)"
                                   + data + ")";
                        }
                    }
                    if (kind.equals(""))
                    { //jagd
                    	
                    	if (def.equals(OMG_valuetype)){                    		
                    		String data2 = "(" + typename + "*)&(*(" + elvalue + "." + member + "))";
                    		buffer.append("\t"
                                    + XmlType2Cpp.getTypeWriter(type, "os", data2)
                                    + ";\n");
                    	}
                    	else
	                        buffer.append("\t"
                                  + XmlType2Cpp.getTypeWriter(type, "os", data)
                                  + ";\n");
                    }
                    else
                    { 
                      String writer;
                      if(!type.getTagName().equals(OMG_type))
                      {                      	
                          // Fix bug [#683] Compilation Error: Struct with a Fixed type as member
                          if (kind.equals(OMG_fixed)) {
                              writer= XmlType2Cpp.getTypeWriter(type, "os", data);
                          } else {
                              writer="os.write_"+ XmlType2Cpp.basicORBTypeMapping(kind) +"("+data+")";
                          }
                      }
                      else
                      {
                              writer= XmlType2Cpp.getTypeWriter(type, "os", data);
                      }
                      buffer.append("\t"
                     //jagd             + XmlType2Cpp.getTypeWriter(type, "os", data)
                                    + writer 
                                    + ";\n");
                    }
                }
            }
        } else if (tagDef.equals(OMG_union)) {
            Union union = UnionManager.getInstance().get(doc);
            Vector switchBody = union.getSwitchBody();
            Element switch_el = (Element) doc.getFirstChild();
            Element discriminator_type = (Element) switch_el.getFirstChild();

            // Write the discriminator to the OutputStream
            //PRA
            String discriminatorType = XmlType2Cpp.getType(discriminator_type);
            buffer.append("\t" + discriminatorType + " disc = "+elvalue+"._d();\n");
            //EPRA
            String write_discrim = XmlType2Cpp.getTypeWriter(discriminator_type, "os", "disc");
            buffer.append("\t" + write_discrim + ";\n");

            // Write the selected member to the OutputStream
            UnionCase union_case = (UnionCase) switchBody.elementAt(switchBody
                .size() - 1);

            StringBuffer acummBuffer = new StringBuffer();
            StringBuffer defaultBuffer = new StringBuffer();
            StringBuffer tmpBuffer;

            for (int i = 0; i < switchBody.size(); i++) {
                // bucle para cada elemento miembro
                union_case = (UnionCase) switchBody.elementAt(i);
                //if (union_case.isDefault)
                //continue;
                Vector case_labels = union_case.m_case_labels;
                tmpBuffer = new StringBuffer();
                if (union_case.m_is_default) {
                    tmpBuffer.append(" else {\n");
                } else
                    for (int j = 0; j < case_labels.size(); j++) {
                        // bucle para cada etiqueta de cada elemento
                        Element label = (Element) case_labels.elementAt(j);
                        // lectura de la etiqueta (el C???digo Maldito,
                        // hab???a que bautizarlo...)
                        String value = null, cppType = "";
                        if (discriminator_type.getTagName().equals(OMG_scoped_name)) {
                            // typedef de tipo entero, caracter u octeto,
                            // o enum definido con anterioridad
                            value = label.getAttribute(OMG_name);
                            if ((value == null) || value.equals("")) { 
                                // Tenemos un discriminator con scope pero
                                // asociado a un tipo b?sico
                                cppType = XmlType2Cpp.basicMapping(
                                    XmlType2Cpp.getDeepKind(discriminator_type));
                                Object obj_expr = XmlExpr2Cpp.getExpr(
                                    label.getParentNode(), union.getDiscKind());
                                value = XmlExpr2Cpp.toString(obj_expr, cppType);
                            } else {
                                value = TypeManager.convert(value);
                            }
                        } else if (
                            discriminator_type.getTagName().equals(OMG_enum)) {
                            // enum definido en el 'switch'
                            value = TypeManager.convert(
                                        label.getAttribute(OMG_name));
                        } else {
                            // tipo entero, caracter u octeto
                            cppType = XmlType2Cpp.getType(discriminator_type);
                            Object obj_expr = XmlExpr2Cpp.getExpr(
                                label.getParentNode(), union.getDiscKind());
                            value = XmlExpr2Cpp.toString(obj_expr, cppType);
                        }

                        // condicion para cada elemento
                        if (j == 0) {
                            if (!acummBuffer.toString().equals(""))
                                tmpBuffer.append(" else ");
                            else
                                tmpBuffer.append("\n\t");
                            tmpBuffer.append("if (");
                        } else
                            tmpBuffer.append("||");

                        tmpBuffer.append("(disc == " + value + ")");
                    }
                if (!union_case.m_is_default)
                    tmpBuffer.append(") {\n");

                // escritura del elemento
                Element type = union_case.m_type_spec;
                Element decl = union_case.m_declarator;
                String member = decl.getAttribute(OMG_name);
                String cast = "";
                if (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type)) {
                    if (type.getTagName().equals(OMG_scoped_name)) {
                        if (XmlType2Cpp.isAString(type))
                            cast = "(char*)";
                        else if (XmlType2Cpp.isAWString(type))
                            cast = "(CORBA::WChar*)";
                    } else if (type.getFirstChild() != null) {
                        tmpBuffer.append("\t\tCORBA::ULong _length_" + member
                                         + " = 0;\n");
                        tmpBuffer.append("\t\twhile(("+elvalue+"." + member
                                         + "())[_length_" + member
                                         + "] != NULL)\n");
                        tmpBuffer.append("\t\t\t_length_" + member + "++;\n");
                        tmpBuffer.append("\t\tif(_length_"
                                         + member
                                         + " > "
                                         + XmlExpr2Cpp.getIntExpr(type
                                             .getFirstChild()) + ")\n");
                        tmpBuffer.append("\t\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                    }
                }
                tmpBuffer.append("\t\t"
                                 + XmlType2Cpp.getTypeWriter(type, "os", cast
                                                             + elvalue+"."
                                                             + member
                                                             + "()")
                                 + ";\n");
                tmpBuffer.append("\t}");

                if (union_case.m_is_default)
                    defaultBuffer = tmpBuffer;
                else
                    acummBuffer.append(tmpBuffer);
            }
            buffer.append(acummBuffer);
            buffer.append(defaultBuffer);
            buffer.append("\n");

        } else if (tagDef.equals(OMG_valuetype)) {
        	buffer.append("\tos.write_Value("+elvalue+");\n");
        } else if (tagDef.equals(OMG_interface)) {
            if (!doc.getAttribute(OMG_local).equals(OMG_true))
                buffer.append("\tos.write_Object((::CORBA::Object_ptr)"+elvalue+");\n");
            else
                buffer.append("\tthrow CORBA::MARSHAL(4,CORBA::COMPLETED_NO);\n");
        } else if (tagDef.equals(OMG_array)) {
            NodeList indexes = doc.getChildNodes();
            String ident = "\t";
            String indexesStr = "";
            

            // escritura de elemento
            Element type = (Element) doc.getParentNode().getFirstChild();
            
            if (type.getFirstChild() != null
                && (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type))) {
            	
            		// bucle para recorrido de elementos
                for (int i = 0; i < indexes.getLength(); i++) {
                    buffer.append(ident + "for (CORBA::ULong i" + i + "=0; i" + i
                                  + "<" + XmlExpr2Cpp.getIntExpr(indexes.item(i))
                                  + "; i" + i + "++) {\n");
                    ident += "\t";
                    indexesStr += "[i" + i + "]";
                }	
            	
                buffer.append(ident + "CORBA::ULong _strLength = 0;\n");
                buffer.append(ident + "while("+elvalue + indexesStr
                              + "[_strLength] != NULL)\n");
                buffer.append(ident + "\t_strLength++;\n");
                buffer.append(ident + "if(_strLength > "
                              + XmlExpr2Cpp.getIntExpr(type.getFirstChild())
                              + ")\n");
                buffer.append(ident
                    + "\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                
                buffer.append(ident
                        + XmlType2Cpp.getTypeWriter(type, "os", elvalue
                                                    + indexesStr)
                        + ";\n");

                // cierre de los bucles
                for (int i = 0; i < indexes.getLength(); i++) {
                		ident = ident.substring(1);
                		buffer.append(ident + "}\n");
                }
                
                
            } else {
            		
            		String writer = XmlType2Cpp.getTypeWriter(type, "os", "_value" + indexesStr);
            		if (writer.equals("os.write_octet(_value)")){
            			// bucle para recorrido de elementos
            			for (int i = 0; i < indexes.getLength(); i++) {    
            				if (XmlExpr2Cpp.getIntExpr(indexes.item(i)) > 0)
            					buffer.append("\tos.write_octet_array(_value, 0, " + 
            							XmlExpr2Cpp.getIntExpr(indexes.item(i)) + ");\n");
            			}
            	
            		} else {
            			// bucle para recorrido de elementos
            			for (int i = 0; i < indexes.getLength(); i++) {
            				buffer.append(ident + "for (CORBA::ULong i" + i + "=0; i" + i
                                  + "<" + XmlExpr2Cpp.getIntExpr(indexes.item(i))
                                  + "; i" + i + "++) {\n");
            				ident += "\t";
            				indexesStr += "[i" + i + "]";
            			}		
            	
            			buffer.append(ident
                          + XmlType2Cpp.getTypeWriter(type, "os", "_value"
                                                      + indexesStr)
                          + ";\n");

            			// cierre de los bucles
            			for (int i = 0; i < indexes.getLength(); i++) {
            				ident = ident.substring(1);
            				buffer.append(ident + "}\n");
            			}
            		}
            }
        } else if (tagDef.equals(OMG_sequence)) {
            // control de limites
            Element bound = (Element) doc.getFirstChild().getNextSibling();
            if (bound != null && bound.getTagName().equals(OMG_expr)) {
                buffer.append("\tif ("+elvalue+".length() > "
                              + XmlExpr2Cpp.getIntExpr(bound) + ")\n");
                buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Invalid sequence length\");\n");
            }
            buffer.append("\tCORBA::ULong _length = "+elvalue+".length();\n");
            buffer.append("\tos.write_ulong(_length);\n");
            // escritura de elementos
            //buffer.append("\tfor (CORBA::ULong i=0; i<_value.length(); i++) {\n");
            Element type = (Element) doc.getFirstChild(); // s???lo ser???
                                                          // posible tener
                                                          // valores de etiqueta
                                                          // 'OMG_type' o
                                                          // 'OMG_scoped'
             //jagd
            if (!(type.getFirstChild() != null
                && (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type)))) 
            {
              String tagDef2 = XmlType2Cpp.getDefinitionType(type);
              if(tagDef2.equals(OMG_struct))
              {
                String type_pointer = XmlType2Cpp.getTypeHelper(type);
                buffer.append("\tconst " + type_pointer + "*" + " _buffer="+elvalue+".get_buffer();\n");
              } 
            }


            if (type.getFirstChild() != null
                && (XmlType2Cpp.isAString(type) || XmlType2Cpp.isAWString(type))) {
            		buffer.append("\tfor (CORBA::ULong i=0; i<_length; i++) {\n");
                buffer.append("\t\tCORBA::ULong _strLength = 0;\n");
                buffer.append("\t\twhile("+elvalue+"[i][_strLength] != NULL)\n");
                buffer.append("\t\t\t_strLength++;\n");
                buffer.append("\t\tif(_strLength > "
                              + XmlExpr2Cpp.getIntExpr(type.getFirstChild())
                              + ")\n");
                buffer.append("\t\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
                buffer.append("\t\t"
                        + XmlType2Cpp.getTypeWriter(type, "os", elvalue+"[i]")
                        + ";\n");
                buffer.append("\t}\n");
            }
            else{
            		String writer = XmlType2Cpp.getTypeWriter(type, "os", "_value[i]");
            		if (writer.equals("os.write_octet(_value[i])")){            			
            			buffer.append("\tif (_length > 0)\n");
            			buffer.append("\t\tos.write_octet_array(_value.get_buffer(), 0, _length);\n");
            		}
            		else{            		
            			buffer.append("\tfor (CORBA::ULong i=0; i<_length; i++) {\n");
            			String tagDef2 = XmlType2Cpp.getDefinitionType(type);
            			if(!tagDef2.equals(OMG_struct))
	            			buffer.append("\t\t"
            							+ XmlType2Cpp.getTypeWriter(type, "os", "_value[i]")
            							+ ";\n");
            			else
            			  {
			                buffer.append("\t\t"
            	              + XmlType2Cpp.getTypeWriter(type, "os", "*_buffer")
                	          + ";\n");
			                buffer.append("\t\t_buffer++;\n");
            			  }           			
            			buffer.append("\t}\n");
            		}
            }
        } else /* OMG_scoped_name o tipos basicos */{
            String kind="";
            String type="";
            type = XmlType2Cpp.getDefinitionType(doc);
            if (type.equals(OMG_kind))
              kind = XmlType2Cpp.getDeepKind(doc);
            else
              kind = "";

            if (tagDef.equals(OMG_type) && doc.getFirstChild() != null
                && (XmlType2Cpp.isAString(doc) || XmlType2Cpp.isAWString(doc))) {
                buffer.append("\tCORBA::ULong _strLength = 0;\n");
                buffer.append("\twhile("+elvalue+"[_strLength] != NULL)\n");
                buffer.append("\t\t_strLength++;\n");
                buffer.append("\tif(_strLength > "
                              + XmlExpr2Cpp.getIntExpr(doc.getFirstChild())
                              + ")\n");
                buffer.append("\t\tthrow CORBA::BAD_PARAM(\"Invalid string length\");\n");
            }
            String writer;
            if(doc.getTagName().equals(OMG_type)||kind.equals(""))
            {
              writer= XmlType2Cpp.getTypeWriter(doc, "os", elvalue);
            }
            else //jagd
            {
                 writer="os.write_"+ XmlType2Cpp.basicORBTypeMapping(kind) +"("+elvalue+")";
            } 
            buffer.append("\t"
             //jagd             + XmlType2Cpp.getTypeWriter(type, "os", data)
                                + writer
                                + ";\n");

        }
    }
}
