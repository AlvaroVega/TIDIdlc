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

package es.tid.TIDIdlc.xmlsemantics;

import es.tid.TIDIdlc.idl2xml.*;
import org.w3c.dom.*;

import java.util.Vector;

/**
 * Class for the Valuetype Inheritance. It checks the correctness of the derived
 * valuetype in various points: - Redefinition of state member, factories,
 * operations or attributes in a derived valuetype
 */

public class ValuetypeInheritance
    implements Idl2XmlNames
{
    public static void checkRedefinitions(Element derived_valuetype,
                                          Vector inheritance_scopes)
        throws SemanticException
    {
        // Obtain all the fathers
        for (int k = 0; k < inheritance_scopes.size(); k++) {
            Scope father = (Scope) inheritance_scopes.elementAt(k);
            Element elfather = father.getElement();
            NodeList nodes = elfather.getChildNodes();
            // Obtains all the state members, factories, operations or
            // attributes of the father
            for (int i = 0; i < nodes.getLength(); i++) {
                Element father_el = (Element) nodes.item(i);
                String tag = father_el.getTagName();
                if (tag.equals(OMG_op_dcl)) {
                    // Compare with all the operations of the derived valuetype
                    NodeList derived_nodes = derived_valuetype.getChildNodes();
                    for (int j = 0; j < derived_nodes.getLength(); j++) {
                        Element child_el = (Element) derived_nodes.item(j);
                        String child_tag = child_el.getTagName();
                        if (child_tag.equals(OMG_op_dcl)) {
                            String child_op = child_el.getAttribute(OMG_name);
                            String father_op = father_el.getAttribute(OMG_name);
                            if (child_op.equals(father_op))
                                throw new SemanticException(
                                              "Redefinition of the operation '"
                                              + child_op
                                              + "' in the derived valuetype '"
                                              + derived_valuetype.getAttribute(OMG_name)
                                              + "'.");
                        }
                    }
                } else if (tag.equals(OMG_attr_dcl)) {
                    // Compare with all the attributes of the derived valuetype
                    NodeList derived_nodes = derived_valuetype.getChildNodes();
                    for (int j = 0; j < derived_nodes.getLength(); j++) {
                        Element child_el = (Element) derived_nodes.item(j);
                        String child_tag = child_el.getTagName();
                        if (child_tag.equals(OMG_attr_dcl)) {
                            String redefinition = checkAttribute(father_el,
                                                                 child_el);
                            if (redefinition != null)
                                throw new SemanticException(
                                              "Redefinition of the atribute '"
                                              + redefinition
                                              + "' in the derived valuetype '"
                                              + derived_valuetype.getAttribute(OMG_name)
                                              + "'.");
                        }
                    }
                } else if (tag.equals(OMG_state_member)) {
                    // Compare with all the state members of the derived
                    // valuetype
                    NodeList derived_nodes = derived_valuetype.getChildNodes();
                    for (int j = 0; j < derived_nodes.getLength(); j++) {
                        Element child_el = (Element) derived_nodes.item(j);
                        String child_tag = child_el.getTagName();
                        if (child_tag.equals(OMG_state_member)) {
                            String redefinition = checkStateMember(father_el,
                                                                   child_el);
                            if (redefinition != null)
                                throw new SemanticException(
                                              "Redefinition of the state member '"
                                              + redefinition
                                              + "' in the derived valuetype '"
                                              + derived_valuetype.getAttribute(OMG_name)
                                              + "'.");
                        }
                    }
                } else if (tag.equals(OMG_factory)) {
                    // Compare with all the factories of the derived valuetype
                    NodeList derived_nodes = derived_valuetype.getChildNodes();
                    for (int j = 0; j < derived_nodes.getLength(); j++) {
                        Element child_el = (Element) derived_nodes.item(j);
                        String child_tag = child_el.getTagName();
                        if (child_tag.equals(OMG_factory)) {
                            String child_fact = child_el.getAttribute(OMG_name);
                            String father_fact = father_el
                                .getAttribute(OMG_name);
                            if (child_fact.equals(father_fact))
                                throw new SemanticException(
                                              "Redefinition of the factory '"
                                              + child_fact
                                              + "' in the derived valuetype '"
                                              + derived_valuetype.getAttribute(OMG_name)
                                              + "'.");
                        }
                    }
                }
            }
        }
    }

    public static void checkInheritance(Element derived_valuetype,
                                        Vector inheritance_scopes,
                                        String truncatable)
        throws SemanticException
    {
        // Obtain all the fathers
        int interfacecount = 0, statefulcount = 0;
        String childIsAbstractS = derived_valuetype.getAttribute(OMG_abstract);
        boolean childIsAbstract = (childIsAbstractS != null)
                                  && (childIsAbstractS.equals(OMG_true));
        for (int k = 0; k < inheritance_scopes.size(); k++) {
            Scope father = (Scope) inheritance_scopes.elementAt(k);
            Element elfather = father.getElement();
            String type = elfather.getTagName();
            String parentIsAbstractS = elfather.getAttribute(OMG_abstract);
            boolean parentIsAbstract = (parentIsAbstractS != null)
                                       && (parentIsAbstractS.equals(OMG_true));
            if (type.equals(OMG_interface)) {
                if (!parentIsAbstract) {
                    if (interfacecount > 0)
                        throw new SemanticException(
                                      "'"
                                      + derived_valuetype.getAttribute(OMG_name)
                                      + "' can´t support multiple non-abstract interfaces");
                    else
                        interfacecount++;
                }
            } else if (type.equals(OMG_valuetype)) {
                String parentIsBoxedS = elfather.getAttribute(OMG_boxed);
                boolean parentIsBoxed = (parentIsBoxedS != null)
                                        && (parentIsBoxedS.equals(OMG_true));
                if (parentIsBoxed)
                    throw new SemanticException(
                                  "'"
                                  + derived_valuetype.getAttribute(OMG_name)
                                  + "' can´t inherits from a boxed valuetype");
                if (!parentIsAbstract) {
                    if (childIsAbstract)
                        throw new SemanticException(
                                      "'"
                                      + derived_valuetype.getAttribute(OMG_name)
                                      + "' can´t inherits from a stateful valuetype");
                    else {
                        if (statefulcount > 0)
                            throw new SemanticException(
                                          "'"
                                          + derived_valuetype.getAttribute(OMG_name)
                                          + "' can´t inherits from multiple stateful valuetypes");
                        else
                            statefulcount++;
                    }
                }
            }
        }
    }

    private static String checkStateMember(Element e1, Element e2)
        throws SemanticException
    {
        NodeList e1_nodes = e1.getChildNodes();
        NodeList e2_nodes = e2.getChildNodes();
        for (int i = 0; i < e1_nodes.getLength(); i++) {
            Element el1 = (Element) e1_nodes.item(i);
            String el1_tag = el1.getTagName();
            if (el1_tag.equals(OMG_simple_declarator)) {
                String el1_name = el1.getAttribute(OMG_name);
                for (int j = 0; j < e2_nodes.getLength(); j++) {
                    Element el2 = (Element) e2_nodes.item(j);
                    String el2_tag = el2.getTagName();
                    if (el2_tag.equals(OMG_simple_declarator)
                        && el1_name.equals(el2.getAttribute(OMG_name)))
                        return el1_name;
                }
            }
        }
        return null;
    }

    private static String checkAttribute(Element e1, Element e2)
        throws SemanticException
    {
        NodeList e1_nodes = e1.getChildNodes();
        NodeList e2_nodes = e2.getChildNodes();
        for (int i = 0; i < e1_nodes.getLength(); i++) {
            Element el1 = (Element) e1_nodes.item(i);
            String el1_tag = el1.getTagName();
            if (el1_tag.equals(OMG_attribute)) {
                String el1_name = el1.getAttribute(OMG_name);
                for (int j = 0; j < e2_nodes.getLength(); j++) {
                    Element el2 = (Element) e2_nodes.item(j);
                    String el2_tag = el2.getTagName();
                    if (el2_tag.equals(OMG_attribute)
                        && el1_name.equals(el2.getAttribute(OMG_name)))
                        return el1_name;
                }
            }
        }
        return null;
    }

}