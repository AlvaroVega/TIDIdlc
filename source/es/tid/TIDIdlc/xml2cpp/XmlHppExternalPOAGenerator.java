/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 51 $
* Date: $Date: 2005-06-29 10:20:22 +0200 (Wed, 29 Jun 2005) $
* Last modified by: $Author: caceres $
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

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

/**
 * T?tulo: Idlc Compilador IDL a Java y C++ 
 */
import org.w3c.dom.*;

import es.tid.TIDIdlc.xmlsemantics.TypedefManager;
import es.tid.TIDIdlc.xmlsemantics.Scope;

import java.io.File;

public class XmlHppExternalPOAGenerator
    implements Idl2XmlNames
{

    public XmlHppExternalPOAGenerator()
    {}

    public static void generateHpp(Element doc, StringBuffer buffer,
                                   String type, String name, String genPackage)
    {
        String hierarchy = getHierarchy(name, genPackage);
        //buffer.append("// External Operations definition & other children
        // header files inclusion\n");
        String prefix = ""; //  s�lo se antepone 'POA_' si el m�dulo tiene
                            // como padre el scope global
        if (genPackage.equals(""))
            prefix = "POA_";
        buffer.append("//\n");
        //buffer.append("// POA_"+name+".h (from "+type+")\n");
        buffer.append("// POA file " + prefix + name + ".h (from " + type
                      + ")\n"); // s�lo se antepone 'POA_' si el m�dulo
                                // tiene como padre el scope global
        buffer.append("//\n");
        buffer.append("// File generated: ");
        java.util.Date currentDate = new java.util.Date();
        buffer.append(currentDate);
        buffer.append("\n");
        buffer.append("//   by TIDorb idl2cpp " + CompilerConf.st_compiler_version + " \n");
        buffer.append("//   external POA definition File.\n");
        buffer.append("//\n\n");

        String includeFile = ""; // correccion en jerarquia de includes
                                 // de POAs
        String parentFile = "";
        java.util.StringTokenizer tok = new java.util.StringTokenizer(
                                                 genPackage, "::");
        while (tok.hasMoreTokens()) {
            String toAppend = tok.nextToken();
            includeFile += toAppend + java.io.File.separator;
            parentFile += toAppend;
            if (tok.countTokens() > 0)
                parentFile += java.io.File.separator;
        }
        includeFile += name;
        if (!parentFile.equals(""))
            buffer.append("#include \"POA_" + parentFile
                          + ".h\"  // Parent File \n\n");

        buffer.append("\n#ifndef _POA" + hierarchy);
        buffer.append("\n#define _POA" + hierarchy + "\n\n");
        if (type.equals("module")) {
            if (parentFile.equals("")) // correccion en jerarquia de
                                       // includes de POAs
                buffer.append("#include \"" + includeFile + ".h\"\n");
            buffer.append("#include \"PortableServer.h\"\n\n");
            //buffer.append("namespace POA_"+name+"\n{\n");
            buffer.append("namespace " + prefix + name + "\n{\n"); 
            // DAVV - s�lo se antepone 'POA_' si el m�dulo
            // tiene como padre el scope global
        }

        includePOAChildrenHeaderFiles(doc, buffer, name, genPackage);

        if (type.equals("module"))
            buffer.append("}\n");

        buffer.append("\n#endif // _POA" + hierarchy + "\n\n");
        //buffer.append("\n// End External Operations definition & other
        // children header files inclusion\n");
    }

    private static void includePOAChildrenHeaderFiles(Element doc,
                                                      StringBuffer buffer,
                                                      String name,
                                                      String genPackage)
    {
        // Include all of its Children.
        // Include children headers.
        NodeList nl = doc.getChildNodes();
        Element nt = null;
        Node temp = null;
        String tipo;
        String nombre;
        boolean isForward, isLocal,isAbstract;
        String route = getRouteToHere(name, genPackage);
        boolean atLeastOne = false;
        StringBuffer bufferTemp = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
            nt = (Element) nl.item(i);
            tipo = nt.getNodeName();
            if (tipo.equalsIgnoreCase(OMG_interface)) {
                isForward = nt.getAttribute(OMG_fwd).equals(OMG_true);
                isLocal = nt.getAttribute(OMG_local).equals(OMG_true);
                isAbstract = nt.getAttribute(OMG_abstract).equals(OMG_true);
                if (!isForward && !isLocal && !isAbstract) {
                    nombre = nt.getAttribute(OMG_name);
                    bufferTemp.append("#include \"POA_" + route
                                      + File.separator + nombre + ".h\" \n");
                    atLeastOne = true;
                }
            } else if (nt
                .getNodeName().equalsIgnoreCase(Idl2XmlNames.OMG_valuetype)) {
                Element inheritance = (Element) nt.getFirstChild();
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
                                    Element supportedDef = 
                                        inhScope.getElement();
                                    if (supportedDef.getTagName().equals(OMG_interface)
                                        && !supportedDef.getAttribute(OMG_abstract).equals(OMG_true))
                                        
                                    {
                                        bufferTemp.append("#include \"POA_" + route
                                                    + File.separator
                                                    + nt.getAttribute(OMG_name)
                                                    + ".h\" \n");
                                        atLeastOne = true;
                                    }
                                }
                            }
                        }
                    }
                }
            } else if (tipo.equalsIgnoreCase(OMG_module)) {
                NodeList interfaces = nt.getElementsByTagName(OMG_interface);
                boolean thereIsOnePOAInterface = false;
                for (int k = 0; k < interfaces.getLength()
                                && !thereIsOnePOAInterface; k++) {
                    Element anInterface = (Element) interfaces.item(k);
                    isLocal = 
                        anInterface.getAttribute(OMG_fwd).equals(OMG_true);
                    isForward = 
                        anInterface.getAttribute(OMG_local).equals(OMG_true);
                    thereIsOnePOAInterface = !isForward && !isLocal;
                }
                NodeList valuetypes = nt.getElementsByTagName(OMG_valuetype);
                boolean thereIsOnePOAValuetype = false;
                for (int j = 0; j < valuetypes.getLength()
                                && !thereIsOnePOAValuetype; j++) {
                    Element valuetype = (Element) valuetypes.item(j);
                    Element inheritance = (Element) valuetype.getFirstChild();
                    if (inheritance.getTagName().equals(OMG_value_inheritance_spec)) {
                        NodeList parents = inheritance.getChildNodes();
                        for (int k = 0; k < parents.getLength(); k++) {
                            Element parent = (Element) parents.item(k);
                            if (parent.getTagName().equals(OMG_supports)) {
                                NodeList supports = parent.getChildNodes();
                                for (int l = 0; l < supports.getLength(); l++) {
                                    Element supported = 
                                        (Element) supports.item(l);
                                    if (supported.getTagName().equals(OMG_scoped_name)) {
                                        Scope inhScope = 
                                            Scope.getGlobalScopeInterface(
                                                supported.getAttribute(OMG_name));
                                        Element supportedDef = 
                                            inhScope.getElement();
                                        thereIsOnePOAValuetype = (
                                            supportedDef.getTagName().equals(OMG_interface) 
                                            && !supportedDef.getAttribute(
                                                 OMG_abstract).equals(OMG_true));
                                    }
                                }
                            }
                        }
                    }
                }
                if (thereIsOnePOAInterface || thereIsOnePOAInterface) {
                    nombre = nt.getAttribute(OMG_name);
                    bufferTemp.append("#include \"POA_" + route
                                      + File.separator + nombre + ".h\" \n");
                    atLeastOne = true;
                }
            }
        }// for nl.getLength
        if (atLeastOne) {
            buffer.append(bufferTemp.toString());
        }
    }//end of method includeChildrenHeaderFiles()

    protected static String getRouteToHere(String name, String genPackage)
    {
        java.util.StringTokenizer st = new java.util.StringTokenizer(
                                                         genPackage, "::");
        String route = "";
        while (st.hasMoreTokens())
            route += st.nextToken() + File.separator;
        route += name;
        return route;
    }// end of method getRouteToHere.

    protected static String getUpperCaseHierarchy(String hierarchy)
    {
        // MegaCorba --> MEGA_CORBA_EXT_H
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
        temp += "_H_";
        return temp;
    }// end of method getUpperCaseHierarchy()

    protected static String getHierarchy(String name, String genPackage)
    {
        java.util.StringTokenizer st = new java.util.StringTokenizer(
                                                         genPackage, "::");
        String route = "";
        while (st.hasMoreTokens())
            route += st.nextToken() + "_";
        route += name;
        return getUpperCaseHierarchy(route);
    }
}