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

package es.tid.TIDIdlc.util;

import java.io.PrintStream;

/**
 * Trace class. Improves performace when traces are not used.
 */
public class Traces
{

    public static final int NONE = 0;

    public static final int USER = 1;

    public static final int FLOW = 2;

    public static final int DEBUG = 3;

    public static final int DEEP_DEBUG = 4;

    public static void setLevel(int value)
    {
        st_level = value;
    }

    public static int getLevel()
    {
        return st_level;
    }

    public static void setCurrentLevel(int value)
    {
        st_current_level = value;
    }

    public static void println()
    {
        if (st_current_level <= st_level) {
            st_out.println();
        }
    }

    public static void println(Object newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(Object newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.print(newTrace);
        }
    }

    public static void print(int newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.print(newTrace);
        }
    }

    public static void print(long newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(float newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(double newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(char newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(boolean newTrace)
    {
        if (st_current_level <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void println(int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println();
        }
    }

    public static void println(Object newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(Object newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.print(newTrace);
        }
    }

    public static void print(int newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.print(newTrace);
        }
    }

    public static void print(long newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(float newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(double newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(char newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void print(boolean newTrace, int currentLevel)
    {
        if (currentLevel <= st_level) {
            st_out.println(newTrace);
        }
    }

    public static void setOut(PrintStream newOut)
    {
        st_out = newOut;
    }

    private static int st_level = NONE;

    private static int st_current_level = NONE;

    private static PrintStream st_out = System.out;

}