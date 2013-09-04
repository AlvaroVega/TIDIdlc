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

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

/**
 * Generates Cpp for all holder classes.
 */
class XmlCppHolderGenerator
    implements Idl2XmlNames
{

    public static String generateCpp(String genPackage, String className,
                                     String classType)
    {

        StringBuffer buffer = new StringBuffer();

        String holderClass = classType;
        //String typeCodePtr=className.substring(className.lastIndexOf(
        // "::")+2);

        /*
        PRA: _tc_Package::Name may not be initialized yet, so we call
        type() method of its associated helper. No memory leaks appear
        because type() first checks whether or not _tc_* is initialized

        buffer.append("::CORBA::TypeCode_ptr " + holderClass
                      + "::_type() const {\n\treturn CORBA::TypeCode::_duplicate("
                      + genPackage + "::_tc_" + className + ");\n}\n\n");
        */
					  
        buffer.append("::CORBA::TypeCode_ptr " + holderClass
                + "::_type() const {\n\treturn CORBA::TypeCode::_duplicate("
                + genPackage + "::_" + className + "Helper::type());\n}\n\n");
        
        return buffer.toString();
    }

    /**
     * generate the holder class of a className class the name of the holder
     * class is inside classType
     * 
     * @param genPackage
     *            the package Name
     * @param className
     *            the name of the class which the holder is created.
     * @param classType
     *            the name of the holder class.
     */
    public static StringBuffer generateHpp(String corbaType, String genPackage,
                                     String className, String classType)
    {
        StringBuffer buffer = new StringBuffer();
        //String
        // helperClass=TypedefManager.getInstance().getUnrolledHelperType(className);
        // // DAVV - RELALO
        String helperClass = XmlType2Cpp.getHelperName(className);
        String holderClass = genPackage.equals("") ? classType : 
            classType.substring(genPackage.length() + 2);
        // Class header
        if (corbaType.equals(OMG_Holder_Simple)) {// Tamanio Fijo y tipo simple.
            generateSimpleHolder(buffer, holderClass, helperClass, className);
        }
        /*
         * else if(corbaType.equals(OMG_Holder_Complex)) {// Tamanio Variable
         * generateComplexHolder(buffer,holderClass,helperClass,className); }
         */
        else if (corbaType.equals(OMG_Holder_Complex)
                 || corbaType.equals(OMG_Holder_Any)) 
        {// Estructura de tama?o Variable.
            generateComplexHolder(buffer, holderClass, helperClass, className,
                                  corbaType);
        } else if (corbaType.equals(OMG_Holder_Ref)) {// Tipo que tiene
                                                      // Referencia.
            generateRefHolder(buffer, holderClass, helperClass, className);
        } else if (corbaType.equals(OMG_Holder_String)
                   || corbaType.equals(OMG_Holder_WString)) {// Estructura de
                                                             // tama???o Variable.
            generateStringHolder(buffer, holderClass, helperClass, className,
                                 corbaType);
        } else if (corbaType.equals(OMG_Holder_Array)) { // Array.
            generateArrayHolder(buffer, holderClass, helperClass, className);
        } else if (corbaType.equals(OMG_valuetype)) {
            generateValueHolder(buffer, holderClass, helperClass, className);
        }

        return buffer;
    }

    private static void generateSimpleHolder(StringBuffer buffer,
                                             String holderClass,
                                             String helperClass,
                                             String className)
    {
        // Coommon.
        buffer.append("class ");
        buffer.append(holderClass);
        buffer.append(": public virtual ::TIDorb::portable::Streamable {\n\n");
        buffer.append("\tpublic:\n\t\t");
        // eoCommon.

        buffer.append(className + " value; \n\n");
        buffer.append("\t\t" + holderClass + "() {};\n");
        buffer.append("\t\t" + holderClass + "(const " + className
                      + "& initial) : value(initial) {}\n");

        buffer.append("\t\t::CORBA::TypeCode_ptr _type() const;\n");

        buffer.append("\t\tvoid _write(::TIDorb::portable::OutputStream& outs) const\n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::write(outs,value); \n\t\t}\n");

        buffer.append("\t\tvoid _read(::TIDorb::portable::InputStream& ins) \n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::read(ins,value);\n");
        buffer.append("\t\t}\n");
        buffer.append("}; // end of holder class SIMPLE \n");
    }

    private static void generateComplexHolder(StringBuffer buffer,
                                              String holderClass,
                                              String helperClass,
                                              String className, String corbaType)
    {
        buffer.append("class ");
        buffer.append(holderClass);
        buffer.append(": public virtual ::TIDorb::portable::Streamable {\n\n");
        buffer.append("\tpublic:\n\t\t");
        buffer.append(className + "* value; \n\n");
        //buffer.append("\t\t" + holderClass + "() {value = NULL;}\n");
        //jagd
        buffer.append("\t\tbool must_free; \n\n");
        
        //jagd 
        buffer.append("\t\t" + holderClass + "() {value = NULL; must_free=true;}\n");
        buffer.append("\t\t" + holderClass + "("+className+" * pValue, bool aFree=false) {value = pValue;must_free=aFree;}\n");
        
        buffer.append("\t\t" + holderClass + "(const " + className
                      + "& initial){\n");
        //buffer.append("\t\t\tvalue = new " + className + "(initial);\n");
        buffer.append("\t\t\tvalue = new " + className + "(initial); \n");
        buffer.append("\t\t\tmust_free=true;\n");
        buffer.append("\t\t}\n");
        //buffer.append("\t\t~" + holderClass + "() {delete value;}\n");
        buffer.append("\t\t~" + holderClass + "() {if(must_free) delete value;}\n");
        buffer.append("\t\t" + holderClass + "& operator= (const "
                      + holderClass + "& other) {\n");
        //buffer.append("\t\t\tif (value) delete value;\n");
        //buffer.append("\t\t\tvalue = new " + className + "(*(other.value));\n");
        buffer.append("\t\t\tif (value && must_free) delete value;\n");
        buffer.append("\t\t\tvalue = new " + className + "(*(other.value));\n");
        buffer.append("\t\t\tmust_free=true;\n");
        buffer.append("\t\t\treturn *this;\n\t\t}\n");
        buffer.append("\t\t::CORBA::TypeCode_ptr _type() const;\n");
        buffer.append("\t\tvoid _write(::TIDorb::portable::OutputStream& outs) const\n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::write(outs,*value); \n\t\t}\n");
        buffer.append("\t\tvoid _read(::TIDorb::portable::InputStream& ins) \n\t\t{\n\t\t\t");
        	//buffer.append("if (!value) value = new " + className + "();\n\t\t\t");
        buffer.append("if (!value) {value = new " + className + "(); must_free=true;}\n\t\t\t");
        buffer.append(helperClass);
        
        // All complex types passed by reference        
        buffer.append("::read(ins, *value); \n\t\t}\n"); 
        
        buffer.append("}; // end of holder class COMPLEX\n");
    }

    private static void generateRefHolder(StringBuffer buffer,
                                          String holderClass,
                                          String helperClass, String className)
    {
        buffer.append("class ");
        buffer.append(holderClass);
        buffer.append(": public virtual ::TIDorb::portable::Streamable {\n\n");
        buffer.append("\tpublic:\n\t\t");

        buffer.append(className + "_ptr value; \n\n");
        buffer.append("\t\t" + holderClass + "() {value = " + className
                      + "::_nil();}\n");
        //buffer.append("\t\t" + holderClass + "(" + className + "* initial) :
        // value(initial) {} // Non copying\n");
        buffer.append("\t\t" + holderClass + "(const " + className
                      + "_ptr initial){");//copying");
        buffer.append(" value=" + className + "::_duplicate((" + className
                      + "_ptr)initial);\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\t::CORBA::TypeCode_ptr _type() const;\n");
        buffer.append("\t\tvoid _write(::TIDorb::portable::OutputStream& outs) const\n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::write(outs,value); \n\t\t}\n");

        buffer.append("\t\tvoid _read(::TIDorb::portable::InputStream& ins) \n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::read(ins,value); \n\t\t}\n");
        buffer.append("}; // end of holder class REF\n");
    }

    private static void generateStringHolder(StringBuffer buffer,
                                             String holderClass,
                                             String helperClass,
                                             String className, String corbaType)
    {
        buffer.append("class ");
        buffer.append(holderClass);
        buffer.append(": public virtual ::TIDorb::portable::Streamable {\n\n");
        buffer.append("\tpublic:\n\t\t");

        buffer.append(className + " value; \n\n");
        buffer.append("\t\t" + holderClass + "() {");
        if (corbaType.equals(OMG_Holder_String))
            buffer.append("value=CORBA::string_dup(\"\");");
        else
            buffer.append("value=CORBA::wstring_dup(L\"\");");
        buffer.append("}\n");
        buffer.append("\t\t" + holderClass + "(const "
                      + XmlType2Cpp.basicMapping(corbaType) + " initial){\n");
        buffer.append("\t\t\tvalue=CORBA::" + corbaType + "_dup(initial);\n");
        buffer.append("\t\t}\n");

        buffer.append("\t\t::CORBA::TypeCode_ptr _type() const;\n");

        buffer.append("\t\tvoid _write(::TIDorb::portable::OutputStream& outs) const\n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::write(outs,value); \n\t\t}\n");

        buffer.append("\t\tvoid _read(::TIDorb::portable::InputStream& ins) \n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::read(ins,value); \n\t\t}\n");
        buffer.append("}; // end of holder class STRING\n");
    }

    private static void generateArrayHolder(StringBuffer buffer,
                                            String holderClass,
                                            String helperClass, String className)
    { 
        buffer.append("class ");
        buffer.append(holderClass);
        buffer.append(": public virtual ::TIDorb::portable::Streamable {\n\n");
        buffer.append("\tpublic:\n\t\t");

        buffer.append(className + "_slice* value; \n\n");
        buffer.append("\t\t" + holderClass + "() {value = NULL;}\n");
        buffer.append("\t\t" + holderClass + "(const " + className
                      + "_slice* initial) : value(" + className
                      + "_dup(initial)) {}\n");

        buffer.append("\t\t::CORBA::TypeCode_ptr _type() const;\n");

        buffer.append("\t\tvoid _write(::TIDorb::portable::OutputStream& outs) const\n\t\t{\n\t\t\t");
        buffer.append(helperClass);
        buffer.append("::write(outs,value); \n\t\t}\n");

        buffer.append("\t\tvoid _read(::TIDorb::portable::InputStream& ins) \n\t\t{\n\t\t\t");
        buffer.append("value = " + className + "_alloc();\n");
        buffer.append("\t\t\t");
        buffer.append(helperClass);
        buffer.append("::read(ins,value); \n\t\t}\n");
        buffer.append("}; // end of holder class ARRAY\n");
    }

    private static void generateValueHolder(StringBuffer buffer,
                                            String holderClass,
                                            String helperClass, String className)
    {
        buffer.append("class ");
        buffer.append(holderClass);
        buffer.append(": public virtual ::TIDorb::portable::Streamable {\n\n");
        buffer.append("\tpublic:\n");
        buffer.append("\t\t" + className + "* value;\n\n");
        buffer.append("\t\t" + holderClass + "() {value = NULL;}\n");
        buffer.append("\t\t" + holderClass + "(" + className + "* initial){\n");
        buffer.append("\t\t\tvalue = " + className
        //              + "::_downcast(initial);\n");
                      + "::_downcast(initial->_copy_value());\n");
        //buffer.append("\t\t\tCORBA::add_ref(value);\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\t~" + holderClass + "() {CORBA::remove_ref(value);}\n");
        buffer.append("\t\t" + holderClass + "& operator= (const "
                      + holderClass + "& other) {\n");
        buffer.append("\t\t\tCORBA::remove_ref(value);\n");
        buffer.append("\t\t\tvalue = " + className
                      + "::_downcast(other.value);\n");
        buffer.append("\t\t\tCORBA::add_ref(value);\n");
        buffer.append("\t\t\treturn *this;\n");
        buffer.append("\t\t}\n");
        buffer.append("\t\t::CORBA::TypeCode_ptr _type() const;\n");
        buffer.append("\t\tvoid _write(::TIDorb::portable::OutputStream& outs) const\n\t\t{\n");
//        //buffer.append("\t\t\t" + helperClass + "::write(outs, value); \n");
        buffer.append("\t\t\touts.write_Value(value);\n");
        buffer.append("\t\t}\n\n");
        buffer.append("\t\tvoid _read(::TIDorb::portable::InputStream& ins) \n");
        buffer.append("\t\t{\n");
        /*buffer.append("\t\t\tif (!value) {\n");
        buffer.append("\t\t\t\tCORBA::ValueFactory vf = ins.orb()->lookup_value_factory("
                      + helperClass + "::id());\n");
        buffer.append("\t\t\t\tvalue = " + className
                      + "::_downcast(ins.orb()->get_value_for_unmarshal(vf));\n");
        buffer.append("\t\t\t}\n");
        buffer.append("\t\t\t" + helperClass + "::read(ins,value); \n");*/
//        buffer.append("\t\t\tif (value) {\n");
//        buffer.append("\t\t\t\tvalue->_remove__ref();\n");
//        buffer.append("\t\t\t}\n\n");
        buffer.append("\t\t\tCORBA::ValueBase_var val;\n");
        buffer.append("\t\t\tins.read_Value(val.out());\n");
        buffer.append("\t\t\tCORBA::remove_ref(value); //Remove internal reference\n");
        //buffer.append("\t\t\tif (aux == NULL) {\n");
//        buffer.append("\t\t\t\tvalue = NULL;\n");
//        buffer.append("\t\t\t\treturn;\n");
//        buffer.append("\t\t\t}\n\n");
        buffer.append("\t\t\tvalue = " + className + "::_downcast(val);\n");
        buffer.append("\t\t\tCORBA::add_ref(value);\n");
//        buffer.append("\t\t\tif (value == NULL) {\n");
//        buffer.append("\t\t\t\tthrow CORBA::MARSHAL();\n");
//        buffer.append("\t\t\t}\n");
		buffer.append("\t\t}\n");
        buffer.append("}; // end of holder class VALUE\n");
    }
}

