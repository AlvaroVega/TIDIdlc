/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 330 $
* Date: $Date: 2012-02-27 18:02:15 +0100 (Mon, 27 Feb 2012) $
* Last modified by: $Author: avega $
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

package es.tid.TIDIdlc;

import es.tid.TIDIdlc.async.ExceptionHolderIdl;
import es.tid.TIDIdlc.async.ReplyHandlerIdl;
import es.tid.TIDIdlc.idl2xml.Preprocessor; // define & undefine
import es.tid.TIDIdlc.CompilerConf;
import es.tid.TIDIdlc.util.Traces;

import java.io.File;
import java.util.Vector;

/**
 * Class for command line parameters
 */

// review: David A. Velasco
public class Arguments
{

    /**
     * This method parses the command line searching for arguments.
     */

    private static String st_version_line = "TIDorb IDL compiler version " + CompilerConf.st_compiler_version + " (c) Telef\u00F3nica I+D.\n";
	
    public static void parse(String args[])
        throws Exception
    {
        boolean standardInput = false;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.equals("-h") || arg.equals("--help")) {
                printHelp();
                System.exit(0);
            } else if (arg.equals("-v") || arg.equals("--version")) {
                Traces.println(st_version_line, Traces.NONE);
                System.exit(0);
            } else if (arg.startsWith("-I")) {
                if (arg.equals("-I")) {
                    i++;
                    if (i >= args.length) {
                        throw new Exception("Bad parameter usage: " + arg);
                    }
                    arg = args[i];
                } else {
                    arg = arg.substring(2);
                }
                int k = 0;
                do {
                    int j = arg.indexOf(File.pathSeparatorChar, k);
                    if (j == -1) {
                        String s1 = arg.substring(k);
                        if (s1.length() > 0) {
                            Vector searchPath = CompilerConf.getSearchPath();
                            searchPath.insertElementAt(s1, 0);
                            CompilerConf.setSearchPath(searchPath);
                        }
                        break;
                    }
                    String s2 = arg.substring(k, j);
                    Vector searchPath = CompilerConf.getSearchPath();
                    searchPath.insertElementAt(s2, 0);
                    CompilerConf.setSearchPath(searchPath);
                    k = j + 1;
                } while (true);
            } else if (arg.startsWith("-D")) {
                if (arg.equals("-D")) {
                    i++;
                    if (i >= args.length)
                        throw new Exception("Bad parameter usage: " + arg);
                    arg = args[i];
                } else {
                    arg = arg.substring(2);
                }
                int l = arg.indexOf(61);
                String s3;
                String s4;
                if (l == -1) {
                    s3 = arg;
                    s4 = "";
                } else {
                    s3 = arg.substring(0, l);
                    s4 = arg.substring(l + 1);
                }
                if (!Preprocessor.getInstance().isSymbolName(s3)) {
                    throw new Exception("Bad parameter usage: -D\nUnexpected token ["
                                        + s3
                                        + "] encountered while searching for identifier");
                }
                String s5 = s4.trim();
                Preprocessor.getInstance().define(s3, s5);
            } else if (arg.startsWith("-U")) {
                if (arg.equals("-U")) {
                    i++;
                    if (i >= args.length)
                        throw new Exception("Bad parameter usage: " + arg);
                    arg = args[i];
                } else {
                    arg = arg.substring(2);
                }
                if (!Preprocessor.getInstance().isSymbolName(arg)) {
                    throw new Exception("Bad parameter usage: -U\nUnexpected token ["
                                        + arg
                                        + "] encountered while searching for identifier");
                }
                String s = arg;
                Preprocessor.getInstance().undefine(s);
            } else if (arg.equals("-E")) {
                CompilerConf.setJustExpand(true);
            } else if (arg.equals("-no_tie")) {
                CompilerConf.setNoTie(true);
            } else if (arg.equals("-no_stub")) {
                CompilerConf.setNoStub(true);
            } else if (arg.equals("-no_skel")) {
                CompilerConf.setNoSkel(true);
            } else if (arg.equals("-generate_code")) {
                i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                
                if (args[i].equals("FILE")) {
                	CompilerConf.setNotGenerateCode(true);
                }
                else if (args[i].equals("ALL")) {
                	CompilerConf.setNotGenerateCode(false);
                }
                else {
                	// Si es distinto de ambos, de trata por omisión como FILE
                	CompilerConf.setNotGenerateCode(true);
                	i--;
                } 
                
            } else if (arg.equals("-output")) {
                i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                CompilerConf.setOutputPath(args[i]);
            } else if (arg.equals("-output_h")) {
                i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                CompilerConf.setOutputHeaderDir(args[i]);
                /*
                 * }else if (arg.equals("-output_C")) { i++; if (i>=args.length)
                 * throw new Exception("Bad parameter usage: " + arg);
                 * CompilerConf.setOutputSourceDir(args[i]);
                 */
            } else if (arg.equals("-package")) {
                i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                String name = args[i];
                if (CompilerConf.getCompilerType().equals("Cpp")) {
                    if (name.indexOf('.') >= 0) {
                        throw new Exception("Bad parameter usage: -package\nUse '::' as separator for C++ modules with "
                                            + arg);
                    }
                    if (name.startsWith("::")) {
                        name = name.substring(2);
                    }
                }
                CompilerConf.setPackageUsed(name);
            } else if (arg.equals("-package_to")) {
                i += 2;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                // Elements are inserted in inverse order
                String theModule = args[i - 1];
                String thePackage = args[i];

                if (CompilerConf.getCompilerType().equals("Cpp")) {
                    if (thePackage.indexOf('.') >= 0) {
                        throw new Exception("Bad parameter usage: -package_to\nUse '::' as separator for C++ modules");
                    }
                    if (thePackage.startsWith("::")) {
                        thePackage = thePackage.substring(2);
                    }
                    if (theModule.startsWith("::")) {
                        theModule = theModule.substring(2);
                    }
                }

                if (theModule.endsWith(".idl")) {
                    CompilerConf.getFilePackaged().insertElementAt(theModule, 0);
                    //CompilerConf.getPackage_For_File_To().insertElementAt(thePackage,0);
                } else {
                    CompilerConf.getModule_Packaged().insertElementAt(theModule, 0);
                    //CompilerConf.getPackageTo().insertElementAt(thePackage,0);
                }
                CompilerConf.getPackageToTable().put(theModule, thePackage);

            } else if (arg.equals("-package_to_on_error")) {
                i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                if (args[i].equals("STOP")) {
                    CompilerConf.setPackageToError("STOP");
                } else if (args[i].equals("WARNING")) {
                    CompilerConf.setPackageToError("WARNING");
                } else if (args[i].equals("CONTINUE")) {
                    CompilerConf.setPackageToError("CONTINUE");
                } else {
                    throw new Exception("Bad parameter usage: " + arg);
                }
            } else if (arg.equals("-portable")) {
                CompilerConf.setPortable(true);
            } else if (arg.startsWith("-verbose")) {
                String level = "";
                if (!arg.equals("-verbose") && arg.indexOf("=") == 8) {
                    level = arg.substring(9, arg.length());
                }
                if (level.equals("NONE")) {
                    Traces.setLevel(Traces.NONE);
                } else if (level.equals("USER") || arg.equals("-verbose")) {
                    Traces.setLevel(Traces.USER);
                } else if (level.equals("FLOW")) {
                    Traces.setLevel(Traces.FLOW);
                } else if (level.equals("DEBUG")) {
                    Traces.setLevel(Traces.DEBUG);
                } else if (level.equals("DEEP_DEBUG")) {
                    Traces.setLevel(Traces.DEEP_DEBUG);
                } else
                    throw new Exception("Bad parameter usage: " + arg);
            } else if (arg.equals("-CORBA_IDL")) {
                CompilerConf.setCORBA_IDL(true);
            } else if (arg.equals("-minimun")) {
                CompilerConf.setMinimun(true);
            } else if (arg.equals("-expanded")) {
            	CompilerConf.setExpanded(true);
            } else if (arg.equals("-h_ext")) {
            	i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                CompilerConf.setHeaderExtension(args[i]);
            } else if (arg.equals("-c_ext")) {
            	i++;
                if (i >= args.length) {
                    throw new Exception("Bad parameter usage: " + arg);
                }
                CompilerConf.setSourceExtension(args[i]);
            } else if (arg.equals("-async")) {
            	Traces.println("Using asynchronous invocation ...\n", Traces.USER);
                CompilerConf.setAsynchronous(true);
            } else if (arg.equals("-non_copying_operators")) {
            	Traces.println("Using non copying operators\n", Traces.USER);
                CompilerConf.setNonCopyingOperators(true);
            } else if (arg.equals("-no_enum_check")) {
            	Traces.println("-no_enum_check\n", Traces.USER);
                CompilerConf.setEnumCheck(false);
            } else {
                if (i == args.length - 1
                    && (arg.equals("-") || !arg.startsWith("-"))) {
                    if (arg.equals("-")) {
                        standardInput = true;
                        Traces.println("Reading from standard input...",
                                       Traces.USER);
                    } else {
                    	String[] fileName;
                    	fileName = arg.split("[\\\\|/]"); 
                    	CompilerConf.setFileName(fileName[fileName.length-1].toString());
                        CompilerConf.setFile(arg);
                        Traces.println("Reading from file "
                                       + CompilerConf.getFile() + "...",
                                       Traces.USER);
                    }
                } else {
                    throw new Exception("Invalid parameter: " + arg);
                }
            }
        }
        if (!standardInput && CompilerConf.getFile() == null) {
            printMiniHelp();
            System.exit(0);
        }
    }

    /**
     * This method prints the list of parameters of the IDL compiler.
     */
    private static void printHelp()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(st_version_line);
        buf.append("Usage: idl2(cpp|java) [options] file\n");
        buf.append(" -h, --help                 show this help\n");
        buf.append(" -v, --version              show compiler version\n");
        buf.append(" -I[ ]dir                   preprocessor: add <dir> to include search path\n");
        buf.append(" -D[ ]name[=value]          preprocessor: define a macro\n");
        buf.append(" -U[ ]name                  preprocessor: undefine a macro\n");
        buf.append(" -E                         preprocessor: print result on stdout and finish\n");
        buf.append(" -portable                  generate portable helpers (idl2java only)\n");
        buf.append(" -async                     generate asynchronous methods (idl2java only)\n");
        buf.append(" -non_copying_operators     internal use of non-copying any insert/extract\n");
        buf.append("                            operators from C++ stub and skeletons(idl2cpp only)\n");
        buf.append(" -no_tie                    suppress generation of tie code\n");
        buf.append(" -no_stub                   suppress generation of stub code\n");
        buf.append(" -no_skel                   suppress generation of skeleton code\n");
        buf.append(" -no_enum_check             suppress enumerate value checking (idl2cpp only)\n");
        buf.append(" -output dir                write generated source files to <dir>\n");
        buf.append(" -output_h dir              write generated C++ headers to <dir> (idl2cpp only)\n");
        buf.append(" -h_ext suffix              use <suffix> for C++ header files    (idl2cpp only)\n");
        buf.append(" -c_ext suffix              use <suffix> for C++ source files    (idl2cpp only)\n");
        buf.append(" -expanded                  generate C++ sources for each class  (idl2cpp only)\n");
        buf.append(" -package pkg               define generated classes in package/namespace <pkg>\n");
        buf.append(" -package_to name pkg       define generated classes for <name> in <pkg>\n");
        buf.append("                            (where <name> must be an IDL module or an IDL file)\n");
        buf.append(" -package_to_on_error mode  action if -package_to fails: CONTINUE, WARNING, STOP\n");
        buf.append(" -verbose[=level]           compiler verbosity: USER, FLOW, DEBUG, DEEP_DEBUG\n");
        buf.append(" -CORBA_IDL                 recognize #include <CORBA.idl> as #include <orb.idl>\n");
        buf.append(" -minimun                   enables Minimun CORBA\n");
        /*
         * buf.append(" -output_C <dir> Directory where the source code files
         * will\n"); buf.append(" be generated, only (only C++)\n");
         */
        Traces.println(buf, Traces.NONE);
    }

    /**
     * This method prints and advice when no source file is given.
     */
    private static void printMiniHelp()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(st_version_line);
        buf.append("No source file specified. Type -h for help");
        Traces.println(buf, Traces.NONE);
    }
}
