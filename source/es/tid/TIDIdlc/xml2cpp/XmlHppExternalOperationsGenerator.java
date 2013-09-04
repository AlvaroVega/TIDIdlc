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

/**
 * T?tulo: Idlc Compilador IDL a Java y C++ Descripcion: . 
 */
import org.w3c.dom.*;

import es.tid.TIDIdlc.xmlsemantics.TypedefManager;

import java.io.File;

public class XmlHppExternalOperationsGenerator
    implements Idl2XmlNames
{

    public XmlHppExternalOperationsGenerator()
    {}

    public static void generateHpp(Element doc, StringBuffer buffer,
                                   String type, String name, String genPackage)
    {
        String hierarchy = getHierarchy(name, genPackage);
        buffer.append("//\n");
        buffer.append("// " + name + "_ext.h (from " + type + ")\n");
        buffer.append("//\n");
        buffer.append("// File generated: ");
        java.util.Date currentDate = new java.util.Date();
        buffer.append(currentDate);
        buffer.append("\n");
        buffer.append("//   by TIDorb idl2cpp " + CompilerConf.st_compiler_version + "\n");
        buffer.append("//   external Any operators definition File.\n");
        buffer.append("//\n\n");

        buffer.append("\n#ifndef " + hierarchy);
        buffer.append("\n#define " + hierarchy + "\n\n");
        generateAnyOperators(doc, buffer, name, genPackage);
        includeChildrenAnyOperatorsHeaderFiles(doc, buffer, name, genPackage);
        buffer.append("\n#endif //" + hierarchy + "\n\n");
        //buffer.append("\n// End External Operations definition & other
        // children header files inclusion\n");

    }

    private static void generateAnyOperators(Element doc, StringBuffer buffer,
                                             String name, String genPackage)
    {
        String nodeName = doc.getNodeName();
        if (nodeName.equals(OMG_scoped_name) || nodeName.equals(OMG_type)) { 
            // s???lo puede proceder de typedef
            Element parent = (Element) doc.getParentNode(); // 'parent'sera
                                                            // el 'typedef'
            NodeList list = parent.getChildNodes();
            for (int i = 1; i < list.getLength(); i++) {
                Element decl = (Element) list.item(i);
                // DAVV - un poco feo, pero exacto... buscamos la declaracion
                // adecuada de entre las multiples posibles en el typedef
                if ((decl != null) && decl.getAttribute(OMG_name).equals(name))
                    if ((decl.getTagName().equals(OMG_array))) {
                        String nameWithPackage = genPackage.equals("") ? name
                            : genPackage + "::" + name;
                        //String helperClass =
                        // TypedefManager.getInstance().getUnrolledHelperType(nameWithPackage);

                        String helperClass = 
                            XmlType2Cpp.getHelperName(nameWithPackage);
                        buffer.append("\t\t#include \""
                                      + getRouteToHere(name, genPackage)
                                      + ".h\"\n");
                        buffer.append("\t\tinline void operator <<= (::CORBA::Any& any, ");
                        buffer.append("const " + nameWithPackage + "_forany&");
                        buffer.append(" _value)\n\t\t{\n");
                        buffer.append("\t\t\t");
                        buffer.append(helperClass);
                        buffer.append("::insert(any, _value);\n");
                        buffer.append("\t\t}\n");

                        buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any,  ");
                        buffer.append(nameWithPackage + "_forany&");
                        buffer.append(" _value)\n\t\t{\n");
                        buffer.append("\t\t\treturn ");
                        buffer.append(helperClass);
                        buffer.append("::extract(any, _value);\n");
                        buffer.append("\t\t}\n");
                        return; // fin de operadores para arrays
                    }
            }

            buffer.append("\t\t// ******************************************.\n");
            buffer.append("\t\t// Redefinition... returns to its base class.\n");
            buffer.append("\t\t// ******************************************.\n");
            return;

        }

        boolean isLocal = false;
        if (nodeName.equals(Idl2XmlNames.OMG_interface)) {
            String isLocalS = doc.getAttribute(OMG_local);
            isLocal = ((isLocalS != null) && (isLocalS.equals("true")));
        }

        if ((nodeName.equals(Idl2XmlNames.OMG_interface) && !isLocal)
            || (nodeName.equals(Idl2XmlNames.OMG_struct))
            || (nodeName.equals(Idl2XmlNames.OMG_exception))
            || (nodeName.equals(Idl2XmlNames.OMG_sequence))
            || (nodeName.equals(Idl2XmlNames.OMG_union))
            || (nodeName.equals(Idl2XmlNames.OMG_valuetype))) {
            String nameWithPackage = genPackage.equals("") ? name : genPackage
                                                                    + "::"
                                                                    + name;
            //String
            // helperClass=TypedefManager.getInstance().getUnrolledHelperType(nameWithPackage);

            String helperClass = XmlType2Cpp.getHelperName(nameWithPackage);
            buffer.append("\t\t#include \"" + getRouteToHere(name, genPackage)
                          + ".h\"\n");
            buffer.append("\t\tinline void operator <<= (::CORBA::Any& any, ");

            buffer.append(XmlType2Cpp.getAnyInsertionParameter(doc, name,
                                                               genPackage,
                                                               false));
            buffer.append(" _value)\n\t\t{\n\t\t\t");
            buffer.append(helperClass);
            buffer.append("::insert(any,_value);\n\t\t}\n");

            //if (!nodeName.equals(Idl2XmlNames.OMG_interface)) { // DAVV -
            // atencion!! esto es PROVISIONAL
            buffer.append("\t\tinline void operator <<= (::CORBA::Any& any, "); 
            // DAVV - a???adido operador de insercion noncopy ...

            buffer.append(XmlType2Cpp
                .getAnyInsertionParameter(doc, name, genPackage, true));
            buffer.append(" _value)\n\t\t{\n\t\t\t");
            buffer.append(helperClass);
            buffer.append("::insert(any,_value);\n\t\t}\n"); 
            //}

            String anyType = XmlType2Cpp.getAnyExtractionParameter(doc, name, genPackage);

            // Bug #75
            if (!nodeName.equals(Idl2XmlNames.OMG_interface)) {
                // structs, exceptions, sequences, unions and valuetypes uses a const _value
                // workaround "const const"
            	if (!anyType.startsWith("const ")) {
                	// Este trozo se borrara en un futuro
                	buffer.append("\t\t// Deprecated\n");
                	buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                    buffer.append(anyType);
                    buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                    buffer.append(helperClass);
                    buffer.append("::extract(any, (const " + anyType + ") _value);\n\t\t}\n");
                	// Fin del trozo que se borrara en un futuro

                    buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, const ");
                    buffer.append(anyType);
                    buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                    buffer.append(helperClass);
                    buffer.append("::extract(any, _value);\n\t\t}\n");
            	} else {
                    //jagd se anaden extensiones que solo usara el orb ppr razones de eficiencia
                      
                    buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                    buffer.append(anyType);
                    buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                    buffer.append(helperClass);
                    buffer.append("::extract(any, _value);\n\t\t}\n");
  
                    //jagd operador no const que se aduena de la memoria del any
                    String tag = doc.getTagName();
                    if (tag.equals(OMG_struct) || tag.equals(OMG_union)
                        || tag.equals(OMG_any) || tag.equals(OMG_exception))
                    {
                      if( !(XmlType2Cpp.isAString(doc) || XmlType2Cpp.isAWString(doc)) )
                      { 
                        buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                        String type = "";
                        if (!genPackage.equals(""))
                          type += genPackage + "::";
                        type += doc.getAttribute(OMG_name);
                        type += "*&";
                        buffer.append(type);
                        buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                        buffer.append(helperClass);
                        buffer.append("::extract(any, _value);\n\t\t}\n");
                     
                        buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                        type = "";
                        if (!genPackage.equals(""))
                          type += genPackage + "::";
                        type += doc.getAttribute(OMG_name);
                        type += "&";
                        buffer.append(type);
                        buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                        buffer.append(helperClass);
                        buffer.append("::extract(any, _value);\n\t\t}\n");
 
                      }
                    }
                    if ( tag.equals(OMG_sequence))
                    {

                        buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                        String type = "";
                        if (!genPackage.equals(""))
                          type += genPackage + "::";
                        type += name;
                        type += "*&";
                        buffer.append(type);
                        buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                        buffer.append(helperClass);
                        buffer.append("::extract(any, _value);\n\t\t}\n");

                        buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                        type = "";
                        if (!genPackage.equals(""))
                          type += genPackage + "::";
                        type += name;
                        type += "&";
                        buffer.append(type);
                        buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                        buffer.append(helperClass);
                        buffer.append("::extract(any, _value);\n\t\t}\n");
                    }
                    //fin jagd



           	}
            } else {
                buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any, ");
                buffer.append(anyType);
                buffer.append(" _value)\n\t\t{\n\t\t\treturn ");
                buffer.append(helperClass);
                buffer.append("::extract(any, _value);\n\t\t}\n");
            }

            //buffer.append("// End of External Operations Definition\n");
        } else if (nodeName.equals(Idl2XmlNames.OMG_enum)
                   || nodeName.equals(Idl2XmlNames.OMG_kind)) {
            // SIMPLE TYPE ANY INSERT EXTRACTION
            String nameWithPackage = genPackage.equals("") ? name : 
                genPackage + "::" + name;
            //String
            // helperClass=TypedefManager.getInstance().getUnrolledHelperType(nameWithPackage);
            // RELALO
            String helperClass = XmlType2Cpp.getHelperName(nameWithPackage);
            buffer.append("\t\t#include \"" + getRouteToHere(name, genPackage)
                          + ".h\"\n");
            buffer.append("\t\tinline void operator <<= (::CORBA::Any& any,const ");
            buffer.append(nameWithPackage); // pag 1-53 mapping.
            buffer.append(" _value)\n\t\t{\n\t\t\t");
            buffer.append(helperClass);
            buffer.append("::insert(any,_value);\n\t\t}\n");
            buffer.append("\t\tinline CORBA::Boolean operator >>= (const ::CORBA::Any& any,");
            buffer.append(nameWithPackage);
            buffer.append("& _value)\n\t\t{\n\t\t\treturn "); // pag 1-56,1-57 mapping.
            buffer.append(helperClass);
            buffer.append("::extract(any,_value);\n\t\t}\n");
            
           
            //buffer.append("// End of External Operations Definition\n");
        }
        //else buffer.append("\n// No external operation definition for this
        // type ->"+nodeName+"\n\n");
    }

    private static void includeChildrenAnyOperatorsHeaderFiles(
                                                               Element doc,
                                                               StringBuffer buffer,
                                                               String name,
                                                               String genPackage)
    {
        // Include all of its Children.
        // Include children headers.
        NodeList nl = doc.getChildNodes();
        Element nt = null;
        boolean atLeastOne = false;
        StringBuffer bufferTemp = new StringBuffer();

        if (!doc.getTagName().equals(Idl2XmlNames.OMG_union)) {
            for (int i = 0; i < nl.getLength(); i++) {
                nt = (Element) nl.item(i);
                atLeastOne = generateChildrenHeaderElement(nt, genPackage,
                                                           name, bufferTemp)
                             || atLeastOne;
            }// for nl.getLength
        } else { // union
            for (int i = 0; i < nl.getLength(); i++) {
                Element el = (Element) nl.item(i);
                String tag = el.getTagName();
                if (tag.equals(Idl2XmlNames.OMG_case)) {
                    NodeList nodes2 = el.getChildNodes();
                    for (int j = 0; j < nodes2.getLength(); j++) {
                        Element union_el = (Element) nodes2.item(j);
                        String tag2 = union_el.getTagName();
                        if (tag2.equals("value")) {
                            nt = (Element) union_el.getFirstChild();
                            if (nt.getTagName().equals(
                                         Idl2XmlNames.OMG_scoped_name)) 
                                //  procesamiento de sequences anonimas
                                while (nt != null && 
                                       !nt.getTagName().equals(
                                            Idl2XmlNames.OMG_typedef))
                                    nt = (Element) nt.getNextSibling();
                            if (nt != null)
                                atLeastOne = generateChildrenHeaderElement(
                                                 nt, genPackage, name, 
                                                 bufferTemp)
                                             || atLeastOne;
                        }
                    }
                } else if (tag.equals(Idl2XmlNames.OMG_switch)) {
                    nt = (Element) el.getFirstChild();
                    atLeastOne = generateChildrenHeaderElement(nt, genPackage,
                                                               name, bufferTemp)
                                 || atLeastOne;
                }
            }
        }
        if (atLeastOne) {
            //buffer.append("\n// Begin of children <<Any Operators>> header
            // files inclusion\n");
            buffer.append(bufferTemp.toString());
            //buffer.append("// End of children <<Any Operators>> header files
            // inclusion\n\n");
        }
        //else buffer.append("\n// No Children to Include.");

    }//end of method includeChildrenHeaderFiles()

    private static String getRouteToHere(String name, String genPackage)
    {
        java.util.StringTokenizer st = new java.util.StringTokenizer(
                                              genPackage, "::");
        String route = "";
        while (st.hasMoreTokens())
            route += st.nextToken() + File.separator;
        route += name;
        return route;
    }// end of method getRouteToHere.

    private static String getUpperCaseHierarchy(String hierarchy)
    {
        // MegaCorba --> MEGA_CORBA__EXT_H
        String temp = "_";
        char prev = '_';
        for (int i = 0; i < hierarchy.length(); i++) {
            char ch = hierarchy.charAt(i); // The first character [0] must be
                                           // the same.
            if (Character.isUpperCase(ch) && (prev != '_') && (i != 0)
                && !Character.isUpperCase(prev))
                //The second contition to avoid "__"; 
                // The Fourth To avoid _C_O_R_B_A
                temp += "_";

            temp += Character.toUpperCase(ch);
            prev = ch;
        }
        temp += "__EXT_H_";
        return temp;
    }// end of method getUpperCaseHierarchy()

    private static String getHierarchy(String name, String genPackage)
    {
        java.util.StringTokenizer st = new java.util.StringTokenizer(
                                                genPackage, "::");
        String route = "";
        while (st.hasMoreTokens())
            route += st.nextToken() + "_";
        route += name;
        return getUpperCaseHierarchy(route);
    }

    private static boolean generateChildrenHeaderElement(
                               Element nt,
                               String genPackage,
                               String name,
                               StringBuffer bufferTemp)
    {
        Node temp = null;
        String tipo;
        String nombre, forward = "";
        String route = getRouteToHere(name, genPackage);
        forward = nt.getAttribute(Idl2XmlNames.OMG_fwd);
        boolean atLeastOne = false;
        if (forward == null || forward.equals("")
            || forward.equalsIgnoreCase("false")) {
            tipo = nt.getNodeName();
            if ((tipo.equalsIgnoreCase(Idl2XmlNames.OMG_interface))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_module))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_enum))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_struct))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_exception))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_union))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_valuetype))) {

                NamedNodeMap nnm = nt.getAttributes();
                if (nnm != null) {
                    temp = nnm.getNamedItem(Idl2XmlNames.OMG_name);
                    if (temp != null) {
                        nombre = temp.getNodeValue();
                        bufferTemp.append("#include \"" + route
                                          + File.separator + nombre
                                          + "_ext.h\" \n");
                        atLeastOne = true;
                    }// temp!=null
                }//nnm!=null
            }//tipo.equals....

            else if (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_typedef)) {
                NodeList inL = nt.getChildNodes();
                for (int j = 1; j < inL.getLength(); j++) {
                    Element decl = (Element) inL.item(j);
                    String typeName = decl.getAttribute(Idl2XmlNames.OMG_name);
                    bufferTemp.append("#include \"" + route + File.separator
                                      + typeName + "_ext.h\" \n");
                    atLeastOne = true;
                }// del for .
            }
        }
        return atLeastOne;
    }

}

