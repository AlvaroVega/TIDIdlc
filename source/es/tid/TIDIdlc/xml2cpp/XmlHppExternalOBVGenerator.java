/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 103 $
* Date: $Date: 2006-01-24 17:45:04 +0100 (Tue, 24 Jan 2006) $
* Last modified by: $Author: avega $
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

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

/**
 * T?tulo: Idlc Compilador IDL a Java y C++ 
 */
import org.w3c.dom.*;

import java.io.File;

public class XmlHppExternalOBVGenerator extends XmlHppExternalPOAGenerator
    implements Idl2XmlNames
{

    public XmlHppExternalOBVGenerator()
    {}

    public static void generateHpp(Element doc, StringBuffer buffer,
                                   String type, String name, String genPackage)
    {

        String hierarchy = getHierarchy(name, genPackage);
        String prefix = ""; // sólo se antepone 'OBV_' si el módulo tiene
                            // como padre el scope global
        if (genPackage.equals(""))
            prefix = "OBV_";
        buffer.append("//\n");
        buffer.append("// OBV file " + prefix + name + ".h (from " + type
                      + ")\n"); // sólo se antepone 'OBV_' si el módulo
                                // tiene como padre el scope global
        buffer.append("//\n");
        buffer.append("// File generated: ");
        java.util.Date currentDate = new java.util.Date();
        buffer.append(currentDate);
        buffer.append("\n");
        //        buffer.append("//   by TIDorb idl2cpp 1.0.4 \n");
        buffer.append("//   by TIDorb idl2cpp " + CompilerConf.st_compiler_version + "\n");

        buffer.append("//   external OBV definition File.\n");
        buffer.append("//\n\n");

        String includeFile = ""; 
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
            buffer.append("#include \"OBV_" + parentFile
                          + ".h\"  // Parent File \n\n");
        
        buffer.append("\n#ifndef _OBV" + hierarchy);
        buffer.append("\n#define _OBV" + hierarchy + "\n\n");
        if (type.equals("module")) {
            if (parentFile.equals("")) 
                buffer.append("#include \"" + includeFile + ".h\"\n");
            buffer.append("namespace " + prefix + name + "\n{\n"); 
            // sólo se antepone 'OBV_' si el módulo tiene como
            // padre el scope global
        }

        includeOBVChildrenHeaderFiles(doc, buffer, name, genPackage);

        if (type.equals("module"))
            buffer.append("}\n");

        buffer.append("\n#endif // _OBV" + hierarchy + "\n\n");
    }

    private static void includeOBVChildrenHeaderFiles(Element doc,
                                                      StringBuffer buffer,
                                                      String name,
                                                      String genPackage)
    {
        // Include all of its Children.
        // Include children headers.
        NodeList nl = doc.getChildNodes();
        Element nt = null;
        String tipo;
        boolean isAbstract, isBoxed;
        String route = getRouteToHere(name, genPackage);
        boolean atLeastOne = false;
        StringBuffer bufferTemp = new StringBuffer();
        for (int i = 0; i < nl.getLength(); i++) {
            nt = (Element) nl.item(i);
            tipo = nt.getNodeName();
            if (tipo.equalsIgnoreCase(OMG_valuetype)) {
                isAbstract = nt.getAttribute(OMG_abstract).equals(OMG_true);
                isBoxed = nt.getAttribute(OMG_boxed).equals(OMG_true);
                if (!isAbstract && !isBoxed) {
                    bufferTemp.append("#include \"OBV_" + route
                                      + File.separator
                                      + nt.getAttribute(OMG_name) + ".h\" \n");
                    atLeastOne = true;
                }
            }
            if (tipo.equalsIgnoreCase(OMG_module)) {
                NodeList valuetypes = nt.getElementsByTagName(OMG_valuetype);
                boolean thereIsOneConcreteValuetype = false;
                for (int j = 0; j < valuetypes.getLength()
                                && !thereIsOneConcreteValuetype; j++) {
                    isAbstract = nt.getAttribute(OMG_abstract).equals(OMG_true);
                    isBoxed = nt.getAttribute(OMG_boxed).equals(OMG_true);
                    thereIsOneConcreteValuetype = !isAbstract && !isBoxed;
                }
                if (thereIsOneConcreteValuetype) {
                    bufferTemp.append("#include \"OBV_" + route
                                      + File.separator
                                      + nt.getAttribute(OMG_name) + ".h\" \n");
                    atLeastOne = true;
                }
            }
        }// for nl.getLength

        if (atLeastOne) {
            buffer.append(bufferTemp.toString());
        }
    }//end of method includeOBVChildrenHeaderFiles()

}
