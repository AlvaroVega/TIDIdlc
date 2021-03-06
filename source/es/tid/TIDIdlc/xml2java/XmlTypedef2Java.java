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
import es.tid.TIDIdlc.xml2java.structures.*;
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.util.Traces;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Vector;

import org.w3c.dom.*;

/**
 * Generates Java for typedefs.
 */
class XmlTypedef2Java
    implements Idl2XmlNames
{

    private boolean m_generate;

    public XmlTypedef2Java()
    {}

    /** Generate Java */
    public void generateJava(Element doc, String outputDirectory,
                             String genPackage, boolean generateCode)
        throws Exception
    {

        m_generate = generateCode;
        NodeList nodes = doc.getChildNodes();
        Element type = (Element) nodes.item(0);
        String typeString = XmlType2Java.getTypedefType(type);
        if (type.getTagName().equals(OMG_sequence)) {
            Element decl = (Element) nodes.item(1);
            generateArrayJava(type, decl, outputDirectory, genPackage);
        } else if (type.getTagName().equals(OMG_enum)) {
            XmlEnum2Java gen = new XmlEnum2Java();
            gen.generateJava(type, outputDirectory, genPackage, generateCode);
        }

        for (int i = 1; i < nodes.getLength(); i++) {
            Element decl = (Element) nodes.item(i);
            String typeName = decl.getAttribute(OMG_name);
            String scopedTypeName = decl.getAttribute(OMG_scoped_name);
            String holderType = XmlType2Java.basicOutMapping(typeString);
            if (decl.getTagName().equals(OMG_array)) {
                generateArrayJava(type, decl, outputDirectory, genPackage);
                // Un array es como una secuencia: el tipo que se guarda en
                // el TypedefManager ya es un tipo Java valido. No hay que
                // convertirlo a posteriori.
                typeString = XmlType2Java.basicMapping(typeString);
                holderType = XmlType2Java.basicOutMapping(typeString);
                String bounds = "";
                for (int k = 0; k < decl.getChildNodes().getLength(); k++)
                    bounds += "[]";
                TypedefManager
                    .getInstance()
                    .typedef(
                             scopedTypeName,
                             typeString + bounds,
                             TypeManager.convert(scopedTypeName)/* holderType */,
                             scopedTypeName, null, null);
            } else {
                // Si no es un array se almacena lo que nos devolvio
                // xmlType2Java.getTypedefType
                // que para el caso de secuencias es un tipo Java ya convertido,
                // pero en el resto de los casos es todavia un tipo IDL.
                String helperType = "";
                if (type.getTagName().equals(OMG_scoped_name)) {
                    helperType = scopedTypeName;
                    if (holderType == null) {
                        // if we have a typedef mapped to a scoped name, we use
                        // the holder of the originary typedef, if exists
                        String type_name = type.getAttribute(OMG_name);
                        holderType = TypedefManager
                            .getInstance().getUnrolledHolderType(type_name);
                        if (holderType == null)
                            // If not exists, the original type is not a
                            // typedef, so we use the holder of the original type
                            holderType = typeString;
                        else
                            holderType = TypeManager.convert(holderType);
                    }
                }
                TypedefManager.getInstance().typedef(scopedTypeName,
                		typeString, holderType,
						helperType, null, null);
            }
        }

        if (!type.getTagName().equals(OMG_sequence)
            && !type.getTagName().equals(OMG_enum)) {
            Element decl = (Element) nodes.item(1);
            generateArrayJava(type, decl, outputDirectory, genPackage);
        }
    }

    /** Generate Java */
    private void generateArrayJava(Element type, Element decl,
                                   String outputDirectory, String genPackage)
        throws Exception
    {
        // Get package components
        String targetDirName = outputDirectory;
        if (targetDirName.charAt(targetDirName.length() - 1) == File.separatorChar) {
            targetDirName = targetDirName.substring(0,
            		targetDirName.length() - 1);
        }
        StringTokenizer tok = new StringTokenizer(genPackage, ".");
        while (tok.hasMoreTokens()) {
            targetDirName = targetDirName + File.separatorChar
                            + tok.nextToken();
        }

        if (this.m_generate) {
            // Make target directory
            File targetDir = new File(targetDirName);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }

        FileWriter writer;
        BufferedWriter buf_writer;
        String fileName, contents;

        String typeHolder = XmlType2Java.getType(type);
        // ArrayHolder generation. Only generate the holder for the sequence type
        if (type.getTagName().equals(OMG_sequence)
            || (decl.getTagName().equals(OMG_array))) {
            fileName = decl.getAttribute(OMG_name) + "Holder" + ".java";
            if (this.m_generate) {
                Traces.println("XmlTypedef2Java:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + targetDirName
                               + File.separatorChar + fileName + "...",
                               Traces.USER);
            }
            String name = decl.getAttribute(OMG_name);
            if (decl.getTagName().equals(OMG_array)) {
                NodeList children = decl.getChildNodes();
                for (int i = 0; i < children.getLength(); i++) {
                    typeHolder += "[]";
                }
            }
            contents = XmlJavaHolderGenerator.generate(genPackage, name,
                                                       typeHolder);
            if (this.m_generate) {
                writer = new FileWriter(targetDirName + File.separatorChar
                                        + fileName);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(contents);
                buf_writer.close();
            }
        }

        // Helper generation. Generate the Helper for all the types
        fileName = decl.getAttribute(OMG_name) + "Helper" + ".java";
        if (this.m_generate) {
            Traces.println("XmlTypedef2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }

        contents = generateJavaHelperDef(type, decl, genPackage, typeHolder);
        if (this.m_generate) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }
    }

    private String generateJavaHelperDef(Element type, Element decl,
                                         String genPackage, String internalType)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();

        Vector indexes = new Vector();
        Vector isArray = new Vector();

        NodeList indexChilds = decl.getChildNodes();
        for (int k = 0; k < indexChilds.getLength(); k++) {
            Element indexChild = (Element) indexChilds.item(k);
            if (indexChild != null) {
                indexes.insertElementAt(new Long(XmlExpr2Java.getIntExpr(indexChild)), k);
                isArray.insertElementAt(new Boolean(true), k);
            }
        }

        boolean isSequence = type.getTagName().equals(OMG_sequence);

        if (isSequence) {
            int val = 0;
            String tag = type.getTagName();
            Element el = type;
            while (tag.equals(OMG_sequence)) {
                el = (Element) el.getFirstChild();
                Element expr = (Element) el.getNextSibling();
                if (expr != null) {
                    indexes.addElement(new Long(XmlExpr2Java.getIntExpr(expr)));
                } else {
                    indexes.addElement(new String("length" + val));
                }
                isArray.addElement(new Boolean(false));
                tag = el.getTagName();
                val++;
            }
            type = el;
        }

        boolean isBoundedString = (XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_string) || 
        		XmlType2Java.getTypedefType(type).equals(Idl2XmlNames.OMG_wstring)) &&
				(type.getFirstChild() != null);

        if (isBoundedString) {
            int val = 0;
            Element expr = (Element) type.getFirstChild();
            if (expr != null)
                indexes.addElement(new Long(XmlExpr2Java.getIntExpr(expr)));
            isArray.addElement(new Boolean(false));
        }

        StructProcessor processor;

        // Header
        String name = decl.getAttribute(OMG_name);
        XmlJavaHeaderGenerator.generate(buffer, "helper", name + "Helper",
                                        genPackage);

        // Class header
        buffer.append("abstract public class ");
        buffer.append(name);
        buffer.append("Helper {\n\n");
        buffer.append("  private static org.omg.CORBA.ORB _orb() {\n");
        buffer.append("    return org.omg.CORBA.ORB.init();\n");
        buffer.append("  }\n\n");

        if (!isSequence) {
            String scopedType = decl.getAttribute(OMG_scoped_name);
            String holder_name = TypedefManager.getInstance().getUnrolledHolderType(scopedType);
            XmlJavaHelperGenerator.generateInsertExtract(buffer, internalType, holder_name + "Holder");
        } else {
            XmlJavaHelperGenerator.generateInsertExtract(buffer, internalType, name + "Holder");
        }

        buffer
            .append("  private static org.omg.CORBA.TypeCode _type = null;\n");
        buffer.append("  public static org.omg.CORBA.TypeCode type() {\n");
        buffer.append("    if (_type == null) {\n");
        processor = new StructType();
        processor.generateJava(buffer, null, type, indexes, isArray);
        buffer.append("      _type = _orb().create_alias_tc(id(), \"");
        buffer.append(name);
        buffer.append("\", original_type);\n");
        buffer.append("    }\n");
        buffer.append("    return _type;\n");
        buffer.append("  };\n\n");

        XmlJavaHelperGenerator.generateRepositoryId(buffer, decl);

        buffer.append("  public static ");
        buffer.append(internalType);
        buffer.append(" read(org.omg.CORBA.portable.InputStream is) {\n");
        buffer.append("    ");
        buffer.append(internalType);
        buffer.append(" result;\n");
        processor = new StructReader();
        processor.generateJava(buffer, null, type, indexes, isArray);
        buffer.append("    return result;\n");
        buffer.append("  };\n\n");

        buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, ");
        buffer.append(internalType);
        buffer.append(" val) {\n");
        processor = new StructWriter();
        processor.generateJava(buffer, null, type, indexes, isArray);
        buffer.append("  };\n\n");

        buffer.append("}\n");

        return buffer.toString();
    }

}