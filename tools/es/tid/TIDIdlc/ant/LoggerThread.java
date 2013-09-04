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

/*
 * Created on 21-nov-2003
 */

package es.tid.TIDIdlc.ant;

import java.io.*;

/**
 * @author davv
 * used to map the output and error of new processes to console
 * 'in' will be connected to the output or error from new process
 * 'out' will be System.out or System.err
 */

public class LoggerThread extends Thread {
    private InputStream in;
    private PrintStream out;
    public LoggerThread(InputStream in, PrintStream out) {
        this.in = in;
        this.out = out;
    }
    public void run() {
        try {
            InputStreamReader sr = new InputStreamReader(in);
            BufferedReader br = new BufferedReader(sr);
            String iLine;
            while ((iLine = br.readLine())!= null)
                out.println(iLine);
            br.close();
        } catch (IOException e) {
        }
    }
}
