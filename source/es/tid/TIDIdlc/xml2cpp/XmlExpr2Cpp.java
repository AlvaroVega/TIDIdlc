/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 242 $
* Date: $Date: 2008-03-03 15:29:05 +0100 (Mon, 03 Mar 2008) $
* Last modified by: $Author: avega $
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

package es.tid.TIDIdlc.xml2cpp;


import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.math.BigDecimal;

import org.w3c.dom.*;

/**
 * Generates Cpp for expresions.
 */
public class XmlExpr2Cpp
    implements Idl2XmlNames
{

    public static String toString(Object expr, String typeExpr)
    {
        if (typeExpr.equals("CORBA::WChar*")) {
            return "L\"" + expr + "\"";
        // TODO: review this implementation
        } else if (typeExpr.equals("char") || typeExpr.equals("CORBA::Char")) {
            return "'" + expr + "'";
        } else if (typeExpr.equals("CORBA::WChar")) {
            return "L'" + expr + "'";
        } else if (typeExpr.equals("char*")) {
        	/* BUG #44
        	 * return "\"" + expr + "\"";
        	 */
            return expr +"";
        } else if (typeExpr.equals("CORBA::Long")) {
            return expr + "L";
        } else if (typeExpr.equals("CORBA::LongLong")) {
        	return expr + "LL";
        } else if (typeExpr.equals("CORBA::ULong")) {
        	return expr + "UL";
        } else if (typeExpr.equals("CORBA::ULongLong")) {
            return expr + "ULL";
        } else {
            return expr.toString();
        }
    }

    /**
     * En un gran fallo de dise?o se invoca desde distintos punto a este metodo.
     * asi. Unas veces los tipos vienen predefinidos como CORBA y otros como
     * tipos basicos.. En fin..
     * 
     * @param doc
     * @param type
     * @return
     * @throws SemanticException
     */
    public static Object getExpr(Node doc, String type)
        throws SemanticException
    {
        Element el = (Element) doc.getChildNodes().item(0);

        /*
         * Los elementos entre <> corresponden a elementos de la gram�tica IDL
         * definidos en la regla 28 <const_type> ::= ...
         */

        // tipos de cadena - <string_type> y <wide_string_type>
        // if (type.equals("char*")|| type.equals("wchar*") ||
        // type.equals("CORBA::WChar*")) {
        if (type.equals("char*")) {
            return getString(el);
        } else if (type.equals("wchar*") || type.equals("CORBA::WChar*")) {
            return getWideString(el);
            // tipos enteros - <integer_type>
        } else if (type.equals("CORBA::UShort")) {
            long retu = getIntExpr(el);
            if (retu < 0 || retu > 2 * (Short.MAX_VALUE + 1) - 1)
                throw new SemanticException(
                              "Overflow of unsigned short constant.");
            return new Long((long) retu);
        } else if (type.equals("CORBA::Short")) {
            long retu = getIntExpr(el);
            if (retu < Short.MIN_VALUE || retu > Short.MAX_VALUE)
                throw new SemanticException("Overflow of short constant.");
            return new Short((short) retu);
        } else if (type.equals("CORBA::Long")) {
            long retu = getIntExpr(el);
            if (retu < Integer.MIN_VALUE || retu > Integer.MAX_VALUE)
                throw new SemanticException("Overflow of long constant.");
            return new Integer((int) retu);
        } else if (type.equals("CORBA::ULong")) {
            long retu = getIntExpr(el);
            if (retu < 0 || retu > 2 * (((long) Integer.MAX_VALUE) + 1) - 1)
                throw new SemanticException("Overflow of unsigned long constant.");
            return new Long(retu);
        } else if (type.equals("CORBA::ULongLong")
                   || type.equals("CORBA::LongLong")) {
            return new Long((long) getIntExpr(el));

            // tipos car�cter - <char_type> y <wide_char_type>
			/* ¿pueden entrar tipos "char"? ¿o son todos "CORBA::Char"? */
        } else if (type.equals("char") || type.equals("CORBA::Char")) {
            return getChar(el);
        } else if (type.equals("CORBA::WChar")) {
            return getWideChar(el);

            // tipo boolean - <boolean_type>
        } else if (type.equals("CORBA::Boolean")) {
            return new Boolean(getBooleanExpr(el));

            // tipos de punto flotante - <float_pt_type>
        } else if (type.equals("CORBA::Float")) {
            return new Float((float) getFloatExpr(el));
        } else if (type.equals("CORBA::Double")) {
            return new Double(getFloatExpr(el));
        } else if (type.equals("CORBA::LongDouble")) {
            throw new SemanticException("Long double is not supported by this compiler yet.");

            // tipo octeto - <octet_type>
        } else if (type.equals("CORBA::Octet")) {
            long value = getIntExpr(el);
            if ((value < 0) || (value > 255))
                throw new SemanticException("Overflow of octet constant.");
            else
                return new Byte((byte) value);

            // tipo punto fijo - <fixed_pt_type>
        } else if (type.equals("CORBA::Fixed")) {
            //return (java.math.BigDecimal)getBigDecimalExpr(el);
            return getBigDecimalExpr(el);

        } else {
            if (type.equals("int")) {
                return new Integer((int) getIntExpr(el));
            } else if (type.equals("long")) {
                return new Long((long) getIntExpr(el));
            } else if (type.equals("byte")) {
                return new Byte((byte) getIntExpr(el));
            } else if (type.equals("short")) {
                return new Short((short) getIntExpr(el));
            } else if (type.equals("float")) {
                return new Float((float) getFloatExpr(el));
                /*
                 * } else if (type.equals("java.math.BigDecimal")) { return
                 * (java.math.BigDecimal)getBigDecimalExpr(el);
                 */
            }
            throw new SemanticException("Not implemented getExpr: " + type
                                        + "  -->" + el.toString());
        }
    }

    /**
     * Obtiene el valor de una expresi�n constante formada por
     * <integer_literal>s y operaciones
     * 
     * @param doc
     * @return
     * @throws SemanticException
     */

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

            // bug #67
            // compruebo para cada uno de los posibles sumandos

            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);               
                String name =el.getAttribute(OMG_scoped_name);                
                //nombre del primer operando  
                //busco si es cte
                Element root = doc.getOwnerDocument().getDocumentElement();       		 
                NodeList enums = root.getElementsByTagName(OMG_const_dcl);       	    
                for (int k = 0; k < enums.getLength(); k++) {
           	 		Element en = (Element) enums.item(k);
           	 		
           			if (en.getAttribute(OMG_scoped_name).equals(name)) {
           				 Element subchild=(Element) en.getFirstChild();

           				 if (subchild.getTagName() == OMG_type) {
           				 	// basic type
           				 	if (subchild.getAttribute(OMG_kind) == OMG_octet ||
           				 			subchild.getAttribute(OMG_kind) == OMG_char) 
           		       	        throw new SemanticException("The Arithmetical operations with constants of" +
           		       	        		" type char or octect are not allowed");
           			     }
           				 else if (subchild.getTagName() == OMG_scoped_name){
           				 	// defined type 
           				 	String typeName = getBasicType (doc.getOwnerDocument().getDocumentElement(),
           				 		subchild.getAttribute(OMG_name)); 
           				 	if (typeName.equalsIgnoreCase(OMG_octet) || typeName.equalsIgnoreCase(OMG_char))
           		       	        throw new SemanticException("The Arithmetical operations with constants of" +
           		       	        		" type char or octect are not allowed");
           				 }
           			}
                }
            }
            // End bug #67
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
        //}
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
            int1 = getFloatExpr((Element) nodes.item(0));
            int2 = getFloatExpr((Element) nodes.item(1));
            if (tag.equals(OMG_plus)) {
                s = int1 + int2;
            } else if (tag.equals(OMG_times)) {
                s = int1 * int2;
            } else if (tag.equals(OMG_div)) {
                s = int1 / int2;
            } else if (tag.equals(OMG_minus)) {
                s = int1 - int2;
             } else {
                throw new SemanticException(
                              "Bitwise operators require integer arguments",
                              (Element) doc);
            }
        } else if (nodes.getLength() == 1) {
            if (tag.equals(OMG_expr) || tag.equals(OMG_plus)) {
                s = getFloatExpr(nodes.item(0));
            } else if (tag.equals(OMG_minus)) {
                s = -getFloatExpr((Element) nodes.item(0));
            } else {
                throw new SemanticException(
                              "Operator '~' requires integer argument",
                              (Element) doc);
            }
        } else {
            s = getDouble((Element) doc);
        }
        return s;
    }

    //private static java.math.BigDecimal getBigDecimalExpr(Node doc) throws
    // SemanticException {
    private static String getBigDecimalExpr(Node doc)
        throws SemanticException
    {
        String tag = ((Element) doc).getTagName();
        NodeList nodes = doc.getChildNodes();
        //double s = 0.0;
        BigDecimal s;
        if (nodes.getLength() == 2) {
            //double int1=0.0, int2=0.0;
            BigDecimal int1, int2;
            int1 = new BigDecimal(getBigDecimalExpr((Element) nodes.item(0)));
            int2 = new BigDecimal(getBigDecimalExpr((Element) nodes.item(1)));
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
            } else {
                throw new SemanticException(
                              "Bitwise operators require integer arguments",
                              (Element) doc);
            }
        } else if (nodes.getLength() == 1) {
            if (tag.equals(OMG_expr) || tag.equals(OMG_plus)) {
                //s = getBigDecimalExpr(nodes.item(0)).doubleValue();
                s = new BigDecimal(getBigDecimalExpr(nodes.item(0)));
            } else if (tag.equals(OMG_minus)) {
                s = new BigDecimal("-" + getBigDecimalExpr(nodes.item(0)));
            } else {
                throw new SemanticException(
                              "Operator '~' requires integer argument",
                              (Element) doc);
            }
        } else {
            //s = getBigDecimal((Element)doc).doubleValue();
            s = getBigDecimal((Element) doc);
        }
        //return new java.math.BigDecimal(s);
        return s.toString();
    }

    private static String getString(Element el)
        throws SemanticException
    {
        if (el.getChildNodes().getLength() > 0)
            throw new SemanticException(
                          "Binary operators are not allowed between strings",
                          el);
        String tag = el.getTagName();
        if (tag.equals(OMG_string_literal)) {
            return el.getAttribute("value");
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("char*"))
                throw new SemanticException("Expected string type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            return (String) value;
        } else
            throw new SemanticException("Expected string type.", el);
    }

    private static String getWideString(Element el)
        throws SemanticException
    {
        if (el.getChildNodes().getLength() > 0)
            throw new SemanticException(
                          "Binary operators are not allowed between wide strings",
                          el);
        String tag = el.getTagName();
        if (tag.equals(OMG_string_literal)
            || tag.equals(OMG_wide_string_literal)) { // De momento lo pasamos
            return el.getAttribute("value");
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("char*") && !type.equals("CORBA::WChar*")
                && !type.equals("wchar*"))
                throw new SemanticException("Expected wide string type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            return (String) value;
        } else
            throw new SemanticException("Expected wide string type.", el);
    }

    private static String getBasicType(Element root, String typeToSearch) {
		NodeList enums = root.getElementsByTagName(OMG_typedef);

		for (int k = 0; k < enums.getLength(); k++){
			Element en = (Element) enums.item(k);

			NodeList child = en.getChildNodes();    				    
			    
			Element firstNode = (Element) child.item(0);
			Element secondNode = (Element) child.item(1);
			String curTypeName = secondNode.getAttribute(OMG_scoped_name);

			if (curTypeName.equals(typeToSearch)) {
				String typedefType = firstNode.getTagName();

				if (typedefType.equals(OMG_scoped_name)) {
					return getBasicType(root, firstNode.getAttribute(OMG_name));
				} 
			 	return firstNode.getAttribute(OMG_kind);
			}
		}
		return "UnknowType";
    }

    /**
     * Obtiene el valor de un <integer_litera>
     * 
     * @param el
     * @return
     * @throws SemanticException
     */
    private static long getLong(Element el)
        throws SemanticException
    {
        String tag = el.getTagName();
        if (tag.equals(OMG_integer_literal)) {
            /*
             * try { return Long.parseLong(el.getAttribute("value")); } catch
             * (Exception e) { return
             * Integer.decode(el.getAttribute("value")).longValue(); }
             */

        	// PRA: integer literals are always positive (see CORBA 2.6 chapter 3.9.2)
        	// Due to 2's complement internal representation, the minimum (negative)
        	// value is -(MAX_VALUE+1). We take advantage of this and negate the integer
        	// literal "value" to avoid overflow when parsing constants.

        	// return Long.decode(el.getAttribute("value")).longValue();
        	return (- Long.decode("-" + el.getAttribute("value")).longValue());
        
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = "";
            scopedName = el.getAttribute(OMG_scoped_name);
            if (scopedName == null || scopedName.equals("")) {
                scopedName = el.getAttribute(OMG_name);
            }
            String type = IdlConstants.getInstance().getType(scopedName);
          
            if (!type.equals("CORBA::Short") && !type.equals("CORBA::UShort")
                && !type.equals("CORBA::Long") && !type.equals("CORBA::ULong")
                && !type.equals("CORBA::ULongLong")
                && !type.equals("CORBA::LongLong")
                && !type.equals("CORBA::Octet")
				) {
           	    // bug #67
            	type = getBasicType(el.getOwnerDocument().getDocumentElement(), "::" + type);
				if (!type.equalsIgnoreCase("Short") && !type.equalsIgnoreCase("UShort")
		                && !type.equalsIgnoreCase("Long") && !type.equalsIgnoreCase("ULong")
						&& !type.equalsIgnoreCase("unsigned long")
		                && !type.equalsIgnoreCase("ULongLong")
		                && !type.equalsIgnoreCase("LongLong")
		                && !type.equalsIgnoreCase("Octet")
						)
					throw new SemanticException("Expected integer type.", el);
	            // end bug #67
            }

            Object value = IdlConstants.getInstance().getValue(scopedName);
            if (value instanceof Short)
                return ((Short) value).longValue(); // CORBA::Short
            else if (value instanceof Long)
                return ((Long) value).longValue(); // CORBA::Long
            else if (value instanceof Integer)
                return ((Integer) value).longValue(); // CORBA::LongLong
            else
                return ((Byte) value).longValue(); // CORBA::Octet
        } else
            throw new SemanticException("Expected integer type.", el);
    }

    private static boolean getBoolean(Element el)
        throws SemanticException
    {
        String tag = el.getTagName();
        if (tag.equals(OMG_boolean_literal)) {
            return Boolean
                .valueOf(el.getAttribute("value").toLowerCase()).booleanValue();
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
            if (!type.equals("CORBA::Double") && !type.equals("CORBA::Float")) {
            	type = getBasicType(el.getOwnerDocument().getDocumentElement(), "::" + type);
            	if (!type.equalsIgnoreCase("double") && !type.equalsIgnoreCase("float"))
            		throw new SemanticException("Expected floating point type.", el);            	
            }

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
            throw new SemanticException(
                          "Binary operators are not allowed between chars",
                          el);
        String tag = el.getTagName();
        if (tag.equals(OMG_character_literal)) {
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

    private static String getWideChar(Element el)
        throws SemanticException
    {
        if (el.getChildNodes().getLength() > 0)
            throw new SemanticException("Binary operators are not allowed between chars",
                                        el);
        String tag = el.getTagName();
        if (tag.equals(OMG_wide_character_literal)
            || tag.equals(OMG_character_literal)) {
            return el.getAttribute("value");
        } else if (tag.equals(OMG_scoped_name)) {
            String scopedName = el.getAttribute(OMG_name);
            String type = IdlConstants.getInstance().getType(scopedName);
            if (!type.equals("CORBA::WChar") || !type.equals("char"))
                throw new SemanticException("Expected wide char type.", el);
            Object value = IdlConstants.getInstance().getValue(scopedName);
            return (String) value;
        } else
            throw new SemanticException("Expected wide char type.", el);
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

    public static Object getEnumExpr(Node doc)
    {
        Element el = (Element) doc.getChildNodes().item(0);
        String value = TypeManager.convert(el.getAttribute(OMG_name));
        return new EnumExpr(value);
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