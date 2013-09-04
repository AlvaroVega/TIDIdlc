/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 28 $
* Date: $Date: 2005-05-13 13:10:50 +0200 (Fri, 13 May 2005) $
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

package es.tid.TIDIdlc.xmlsemantics;

import es.tid.TIDIdlc.CompilerConf;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * Manages typedefs definitions. It manages the unrolling of typedefs, according
 * to the CORBA 2.3 java mapping specification.
 */

public class TypedefManager
{

    /**
     * @return The Singleton instance of TypedefManager
     */
    public static TypedefManager getInstance()
    {
        if (st_the_instance == null)
            st_the_instance = new TypedefManager();
        return st_the_instance;
    }

    public static void Shutdown()
	{
    	st_the_instance = null;
 	}
    /**
     * Saves a typedef definition. It produces the saves the unrolled type if
     * the targetType is already defined.
     * 
     * @param type
     *            New type
     * @param targetType
     *            Target type of the typedef definition (used for Java)
     * @param holderType
     *            Holder type to be used for
     * @param type
     *            (used for Java)
     * @param helperType
     *            Helper type to be used for
     * @param type
     *            (used for Java)
     * @param definition
     *            IDL definition key (OMg_struc, OMG_enum..) (used for Cpp)
     * @param kind
     *            Value of OMG_kind attribute (used for Cpp)
     */
    public void typedef(String type, String targetType, String holderType,
                        String helperType, String definition, String kind)
    {
        // DAVV - Ante la falta de utilidad del TypedefManager original para la
        // generaci�n de c�digo
        //          C++, se a�aden dos nuevos campos a TypeInfo que s� son de inter�s
        // para el proceso
        TypeInfo typeInfo, newTypeInfo = null;
        if (CompilerConf.getCompilerType().equals("Java")) {
            // DAVV - este es el c�digo original
            typeInfo = (TypeInfo) m_types.get(targetType);
            if (typeInfo == null) {
                if (holderType == null)
                    // Use new info (new target, but no holderType)
                    newTypeInfo = new TypeInfo(targetType, // targetType
                                               targetType, // unrolledType
                                               type, // unrolledHolderType
                                               type, // unrolledHelperType
                                               null, null);
                else
                    // Use new info (new target & holder type)
                    newTypeInfo = new TypeInfo(targetType, // targetType
                                               targetType, // unrolledType
                                               holderType, // unrolledHolderType
                                               helperType, // unrolledHelperType
                                               null, null);
            } else {
                // Use info of the old type (unrolledType)
                newTypeInfo = new TypeInfo(targetType, // targetType
                                           typeInfo.m_unrolled_type, // unrolledType
                                           typeInfo.m_unrolled_holder_type, // unrolledHolderType
                                           typeInfo.m_unrolled_helper_type, // unrolledHelperType
                                           null, null);
            }
            m_types.put(type, newTypeInfo);

        } else { // DAVV este el c�digo para C++;
            typeInfo = (TypeInfo) m_types.get(type);
            if (typeInfo == null) { // DAVV - se hace typedef una vez y fuera
                newTypeInfo = new TypeInfo(null, null, null, null, definition,
                                           kind);
                m_types.put(type, newTypeInfo);
            }
        }
    }

    /**
     * @param type
     *            Type defined in the typedef declaration
     * @return TargetType of
     * @param type
     */
    public String getTargetType(String type)
    {
        TypeInfo typeInfo = (TypeInfo) m_types.get(type);
        if (typeInfo == null)
            return null;
        return typeInfo.m_target_type;
    }

    /**
     * @param type
     *            Type defined in the typedef declaration
     * @return Unrolled type of
     * @param type
     */
    public String getUnrolledType(String type)
    {
        TypeInfo typeInfo = (TypeInfo) m_types.get(type);
        if (typeInfo == null)
            return null;
        return typeInfo.m_unrolled_type;
    }

    /**
     * @param type
     *            Type defined in the typedef declaration
     * @return Unrolled holder type of
     * @param type
     */
    public String getUnrolledHolderType(String type)
    {
        TypeInfo typeInfo = (TypeInfo) m_types.get(type);
        if (typeInfo == null)
            return null;
        return typeInfo.m_unrolled_holder_type;
    }

    /**
     * @param type
     *            Type defined in the typedef declaration
     * @return Unrolled helper type of
     * @param type
     */
    public String getUnrolledHelperType(String type)
    {
        TypeInfo typeInfo = (TypeInfo) m_types.get(type);
        if (typeInfo == null)
            return null;
        return typeInfo.m_unrolled_helper_type;
    }

    /**
     * @param type
     *            Type defined in the typedef declaration
     * @return Definition of
     * @param type
     */
    public String getDefinitionType(String type)
    {
        TypeInfo typeInfo = (TypeInfo) m_types.get(type);
        if (typeInfo == null)
            return null;
        return typeInfo.m_definition_type;
    }

    /**
     * @param type
     *            Type defined in the typedef declaration
     * @return Kind of
     * @param type
     */
    public String getKind(String type)
    {
        TypeInfo typeInfo = (TypeInfo) m_types.get(type);
        if (typeInfo == null)
            return null;
        return typeInfo.m_kind_type;
    }

    /**
     * Shows info stored in the TypedefManager.
     */
    public void dump()
    {
        Enumeration keys = m_types.keys();
        Enumeration elements = m_types.elements();
        for (; keys.hasMoreElements();) {
            System.out.print(keys.nextElement());
            TypeInfo info = (TypeInfo) elements.nextElement();
            if (CompilerConf.getCompilerType().equals("Java")) {
                System.out.print(" : ");
                System.out.print(info.m_target_type);
                System.out.print(" : ");
                System.out.print(info.m_unrolled_type);
                System.out.print(" : ");
                System.out.print(info.m_unrolled_holder_type);
                System.out.print(" : ");
                System.out.print(info.m_unrolled_helper_type);
                System.out.println();
            } else {
                System.out.print(" : ");
                System.out.print(info.m_definition_type);
                System.out.print(" : ");
                System.out.print(info.m_kind_type);
            }
        }
    }


    public String getTypedefKind (String name){
        Enumeration keys = m_types.keys();
        Enumeration elements = m_types.elements();
        for (; keys.hasMoreElements();) {
        	String clave = (String) keys.nextElement();
        	TypeInfo info = (TypeInfo) elements.nextElement();
        	if (clave.equals(name)) {
        		return info.m_kind_type;
        	}
      	}
        return "";
    }
    
    
    private TypedefManager()
    {}

    private Hashtable m_types = new Hashtable();

    private static TypedefManager st_the_instance = null;
}

class TypeInfo
{

    public TypeInfo(String targetType, String unrolledType,
                    String unrolledHolderType, String unrolledHelperType,
                    String definitionType, String kindType)
    {
        this.m_target_type = targetType;
        this.m_unrolled_type = unrolledType;
        this.m_unrolled_holder_type = unrolledHolderType;
        this.m_unrolled_helper_type = unrolledHelperType;
        this.m_definition_type = definitionType;
        this.m_kind_type = kindType;
    }

    public String m_target_type;

    public String m_unrolled_type;

    public String m_unrolled_holder_type;

    public String m_unrolled_helper_type;

    public String m_definition_type;

    public String m_kind_type;

}