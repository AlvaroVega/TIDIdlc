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

import java.util.Hashtable;
import java.util.Enumeration;
import java.util.StringTokenizer;
import org.w3c.dom.*;

/**
 * Class for repository Ids. Manages version & prefix pragmas, and generates
 * repository Ids for helper classes.
 */
public class RepositoryIdManager
{

    private RepositoryIdManager()
    {}

    private Hashtable m_ids = new Hashtable();

    private Hashtable m_ids_scope = new Hashtable();

    private static RepositoryIdManager st_the_instance = null;

    public static RepositoryIdManager getInstance()
    {
        if (st_the_instance == null)
            st_the_instance = new RepositoryIdManager();
        return st_the_instance;
    }

    public static void Shutdown()
	{
    	st_the_instance = null;
	}

    public String get(Element el)
    {
        RepositoryId id = (RepositoryId) m_ids.get(el);
        if (id != null) {
            return id.toString();
        } else
            return "unknownObject";
    }

    /**
     * Set the Repository Id based in a prefix and a scoped name.
     * 
     * @param el
     *            Document node.
     * @param scope
     *            Scope to which the node belongs.
     * @param name
     *            Name of the idl entity for the Repository Id.
     * @param prefix
     *            Prefix of the idl entity for the Repository Id.
     */
    public void setName(Element el, Scope scope, String name, String prefix)
        throws SemanticException
    {
        String scopeName = scope.getCompleteName();
        String completeScopeName = scopeName + Scope.SEP + name;
        RepositoryId id = (RepositoryId) m_ids.get(el);
        if (id != null) {
            throw new SemanticException("Repository ID redefinition for: "
                                        + id.m_name);
        }
        id = new RepositoryId();
        //String path = getPath(completeScopeName);
        if (prefix.equals("")) {
            //String scopeName = scope.getCompleteName();
            //String completeScopeName = scopeName + Scope.SEP + name;
            String path = getPath(completeScopeName);
            id.m_name = path;
        } else {
            id.m_name = prefix + "/" + name;
        }
        m_ids.put(el, id);
        m_ids_scope.put(completeScopeName, el);
    }

    /**
     * Set the Repository Id based in a ID pragma.
     * 
     * @param scope
     *            Scope where the pragma is found.
     * @param name
     *            Name of the idl entity.
     * @param idText
     *            Complete text of the Repository Id.
     */
    public void setId(Scope scope, String name, String idText)
        throws SemanticException
    {
        try {
            //String completeScopeName = scope.getCompleteName() + Scope.SEP +
            // name;
            String completeScopeName = scope.getCompleteName(name);
            Element el = (Element) m_ids_scope.get(completeScopeName);
            RepositoryId id = (RepositoryId) m_ids.get(el);
            if (id != null) {
                if (id.m_already_set) {
                    throw new SemanticException(
                                  "Repository ID redefinition for: "
                                  + completeScopeName);
                }	
            } else {
                throw new SemanticException("Repository ID not found!!!: "
                                            + completeScopeName);
            }
            id.m_already_set = true;

            idText = idText.trim();
            String idTextNoQuotes = idText.substring(1, idText.length() - 1);
            StringTokenizer tok = new StringTokenizer(idTextNoQuotes, ":");
            String kind = tok.nextToken();
            if (!kind.equals("IDL") && !kind.equals("RMI")) {
                throw new SemanticException("Invalid RepositoryId format: "
                                            + id);
            }
            id.m_kind = kind;
            String completeName = tok.nextToken();
            id.m_name = completeName;
            String versionNumber = tok.nextToken();
            id.setVersion(versionNumber);
        }
        catch (Exception e) {
            //e.printStackTrace();
            throw new SemanticException("Invalid RepositoryId format: "
                                        + idText + "\n" + e.getMessage());
        }
    }

    /**
     * Set the version of an idl entity.
     * 
     * @param scope
     *            Scope where the pragma is found.
     * @param name
     *            Name of the idl entity.
     * @param versionNumber
     *            Version for the Repository Id.
     */
    public void setVersion(Scope scope, String name, String versionNumber)
        throws SemanticException
    {
        String completeScopeName = scope.getCompleteName(name);
        Element el = (Element) m_ids_scope.get(completeScopeName);
        RepositoryId id = (RepositoryId) m_ids.get(el);
        if (id != null) {
            if (id.isVersionSet()) {
                throw new SemanticException("Version redefinition for: "
                                            + completeScopeName);
            }
            id.setVersion(versionNumber);
        }
    }

    /**
     * Dumps the contents of the RepositoryId Manager.
     */
    public void dump()
    {
        Enumeration keys = m_ids.keys();
        Enumeration elements = m_ids.elements();
        for (; keys.hasMoreElements();) {
            System.out.print(keys.nextElement());
            RepositoryId info = (RepositoryId) elements.nextElement();
            System.out.print(" -> ");
            System.out.println(info);
        }
    }

    private static String getPath(String name)
    {
        String s = "";
        StringTokenizer tok = new StringTokenizer(name, Scope.SEP);
        while (tok.hasMoreTokens()) {
            Object obj = tok.nextToken();
            s += obj.toString();
            if (tok.hasMoreTokens()) {
                s += "/";
            }
        }
        return s;
    }

}

class RepositoryId
{

    public String m_name;

    public boolean m_already_set = false; // true if has been created by a
                                          // #pragma ID directive

    private boolean m_version_is_set = false; // true if has been created by a
                                              // #pragma version directive

    private int m_version_major = 1;

    private int m_version_minor = 0;

    private String m_version_rmi = "0000000000000000"; // (for interoperability
                                                       // with Java RMI)

    public String m_kind = "IDL"; // 'IDL' or 'RMI' (for interoperability with
                                  // Java RMI)

    public boolean isVersionSet()
    {
        return m_version_is_set;
    }

    public void setVersion(String versionNumber)
        throws SemanticException
    {
        try {
            if (m_kind.equals("IDL")) {
                StringTokenizer tok = new StringTokenizer(versionNumber, ".",
                                                          false);
                m_version_major = Integer.parseInt(tok.nextToken().trim());
                m_version_minor = Integer.parseInt(tok.nextToken().trim());
                if (tok.hasMoreTokens())
                    throw new SemanticException("Too much version numbers.");
            } else if (m_kind.equals("RMI")) {
                m_version_rmi = versionNumber;
            }
            m_version_is_set = true;
        }
        catch (Exception e) {
            throw new SemanticException("Invalid version number: "
                                        + versionNumber);
        }
    }

    public String toString()
    {
        //return "IDL:" + name + ":" + version_major + "." + version_minor;
        if (m_kind.equals("IDL"))
            return m_kind + ":" + m_name + ":" + m_version_major + "."
                   + m_version_minor;
        else
            return m_kind + ":" + m_name + ":" + m_version_rmi;
    }
}