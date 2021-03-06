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

import java.io.*;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for interface stubs.
 */
class XmlInterfaceSkeleton2Java extends XmlInterfaceUtils2Java
    implements Idl2XmlNames
{

    private java.util.Hashtable m_interface_parents = new java.util.Hashtable();

    public String generateJava(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        XmlJavaHeaderGenerator.generate(buffer, "skeleton", name + "POA", genPackage);

        // Class header
        buffer.append("abstract public class ");
        buffer.append(name);
        buffer.append("POA\n");
        buffer.append(" extends org.omg.PortableServer.DynamicImplementation\n");
        buffer.append(" implements ");
        buffer.append(name);
        buffer.append("Operations {\n\n");

        buffer.append("  public ");
        buffer.append(name);
        buffer.append(" _this() {\n");
        buffer.append("    return ");
        buffer.append(name);
        buffer.append("Helper.narrow(super._this_object());\n");
        buffer.append("  };\n\n");

        buffer.append("  public ");
        buffer.append(name);
        buffer.append(" _this(org.omg.CORBA.ORB orb) {\n");
        buffer.append("    return ");
        buffer.append(name);
        buffer.append("Helper.narrow(super._this_object(orb));\n");
        buffer.append("  };\n\n");

        buffer.append("  public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectID) {\n");
        buffer.append("    return __ids;\n");
        buffer.append("  };\n\n");

        buffer.append("  private static java.lang.String[] __ids = {\n");
        generateInterfacesSupported(buffer, doc);
        buffer.append("\n  };\n\n");

        buffer.append("  private static java.util.Dictionary _methods = new java.util.Hashtable();\n");
        buffer.append("  static {\n");
        generateJavaSkeletonMethodArray(buffer, doc, 0);
        buffer.append("  }\n\n");

        buffer.append("  public void invoke(org.omg.CORBA.ServerRequest _request) {\n");
        buffer.append("    java.lang.Object _method = _methods.get(_request.operation());\n");
        buffer.append("    if (_method == null) {\n");
        buffer.append("      throw new org.omg.CORBA.BAD_OPERATION(_request.operation());\n");
        buffer.append("    }\n");
        buffer.append("    int _method_id = ((java.lang.Integer)_method).intValue();\n");
        buffer.append("    switch(_method_id) {\n");
        m_interface_parents = new java.util.Hashtable();
        generateJavaSkeletonExportDef(buffer, doc, 0);
        buffer.append("    }\n");
        buffer.append("  }\n");

        buffer.append("}\n");

        return buffer.toString();
    }

    private int generateJavaSkeletonExportDef(StringBuffer buffer, Element doc,
                                              int index)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("    case " + index);
                buffer.append(": {\n");
                generateJavaSkeletonMethod(buffer, el);
                buffer.append("      return;\n");
                buffer.append("    }\n");
                index++;
            } else if (tag.equals(OMG_attr_dcl)) {
                index = generateJavaSkeletonAttribute(buffer, el, index);
            }
        }

        // Items definition
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    String clase = el.getAttribute(OMG_name);
                    Scope scope = Scope.getGlobalScopeInterface(clase);
                    Element inhElement = scope.getElement();
                    // Generate the operation for all the interface parents
                    if (!m_interface_parents.containsKey(inhElement)) {
                        // This is to avoid the duplication of the operation
                        // when there's multiple
                        // inheritance and one of the father inherits from the
                        // other
                        m_interface_parents.put(inhElement, "void");
                        index = generateJavaSkeletonExportDef(buffer,
                                                              inhElement, index);
                    }
                }
            }
        }
        return index;
    }

    private int generateJavaSkeletonMethodArray(StringBuffer buffer,
                                                Element doc, int index)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("    _methods.put(\"");
                buffer.append(el.getAttribute(OMG_name));
                buffer.append("\", new Integer(" + index);
                buffer.append("));\n");
                index++;
            } else if (tag.equals(OMG_attr_dcl)) {
                index = generateJavaSkeletonMethodArrayAttr(buffer, el, index);
            }
        }

        // Items definition
        Element el1 = (Element) doc.getFirstChild();
        if (el1 != null) {
            if (el1.getTagName().equals(OMG_inheritance_spec)) {
                nodes = el1.getChildNodes();
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element el = (Element) nodes.item(i);
                    String clase = el.getAttribute(OMG_name);
                    Scope scope = Scope.getGlobalScopeInterface(clase);
                    Element inhElement = scope.getElement();
                    // Generate the method for all the interface parents
                    if (!m_interface_parents.containsKey(inhElement)) {
                        // This is to avoid the duplication of the method when
                        // there's multiple
                        // inheritance and one of the father inherits from the
                        // other
                        m_interface_parents.put(inhElement, "void");
                        index = generateJavaSkeletonMethodArray(buffer,
                                                                inhElement,
                                                                index);
                    }
                }
            }
        }
        return index;
    }

    public int generateJavaSkeletonMethodArrayAttr(StringBuffer buffer,
                                                   Element doc, int index)
    {
        // Get type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        String readonly = doc.getAttribute(OMG_readonly);

        // Get & set methods
        for (int i = 1; i < nodes.getLength(); i++) {
            Element att = (Element) nodes.item(i);
            String name = att.getAttribute(OMG_name);
            buffer.append("    _methods.put(\"_get_");
            buffer.append(name);
            buffer.append("\", new Integer(" + index);
            buffer.append("));\n");
            index++;
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("    _methods.put(\"_set_");
                buffer.append(name);
                buffer.append("\", new Integer(" + index);
                buffer.append("));\n");
                index++;
            }
        }
        return index;
    }

    private void generateJavaSkeletonMethod(StringBuffer buffer, Element doc)
        throws Exception
    {
        NodeList nodes = doc.getChildNodes();
        NodeList exceps = null;

        int numParams = nodes.getLength() - 1;

        // Exceptions
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_raises)) {
                exceps = el.getChildNodes();
                numParams -= 1;
                break;
            }
        }
        boolean exceptsExist = ((exceps != null) && (exceps.getLength() > 0));

        if (exceptsExist) {
            buffer.append("      try {\n");
        }

        buffer
            .append("      org.omg.CORBA.NVList _params = _orb().create_list(");
        buffer.append(numParams);
        buffer.append(");\n");

        // Parameters (any values declaration)
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                buffer.append("      org.omg.CORBA.Any $");
                buffer.append(paramName);
                buffer.append(" = _orb().create_any();\n");
                buffer.append("      $");
                buffer.append(paramName);
                buffer.append(".type(");
                buffer.append(XmlType2Java.getTypecode(paramType));
                buffer.append(");\n");
                buffer.append("      _params.add_value(\"");
                buffer.append(paramName);
                buffer.append("\", $");
                buffer.append(paramName);
                if (in) {
                    buffer.append(", org.omg.CORBA.ARG_IN.value);\n");
                } else if (inout) {
                    buffer.append(", org.omg.CORBA.ARG_INOUT.value);\n");
                } else {
                    buffer.append(", org.omg.CORBA.ARG_OUT.value);\n");
                }
            }
        }
        buffer.append("      _request.arguments(_params);\n");

        // Parameters
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (!in) {
                    buffer.append("      ");
                    buffer.append(XmlType2Java.getParamType(paramType, true));
                    buffer.append(" ");
                    buffer.append(paramName);
                    buffer.append(" = new ");
                    buffer.append(XmlType2Java.getParamType(paramType, true));
                    buffer.append("();\n");
                } else {
                    buffer.append("      ");
                    buffer.append(XmlType2Java.getParamType(paramType, false));
                    buffer.append(" ");
                    buffer.append(paramName);
                    buffer.append(";\n");
                }
                if (!out) {
                    buffer.append("      ");
                    buffer.append(paramName);
                    if (inout)
                        buffer.append(".value");
                    buffer.append(" = ");
                    String helper = XmlType2Java.getHelperType(paramType);
                    if (helper == null) {
                        buffer.append("$");
                        buffer.append(paramName);
                        buffer.append(".extract_");
                        buffer.append(XmlType2Java
                            .basicORBTypeMapping(paramType));
                        buffer.append("();\n");
                    } else {
                        buffer.append(helper);
                        buffer.append(".extract($");
                        buffer.append(paramName);
                        buffer.append(");\n");
                    }
                }
            }
        }

        buffer.append("      ");
        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        if (returnTypeL.getLength() > 0) {
            Element ret = (Element) returnTypeL.item(0);
            buffer.append(XmlType2Java.getType(ret));
            buffer.append(" _result = ");
        }

        // Method name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append("this.");
        buffer.append(nombre);
        buffer.append("(");

        // Parameters (passing)
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                if (i > 1)
                    buffer.append(", ");
                buffer.append(paramName);
            }
        }
        buffer.append(");\n");

        // Get the result
        if (returnTypeL.getLength() > 0) {
            Element ret = (Element) returnTypeL.item(0);
            buffer.append("      org.omg.CORBA.Any _resultAny = _orb().create_any();\n");
            String helper = XmlType2Java.getHelperType(ret);
            buffer.append("      ");
            if (helper == null) {
                buffer.append("_resultAny.insert_");
                buffer.append(XmlType2Java.basicORBTypeMapping(ret));
                buffer.append("(_result);\n");
            } else {
                buffer.append(helper);
                buffer.append(".insert(_resultAny, _result);\n");
            }
            buffer.append("      _request.set_result(_resultAny);\n");
        }

        // Parameters (returning out parameters)
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (!in) {
                    buffer.append("      ");
                    String helper = XmlType2Java.getHelperType(paramType);
                    if (helper == null) {
                        buffer.append("$");
                        buffer.append(paramName);
                        buffer.append(".insert_");
                        buffer.append(XmlType2Java
                            .basicORBTypeMapping(paramType));
                        buffer.append("(");
                        buffer.append(paramName);
                        buffer.append(".value);\n");
                    } else {
                        buffer.append(helper);
                        buffer.append(".insert($");
                        buffer.append(paramName);
                        buffer.append(",");
                        buffer.append(paramName);
                        buffer.append(".value);\n");
                    }
                }
            }
        }

        if (exceptsExist) {
            for (int j = 0; j < exceps.getLength(); j++) {
                Element ex = (Element) exceps.item(j);
                buffer.append("      } catch(");
                buffer.append(XmlType2Java.getType(ex));
                buffer.append(" _exception) {\n");
                buffer.append("        org.omg.CORBA.Any _exceptionAny = _orb().create_any();\n");
                buffer.append("        ");
                buffer.append(XmlType2Java.getHelperType(ex));
                buffer.append(".insert(_exceptionAny, _exception);\n");
                buffer.append("        _request.set_exception(_exceptionAny);\n");
            }
            buffer.append("      }\n");
        }

    }

    private int generateJavaSkeletonAttribute(StringBuffer buffer, Element doc,
                                              int index)
        throws Exception
    {

        // Get the type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        String readonly = doc.getAttribute(OMG_readonly);

        for (int i = 1; i < nodes.getLength(); i++) {
            // Accessors generation
            buffer.append("    case " + index);
            buffer.append(": {\n");

            buffer.append("      org.omg.CORBA.NVList _params = _orb().create_list(0);\n");
            buffer.append("      _request.arguments(_params);\n");

            buffer.append("      ");
            // Return type
            buffer.append(XmlType2Java.getType(typeEl));
            buffer.append(" _result = ");

            // Method name
            Element nameEl = (Element) nodes.item(i);
            String name = nameEl.getAttribute(OMG_name);
            buffer.append("this.");
            buffer.append(name);
            buffer.append("();\n");

            // Get the result
            buffer.append("      org.omg.CORBA.Any _resultAny = _orb().create_any();\n");
            String helper = XmlType2Java.getHelperType(typeEl);
            buffer.append("      ");
            if (helper == null) {
                buffer.append("_resultAny.insert_");
                buffer.append(XmlType2Java.basicORBTypeMapping(typeEl));
                buffer.append("(_result);\n");
            } else {
                buffer.append(helper);
                buffer.append(".insert(_resultAny, _result);\n");
            }
            buffer.append("      _request.set_result(_resultAny);\n");

            buffer.append("      return;\n");
            buffer.append("    }\n");
            index++;

            // Modifiers generation

            if (readonly == null || !readonly.equals(OMG_true)) {

                buffer.append("    case " + index);
                buffer.append(": {\n");

                buffer.append("      org.omg.CORBA.NVList _params = _orb().create_list(1);\n");
                // Parameter (any value declaration)
                buffer.append("      org.omg.CORBA.Any $value = _orb().create_any();\n");
                buffer.append("      $value.type(");
                buffer.append(XmlType2Java.getTypecode(typeEl));
                buffer.append(");\n");
                buffer.append("      _params.add_value(\"value\", $value, org.omg.CORBA.ARG_IN.value);\n");
                buffer.append("      _request.arguments(_params);\n");

                buffer.append("      ");
                buffer.append(XmlType2Java.getParamType(typeEl, false));
                buffer.append(" value;\n");
                buffer.append("      value = ");
                if (helper == null) {
                    buffer.append("$value.extract_");
                    buffer.append(XmlType2Java.basicORBTypeMapping(typeEl));
                    buffer.append("();\n");
                } else {
                    buffer.append(helper);
                    buffer.append(".extract($value);\n");
                }

                buffer.append("      ");

                // Method name
                buffer.append("this.");
                buffer.append(name);
                buffer.append("(value);\n");

                buffer.append("      return;\n");
                buffer.append("    }\n");
                index++;
            }
        }

        return index;
    }

}