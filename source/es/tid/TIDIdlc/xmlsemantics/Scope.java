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

import org.w3c.dom.*;

import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

/**
 * Class for scopes. Finds ids in scopes and does scope conversions.
 */
public class Scope
{

    public static final int KIND_ROOT = 0;

    public static final int KIND_TYPE = 1;

    public static final int KIND_ELEMENT = 2;

    public static final int KIND_INTERFACE = 3;

    public static final int KIND_INTERFACE_FWD = 4;

    public static final int KIND_INTERFACE_ABS = 5;

    public static final int KIND_INTERFACE_FWD_ABS = 6;

    public static final int KIND_MODULE = 7;

    public static final int KIND_CONST = 8;

    public static final int KIND_VALUETYPE = 9;

    public static final int KIND_VALUETYPE_FWD = 10;

    public static final int KIND_VALUETYPE_ABS = 11;

    public static final int KIND_VALUETYPE_FWD_ABS = 12;

    public static final int KIND_ATTRIBUTE = 13;

    public static final int KIND_STATE_MEMBER = 14;

    public static final int KIND_OPERATION = 15;

    public static final int KIND_STRUCT = 16;

    public static final int KIND_STRUCT_FWD = 17;

    public static final int KIND_UNION = 18;

    public static final int KIND_UNION_FWD = 19;

    // Constructors

    /**
     * Constructor.
     * 
     * @param name
     *            Name of the scope
     */
    public Scope(String name)
        throws SemanticException
    {
        this(name, null, KIND_ROOT, null);
        st_root = this;
    }

    /**
     * Constructor.
     * 
     * @param name
     *            Name of the scope
     * @param parent
     *            Parent scope
     * @param kind
     *            Kind of scope
     * @param element
     *            XML (from Idl) element which defines the scope
     */
    public Scope(String name, Scope parent, int kind, Element element)
        throws SemanticException
    {
        m_parent = parent;
        m_name = name;
        m_kind = kind;
        m_element = element;
        m_names.put(m_name, new Integer(m_kind));
        if (m_parent != null)
            m_parent.addChild(this);
    }

    /**
     * Add a scoped name to this scope.
     * 
     * @param name
     *            Name of the element to be defined in this scope
     * @param kind
     *            Kind of element associated which the scoped name
     */
    public void add(String name, int kind)
        throws SemanticException
    {
        if (m_names.containsKey(name)) {
            int value = ((Integer) m_names.get(name)).intValue();
            if ((value != KIND_INTERFACE_FWD)
                && (value != KIND_INTERFACE_FWD_ABS)
                && (value != KIND_VALUETYPE_FWD)
                && (value != KIND_VALUETYPE_FWD_ABS)
                && (value != KIND_STRUCT_FWD) && (value != KIND_UNION_FWD)) {
                throw new SemanticException("Redefinition of " + name);
            }
            // CORBA 2.6 Architecture and Specification:
            //        3.7.4 "Multiple forward declarations of the same interface are
            // legal"
            //        3.8.4 "Multiple forward declarations of the same value type are
            // legal"
            /*
             * else if ((value - kind ==0)){ throw new
             * SemanticException("Redefinition of " + name); }
             */
            else if ((value - kind != 1) && (value - kind != 0)) {
                throw new SemanticException("Don�t match up the definition of "
                                            + name + " to forward declaration");
            }
        }
        m_names.put(name, new Integer(kind));
    }

    /**
     * Create an inheritance relationship between scopes.
     * 
     * @param other
     *            The scope that this scope inherits from.
     */
    public void addInheritance(Scope other)
        throws SemanticException
    {
        this.m_parent_scope.addElement(other.m_parent);
    	for (int i=0;i<m_parent_scope.size()-1;i++) {
    		if ((other.m_parent.equals(m_parent_scope.elementAt(i)))){
    			if (m_inherits.contains(other)) {
    				throw new SemanticException(m_name + " inherits twice from "
    											+ other.getName());
    			}
    		}
    	}

        m_inherits.addElement(other);
    }

    // Finding scopes

    /**
     * @param name
     *            name of scope to find among the child scopes
     */
    public Scope getScope(String name)
        throws SemanticException
    {
        Vector v = convert(name);
        Scope scope = this;
        for (int i = 0; i < v.size(); i++) {
            scope = scope.getChild((String) v.elementAt(i));
            if (scope == null)
                throw new SemanticException("Invalid scope: " + name);
        }
        return scope;
    }

    /**
     * @param name
     *            name of scope to be resolved
     */
    public String getCompleteName(String name)
        throws SemanticException
    {
        // Id del tipo CORBA::X -> quitamos el prefijo CORBA
        /*
         * if (name.startsWith("CORBA::")) { name = "org::omg::" + name; }
         */
        Vector v = getVectorOfScope(name);
        if (!v.lastElement().toString().equals(name)) {
            if (name.startsWith(SEP))
                return convert(v) + name;
            else
                return convert(v) + SEP + name;
        } else {
            return convert(v);
        }
    }

    public Scope getScopeOfType(String name)
        throws SemanticException
    {
        // Id del tipo CORBA::X -> quitamos el prefijo CORBA
        /*
         * if (name.startsWith("CORBA::")) { name = "org::omg::" + name; }
         */
        Vector v = getVectorOfScope(name);
        if (v.lastElement().toString().equals(name)) {
            throw new SemanticException(
                                        "The scoped name '"
                                                                                                                                                                + name
                                                                                                                                                                + "' must be a previously defined integer, char, boolean or enum type");
        } else {
            return (Scope) v.lastElement();
        }

    }

    /**
     * @return returns the complete scope name
     */
    public String getCompleteName()
        throws SemanticException
    {
        Vector v = getParents();
        return convert(v);
    }

    /**
     * @param name
     *            name of identifier (qualified or not)
     * @return if name is in this scope
     */
    public boolean isInThisScope(String name)
    {
        Vector scope = convert(name);
        return isInThisScope(scope);
    }

    /**
     * @param name
     *            name of identifier (qualified or not)
     * @return a name not in this scope preceeded by '_'
     */
    // by MACP
    public String getNameNotInThisScope(String name)
    {
        String value = "_" + name; // ever created with a '_' at the begining;
        while (isInThisScope(value))
            value = "_" + value;
        return value;

    }

    /**
     * Converts Scope name to a Vector of strings
     */
    public static Vector convert(String scopedName)
    {
        return convert(scopedName, SEP);
    }

    /**
     * Converts Scope name to a Vector of strings
     */
    private static Vector convert(String scopedName, String separator)
    {
        Vector v = new Vector();
        StringTokenizer tok = new StringTokenizer(scopedName, separator);
        while (tok.hasMoreTokens()) {
            v.addElement(tok.nextToken());
        }
        return v;
    }

    /**
     * Converts a complete scope name to a Vector of scopes
     */
    public static Vector convertToScopes(String scopedName)
    {
        Vector v = new Vector();
        Scope scope = st_root;
        StringTokenizer tok = new StringTokenizer(scopedName, SEP);
        while (tok.hasMoreTokens()) {
            String childName = tok.nextToken();
            Scope child = scope.getChild(childName);
            if (child == null)
                v.addElement(childName);
            else {
                v.addElement(child);
                scope = child;
            }
        }
        return v;
    }

    /**
     * Converts a Vector of strings to a Scope name
     */
    public static String convert(Vector scope)
    {
        String s = "";
        for (int i = 0; i < scope.size(); i++) {
            if (i > 0)
                s += SEP;
            s += scope.elementAt(i);
        }
        return s;
    }

    /**
     * Gets the kind of scope of a partial scope name
     */
    public static int getKind(String partialName)
    {
        try {
            Scope scope = getGlobalScope(partialName);
            return scope.getKind();
        }
        catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    // GET methods

    public String getName()
    {
        return m_name;
    }

    public int getKind()
    {
        return m_kind;
    }

    public Scope getChild(String name)
    {
        Scope copy = null;
        for (int i = 0; i < m_childs.size(); i++) {
            Scope s = (Scope) m_childs.elementAt(i);
            if (s.getName().equals(name)) {
                if (s.getKind() == KIND_INTERFACE_FWD
                    || s.getKind() == KIND_INTERFACE_FWD_ABS || // Si tenemos
                                                                // forward,
                                                                // preferimos
                                                                // usar su
                                                                // declaracion
                                                                // si ya existe
                    s.getKind() == KIND_VALUETYPE_FWD
                    || s.getKind() == KIND_VALUETYPE_FWD_ABS) {
                    copy = s;
                } else
                    return s;
            }
        }
        if (copy != null)
            return copy;
        else
            return null;
    }

    public Scope getParent()
    {
        return m_parent;
    }

    public Element getElement()
    {
        return m_element;
    }

    public void setElement(Element el)
    {
        m_element = el;
    }

    public Vector getInheritance()
    {
        Vector v = new Vector();
        getInheritance2(v);
        return v;
    }

    private void getInheritance2(Vector v)
    {
        for (int i = 0; i < m_inherits.size(); i++) {
            Scope scope = (Scope) m_inherits.elementAt(i);
            v.addElement(scope); // add actual scope
            scope.getInheritance2(v);
        }
    }

    public Vector getValuetypeInheritance()
    {
        return m_inherits;
    }

    // Printing & dumping

    public void dump()
    {
        System.out.println(this);
        dump("  ");
    }

    public String toString()
    {
        return m_name;
    }

    // Private methods

    private Vector getVectorOfScope(String name)
        throws SemanticException
    {
        if (isInThisScope(name))
            return getParents(); // Encontrado

        Scope inheritedScope = getVectorOfScopeAtInheritance(name);
        if (inheritedScope != null)
            return inheritedScope.getParents();

        if (m_parent == null)
            throw new SemanticException(name + " not in Scope");

        return m_parent.getVectorOfScope(name);
    }

    private Scope getVectorOfScopeAtInheritance(String name)
        throws SemanticException
    {
        // comprobamos los scopes heredados tanto DIRECTA como
        // INDIRECTAMENTE
        Scope goodScope = null;
        for (int i = 0; i < m_inherits.size(); i++) {
            Scope scope = (Scope) m_inherits.elementAt(i);
            if (!scope.isInThisScope(name)) // (1)
                scope = scope.getVectorOfScopeAtInheritance(name); // (2)
            // he aqui el meollo para la herencia indirecta
            if (scope != null) { // es decir, o (1) es cierta, o (2)
                                 // devuelve un scope que cumple (1)
                if (goodScope != null && goodScope != scope) // DAVV - ojo: es
                                                             // posible la
                                                             // herencia
                                                             // m�ltiple!!
                    throw new SemanticException("Ambiguous use of: " + name);
                else
                    goodScope = scope;
            }
        }
        return goodScope;
    }

    private void dump(String ident)
    {
        for (int i = 0; i < m_childs.size(); i++) {
            Scope scope = (Scope) m_childs.elementAt(i);
            System.out.print(ident);
            System.out.println(scope);
            scope.dump(ident + "  ");
        }
    }

    private void addChild(Scope child)
        throws SemanticException
    {
        m_childs.addElement(child);
        add(child.getName(), child.getKind());
    }

    private Vector getParents()
    {
        if (m_parent == null) {
            Vector v = new Vector();
            v.addElement(this);
            return v;
        } else {
            Vector v = m_parent.getParents();
            v.addElement(this);
            return v;
        }
    }

    private boolean isInThisScope(Vector name)
    {
        if (name.size() == 1) {
            if (m_names.containsKey(name.elementAt(0))) {
                return true; // Found
            }
        }
        // Get all the child scopes and check if one
        // matches the first element of the scoped name
        for (int i = 0; i < m_childs.size(); i++) {
            Scope scope = (Scope) m_childs.elementAt(i);
            // Ingnore interface forward declarations
            if (scope.getKind() != KIND_INTERFACE_FWD
                && scope.getKind() != KIND_INTERFACE_FWD_ABS
                && scope.getName().equals(name.elementAt(0))) {
                name.removeElementAt(0);
                if (scope.isInThisScope(name)) {
                    return true; // Found
                }
            }
        }
        return false;
    }

    public static Scope getGlobalScope(String name)
    {
        Vector scope = convert(name);
        return st_root.getGlobalScope(scope);
    }

    public static Scope getGlobalScopeInterface(String name)
    {
        Vector scope = convert(name);
        return st_root.getGlobalScopeInterface(scope);
    }

    public static Scope getGlobalScope(String name, String separator)
    {
        Vector scope = convert(name, separator);
        return st_root.getGlobalScope(scope);
    }

    private Scope getGlobalScope(Vector name)
    {
        if ((m_childs.size() == 0) && (name.size() == 0)) {
            return this;
        }
        for (int i = 0; i < m_childs.size(); i++) {
            Scope scope = (Scope) m_childs.elementAt(i);
            if (name.size() > 0 && scope.getName().equals(name.elementAt(0))) {
                // If there's no more elements in Vector, and the
                // name of the scope equals the last element, the scope is
                // returned, although
                // the scope has more childs
                if (name.size() == 1) {
                    return scope;
                }
                name.removeElementAt(0);
                return scope.getGlobalScope(name);
            }
        }
        return null;
    }

    // Returns the global scope asociated with a scoped identifier
    // It ignores the forward interface declarations
    private Scope getGlobalScopeInterface(Vector name)
    {
        if ((m_childs.size() == 0) && (name.size() == 0)) {
            return this;
        }
        for (int i = 0; i < m_childs.size(); i++) {
            Scope scope = (Scope) m_childs.elementAt(i);
            if ((name.size() > 0)
                && (scope.getName().equals(name.elementAt(0)))
                && (scope.getKind() != KIND_INTERFACE_FWD)
                && (scope.getKind() != KIND_INTERFACE_FWD_ABS)
                && (scope.getKind() != KIND_VALUETYPE_FWD)
                && (scope.getKind() != KIND_VALUETYPE_FWD_ABS)) {
                // If there's no more elements in Vector, and the
                // name of the scope equals the last element, the scope is
                // returned, although
                // the scope has more childs
                if (name.size() == 1) {
                    return scope;
                }
                name.removeElementAt(0);
                return scope.getGlobalScopeInterface(name);
            }
        }
        return null;
    }

    public static Scope getGlobalScopeInterface(String name, String separator)
    {
        Vector scope = convert(name, separator);
        return st_root.getGlobalScopeInterface(scope);
    }

    public static void removePrefix(Vector v) //throws SemanticException
    {
        boolean hasCorba = false;
        int i = 0;
        while ((!hasCorba) && (i < v.size())) {
            Scope scope = (Scope) v.elementAt(i);
            if (scope.m_name.equals("CORBA"))
                hasCorba = true;
            else
                i++;
        }
        if (hasCorba) {
            for (i = 0; i < v.size();) {
                Scope scope = (Scope) v.elementAt(i);
                if (scope.m_name.equals("org"))
                    break;
                else
                    v.removeElementAt(i);
            }
        }
    }

    public boolean equals(Object o)
    {
        if (o instanceof Scope) {
            return this.m_name.equals(((Scope) o).m_name);
        } else
            return false;
    }

    private static Scope st_root; // Root node

    public static final String SEP = "::"; // separator

    private Scope m_parent;

    private Vector m_childs = new Vector();

    private Vector m_inherits = new Vector(); // Scope of inherited classes or
                                              // valuetypes(if needed)
    private Vector m_parent_scope = new Vector();

    private String m_name; // name of scope

    private int m_kind; // kind of scope

    private Element m_element = null;

    private Hashtable m_names = new Hashtable(); // identifiers in scope
}