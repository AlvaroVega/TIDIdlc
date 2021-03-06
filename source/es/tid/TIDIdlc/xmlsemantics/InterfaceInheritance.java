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

package es.tid.TIDIdlc.xmlsemantics;

import es.tid.TIDIdlc.idl2xml.*;
import org.w3c.dom.*;

import java.util.Vector;

/**
 * Class for the Interface Inheritance. It checks the correctness of the derived
 * interface in various points: - Redefinition of operations or attributes in a
 * derived interface
 */

public class InterfaceInheritance
    implements Idl2XmlNames
{
    public static void checkRedefinitions(Element derived_interface,
                                          Vector inheritance_scopes)
        throws SemanticException
    {
        // Obtain all the fathers
        for (int k = 0; k < inheritance_scopes.size(); k++) {
            Scope father = (Scope) inheritance_scopes.elementAt(k);
            Element elfather = father.getElement();
            NodeList nodes = elfather.getChildNodes();
            // Obtains all the operations or attributes of the father
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String tag = el.getTagName();
                if (tag.equals(OMG_op_dcl)) {
                    // Compare with all the operations of the derived interface
                    NodeList derived_nodes = derived_interface.getChildNodes();
                    for (int j = 0; j < derived_nodes.getLength(); j++) {
                        Element child_el = (Element) derived_nodes.item(j);
                        String child_tag = child_el.getTagName();
                        if (child_tag.equals(OMG_op_dcl)) {
                            String child_op = child_el.getAttribute(OMG_name);
                            String father_op = el.getAttribute(OMG_name);
                            if (child_op.equals(father_op))
                                throw new SemanticException(
                                             "Redefinition of the operation '"
                                             + child_op
                                             + "' in the derived interface '"
                                             + derived_interface.getAttribute(OMG_name)
                                             + "'.",
                                             child_el);
                        }
                    }
                } else if (tag.equals(OMG_attr_dcl)) {
                    // Compare with all the attributes of the derived interface
                    NodeList derived_nodes = derived_interface.getChildNodes();
                    for (int j = 0; j < derived_nodes.getLength(); j++) {
                        Element child_el = (Element) derived_nodes.item(j);
                        String child_tag = child_el.getTagName();
                        if (child_tag.equals(OMG_attr_dcl)) {
                            /*
                             * String child_att= getAttributeName(child_el);
                             * String father_att = getAttributeName(el);
                             */
                            String child_att, father_att;
                            NodeList child_derived_nodes = 
                                child_el.getChildNodes();
                            NodeList father_derived_nodes = el.getChildNodes();
                            for (int index1 = 0; index1 < 
                                 child_derived_nodes.getLength(); index1++) {
                                Element el1 = 
                                    (Element) child_derived_nodes.item(index1);
                                String tag1 = el1.getTagName();
                                if (tag1.equals(OMG_attribute))
                                    child_att = el1.getAttribute(OMG_name);
                                else
                                    continue;
                                for (int index2 = 0; 
                                     index2 < father_derived_nodes.getLength(); 
                                     index2++) {
                                    Element el2 = 
                                        (Element) father_derived_nodes.item(index2);
                                    String tag2 = el2.getTagName();
                                    if (tag2.equals(OMG_attribute))
                                        father_att = el2.getAttribute(OMG_name);
                                    else
                                        continue;
                                    if (child_att.equals(father_att))
                                        throw new SemanticException(
                                                      "Redefinition of the attribute '"
                                                      + child_att
                                                      + "' in the derived interface '"
                                                      + derived_interface.getAttribute(OMG_name)
                                                      + "'.",
                                                      el1);
                                }

                            }

                        }
                    }
                }
            }
        }
    }
    // DAVV - usando este m�todo s�lo se comprobaba el primer nombre en caso de
    // una declaraci�n m�ltiple de atributos
    /*
     * private static String getAttributeName(Element attr_dcl) { NodeList
     * derived_nodes = attr_dcl.getChildNodes(); for (int j=0; j
     * <derived_nodes.getLength(); j++) { Element el =
     * (Element)derived_nodes.item(j); String tag = el.getTagName(); if
     * (tag.equals(OMG_attribute)) return el.getAttribute(OMG_name); } return
     * null; }
     */
}