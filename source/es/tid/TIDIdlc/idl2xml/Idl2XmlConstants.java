/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 172 $
* Date: $Date: 2006-08-03 11:25:25 +0200 (Thu, 03 Aug 2006) $
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

/* Generated By:JavaCC: Do not edit this line. Idl2XmlConstants.java */
package es.tid.TIDIdlc.idl2xml;

public interface Idl2XmlConstants {

  int EOF = 0;
  int ID = 83;
  int OCTALINT = 84;
  int DECIMALINT = 85;
  int HEXADECIMALINT = 86;
  int FIXED = 87;
  int FLOATONE = 88;
  int FLOATTWO = 89;
  int WCHARACTER = 90;
  int CHARACTER = 91;
  int WSTRING = 92;
  int STRING = 93;
  int PRAGMA_PREFIX = 98;
  int PRAGMA = 100;

  int DEFAULT = 0;
  int in_pragma = 1;
  int in_pragma3 = 2;
  int end_pragma3 = 3;
  int in_pragma2 = 4;
  int in_pragma4 = 5;

  String[] tokenImage = {
    "<EOF>",
    "\" \"",
    "\"\\t\"",
    "\"\\n\"",
    "\"\\r\"",
    "<token of kind 5>",
    "<token of kind 6>",
    "<token of kind 7>",
    "\";\"",
    "\"module\"",
    "\"{\"",
    "\"}\"",
    "\"abstract\"",
    "\"local\"",
    "\"interface\"",
    "\":\"",
    "\",\"",
    "\"::\"",
    "\"valuetype\"",
    "\"custom\"",
    "\"truncatable\"",
    "\"supports\"",
    "\"public\"",
    "\"private\"",
    "\"factory\"",
    "\"(\"",
    "\")\"",
    "\"in\"",
    "\"const\"",
    "\"=\"",
    "\"|\"",
    "\"^\"",
    "\"&\"",
    "\">>\"",
    "\"<<\"",
    "\"+\"",
    "\"-\"",
    "\"*\"",
    "\"/\"",
    "\"%\"",
    "\"~\"",
    "\"TRUE\"",
    "\"FALSE\"",
    "\"typedef\"",
    "\"native\"",
    "\"float\"",
    "\"double\"",
    "\"long\"",
    "\"short\"",
    "\"unsigned\"",
    "\"char\"",
    "\"wchar\"",
    "\"boolean\"",
    "\"octet\"",
    "\"any\"",
    "\"Object\"",
    "\"switch\"",
    "\"case\"",
    "\"default\"",
    "\"enum\"",
    "\"sequence\"",
    "\"<\"",
    "\">\"",
    "\"string\"",
    "\"wstring\"",
    "\"[\"",
    "\"]\"",
    "\"readonly\"",
    "\"attribute\"",
    "\"exception\"",
    "\"oneway\"",
    "\"void\"",
    "\"out\"",
    "\"inout\"",
    "\"raises\"",
    "\"context\"",
    "\"fixed\"",
    "\"ValueBase\"",
    "\"struct\"",
    "\"union\"",
    "\"CORBA::TypeCode\"",
    "\"AbstractBase\"",
    "\"CORBA::AbstractBase\"",
    "<ID>",
    "<OCTALINT>",
    "<DECIMALINT>",
    "<HEXADECIMALINT>",
    "<FIXED>",
    "<FLOATONE>",
    "<FLOATTWO>",
    "<WCHARACTER>",
    "<CHARACTER>",
    "<WSTRING>",
    "<STRING>",
    "\"#pragma\"",
    "<token of kind 95>",
    "<token of kind 96>",
    "<token of kind 97>",
    "<PRAGMA_PREFIX>",
    "<token of kind 99>",
    "<PRAGMA>",
    "<token of kind 101>",
  };

}