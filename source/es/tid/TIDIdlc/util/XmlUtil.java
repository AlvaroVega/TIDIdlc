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

package es.tid.TIDIdlc.util;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;

/**
 * @author rafa
 *
 */
public class XmlUtil implements Idl2XmlNames
{
    
    public static String getIdlFileName(Element elem) {
        String file_name = null;
        Element current = elem;

        if(!current.getTagName().equals(OMG_specification)) {

            // file_name attribute is only stored at 'module' level

            while ( (current!=null) && (!current.getTagName().equals(OMG_module)) ) {

                // Avoid ClassCastException if node is not an element
                //current = current.getParentNode();

                Node node = current.getParentNode();

                if (node.getNodeType() == Node.ELEMENT_NODE) 
                    current = (Element) node; 
                else
                    current = null;
            }

            if (current!=null) {
                file_name = current.getAttribute(OMG_file_name);
            }

        }

        return file_name;
    }
}
