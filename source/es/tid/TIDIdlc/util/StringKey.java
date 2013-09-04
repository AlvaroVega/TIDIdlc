/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 303 $
* Date: $Date: 2009-05-19 16:12:23 +0200 (Tue, 19 May 2009) $
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

import java.lang.Comparable;
import java.lang.Object;
import java.lang.String;

public class StringKey extends Object
    implements Comparable
{
    Object _obj;
    
    public StringKey(Object obj)
    {
        super();
        _obj = obj;
    }

    public int compareTo(Object o)
    {
        String str = (String) ((StringKey)o).get();
        return ((String)_obj).compareTo(str);
        //return str.compareTo((String)_obj);
    }

    public Object get()
    {
        return _obj;
    }
}
