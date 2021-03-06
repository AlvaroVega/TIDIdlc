/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 22 $
* Date: $Date: 2005-05-05 13:10:09 +0200 (Thu, 05 May 2005) $
* Last modified by: $Author: aarranz $
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

/*
 * Created on 11-nov-2003
 *
 */

package es.tid.TIDIdlc.ant;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.BuildException;

import java.io.*;
import java.util.StringTokenizer;

import es.tid.TIDIdlc.*;

/**
 * @author davv
 *
 */

public class Idl2CppTask extends Task {

	public void execute() throws BuildException {
        String[] arguments = create_arguments();

        System.out.print("idl2cpp");
        System.out.print(" (num_arguments = " + arguments.length + ") ");
        for (int i = 0; i < arguments.length; i++)
            System.out.print(" " + arguments[i]);

        System.out.println();

        Idl2Cpp.main(arguments);
        
        clean();
	}

	/**
	 * Idl2Cpp source directory
	 */

	public void setSrcDir(String v) {
		if (v.length() > 0) {
			_srcdir = v;
		}
	}

	public String getSrcDir() {
		return _srcdir;
	}

    /**
      * Idl2Java source path
      */

    /*public void setSrcPath(String v) {
        if (v.length() > 0) {
            _srcpath = v;
        }
    }

    public String getSrcPath() {
        return _srcpath;
    }*/

	/**
	 * Idl2Cpp option -output dir
	 */

	public void setDestDir(String v) {
		if (v.length() > 0) {
			_output = v;
			_num_arguments += 2;
		}
	}

	public String getDestDir() {
		return _output;
	}

	/**
	 * Idl2Cpp option -output_h dir
	 */

	public void setHdrDestDir(String v) {
		if (v.length() > 0) {
			_output_h = v;
			_num_arguments += 2;
		}
	}

	public String getHdrDestDir() {
		return _output_h;
	}
	
	/**
	 * Idl2Cpp IDL source file
	 */
	public void setSource(String v) {
		if (v.length() > 0) {
			_source = v;
			_num_arguments++;
		}
	}

	public String getSource() {
		return _source;
	}

	/**
	 * Idl2Cpp option -I include_path
	 */

	public Include createInclude() {
		if (_includes == null)
			_includes = new java.util.Vector();

		Include new_include = new Include();

		_includes.add(new_include);

		_num_arguments += 2;

		return new_include;
	}

    /**
     * Idl2Cpp option -D name=value
     */

    public Define createDefine() {
        if (_defines == null)
            _defines = new java.util.Vector();

        Define new_define = new Define();

        _defines.add(new_define);

        _num_arguments += 2;

        return new_define;
    }

    /**
     * Idl2Cpp option -U name
     */

    public Undef createUndef() {
        if (_undefs == null)
            _undefs = new java.util.Vector();

        Undef new_undef = new Undef();

        _undefs.add(new_undef);

        _num_arguments += 2;

        return new_undef;
    }

    /**
     * Idl2Java option -packageTo module package
     */

	public PackageTo createPackageTo() {
		if (_package_to == null)
			_package_to = new java.util.Vector();

		PackageTo new_package = new PackageTo();

		_package_to.add(new_package);

		_num_arguments += 3;

		return new_package;
	}

	/**
	 * Idl2Cpp option -package_to_on_error [STOP | WARNING | CONTINUE]
	 */

	public void setPackageToOnError(String v) {
		if ((v.length() > 0)
			&& (v.equals("STOP")
				|| v.equals("WARNING")
				|| v.equals("CONTINUE"))) {
			_package_to_on_error = v;
			_num_arguments += 2;
		}
	}

	public String getPackageToOnError() {
		return _package_to_on_error;
	}

	/**
	 * Idl2Cpp option -portable
	 */

	public void setPortable(boolean v) {
		_portable = v;
		if (_portable)
			_num_arguments++;
	}

	public boolean getPortable() {
		return _portable;
	}

	/**
	 * Idl2Cpp option -package package
	 */
	public void setPackage(String v) {
		if (v.length() > 0) {
			_package = v;
			_num_arguments += 2;
		}
	}

	public String getPackage() {
		return _package;
	}

	public void setTie(boolean v) {
		_tie = v;
		if (!_tie)
			_num_arguments++;
	}

	public boolean getTie() {
		return _tie;
	}

	public void setSkel(boolean v) {
		_skel = v;
		if (!_skel)
			_num_arguments++;
	}

	public boolean getSkel() {
		return _skel;
	}

	public void setStub(boolean v) {
		_stub = v;
		if (!_stub)
			_num_arguments++;
	}

	public boolean getStub() {
		return _stub;
	}

	public void setVerbose(String v) {
		if ((v.length() > 0)
			&& (v.equals("NONE")
				|| v.equals("USER")
				|| v.equals("FLOW")
				|| v.equals("DEBUG")
				|| v.equals("DEEP_DEBUG"))) {
			_verbose = v;
			_num_arguments += 2;
		}
	}

	public String getVerbose() {
		return _verbose;
	}

	protected String[] create_arguments() throws BuildException {

        // source file required

        if (_source == null)
            throw new BuildException("Attribute source is required");

		// pre-process the source dir
        String srcpath = getProject().getProperty("tididlc.src.path");

		if (_srcdir == null && (srcpath == null || srcpath.length() == 0)) {
			throw new BuildException("Attribute srcdir or property tididlc.src.path is required");
		} else if (_srcdir != null) {
			File srcdir_file = new File(_srcdir);
			if (!srcdir_file.isAbsolute())
				_srcdir =
					getProject().getBaseDir().getAbsolutePath()
						+ File.separator
						+ _srcdir;
		} else { // _srcdir == null, srcpath != null
            // DAVV - elegimos un _srcdir a partir de srcpath
            StringTokenizer tokenizer = new StringTokenizer(srcpath, File.pathSeparator);
            boolean found = false;
            while (tokenizer.hasMoreTokens() && !found) {
                _srcdir = tokenizer.nextToken();
                File srcdir_file = new File(_srcdir);
                if (!srcdir_file.isAbsolute())
                    _srcdir = getProject().getBaseDir().getAbsolutePath() + File.separator + _srcdir;
                found = (new File(_srcdir + File.separator + _source)).exists();
            }
        }

		// Include always -I . to set the basedir in include path

		_num_arguments += 2;

		String[] arguments = new String[_num_arguments];

		int i = 0;

		if (_verbose != null) {
            arguments[i++] = "-verbose=" + _verbose;
		}

		if (_portable) {
			arguments[i++] = "-portable";
		}

		if (!_skel) {
			arguments[i++] = "-no_skel";
		}

		if (!_tie) {
			arguments[i++] = "-no_tie";
		}

		if (!_stub) {
			arguments[i++] = "-no_stub";
		}

		if (_package != null) {
			arguments[i++] = "-package";
			arguments[i++] = _package;
		}

		if (_package_to != null) {
			for (int j = 0; j < _package_to.size(); j++) {
				PackageTo pack_to = (PackageTo) _package_to.elementAt(j);
				arguments[i++] = "-package_to";
				arguments[i++] = pack_to.getModule();
				arguments[i++] = pack_to.getPackage();
			}
		}

		if (_package_to_on_error != null) {
			arguments[i++] = "-package_to_on_error";
			arguments[i++] = _package_to_on_error;
		}

		// include -I .
		arguments[i++] = "-I";
		arguments[i++] = getProject().getBaseDir().getAbsolutePath();

		if (_includes != null) {
			for (int j = 0; j < _includes.size(); j++) {
                Include include = (Include) _includes.elementAt(j);

                arguments[i++] = "-I";
                File file = new File(include.getDir());
                if (file.isAbsolute())
                    arguments[i++] = include.getDir();
                else
                    arguments[i++] = getProject().getBaseDir().getAbsolutePath() + File.separator + include.getDir();

			}
		}

        if (_defines != null) {
            for (int j = 0; j < _defines.size(); j++) {
                Define define = (Define) _defines.elementAt(j);

                arguments[i++] = "-D";
                arguments[i++] = define.getValue().equals("")?define.getName():define.getName() + "=" + define.getValue();

            }
        }

        if (_undefs != null) {
            for (int j = 0; j < _undefs.size(); j++) {
                Undef undef = (Undef) _undefs.elementAt(j);

                arguments[i++] = "-U";
                arguments[i++] = undef.getName();

            }
        }

		if (_output != null) {
			arguments[i++] = "-output";

            File file = new File(_output);

            if (file.isAbsolute())
                arguments[i++] = _output;
            else
                arguments[i++] = getProject().getBaseDir().getAbsolutePath() + File.separator + _output;
		}
		
		if (_output_h != null) {
			arguments[i++] = "-output_h";
            File file = new File(_output_h);

            if (file.isAbsolute())
                arguments[i++] = _output_h;
            else
                arguments[i++] = getProject().getBaseDir().getAbsolutePath() + File.separator + _output_h;
		}

		// source file

        // source file
        File file = new File(_source);
        if (file.isAbsolute())
            arguments[i] = _source;
        else if (_srcdir != null)
            arguments[i] = _srcdir + File.separator + _source;
        else
            arguments[i] = getProject().getBaseDir().getAbsolutePath() + File.separator + _source;

		return arguments;
	}

    /*protected String toAbsolutePath(String path) {
        File file = new File(path);

        if (file.isAbsolute()) {
            return path;
        }

        if (_srcdir != null)
            return _srcdir + File.separator + path;
        else
            return getProject().getBaseDir().getAbsolutePath() + File.separator + path;
    }*/

	/**
	 * Idl2Cpp option -package package
	 */
	 
	protected String _package = null;

	/**
	 * Idl2Cpp option -output dir
	 */

	protected String _output = null;

	/**
	 * Idl2Cpp option -output_h dir
	 */

	protected String _output_h = null;

	/**
	 * Idl2Cpp option -package_to_on_error [STOP | WARNING | CONTINUE]
	 */

	protected String _package_to_on_error = null;

	/**
	 * Idl2Cpp option -portable
	 */

	protected boolean _portable = false;

	/**
	 * Idl2Cpp option -no_tie
	 */
	 
	protected boolean _tie = true;

	/**
	 * Idl2Cpp option -no_stub
	 */
	
	protected boolean _stub = true;

	/**
	 * Idl2Cpp option -no_skel
	 */
	 
	protected boolean _skel = true;

	/**
	 * Idl2Cpp IDL source file
	 */
	 
	protected String _source = null;

	/**
	 * Idl2Cpp IDL source file
	 */
	 
	protected String _srcdir = null;
	
    /**
      * Idl2Java IDL source path
      */

    //protected String _srcpath = null;

	/**
	 * Idl2Cpp IDL -verbose option
	 */
	 
	protected String _verbose = null;

	/**
	 * Idl2Cpp options -package_to module package
	 */
	 
	protected java.util.Vector _package_to = null;

	/**
	 * Idl2Cpp options -I path
	 */
	 
	protected java.util.Vector _includes = null;

    /**
     * Idl2Cpp options -D name=value
     */

    protected java.util.Vector _defines = null;

    /**
     * Idl2Cpp options -U name
     */

    protected java.util.Vector _undefs = null;

    /**
     * Idl2Java options -U name
     */

	protected int _num_arguments = 0;

    /**
     * Reinicializa los campos para que sucesivas ejecuciones de la tarea (sucesivas llamadas a execute())
     * funcionen adecuadamente
     */

    protected void clean() {
        _num_arguments = 0;
        _defines = null;
        _includes = null;
        _output = null;
        _package = null;
        _package_to = null;
        _package_to_on_error = null;
        _portable =  false;
        _skel = true;
        _source = null;
        _srcdir = null;
        //_srcpath = null;
        _stub = true;
        _tie = true;
        _verbose = null;
        _undefs = null;
    }

    protected String getIdlcClassPath() {
        String classpath = getProject().getProperty("tididlc.class.path");
        if (classpath == null || classpath == "") {
            classpath = System.getProperty("tididlc.class.path");
            if (classpath == null)
                classpath = "";
        }
        return classpath;
    }

}
