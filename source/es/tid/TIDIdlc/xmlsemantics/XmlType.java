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

package es.tid.TIDIdlc.xmlsemantics;

/**
 * Título: Idlc Compilador IDL a Java y C++  
 */

import org.w3c.dom.*;

public abstract class XmlType
{

    /**
     * Returns the type for typedefs.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The converted type.
     */
    /*
     * RELALO - queda util solo para Java - public abstract String
     * getTypedefType(Element doc) ;
     */

    /*
     * tal y como se usa, no aporta nada con respecto a getTypedefType
     * /** Returns the type for typedefs, using getAbsoluteUnrolledName @param
     * doc The XML node where the type is. @return The converted type. -/ public
     * abstract String getAbsoluteTypedefType(Element doc) ;
     */

    /**
     * Returns the Cpp type for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    public abstract String getType(Element doc);

    /**
     * Returns the Cpp type for an XML node, ignoring the -package_to param
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    //public abstract String getTypeWithoutPackage(Element doc) ;

    /**
     * Returns the Cpp type for method parameter (in/out/inout). Table 1-3 pag
     * 106 IDL C++ mapping, inout and out parameters are equals except with not
     * fixed (variable) types.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param out
     *            True if it is an OUT or INOUT parameter.
     * @return The Cpp type.
     * 
     * public abstract String getParamType(Element doc, boolean out) ;
     */

    /**
     * Returns the Cpp helper type for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp type.
     */
    public abstract String getHelperType(Element doc);

    /**
     * Returns the Cpp typecode for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @return The Cpp typecode.
     */
    public abstract String getTypecode(Element doc)
        throws Exception;

    /**
     * Returns the Cpp type reader for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param inputStreamName
     *            name of the inputStream to be used.
     * @return The Cpp type reader.
     */
    public abstract String getTypeReader(Element doc, String inputStreamName)
        throws Exception;

    /**
     * Returns the Cpp type writer for an XML node.
     * 
     * @param doc
     *            The XML node where the type is.
     * @param outputStreamName
     *            Name of the outputStream to be used.
     * @param outputData
     *            Name of the output data.
     * @return The Cpp type writer.
     */
    public abstract String getTypeWriter(Element doc, String outputStreamName,
                                         String outputData)
        throws Exception;

    /**
     * Returns the Cpp type for an IDL basic type.
     * 
     * @param type
     *            The IDL type.
     * @return The Cpp type.
     */
    public abstract String basicMapping(String type);

    /**
     * Returns the Cpp Holder type for an IDL basic type.
     * 
     * @param type
     *            The IDL type.
     * @return The Cpp Holder type.
     */
    public abstract String basicOutMapping(String type);

    /**
     * Returns the ORB mapping for an XML node. ORB methods considered: read,
     * write, insert, extract.
     * 
     * @param el
     *            The XML node where the type is.
     * @return The ORB type.
     */
    public abstract String basicORBTypeMapping(Element el);

    /**
     * Returns the ORB mapping (for TCKind) for an XML node.
     * 
     * @param el
     *            The XML node where the type is.
     * @return The ORB type.
     */
    public abstract String basicORBTcKindMapping(Element el);

    /*
     * RELALO - nunca se usan public abstract String
     * getUnrolledName(String scopedName) ;
     * 
     * public abstract String getUnrolledName(Element doc ) ;
     */

    /*
     * tal y como se usa, no aporta nada con respecto a getUnrolledName
     * (salvo confusión) public abstract String getAbsoluteUnrolledName(Element
     * doc);
     */

    /*
     * DAVV - RELALO public abstract String getUnrolledNameWithoutPackage(String
     * scopedName) ;
     * 
     * public abstract String getUnrolledNameWithoutPackage(Element doc) ;
     */

    public abstract String getDefaultConstructor(String type);

}