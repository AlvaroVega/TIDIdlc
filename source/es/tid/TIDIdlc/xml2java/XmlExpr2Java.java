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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.xml2java.TypeManager;

import java.io.*;
import java.util.StringTokenizer;
import java.math.BigDecimal;

import org.w3c.dom.*;

/**
 * Generates Java for expresions.
 */
public class XmlExpr2Java
    implements Idl2XmlNames
{

    public static String toString(Object expr, String typeExpr)
    {
        if (expr instanceof java.math.BigDecimal) {
            return "new java.math.BigDecimal(" + expr + ")";
        } else if (typeExpr.equals("char")) {
            return "'" + expr + "'";
        } else if (expr instanceof java.lang.String) {
            return "\"" + expr + "\"";
        } else {
            return expr.toString();
        }
    }

    public static Object getExpr(Node doc, String type)
        throws SemanticException
    {

        Element el = (Element) doc.getChildNodes().item(0);

        /*
         * Los elementos entre <> corresponden a elementos de la gramática IDL
         * definidos en la regla 28 <const_type> ::= ...
         */

        // tipos de cadena - <string_type> y <wide_string_type>
        if (type.equals("java.lang.String")) {
            return getString(el);

            // tipos enteros - <integer_type>
        } else if (type.equals("short")) {
            return new Short((short) getIntExpr(el));
        } else if (type.equals("int")) {
            return new Integer((int) getIntExpr(el));
        } else if (type.equals("long")) {
            return new Long(getIntExpr(el));

            // tipos carácter - <char_type> y <wide_char_type>
        } else if (type.equals("char")) {
            return getChar(el);

            // tipo boolean - <boolean_type>
        } else if (type.equals("boolean")) {
            return new Boolean(getBoolean(el));

            // tipos de punto flotante - <float_pt_type>
        } else if (type.equals("float")) {
            return new Float((float) getFloatExpr(el));
        } else if (type.equals("double")) {
            return new Double(getFloatExpr(el));
        } else if (type.equals("long double")) {
            throw new SemanticException("Long double is not supported in IDL to Java mapping 2.6.", el);

            // tipo octeto - <octet_type>
        } else if (type.equals("byte")) {
            long value = getIntExpr(el);
            if (((byte) value < 0) || (value > 255))
                throw new SemanticException("Overflow of octet constant: "
                                            + el.toString() + ".", el);
            else
                return new Byte((byte) value);

            // tipo punto fijo - <fixed_pt_type>
        } else if (type.equals("java.math.BigDecimal")) {
            return (java.math.BigDecimal) getBigDecimalExpr(el);

        } else {
            throw new SemanticException("Not implemented getExpr: " + type, el);
        }
    }

    public static long getIntExpr(Node doc)
        throws SemanticException
    {
        String tag = ((Element) doc).getTagName();
        NodeList nodes = doc.getChildNodes();
        long s = 0;
        if (nodes.getLength() == 2) {
            long int1 = 0, int2 = 0;
            int1 = getIntExpr((Element) nodes.item(0));
            int2 = getIntExpr((Element) nodes.item(1));
            if (tag.equals(OMG_plus)) {
                s = int1 + int2;
            } else if (tag.equals(OMG_times)) {
                s = int1 * int2;
            } else if (tag.equals(OMG_div)) {
                s = int1 / int2;
            } else if (tag.equals(OMG_minus)) {
                s = int1 - int2;
            } else if (tag.equals(OMG_mod)) {
                s = int1 % int2;
            } else if (tag.equals(OMG_shiftR)) {
                s = int1 >> int2;
            } else if (tag.equals(OMG_shiftL)) {
                s = int1 << int2;
            } else if (tag.equals(OMG_and)) {
                s = int1 & int2;
            } else if (tag.equals(OMG_or)) {
                s = int1 | int2;
            } else if (tag.equals(OMG_xor)) {
                s = int1 ^ int2;
            }
        } else if (nodes.getLength() == 1) {
            if (tag.equals(OMG_expr) || tag.equals(OMG_plus)) {
                s = getIntExpr(nodes.item(0));
            } else if (tag.equals(OMG_not)) {
                s = ~getIntExpr((Element) nodes.item(0));
            } else if (tag.equals(OMG_minus)) {
                s = -getIntExpr((Element) nodes.item(0));
            }
        } else {
            s = getLong((Element) doc);
        }
        return s;
    }

    public static boolean getBooleanExpr(Node doc)
        throws SemanticException
    {
        String tag = ((Element) doc).getTagName();
        NodeList nodes = doc.getChildNodes();
        boolean s = false;
        s = getBoolean((Element) doc);
        return s;
    }

    private static double getFloatExpr(Node doc)
        throws SemanticException
    {
        String tag = ((Element) doc).getTagName();
        NodeList nodes = doc.getChildNodes();
        double s = 0.0;
        if (nodes.getLength() == 2) {
            double int1 = 0.0, int2 = 0.0;
            int1 = getFloatExpr(nodes.item(0));
            int2 = getFloatExpr(nodes.item(1));
            if (tag.equals(OMG_plus)) {
                s = int1 + int2;
            } else if (tag.equals(OMG_times)) {
                s = int1 * int2;
            } else if (tag.equals(OMG_div)) {
                s = int1 / int2;
            } else if (tag.equals(OMG_minus)) {
                s = int1 - int2;
            } else if (tag.equals(OMG_mod)) {
                s = int1 % int2;
            } else {
                throw new SemanticException("Bitwise operators require integer arguments", (Element) doc);
            }
        } else if (nodes.getLength() == 1) {
            if (tag.equals(OMG_expr)) {
                s = getFloatExpr(nodes.item(0));
            } else {
                throw new SemanticException("Bitwise operators require integer arguments", (Element) doc);
            }
        } else {
            s = getDouble((Element) doc);
        }
        return s;
    }

    private static java.math.BigDecimal getBigDecimalExpr(Node doc)
        throws SemanticException
    {
        String tag = ((Element) doc).getTagName();
        NodeList nodes = doc.getChildNodes();
        BigDecimal s;
        if (nodes.getLength() == 2) {
            //double int1=0.0, int2=0.0;
            BigDecimal int1, int2;
            int1 = getBigDecimalExpr((Element) nodes.item(0));
            int2 = getBigDecimalExpr((Element) nodes.item(1));
            if (tag.equals(OMG_plus)) {
                //s = int1 + int2;
                s = int1.add(int2);
            } else if (tag.equals(OMG_times)) {
                //s = int1 * int2;
                s = int1.multiply(int2);
            } else if (tag.equals(OMG_div)) {
                //s = int1 / int2;
                s = int1.divide(int2, BigDecimal.ROUND_UNNECESSARY);
            } else if (tag.equals(OMG_minus)) {
                //s = int1 - int2;
                s = int1.subtract(int2);
            } else if (tag.equals(OMG_mod)) {
                //s = int1 % int2;
                s = int1.subtract(int1
                    .divide(int2, BigDecimal.ROUND_UNNECESSARY).multiply(int2));
            } else {
                throw new SemanticException("Bitwise operators require integer arguments", (Element) doc);
            }
        } else if (nodes.getLength() == 1) {
            if (tag.equals(OMG_expr)) {
                s = getBigDecimalExpr(nodes.item(0));
            } else {
                throw new SemanticException("Bitwise operators require integer arguments", (Element) doc);
            }
        } else {
            s = getBigDecimal((Element) doc);
        }
        return s;
    }

    private static String getString(Element el)
        throws SemanticException
    {
        if (el.getChildNodes().getLength() > 0)
            throw new SemanticException("Binary operators are not allowed between strings", el);
        String tag = el.getTagName();
        if (tag.equals(OMG_string_literal)
            || tag.equals(OMG_wide_string_literal)) {
            String retu = el.getAttribute("value");
            for (int i = 0; i < retu.length(); i++)
                if (retu.charAt(i) == '\"')
                    if (i < retu.length() - 1 && retu.charAt(i + 1) == '\"')
                        throw new SemanticException("Concatenation of adjacent string literals is not supported in Java.",el);
            return retu;
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("java.lang.String"))
                throw new SemanticException("Expected string type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            return (String) value;
        } else
            throw new SemanticException("Expected string type.", el);
    }

    private static long getLong(Element el)
        throws SemanticException
    {
        String tag = el.getTagName();
        if (tag.equals(OMG_integer_literal)) {
            return Long.decode(el.getAttribute("value")).longValue();
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = "";
            scopedName = el.getAttribute(OMG_scoped_name);
            if (scopedName == null || scopedName.equals("")) {
                scopedName = el.getAttribute(OMG_name);
            }
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("long") && !type.equals("short")
                && !type.equals("byte") && !type.equals("int"))
                throw new SemanticException("Expected integer type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            if (value instanceof Long)
                return ((Long) value).longValue();
            else if (value instanceof Short)
                return ((Short) value).shortValue();
            else if (value instanceof Integer)
                return ((Integer) value).intValue();
            else
                return ((Byte) value).longValue();
        } else
            throw new SemanticException("Expected integer type.", el);
    }

    private static boolean getBoolean(Element el)
        throws SemanticException
    {
        String tag = el.getTagName();
        if (tag.equals(OMG_boolean_literal)) {
            return Boolean.valueOf(el.getAttribute("value").toLowerCase()).booleanValue();
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("boolean"))
                throw new SemanticException("Expected boolean type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            if (value instanceof Boolean)
                return ((Boolean) value).booleanValue();
        }
        throw new SemanticException("Expected boolean type.", el);
    }

    private static double getDouble(Element el)
        throws SemanticException
    {
        String tag = el.getTagName();
        if (tag.equals(OMG_floating_pt_literal)) {
            return Double.valueOf(el.getAttribute("value")).doubleValue();
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("double") && !type.equals("float"))
                throw new SemanticException("Expected floating point type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            if (value instanceof Float)
                return ((Float) value).doubleValue();
            else
                return ((Double) value).doubleValue();
        } else
            throw new SemanticException("Expected floating point type.", el);
    }

    private static String getChar(Element el)
        throws SemanticException
    {
        if (el.getChildNodes().getLength() > 0)
            throw new SemanticException("Binary operators are not allowed between chars",el);
        String tag = el.getTagName();
        if (tag.equals(OMG_character_literal)
            || tag.equals(OMG_wide_character_literal)) {
            return el.getAttribute("value");
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("char"))
                throw new SemanticException("Expected char type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            return (String) value;
        } else
            throw new SemanticException("Expected char type.", el);
    }

    private static java.math.BigDecimal getBigDecimal(Element el)
        throws SemanticException
    {
        String tag = el.getTagName();
        if (tag.equals(OMG_fixed_pt_literal)) {
            return new java.math.BigDecimal(el.getAttribute("value"));
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("java.math.BigDecimal"))
                throw new SemanticException("Expected fixed point type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            return (java.math.BigDecimal) value;
        } else
            throw new SemanticException("Expected fixed point type.", el);
    }

    public static Object getEnumExpr(Node doc, String type)
    {
        Element el = (Element) doc.getChildNodes().item(0);
        String value = el.getAttribute(OMG_name);
        if (value.lastIndexOf("::") >= 0)
            value = value.substring(value.lastIndexOf("::") + 2);
        return new EnumExpr(type + "." + value);
    }
}

class EnumExpr
{

    private String m_value;

    public EnumExpr(String value)
    {
        m_value = value;
    }

    public String toString()
    {
        return m_value;
    }
}