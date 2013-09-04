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

package es.tid.TIDIdlc.idl2xml;

/**
 * Class preprocessor values.
 */

public class CPreprocessorValue
{

    protected static final int UNKNOWN = 1;

    protected static final int INTEGER = 2;

    protected static final int DOUBLE = 3;

    protected static final int STRING = 4;

    protected static final int BOOLEAN = 5;

    protected int type;

    protected int iValue;

    protected double dValue;

    protected String sValue;

    protected boolean bValue;

    protected CPreprocessorValue()
    {
        type = 1;
    }

    protected CPreprocessorValue(double d)
    {
        type = 3;
        dValue = d;
    }

    protected CPreprocessorValue(int i)
    {
        type = 2;
        iValue = i;
    }

    protected CPreprocessorValue(String s)
    {
        type = 4;
        sValue = s;
    }

    protected CPreprocessorValue(CPreprocessorValue ppvalue)
        throws Exception
    {
        type = ppvalue.type;
        switch (type)
        {
            case 2: // '\002'
                iValue = ppvalue.iValue;
                return;

            case 3: // '\003'
                dValue = ppvalue.dValue;
                return;

            case 4: // '\004'
                sValue = ppvalue.sValue;
                return;

            case 5: // '\005'
                bValue = ppvalue.bValue;
                return;

        }
        throw new Exception("unexpected type [" + type + "]");
    }

    protected CPreprocessorValue(boolean flag)
    {
        type = 5;
        bValue = flag;
    }

    protected boolean getBooleanValue()
        throws Exception
    {
        switch (type)
        {
            case 2: // '\002'
                return iValue != 0;

            case 3: // '\003'
                return dValue != 0.0D;

            case 4: // '\004'
                if (sValue == null)
                    return false;
                return sValue.length() != 0;

            case 5: // '\005'
                return bValue;

        }
        throw new Exception("unexpected type [" + type + "]");
    }

    protected double getDoubleValue()
        throws Exception
    {
        switch (type)
        {
            case 2: // '\002'
                return (double) iValue;

            case 3: // '\003'
                return dValue;

            case 4: // '\004'
                return Double.valueOf(sValue).doubleValue();

        }
        throw new Exception("unexpected type [" + type + "]");
    }

    protected int getIntegerValue()
        throws Exception
    {
        switch (type)
        {
            case 2: // '\002'
                return iValue;

            case 3: // '\003'
                return (int) dValue;

            case 4: // '\004'
                return Integer.parseInt(sValue);

        }
        throw new Exception("unexpected type [" + type + "]");
    }

    protected String getStringValue()
        throws Exception
    {
        switch (type)
        {
            case 2: // '\002'
                return String.valueOf(iValue);

            case 3: // '\003'
                return String.valueOf(dValue);

            case 4: // '\004'
                return sValue;

        }
        throw new Exception("unexpected type [" + type + "]");
    }

    protected Object getValue()
        throws Exception
    {
        switch (type)
        {
            case 2: // '\002'
                return new Integer(getIntegerValue());

            case 3: // '\003'
                return new Double(getDoubleValue());

            case 4: // '\004'
                return new String(getStringValue());

            case 5: // '\005'
                return new Boolean(getBooleanValue());

        }
        throw new Exception("unexpected type [" + type + "]");
    }
}