/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 31 $
* Date: $Date: 2005-05-17 13:22:05 +0200 (Tue, 17 May 2005) $
* Last modified by: $Author: gsanjuan $
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

package es.tid.TIDIdlc.idl2xml;

import es.tid.TIDIdlc.util.Traces;

import java.io.*;
import java.util.*;

/**
 * Preprocessor adapter.
 */
public class Preprocessor
    implements LineManager
{

    public static Preprocessor getInstance()
    {
        return _theInstance;
    }

    public void preprocess(String fileName, Vector searchPath)
        throws Exception
    {
        if (searchPath != null)
            _preprocessor.searchPath = searchPath;
        _preprocessor.generateLineDirectives = false;
        _preprocessor.displayLogo = false;
        _preprocessor.setLineManager(this);
        _current = new StringWriter();
        Reader reader;
        if (fileName != null) {
            _firstFile = fileName;
            reader = new FileReader(_firstFile);
        } else {
            _firstFile = "<stdin>";
            reader = new InputStreamReader(System.in);
        }
        _preprocessor.preprocess(new BufferedReader(reader), _current);
    }
    
    public String getFile(){
    	return _firstFile;
    }

    public void define(String s, String s1)
        throws Exception
    {
        _preprocessor.define(s, s1);
    }

    public void undefine(String s)
    {
        _preprocessor.undefine(s);
    }

    public boolean isSymbolName(String s)
    {
        return _preprocessor.isSymbolName(s);
    }

    public Reader getReader()
    {
        if (Traces.getLevel() == Traces.DEEP_DEBUG) {
            System.out.println(_current.getBuffer().toString());
        }

        return new StringReader(_current.getBuffer().toString());
    }

    public String locate(int lineNumber)
    {
        includeFile file = null;
        for (int i = 0; i < lineNumbers.size(); i++) {
            file = (includeFile) lineNumbers.elementAt(i);
            if (file.prepLine >= lineNumber) {
                if (i == 0)
                    return _firstFile + ":" + (lineNumber - 1);
                else {
                    includeFile file1 = 
                        (includeFile) lineNumbers.elementAt(i - 1);
                    int valor = (lineNumber - file1.prepLine) + file1.line - 1;
                    if (file1.name == null)
                        return _firstFile + ":" + valor;
                    else
                        return file1.name + ":" + valor;
                }
            }
        }
        if (file == null)
            return _firstFile + ":" + (lineNumber - 1);
        else
            return _firstFile + ":" + (lineNumber - file.prepLine + file.line);
    }

    public String locateFile(int lineNumber)
    {
        includeFile file = null;
        for (int i = 0; i < lineNumbers.size(); i++) {
            file = (includeFile) lineNumbers.elementAt(i);
            if (file.prepLine >= lineNumber) {
                if (i == 0)
                    return _firstFile + ":" + (lineNumber - 1);
                else {
                    includeFile file1 = 
                        (includeFile) lineNumbers.elementAt(i - 1);
                    if (file1.name == null)
                        return _firstFile;
                    else
                        return file1.name;
                }
            }
        }
        if (file == null)
            return _firstFile;
        else
            return _firstFile;
    }

    public void add(String file, int line, int convline)
    {
        lineNumbers.addElement(new includeFile(convline, line, file));
    }
    
    public boolean findTypedefAndConstNames(String id) {
    	return _preprocessor.externalTypedsAndConstNames(id); 
    }

    private static Preprocessor _theInstance = new Preprocessor();

    private StringWriter _current = null;

    private CPreprocessor _preprocessor = null;
    
   // public static void ReInit() {
   // 	_theInstance = new Preprocessor();
   // }

    private static Vector lineNumbers = new Vector(); // vector of includeFile

    private String _firstFile = null;

    private Preprocessor()
    {
        _preprocessor = new CPreprocessor();
    }

}

class includeFile
{
    public int prepLine;

    public int line;

    public String name;

    public includeFile(int prepLine, int line, String name)
    {
        this.line = line;
        this.name = name;
        this.prepLine = prepLine;
    }

    public String toString()
    {
        if (name != null)
            return name + ":" + line;
        else
            return "" + line;
    }
}