/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 197 $
* Date: $Date: 2007-05-10 12:15:19 +0200 (Thu, 10 May 2007) $
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

package es.tid.TIDIdlc.async;

import es.tid.TIDIdlc.*;

import java.io.*;

/**
 * Generates IDL headers for all classes.
 */
class XmlIdlHeaderGenerator
{

    /** File header generation */
    public static void generate(StringBuffer buffer, String type, String name)
    {
        buffer.append("//\n");
        buffer.append("// " + name + " (" + type + ")\n");
        buffer.append("//\n");
        buffer.append("// File generated: ");
        java.util.Date current = new java.util.Date();
        buffer.append(current);
        buffer.append("\n");
        buffer.append("//   by TIDorb idl2java " + 
                      CompilerConf.st_compiler_version + "\n");
        buffer.append("//\n\n");
    }

}

