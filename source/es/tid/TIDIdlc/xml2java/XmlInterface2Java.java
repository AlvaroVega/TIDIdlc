/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 314 $
* Date: $Date: 2009-07-17 12:50:46 +0200 (Fri, 17 Jul 2009) $
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

import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.xmlsemantics.*;
import es.tid.TIDIdlc.util.Traces;
import es.tid.TIDIdlc.CompilerConf;
import java.io.*;
import java.util.StringTokenizer;

import org.w3c.dom.*;

/**
 * Generates Java for interfaces.
 */
class XmlInterface2Java extends XmlInterfaceUtils2Java
    implements Idl2XmlNames
{

    private boolean m_generate;

    /** Generate Java */
    public void generateJava(Element doc, String outputDirectory,
                             String genPackage, boolean generateCode)
        throws Exception
    {

        m_generate = generateCode;
        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null)
                             && (isAbstractS.equals(OMG_true));
        String isLocalS = doc.getAttribute(OMG_local);
        boolean isLocal = (isLocalS != null) && (isLocalS.equals(OMG_true));

        if (doc.getAttribute(OMG_fwd).equals(OMG_true)) {
            return; // it is a forward declaration
        }

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

        if (generateCode) {
            // Make target directory
            File targetDir = new File(targetDirName);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
        }

        FileWriter writer;
        BufferedWriter buf_writer;
        String fileName, contents;

        if (isAbstract) {

            // Interface generation
            fileName = doc.getAttribute(OMG_name) + ".java";
            if (generateCode) {
                Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + targetDirName
                               + File.separatorChar + fileName + "...",
                               Traces.USER);
            }
            contents = generateJavaAbstractInterfaceDef(doc, outputDirectory,
                                                        genPackage);
            if (generateCode) {
                writer = new FileWriter(targetDirName + File.separatorChar
                                        + fileName);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(contents);
                buf_writer.close();
            }

        } else {

            // InterfaceOperations generation
            fileName = doc.getAttribute(OMG_name) + "Operations.java";
            if (generateCode) {
                Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + targetDirName
                               + File.separatorChar + fileName + "...",
                               Traces.USER);
            }
            contents = generateJavaInterfaceOperationsDef(doc, outputDirectory,
                                                          genPackage);
            if (generateCode) {
                writer = new FileWriter(targetDirName + File.separatorChar
                                        + fileName);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(contents);
                buf_writer.close();
            }

            // Interface generation
            fileName = doc.getAttribute(OMG_name) + ".java";
            if (generateCode) {
                Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + targetDirName
                               + File.separatorChar + fileName + "...",
                               Traces.USER);
            }
            contents = generateJavaInterfaceDef(doc, genPackage);
            if (generateCode) {
                writer = new FileWriter(targetDirName + File.separatorChar
                                        + fileName);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(contents);
                buf_writer.close();
            }

            // _InterfaceStub generation
            if ((!CompilerConf.getNoStub()) && (!isLocal)) {
                fileName = "_" + doc.getAttribute(OMG_name) + "Stub" + ".java";
                if (generateCode) {
                    Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + targetDirName
                                   + File.separatorChar + fileName + "...",
                                   Traces.USER);
                }
                XmlInterfaceStub2Java genStub = new XmlInterfaceStub2Java();
                contents = genStub.generateJava(doc, genPackage);
                if (generateCode) {
                    writer = new FileWriter(targetDirName + File.separatorChar
                                            + fileName);
                    buf_writer = new BufferedWriter(writer);
                    buf_writer.write(contents);
                    buf_writer.close();
                }
            }// End of CompilerConf.getNo_Stub() && !isLocal

            // InterfacePOA generation
            if ((!CompilerConf.getNoSkel()) && (!isLocal)) {
                fileName = doc.getAttribute(OMG_name) + "POA" + ".java";
                if (generateCode) {
                    Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + targetDirName
                                   + File.separatorChar + fileName + "...",
                                   Traces.USER);
                }
                XmlInterfaceSkeleton2Java genSkeleton = new XmlInterfaceSkeleton2Java();
                contents = genSkeleton.generateJava(doc, genPackage);
                if (generateCode) {
                    writer = new FileWriter(targetDirName + File.separatorChar
                                            + fileName);
                    buf_writer = new BufferedWriter(writer);
                    buf_writer.write(contents);
                    buf_writer.close();
                }

                // InterfacePOATie generation
                if (!CompilerConf.getNoTie()) {
                    fileName = doc.getAttribute(OMG_name) + "POATie" + ".java";
                    if (generateCode) {
                        Traces.println("XmlInterface2Java:->",
                                       Traces.DEEP_DEBUG);
                        Traces.println("Generating : " + targetDirName
                                       + File.separatorChar + fileName + "...",
                                       Traces.USER);
                    }
                    XmlInterfaceTie2Java genTie = new XmlInterfaceTie2Java();
                    contents = genTie.generateJava(doc, genPackage);
                    if (generateCode) {
                        writer = new FileWriter(targetDirName
                                                + File.separatorChar + fileName);
                        buf_writer = new BufferedWriter(writer);
                        buf_writer.write(contents);
                        buf_writer.close();
                    }
                }// End of CompilerConf.getNo_Tie()
            }// End of CompilerConf.getNo_Skel()
        }// End of isAbstract is false..

        // InterfaceHolder generation
        fileName = doc.getAttribute(OMG_name) + "Holder" + ".java";
        if (generateCode) {
            Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        String name = doc.getAttribute(OMG_name);
        contents = XmlJavaHolderGenerator.generate(genPackage, name, name);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }

        // InterfaceHelper generation
        fileName = doc.getAttribute(OMG_name) + "Helper" + ".java";
        if (generateCode) {
            Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating : " + targetDirName + File.separatorChar
                           + fileName + "...", Traces.USER);
        }
        contents = generateJavaHelperDef(doc, genPackage);
        if (generateCode) {
            writer = new FileWriter(targetDirName + File.separatorChar
                                    + fileName);
            buf_writer = new BufferedWriter(writer);
            buf_writer.write(contents);
            buf_writer.close();
        }
        // Interface Local Base
        if (isLocal) {// It is a local interface.
            fileName = doc.getAttribute(OMG_name) + "LocalBase" + ".java";
            if (generateCode) {
                Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                Traces.println("Generating : " + targetDirName
                               + File.separatorChar + fileName + "...",
                               Traces.USER);
            }
            contents = generateJavaLocalBaseDef(doc, genPackage);
            if (generateCode) {
                writer = new FileWriter(targetDirName + File.separatorChar
                                        + fileName);
                buf_writer = new BufferedWriter(writer);
                buf_writer.write(contents);
                buf_writer.close();
            }// end of generate code
            if (!CompilerConf.getNoTie()) {
            	// generates the Local Tie for a
                // Local Object CORBA3.0
                fileName = doc.getAttribute(OMG_name) + "LocalTie" + ".java";
                if (generateCode) {
                    Traces.println("XmlInterface2Java:->", Traces.DEEP_DEBUG);
                    Traces.println("Generating : " + targetDirName
                                   + File.separatorChar + fileName + "...",
                                   Traces.USER);
                }
                XmlInterfaceTie2Java genTie = new XmlInterfaceTie2Java();
                contents = genTie.generateLocalTie(doc, genPackage);
                if (generateCode) {
                    writer = new FileWriter(targetDirName + File.separatorChar
                                            + fileName);
                    buf_writer = new BufferedWriter(writer);
                    buf_writer.write(contents);
                    buf_writer.close();
                }
            }// End of CompilerConf.getNo_Tie()
        }// end of local!=null
    }

    private String generateJavaInterfaceOperationsDef(Element doc,
                                                      String outputDir,
                                                      String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        // Package header
        XmlJavaHeaderGenerator.generate(buffer, "interfaceOperations", name,
                                        genPackage);

        // Class header
        buffer.append("public interface ");
        buffer.append(name);
        buffer.append("Operations");
        String inh = generateJavaInheritance(doc, true, false);
        if (inh.length() > 0) {
            buffer.append("\n   extends ");
            // Not enougth for all cases
            if (name.startsWith("AMI_") && inh.startsWith("Messaging"))
                buffer.append("org.omg.");
            buffer.append(inh);
        }
        buffer.append(" {\n\n");

        generateJavaExportDef(buffer, doc, outputDir, name, genPackage);

        buffer.append("}\n");

        return buffer.toString();
    }

    private String generateJavaInterfaceDef(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        // Package header
        XmlJavaHeaderGenerator.generate(buffer, "interface", name, genPackage);

        // Class header
        buffer.append("public interface ");
        buffer.append(name);
        buffer.append("\n   extends ");
        buffer.append(name);
        buffer.append("Operations,\n");
        String inh = generateJavaInheritance(doc, false, true);
        if (inh.length() > 0) {
            buffer.append("           ");
            buffer.append(inh);
            buffer.append("\n");
        }
        name = doc.getAttribute(OMG_local); // by macp
        if ((name != null) && (name.equals(OMG_true)))
        //  Local interface
        //  Implementation Corba
        //  3.0.
        {
            buffer.append("           org.omg.CORBA.LocalInterface,\n");
            // inheritance from Local not from Object.
        } else {
            buffer.append("           org.omg.CORBA.Object,\n");
            // Usual Interface.
        }
        buffer.append("           org.omg.CORBA.portable.IDLEntity {\n\n");
        buffer.append("}\n");

        return buffer.toString();
    }

    private String generateJavaAbstractInterfaceDef(Element doc,
                                                    String outputDir,
                                                    String genPackage)
        throws Exception
    {
        StringBuffer buffer = new StringBuffer();
        String name = doc.getAttribute(OMG_name);
        // Package header
        XmlJavaHeaderGenerator.generate(buffer, "interface", name, genPackage);

        // Class header
        buffer.append("public interface ");
        buffer.append(name);
        buffer.append("\n   extends ");
        String inh = generateJavaInheritance(doc, true, true);
        if (inh.length() > 0) {
            buffer.append(inh);
            buffer.append("\n           ");
        }
        buffer.append("org.omg.CORBA.portable.IDLEntity {\n\n");

        generateJavaExportDef(buffer, doc, outputDir, name, genPackage);

        buffer.append("}\n");

        return buffer.toString();
    }

    private void generateJavaExportDef(StringBuffer buffer, Element doc,
                                       String outputDir, String interfaceName,
                                       String genPackage)
        throws Exception
    {
        // Items definition
        NodeList nodes = doc.getChildNodes();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String tag = el.getTagName();
            if (tag.equals(OMG_op_dcl)) {// Operation declaration
                buffer.append("  ");
                generateJavaMethodHeader(buffer, el);
                buffer.append(";\n\n");
            } else if (tag.equals(OMG_attr_dcl)) {// Attribute declaration
                generateJavaAttributeDecl(buffer, el);
            } else if (tag.equals(OMG_const_dcl)) {// Constant declaration
                generateJavaConstDecl(buffer, el);
            } else { // SubPackage definition.
                generateJavaSubPackageDef(el, outputDir, interfaceName,
                                          genPackage);
            }
        }
        buffer.append("\n");
    }

    private void generateJavaSubPackageDef(Element doc, String outputDir,
                                           String interfaceName,
                                           String genPackage)
        throws Exception
    {
        String newPackage;
        if (!genPackage.equals("")) {
            newPackage = genPackage + "." + interfaceName + "Package";
        } else {
            newPackage = interfaceName + "Package";
        }
        Element definition = doc;
        String tag = definition.getTagName();
        if (tag.equals(OMG_const_dcl)) {
            XmlConst2Java gen = new XmlConst2Java();
            gen.generateJava(definition, outputDir, newPackage,
                              this.m_generate);
        } else if (tag.equals(OMG_enum)) {
            XmlEnum2Java gen = new XmlEnum2Java();
            gen.generateJava(definition, outputDir, newPackage,
                              this.m_generate);
        } else if (tag.equals(OMG_struct)) {
            XmlStruct2Java gen = new XmlStruct2Java();
            gen.generateJava(definition, outputDir, newPackage,
                              this.m_generate);
        } else if (tag.equals(OMG_union)) {
            XmlUnion2Java gen = new XmlUnion2Java();
            gen.generateJava(definition, outputDir, newPackage,
                              this.m_generate);
        } else if (tag.equals(OMG_exception)) {
            XmlException2Java gen = new XmlException2Java();
            gen.generateJava(definition, outputDir, newPackage,
                              this.m_generate);
        } else if (tag.equals(OMG_typedef)) {
            XmlTypedef2Java gen = new XmlTypedef2Java();
            gen.generateJava(definition, outputDir, newPackage,
                              this.m_generate);
        }
    }

    private void generateJavaAttributeDecl(StringBuffer buffer, Element doc)
    {
        // Get type
        NodeList nodes = doc.getChildNodes();
        String type = XmlType2Java.getType((Element) nodes.item(0));
        String readonly = doc.getAttribute(OMG_readonly);

        // Accessors geneation
        for (int i = 1; i < nodes.getLength(); i++) {
            Element el = (Element) nodes.item(i);
            String name = el.getAttribute(OMG_name);
            buffer.append("  " + type + " " + name + "();\n");
            if (readonly == null || !readonly.equals(OMG_true)) {
                buffer.append("  void " + name + "(" + type + " value);\n\n");
            }
            buffer.append("\n");
        }
    }

    private void generateJavaConstDecl(StringBuffer buffer, Element doc)
        throws SemanticException
    {
        NodeList nodes = doc.getChildNodes();
        String scopedName = doc.getAttribute(OMG_scoped_name);

        // Value generation
        Element typeEl = (Element) nodes.item(0);
        String type = XmlType2Java.getType(typeEl);
        buffer.append("  ");
        buffer.append(type);
        buffer.append(" ");
        buffer.append(doc.getAttribute(OMG_name));
        buffer.append(" = (");
        buffer.append(type);
        buffer.append(")");

        // Expre generation
        Element exprEl = (Element) nodes.item(1);
        Object expr = IdlConstants.getInstance().getValue(scopedName);
        String typeExpr = IdlConstants.getInstance().getType(scopedName);
        buffer.append(XmlExpr2Java.toString(expr, typeExpr));
        buffer.append(";\n\n");
    }

    private String generateJavaInheritance(Element doc, boolean doAbstracts,
                                           boolean finalComma)
    {
        StringBuffer buffer = new StringBuffer();

        // Items definition
        Element el1 = (Element) doc.getFirstChild();
        if (el1 == null)
            return buffer.toString(); // nothing to do

        if (el1.getTagName().equals(OMG_inheritance_spec)) {
            NodeList nodes = el1.getChildNodes();
            for (int i = 0; i < nodes.getLength(); i++) {
                Element el = (Element) nodes.item(i);
                String clase = el.getAttribute(OMG_name);
                if (doAbstracts) {
                    if ((Scope.getKind(clase) == Scope.KIND_INTERFACE_FWD_ABS)
                        || (Scope.getKind(clase) == Scope.KIND_INTERFACE_ABS)) {
                        buffer.append(TypeManager.convert(clase));
                    } else {
                        buffer.append(TypeManager.convert(clase));
                        buffer.append("Operations");
                    }
                    if ((i != nodes.getLength() - 1) || finalComma)
                        buffer.append(", ");
                } else {
                    if (!((Scope.getKind(clase) == Scope.KIND_INTERFACE_FWD_ABS) || (Scope
                        .getKind(clase) == Scope.KIND_INTERFACE_ABS))) {
                        buffer.append(TypeManager.convert(clase));
                        if ((i != nodes.getLength() - 1) || finalComma)
                            buffer.append(", ");
                    }
                }
            }
        }
        return buffer.toString();
    }

    private String generateJavaHelperDef(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        String isAbstractS = doc.getAttribute(OMG_abstract);
        boolean isAbstract = (isAbstractS != null)
                             && (isAbstractS.equals(OMG_true));

        String isLocalS = doc.getAttribute(OMG_local);
        boolean isLocal = (isLocalS != null) && (isLocalS.equals(OMG_true));

        XmlJavaHeaderGenerator.generate(buffer, "helper", name + "Helper",
                                        genPackage);

        // Class header
        buffer.append("abstract public class ");
        buffer.append(name);
        buffer.append("Helper {\n\n");
        buffer.append("  private static org.omg.CORBA.ORB _orb() {\n");
        buffer.append("    return org.omg.CORBA.ORB.init();\n");
        buffer.append("  }\n\n");

        buffer.append("  private static org.omg.CORBA.TypeCode _type = null;\n");
        buffer.append("  public static org.omg.CORBA.TypeCode type() {\n");
        buffer.append("    if (_type == null) {\n");
        buffer.append("      _type = _orb().create_interface_tc(id(), \"");
        buffer.append(name);
        buffer.append("\");\n");
        buffer.append("    }\n");
        buffer.append("    return _type;\n");
        buffer.append("  }\n\n");

        XmlJavaHelperGenerator.generateRepositoryId(buffer, doc);

        XmlJavaHelperGenerator.generateInsertExtract_Object(buffer, name,
                                                            name + "Holder");

        if (!isAbstract) {
            buffer.append("  public static ");
            buffer.append(name);
            buffer.append(" read(org.omg.CORBA.portable.InputStream is) {\n");
            buffer.append("    return narrow(is.read_Object(), true); \n");
            buffer.append("  }\n\n");

            buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, ");
            buffer.append(name);
            buffer.append(" val) {\n");
            buffer.append("    if (!(os instanceof org.omg.CORBA_2_3.portable.OutputStream)) {;\n");
            buffer.append("      throw new org.omg.CORBA.BAD_PARAM();\n");
            buffer.append("    };\n");
            buffer.append("    if (val != null && !(val instanceof org.omg.CORBA.portable.ObjectImpl)) {;\n");
            buffer.append("      throw new org.omg.CORBA.BAD_PARAM();\n");
            buffer.append("    };\n");
            buffer.append("    os.write_Object((org.omg.CORBA.Object)val);\n");
            buffer.append("  }\n\n");

            buffer.append("  public static ");
            buffer.append(name);
            buffer.append(" narrow(org.omg.CORBA.Object obj) {\n");
            buffer.append("    return narrow(obj, false);\n");
            buffer.append("  }\n\n");

            buffer.append("  public static ");
            buffer.append(name);
            buffer.append(" unchecked_narrow(org.omg.CORBA.Object obj) {\n");
            buffer.append("    return narrow(obj, true);\n");
            buffer.append("  }\n\n");

            String stub = "_" + name + "Stub";
            buffer.append("  private static ");
            buffer.append(name);
            buffer.append(" narrow(org.omg.CORBA.Object obj, boolean is_a) {\n");
            buffer.append("    if (obj == null) {\n");
            buffer.append("      return null;\n");
            buffer.append("    }\n");
            buffer.append("    if (obj instanceof " + name + ") {\n");
            buffer.append("      return (" + name + ")obj;\n");
            buffer.append("    }\n");
            if (!isLocal) {
                buffer.append("    if (is_a || obj._is_a(id())) {\n");
                buffer.append("      " + stub + " result = (" + stub + ")new "
                              + stub + "();\n");
                buffer.append("      ((org.omg.CORBA.portable.ObjectImpl) result)._set_delegate\n");
                buffer.append("        (((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate());\n");
                buffer.append("      return (" + name + ")result;\n");
                buffer.append("    }\n");
            }
            buffer.append("    throw new org.omg.CORBA.BAD_PARAM();\n");
            buffer.append("  }\n\n");
        } else {//is an Abstract Interface
            buffer.append("    /* Methods write, read, insert, extract and narrow\n");
            buffer.append("       are not implemented yet for abstract interfaces */\n");
            buffer.append("  public static ");
            buffer.append(name);
            buffer.append(" read(org.omg.CORBA.portable.InputStream is) {\n");
            buffer.append("    return null; // Not implemented yet \n");
            buffer.append("  }\n\n");

            buffer.append("  public static void write(org.omg.CORBA.portable.OutputStream os, ");
            buffer.append(name);
            buffer.append(" val) {\n");
            buffer.append("    // Not implemented yet \n");
            buffer.append("  }\n\n");

            buffer.append("  public static ");
            buffer.append(name);
            buffer.append(" narrow(java.lang.Object obj) {\n");
            buffer.append("    return null; // Not implemented yet \n");
            buffer.append("  }\n\n");

            buffer.append("  public static ");
            buffer.append(name);
            buffer.append(" unckeched_narrow(java.lang.Object obj) {\n");
            buffer.append("    return null; // Not implemented yet \n");
            buffer.append("  }\n\n");

            buffer.append("  private static ");
            buffer.append(name);
            buffer.append(" narrow(org.omg.CORBA.Object obj, boolean is_a) {\n");
            buffer.append("    return null; // Not implemented yet \n");
            buffer.append("  }\n\n");
        }

        buffer.append("}\n");

        return buffer.toString();
    }

    private String generateJavaLocalBaseDef(Element doc, String genPackage)
    {
        StringBuffer buffer = new StringBuffer();
        // Header
        String name = doc.getAttribute(OMG_name);
        XmlJavaHeaderGenerator.generate(buffer, "LocalBase",
                                        name + "LocalBase", genPackage);

        // Class header
        buffer.append("public abstract class ");
        buffer.append(name);
        buffer.append("LocalBase ");
        buffer.append("extends \n org.omg.CORBA.LocalObject implements ");
        if (!genPackage.equals(""))
            buffer.append(genPackage + ".");
        buffer.append(name + "{\n\n");
        buffer.append("  private String [] _type_ids = {\n");
        new XmlInterfaceUtils2Java().generateInterfacesSupported(buffer, doc);
        buffer.append("  };\n\n");
        buffer.append("  public String [] _ids(){\n return (String[]) _type_ids.clone(); ");
        buffer.append("  }\n\n");
        buffer.append("  }\n\n");
        return buffer.toString();
    }
}
