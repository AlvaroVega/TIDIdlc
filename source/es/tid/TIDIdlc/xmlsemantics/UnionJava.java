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

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import es.tid.TIDIdlc.xml2java.XmlExpr2Java;
import es.tid.TIDIdlc.xml2java.XmlType2Java;

import java.math.BigInteger;
import java.util.Vector;

public class UnionJava extends Union
{
    //	boolean minus = false;
    public UnionJava(Scope father_scope, Element union_element)
    {
        super(father_scope, union_element);
    }

    public void checkCaseLabel(Element switch_el, Scope union_scope)
        throws SemanticException
    {

        Element union_el = union_scope.getElement();
        //NodeList expr_list = union_el.getElementsByTagName(OMG_expr); // DAVV
        // - la que se arma si hay un miembro union dentro del union!!
        Vector expr_list = new Vector();
        NodeList union_children = union_el.getChildNodes();
        for (int i = 0; i < union_children.getLength(); i++)
            if (((Element) union_children.item(i))
                .getTagName().equals(OMG_case)) {
                NodeList case_children = union_children.item(i).getChildNodes();
                for (int j = 0; j < case_children.getLength(); j++)
                    if (((Element) case_children.item(j))
                        .getTagName().equals(OMG_expr))
                        expr_list.add(case_children.item(j).getFirstChild()); 
                                          // construimos vector con los 'expr'
                                          // de los 'case'
            }

        Element discriminator_type = (Element) switch_el.getFirstChild();

        if (discriminator_type.getTagName().equals(OMG_enum)) {
            // ********* We have an enumeration Discriminator (not scoped)
            // **************
            m_enum_values = ((Element) union_el.getFirstChild())
                .getElementsByTagName(OMG_enumerator);
            //this.scoped_name = ((Element)
            // switch_el.getFirstChild()).getAttribute(OMG_name); // DAVV - se
            // asigna anteriormente, en XmlSemantcis
            //for (int i=0; i<expr_list.getLength(); i++) { // DAVV
            for (int i = 0; i < expr_list.size(); i++) {
                Element expr = (Element) expr_list.get(i);
                if (!expr.getTagName().equals(OMG_scoped_name)) {
                    String expr_type = expr.getTagName();
                    SemanticException se = new SemanticException(
                                                    "The identifier of the kind '"
                                                    + expr_type
                                                    + "' isn't a valid value for the enumeration '"
                                                    + this.m_scoped_name
                                                    + "'.");
                    se.locate(expr);
                    throw se;
                }
                // Check enum range
                checkEnumRange(expr);
            }
        } else {
            String discrim_kind = null;

            if (discriminator_type.getTagName().equals(OMG_scoped_name))
                discrim_kind = m_scoped_discrim_kind;

            if (discriminator_type.getTagName().equals(OMG_type))
                discrim_kind = discriminator_type.getAttribute(OMG_kind);

            if (!discrim_kind.equals(OMG_enum)) {
                // Scoped values in case labels (but not enum)
                NodeList scopelist2 = union_el
                    .getElementsByTagName(OMG_scoped_name);
                for (int i = 0; i < scopelist2.getLength(); i++) {
                    // por qué hacen esto aquí??? para qué sirve???
                    Element scoped_name_el = (Element) scopelist2.item(i);
                    String scoped_name = scoped_name_el.getAttribute(OMG_name);
                    String scoped_Name2 = union_scope
                        .getCompleteName(scoped_name);
                    scoped_name_el.setAttribute(OMG_scoped_name, scoped_Name2);
                }
            }

            //for (int i=0; i<expr_list.getLength(); i++) { // DAVV
            for (int i = 0; i < expr_list.size(); i++) {
                //Element expr = (Element)
                // ((Element)expr_list.item(i)).getFirstChild(); // DAVV
                Element expr = (Element) expr_list.get(i);
                if (expr.getTagName().equals(OMG_minus))
                    expr = (Element) expr.getFirstChild();

                String expr_type = expr.getTagName();
                // If we have a scoped discriminator we must check its type with
                // the type of the case expresions
                try {
                    if (discrim_kind.equals(OMG_short)) {
                        //if (((Element)
                        // expr_list.item(i).getParentNode()).getTagName().equals(OMG_case))
                        // { 
                        //	String val =
                        // String.valueOf(XmlExpr2Java.getIntExpr(expr_list.item(i)));
                        String val = String.valueOf(XmlExpr2Java
                            .getIntExpr((Node) expr_list.get(i)));
                        // Check short range
                        checkShortRange(val);
                    } else if (discrim_kind.equals(OMG_long)) {
                        //if (((Element)
                        // expr_list.item(i).getParentNode()).getTagName().equals(OMG_case))
                        // { 
                        //	String val =
                        // String.valueOf(XmlExpr2Java.getIntExpr(expr_list.item(i)));
                        String val = String.valueOf(XmlExpr2Java
                            .getIntExpr((Node) expr_list.get(i)));
                        // Check long range
                        checkLongRange(val);
                    } else if (discrim_kind.equals(OMG_longlong)) {
                        //if (((Element)
                        // expr_list.item(i).getParentNode()).getTagName().equals(OMG_case))
                        // { 
                        //	String val =
                        // String.valueOf(XmlExpr2Java.getIntExpr(expr_list.item(i)));
                        String val = String.valueOf(XmlExpr2Java
                            .getIntExpr((Node) expr_list.get(i)));
                        // Check longlong range
                        checkLongLongRange(val);
                    } else if (discrim_kind.equals(OMG_unsignedshort)) {
                        //if (((Element)
                        // expr_list.item(i).getParentNode()).getTagName().equals(OMG_case))
                        // { // DAVV
                        //	String val =
                        // String.valueOf(XmlExpr2Java.getIntExpr(expr_list.item(i)));
                        String val = String.valueOf(XmlExpr2Java
                            .getIntExpr((Node) expr_list.get(i)));
                        // Check unsigned short range
                        checkUShortRange(val);
                    } else if (discrim_kind.equals(OMG_unsignedlong)) {
                        //if (((Element)
                        // expr_list.item(i).getParentNode()).getTagName().equals(OMG_case))
                        // { // DAVV
                        //	String val =
                        // String.valueOf(XmlExpr2Java.getIntExpr(expr_list.item(i)));
                        String val = String.valueOf(XmlExpr2Java
                            .getIntExpr((Node) expr_list.get(i)));
                        // Check unsigned long range
                        checkULongRange(val);
                    } else if (discrim_kind.equals(OMG_unsignedlonglong)) {
                        //if (((Element)
                        // expr_list.item(i).getParentNode()).getTagName().equals(OMG_case))
                        // { 
                        //	String val =
                        // String.valueOf(XmlExpr2Java.getIntExpr(expr_list.item(i)));
                        String val = String.valueOf(XmlExpr2Java
                            .getIntExpr((Node) expr_list.get(i)));
                        // Check unsigned long long range
                        checkULongLongRange(val);
                    } else if (discrim_kind.equals(OMG_boolean)) {
                        if (((Element) expr.getParentNode())
                            .getTagName().equals(OMG_minus))
                            throw new SemanticException(
                                          "In '"
                                          + union_el.getAttribute(OMG_name)
                                          + "' Union, the case expresion of the type 'minus' must be of type '"
                                          + discrim_kind
                                          + "'.");
                        if (!expr_type.equals(OMG_boolean_literal))
                            throw new SemanticException(
                                          "In '"
                                          + union_el.getAttribute(OMG_name)
                                          + "' Union, the case expresion of the type '"
                                          + expr_type
                                          + "' must be of type '"
                                          + discrim_kind
                                          + "'.");
                        // The boolean range is checked implicitly
                    } else if (discrim_kind.equals(OMG_char)) {
                        if (!expr_type.equals(OMG_character_literal))
                            throw new SemanticException(
                                          "In '"
                                          + union_el.getAttribute(OMG_name)
                                          + "' Union, the case expresion of the type '"
                                          + expr_type
                                          + "' must be of type '"
                                          + discrim_kind
                                          + "'.");
                        // Check char range
                    } else if (discrim_kind.equals(OMG_wchar)) {
                        if (!expr_type.equals(OMG_wide_character_literal))
                            throw new SemanticException(
                                          "In '"
                                          + union_el.getAttribute(OMG_name)
                                          + "' Union, the case expresion of the type '"
                                          + expr_type
                                          + "' must be of type '"
                                          + discrim_kind
                                          + "'.");
                        // Check wchar range
                    } else if (discrim_kind.equals(OMG_enum)) {
                        // scoped enumeration
                        if (!expr_type.equals(OMG_scoped_name))
                            throw new SemanticException(
                                          "In '"
                                          + union_el.getAttribute(OMG_name)
                                          + "' Union, the case expresion of the type '"
                                          + expr_type
                                          + "' must be of type '"
                                          + discriminator_type.getAttribute(OMG_name)
                                          + "'.");

                        String labelName = expr.getAttribute(OMG_name);
                        // Check enum range
                        checkEnumRange(expr);
                        // Add the labels to the union scope
                        //union_scope.add(labelName, Scope.KIND_ELEMENT);
                    }
                } //try
                catch (SemanticException ex) {
                    ex.locate(expr);
                    throw ex;
                }
                catch (Exception ex) {
                    String dis;
                    if (discrim_kind.equals(OMG_enum))
                        dis = discriminator_type.getAttribute(OMG_name);
                    else
                        dis = discrim_kind;
                    throw new SemanticException(
                                  "In '"
                                  + union_el.getAttribute(OMG_name)
                                  + "' Union, the case expresion must be of type '"
                                  + dis
                                  + "'.");
                }
            }
        }
    }

    public void checkCaseLabelValues()
        throws SemanticException
    {
        java.util.Hashtable table = new java.util.Hashtable();
        java.util.Vector union_cases = this.m_switch_body;
        long number_of_case_labels = 0;
        for (int i = 0; i < union_cases.size(); i++) {
            UnionCase union_case = (UnionCase) union_cases.elementAt(i);
            java.util.Vector case_labels = union_case.m_case_labels;
            for (int j = 0; j < case_labels.size(); j++) {
                Element el = (Element) case_labels.elementAt(j);
                String value = null;
                try {
                    Object expr = XmlExpr2Java.getExpr(el.getParentNode(),
                                                       getDiscKind());
                    value = expr.toString();
                }
                catch (SemanticException e) {}
                if ((value == null) || value.equals("")) {
                    value = el.getAttribute(OMG_name);
                }
                if ((value == null) || value.equals("")) {
                    // We have a boolean value (modificar con expr booleanas
                    value = el.getAttribute(OMG_value);
                }
                String unscopedValue = value;
                if (value.lastIndexOf("::") >= 0) // ya se comprobó
                                                  // anteriormente la validez
                                                  // del scope
                    unscopedValue = value
                        .substring(value.lastIndexOf("::") + 2); // por
                                                                 // eso pordemos
                                                                 // prescindir
                                                                 // de él sin
                                                                 // problemas
                if (table.containsKey(unscopedValue)) {
                    SemanticException se;
                    se = new SemanticException(
                                 "In Union '"
                                 + m_union_element.getAttribute(OMG_name)
                                 + "', there are two case labels with the same value: "
                                 + value);
                    se.locate(el);
                    throw se;
                }
                table.put(unscopedValue, "");
                number_of_case_labels++;
            }
        }
        BigInteger range = getDiscriminatorRange();
        if (range.equals(BigInteger.valueOf(number_of_case_labels))) {
            m_default_allowed = false;
            if (m_has_default) {
                SemanticException se = new SemanticException(
                                               "In Union '"
                                               + m_union_element.getAttribute(OMG_name)
                                               + "', the default case label is not allowed. The set of case labels covers all the posible values for the discriminant.");
                se.locate(m_union_element);
                throw se;
            }
        }
        m_default_value = calculateDefaultValue(table);
    }

    public String getDiscKind()
    {
        if (m_disc_kind == null) {
            Element switch_el = (Element) m_union_element
                .getElementsByTagName(OMG_switch).item(0);
            NodeList scopelist = switch_el
                .getElementsByTagName(OMG_scoped_name);
            String kind;
            if (scopelist.getLength() > 0) {
                // We have a scoped discriminator
                kind = this.m_scoped_discrim_kind;
                m_disc_kind = XmlType2Java.basicMapping(kind);
            } else if (((Element) switch_el.getFirstChild())
                .getTagName().equals(OMG_enum)) { // enum definido en el
                                                  // propio discriminante
                m_disc_kind = OMG_enum;
                m_scoped_discrim_kind = OMG_enum;
            } else {
                //kind = ((Element)
                // switch_el.getFirstChild()).getAtribute(OMG_kind);
                m_disc_kind = XmlType2Java.getType((Element) switch_el
                    .getFirstChild());
            }
        }
        return m_disc_kind;
    }
}