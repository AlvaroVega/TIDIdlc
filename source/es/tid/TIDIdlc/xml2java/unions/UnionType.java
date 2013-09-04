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

package es.tid.TIDIdlc.xml2java.unions;

import es.tid.TIDIdlc.xml2java.*;
import es.tid.TIDIdlc.idl2xml.*;
import es.tid.TIDIdlc.xml2java.structures.*;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for unions declarations.
 */
public class UnionType extends StructProcessor
    implements Idl2XmlNames
{

    private Vector m_switch_body;

    private Union m_union;

    private String m_discriminator_type;

    public UnionType(Union union, String discriminatorType)
    {
        this.m_union = union;
        this.m_switch_body = union.getSwitchBody();
        this.m_discriminator_type = discriminatorType;
    }

    public void generateJava(StringBuffer buffer, String objectName,
                             Element classType, Vector indexes, Vector isArray)
        throws Exception
    {

        UnionCase union_case = (UnionCase) m_switch_body.elementAt(m_actual);

        Element union_el = m_union.getUnionElement();
        Element switch_el = (Element) union_el.getFirstChild();
        Element discriminator_type = (Element) switch_el.getFirstChild();
        String type = null;
        String javaType = null;
        String value = null;
        Vector case_labels = union_case.m_case_labels;
        boolean isDefault = union_case.m_is_default;
        for (int numberOfCaseLabels = 0; (numberOfCaseLabels < case_labels.size())
                                         || isDefault; numberOfCaseLabels++, isDefault = false) {
            buffer.append("       org.omg.CORBA.Any _any" + m_labels
                          + " = _orb().create_any();\n");
            if (union_case.m_is_default) {
                type = "octet";
                value = "(byte)0";
                buffer.append("       _any" + m_labels + ".insert_" + type
                              + "(" + value + ");\n");
            } else {
                Element expr = (Element) case_labels
                    .elementAt(numberOfCaseLabels);
                if (discriminator_type.getTagName().equals(OMG_scoped_name)) {
                    type = XmlType2Java.basicORBTypeMapping(discriminator_type);
                    value = expr.getAttribute(OMG_name);
                    if ((value == null) || value.equals("") || 
                        type.equals("ushort") || type.equals("ulong")
                        || type.equals("ulonglong") || type.equals("longlong")
                        || XmlType2Java.isPrimitiveJavaType(type)) {
                        javaType = XmlType2Java.getType(discriminator_type); /* basicMapping */
                        value = null;
                        try {
                            Object obj_expr = XmlExpr2Java.getExpr(expr
                                .getParentNode(), m_union.getDiscKind());
                            value = obj_expr.toString();
                        }
                        catch (SemanticException e) {}
                        if (javaType.equals("boolean"))
                            value = expr.getAttribute(OMG_value).toLowerCase();
                        if (javaType.equals("char")) {
                            value = "'" + value + "'";
                            buffer.append("       _any" + m_labels + ".insert_"
                                          + type + "((" + javaType + ") "
                                          + value + ");\n");
                        } else {
                            if (javaType.equals("long"))
                                value += "L";
                            buffer.append("       _any" + m_labels + ".insert_"
                                          + type + "((" + javaType + ") "
                                          + value.toLowerCase() + ");\n");
                        }
                    } else {
                        StringTokenizer tokenizer = new StringTokenizer(
                                                                        value,
                                                                        Scope.SEP);
                        String label = null;
                        while (tokenizer.hasMoreTokens()) {
                            label = (String) tokenizer.nextToken();
                        }
                        buffer.append("       " + m_discriminator_type
                                      + "Helper.insert(_any" + m_labels + ", ");
                        buffer.append(m_discriminator_type + "." + label
                                      + ");\n");
                    }
                } else if (discriminator_type.getTagName().equals(OMG_enum)) {
                    value = expr.getAttribute(OMG_name);
                    StringTokenizer tokenizer = new StringTokenizer(value,
                                                                    Scope.SEP);
                    String label = null;
                    while (tokenizer.hasMoreTokens()) {
                        label = (String) tokenizer.nextToken();
                    }
                    buffer.append("       " + m_discriminator_type
                                  + "Helper.insert(_any" + m_labels + ", ");
                    buffer.append(m_discriminator_type + "." + label + ");\n");
                } else {
                    type = XmlType2Java.basicORBTypeMapping(discriminator_type);
                    javaType = XmlType2Java.basicMapping(discriminator_type.getAttribute(OMG_kind));
                    value = null;
                    try {
                        Object obj_expr = XmlExpr2Java.getExpr(expr.getParentNode(), m_union.getDiscKind());
                        value = obj_expr.toString();
                    }
                    catch (SemanticException e) {}
                    if (javaType.equals("boolean")) {
                        value = expr.getAttribute(OMG_value);
                    }
                    if (javaType.equals("char")) {
                        value = "'" + value + "'";
                        buffer.append("       _any" + m_labels + ".insert_"
                                      + type + "((" + javaType + ") " + value
                                      + ");\n");
                    } else {
                        if (javaType.equals("long"))
                            value += "L";
                        buffer.append("       _any" + m_labels + ".insert_"
                                      + type + "((" + javaType + ") "
                                      + value.toLowerCase() + ");\n");
                    }
                }
            }

            if (objectName != null) {
                buffer.append("       members[");
                buffer.append(m_labels);
                buffer.append("] = new org.omg.CORBA.UnionMember(\"");
                buffer.append(objectName);
                buffer.append("\", ");
            } else {
                buffer.append("      org.omg.CORBA.TypeCode original_type = ");
            }

            buffer.append("_any" + m_labels + ", ");

            if (indexes.size() == 0) {
                buffer.append(XmlType2Java.getTypecode(classType));
            } else {
                for (int i = 0; i < indexes.size(); i++) {
                    if (indexes.elementAt(i) instanceof Long) {
                        if ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) || 
                        		XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring))  &&
								!((Element) classType.getParentNode()).getTagName().equals(Idl2XmlNames.OMG_sequence) &&
								!((Element) classType.getNextSibling()).getTagName().equals(Idl2XmlNames.OMG_array)) {
                            // We have a bounded string
                            buffer.append("_orb().create_string_tc(");
                            buffer.append(indexes.elementAt(i));
                            if (objectName != null)
                                buffer.append("),null);\n");
                            else
                                buffer.append(");\n");
                        } else {
                            Boolean isThisAnArray = (Boolean) isArray
                                .elementAt(i);
                            if (isThisAnArray.booleanValue())
                                buffer.append("_orb().create_array_tc(");
                            else
                                buffer.append("_orb().create_sequence_tc(");
                            buffer.append(indexes.elementAt(i));
                            buffer.append(", ");
                        }
                    } else {
                        buffer.append("_orb().create_sequence_tc(0 , ");
                    }
                }
                if ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) ||
                		XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring)) &&
						!((Element) classType.getParentNode()).getTagName().equals(Idl2XmlNames.OMG_sequence) &&
						!((Element) classType.getNextSibling()).getTagName().equals(Idl2XmlNames.OMG_array)) {
                	} else {
                    buffer.append(XmlType2Java.getTypecode(classType));
                    for (int i = 0; i < indexes.size(); i++) {
                        buffer.append(")");
                    }
                }
            }
            if ((XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_string) ||
            		XmlType2Java.getTypedefType(classType).equals(Idl2XmlNames.OMG_wstring)) &&
					(classType.getFirstChild() != null)) {
            	} else if (objectName != null) {
                buffer.append(", null);\n");
            } else {
                buffer.append(";\n");
            }
            m_labels++;
        }
        m_actual++;
    }

    public void generateJava(String unionName, StringBuffer buffer,
                             String objectName, Element classType,
                             Vector indexes, Vector isArray)
        throws Exception
    {

        this.m_union_name = unionName;
        this.generateJava(buffer, objectName, classType, indexes, isArray);
    }

    private String m_union_name = null;

    private int m_actual = 0;

    private int m_labels = 0;
}