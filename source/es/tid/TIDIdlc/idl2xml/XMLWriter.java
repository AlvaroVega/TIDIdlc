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

package es.tid.TIDIdlc.idl2xml;

import org.w3c.dom.*;
import java.io.*;

/**
 * Xml writer. Writes a DOM Nodes to a PrintStream.
 */

public class XMLWriter
{

    public XMLWriter(Document dom)
    {
        this.dom = dom;
    }

    public void write(PrintStream out)
    {
        this.out = out;
        write(dom.getDocumentElement(), 0);
    }

    private void write(Node node, int ident)
    {
        for (int i = 0; i < ident; i++)
            out.print(" ");
        out.print("<");
        out.print(node.getNodeName());

        NamedNodeMap attribs = node.getAttributes();
        for (int i = 0; i < attribs.getLength(); i++) {
            out.print(" " + attribs.item(i).getNodeName() + "=\""
                      + attribs.item(i).getNodeValue() + "\"");
        }

        NodeList nodes = node.getChildNodes();

        if (nodes.getLength() > 0) {
            out.print(">\n");
            for (int i = 0; i < nodes.getLength(); i++)
                write(nodes.item(i), ident + 2);
            for (int i = 0; i < ident; i++)
                out.print(" ");
            out.print("</");
            out.print(node.getNodeName());
        } else {
            out.print("/");
        }

        out.print(">\n");
    }

    Document dom;

    PrintStream out;
}