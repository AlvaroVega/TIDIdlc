/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 117 $
* Date: $Date: 2006-02-22 13:25:35 +0100 (Wed, 22 Feb 2006) $
* Last modified by: $Author: iredondo $
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

import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.idl2xml.Idl2XmlNames;
import es.tid.TIDIdlc.idl2xml.IncludeORB;
import es.tid.TIDIdlc.util.Traces;

import java.io.*;
import java.util.*;

import org.w3c.dom.*;

/**
 * Generates Java for modules.
 */
class XmlModule2Java
    implements Idl2XmlNames
{

    /** Generate Java */
    public void generateJava(Element doc, String outputDirectory,
                             String genPackage, boolean createDir)
        throws Exception
    {
        // Get the package whole name
        if (genPackage.length() > 0) {
            String s = doc.getAttribute(OMG_name);
            if (!s.equals("")) {
                genPackage = genPackage + "." + doc.getAttribute(OMG_name);
            }
        } else {
            genPackage = doc.getAttribute(OMG_name);
        }

        Enumeration packageTableKeys = CompilerConf.getPackageToTable().keys();
        boolean packUser = false;
        String module, pack;
        while (packageTableKeys.hasMoreElements() && !packUser) {
        	module = (String) packageTableKeys.nextElement();
        	pack = (String) CompilerConf.getPackageToTable().get(module);
        	if (!IncludeORB.isHardCodedIDL(module) && !IncludeORB.isHardCodedModule(module) && pack.equals("org.omg"))
        		packUser = true;
        }
        
        // Modify the genPackage if the module name matches a -package_to param
        if (createDir && (!genPackage.equals("org.omg") || packUser)) {
            // Get package components
            String targetDirName = outputDirectory;
            if (targetDirName.charAt(targetDirName.length() - 1) == File.separatorChar) {
                targetDirName = targetDirName.substring(0, targetDirName.length() - 1);
            }
            StringTokenizer tok = new StringTokenizer(genPackage, ".");
            while (tok.hasMoreTokens()) {
                targetDirName = targetDirName + File.separatorChar+ tok.nextToken();
            }
            // Make target directory
            File targetDir = new File(targetDirName);
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }

            // Module generation
            Traces.println("XmlModule2Java:->", Traces.DEEP_DEBUG);
            Traces.println("Generating module: " + targetDirName + "...",Traces.USER);
        }
        generateJavaModuleDef(doc, outputDirectory, genPackage, createDir);
    }

    private void generateJavaModuleDef(Element doc, String outputDir,
                                       String genPackage, boolean createDir)
        throws Exception
    {
        NodeList definitions = doc.getChildNodes();
        for (int i = 0; i < definitions.getLength(); i++) {
            Element definition = (Element) definitions.item(i);
            String tag = definition.getTagName();
            if (tag.equals(OMG_module)) {
                // Conditional compilation to avoid the generation of code
                // asociated with the molule org.omg.CORBA
                Attr not_code = definition.getAttributeNode(OMG_Do_Not_Generate_Code);
                XmlModule2Java gen = new XmlModule2Java();
                if (not_code == null) {
                    // The attribute OMG_Do_Not_Generate_Code
                    // doesn't exists, so we generate code
                    gen.generateJava(definition, outputDir, genPackage, true);
                } else {
                    // The attribute OMG_Do_Not_Generate_Code
                    // exists, so we don't generate code
                    gen.generateJava(definition, outputDir, genPackage, false);
                }
            } else if (tag.equals(OMG_interface)) {
                XmlInterface2Java gen = new XmlInterface2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_const_dcl)) {
                XmlConst2Java gen = new XmlConst2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_enum)) {
                XmlEnum2Java gen = new XmlEnum2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_struct)) {
                XmlStruct2Java gen = new XmlStruct2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_union)) {
                XmlUnion2Java gen = new XmlUnion2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_exception)) {
                XmlException2Java gen = new XmlException2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_typedef)) {
                XmlTypedef2Java gen = new XmlTypedef2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            } else if (tag.equals(OMG_valuetype)) {
                XmlValuetype2Java gen = new XmlValuetype2Java();
                gen.generateJava(definition, outputDir, genPackage, createDir);
            }
        }
    }

}