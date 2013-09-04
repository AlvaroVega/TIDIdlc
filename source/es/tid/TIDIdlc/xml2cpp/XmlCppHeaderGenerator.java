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

import java.io.*;
import org.w3c.dom.*;

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

/**
 * Generates Cpp headers for all classes.
 * <p>
 */
class XmlCppHeaderGenerator
{

    /** File header generation */
    public static void generate(StringBuffer buffer, String type, String name,
                                String genPackage)
    {
        buffer.append("//\n");
        buffer.append("// " + name + ".C (" + type + ")\n");
        buffer.append("//\n");
        buffer.append("// File generated: ");
        java.util.Date current = new java.util.Date();
        buffer.append(current);
        buffer.append("\n");
        buffer.append("//   by TIDIdlc idl2cpp " + CompilerConf.st_compiler_version + "\n");
        buffer.append("//\n\n");
        buffer.append("#include \"" + getRouteToHere(name, genPackage)
                      + ".h\" \n");

        // solo en interfaces de ambito global
        if (genPackage.equals("") && type.equals(Idl2XmlNames.OMG_interface))
            buffer.append("#include \"_" + name + "Stub.h\"\n");

        buffer.append("\n#include \"TIDorb" + File.separator + "portable"
                      + File.separator + "TypeCodeFactory.h\" \n\n");
    }

    private static String getRouteToHere(String name, String genPackage)
    {
        java.util.StringTokenizer st = 
            new java.util.StringTokenizer(genPackage, "::");
        String route = "";
        while (st.hasMoreTokens()) {
            route += st.nextToken() + File.separator;
        }
        route += name;
        return route;
    }// end of method getRouteToHere.

}

