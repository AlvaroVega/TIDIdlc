/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 306 $
* Date: $Date: 2009-05-25 16:45:04 +0200 (Mon, 25 May 2009) $
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

package es.tid.TIDIdlc.xml2java;

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Hashtable;

import org.w3c.dom.*;

/**
 * Generates Java for interface stubs.
 */
class XmlInterfaceStub2Java extends XmlInterfaceUtils2Java
    implements Idl2XmlNames
{

    private Hashtable m_interface_parents = new Hashtable();
    
    private boolean async_mode = false;
    

    public String generateJava(Element doc, String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        
        XmlJavaHeaderGenerator.generate(buffer, "stub", "_" + name + "Stub", genPackage);

        // Class header
        buffer.append("public class _");
        buffer.append(name);
        buffer.append("Stub\n");
        buffer.append(" extends org.omg.CORBA.portable.ObjectImpl\n");
        buffer.append(" implements ");
        buffer.append(name);
        buffer.append(" {\n\n");

        buffer.append("  public java.lang.String[] _ids() {\n");
        buffer.append("    return __ids;\n");
        buffer.append("  }\n\n");

        buffer.append("  private static java.lang.String[] __ids = {\n");
        generateInterfacesSupported(buffer, doc);
        buffer.append("  };\n\n");

        generateJavaStubExportDef(buffer, doc, genPackage);

        buffer.append("}\n");

        return buffer.toString();
    }

    private void generateJavaStubExportDef(StringBuffer buffer, Element doc, String genPackage)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {
                buffer.append("  public ");
                generateJavaMethodHeader(buffer, el);
                generateJavaStubMethodBody(buffer, el);
                buffer.append("\n\n");
                if (CompilerConf.getAsynchronous() && el.getAttribute(OMG_oneway).compareTo(OMG_true) != 0){
                    // Ahora el metodo asincrono
                    buffer.append("  public ");
                    String name = doc.getAttribute(OMG_name);
                    generateJavaMethodHeaderAsync(buffer, el, name);
                    generateJavaStubMethodBodyAsync(buffer, el);
                    buffer.append("\n\n");
                }
            } else if (tag.equals(OMG_attr_dcl)) {
                generateJavaStubAttributeDecl(buffer, el);
                if (CompilerConf.getAsynchronous()){
                    // Ahora el metodo asincrono
                    String name = doc.getAttribute(OMG_name);
                    generateJavaStubAttributeDeclAsync(buffer, el, name);
                }
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
                    if (!m_interface_parents.containsKey(inhElement)) {
                        m_interface_parents.put(inhElement, "void");
                        generateJavaStubExportDef(buffer, inhElement, genPackage);
                    }
                }
            }
        }
        buffer.append("\n");
    }

    private void generateJavaStubMethodBody(StringBuffer buffer, Element doc)
        throws Exception
    {
        buffer.append(" {\n");
        buffer.append("    ");

        // Method name
        String nombre = doc.getAttribute(OMG_name);
        buffer.append("org.omg.CORBA.Request _request = this._request(\"");
        buffer.append(nombre);
        buffer.append("\");\n");

        NodeList nodes = doc.getChildNodes();

        // Return type
        Element returnType = (Element) nodes.item(0);
        NodeList returnTypeL = returnType.getChildNodes();
        if (returnTypeL.getLength() > 0) {
            buffer.append("    _request.set_return_type(");
            Element ret = (Element) returnTypeL.item(0);
            buffer.append(XmlType2Java.getTypecode(ret));
            buffer.append(");\n");
        }

        // Parameters
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (out) {
                    buffer.append("    org.omg.CORBA.Any $");
                    buffer.append(paramName);
                    buffer.append(" = _request.add_named_out_arg(\"");
					buffer.append(paramName);
					buffer.append("\");\n");
                    buffer.append("    $");
                    buffer.append(paramName);
                    buffer.append(".type(");
                    buffer.append(XmlType2Java.getTypecode(paramType));
                    buffer.append(");\n");
                } else {
                    buffer.append("    org.omg.CORBA.Any $");
                    buffer.append(paramName);
                    if (inout) {
                        buffer.append(" = _request.add_named_inout_arg(\"");
                        buffer.append(paramName);
                        buffer.append("\");\n");                    
                    }
                    else {
                        buffer.append(" = _request.add_named_in_arg(\"");
                        buffer.append(paramName);
                        buffer.append("\");\n");
                    }
                    String helper = XmlType2Java.getHelperType(paramType);
                    if (helper == null) {
                        buffer.append("    $");
                        buffer.append(paramName);
                        buffer.append(".insert_");
                        buffer.append(XmlType2Java.basicORBTypeMapping(paramType));
                        buffer.append("(");
                        buffer.append(paramName);
                        if (inout)
                            buffer.append(".value");
                        buffer.append(");\n");
                    } else {
                        buffer.append("    ");
                        buffer.append(helper);
                        buffer.append(".insert($");
                        buffer.append(paramName);
                        buffer.append(",");
                        buffer.append(paramName);
                        if (inout)
                            buffer.append(".value");
                        buffer.append(");\n");
                    }
                }
            }
        }

        // Exceptions
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_raises)) {
                NodeList exceps = el.getChildNodes();
                for (int j = 0; j < exceps.getLength(); j++) {
                    Element ex = (Element) exceps.item(j);
                    buffer.append("    _request.exceptions().add(");
                    buffer.append(XmlType2Java.getHelperType(ex));
                    buffer.append(".type());\n");
                }
                break;
            }
        }

        String oneway = doc.getAttribute(OMG_oneway);
        if (oneway.equals(OMG_true)) {
            // Oneway invocation
            buffer.append("    _request.send_oneway();\n");

        } else {
            // Invocation
        	if (async_mode){
                    buffer.append("    es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate delegate = \n");
                    buffer.append("\t (es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate)_get_delegate();\n");
                    buffer.append("    delegate.asyncRequest((es.tid.TIDorbj.core.RequestImpl)_request, \n");
                    buffer.append("\t (org.omg.CORBA.Object)ami_handler);\n");

                    async_mode = false;
        	}
        	else
        		buffer.append("    _request.invoke();\n");

            // Catch exceptions
            buffer.append("    java.lang.Exception _exception = _request.env().exception();\n");
            buffer.append("    if (_exception != null) {\n");
            buffer.append("      if (_exception instanceof org.omg.CORBA.UnknownUserException) {\n");
            buffer.append("        org.omg.CORBA.UnknownUserException _userException = \n");
            buffer.append("          (org.omg.CORBA.UnknownUserException) _exception;\n");
            for (int i = 1; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getTagName().equals(OMG_raises)) {
                    NodeList exceps = el.getChildNodes();
                    for (int j = 0; j < exceps.getLength(); j++) {
                        Element ex = (Element) exceps.item(j);
                        buffer.append("        if (_userException.except.type().equal(");
                        buffer.append(XmlType2Java.getHelperType(ex));
                        buffer.append(".type())) {\n");
                        buffer.append("          throw ");
                        buffer.append(XmlType2Java.getHelperType(ex));
                        buffer.append(".extract(_userException.except);\n");
                        buffer.append("        }\n");
                    }
                    buffer.append("        throw new org.omg.CORBA.UNKNOWN();\n");
                    break;
                }
            }
            buffer.append("      }\n");
            buffer.append("      throw (org.omg.CORBA.SystemException) _exception;\n");
            buffer.append("    };\n");

            // Get the result
            if (returnTypeL.getLength() > 0) {
                Element ret = (Element) returnTypeL.item(0);
                buffer.append("    ");
                buffer.append(XmlType2Java.getType(ret));
                buffer.append(" _result;\n");
                String helper = XmlType2Java.getHelperType(ret);
                if (helper == null) {
                    buffer.append("    _result = _request.return_value().extract_");
                    buffer.append(XmlType2Java.basicORBTypeMapping(ret));
                    buffer.append("();\n");
                } else {
                    buffer.append("    _result = ");
                    buffer.append(helper);
                    buffer.append(".extract(_request.return_value());\n");
                }
            }

            // Return out parameters
            for (int i = 1; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                if (el.getTagName().equals(OMG_parameter)) {
                    Element paramType = (Element) el.getChildNodes().item(0);
                    String paramName = el.getAttribute(OMG_name);
                    boolean in = el.getAttribute(OMG_kind).equals("in");
                    if (!in) {
                        String helper = XmlType2Java.getHelperType(paramType);
                        if (helper == null) {
                            buffer.append("    ");
                            buffer.append(paramName);
                            buffer.append(".value = $");
                            buffer.append(paramName);
                            buffer.append(".extract_");
                            buffer.append(XmlType2Java.basicORBTypeMapping(paramType));
                            buffer.append("();\n");
                        } else {
                            buffer.append("    ");
                            buffer.append(paramName);
                            buffer.append(".value = ");
                            buffer.append(helper);
                            buffer.append(".extract($");
                            buffer.append(paramName);
                            buffer.append(");\n");
                        }
                    }
                }
            }

            // Return the result
            if (returnTypeL.getLength() > 0) {
                buffer.append("    return _result;\n");
            }
        }

        buffer.append("  }");
    }
    
    private void generateJavaStubMethodBodyAsync(StringBuffer buffer, Element doc)
    	throws Exception
    {
    	buffer.append(" {\n");
    	buffer.append("    ");

    	// Method name
    	String nombre = doc.getAttribute(OMG_name);
    	buffer.append("org.omg.CORBA.Request _request = this._request(\"");
    	buffer.append(nombre);
    	buffer.append("\");\n");

    	NodeList nodes = doc.getChildNodes();

    	// Return type
    	Element returnType = (Element) nodes.item(0);
    	NodeList returnTypeL = returnType.getChildNodes();
    	if (returnTypeL.getLength() > 0) {
    		buffer.append("    _request.set_return_type(");
    		Element ret = (Element) returnTypeL.item(0);
    		buffer.append(XmlType2Java.getTypecode(ret));
    		buffer.append(");\n");
    	}

        // Parameters
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            if (el.getTagName().equals(OMG_parameter)) {
                Element paramType = (Element) el.getChildNodes().item(0);
                String paramName = el.getAttribute(OMG_name);
                boolean in = el.getAttribute(OMG_kind).equals("in");
                boolean inout = el.getAttribute(OMG_kind).equals("inout");
                boolean out = el.getAttribute(OMG_kind).equals("out");
                if (out) {
                    buffer.append("    org.omg.CORBA.Any $");
                    buffer.append(paramName);
                    buffer.append(" = _request.add_named_out_arg(\"");
					buffer.append(paramName);
					buffer.append("\");\n");
                    buffer.append("    $");
                    buffer.append(paramName);
                    buffer.append(".type(");
                    buffer.append(XmlType2Java.getTypecode(paramType));
                    buffer.append(");\n");
                } else {
                    buffer.append("    org.omg.CORBA.Any $");
                    buffer.append(paramName);
                    if (inout) {
                        buffer.append(" = _request.add_named_inout_arg(\"");
                        buffer.append(paramName);
                        buffer.append("\");\n");                    
                    }
                    else {
                        buffer.append(" = _request.add_named_in_arg(\"");
                        buffer.append(paramName);
                        buffer.append("\");\n");
                    }
                    String helper = XmlType2Java.getHelperType(paramType);
                    if (helper == null) {
                        buffer.append("    $");
                        buffer.append(paramName);
                        buffer.append(".insert_");
                        buffer.append(XmlType2Java.basicORBTypeMapping(paramType));
                        buffer.append("(");
                        buffer.append(paramName);
                        buffer.append(");\n");
                    } else {
                        buffer.append("    ");
                        buffer.append(helper);
                        buffer.append(".insert($");
                        buffer.append(paramName);
                        buffer.append(",");
                        buffer.append(paramName);
                        buffer.append(");\n");
                    }
                }
            }
        }
    	
    	// Exceptions
    	for (int i = 1; i < nodes.getLength(); i++) {
    		Element el = (Element) nodes.item(i);
    		if (el.getTagName().equals(OMG_raises)) {
    			NodeList exceps = el.getChildNodes();
    			for (int j = 0; j < exceps.getLength(); j++) {
    				Element ex = (Element) exceps.item(j);
    				buffer.append("    _request.exceptions().add(");
    				buffer.append(XmlType2Java.getHelperType(ex));
    				buffer.append(".type());\n");
    			}
    			break;
    		}
    	}

        buffer.append("    es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate delegate = \n");
        buffer.append("\t (es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate)_get_delegate();\n");
        buffer.append("    delegate.asyncRequest((es.tid.TIDorbj.core.RequestImpl)_request, \n");
        buffer.append("\t (org.omg.CORBA.Object)ami_handler);\n");
    	buffer.append("  }");
    }


    private void generateJavaStubAttributeDecl(StringBuffer buffer, Element doc)
        throws Exception
    {
        // Get the type
        NodeList nodes = doc.getChildNodes();
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        String readonly = doc.getAttribute(OMG_readonly);

        // Get & set methods
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);

            //
            // Accessor
            //
            buffer.append("  public " + type + " " + name + "() {\n");

            // Method name
            buffer.append("    org.omg.CORBA.Request _request = this._request(\"_get_");
            buffer.append(name);
            buffer.append("\");\n");

            // Return type
            buffer.append("    _request.set_return_type(");
            buffer.append(XmlType2Java.getTypecode(typeEl));
            buffer.append(");\n");

            // Invocation
            buffer.append("    _request.invoke();\n");

            // Catch exceptions
            buffer.append("    java.lang.Exception _exception = _request.env().exception();\n");
            buffer.append("    if (_exception != null) {\n");
            buffer.append("      throw (org.omg.CORBA.SystemException) _exception;\n");
            buffer.append("    };\n");

            // Return the result
            buffer.append("    ");
            buffer.append(XmlType2Java.getType(typeEl));
            buffer.append(" _result;\n");
            String helper = XmlType2Java.getHelperType(typeEl);
            if (helper == null) {
                buffer.append("    _result = _request.return_value().extract_");
                buffer.append(XmlType2Java.basicORBTypeMapping(typeEl));
                buffer.append("();\n");
            } else {
                buffer.append("    _result = ");
                buffer.append(helper);
                buffer.append(".extract(_request.return_value());\n");
            }
            buffer.append("    return _result;");

            buffer.append("  }\n\n");

            //
            // Modifier
            //
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("  public void " + name + "(" + type
                              + " value) {\n");

                // Method name
                buffer.append("    org.omg.CORBA.Request _request = this._request(\"_set_");
                buffer.append(name);
                buffer.append("\");\n");

                // Input parameter
                buffer.append("    org.omg.CORBA.Any $value = _request.add_in_arg();\n");
                helper = XmlType2Java.getHelperType(typeEl);
                if (helper == null) {
                    buffer.append("    $value.insert_");
                    buffer.append(XmlType2Java.basicORBTypeMapping(typeEl));
                    buffer.append("(value");
                    buffer.append(");\n");
                } else {
                    buffer.append("    ");
                    buffer.append(helper);
                    buffer.append(".insert($value,value);\n");
                }

                // Invocation
                buffer.append("    _request.invoke();\n");

                // Catch exceptions
                buffer.append("    java.lang.Exception _exception = _request.env().exception();\n");
                buffer.append("    if (_exception != null) {\n");
                buffer.append("      throw (org.omg.CORBA.SystemException) _exception;\n");
                buffer.append("    };\n");

                buffer.append("  }\n\n");
            }

        }
    }

    private void generateJavaStubAttributeDeclAsync(StringBuffer buffer, Element doc, String i_name)
    	throws Exception 
    {
    	// Get the type
    	NodeList nodes = doc.getChildNodes();
    	Element typeEl = (Element) nodes.item(0);
    	String type = XmlType2Java.getType(typeEl);
    	String readonly = doc.getAttribute(OMG_readonly);

    	// Get & set methods
    	for (int i = 1; i < nodes.getLength(); i++) {
    		Element el = (Element) nodes.item(i);
    		String name = el.getAttribute(OMG_name);

    		//
    		// 	Accessor
    		//
    		buffer.append("  public void sendc_get_" + name + "(AMI_" + i_name + "Handler ami_handler) {\n");

    		// Method name
    		buffer.append("    org.omg.CORBA.Request _request = this._request(\"_get_");
    		buffer.append(name);
    		buffer.append("\");\n");

    		// Return type
    		buffer.append("    _request.set_return_type(");
    		buffer.append(XmlType2Java.getTypecode(typeEl));
    		buffer.append(");\n");

    		// Invocation
                buffer.append("    es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate delegate = \n");
                buffer.append("\t (es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate)_get_delegate();\n");
                buffer.append("    delegate.asyncRequest((es.tid.TIDorbj.core.RequestImpl)_request, \n");
                buffer.append("\t (org.omg.CORBA.Object)ami_handler);\n");
                
                buffer.append("  }\n\n");

    		//
    		// Modifier
    		//
    		if (readonly == null || !readonly.equals(OMG_true)) {
    			buffer.append("  public void sendc_set_" + name + "(AMI_" + i_name + "Handler ami_handler, " 
    							+ type + " attr_" + name + ") {\n");

    			// Method name
    			buffer.append("    org.omg.CORBA.Request _request = this._request(\"_set_");
    			buffer.append(name);
    			buffer.append("\");\n");

    			// Input parameter
    			buffer.append("    org.omg.CORBA.Any $attr_" + name + " = _request.add_in_arg();\n");
                        String helper = XmlType2Java.getHelperType(typeEl);
    			if (helper == null) {
    				buffer.append("    $attr_" + name + ".insert_");
    				buffer.append(XmlType2Java.basicORBTypeMapping(typeEl));
    				buffer.append("(attr_" + name);
    				buffer.append(");\n");
    			} else {
    				buffer.append("    ");
    				buffer.append(helper);
    				buffer.append(".insert($attr_" + name + ",attr_" + name + ");\n");
    			}

    			// Invocation
                        buffer.append("    es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate delegate = \n");
                        buffer.append("\t (es.tid.TIDorbj.core.comm.iiop.IIOPCommunicationDelegate)_get_delegate();\n");
                        buffer.append("    delegate.asyncRequest((es.tid.TIDorbj.core.RequestImpl)_request, \n");
                        buffer.append("\t (org.omg.CORBA.Object)ami_handler);\n");
                        
    			buffer.append("  }\n\n");
    		}
    	}
    }

}
