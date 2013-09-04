/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 168 $
* Date: $Date: 2006-08-02 07:50:17 +0200 (Wed, 02 Aug 2006) $
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

package es.tid.TIDIdlc.idl2xml;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import es.tid.TIDIdlc.*;
import es.tid.TIDIdlc.util.FileManager;
import es.tid.TIDIdlc.util.IncludeFileManager;


/**
 * ANSI C Preprocessor.
 */

public final class CPreprocessor
{

    private static final int NOTFOUND = -1;

    private static final int ERROR = -1;

    private static final int OK = 0;

    private static final int TRUE = 1;

    private static final int FALSE = 0;

    private static final int IGNORE = -1;

    private static final Integer iFALSE = new Integer(0);

    private static final Integer iTRUE = new Integer(1);

    private static final Integer iIGNORE = new Integer(-1);

    private static final String TK_HEX = "0123456789ABCDEFabcdef";

    private static final String TK_SPECIAL = "$_";

    private static final String TK_DIRECTIVE_DELIMITER = "#";

    private static final String STDIN = "<stdin>";

    public String directiveDelimiter;

    public String lineDelimiter;

    private String inputFileName;

    private String outputFileName;

    private String internalFileName;

    private String errorFileName;

    private LineNumberReader inputReader;

    private Writer errorWriter;

    private Writer outputWriter;

    private LineNumberReader _inputReader;

    private Writer _errorWriter;

    private Writer _outputWriter;

    private boolean usingStdin;

    private boolean usingStdout;

    private boolean usingStderr;

    private Stack ifStack;

    private int level;

    private boolean generate_line_directive;

    private int line_number;

    private Hashtable symbolTable;

    public Vector searchPath;

    public boolean displayLogo;

    public boolean verboseMessages;

    public boolean warningMessages;

    public boolean removeComments;

    public boolean embeddedQuotes;

    public boolean removeBlankLines;

    public boolean generateLineDirectives;

    private String cwd;

    private int tokenOffset;

    private int nextTokenOffset;

    private String line;

    private StringBuffer tbuffer;

    private boolean inEsc;

	private int bracesCounter;

	private int insideInclude;
	
	private Vector externalTypedefsAndConst;

	private int insideTypedef;

	private int insideConst;

	private String lastName;

    public CPreprocessor()
    {
        directiveDelimiter = "#";
        ifStack = new Stack();
        symbolTable = new Hashtable();
        searchPath = new Vector();
        tbuffer = new StringBuffer();
        displayLogo = true;
        verboseMessages = false;
        warningMessages = true;
        removeComments = true;
        embeddedQuotes = false;
        removeBlankLines = true;
        generateLineDirectives = false;
        lineDelimiter = System.getProperty("line.separator");
        usingStdin = true;
        _inputReader = new LineNumberReader(new InputStreamReader(System.in));
        inputReader = _inputReader;
        usingStdout = true;
        _outputWriter = new PrintWriter(System.out);
        outputWriter = _outputWriter;
        usingStderr = true;
        _errorWriter = new PrintWriter(System.err);
        errorWriter = _errorWriter;
        insideTypedef = 0;
		insideConst = 0;
        bracesCounter = 0;
        insideInclude = 0;
        externalTypedefsAndConst = new Vector();
        lastName = null;
    }

    public CPreprocessor(String s)
    {
        this();
        directiveDelimiter = s;
    }

    private void _define(String s, int i)
    {
        symbolTable.put(s, new CPreprocessorValue(i));
    }

    private void _define(String s, String s1)
    {
        symbolTable.put(s, new CPreprocessorValue(s1));
    }

    private void _define(String s, CPreprocessorValue ppvalue)
    {
        symbolTable.put(s, ppvalue);
    }

    private void _init()
        throws Exception
    {
        level = 0;
        line_number = 0;
        generate_line_directive = true;
        tbuffer.setLength(0);
        //tbuffer.append("TIDorb 1.0 17/4/2000");
        tbuffer.append("TIDorb" + CompilerConf.st_compiler_version);
        Object obj = tbuffer.toString();
        symbolTable.put("__TIDorb__", new CPreprocessorValue(((String) (obj))));
        obj = Calendar.getInstance();
        java.util.Date date = ((Calendar) (obj)).getTime();
        SimpleDateFormat simpledateformat = 
            (SimpleDateFormat) DateFormat.getDateTimeInstance(1, 1);
        simpledateformat.applyPattern("EEE MMM d H:mm:ss yyyy");
        tbuffer.setLength(0);
        tbuffer.append("\"");
        tbuffer.append(simpledateformat.format(date));
        tbuffer.append("\"");
        String s = tbuffer.toString();
        symbolTable.put("__TIMESTAMP__", new CPreprocessorValue(s));
        simpledateformat.applyPattern("MMM d yyyy");
        tbuffer.setLength(0);
        tbuffer.append("\"");
        tbuffer.append(simpledateformat.format(date));
        tbuffer.append("\"");
        s = tbuffer.toString();
        symbolTable.put("__DATE__", new CPreprocessorValue(s));
        simpledateformat.applyPattern("H:mm:ss");
        tbuffer.setLength(0);
        tbuffer.append("\"");
        tbuffer.append(simpledateformat.format(date));
        tbuffer.append("\"");
        s = tbuffer.toString();
        symbolTable.put("__TIME__", new CPreprocessorValue(s));
        tbuffer.setLength(0);
        tbuffer.append("\"");
        tbuffer.append(inputFileName);
        tbuffer.append("\"");
        s = tbuffer.toString();
        symbolTable.put("__FILE__", new CPreprocessorValue(s));
    }

    private String _peekAtNextToken(boolean flag)
        throws Exception
    {
        StringBuffer stringbuffer = new StringBuffer();
        int i = 0;
        boolean flag1 = false;
        boolean flag2 = true;
        int j = 0;
        if (flag)
            for (nextTokenOffset = tokenOffset; nextTokenOffset < line.length(); nextTokenOffset++) {
                if (!Character.isWhitespace(line.charAt(nextTokenOffset)))
                    break;
            }
        else
            nextTokenOffset = tokenOffset;
        if (line.length() > nextTokenOffset + 2
            && line.charAt(nextTokenOffset) == '0'
            && (line.charAt(nextTokenOffset + 1) == 'x' 
            || line.charAt(nextTokenOffset + 1) == 'x')) {
            stringbuffer.append("0x");
            for (nextTokenOffset += 2; nextTokenOffset < line.length(); nextTokenOffset++) {
                char c = line.charAt(nextTokenOffset);
                if (!Character.isLetter(c) && !Character.isDigit(c)
                    && "$_".indexOf(c) == -1 && true)
                    break;
                if ("0123456789ABCDEFabcdef".indexOf(c) == -1)
                    throw new Exception(
                                "Unexpected token ["
                                + c
                                + "] encountered while searching for hexadecimal value");
                stringbuffer.append(c);
            }

            return stringbuffer.toString();
        }
        boolean flag3;
        do {
            flag3 = false;
            for (; nextTokenOffset < line.length(); nextTokenOffset++) {
                char c1 = line.charAt(nextTokenOffset);
                if (j == 0) {
                    if (!inEsc && (c1 == '\'' || c1 == '"')) {
                        stringbuffer.append(c1);
                        if (flag1) {
                            if (i != c1)
                                continue;
                            nextTokenOffset++;
                            break;
                        }
                        flag1 = true;
                        i = c1;
                        continue;
                    }
                    if (embeddedQuotes)
                        if (!inEsc && c1 == '\\')
                            inEsc = true;
                        else
                            inEsc = false;
                    if (!flag1) {
                        if (c1 == '/') {
                            int k = nextTokenOffset + 1;
                            if (k < line.length()) {
                                char c2 = line.charAt(k);
                                if (c2 == '*') {
                                    j++;
                                } else {
                                    if (c2 == '/') {
                                        stringbuffer.append(
                                            line.substring(nextTokenOffset));
                                        nextTokenOffset = line.length();
                                    } else if (stringbuffer.length() == 0) {
                                        nextTokenOffset++;
                                        stringbuffer.append(c1);
                                    }
                                    break;
                                }
                            } else {
                                if (stringbuffer.length() == 0) {
                                    nextTokenOffset++;
                                    stringbuffer.append(c1);
                                }
                                break;
                            }
                        } else if (!Character.isLetter(c1)
                                   && !Character.isDigit(c1)
                                   && "$_".indexOf(c1) == -1 && true) {
                            if (c1 == '.' && flag2) {
                                if (stringbuffer.length() > 0
                                    && (!Character.isDigit(stringbuffer.charAt(stringbuffer.length() - 1)) 
                                        || nextTokenOffset + 1 < line.length()
                                        && Character.isLetter(line.charAt(nextTokenOffset + 1))))
                                    break;
                                flag2 = false;
                                stringbuffer.append(c1);
                                continue;
                            }
                            if (stringbuffer.length() == 0) {
                                nextTokenOffset++;
                                stringbuffer.append(c1);
                            }
                            break;
                        }
                        if (!Character.isDigit(c1))
                            flag2 = false;
                        if (stringbuffer.length() > 0
                            && stringbuffer.charAt(stringbuffer.length() - 1) == '.'
                            && !Character.isDigit(c1))
                            return stringbuffer.toString();
                    }
                } else if (c1 == '/') {
                    int l = nextTokenOffset + 1;
                    if (l < line.length()) {
                        char c3 = line.charAt(l);
                        if (c3 == '*')
                            j++;
                    }
                } else if (c1 == '*') {
                    int i1 = nextTokenOffset + 1;
                    if (i1 < line.length()) {
                        char c4 = line.charAt(i1);
                        if (c4 == '/') {
                            stringbuffer.append(c1);
                            stringbuffer.append(c4);
                            nextTokenOffset += 2;
                            return stringbuffer.toString();
                        }
                    }
                }
                stringbuffer.append(c1);
            }

            if (j != 0) {
                String s = inputReader.readLine();
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of file while in comment [/*]");
                tbuffer.setLength(0);
                tbuffer.append(line);
                tbuffer.append(System.getProperty("line.separator"));
                tbuffer.append(s);
                line = tbuffer.toString();
                if (verboseMessages) {
                    tbuffer.setLength(0);
                    tbuffer.append(inputFileName != null ? inputFileName
                        : "<stdin>");
                    tbuffer.append('(');
                    tbuffer.append(inputReader.getLineNumber());
                    tbuffer.append("): [");
                    tbuffer.append(s);
                    tbuffer.append(']');
                    Writer writer = outputWriter;
                    String s1 = tbuffer.toString();
                    writer.write(s1);
                    writer.write(lineDelimiter);
                    inc_line();
                    writer.flush();
                }
                if (removeComments)
                    if (removeBlankLines) {
                        generate_line_directive = true;
                    } else {
                        Writer writer1 = outputWriter;
                        writer1.write("");
                        writer1.write(lineDelimiter);
                        inc_line();
                        writer1.flush();
                    }
                flag3 = true;
            }
        } while (flag3);
        if (stringbuffer.length() == 0)
            return null;
        else
            return stringbuffer.toString();
    }

    private void _preprocess(Reader reader, Writer writer, Writer writer1)
        throws Exception
    {
        errorWriter = writer1;
        usingStderr = false;
        if (reader instanceof LineNumberReader)
            inputReader = (LineNumberReader) reader;
        else
            inputReader = new LineNumberReader(reader);
        usingStdin = false;
        outputWriter = writer;
        usingStdout = false;
        _init();
        cwd = (new File(".")).getCanonicalPath();
        String s = cwd;
        symbolTable.put("__CWD__", new CPreprocessorValue(s));
        searchPath.addElement(cwd);
        run();
        _wrapup();
    }

    private void _wrapup()
    {
        ifStack.removeAllElements();
        symbolTable.clear();
        searchPath.removeAllElements();
        tbuffer.setLength(0);
        inputFileName = null;
        outputFileName = null;
        internalFileName = null;
        errorFileName = null;
        inputReader = null;
        outputWriter = null;
        errorWriter = null;
        usingStdin = true;
        inputReader = _inputReader;
        usingStdout = true;
        outputWriter = _outputWriter;
        usingStderr = true;
        errorWriter = _errorWriter;
    }

    private String changeAll(String s, String s1, String s2)
    {
        StringBuffer stringbuffer = new StringBuffer();
        int i;
        int j;
        for (j = 0; (i = s.indexOf(s1, j)) != -1; j = i + s1.length()) {
            stringbuffer.append(s.substring(j, i));
            stringbuffer.append(s2);
        }

        stringbuffer.append(s.substring(j));
        return stringbuffer.toString();
    }

    private String convertFileName(String s)
    {
        tbuffer.setLength(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            /*
             * if(c == '\\') tbuffer.append(c);
             */
            tbuffer.append(c);
        }

        String s1 = tbuffer.toString();
        tbuffer.setLength(0);
        return s1;
    }

    public void define(String s)
        throws Exception
    {
        line = s;
        processDefine();
    }

    public void define(String s, double d)
        throws Exception
    {
        define(s, new CPreprocessorValue(d));
    }

    public void define(String s, int i)
        throws Exception
    {
        define(s, new CPreprocessorValue(i));
    }

    public void define(String s, String s1)
        throws Exception
    {
        define(s, new CPreprocessorValue(s1));
    }

    private void define(String s, CPreprocessorValue ppvalue)
        throws Exception
    {
        if (symbolTable.containsKey(s) && warningMessages) {
            tbuffer.setLength(0);
            tbuffer.append("Warning: Macro redefinition [");
            tbuffer.append(s);
            tbuffer.append("]");
            log(tbuffer.toString());
        }
        symbolTable.put(s, ppvalue);
    }

    public void define(String s, boolean flag)
        throws Exception
    {
        define(s, new CPreprocessorValue(flag));
    }

    private void displayLogo()
        throws Exception
    {
        if (displayLogo) {
            System.err.println("TidORB Preprocessor Version 1.0\n");
            displayLogo = false;
        }
    }

    private void generateLineDirective()
        throws Exception
    {
        if (lineManager != null) {
            lineManager.add(internalFileName, line_number, convline);
        }
        if (generate_line_directive) {
            generate_line_directive = false;
            if (generateLineDirectives) {
                tbuffer.setLength(0);
                tbuffer.append("#line ");
                int i = line_number;
                if (i == 0)
                    i = 1;
                tbuffer.append(i);
                tbuffer.append(" \"");
                tbuffer.append(internalFileName != null ? internalFileName
                    : "<stdin>");
                tbuffer.append("\"");
                Writer writer = outputWriter;
                String s = tbuffer.toString();
                writer.write(s);
                writer.write(lineDelimiter);
                inc_line();
                writer.flush();
            }
        }
    }

    public String getFileName()
    {
        return inputFileName;
    }

    public int getLineNumber()
    {
        if (inputReader == null)
            return -1;
        else
            return inputReader.getLineNumber();
    }

    private String getNextToken(boolean flag, boolean flag1)
        throws Exception
    {
        String s = peekAtNextToken(flag, flag1);
        tokenOffset = nextTokenOffset;
               
        if (s!=null&&s.equals("typedef")) {
        	insideTypedef=1;
        	return s;
        }
        
        if (s!=null&&s.equals("const")) {
        	insideConst=1;
        	return s;
        }
        
        if (s!=null&&s.equals("{")) {
        	bracesCounter++;
        	return s;
        }
        
        if (s!=null&&s.equals("}")) {
        	bracesCounter--;
        	return s;
        }
        
        if (insideTypedef==1&&s!=null&&s.equals(";")&&insideInclude>0&&CompilerConf.getNotGenerateCode()) {
        	insideTypedef=0;
        	externalTypedefsAndConst.add(lastName);
        	return s;
        }
              
        if (insideConst==1&&s!=null&&s.equals("=")&&insideInclude>0&&CompilerConf.getNotGenerateCode()) {
        	insideConst=0;
        	externalTypedefsAndConst.add(lastName);
        	return s;
        }
       
        if (s!=null&&s.length()>=2)
        	lastName=s;
        
        return s;
    }

    public static String getVersion()
    {
        return "1.0 03/04/2000";
    }

    private int ifStackState()
    {
        if (ifStack.empty())
            return 1;
        else
            return ((Integer) ifStack.peek()).intValue();
    }

    private void incrementTokenOffset()
    {
        tokenOffset = nextTokenOffset;
    }

    private void init()
        throws Exception
    {
        if (errorFileName != null) {
            errorWriter = new PrintWriter(new OutputStreamWriter(
                                  new FileOutputStream(errorFileName)));
            usingStderr = false;
        }
        File file;
        if (inputFileName == null) {
            file = new File(".");
        } else {
            file = new File(inputFileName);
            if (!file.exists())
                throw new Exception("File [" + inputFileName
                                    + "] does not exist");
            internalFileName = convertFileName(file.getCanonicalPath());
            inputReader = new LineNumberReader(
                             new InputStreamReader(new FileInputStream(file)));
            usingStdin = false;
        }
        if (outputFileName != null) {
            outputWriter = new PrintWriter(new OutputStreamWriter(
                                   new FileOutputStream(outputFileName)));
            usingStdout = false;
        }
        _init();
        cwd = file.getCanonicalPath();
        int i = cwd.lastIndexOf(File.separatorChar);
        if (i == -1)
            i = cwd.length();
        cwd = cwd.substring(0, i);
        String s = cwd;
        symbolTable.put("__CWD__", new CPreprocessorValue(s));
        searchPath.addElement(cwd);
    }

    private boolean isDefined(String s)
    {
        Object obj = symbolTable.get(s);
        return obj != null;
    }

    private boolean isDoubleConstant(String s)
    {
        int i = s.length();
        int j = 0;
        boolean flag = false;
        for (; j < i; j++) {
            char c = s.charAt(j);
            if (c == '.') {
                if (flag)
                    return false;
                flag = true;
            } else if (!Character.isDigit(c))
                return false;
        }

        return flag;
    }

    private boolean isHexConstant(String s)
    {
        boolean flag = false;
        if (s.length() > 2 && s.charAt(0) == '0'
            && (s.charAt(1) == 'x' || s.charAt(1) == 'X')) {
            for (int i = 2; i < s.length(); i++)
                if ("0123456789ABCDEFabcdef".indexOf(s.charAt(i)) == -1)
                    break;

            flag = true;
        }
        return flag;
    }

    private boolean isIntegerConstant(String s)
    {
        int i = s.length();
        for (int j = 0; j < i; j++)
            if (!Character.isDigit(s.charAt(j)))
                return false;

        return true;
    }

    private boolean isLongConstant(String s)
    {
        int i = s.length();
        for (int j = 0; j < i; j++)
            if (!Character.isDigit(s.charAt(j)))
                return false;

        return true;
    }

    private boolean isSpecial(char c)
    {
        return "$_".indexOf(c) != -1;
    }

    private boolean isStringConstant(String s)
    {
        int i = s.length() - 1;
        char c = s.charAt(0);
        char c1 = s.charAt(i);
        if (c == '"' && c1 == '"')
            return true;
        return c == '\'' && c1 == '\'';
    }

    protected boolean isSymbolName(String s)
    {
        int i = s.length();
        if (Character.isDigit(s.charAt(0)))
            return false;
        for (int j = 0; j < i; j++) {
            char c = s.charAt(j);
            if (!Character.isLetter(c) && !Character.isDigit(c)
                && "$_".indexOf(c) == -1 && true)
                return false;
        }

        return true;
    }

    private void log(String s)
        throws Exception
    {
        tbuffer.setLength(0);
        tbuffer.append(inputFileName != null ? inputFileName : "<stdin>");
        if (inputReader == null) {
            tbuffer.append(": ");
        } else {
            tbuffer.append('(');
            tbuffer.append(inputReader.getLineNumber());
            tbuffer.append("): ");
        }
        tbuffer.append(s);
        String s1 = tbuffer.toString();
        tbuffer.setLength(0);
        Writer writer = errorWriter;
        writer.write(s1);
        writer.write(lineDelimiter);
        inc_line();
        writer.flush();
    }

    private void output(Writer writer, String s)
        throws Exception
    {
        writer.write(s);
        writer.flush();
    }

    private void outputln(Writer writer, String s)
        throws Exception
    {
        writer.write(s);
        writer.write(lineDelimiter);
        inc_line();
        writer.flush();
    }

    private String peekAtNextToken(boolean flag, boolean flag1)
        throws Exception
    {
        String s;
        boolean flag2;
        do {
            flag2 = false;
            s = _peekAtNextToken(flag);
            if (s != null && flag1
                && (s.startsWith("//") || s.startsWith("/*"))) {
                tokenOffset = nextTokenOffset;
                flag2 = true;
            }
        } while (flag2);
        return s;
    }

    public void preprocess(Reader reader, Writer writer)
        throws Exception
    {
        if (displayLogo) {
            System.err.println("TdiORB Preprocessor Version 1.0\n");
            displayLogo = false;
        }
        _preprocess(reader, writer, errorWriter);
    }

    public void preprocess(Reader reader, Writer writer, Writer writer1)
        throws Exception
    {
        if (displayLogo) {
            System.err.println("TidORB Preprocessor Version 1.0\n");
            displayLogo = false;
        }
        _preprocess(reader, writer, writer1);
    }

    public String preprocess(String s)
        throws Exception
    {
        if (displayLogo) {
            System.err.println("TdiORB Preprocessor Version 1.0\n");
            displayLogo = false;
        }
        StringReader stringreader = new StringReader(s);
        StringWriter stringwriter = new StringWriter();
        _preprocess(stringreader, stringwriter, errorWriter);
        return stringwriter.toString();
    }

    public void preprocess(String s, String s1, String s2)
        throws Exception
    {
        if (displayLogo) {
            System.err.println("TidORB Preprocessor Version 1.0\n");
            displayLogo = false;
        }
        inputFileName = s;
        outputFileName = s1;
        errorFileName = s2;
        LineNumberReader linenumberreader = inputReader;
        if (inputFileName != null) {
            linenumberreader = new LineNumberReader(new InputStreamReader(
                                       new FileInputStream(inputFileName)));
            File file = new File(inputFileName);
            internalFileName = convertFileName(file.getCanonicalPath());
        }
        Object obj = outputWriter;
        if (outputFileName != null)
            obj = new PrintWriter(new OutputStreamWriter(
                          new FileOutputStream(outputFileName)));
        Object obj1 = errorWriter;
        if (errorFileName != null)
            obj1 = new PrintWriter(new OutputStreamWriter(
                           new FileOutputStream(errorFileName)));
        _preprocess(linenumberreader, ((Writer) (obj)), ((Writer) (obj1)));
    }

    private CPreprocessorValue processAdd()
        throws Exception
    {
        CPreprocessorValue ppvalue = processMult();
        CPreprocessorValue ppvalue1;
        label0: do {
            String s = peekAtNextToken(true, true);
            if (s == null)
                return ppvalue;
            char c;
            if (s.equals("+") || s.equals("-")) {
                c = s.charAt(0);
                tokenOffset = nextTokenOffset;
                ppvalue1 = processMult();
            } else {
                return ppvalue;
            }
            switch (c)
            {
                case 44: // ','
                default:
                break;

                case 43: // '+'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                      "Operator [+] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [+] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(ppvalue.getDoubleValue()
                                                  + ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue()
                                                  + ppvalue1.getIntegerValue());

                case 45: // '-'
                break label0;

            }
        } while (true);
        if (ppvalue.type == 5 || ppvalue1.type == 5)
            throw new Exception(
                          "Operator [-] can not be performed on [Boolean] value");
        if (ppvalue.type == 4 || ppvalue1.type == 4)
            throw new Exception(
                          "Operator [-] can not be performed on [String] value");
        if (ppvalue.type == 3 || ppvalue1.type == 3)
            return new CPreprocessorValue(ppvalue.getDoubleValue()
                                          - ppvalue1.getDoubleValue());
        else
            return new CPreprocessorValue(ppvalue.getIntegerValue()
                                          - ppvalue1.getIntegerValue());
    }

    private CPreprocessorValue processBit()
        throws Exception
    {
        CPreprocessorValue ppvalue = processAdd();
        CPreprocessorValue ppvalue1;
        label0: do {
            String s = peekAtNextToken(true, true);
            if (s == null)
                return ppvalue;
            char c;
            if (s.equals("<")) {
                int i = tokenOffset;
                int k = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals("<")) {
                    tokenOffset = i;
                    nextTokenOffset = k;
                    return ppvalue;
                }
                c = '<';
            } else if (s.equals(">")) {
                int j = tokenOffset;
                int l = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals(">")) {
                    tokenOffset = j;
                    nextTokenOffset = l;
                    return ppvalue;
                }
                c = '>';
            } else if (s.equals("&") || s.equals("|") || s.equals("^"))
                c = s.charAt(0);
            else
                return ppvalue;
            tokenOffset = nextTokenOffset;
            ppvalue1 = processAdd();
            switch (c)
            {
                default:
                break;

                case 60: // '<'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                      "Operator [<<] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [<<] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        throw new Exception(
                                      "Operator [<<] can not be performed on [Double] value");
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue() 
                                                      << ppvalue1.getIntegerValue());

                case 94: // '^'
                break label0;

                case 62: // '>'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                      "Operator [>>] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [>>] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        throw new Exception(
                                      "Operator [>>] can not be performed on [Double] value");
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue()
                                                >> ppvalue1.getIntegerValue());

                case 38: // '&'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        return new CPreprocessorValue(ppvalue.getBooleanValue()
                                                 & ppvalue1.getBooleanValue());
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [&] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        throw new Exception(
                                      "Operator [&] can not be performed on [Double] value");
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue()
                                                & ppvalue1.getIntegerValue());

                case 124: // '|'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        return new CPreprocessorValue(ppvalue.getBooleanValue()
                                                      | ppvalue1.getBooleanValue());
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [|] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        throw new Exception(
                                      "Operator [|] can not be performed on [Double] value");
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue()
                                                      | ppvalue1.getIntegerValue());

            }
        } while (true);
        if (ppvalue.type == 5 || ppvalue1.type == 5)
            return new CPreprocessorValue(ppvalue.getBooleanValue()
                                          ^ ppvalue1.getBooleanValue());
        if (ppvalue.type == 4 || ppvalue1.type == 4)
            throw new Exception(
                          "Operator [^] can not be performed on [String] value");
        if (ppvalue.type == 3 || ppvalue1.type == 3)
            throw new Exception(
                          "Operator [^] can not be performed on [Double] value");
        else
            return new CPreprocessorValue(ppvalue.getIntegerValue()
                                          ^ ppvalue1.getIntegerValue());
    }

    private CPreprocessorValue processComp()
        throws Exception
    {
        CPreprocessorValue ppvalue = processBit();
        CPreprocessorValue ppvalue1;
        label0: do {
            String s = peekAtNextToken(true, true);
            if (s == null)
                return ppvalue;
            byte byte0;
            if (s.equals("=")) {
                int i = tokenOffset;
                int i1 = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals("=")) {
                    tokenOffset = i;
                    nextTokenOffset = i1;
                    return ppvalue;
                }
                byte0 = 61;
            } else if (s.equals("!")) {
                int j = tokenOffset;
                int j1 = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals("=")) {
                    tokenOffset = j;
                    nextTokenOffset = j1;
                    return ppvalue;
                }
                byte0 = 33;
            } else if (s.equals("<")) {
                int k = tokenOffset;
                int k1 = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (s.equals("=")) {
                    byte0 = 76;
                } else {
                    tokenOffset = k;
                    nextTokenOffset = k1;
                    byte0 = 60;
                }
            } else if (s.equals(">")) {
                int l = tokenOffset;
                int l1 = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                String s1 = peekAtNextToken(true, true);
                if (s1 == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (s1.equals("=")) {
                    byte0 = 71;
                } else {
                    tokenOffset = l;
                    nextTokenOffset = l1;
                    byte0 = 62;
                }
            } else {
                return ppvalue;
            }
            tokenOffset = nextTokenOffset;
            ppvalue1 = processBit();
            switch (byte0)
            {
                default:
                break;

                case 33: // '!'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        return new CPreprocessorValue(
                                        ppvalue.getBooleanValue() 
                                        != ppvalue1.getBooleanValue());
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        return new CPreprocessorValue(
                                       ppvalue.getStringValue().compareTo(
                                              ppvalue1.getStringValue()) != 0);
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(
                                       ppvalue.getDoubleValue() 
                                       != ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(
                                       ppvalue.getIntegerValue() 
                                       != ppvalue1.getIntegerValue());

                case 62: // '>'
                break label0;

                case 61: // '='
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        return new CPreprocessorValue(
                                       ppvalue.getBooleanValue() 
                                       == ppvalue1.getBooleanValue());
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        return new CPreprocessorValue(
                                       ppvalue.getStringValue().compareTo(
                                              ppvalue1.getStringValue()) == 0);
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(
                                       ppvalue.getDoubleValue() 
                                       == ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(
                                       ppvalue.getIntegerValue() 
                                       == ppvalue1.getIntegerValue());

                case 76: // 'L'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                            "Operator [<=] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        return new CPreprocessorValue(
                                       ppvalue.getStringValue().compareTo(
                                           ppvalue1.getStringValue()) <= 0);
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(
                                       ppvalue.getDoubleValue() 
                                       <= ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(
                                       ppvalue.getIntegerValue() 
                                       <= ppvalue1.getIntegerValue());

                case 60: // '<'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                      "Operator [<] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        return new CPreprocessorValue(
                                       ppvalue.getStringValue().compareTo(
                                            ppvalue1.getStringValue()) < 0);
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(
                                       ppvalue.getDoubleValue() 
                                       < ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(
                                       ppvalue.getIntegerValue() 
                                       < ppvalue1.getIntegerValue());

                case 71: // 'G'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                            "Operator [>=] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        return new CPreprocessorValue(
                                       ppvalue.getStringValue().compareTo(
                                           ppvalue1.getStringValue()) >= 0);
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(
                                       ppvalue.getDoubleValue() 
                                       >= ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(
                                       ppvalue.getIntegerValue() 
                                       >= ppvalue1.getIntegerValue());

            }
        } while (true);
        if (ppvalue.type == 5 || ppvalue1.type == 5)
            throw new Exception(
                          "Operator [>] can not be performed on [Boolean] value");
        if (ppvalue.type == 4 || ppvalue1.type == 4)
            return new CPreprocessorValue(ppvalue.getStringValue().compareTo(
                                               ppvalue1.getStringValue()) > 0);
        if (ppvalue.type == 3 || ppvalue1.type == 3)
            return new CPreprocessorValue(ppvalue.getDoubleValue() 
                                          > ppvalue1.getDoubleValue());
        else
            return new CPreprocessorValue(ppvalue.getIntegerValue() 
                                          > ppvalue1.getIntegerValue());
    }

    private void processDefine()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) != 1)
            return;
        String s = getNextToken(true, true);
        if (s == null)
            throw new Exception(
                          "Unexpected end of line encountered while searching for identifier");
        if (!isSymbolName(s))
            throw new Exception(
                          "Unexpected token ["
                                                                                                                                + s
                                                                                                                                + "] encountered while searching for identifier");
        String s1 = peekAtNextToken(false, true);
        if (s1 == null) {
            processDefineIdentifier(s);
            return;
        }
        if (s1.equals("(")) {
            processDefineMacro(s);
            return;
        } else {
            processDefineIdentifier(s);
            return;
        }
    }

    private void processDefineIdentifier(String s)
        throws Exception
    {
        String s1 = getNextToken(true, true);
        if (s1 == null) {
            define(s, new CPreprocessorValue(""));
            return;
        }
        StringBuffer stringbuffer = new StringBuffer();
        do
            stringbuffer.append(s1);
        while ((s1 = getNextToken(false, true)) != null);
        String s2 = stringbuffer.toString().trim();
        define(s, new CPreprocessorValue(s2));
    }

    private void processDefineMacro(String s)
        throws Exception
    {
        tokenOffset = nextTokenOffset;
        boolean flag = false;
        boolean flag1 = false;
        Vector vector = new Vector();
        do {
            String s1 = getNextToken(true, true);
            if (s1 == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for [)]");
            if (s1.equals(")")) {
                if (flag1)
                    throw new Exception("Unexpected empty expression");
                break;
            }
            if (s1.equals(",")) {
                if (!flag)
                    throw new Exception("Unexpected empty expression");
                flag = false;
                flag1 = true;
            } else {
                if (!isSymbolName(s1))
                    throw new Exception(
                                  "Unexpected token ["
                                  + s1
                                  + "] encountered while searching for identifier");
                for (int i = 0; i < vector.size(); i++)
                    if (vector.elementAt(i).equals(s1))
                        throw new Exception("Identifier [" + s1
                                            + "] already exists");

                vector.addElement(s1);
                flag = true;
                flag1 = false;
            }
        } while (true);
        StringBuffer stringbuffer = new StringBuffer();
        String s2;
        while ((s2 = getNextToken(false, true)) != null) {
            if (isSymbolName(s2)) {
                for (int j = 0; j < vector.size(); j++)
                    if (vector.elementAt(j).equals(s2))
                        s2 = String.valueOf((char) (128 + j));

            }
            stringbuffer.append(s2);
        }

        String s3 = (char) (128 + vector.size())
                    + stringbuffer.toString().trim();
        define(s, new CPreprocessorValue(s3));
    }

    private void processDirective()
        throws Exception
    {
        if (removeBlankLines) {
            generate_line_directive = true;
        } else {
            Writer writer = outputWriter;
            writer.write("");
            writer.write(lineDelimiter);
            inc_line();
            writer.flush();
        }
        tokenOffset = nextTokenOffset;
        String s = getNextToken(true, true);
        if (s == null)
            return;
        s = s.toLowerCase();
        if (!s.equals(directiveDelimiter)) {
            if (s.equals("ifdef")) {
                processIfdef();
                return;
            }
            if (s.equals("ifndef")) {
                processIfndef();
                return;
            }
            if (s.equals("if")) {
                processIf();
                return;
            }
            if (s.equals("elif") || s.equals("elseif")) {
                processElif();
                return;
            }
            if (s.equals("else")) {
                processElse();
                return;
            }
            if (s.equals("endif"))
                if (ifStack.empty()) {
                    throw new Exception(
                                  "Unexpected token [endif] encountered without if/ifdef/ifndef");
                } else {
                    ifStack.pop();
                    return;
                }
            if (s.equals("define")) {
                processDefine();
                return;
            }
            if (s.equals("undef")) {
                processUndef();
                return;
            }
            if (s.equals("include")) {
            	
                generate_line_directive = true;
                insideInclude++;
                processIdlInclude();
                processInclude();
                insideInclude--;
                generate_line_directive = true;
                return;
            }
            if (s.equals("message")) {
                if ((ifStack.empty() ? 1 
                    : ((Integer) ifStack.peek()).intValue()) == 1) {
                    Writer writer1 = outputWriter;
                    String s1 = line.substring(tokenOffset).trim();
                    writer1.write(s1);
                    writer1.write(lineDelimiter);
                    inc_line();
                    writer1.flush();
                    return;
                }
            } else if (s.equals("error")) {
                if ((ifStack.empty() ? 1 
                    : ((Integer) ifStack.peek()).intValue()) == 1) {
                    Writer writer2 = errorWriter;
                    String s2 = line.substring(tokenOffset).trim();
                    writer2.write(s2);
                    writer2.write(lineDelimiter);
                    inc_line();
                    writer2.flush();
                    return;
                }
            } else if (s.equals("pragma")) {
                if ((ifStack.empty() ? 1 
                    : ((Integer) ifStack.peek()).intValue()) == 1)
                    processLine();
                return;
            } else {
                throw new Exception("Unexpected directive [" + line + "]");
            }
        }
    }

    private void processIdlInclude()
        throws Exception
	    {
	        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) != 1)
	            return;
	    	String idl = "";
	    	String inc = "";
	    	if(this.line.indexOf('\"')!=-1){
	    		inc = this.line.substring(this.line.indexOf("\"")+1, this.line.lastIndexOf("\""));
	    	} else {
	    		inc = this.line.substring(this.line.indexOf("<")+1, this.line.lastIndexOf(">"));
	    	}
	    	
	    	if(!inc.equals("orb.idl") && !inc.equals("PortableServer.h")){
	    		
	    		// si inputFileName == null
		    	// estamos en el idl base.
		    	if(inputFileName==null) {
		    		Preprocessor prep = Preprocessor.getInstance();
		    		idl = prep.getFile().substring(prep.getFile().lastIndexOf(File.separatorChar)+1);
		    	} else {
		        	idl = inputFileName.substring(inputFileName.lastIndexOf(File.separatorChar)+1);
		    	}
		    	IncludeFileManager ifm = IncludeFileManager.getInstance();
		    	ifm.addIncludeToIdlFile(idl,inc);
	    	}
    }

    private void processElif()
        throws Exception
    {
        if (ifStack.empty())
            throw new Exception(
                          "Unexpected token [elif] encountered without if/ifdef/ifndef");
        int i = ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue();
        ifStack.pop();
        if (i == 1) {
            ifStack.push(iIGNORE);
            return;
        }
        if (i == 0) {
            if (processLogical().getBooleanValue()) {
                ifStack.push(iTRUE);
                return;
            } else {
                ifStack.push(iFALSE);
                return;
            }
        } else {
            ifStack.push(iIGNORE);
            return;
        }
    }

    private void processElse()
        throws Exception
    {
        if (ifStack.empty())
            throw new Exception(
                          "Unexpected token [else] encountered without if/ifdef/ifndef");
        int i = ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue();
        ifStack.pop();
        if (i == 1) {
            ifStack.push(iIGNORE);
            return;
        }
        if (i == 0) {
            ifStack.push(iTRUE);
            return;
        } else {
            ifStack.push(iIGNORE);
            return;
        }
    }

    private void processEndif()
        throws Exception
    {
        if (ifStack.empty()) {
            throw new Exception(
                          "Unexpected token [endif] encountered without if/ifdef/ifndef");
        } else {
            ifStack.pop();
            return;
        }
    }

    private void processError()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) == 1) {
            Writer writer = errorWriter;
            String s = line.substring(tokenOffset).trim();
            writer.write(s);
            writer.write(lineDelimiter);
            inc_line();
            writer.flush();
        }
    }

    private CPreprocessorValue processExpression()
        throws Exception
    {
        return processLogical();
    }

    private CPreprocessorValue processFunction(String s)
        throws Exception
    {
        tokenOffset = nextTokenOffset;
        if (s.equalsIgnoreCase("defined")) {
            String s3 = getNextToken(true, true);
            if (s3 == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for parameter");
            if (!isSymbolName(s3))
                throw new Exception(
                              "Unexpected token ["
                                                                                                                                                + s3
                                                                                                                                                + "] encountered while searching for identifier");
            String s1 = peekAtNextToken(true, true);
            if (s1 == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for [)]");
            if (!s1.equals(")")) {
                throw new Exception("Unexpected token [" + s1
                              + "] encountered while searching for [)]");
            } else {
                tokenOffset = nextTokenOffset;
                return new CPreprocessorValue(isDefined(s3));
            }
        }
        if (s.equalsIgnoreCase("exist")) {
            String s4 = processExpression().getStringValue();
            String s2 = peekAtNextToken(true, true);
            if (s2 == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for [)]");
            if (!s2.equals(")")) {
                throw new Exception("Unexpected token [" + s2
                                    + "] encountered while searching for [)]");
            } else {
                tokenOffset = nextTokenOffset;
                File file = new File(s4);
                return new CPreprocessorValue(file.exists());
            }
        } else {
            throw new Exception("Function [" + s + "] is not defined");
        }
    }

    private CPreprocessorValue processIdentifier(String s)
        throws Exception
    {
        Object obj = symbolTable.get(s);
        if (obj == null) {
            return new CPreprocessorValue("");
        } else {
            String s1 = line;
            int i = tokenOffset;
            int j = nextTokenOffset;
            line = ((CPreprocessorValue) obj).getStringValue();
            tokenOffset = 0;
            nextTokenOffset = 0;
            CPreprocessorValue ppvalue = processExpression();
            line = s1;
            tokenOffset = i;
            nextTokenOffset = j;
            return new CPreprocessorValue(ppvalue);
        }
    }

    private void processIf()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) == 1) {
            if (processLogical().getBooleanValue()) {
                ifStack.push(iTRUE);
                return;
            } else {
                ifStack.push(iFALSE);
                return;
            }
        } else {
            ifStack.push(iIGNORE);
            return;
        }
    }

    private void processIfdef()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) == 1) {
            String s = getNextToken(true, true);
            if (s == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for identifier");
            Object obj = symbolTable.get(s);
            if (obj == null) {
                ifStack.push(iFALSE);
                return;
            } else {
                ifStack.push(iTRUE);
                return;
            }
        } else {
            ifStack.push(iIGNORE);
            return;
        }
    }

    private void processIfndef()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) == 1) {
            String s = getNextToken(true, true);
            if (s == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for identifier");
            Object obj = symbolTable.get(s);
            if (obj == null) {
                ifStack.push(iTRUE);
                return;
            } else {
                ifStack.push(iFALSE);
                return;
            }
        } else {
            ifStack.push(iIGNORE);
            return;
        }
    }

    private void processInclude()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) != 1)
            return;
        String s = getNextToken(true, true);
        if (s == null)
            throw new Exception(
                          "Unexpected end of line encountered while searching for filename");
        File file = null;
        String s1;
        boolean flagExists = false;
        if (s.equals("<")) {
            int i = tokenOffset;
            String s2;
            String s4;
            for (s4 = null; (s2 = getNextToken(true, true)) != null; s4 = s2);
            if (s4 == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for ["
                                                                                                                                                + s
                                                                                                                                                + "]");
            if (!s4.equals(">"))
                throw new Exception("Unexpected token [" + s4
                                    + "] encountered while searching for [>]");

            int pos = line.indexOf('>');
            s1 = line.substring(i, pos);

            if (s1.equals("CORBA.idl") && CompilerConf.getCORBA_IDL())
                s1 = "orb.idl";

            boolean flag1 = false;
            for (int k = 0; k < searchPath.size(); k++) {
                tbuffer.setLength(0);
                tbuffer.append(searchPath.elementAt(k));
                tbuffer.append(File.separatorChar);
                tbuffer.append(s1);
                file = new File(tbuffer.toString());
                if (!file.exists())
                    continue;
                flag1 = true;
                break;
            }

            flagExists = flag1;
            //if ((!flag1) && (!file.getName().equals("orb.idl")))
            if ((!flag1) && (!IncludeORB.isHardCodedIDL(file.getName())))
                throw new Exception("Include file [" + s1 + "] not found");
        } else if (s.charAt(0) == '"') {
            s1 = s.substring(1, s.length() - 1);

            if (s1.equals("CORBA.idl") && CompilerConf.getCORBA_IDL())
                s1 = "orb.idl";

            tbuffer.setLength(0);
            tbuffer.append(cwd);
            tbuffer.append(File.separatorChar);
            tbuffer.append(s1);
            file = new File(tbuffer.toString());
            //if(!file.exists() && !file.getName().equals("orb.idl")) {
            if (!file.exists() && !IncludeORB.isHardCodedIDL(file.getName())) {
                boolean flag = false;
                for (int j = 0; j < searchPath.size(); j++) {
                    tbuffer.setLength(0);
                    tbuffer.append(searchPath.elementAt(j));
                    tbuffer.append(File.separatorChar);
                    tbuffer.append(s1);
                    file = new File(tbuffer.toString());
                    if (!file.exists())
                        continue;
                    flag = true;
                    break;
                }

                flagExists = flag;
                if (!flag)
                    throw new Exception("Include file [" + s1 + "] not found");
            }
        } else {
            throw new Exception("Unexpected token [" + s
                                + "] encountered while searching for filename");
        }
        LineNumberReader linenumberreader = null;
        //if (file.getName().equals("orb.idl"))
        if (IncludeORB.isHardCodedIDL(file.getName()) && !flagExists) {
            //IncludeORB i = new IncludeORB();
            //System.err.println(i.getFile());
            linenumberreader = new LineNumberReader(new StringReader(IncludeORB
                .getFile(file.getName())));
            //CompilerConf.setOrbIncluded(true);
        } else {
            linenumberreader = new LineNumberReader(
                                       new InputStreamReader(
                                               new FileInputStream(file)));
        }
        String s3 = inputFileName;
        String s5 = internalFileName;
        boolean flag2 = usingStdin;
        LineNumberReader linenumberreader1 = inputReader;
        usingStdin = false;
        int l = line_number;
        Object obj = symbolTable.get("__LINE__");
        Object obj1 = symbolTable.get("__FILE__");
        inputFileName = s1;
        internalFileName = convertFileName(file.getCanonicalPath());
        inputReader = linenumberreader;
        tbuffer.setLength(0);
        tbuffer.append("\"");
        tbuffer.append(inputFileName);
        tbuffer.append("\"");
        String s6 = tbuffer.toString();
        symbolTable.put("__FILE__", new CPreprocessorValue(s6));
        line_number = inputReader.getLineNumber();
        int i1 = line_number;
        symbolTable.put("__LINE__", new CPreprocessorValue(i1));
        level++;
        run();
        level--;
        linenumberreader.close();
        inputFileName = s3;
        internalFileName = s5;
        inputReader = linenumberreader1;
        usingStdin = flag2;
        line_number = l;
        symbolTable.put("__FILE__", obj1);
        symbolTable.put("__LINE__", obj);
        
//        if(!inputFileName.equals("null")){
//	        IncludeFileManager ifm = IncludeFileManager.getInstance();
//	        if(ifm.thereIsFile(inputFileName)) {
//	        	ifm.addIncludeToIdlFile(inputFileName,s1);
//	        } else {
//	        	ifm.addIdlFile(s1);
//	        	ifm.addIncludeToIdlFile(inputFileName,s1);
//	        }
//        }
        
    }

    private void processLine()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) != 1)
            if (removeBlankLines) {
                generate_line_directive = true;
                return;
            } else {
                Writer writer = outputWriter;
                writer.write("");
                writer.write(lineDelimiter);
                inc_line();
                writer.flush();
                return;
            }
        int i = tokenOffset;
        int j = nextTokenOffset;
        char c = '\0';
        boolean flag1;
        do {
            inEsc = false;
            flag1 = false;
            StringBuffer stringbuffer = 
                new StringBuffer(line.substring(0, tokenOffset));
            String s;
            while ((s = getNextToken(false, removeComments)) != null) {
                if (s.equals("#")) {
                    String s1 = peekAtNextToken(false, removeComments);
                    if (s1 != null) {
                        if (s1.equals("#")) {
                            c = '#';
                            tokenOffset = nextTokenOffset;
                        } else if (s1.equals("@")) {
                            c = '\'';
                            tokenOffset = nextTokenOffset;
                        } else {
                            c = '"';
                        }
                        continue;
                    }
                } else if (isSymbolName(s)) {
                    Object obj = symbolTable.get(s);
                    if (obj != null) {
                        String s3 = peekAtNextToken(false, removeComments);
                        boolean flag;
                        if (s3 == null)
                            flag = false;
                        else if (s3.equals("("))
                            flag = true;
                        else
                            flag = false;
                        String s2 = ((CPreprocessorValue) obj).getStringValue();
                        if (s2.length() == 0) {
                            s = s2;
                            flag1 = true;
                        } else if (s2.charAt(0) >= (char) 200) {
                            if (flag) {
                                s = processMacros(s2);
                                flag1 = true;
                            }
                        } else {
                            s = s2;
                            flag1 = true;
                        }
                    }
                }
                if (c == '#') {
                    c = '\0';
                    flag1 = true;
                } else if (c == '"' || c == '\'') {
                    tbuffer.setLength(0);
                    tbuffer.append(c);
                    tbuffer.append(s);
                    tbuffer.append(c);
                    s = tbuffer.toString();
                    c = '\0';
                }
                stringbuffer.append(s);
            }

            line = stringbuffer.toString();
            tokenOffset = i;
            nextTokenOffset = j;
        } while (flag1);
        if (line.length() == 0 && removeBlankLines) {
            generate_line_directive = true;
            return;
        } else {
            generateLineDirective();
            Writer writer1 = outputWriter;
            String s4 = line;
            writer1.write(s4);
            writer1.write(lineDelimiter);
            inc_line();
            writer1.flush();
            return;
        }
    }

    private CPreprocessorValue processLogical()
        throws Exception
    {
        CPreprocessorValue ppvalue = processComp();
        CPreprocessorValue ppvalue1;
        label0: do {
            String s = peekAtNextToken(true, true);
            if (s == null)
                return ppvalue;
            if (s.equals("&")) {
                int i = tokenOffset;
                int l = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals("&")) {
                    tokenOffset = i;
                    nextTokenOffset = l;
                    return ppvalue;
                }
            } else if (s.equals("|")) {
                int j = tokenOffset;
                int i1 = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals("|")) {
                    tokenOffset = j;
                    nextTokenOffset = i1;
                    return ppvalue;
                }
            } else if (s.equals("^")) {
                int k = tokenOffset;
                int j1 = nextTokenOffset;
                tokenOffset = nextTokenOffset;
                s = peekAtNextToken(true, true);
                if (s == null)
                    throw new Exception(
                                  "Unexpected end of line encountered while searching for token");
                if (!s.equals("^")) {
                    tokenOffset = k;
                    nextTokenOffset = j1;
                    return ppvalue;
                }
            } else {
                return ppvalue;
            }
            char c = s.charAt(0);
            tokenOffset = nextTokenOffset;
            ppvalue1 = processComp();
            switch (c)
            {
                default:
                break;

                case 38: // '&'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        return new CPreprocessorValue(ppvalue.getBooleanValue()
                                                      && ppvalue1
                                                          .getBooleanValue());
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [&&] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        throw new Exception(
                                      "Operator [&&] can not be performed on [Double] value");
                    else
                        throw new Exception(
                                      "Operator [&&] can not be performed on [Integer] value");

                case 94: // '^'
                break label0;

                case 124: // '|'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        return new CPreprocessorValue(ppvalue.getBooleanValue()
                                                || ppvalue1.getBooleanValue());
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [||] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        throw new Exception(
                                      "Operator [||] can not be performed on [Double] value");
                    else
                        throw new Exception(
                                      "Operator [||] can not be performed on [Integer] value");

            }
        } while (true);
        if (ppvalue.type == 5 || ppvalue1.type == 5) {
            boolean flag = ppvalue.getBooleanValue();
            boolean flag1 = ppvalue1.getBooleanValue();
            if (flag && flag1)
                return new CPreprocessorValue(false);
            if (!flag && !flag1)
                return new CPreprocessorValue(false);
            else
                return new CPreprocessorValue(true);
        }
        if (ppvalue.type == 4 || ppvalue1.type == 4)
            throw new Exception(
                          "Operator [^^] can not be performed on [String] value");
        if (ppvalue.type == 3 || ppvalue1.type == 3)
            throw new Exception(
                          "Operator [^^] can not be performed on [Double] value");
        else
            throw new Exception(
                          "Operator [^^] can not be performed on [Integer] value");
    }

    private String processMacros(String s)
        throws Exception
    {
        int i = s.charAt(0) - 128;
        s = s.substring(1);
        String s1 = getNextToken(false, true);
        if (s1 == null)
            throw new Exception(
                          "Unexpected end of line encountered while searching for parameters");
        if (!s1.equals("("))
            throw new Exception("Unexpected token [" + s1
                                + "] encountered while searching for [(]");
        String s2 = new String(s);
        StringBuffer stringbuffer = new StringBuffer();
        char c = (char) 200;
        int j = 0;
        while ((s1 = getNextToken(false, true)) != null) {
            if (s1.equals("(")) {
                j++;
                stringbuffer.append(s1);
                continue;
            }
            if (s1.equals(")")) {
                if (j == 0) {
                    if (stringbuffer.length() > 0) {
                        s2 = changeAll(s2, String.valueOf(c++), 
                                       stringbuffer.toString());
                        stringbuffer.setLength(0);
                    }
                    break;
                }
                stringbuffer.append(s1);
                j--;
            } else if (s1.equals(",")) {
                if (j == 0) {
                    if (stringbuffer.length() > 0) {
                        s2 = changeAll(s2, String.valueOf(c++), 
                                       stringbuffer.toString());
                        stringbuffer.setLength(0);
                    }
                } else {
                    stringbuffer.append(s1);
                }
            } else {
                stringbuffer.append(s1);
            }
        }

        if (c - 128 < i) {
            if (warningMessages)
                log("Warning: Unexpected token [)] encountered while searching for parameter");
            for (int k = i + 128; c < k; c++)
                s2 = changeAll(s2, String.valueOf(c), "");

        } else if (c - 128 > i && warningMessages)
            log("Warning: Unexpected parameter encountered while searching for [)]");
        return s2.toString();
    }

    private void processMessage()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) == 1) {
            Writer writer = outputWriter;
            String s = line.substring(tokenOffset).trim();
            writer.write(s);
            writer.write(lineDelimiter);
            inc_line();
            writer.flush();
        }
    }

    private CPreprocessorValue processMult()
        throws Exception
    {
        CPreprocessorValue ppvalue = processUnary();
        CPreprocessorValue ppvalue1;
        label0: do {
            String s = peekAtNextToken(true, true);
            if (s == null)
                return ppvalue;
            char c;
            if (s.equals("*") || s.equals("/") || s.equals("%")) {
                c = s.charAt(0);
                tokenOffset = nextTokenOffset;
                ppvalue1 = processUnary();
            } else {
                return ppvalue;
            }
            switch (c)
            {
                default:
                break;

                case 37: // '%'
                break label0;

                case 42: // '*'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                      "Operator [*] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [*] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(ppvalue.getDoubleValue()
                                                  * ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue()
                                                  * ppvalue1.getIntegerValue());

                case 47: // '/'
                    if (ppvalue.type == 5 || ppvalue1.type == 5)
                        throw new Exception(
                                      "Operator [/] can not be performed on [Boolean] value");
                    if (ppvalue.type == 4 || ppvalue1.type == 4)
                        throw new Exception(
                                      "Operator [/] can not be performed on [String] value");
                    if (ppvalue.type == 3 || ppvalue1.type == 3)
                        return new CPreprocessorValue(ppvalue.getDoubleValue()
                                                  / ppvalue1.getDoubleValue());
                    else
                        return new CPreprocessorValue(ppvalue.getIntegerValue()
                                                  / ppvalue1.getIntegerValue());

            }
        } while (true);
        if (ppvalue.type == 5 || ppvalue1.type == 5)
            throw new Exception(
                          "Operator [%] can not be performed on [Boolean] value");
        if (ppvalue.type == 4 || ppvalue1.type == 4)
            throw new Exception(
                          "Operator [%] can not be performed on [String] value");
        if (ppvalue.type == 3 || ppvalue1.type == 3)
            return new CPreprocessorValue(ppvalue.getDoubleValue()
                                          % ppvalue1.getDoubleValue());
        else
            return new CPreprocessorValue(ppvalue.getIntegerValue()
                                          % ppvalue1.getIntegerValue());
    }

    private void processPragma()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) != 1) {
            return;
        } else {
            processLine();
            return;
        }
    }

    private CPreprocessorValue processUnary()
        throws Exception
    {
        char c = '\0';
        String s = peekAtNextToken(true, true);
        if (s != null
            && (s.equals("+") || s.equals("-") || s.equals("!") 
                || s.equals("~"))) {
            c = s.charAt(0);
            tokenOffset = nextTokenOffset;
        }
        s = peekAtNextToken(true, true);
        CPreprocessorValue ppvalue;
        if (s == null)
            ppvalue = processValue();
        else if (s.equals("(")) {
            tokenOffset = nextTokenOffset;
            ppvalue = processLogical();
            String s1 = peekAtNextToken(true, true);
            if (s1 == null)
                throw new Exception(
                              "Unexpected end of line encountered while searching for [)]");
            if (!s1.equals(")"))
                throw new Exception("Unexpected token [" + s1
                                    + "] encountered while searching for [)]");
            tokenOffset = nextTokenOffset;
        } else {
            ppvalue = processValue();
        }
        switch (c)
        {
            case 43: // '+'
                switch (ppvalue.type)
                {
                    case 5: // '\005'
                        throw new Exception(
                                      "Operator [+] can not be performed on [Boolean] value");

                    case 4: // '\004'
                        throw new Exception(
                                      "Operator [+] can not be performed on [String] value");

                    case 3: // '\003'
                        return ppvalue;

                    case 2: // '\002'
                        return ppvalue;

                }
            // fall through

            case 45: // '-'
                switch (ppvalue.type)
                {
                    case 5: // '\005'
                        throw new Exception(
                                      "Operator [-] can not be performed on [Boolean] value");

                    case 4: // '\004'
                        throw new Exception(
                                      "Operator [-] can not be performed on [String] value");

                    case 3: // '\003'
                        return new CPreprocessorValue(-ppvalue.getDoubleValue());

                    case 2: // '\002'
                        return new CPreprocessorValue(-ppvalue
                            .getIntegerValue());

                }
            // fall through

            case 33: // '!'
                switch (ppvalue.type)
                {
                    case 5: // '\005'
                        return new CPreprocessorValue(
                                       !ppvalue.getBooleanValue());

                    case 4: // '\004'
                        throw new Exception(
                                      "Operator [!] can not be performed on [String] value");

                    case 3: // '\003'
                        throw new Exception(
                                      "Operator [!] can not be performed on [Double] value");

                    case 2: // '\002'
                        throw new Exception(
                                      "Operator [!] can not be performed on [Integer] value");

                }
            // fall through

            case 126: // '~'
                switch (ppvalue.type)
                {
                    case 5: // '\005'
                        throw new Exception(
                                      "Operator [~] can not be performed on [Boolean] value");

                    case 4: // '\004'
                        throw new Exception(
                                      "Operator [~] can not be performed on [String] value");

                    case 3: // '\003'
                        throw new Exception(
                                      "Operator [~] can not be performed on [Double] value");

                    case 2: // '\002'
                        return new CPreprocessorValue(
                                       ~ppvalue.getIntegerValue());

                }
            // fall through

            default:
                return ppvalue;

        }
    }

    private void processUndef()
        throws Exception
    {
        if ((ifStack.empty() ? 1 : ((Integer) ifStack.peek()).intValue()) != 1)
            return;
        String s = getNextToken(true, true);
        if (s == null)
            throw new Exception(
                          "Unexpected end of line encountered while searching for identifier");
        if (!isSymbolName(s))
            throw new Exception(
                          "Unexpected token ["
                                                                                                                                + s
                                                                                                                                + "] encountered while searching for identifier");
        String s1 = peekAtNextToken(true, true);
        if (s1 != null) {
            throw new Exception(
                          "Unexpected token ["
                                                                                                                                + s1
                                                                                                                                + "] encountered while searching for end of line");
        } else {
            symbolTable.remove(s);
            return;
        }
    }

    private CPreprocessorValue processValue()
        throws Exception
    {
        String s = getNextToken(true, true);
        if (s == null)
            throw new Exception(
                          "Unexpected end of line encountered while searching for value");
        if (isHexConstant(s))
            return new CPreprocessorValue(Integer.parseInt(s.substring(2), 16));
        if (isStringConstant(s))
            return new CPreprocessorValue(s.substring(1, s.length() - 1));
        if (isIntegerConstant(s))
            return new CPreprocessorValue(Integer.parseInt(s));
        if (isDoubleConstant(s))
            return new CPreprocessorValue((new Double(s)).doubleValue());
        if (isSymbolName(s)) {
            String s1 = peekAtNextToken(false, true);
            if (s1 == null)
                return processIdentifier(s);
            if (s1.equals("("))
                return processFunction(s);
            else
                return processIdentifier(s);
        } else {
            throw new Exception("Unexpected token [" + s
                                + "] encountered while searching for value");
        }
    }

    private void run()
        throws Exception
    {
        generateLineDirective();
        while ((line = inputReader.readLine()) != null) {
            line_number = inputReader.getLineNumber();
            int i = line_number;
            symbolTable.put("__LINE__", new CPreprocessorValue(i));
            if (verboseMessages) {
                tbuffer.setLength(0);
                tbuffer.append(inputFileName != null ? inputFileName
                    : "<stdin>");
                tbuffer.append('(');
                tbuffer.append(inputReader.getLineNumber());
                tbuffer.append("): [");
                tbuffer.append(line);
                tbuffer.append(']');
                Writer writer = outputWriter;
                String s1 = tbuffer.toString();
                writer.write(s1);
                writer.write(lineDelimiter);
                inc_line();
                writer.flush();
            }
            tokenOffset = 0;
            nextTokenOffset = 0;
            if (line.length() == 0) {
                processLine();
            } else {
                int j = 0;
                while (line.charAt(line.length() - 1) == '\\') {
                    String s = inputReader.readLine();
                    if (s == null)
                        throw new Exception(
                                      "Unexpected end of file while in line continuation [\\]");
                    tbuffer.setLength(0);
                    tbuffer.append(line.substring(0, line.length() - 1));
                    tbuffer.append(s);
                    line = tbuffer.toString();
                    if (verboseMessages) {
                        tbuffer.setLength(0);
                        tbuffer.append(inputFileName != null ? inputFileName
                            : "<stdin>");
                        tbuffer.append('(');
                        tbuffer.append(inputReader.getLineNumber());
                        tbuffer.append("): [");
                        tbuffer.append(s);
                        tbuffer.append(']');
                        Writer writer1 = outputWriter;
                        String s3 = tbuffer.toString();
                        writer1.write(s3);
                        writer1.write(lineDelimiter);
                        inc_line();
                        writer1.flush();
                    }
                    if (removeBlankLines)
                        generate_line_directive = true;
                    else
                        j++;
                }

                String s2 = peekAtNextToken(true, removeComments);
                if (s2 == null) {
                    line = "";
                    tokenOffset = 0;
                    nextTokenOffset = 0;
                    processLine();
                } else if (s2.equals(directiveDelimiter))
                    processDirective();
                else
                    processLine();
                for (int k = 0; k < j; k++) {
                    Writer writer2 = outputWriter;
                    writer2.write("");
                    writer2.write(lineDelimiter);
                    inc_line();
                    writer2.flush();
                }

            }
        }

        if (level == 0 && !ifStack.empty())
            throw new Exception(
                          "Unexpected end of file encountered while in if/ifdef/ifndef");
        else
            return;
    }

    public void undefine(String s)
    {
        symbolTable.remove(s);
    }

    private void wrapup()
    {
        if (errorWriter != null && !usingStderr) {
            try {
                errorWriter.close();
            }
            catch (Exception _ex) {}
            errorWriter = null;
        }
        if (outputWriter != null && !usingStdout) {
            try {
                outputWriter.close();
            }
            catch (Exception _ex) {}
            outputWriter = null;
        }
        if (inputReader != null && !usingStdin) {
            try {
                inputReader.close();
            }
            catch (Exception _ex) {}
            inputReader = null;
        }
        _wrapup();
    }

    // Tabla con conversiones de numero de linea

    private int convline = 0;

    private void inc_line()
    {
        convline++;
    }

    private LineManager lineManager = null;

    public void setLineManager(LineManager lineManager)
    {
        this.lineManager = lineManager;
    }
    
    public boolean externalTypedsAndConstNames(String id) {
    	return externalTypedefsAndConst.contains(id);
    }

}