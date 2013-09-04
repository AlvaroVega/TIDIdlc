/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 252 $
* Date: $Date: 2008-05-16 11:02:10 +0200 (Fri, 16 May 2008) $
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
import es.tid.TIDIdlc.util.*;

import java.io.File;

import org.w3c.dom.*;
import es.tid.TIDIdlc.CompilerConf;

import java.util.*;

public class XmlHppHeaderGenerator
{

    public static void generate(Element doc, StringBuffer buffer, String type,
                                String name, String genPackage)
    {

        buffer.append("//\n");
        if ((type.equals("skeleton") || type.equals("tie"))
            && genPackage.equals("")) 
            buffer.append("// POA_" + name + ".h (" + type + ")\n");
        else
            buffer.append("// " + name + ".h (" + type + ")\n");
        buffer.append("//\n");
        buffer.append("// File generated: ");
        java.util.Date currentDate = new java.util.Date();
        buffer.append(currentDate);
        buffer.append("\n");
        buffer.append("//   by TIDIdlc idl2cpp " + CompilerConf.st_compiler_version +"\n");
        buffer.append("//\n\n");
        includeParentsHeaderFiles(doc, buffer, type, name, genPackage);

    }//method generate

    public static void generateFoot(StringBuffer buffer, String type,
                                    String name, String genPackage)
    {

        buffer.append("\n\n");
        // inclusi�n de operadores de insercion en Any s�lo los m�dulos en 
        // �mbito global deben incluir el fichero _ext; si lo incluyen los
        // de niveles sucesivos lo har�n dentro de un namespace diferente al
        // global, y el compilador de C++ no encontrar� los 
        // operadores >>= y <<= de nivel global correspondientes
        // al tipo nuevo
        // - tambi�n los elementos de �mbito global que no sean m�dulos
        // deben incluirlo; si no, no ser�n
        //  'alcanzables' en la jerarqu�a de includes

        if (genPackage.equals("") && !type.equals("stub")
            && !type.equals("skeleton") && !type.equals("tie")
            && !type.equals("constant")
            && !type.equals(Idl2XmlNames.OMG_specification)) {
            buffer.append("#include \"");
            buffer.append(getRouteToHere(name, genPackage));
            buffer.append("_ext.h\"\n");
        }
        // to implement the definiton of the header.
        String hierarchy = getHierarchy(genPackage, type);

        /*
         * if (type.equals("skeleton") ||type.equals("tie")) hierarchy = "_POA_" +
         * hierarchy;
         */

        if (!genPackage.equals(""))
            buffer.append("\n#endif //_" + getUpperCaseName(hierarchy + name)
                          + "_H_  \n\n");
        else
            buffer.append("\n#endif // _" + getUpperCaseName(name)
                          + "_H_  \n\n");
    } // method generateFoot

    private static void includeParentsHeaderFiles(Element doc,
                                                  StringBuffer buffer,
                                                  String type, String name,
                                                  String genPackage)
    {
        // To implement #ifndef PATH_TO_HERE
        String hierarchy = getHierarchy(genPackage, type);
        String fileHierarchy = getFileHierarchy(genPackage, type);
        StringBuffer bufferTemp = new StringBuffer();

        // PRA: Code generated for standard IDLs does not compile if we
        // change the order of #include directives for parent inclusion.
        // Those IDLs are compiled with "-expanded" option, so we append
        // the parent header inclusion only when using that option

        boolean append_hierarchy = CompilerConf.getExpanded();
        
        if (!fileHierarchy.equals(""))
            bufferTemp.append("#include \"" + fileHierarchy
                              + ".h\" // Parent Inclusion \n\n");
        else if (!type.equals(Idl2XmlNames.OMG_specification)
                 && !type.equals("stub") && !type.equals("skeleton"))
            bufferTemp.append("#include \"_global_includes_for_"
                              + CompilerConf.getFileName().substring(0,
                                        CompilerConf.getFileName().length() - 4)
                              + "_idl.h\"  // Global Scope Inclusion \n\n");
        
        if (!append_hierarchy) {
	        if (!hierarchy.equals("")) {
	            bufferTemp.append("#ifndef _" + hierarchy + getUpperCaseName(name)
	                              + "_H_\n");
	            bufferTemp.append("#define _" + hierarchy + getUpperCaseName(name)
	                              + "_H_\n\n");
	        } else {
	            bufferTemp.append("#ifndef _" + getUpperCaseName(name) + "_H_ \n");
	            bufferTemp.append("#define _" + getUpperCaseName(name)
	                              + "_H_  \n\n");
	        }
        }
        

        // If hierarchy was not included before, append it right here!
        if (append_hierarchy) {
	        if (!hierarchy.equals("")) {
	            bufferTemp.append("#ifndef _" + hierarchy + getUpperCaseName(name)
	                              + "_H_\n");
	            bufferTemp.append("#define _" + hierarchy + getUpperCaseName(name)
	                              + "_H_\n\n");
	        } else {
	            bufferTemp.append("#ifndef _" + getUpperCaseName(name) + "_H_ \n");
	            bufferTemp.append("#define _" + getUpperCaseName(name)
	                              + "_H_  \n\n");
	        }
        }
        
        if (fileHierarchy.equals("")) {
            if (type.equals("stub")) // para stubs de interfaces de scope
                                     // global
                bufferTemp.append("#include \""
                                  + name.substring(1, name.indexOf("Stub"))
                                  + ".h\"\n\n");
            else if (type.equals("skeleton")) { // para skeletons de
                                                // interfaces de scope global
                bufferTemp.append("#include \"" + name + ".h\"\n\n");
                bufferTemp.append("#include \"PortableServer.h\"\n\n");
            }
        }

        includeSystemHeaderFiles(bufferTemp, type, name, genPackage);
        /*
         * if (!type.equals(Idl2XmlNames.OMG_specification))
         * includeTopLevel(doc,bufferTemp,name); // incorporado para lo
         * que se genere en �mbito global
         */

        if (bufferTemp.length() > 0)
            buffer.append(bufferTemp.toString());

    }// end of method includeParentsHeaderFiles()

    public static void includeForwardDeclarations(Element doc,
                                                  StringBuffer buffer,
                                                  String type, String name,
                                                  String genPackage)
    {
        NodeList nl = doc.getChildNodes();
        Element nt = null;
        StringBuffer bufferTemp = new StringBuffer();
        if (!type.equals(Idl2XmlNames.OMG_union)) {
            for (int i = 0; i < nl.getLength(); i++) {
                nt = (Element) nl.item(i);
                generateForwardElement(nt, genPackage, name, bufferTemp);
            }// end of loop for.
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
                            generateForwardElement(nt, genPackage, name,
                                                   bufferTemp);
                        }
                    }
                }
            }
        }

        if (bufferTemp.length() > 0) {
            buffer.append("\t// Begin of forward Declarations  definition\n");
            if (!type.equals("module")
                && !type.equals(Idl2XmlNames.OMG_specification))
                buffer.append("\t\tpublic:\n");
            buffer.append(bufferTemp.toString());
            buffer.append("\t// End of forward declarations\n\n\n");
        }

    }// end of method includeForwardDeclarations()

    public static void includeChildrenHeaderFiles(Element doc,
                                                  StringBuffer buffer,
                                                  String type, String name,
                                                  String genPackage)
    {

        // Include all of its Children.
        // Include children headers.
        NodeList nl = doc.getChildNodes();
        Element nt = null;
        StringBuffer bufferTemp = new StringBuffer();
        Hashtable forwardElements = new Hashtable();
        if (!type.equals(Idl2XmlNames.OMG_union)) {
            for (int i = 0; i < nl.getLength(); i++) {
                nt = (Element) nl.item(i);
                generateChildrenHeaderElement(nt, genPackage, name, bufferTemp,
                                              type, forwardElements);
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
                            if (nt.getTagName().equals(Idl2XmlNames.OMG_scoped_name)) 
                                // procesamiento de sequences anonimas
                                while (nt != null
                                       && !nt.getTagName().equals(
                                                 Idl2XmlNames.OMG_typedef))
                                    nt = (Element) nt.getNextSibling();
                            if (nt != null)
                                generateChildrenHeaderElement(nt, genPackage,
                                                              name, bufferTemp,
                                                              type,
                                                              forwardElements);
                        }
                    }
                } else if (tag.equals(Idl2XmlNames.OMG_switch)) {
                    nt = (Element) el.getFirstChild();
                    generateChildrenHeaderElement(nt, genPackage, name,
                                                  bufferTemp, type,
                                                  forwardElements);
                }
            }
        }

        if (bufferTemp.length() > 0) {
            buffer.append("\t// Begin of children  header files inclusion\n");
            if (!type.equals(Idl2XmlNames.OMG_module)
                && !type.equals(Idl2XmlNames.OMG_specification))
                buffer.append("\t\tpublic:\n");
            buffer.append(bufferTemp.toString());
            buffer.append("\t// End of children  header files inclusion\n\n");

        }

    }//end of method includeChildrenHeaderFiles()

    private static void includeSystemHeaderFiles(StringBuffer buffer,
                                                 String type, String name,
                                                 String genPackage)
    {
        String nameWithPackage = genPackage + "::" + name;
        if (type.equals("module") || genPackage.equals("")) { 
            // para tipos de datos con �mbito global
            buffer.append("#include \"TIDorb" + File.separator
                          + "portable.h\" \n\n");
            buffer.append("#include \"TIDorb" + File.separator
                          + "types.h\" \n\n"); 
            if (nameWithPackage.indexOf("CORBA") == -1)
                buffer.append("#include \"CORBA.h\"\n\n");
        }

    }

    private static String getRouteToHere(String name, String genPackage)
    {
        java.util.StringTokenizer st = new java.util.StringTokenizer(
                                                         genPackage, "::");
        String route = "";
        while (st.hasMoreTokens())
            route += st.nextToken() + File.separator;
        route += name;
        return route;
    } // end of method getRouteToHere.

    
    private static String getCppOMGName(Element nt)
    {
        String type = nt.getNodeName();
        
        String temp = type.charAt(0) + "";
        temp = temp.toUpperCase();
        temp += type.substring(1, type.length());
        if (type.equals(Idl2XmlNames.OMG_interface))
            return temp;
        else if (type.equals(Idl2XmlNames.OMG_struct)
                 || type.equals(Idl2XmlNames.OMG_union)
                 || type.equals(Idl2XmlNames.OMG_exception)) {
            
            if(XmlType2Cpp.isVariableSizeType(nt)) {
                return "VariableSize";
            } else {
                return "FixedSize";
            }
        }
        return temp;

    }

    private static void includeTopLevel(Element doc, StringBuffer buffer,
                                        String name)
    {
        /*
         * if(!genPackage.equals("")) return; // Con esto solo incluimos en los
         * modulos de primer nivel.
         */
        if (!((Element) doc.getParentNode()).getTagName().equals(
                                  Idl2XmlNames.OMG_specification)) {
            // Si el padre no es una especificacion, entonces no estamos a
            // primer nivel. y por lo tanto.
            // El modulo de primer nivel ya habria incluido a los otros.
            return;
        }

        //NodeList
        // nl=doc.getOwnerDocument().getElementsByTagName(Idl2XmlNames.OMG_module);
        NodeList nl = doc.getParentNode().getChildNodes(); 
        // de este modo, s�lo obtenemos hijos del nodo OMG_specification,
        // no nietos, ni biznietos,...
        
        Hashtable fwdDeclarations = new Hashtable();
        for (int i = 0; i < nl.getLength(); i++) {
            Element el = (Element) nl.item(i);
            boolean typedef_broken = false;
            if (el.getTagName().equals(Idl2XmlNames.OMG_typedef)) {
                NodeList typedef_nl = el.getChildNodes();
                for (int j = 1; j < typedef_nl.getLength(); j++) {
                    Element typedef_el = (Element) typedef_nl.item(j);
                    String typedef_name = 
                        typedef_el.getAttribute(Idl2XmlNames.OMG_name);
                    if (typedef_name.equals(name)) {
                        typedef_broken = true;
                        break;
                    } else if (typedef_name.equals("CORBA")
                               || typedef_name.equals("java")) 
                        // "java es incluido para interoperabilidad con RMI
                        continue;
                    else
                        buffer.append("#include \"" + typedef_name + ".h\"\n");
                }
                if (typedef_broken)
                    break;
            } else {
                String current_name = el.getAttribute(Idl2XmlNames.OMG_name);
                if (current_name.equals(name))
                    if (el.getAttribute(Idl2XmlNames.OMG_fwd).equals("true"))
                        continue; // para no incluirse a uno mismo si se
                                  // declaro por adelantado
                    else
                        //break; // solo se incluyen los elementos
                                 // definidos ANTERIORMENTE en el IDL
                        continue; // para incluir tambi�n lo
                                  // definido POSTERIORMENTE, por
                else if (current_name.equals("CORBA")
                         || current_name.equals("java")) // "java es incluido
                                                         // para
                                                         // interoperabilidad
                                                         // con RMI
                    continue;
                /*
                 * else if
                 * (el.getAttribute(Idl2XmlNames.OMG_fwd).equals("true"))
                 * continue;
                 */
                else {
                    if (el.getAttribute(Idl2XmlNames.OMG_fwd).equals("true")) 
                        // si encuentro una declaraci�n adelantada, incluyo
                        // su header
                        fwdDeclarations.put(current_name, ""); // (tendra que
                                                               // estar definida
                                                               // antes o
                                                               // despu�s)
                    else if (fwdDeclarations.containsKey(current_name)) 
                        // si la declaracion fue hecha antes por adelantado,
                        // ya se incluyo su header
                        continue;
                    buffer.append("#include \"" + current_name + ".h\"\n");
                }

            }
        }
        buffer.append("\n");
        return;
    }

    private static void generateForwardElement(Element nt, String genPackage,
                                               String name,
                                               StringBuffer bufferTemp)
    {

        String nombre, forward = "";
        String tipo = nt.getNodeName();
        if (tipo.equals(Idl2XmlNames.OMG_state_member)) {
            nt = (Element) nt.getFirstChild();
            tipo = nt.getNodeName();
        }
        Node temp = null;
        forward = nt.getAttribute(Idl2XmlNames.OMG_fwd);
        if (forward == null || forward.equals("")
            || forward.equalsIgnoreCase("false")) {
            if (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_interface)
                || tipo.equalsIgnoreCase(Idl2XmlNames.OMG_valuetype)
                || tipo.equalsIgnoreCase(Idl2XmlNames.OMG_struct)
                || tipo.equalsIgnoreCase(Idl2XmlNames.OMG_union)
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_exception))) {
                // Forward declaration of a non forward
                NamedNodeMap nnm = nt.getAttributes();
                if (nnm != null) {
                    temp = nnm.getNamedItem(Idl2XmlNames.OMG_name);
                    if (temp != null) {
                        nombre = temp.getNodeValue();

                        bufferTemp.append("\t\tclass ");
                        bufferTemp.append(nombre);
                        bufferTemp.append(";\n");

                        bufferTemp.append("\t\ttypedef ");
                        bufferTemp.append(nombre);
                        bufferTemp.append("* ");
                        bufferTemp.append(nombre);
                        bufferTemp.append("_ptr;\n");

                        bufferTemp.append("\t\ttypedef ::TIDorb::templates::");
                        bufferTemp.append(getCppOMGName(nt));
                        bufferTemp.append("T_var<");
                        bufferTemp.append(nombre);
                        bufferTemp.append("> ");
                        bufferTemp.append(nombre);
                        bufferTemp.append("_var;\n");

                        if(XmlType2Cpp.isVariableSizeType(nt)) {
                            bufferTemp.append("\t\ttypedef ::TIDorb::templates::");
                            bufferTemp.append(getCppOMGName(nt));
                            bufferTemp.append("T_out<");
                            bufferTemp.append(nombre);
                            bufferTemp.append("> ");
                            bufferTemp.append(nombre);
                            bufferTemp.append("_out;\n");
                        } else {
                            bufferTemp.append("\t\ttypedef ");
                            bufferTemp.append(nombre);
                            bufferTemp.append(" &");                            
                            bufferTemp.append(nombre);
                            bufferTemp.append("_out;\n");
                        }                        

                        //if(tipo.equalsIgnoreCase(Idl2XmlNames.OMG_interface))
                        //bufferTemp.append("typedef
                        // ::TIDorb::templates::InterfaceT_ptr_SequenceMember<"
                        // + nombre + "> " + nombre +
                        // "_ptr_SequenceMember;\n\n");
                    }// temp!=null
                }//nnm!=null
            }// if tipo
        } // if forward
        else {// Is a forward Declaration.
            NamedNodeMap nnm = nt.getAttributes();
            if (nnm != null) {
                temp = nnm.getNamedItem(Idl2XmlNames.OMG_name);
                if (temp != null) {
                    nombre = temp.getNodeValue();
                    //Xml2Cpp.generateForwardUtilSimbols(genPackage+"::"+name,nombre);
                }
            }
        }

    }

    private static void generateChildrenHeaderElement(Element nt,
                                                      String genPackage,
                                                      String name,
                                                      StringBuffer bufferTemp,
                                                      String type,
                                                      Hashtable forwardElements)
    {

        String tipo = nt.getNodeName();
        if (tipo.equals(Idl2XmlNames.OMG_state_member)) {
            nt = (Element) nt.getFirstChild();
            tipo = nt.getNodeName();
        }
        String forward = "false";
        String nombre = nt.getAttribute(Idl2XmlNames.OMG_name);
        String route = getRouteToHere(name, genPackage);
        String nombreCompleto = route.equals("") ? nombre : route
                                                            + File.separator
                                                            + nombre;

        forward = nt.getAttribute(Idl2XmlNames.OMG_fwd);

        //if (!forwardElements.containsKey(nombre)) {
        if (forward != null && !forward.equalsIgnoreCase("true")) {

            /*
             * if(forward!=null && forward.equalsIgnoreCase("true"))
             * forwardElements.put(nombre,"");
             */

            if ((tipo.equalsIgnoreCase(Idl2XmlNames.OMG_interface))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_module))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_enum))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_const_dcl) 
                && (type.equals(Idl2XmlNames.OMG_module) 
                || type.equals(Idl2XmlNames.OMG_specification))) 
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_struct))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_union))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_exception))
                || (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_valuetype))) {
                if ((type.equals(Idl2XmlNames.OMG_module) 
                    || type.equals(Idl2XmlNames.OMG_specification))
                    && (nombre.equals("CORBA") || nombre.equals("java"))) 
                    // "java es incluido para interoperabilidad con RMI
                    return;
				// Fix bug #432: IDL constants declared outside of a module are not translated into C++ code
				// Check if it is a global constant and then mark it
                if ( tipo.equalsIgnoreCase(Idl2XmlNames.OMG_const_dcl) &&
                     name.equals("") && route.equals("") ) {
                    bufferTemp.append("\t\t#include \"" + nombreCompleto
                                  + ".h\" // global const \n");
                }
                else {
                    bufferTemp.append("\t\t#include \"" + nombreCompleto
                                  + ".h\" \n");
                }

                String localS = nt.getAttribute(Idl2XmlNames.OMG_local);
                if (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_interface)
                    && !localS.equals("true")) {
                    // Stub Inclusion.
                    String stub = "_" + nombre + "Stub";
                    bufferTemp
                        .append("\t\t// Interface Stub Inclusion (Client Side).\n");
                    bufferTemp.append("\t\t#include \"");
                    bufferTemp.append(getRouteToHere(stub, genPackage + "::"
                                                           + name));
                    bufferTemp.append(".h\"\n");
                }
            }//tipo.equals....
            else if (tipo.equalsIgnoreCase(Idl2XmlNames.OMG_typedef)) {
                NodeList inL = nt.getChildNodes();
                for (int j = 1; j < inL.getLength(); j++) {
                    Element decl = (Element) inL.item(j);
                    String typeName = decl.getAttribute(Idl2XmlNames.OMG_name);
                    String completeTypeName = route.equals("") ? typeName
                        : route + File.separator + typeName;
                    bufferTemp.append("\t\t#include \"" + completeTypeName
                                      + ".h\" \n");
                }// del for .
            }
        } // End of isn't a forward declaration.
    }

    /**
     * This methods converts InterfaceOperations into INTERFACE_OPERATIONS
     */
    private static String getUpperCaseName(String hierarchy)
    {
        String temp = "";
        char prev = '\0';
        for (int i = 0; i < hierarchy.length(); i++) {
            char ch = hierarchy.charAt(i); // The first character [0] must be
                                           // the same.
            /* if (Character.isUpperCase(ch) && (prev != '_') && (i != 0)
                && !Character.isUpperCase(prev))
                temp += "_";*/
            temp += Character.toUpperCase(ch);
            prev = ch;
        }
        return temp;
    }

    private static String getHierarchy(String genPackage, String type)
    {
        String hierarchy = "";
        StringTokenizer tokenizer = new StringTokenizer(genPackage, "::");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            hierarchy += token.toUpperCase() + "_";
        };

        if (type.equals("skeleton") || type.equals("tie"))
            hierarchy = "POA_" + hierarchy;
        return hierarchy;
    }

    private static String getFileHierarchy(String genPackage, String type)
    {
        String fileHierarchy = "";
        StringTokenizer tokenizer = new StringTokenizer(genPackage, "::");
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            fileHierarchy += token;
            if (tokenizer.hasMoreTokens())
                fileHierarchy += File.separator;
        };
        if ((type.equals("skeleton") || type.equals("tie"))
            && !fileHierarchy.equals(""))
            fileHierarchy = "POA_" + fileHierarchy;
        return fileHierarchy;
    }

}// class.
