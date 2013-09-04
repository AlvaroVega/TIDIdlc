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
 * Interface with all the constants used in the Xml representation of the Idl
 * file.
 */

public interface Idl2XmlNames
{

    // Nodes
    static public String OMG_specification = "specification";

    static public String OMG_module = "module";

    static public String OMG_interface = "interface";

    static public String OMG_inheritance_spec = "inheritance_spec";

    static public String OMG_scoped_name = "scoped_name";

    static public String OMG_valuetype = "valuetype";

    static public String OMG_value_inheritance_spec = "value_inheritance_spec";

    static public String OMG_supports = "supports";

    static public String OMG_state_member = "state_member";

    static public String OMG_factory = "factory";

    static public String OMG_init_param_decl = "init_param_decl";

    static public String OMG_const_dcl = "const_dcl";

    static public String OMG_type = "type";

    static public String OMG_expr = "expr";

    static public String OMG_boolean_literal = "boolean_literal";

    static public String OMG_native = "native";

    static public String OMG_typedef = "typedef";

    static public String OMG_simple_declarator = "simple";

    static public String OMG_struct = "struct";

    static public String OMG_union = "union";

    static public String OMG_switch = "switch";

    static public String OMG_case = "case";

    static public String OMG_element_spec = "value";

    static public String OMG_enum = "enum";

    static public String OMG_enumerator = "enumerator";

    static public String OMG_sequence = "sequence";

    static public String OMG_array = "array";

    static public String OMG_attr_dcl = "attr_dcl";

    static public String OMG_attribute = "attribute";

    static public String OMG_exception = "exception";

    static public String OMG_op_dcl = "op_dcl";

    static public String OMG_returnType = "returnType";

    static public String OMG_parameter = "parameter";

    static public String OMG_raises = "raises";

    static public String OMG_context = "context";

    static public String OMG_fixed = "fixed";

    static public String OMG_integer_literal = "integer_literal";

    static public String OMG_string_literal = "string_literal";

    static public String OMG_wide_string_literal = "wide_string_literal";

    static public String OMG_character_literal = "character_literal";

    static public String OMG_wide_character_literal = "wide_character_literal";

    static public String OMG_fixed_pt_literal = "fixed_pt_literal";

    static public String OMG_floating_pt_literal = "floating_pt_literal";

    static public String OMG_or = "or";

    static public String OMG_xor = "xor";

    static public String OMG_and = "and";

    static public String OMG_shiftR = "shiftR";

    static public String OMG_shiftL = "shiftL";

    static public String OMG_plus = "plus";

    static public String OMG_minus = "minus";

    static public String OMG_times = "times";

    static public String OMG_div = "div";

    static public String OMG_mod = "mod";

    static public String OMG_not = "not";

    static public String OMG_pragma = "pragma";

    static public String OMG_pragma_value = "pragma_value";

    static public String OMG_prefix = "prefix";

    // Attributes
    static public String OMG_file_name = "file_name";
    
    static public String OMG_fwd = "forward";

    static public String OMG_abstract = "abstract";

    static public String OMG_local = "local";

    static public String OMG_true = "true";

    static public String OMG_false = "false";

    static public String OMG_name = "name";

    static public String OMG_custom = "custom";

    static public String OMG_truncatable = "truncatable";

    static public String OMG_boxed = "boxed";

    static public String OMG_kind = "kind";

    static public String OMG_public = "public";

    static public String OMG_private = "private";

    static public String OMG_value = "value";

    static public String OMG_string = "string";

    static public String OMG_wstring = "wstring";

    static public String OMG_readonly = "readonly";

    static public String OMG_oneway = "oneway";

    static public String OMG_float = "float";

    static public String OMG_double = "double";

    static public String OMG_longdouble = "long double";

    static public String OMG_short = "short";

    static public String OMG_long = "long";

    static public String OMG_longlong = "long long";

    static public String OMG_unsignedshort = "unsigned short";

    static public String OMG_unsignedlong = "unsigned long";

    static public String OMG_unsignedlonglong = "unsigned long long";

    static public String OMG_char = "char";

    static public String OMG_wchar = "wchar";

    static public String OMG_octet = "octet";

    static public String OMG_any = "any";

    static public String OMG_wide_char_type = "wchar";

    static public String OMG_boolean = "boolean";

    static public String OMG_Object = "Object";

    static public String OMG_ValueBase = "ValueBase";

    static public String OMG_TypeCode = "TypeCode";

    static public String OMG_AbstractBaseCode = "AbstractBaseCode";
    
    static public String OMG_AbstractBase = "AbstractBase";

    static public String OMG_Do_Not_Generate_Code = "Do_Not_Generate_Code";

    static public String OMG_variable_size_type = "VL_Type";

    static public String OMG_Holder_Complex = "complex";

    static public String OMG_Holder_Simple = "simple";

    static public String OMG_Holder_Ref = "reference";

    static public String OMG_Holder_Var = "variable";

    static public String OMG_Holder_String = "string";

    static public String OMG_Holder_Array = "array"; // DAVV

    static public String OMG_Holder_WString = "wstring"; // DAVV

    static public String OMG_Holder_Any = "any"; // DAVV

}