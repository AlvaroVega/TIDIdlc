/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 325 $
* Date: $Date: 2010-01-18 12:45:27 +0100 (Mon, 18 Jan 2010) $
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

package es.tid.TIDIdlc.util;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;
import java.util.Collections;

import es.tid.TIDIdlc.CompilerConf;

public class FileManager {

	public static int TYPE_MAIN_HEADER 		= 0;
	public static int TYPE_MAIN_HEADER_EXT 	= 1;
	public static int TYPE_MAIN_SOURCE 		= 2;
	public static int TYPE_POA_HEADER 		= 3;
	public static int TYPE_POA_SOURCE 		= 4;
	public static int TYPE_OBV_HEADER 		= 5;
	public static int TYPE_OBV_SOURCE 		= 6;
	
	public static int WRITE_EXPANDED		= 0;
	public static int WRITE_CONDENSED		= 1;
	
	/**
     * The unique instance of the singleton class.
     */
	private static FileManager st_my_instance = null;

	/**
     * The base output dir where files will be generated.
     */
	private String m_output_dir;

	/**
     * The base output dir where header files will be generated.
     */
	private String m_header_dir;

	/**
	 * Every piece of code that has been generated.
	 */
	private Hashtable m_snippets;

	/**
	 * Every file that has been parsed.
	 */
	private ArrayList m_files;

	/**
	 * The name of the idl file without extension.
	 * It will be used only if -expanded isn't checked.
	 */
	private String m_file_name;
	
	/**
	 * The name of the original idl file without extension.
	 * It will be used only if -expanded isn't checked.
	 */
	private String m_original_file_name;

	/**
	 * The extension for headers.
	 * It will be used only if -expanded isn't checked.
	 */
	private String m_header_ext;

	/**
	 * The extension for sources.
	 * It will be used only if -expanded isn't checked.
	 */
	private String m_source_ext;
	
	/**
	 * List of header files included.
	 */
	private ArrayList m_header_list;

	/**
	 * List of header files included in source.
	 */
	private ArrayList m_source_list;
	
	/**
	 * List of includes in idl.
	 */
	private ArrayList m_includes_list = null;
	
	/**
	 * List of includes in idl.
	 */
	private ArrayList m_written_list = null;

	/**
	 * List of files.
	 */
	private ArrayList m_files_list = null;

	/**
	 * List of const outside a module.
	 */
	private ArrayList m_const_list = null; 
	
	/**
	 *  Is an include?
	 */
	private boolean m_isInclude = false;

	/**
	 * Method for getting the unique instance of this class. Singleton.
	 * @return The unique instance of this class.
	 */
	public static FileManager getInstance() {
		if (st_my_instance == null) {
			st_my_instance = new FileManager();
		}
		return st_my_instance;
	}
	
	/**
	 * Sets the base output dir where files will be generated.
	 * @param output_dir The base output dir where files will be generated.
	 */
	public void setOutputDir(String output_dir) {
		if(output_dir.lastIndexOf(File.separatorChar)==output_dir.length()-1)
			this.m_output_dir = output_dir;
		else
			this.m_output_dir = output_dir+File.separatorChar;
	}

	/**
	 * Sets the base output dir where header files will be generated.
	 * @param output_dir The base output dir where header files will be generated.
	 */
	public void setHeaderDir(String output_dir) {
		if((output_dir.lastIndexOf(File.separatorChar))==output_dir.length()-1)
			this.m_header_dir = output_dir;
		else
			this.m_header_dir = output_dir + File.separatorChar;
	}

	/**
	 * Sets the base name of the condensed files.
	 * @param file_name The base base name of the condensed files..
	 */
	public void setFileName(String file_name) {
		this.m_file_name = file_name;
	}

	/**
	 * Sets the base name of the condensed files.
	 * @param file_name The base base name of the condensed files..
	 */
	public void setOriginalFileName(String file_name) {
		this.m_file_name = file_name;
	}

	/**
	 * Sets the extension for headers.
	 * @param extension The extension for headers.
	 */
	public void setHeaderExtension(String extension) {
		this.m_header_ext = extension;
	}

	/**
	 * Sets the extension for sources.
	 * @param extension The extension for sources.
	 */
	public void setSourceExtension(String extension) {
		this.m_source_ext = extension;
	}

	/**
	 * Sets the value of the isInclude member.
	 * @param value.
	 */
	public void setIsInclude(boolean value) {
		this.m_isInclude = value;
	}

	/**
	 * Adds the code of a file in expanded mode.
	 * @param buffer Source code to include.
	 * @param file_name Name of the file in expanded mode.
	 * @param rel_path Relative path in expanded mode.
	 * @param idl_file_name Name of the idl file.
	 * @param type Type of the file. Its posible values are FileManager.TYPE_XXX_XXX
	 */
	public void addFile (StringBuffer buffer,
						 String file_name,
						 String path,
						 String idl_file_name,
						 int type) {
		if (file_name != "" && path != "") {
			SnippetFile snippet = new SnippetFile(file_name, path, idl_file_name, type);
			snippet.addCode(buffer);
			// Fix to bug # 299
			// If already is in m_snippets means that module definition is in several IDL files.
			// To fix compilation error about redefinition of definitions of IDL, here
			// delete all previus snippets except global_definitions       
			if (this.m_snippets.containsKey(path+File.separatorChar+file_name)){
				Enumeration keys_list = this.m_snippets.keys();
				SnippetFile sf = null;
				while (keys_list.hasMoreElements()) {
					Object key = keys_list.nextElement();
					sf = (SnippetFile)this.m_snippets.get(key);
					if (sf != null) {
						if (sf.getType() > 0) {
							this.m_snippets.remove(key);							
						}
					}					
				}
			}
			this.m_snippets.put(path+File.separatorChar+file_name,snippet);
		}
		
	}		
	
	

	/**
	 * Add include from idl to the list
	 * @param inc
	 */
	//public void addInclude(String inc) {
	//	if(this.m_includes_list==null)
	//		this.m_includes_list = new ArrayList();
	//	this.m_includes_list.add(inc);
	//}
	
	/**
	 * Prints the source code in the format specified
	 * @param write_type Format of the output files, expanded or condensed.
	 * @throws IOException
	 */
	public void write (int write_type) throws IOException {
		
		if (write_type == WRITE_CONDENSED) {
			writeCondensed();			
		} 
		else if (write_type == WRITE_EXPANDED) {
			writeExpanded();
		}
		
		
	}
	
	/**
	 * Prints the source code using the condensed way.
	 * @throws IOException
	 */
	private void writeCondensed() throws IOException{
		
		// Create main source file
		
		// Make target directory
        File targetDir = new File(this.m_output_dir);
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        
        // Make target directory
        File targetHDir = new File(this.m_header_dir);
        if (!targetHDir.exists()) {
            targetHDir.mkdirs();
        }
		
        // Main generation
        generateFiles();
        
        /*if(list_includes != null) {
	        while(list_includes.size()>0) {
	        	String name = (String) list_includes.get(list_includes.size()-1);
	        	String[] file_name = name.split("\\."); 
	        	this.setFileName(file_name[0]);
	        	list_includes.remove(list_includes.size()-1);
	        	this.setIsInclude(true);
	        	if (this.m_header_list.contains(this.m_header_dir+"PortableServer.h"))
	        		this.m_header_list.remove(this.m_header_dir+"PortableServer.h");
	        	writeCondensed(list_includes);
	        }
        }*/
	}

	private void generateFiles() throws IOException {

		// Main generation
		this.m_header_list = new ArrayList();
		this.m_written_list = new ArrayList();
		generateMainFiles(this.m_file_name);
      
		// POA generation
		generatePOAFiles(this.m_file_name);

		// OBV generation
		generateOBVFiles(this.m_file_name);      
		
	}
	
	private boolean thereIsOBV(String file_name) {
		boolean thereIs = false;
		Set key_set = this.m_snippets.keySet();
		Iterator iterator = key_set.iterator();
		String key = "";
		while(iterator.hasNext()) {
			key = (String)iterator.next();
			if (key.indexOf("OBV_"+file_name)>0) {
				thereIs = true;
				break;
			} 
		}
		
		return thereIs;
	}

	private boolean thereIsPOA(String file_name) {
		boolean thereIs = false;
		Set key_set = this.m_snippets.keySet();
		Iterator iterator = key_set.iterator();
		String key = "";
		while(iterator.hasNext()) {
			key = (String)iterator.next();
			if (key.indexOf("POA_"+file_name)>0) {
				thereIs = true;
				break;
			} 
		}
		
		return thereIs;
	}
	
	private void generateMainFiles(String file_name) throws IOException {
		
		IncludeFileManager ifm = IncludeFileManager.getInstance();
		ArrayList list_includes = ifm.getIncludesFromIdlFile(file_name+".idl");
		ArrayList list_modules = ifm.getModulesFromIdlFile(file_name+".idl");
		boolean thereIsOBV = false;
		boolean thereIsPOA = false;

		// Main generation
		// Cpp source generation
        StringBuffer sourceBuffer = new StringBuffer();
        sourceBuffer = writeCondensedCHeader("", file_name);
        sourceBuffer.append("#include \"" + file_name + this.m_header_ext + "\"\n");
        
        if (list_modules.size()>0) {
        	for(int i=0; i<list_modules.size(); i++){
        		String mod = (String)list_modules.get(i)+this.m_header_ext;

        		if (thereIsOBV(mod)) {
        			if(!thereIsOBV)
        				sourceBuffer.append("#include \"OBV_" + file_name + this.m_header_ext + "\"\n");
                	thereIsOBV = true;
                } else {
                	sourceBuffer.append("\n");
                }
        		
                if (thereIsPOA(mod)) {
                	if(!thereIsPOA)
                		sourceBuffer.append("#include \"POA_" + file_name + this.m_header_ext + "\"\n");
                	thereIsPOA = true;
                } else {
                	sourceBuffer.append("\n");
                }
        	}
        }

        if (list_modules.size()>0) {
        	for(int i=0; i<list_modules.size(); i++){
        		String mod = (String)list_modules.get(i)+this.m_source_ext;
        		sourceBuffer.append(getSourceCode(file_name, mod));
        	}
        }

        // Header generation
        StringBuffer headerBuffer = new StringBuffer();
        
        headerBuffer = writeCondensedHHeader("", file_name);
        
        
        
        if (list_includes.size()>0) {
        	for(int i=0; i<list_includes.size(); i++){
        		String inc = (String)list_includes.get(i);
        		inc = inc.substring(0,inc.lastIndexOf('.'))+this.m_header_ext;
            	headerBuffer.append("#include \""+inc+"\"\n\n");
        	}
        }
        
        headerBuffer.append(getHeaderCode(file_name, list_modules));
        
        appendHFoot(headerBuffer, "", file_name);
        
        if(!this.m_written_list.contains(this.m_output_dir+file_name+this.m_source_ext)&&
        		!file_name.equals("orb")&&
        		!file_name.equals("PortableServer")){
        	writeFile(file_name, this.m_output_dir, sourceBuffer, this.m_source_ext, "");
        	this.m_written_list.add(this.m_output_dir+file_name+this.m_source_ext);
        }
        if(!this.m_written_list.contains(this.m_header_dir+file_name+this.m_header_ext)&&
        		!file_name.equals("orb")&&
        		!file_name.equals("PortableServer")){
        	writeFile(file_name, this.m_header_dir, headerBuffer, this.m_header_ext, "");
        	this.m_written_list.add(this.m_header_dir+file_name+this.m_header_ext);
        }

        /* Do not generate code for included types
        if (list_includes.size()>0) {
        	for(int i=0; i<list_includes.size(); i++){
        		String inc = (String)list_includes.get(i);
        		setIsInclude(true);
            	generateMainFiles(inc.substring(0,inc.lastIndexOf('.')));
        	}
        } 
        */
	}

	private void generatePOAFiles(String file_name) throws IOException {

		boolean write = false;
		IncludeFileManager ifm = IncludeFileManager.getInstance();
		ArrayList list_includes = ifm.getIncludesFromIdlFile(file_name+".idl");
		ArrayList list_modules = ifm.getModulesFromIdlFile(file_name+".idl");
		boolean thereIsOBV = false;
		boolean thereIsPOA = false;

		// POA generation
		// Cpp source generation
        StringBuffer sourceBuffer = new StringBuffer();
        StringBuffer sourceCode = new StringBuffer();
        sourceBuffer = writeCondensedCHeader("POA_", file_name);
        
        
        if (list_modules.size()>0) {
        	for(int i=0; i<list_modules.size(); i++){
        		String mod = (String)list_modules.get(i)+this.m_header_ext;

        		if (thereIsOBV(mod)) {
        			if(!thereIsOBV)
        				sourceBuffer.append("#include \"OBV_" + file_name + this.m_header_ext + "\"\n");
                	thereIsOBV = true;
                } else {
                	sourceBuffer.append("\n");
                }
        		
                if (thereIsPOA(mod)) {
                	if(!thereIsPOA)
                		sourceBuffer.append("#include \"POA_" + file_name + this.m_header_ext + "\"\n");
                	thereIsPOA = true;
                } else {
                	sourceBuffer.append("\n");
                }
        	}
        }

        if (list_modules.size()>0) {
        	for(int i=0; i<list_modules.size(); i++){
        		String mod = (String)list_modules.get(i)+this.m_source_ext;
        		sourceCode.append(getPOASourceCode(mod));
        	}
        }

        // Header generation
        StringBuffer headerBuffer = new StringBuffer();
        
        StringBuffer headerCode = new StringBuffer();
        
        headerBuffer = writeCondensedHHeader("POA_", file_name);
        
        headerBuffer.append("#include \"PortableServer.h\"\n");
        if (list_includes.size()>0) {
        	for(int i=0; i<list_includes.size(); i++){
        		String inc = (String)list_includes.get(i);
        		ArrayList list = ifm.getModulesFromIdlFile(inc);
        		inc = inc.substring(0,inc.lastIndexOf('.'))+this.m_header_ext;
        		for(int j=0; j<list.size(); j++) {
        			String mod = (String)list.get(j)+this.m_header_ext;
        			if(thereIsPOA(mod)){
        				headerBuffer.append("#include \"POA_"+inc+"\"\n\n");
        			}
        		}
        	}
        }

        headerCode.append(getPOAHeaderCode(file_name, list_modules));
        
       

        if (sourceCode.length() > 0) {
        	sourceBuffer.append(sourceCode);
            if(!this.m_written_list.contains(this.m_output_dir+"POA_"+file_name+this.m_source_ext)&&
            		!file_name.equals("orb")&&
            		!file_name.equals("PortableServer")){
            	writeFile(file_name, this.m_output_dir, sourceBuffer, this.m_source_ext, "POA_");
            	this.m_written_list.add(this.m_output_dir+"POA_"+file_name+this.m_source_ext);
            	write = true;
            }
        }
  
        if (headerCode.length() > 0 || write) {
            
        	headerBuffer.append(headerCode);
        	appendHFoot(headerBuffer, "POA_", file_name);
        	
            if(!this.m_written_list.contains(this.m_header_dir+"POA_"+file_name+this.m_header_ext)&&
            		!file_name.equals("orb")&&
            		!file_name.equals("PortableServer")){
            	writeFile(file_name, this.m_header_dir, headerBuffer, this.m_header_ext, "POA_");
            	this.m_written_list.add(this.m_output_dir+"POA_"+file_name+this.m_header_ext);
            }
        }

        /* Do not generate code for included files
        if (list_includes.size()>0) {
        	for(int i=0; i<list_includes.size(); i++){
        		String inc = (String)list_includes.get(i);
            	generatePOAFiles(inc.substring(0,inc.lastIndexOf('.')));
        	}
        } 
        */
	
	}
	
	private void generateOBVFiles(String file_name) throws IOException {

		boolean write = false;
		IncludeFileManager ifm = IncludeFileManager.getInstance();
		ArrayList list_includes = ifm.getIncludesFromIdlFile(file_name+".idl");
		ArrayList list_modules = ifm.getModulesFromIdlFile(file_name+".idl");
		boolean thereIsOBV = false;
		boolean thereIsPOA = false;

		// OBV generation
		// Cpp source generation
        StringBuffer sourceBuffer = new StringBuffer();
        StringBuffer sourceCode = new StringBuffer();
        sourceBuffer = writeCondensedCHeader("OBV_", file_name);
        
        if (list_modules.size()>0) {
        	for(int i=0; i<list_modules.size(); i++){
        		String mod = (String)list_modules.get(i)+this.m_header_ext;

        		if (thereIsOBV(mod)) {
        			if(!thereIsOBV)
        				sourceBuffer.append("#include \"OBV_" + file_name + this.m_header_ext + "\"\n");
                	thereIsOBV = true;
                } else {
                	sourceBuffer.append("\n");
                }
        		
                if (thereIsPOA(mod)) {
                	if(!thereIsPOA)
                		sourceBuffer.append("#include \"POA_" + file_name + this.m_header_ext + "\"\n");
                	thereIsPOA = true;
                } else {
                	sourceBuffer.append("\n");
                }
        	}
        }

        if (list_modules.size()>0) {
        	for(int i=0; i<list_modules.size(); i++){
        		String mod = (String)list_modules.get(i)+this.m_source_ext;
        		sourceCode.append(getOBVSourceCode(mod));
        	}
        }

        // Header generation
        StringBuffer headerBuffer = new StringBuffer();
        StringBuffer headerCode = new StringBuffer();
        headerBuffer = writeCondensedHHeader("OBV_", file_name);
        if (list_includes.size()>0) {
        	for(int i=0; i<list_includes.size(); i++){
        		String inc = (String)list_includes.get(i);
        		ArrayList list = ifm.getModulesFromIdlFile(inc);
        		inc = inc.substring(0,inc.lastIndexOf('.'))+this.m_header_ext;
        		for(int j=0; j<list.size(); j++) {
        			String mod = (String)list.get(j)+this.m_header_ext;
        			if(thereIsOBV(mod)){
        				headerBuffer.append("#include \"OBV_"+inc+"\"\n\n");
        			}
        		}
        	}
        }

        headerCode.append(getOBVHeaderCode(file_name, list_modules));
        
        

        if (sourceCode.length() > 0) {
        	sourceBuffer.append(sourceCode);
            if(!this.m_written_list.contains(this.m_output_dir+"OBV_"+file_name+this.m_source_ext) &&
            		!file_name.equals("orb")&&
            		!file_name.equals("PortableServer")){
            	writeFile(file_name, this.m_output_dir, sourceBuffer, this.m_source_ext, "OBV_");
            	this.m_written_list.add(this.m_output_dir+"OBV_"+file_name+this.m_source_ext);
            	write = true;
            }
        }
  
        if (headerCode.length() > 0 || write) {
        	headerBuffer.append(headerCode);
        	appendHFoot(headerBuffer, "OBV_", file_name);
            if(!this.m_written_list.contains(this.m_header_dir+"OBV_"+file_name+this.m_header_ext)&&
            		!file_name.equals("orb")&&
            		!file_name.equals("PortableServer")){
            	writeFile(file_name, this.m_header_dir, headerBuffer, this.m_header_ext, "OBV_");
            	this.m_written_list.add(this.m_output_dir+"OBV_"+file_name+this.m_header_ext);
            }
        }

        /* Do not generate code for included types
        if (list_includes.size()>0) {
        	for(int i=0; i<list_includes.size(); i++){
        		String inc = (String)list_includes.get(i);
        		setIsInclude(true);
            	generateOBVFiles(inc.substring(0,inc.lastIndexOf('.')));
        	}
        } 
        */
	}
	
	/**
	 * Gathers the source code from every code snippet.
	 * @return buffer The buffer with all the code.
	 * @throws IOException
	 */
	private StringBuffer getHeaderCode(String file_name, ArrayList list_modules) throws IOException {
		
		StringBuffer buffer = new StringBuffer();
		StringBuffer code = new StringBuffer();
		String ext = this.m_header_ext.substring(1);
		this.m_files_list = getIncludesList(this.m_header_dir +
												"_global_includes_for_" +
												file_name +
												"_idl." +
												ext);
		if(this.m_files_list.size()>0){
			this.m_header_list.add(this.m_header_dir +
								   "_global_includes_for_" +
								   file_name +
								   "_idl." +
								   ext);
		}
		this.m_header_list.add("TIDorb/portable.h");
		this.m_header_list.add("TIDorb/types.h");
		
		// Main Header
		if (this.m_file_name.toUpperCase().equals("CORBA")) {
			code.append(expandInclude(this.m_header_dir + "CORBA" + this.m_header_ext));
		}
		
		if(list_modules==null) {
			code.append(expandInclude(this.m_header_dir+file_name));			
		} else {
			for(int i=0; i<list_modules.size();i++) {
				String mod = (String)list_modules.get(i);
				code.append(expandInclude(this.m_header_dir+mod+this.m_header_ext));
			}
		}
		if(m_files_list.size()==0) {
			code.append(expandInclude(this.m_header_dir +
					"_global_includes_for_" +
					file_name +
					"_idl." +
					ext));
		}
		//
		// Fix bug #432: IDL constants declared outside of a module are not translated into C++ code
		//
		this.m_const_list = getIncludesConstantsList(this.m_header_dir +
                                                             "_global_includes_for_" +
                                                             file_name +
                                               "_idl." +
                                                             ext);

		for(int i = 0; i < this.m_const_list.size(); i++) {
                    String fn_h = (String)this.m_const_list.get(i);
                    String fn = fn_h.substring(0, fn_h.lastIndexOf('.'));
                    String fn_idl = fn + ".idl";
                    
                    // Takes includes not defined in the module 'file_name', like global constants
                    if(!(fn.equals(file_name)) && (this.m_file_name.equals(file_name)) ) {
                        code.append("\n#ifndef _" + fn + "_H_");
                        code.append("\n#define _" + fn + "_H_\n");
                        code.append(expandInclude(this.m_header_dir + fn + this.m_header_ext));
                        code.append("\n#endif // _" + fn + "_H_\n\n");
                    }
		}

		
		if (code.length()!=0) {			
			buffer.append("\n#include \"TIDorb/portable.h\"\n");
			buffer.append("\n#include \"TIDorb/types.h\"\n");
			
			if (!file_name.toUpperCase().equals("CORBA")) {
				buffer.append("\n#include \"CORBA" + this.m_header_ext + "\"\n");
				this.m_header_list.add("CORBA" + this.m_header_ext);
			}
			buffer.append(code);			
		}
		
		return buffer;
	}
	
	private void appendHFoot(StringBuffer buffer, String type, String file_name) {
	    String ext = this.m_header_ext.substring(1);
	    buffer.append("#endif // __" + type.toUpperCase() + file_name.toUpperCase() + "_" + ext.toUpperCase() +"_\n");
	}

	/**
	 * Gathers the source code from every code snippet.
	 * @return buffer The buffer with all the code.
	 * @throws IOException
	 */
	private StringBuffer getPOAHeaderCode(String file_name, ArrayList list_modules) throws IOException {
		
		StringBuffer buffer = new StringBuffer();
		StringBuffer code = new StringBuffer();
		String ext = this.m_header_ext.substring(1);
		
		if(list_modules==null) {
			code.append(expandInclude(this.m_header_dir+"POA_"+file_name));			
		} else {
			for(int i=0; i<list_modules.size();i++) {
				String mod = (String)list_modules.get(i);
				code.append(expandInclude(this.m_header_dir+"POA_"+mod+this.m_header_ext));
			}
		}

		if (code.length() !=0) {
			// Main Header
			
			buffer.append("\n#include \"" + file_name + this.m_header_ext + "\"\n");			
			buffer.append(code);			
		}
		return buffer;
	}

	/**
	 * Gathers the source code from every code snippet.
	 * @return buffer The buffer with all the code.
	 * @throws IOException
	 */
	private StringBuffer getOBVHeaderCode(String file_name, ArrayList list_modules) throws IOException {
		StringBuffer buffer = new StringBuffer();
		StringBuffer code = new StringBuffer ();
		String ext = this.m_header_ext.substring(1);

		if(list_modules==null) {
			code.append(expandInclude(this.m_header_dir+"OBV_"+file_name));			
		} else {
			for(int i=0; i<list_modules.size();i++) {
				String mod = (String)list_modules.get(i);
				code.append(expandInclude(this.m_header_dir+"OBV_"+mod+this.m_header_ext));
			}
		}
		
		
//		String original_file = file_name + ".idl";
//		String obv_file = "OBV_" + file_name + this.m_header_ext;
//		
//		if(file_name.toUpperCase().equals("CORBA")) {
//			code.append(expandInclude(this.m_header_dir + "OBV_CORBA" + this.m_header_ext));
//		}
//		
//		for(int i = 0; i < this.m_files_list.size(); i++) {
//			String fn = (String)this.m_files_list.get(i);
//			String fn_idl = fn.substring(0, fn.lastIndexOf('.')) + ".idl";
//			if((fn_idl.equals(original_file))) {
//				code.append(expandInclude(this.m_header_dir + obv_file));
//			}
//		}
		if (code.length() !=0) {
		    
			// Main Header
			buffer.append("\n#include \"" + file_name + this.m_header_ext + "\"\n");
			
			buffer.append(code);			
		}
		return buffer;
	}
	
	/**
	 * Gets a list of includes
	 * @param target Target file whose includes are extracted
	 * @return includes ArrayList of includes.
	 * @throws IOException
	 */
	private ArrayList getIncludesList(String target) throws IOException {
		SnippetFile sf = (SnippetFile)this.m_snippets.get(target);
		ArrayList includes = new ArrayList();
		
		if(sf != null) {
			StringReader reader = new StringReader(sf.getCode());
			BufferedReader buf_reader = new BufferedReader(reader);
			String line = "";
			boolean resolveIncludes = false;
			int c;
			
			while((line=buf_reader.readLine())!=null) {
				if(line.indexOf("// Begin of children")!=-1) {
					resolveIncludes = true;
				}
				if((c=line.indexOf("#include"))!=-1 && resolveIncludes) {
					includes.add(line.substring(c+10,line.length()-2));
				}
			}
			
			buf_reader.close();
		}
			
		return includes;
	}

	/**
	 * Gets a list of includes
	 * @param target Target file whose includes are extracted
	 * @return includes ArrayList of includes.
	 * @throws IOException
	 */
	private ArrayList getIncludesConstantsList(String target) throws IOException {
		SnippetFile sf = (SnippetFile)this.m_snippets.get(target);
		ArrayList includes = new ArrayList();
		
		if(sf != null) {
			StringReader reader = new StringReader(sf.getCode());
			BufferedReader buf_reader = new BufferedReader(reader);
			String line = "";
			boolean resolveIncludes = false;
			int c;
			int d;
			
			while((line=buf_reader.readLine())!=null) {
				if(line.indexOf("// Begin of children")!=-1) {
					resolveIncludes = true;
				}
				if((c=line.indexOf("#include"))!=-1 && resolveIncludes && 
                                   (d=line.indexOf("// global const"))!=-1) {
					includes.add(line.substring(c+10,line.length()-2));
				}
			}
			
			buf_reader.close();
		}
			
		return includes;
	}



	/**
	 * Returns the content of the spedified snippet, but having replaced the 
	 * includes with more snippets
	 * @param target The full path of the file to expand
	 * @return BufferString corresponding to the final source code
	 * @throws IOException
	 */
	private StringBuffer expandInclude(String target) throws IOException
	{
		StringBuffer buffer = new StringBuffer();
		
		
		SnippetFile sf = (SnippetFile)this.m_snippets.get(target);
		if (sf != null) {
			this.m_header_list.add(target);
			StringReader reader = new StringReader(sf.getCode());
			BufferedReader buf_reader = new BufferedReader(reader);
			String line = "";
			String rel_path;
			
			String abs_path = this.m_header_dir; // target.substring(0,target.lastIndexOf(File.separatorChar)+1);
			
			while((line=buf_reader.readLine())!=null) {
				rel_path = findInclude(line);
				if(rel_path == null) {
					if (!line.matches("\\s*#\\s*define .*") && 
						!line.matches("\\s*#\\s*ifndef .*") && 
						!line.matches("\\s*#\\s*endif .*")) {
					buffer.append(line+"\n");
					}
				} else
				{
					if(!this.m_header_list.contains(abs_path + rel_path) &&
					   !rel_path.equals("TIDorb/portable.h") &&
					   !rel_path.equals("TIDorb/types.h")    &&
					   !rel_path.equals("PortableServer.h")    &&
					   !rel_path.equals("CORBA.h")) //			 &&
					   //!isParentInclusion(line))
					{
						if (existsInclude(line)) 
							buffer.append(expandInclude(abs_path + rel_path));
						else {
                                                    //this.m_header_list.add(abs_path + rel_path);
                                                    //buffer.append(line +"\n");
                                                    // Expandir todos los .h
                                                    buffer.append(expandInclude(abs_path + rel_path));

						}
							
					}
				}
			}
		}		
		return buffer;
	}

	/**
	 * Checks if an especific file is in our files.
	 * @return true or false.
	 */
	private boolean existsInclude (String line) {
		boolean res = false;
		String path = findInclude(line);
		if (path != null) { // There is an inclusion
			SnippetFile sf = (SnippetFile)this.m_snippets.get(this.m_header_dir + path);
			if (sf != null)
				res = true;
		}
		return res;
	}

	/**
	 * Gets the path within the include.
	 * @return The path.
	 */
	private String findInclude(String line) {
		int c;
		String[] path = null;
		String res = null;
		
		if(line.matches("\\s*#include .*")) {
			path = line.split("\"");
			res = path[1];
			String[] idl = res.split("\\.");
			if (IsIDLInclude(idl[0])){
				res = null;
			}
			
		}
		return res;
	}
	
	private boolean IsIDLInclude(String inc) {
		boolean res = false;
		int i = 0;
		if(this.m_includes_list!=null) {
			while((i < this.m_includes_list.size()) && !res) {
				String[] idl = this.m_includes_list.get(i).toString().split("\\.");
				if(inc.equals(idl[0]) ||
				   inc.equals("POA_"+idl[0]) ||
				   inc.equals("OBV_" + idl[0])){
					res = true;
				}
				i++;
			}
		}
		
		return res;
	}
	/**
	 * Gathers the source code from every code snippet.
	 * @return buffer The buffer with all the code.
	 * @throws IOException
	 */
	private StringBuffer getSourceCode(String file_name, String module) throws IOException {
		
		StringBuffer buffer = new StringBuffer();
		this.m_source_list = new ArrayList();
		SnippetFile sf;
		
		// Enumeration my_enum = this.m_snippets.elements();
		
		// while (my_enum.hasMoreElements()) {
		// 	sf = (SnippetFile)my_enum.nextElement();

		// Get sorted keys to print the source code in the file always in the same order
		// (usefull to allow checksums management)
		Enumeration my_enum = this.m_snippets.keys();
                Vector my_vector = new Vector();
		while (my_enum.hasMoreElements()) {
                    my_vector.add(new StringKey(my_enum.nextElement()));
		}
                Collections.sort(my_vector);

		int i = 0;
                while (i < my_vector.size()) { 
                        String key = (String) ( (StringKey) my_vector.get(i++)).get();
			sf = (SnippetFile)this.m_snippets.get(key);
			if (sf.getType() == TYPE_MAIN_SOURCE) {
				String file_path = sf.getPath();
				String mod = module.substring(0,module.lastIndexOf('.'));
				String path = file_path.substring(this.m_output_dir.length(),file_path.length());
				if(path.indexOf(File.separatorChar)!=-1)
					path = path.substring(0,path.lastIndexOf(File.separatorChar));
				// Check if snippet was found in current IDL file
				if(path.equals(mod) && (sf.getIdlFileName().equals(file_name + ".idl")) ) {
					buffer.append(filterCode(sf.getCode()));
					buffer.append("\n\n");
				}
			}
		}	
		return buffer;
	}
	
	/**
	 * Gathers the source code from every code snippet.
	 * @return buffer The buffer with all the code.
	 * @throws IOException
	 */
	private StringBuffer getPOASourceCode(String module) throws IOException {
		
		StringBuffer buffer = new StringBuffer();
		this.m_source_list = new ArrayList();
		SnippetFile sf;
		
		// Enumeration my_enum = this.m_snippets.elements();

		// while (my_enum.hasMoreElements()) {
		// 	sf = (SnippetFile)my_enum.nextElement();
                
		// Get sorted keys to print the source code in the file always in the same order
		// (usefull to allow checksums management)
		Enumeration my_enum = this.m_snippets.keys();
                Vector my_vector = new Vector();
		while (my_enum.hasMoreElements()) {
                    my_vector.add(new StringKey(my_enum.nextElement()));
		}
                Collections.sort(my_vector);

		int i = 0;
		while (i < my_vector.size()) { 
                        String key = (String) ( (StringKey) my_vector.get(i++)).get(); 
			sf = (SnippetFile)this.m_snippets.get(key);
			if (sf.getType() == TYPE_POA_SOURCE) {
				String file_path = sf.getPath();
				String mod = module.substring(0,module.lastIndexOf('.'));
				String path = file_path.substring(this.m_output_dir.length(),file_path.length());                       
				if(path.indexOf(File.separatorChar)!=-1)
					path = path.substring(0,path.lastIndexOf(File.separatorChar));
				if(path.equals("POA_" + mod)) {
					buffer.append(filterCode(sf.getCode()));
					buffer.append("\n\n");
				}
			}
		}	
		return buffer;

		
	}

	/**
	 * Gathers the source code from every code snippet.
	 * @return buffer The buffer with all the code.
	 * @throws IOException
	 */
	private StringBuffer getOBVSourceCode(String module) throws IOException {
		
		StringBuffer buffer = new StringBuffer();
		this.m_source_list = new ArrayList();
		SnippetFile sf;
		
		// Enumeration my_enum = this.m_snippets.elements();

		// while (my_enum.hasMoreElements()) {
		//	sf = (SnippetFile)my_enum.nextElement();

		// Get sorted keys to print the source code in the file always in the same order
		// (usefull to allow checksums management)
		Enumeration my_enum = this.m_snippets.keys();
                Vector my_vector = new Vector();
		while (my_enum.hasMoreElements()) {
                    my_vector.add(new StringKey(my_enum.nextElement()));
		}
                Collections.sort(my_vector);

		int i = 0;
		while (i < my_vector.size()) { 
                        String key = (String) ( (StringKey) my_vector.get(i++)).get(); 
			sf = (SnippetFile)this.m_snippets.get(key);	
			if (sf.getType() == TYPE_OBV_SOURCE) {
				String file_path = sf.getPath();
				String mod = module.substring(0,module.lastIndexOf('.'));
				String path = file_path.substring(this.m_output_dir.length(),file_path.length());
				if(path.indexOf(File.separatorChar)!=-1)
					path = path.substring(0,path.lastIndexOf(File.separatorChar));
				if(path.equals("OBV_" + mod)) {
					buffer.append(filterCode(sf.getCode()));
					buffer.append("\n\n");
				}
			}
		}	
		return buffer;
	}

	/**
	 * Removes '#include "..."' of the source code. 
	 * @param code The code to filter
	 * @return filtered_code The filtered code.
	 * @throws IOException
	 */
	private StringBuffer filterCode(String code) throws IOException{
		StringBuffer filtered_code = new StringBuffer();
		StringReader reader = new StringReader(code);
		BufferedReader buf_reader = new BufferedReader(reader);
		String line = "";
		String path = "";

		while((line=buf_reader.readLine())!=null){
			if((path=findInclude(line)) != null) {
				if(!existsInclude(line)) {
					if(!this.m_source_list.contains(path)) {
						filtered_code.append(line+"\n");
						this.m_source_list.add(path);
					}
				}
			} else {
				filtered_code.append(line+"\n");
			}
		}
		
		buf_reader.close();
		return filtered_code;
	}
	
	/**
	 * Writes the file into disk.
	 * @param buffer
	 * @throws IOException
	 */
	private void writeFile(String file_name, String dir, StringBuffer buffer, String extension, String type) throws IOException{
		FileWriter writer = new FileWriter(dir + 
				   File.separatorChar + type +
				   file_name + 
				   extension);
		BufferedWriter buf_writer = new BufferedWriter(writer);
		buf_writer.write(buffer.toString());
		buf_writer.close();
		
	}
	
	/**
	 * Writes the header to a file.
	 * @return main_header The header of the file
	 */
	private StringBuffer writeCondensedHHeader(String type, String file_name) {
		
		StringBuffer main_header = new StringBuffer();
		
		String ext = this.m_header_ext.substring(1);
		
		main_header.append("// " + type + file_name + this.m_header_ext + "\n");
		main_header.append("//\n");
		main_header.append("// File generated: ");
        java.util.Date currentDate = new java.util.Date();
        main_header.append(currentDate);
        main_header.append("\n");
        main_header.append("//   by TIDIdlc idl2cpp " + CompilerConf.st_compiler_version +"\n");
        main_header.append("//\n\n");
        
        String def_name = type.toUpperCase() +
                          file_name.toUpperCase() +
                           "_" + ext.toUpperCase() +"_\n";
        
        main_header.append("#ifndef __" + def_name );
        main_header.append("#define __"  + def_name);
        
        return main_header;
	}

	/**
	 * Writes the header to a file.
	 * @return main_header The header of the file
	 */
	private StringBuffer writeCondensedCHeader(String type, String file_name) {
		
		StringBuffer main_header = new StringBuffer();
		
		main_header.append("// "+ type + file_name + this.m_source_ext + "\n");
		main_header.append("//\n");
		main_header.append("// File generated: ");
        java.util.Date currentDate = new java.util.Date();
        main_header.append(currentDate);
        main_header.append("\n");
        main_header.append("//   by TIDIdlc idl2cpp " + CompilerConf.st_compiler_version +"\n");
        main_header.append("//\n\n");
        //main_header.append("#include \"" + type + file_name + this.m_header_ext + "\"\n");
        
        return main_header;
	}

	
	/**
	 * Prints the source code using the expanded way.
	 * @throws IOException
	 */
	private void writeExpanded() throws IOException {
		
		String contents;
		File targetDir;
		FileWriter writer;
		BufferedWriter buf_writer;
		SnippetFile sf;
		Enumeration my_enum = this.m_snippets.elements();
		
		while (my_enum.hasMoreElements()) {
			sf = (SnippetFile)my_enum.nextElement();
			contents = sf.getCode();
			
			// Make target directory
            targetDir = new File(sf.getPath());
            if (!targetDir.exists()) {
                targetDir.mkdirs();
            }
            
			writer = new FileWriter(sf.getPath() + File.separatorChar + sf.getFileName());
	        buf_writer = new BufferedWriter(writer);
	        buf_writer.write(contents);
	        buf_writer.close();
		}	
	}
	

	/**
	 * Constructor of the class. Private due to the singleton pattern.
	 */
	private FileManager() {
		m_snippets = new Hashtable();
	}
}
