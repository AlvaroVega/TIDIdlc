/*
* MORFEO Project
* http://www.morfeo-project.org
*
* Component: TIDIdlc
* Programming Language: Java
*
* File: $Source$
* Version: $Revision: 330 $
* Date: $Date: 2012-02-27 18:02:15 +0100 (Mon, 27 Feb 2012) $
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

package es.tid.TIDIdlc.idl2xml;

import es.tid.TIDIdlc.CompilerConf;

import java.util.Hashtable;

public class IncludeORB
{

    private static Hashtable buffer_table = new Hashtable();

    public static String getFile(String name)
    {
        if (buffer_table.containsKey(name))
            return (buffer_table.get(name)).toString();
        else {
            String pack = "";
            if (CompilerConf.getCompilerType().equals("Java"))
                pack = "org.omg";
            StringBuffer buffer = new StringBuffer();
            if (name.equals("orb.idl")) {
                /* CORBA 2.5 MODULE */

                CompilerConf.getModule_Packaged().addElement("CORBA");
                CompilerConf.getPackageToTable().put("CORBA", pack);

                buffer.append("\n#pragma prefix \"omg.org\"");

                buffer.append("\n#ifndef __org_omg_CORBA__"); // por posibles
                                                              // inclusiones
                                                              // multiples de
                                                              // orb.idl
                buffer.append("\n#define __org_omg_CORBA__");

                buffer.append("\n module CORBA {");
                buffer.append("\n  local interface TypeCode;");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  interface Contained;");
                buffer.append("\n  interface Repository;");
                buffer.append("\n  interface Container;");
                buffer.append("\n  interface ModuleDef;");
                buffer.append("\n  interface ConstantDef;");
                }
                buffer.append("\n  interface IDLType;");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  interface StructDef;");
                buffer.append("\n  interface UnionDef;");
                buffer.append("\n  interface EnumDef;");
                buffer.append("\n  interface AliasDef;");
                buffer.append("\n  interface InterfaceDef;");
                buffer.append("\n  interface ExceptionDef;");
                buffer.append("\n  interface NativeDef;");
                buffer.append("\n  typedef sequence <InterfaceDef> InterfaceDefSeq;");
                buffer.append("\n  interface ValueDef;");
                buffer.append("\n  typedef sequence <ValueDef> ValueDefSeq;");
                buffer.append("\n  interface ValueBoxDef;");
                buffer.append("\n  typedef sequence <Contained> ContainedSeq;");
                }
                buffer.append("\n  typedef sequence<any> AnySeq;");
                buffer.append("\n  typedef sequence<boolean> BooleanSeq;");
                buffer.append("\n  typedef sequence<char> CharSeq;");
                buffer.append("\n  typedef sequence<wchar> WCharSeq;");
                buffer.append("\n  typedef sequence<octet> OctetSeq;");
                buffer.append("\n  typedef sequence<short> ShortSeq;");
                buffer.append("\n  typedef sequence<unsigned short> UShortSeq;");
                buffer.append("\n  typedef sequence<long> LongSeq;");
                buffer.append("\n  typedef sequence<unsigned long> ULongSeq;");
                buffer.append("\n  typedef sequence<long long> LongLongSeq;");
                buffer.append("\n  typedef sequence<unsigned long long> ULongLongSeq;");
                buffer.append("\n  typedef sequence<long double> LongDoubleSeq;");
                buffer.append("\n  typedef sequence<float> FloatSeq;");
                buffer.append("\n  typedef sequence<double> DoubleSeq;");
                buffer.append("\n  typedef sequence<string> StringSeq;");
                buffer.append("\n  typedef sequence<wstring> WStringSeq;");

                buffer.append("\n  typedef string RepositoryId;");
                ;
                buffer.append("\n  typedef string ScopedName;");
                buffer.append("\n  typedef string ObjectId;");
                buffer.append("\n  typedef sequence <ObjectId> ObjectIdList;");
                buffer.append("\n  typedef string Identifier;");

                buffer.append("\n  const unsigned long OMGVMCID = 1330446336;");

                buffer.append("\n  native AbstractBase;");
                buffer.append("\n  native ValueFactory;");

                buffer.append("\n  enum SetOverrideType {SET_OVERRIDE, ADD_OVERRIDE};");

                buffer.append("\n  interface ORB { // PIDL");
                buffer.append("\n    typedef string ObjectId;");
                buffer.append("\n    typedef sequence <ObjectId> ObjectIdList;");
                buffer.append("\n    exception InvalidName {};");
                buffer.append("\n  };");

                ////////////////////////
                // ServiceInformation //
                ////////////////////////

                buffer.append("\n  typedef unsigned short ServiceType;");
                buffer.append("\n  typedef unsigned long ServiceOption;");
                buffer.append("\n  typedef unsigned long ServiceDetailType;");

                buffer.append("\n  const ServiceType Security = 1;");

                buffer.append("\n  struct ServiceDetail {");
                buffer.append("\n    ServiceDetailType service_detail_type;");
                buffer.append("\n    sequence <octet> service_detail;");
                buffer.append("\n  };");

                buffer.append("\n  struct ServiceInformation {");
                buffer.append("\n    sequence <ServiceOption> service_options;");
                buffer.append("\n    sequence <ServiceDetail> service_details;");
                buffer.append("\n  };");

                /////////////////////////////////
                // Policy Basic IDL definition //
                /////////////////////////////////

                buffer.append("\n  typedef unsigned long PolicyType;");

                buffer.append("\n  interface Policy {");
                buffer.append("\n    readonly attribute PolicyType policy_type;");
                buffer.append("\n    Policy copy();");
                buffer.append("\n    void destroy();");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence<Policy> PolicyList;");

                buffer.append("\n  typedef short PolicyErrorCode;");

                buffer.append("\n  const PolicyErrorCode BAD_POLICY = 0;");
                buffer.append("\n  const PolicyErrorCode UNSUPPORTED_POLICY = 1;");
                buffer.append("\n  const PolicyErrorCode BAD_POLICY_TYPE = 2;");
                buffer.append("\n  const PolicyErrorCode BAD_POLICY_VALUE = 3;");
                buffer.append("\n  const PolicyErrorCode UNSUPPORTED_POLICY_VALUE = 4;");

                buffer.append("\n  exception PolicyError{PolicyErrorCode reason;};");

                ////////////////////////
                // DomainManager //
                ////////////////////////

                buffer.append("\n  interface DomainManager {");
                buffer.append("\n    Policy get_domain_policy (in PolicyType policy_type);");
                buffer.append("\n  };");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  const PolicyType SecConstruction = 11;");

                buffer.append("\n  interface ConstructionPolicy: Policy{");
                buffer.append("\n    void make_domain_manager(in InterfaceDef object_type,");
                buffer.append("\n                             in boolean constr_policy);");
                buffer.append("\n  };");
                }
                buffer.append("\n  typedef sequence <DomainManager> DomainManagersList;  ");

                /////////////
                // Current //
                /////////////

                buffer.append("\n  interface Current { };");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  valuetype StringValue string;");
                buffer.append("\n  valuetype WStringValue wstring;");
                }
                ///////////////////////////
                // Interface Repository //
                ///////////////////////////

                buffer.append("\n  enum DefinitionKind {");
                buffer.append("\n    dk_none, dk_all, dk_Attribute, dk_Constant, dk_Exception, dk_Interface, ");
                buffer.append("\n    dk_Module, dk_Operation, dk_Typedef, dk_Alias, dk_Struct, dk_Union, dk_Enum,");
                buffer.append("\n    dk_Primitive, dk_String, dk_Sequence, dk_Array, dk_Repository, dk_Wstring, ");
                buffer.append("\n    dk_Fixed, dk_Value, dk_ValueBox, dk_ValueMember, dk_Native");
                buffer.append("\n };");

                buffer.append("\n  interface IRObject {");
                buffer.append("\n    readonly attribute DefinitionKind def_kind;");
                buffer.append("\n    void destroy ();");
                buffer.append("\n  };");

                buffer.append("\n  typedef string VersionSpec;");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  interface Contained : IRObject {");
                buffer.append("\n    attribute RepositoryId id;");
                buffer.append("\n    attribute Identifier name;");
                buffer.append("\n    attribute VersionSpec version;");

                // read interface
                buffer.append("\n    readonly attribute Container defined_in;");
                buffer.append("\n    readonly attribute ScopedName absolute_name;");
                buffer.append("\n    readonly attribute Repository containing_repository;");

                buffer.append("\n    struct Description {");
                buffer.append("\n      DefinitionKind kind;");
                buffer.append("\n      any value;");
                buffer.append("\n    };");

                buffer.append("\n    Description describe ();");

                // write interface
                buffer.append("\n    void move ( in Container new_container,");
                buffer.append("\n                in Identifier new_name,");
                buffer.append("\n                in VersionSpec new_version);");
                buffer.append("\n  };");
                }
                buffer.append("\n  struct StructMember {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n    IDLType type_def;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <StructMember> StructMemberSeq;");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  ");
                buffer.append("\n  struct Initializer {");
                buffer.append("\n    StructMemberSeq members;");
                buffer.append("\n    Identifier name;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <Initializer> InitializerSeq;");
                }
                buffer.append("\n  struct UnionMember {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    any label;");
                buffer.append("\n    CORBA::TypeCode type;");
                buffer.append("\n    IDLType type_def;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <UnionMember> UnionMemberSeq;");
                buffer.append("\n  typedef sequence <Identifier> EnumMemberSeq;");

                if (! CompilerConf.getMinimun()) {

                buffer.append("\n  interface Container : IRObject {");
                buffer.append("\n    // read interface");
                buffer.append("\n    Contained lookup (in ScopedName search_name);");

                buffer.append("\n    ContainedSeq contents (");
                buffer.append("\n      in DefinitionKind limit_type,");
                buffer.append("\n      in boolean exclude_inherited");
                buffer.append("\n    );");

                buffer.append("\n    ContainedSeq lookup_name (");
                buffer.append("\n      in Identifier search_name,");
                buffer.append("\n      in long levels_to_search,");
                buffer.append("\n      in DefinitionKind limit_type,");
                buffer.append("\n      in boolean exclude_inherited");
                buffer.append("\n    );");

                buffer.append("\n    struct Description {");
                buffer.append("\n      Contained contained_object;");
                buffer.append("\n      DefinitionKind kind;");
                buffer.append("\n      any value;");
                buffer.append("\n    };");

                buffer.append("\n    typedef sequence<Description> DescriptionSeq;");

                buffer.append("\n    DescriptionSeq describe_contents (");
                buffer.append("\n      in DefinitionKind limit_type,");
                buffer.append("\n      in boolean exclude_inherited,");
                buffer.append("\n      in long max_returned_objs");
                buffer.append("\n    );");

                buffer.append("\n    // write interface");
                buffer.append("\n    ModuleDef create_module (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version");
                buffer.append("\n    );");

                buffer.append("\n    ConstantDef create_constant (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType type,");
                buffer.append("\n      in any value");
                buffer.append("\n    );");

                buffer.append("\n    StructDef create_struct (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in StructMemberSeq members");
                buffer.append("\n    );");

                buffer.append("\n    UnionDef create_union (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType discriminator_type,");
                buffer.append("\n      in UnionMemberSeq members");
                buffer.append("\n    );");

                buffer.append("\n    EnumDef create_enum (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in EnumMemberSeq members");
                buffer.append("\n    );");

                buffer.append("\n    AliasDef create_alias (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType original_type");
                buffer.append("\n    );");

                buffer.append("\n    InterfaceDef create_interface (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in InterfaceDefSeq base_interfaces,");
                buffer.append("\n      in boolean is_abstract");
                buffer.append("\n    );");

                buffer.append("\n    ValueDef create_value(");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in boolean is_custom,");
                buffer.append("\n      in boolean is_abstract,");
                buffer.append("\n      in ValueDef base_value,");
                buffer.append("\n      in boolean is_truncatable,");
                buffer.append("\n      in ValueDefSeq abstract_base_values,");
                buffer.append("\n      in InterfaceDefSeq supported_interfaces,");
                buffer.append("\n      in InitializerSeq initializers");
                buffer.append("\n    );");

                buffer.append("\n    ValueBoxDef create_value_box(");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType original_type_def");
                buffer.append("\n    );");

                buffer.append("\n    ExceptionDef create_exception(");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in StructMemberSeq members");
                buffer.append("\n    );");

                buffer.append("\n    NativeDef create_native(");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version);");
                buffer.append("\n  };");
                }

                buffer.append("\n  interface IDLType : IRObject {");
                buffer.append("\n    readonly attribute TypeCode type;");
                buffer.append("\n  };");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  interface PrimitiveDef;");
                buffer.append("\n  interface StringDef;");
                buffer.append("\n  interface SequenceDef;");
                buffer.append("\n  interface ArrayDef;");
                buffer.append("\n  interface WstringDef;");
                buffer.append("\n  interface FixedDef;");

                buffer.append("\n  enum PrimitiveKind {");
                buffer.append("\n    pk_null, pk_void, pk_short, pk_long, pk_ushort, pk_ulong,");
                buffer.append("\n    pk_float, pk_double, pk_boolean, pk_char, pk_octet,");
                buffer.append("\n    pk_any, pk_TypeCode, pk_Principal, pk_string, pk_objref,");
                buffer.append("\n    pk_longlong, pk_ulonglong, pk_longdouble,");
                buffer.append("\n    pk_wchar, pk_wstring, pk_value_base");
                buffer.append("\n  };");

                buffer.append("\n  interface Repository : Container {");
                // read interface
                buffer.append("\n    Contained lookup_id (in RepositoryId search_id);");

                buffer.append("\n    TypeCode get_canonical_typecode(in TypeCode tc);");

                buffer.append("\n    PrimitiveDef get_primitive (in PrimitiveKind kind);");

                // write interface
                buffer.append("\n    StringDef create_string (in unsigned long bound);");

                buffer.append("\n    WstringDef create_wstring (in unsigned long bound);");

                buffer.append("\n    SequenceDef create_sequence (");
                buffer.append("\n      in unsigned long bound,");
                buffer.append("\n      in IDLType element_type");
                buffer.append("\n    );");

                buffer.append("\n    ArrayDef create_array (");
                buffer.append("\n      in unsigned long length,");
                buffer.append("\n      in IDLType element_type");
                buffer.append("\n    );");

                buffer.append("\n    FixedDef create_fixed (");
                buffer.append("\n      in unsigned short digits,");
                buffer.append("\n      in short scale");
                buffer.append("\n    );");
                buffer.append("\n  };");

                buffer.append("\n  interface ModuleDef : Container, Contained {");
                buffer.append("\n  };");

                buffer.append("\n  struct ModuleDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n  };");

                buffer.append("\n  interface ConstantDef : Contained {");
                buffer.append("\n    readonly attribute TypeCode type;");
                buffer.append("\n    attribute IDLType type_def;");
                buffer.append("\n    attribute any value;");
                buffer.append("\n  };");

                buffer.append("\n  struct ConstantDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n    any value;");
                buffer.append("\n  };");

                buffer.append("\n  interface TypedefDef : Contained, IDLType {");
                buffer.append("\n  };");

                buffer.append("\n  struct TypeDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n  };");

                buffer.append("\n  interface StructDef : TypedefDef, Container {");
                buffer.append("\n    attribute StructMemberSeq members;");
                buffer.append("\n  };");

                buffer.append("\n  interface UnionDef : TypedefDef, Container {");
                buffer.append("\n    readonly attribute TypeCode discriminator_type;");
                buffer.append("\n    attribute IDLType discriminator_type_def;");
                buffer.append("\n    attribute UnionMemberSeq members;");
                buffer.append("\n  };");

                buffer.append("\n  interface EnumDef : TypedefDef {");
                buffer.append("\n    attribute EnumMemberSeq members;");
                buffer.append("\n  };");

                buffer.append("\n  interface AliasDef : TypedefDef {");
                buffer.append("\n    attribute IDLType original_type_def;");
                buffer.append("\n  };");

                buffer.append("\n  interface NativeDef : TypedefDef {");
                buffer.append("\n  };");

                buffer.append("\n  interface PrimitiveDef: IDLType {");
                buffer.append("\n    readonly attribute PrimitiveKind kind;");
                buffer.append("\n  };");

                buffer.append("\n  interface StringDef : IDLType {");
                buffer.append("\n    attribute unsigned long bound;");
                buffer.append("\n  };");

                buffer.append("\n  interface WstringDef : IDLType {");
                buffer.append("\n    attribute unsigned long bound;");
                buffer.append("\n  };");

                buffer.append("\n  interface FixedDef : IDLType {");
                buffer.append("\n    attribute unsigned short digits;");
                buffer.append("\n    attribute short scale;");
                buffer.append("\n  };");

                buffer.append("\n  interface SequenceDef : IDLType {");
                buffer.append("\n    attribute unsigned long bound;");
                buffer.append("\n    readonly attribute TypeCode element_type;");
                buffer.append("\n    attribute IDLType element_type_def;");
                buffer.append("\n  };");

                buffer.append("\n  interface ArrayDef : IDLType {");
                buffer.append("\n    attribute unsigned long length;");
                buffer.append("\n    readonly attribute TypeCode element_type;");
                buffer.append("\n    attribute IDLType element_type_def;");
                buffer.append("\n  };");

                buffer.append("\n  interface ExceptionDef : Contained, Container {");
                buffer.append("\n    readonly attribute TypeCode type;");
                buffer.append("\n    attribute StructMemberSeq members;");
                buffer.append("\n  };");

                buffer.append("\n  struct ExceptionDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n  };");

                buffer.append("\n  enum AttributeMode {ATTR_NORMAL, ATTR_READONLY};");

                buffer.append("\n  interface AttributeDef : Contained {");
                buffer.append("\n    readonly attribute TypeCode type;");
                buffer.append("\n    attribute IDLType type_def;");
                buffer.append("\n    attribute AttributeMode mode;");
                buffer.append("\n  };");

                buffer.append("\n  struct AttributeDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n    AttributeMode mode;");
                buffer.append("\n  };");

                buffer.append("\n  enum OperationMode {OP_NORMAL, OP_ONEWAY};");

                buffer.append("\n  enum ParameterMode {PARAM_IN, PARAM_OUT, PARAM_INOUT};");

                buffer.append("\n  struct ParameterDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n    IDLType type_def;");
                buffer.append("\n    ParameterMode mode;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <ParameterDescription> ParDescriptionSeq;");
                buffer.append("\n  typedef Identifier ContextIdentifier;");
                buffer.append("\n  typedef sequence <ContextIdentifier> ContextIdSeq;");
                buffer.append("\n  typedef sequence <ExceptionDef> ExceptionDefSeq;");
                buffer.append("\n  typedef sequence <ExceptionDescription> ExcDescriptionSeq;");

                buffer.append("\n  interface OperationDef : Contained {");
                buffer.append("\n    readonly attribute TypeCode result;");
                buffer.append("\n    attribute IDLType result_def;");
                buffer.append("\n    attribute ParDescriptionSeq params;");
                buffer.append("\n    attribute OperationMode mode;");
                buffer.append("\n    attribute ContextIdSeq contexts;");
                buffer.append("\n    attribute ExceptionDefSeq exceptions;");
                buffer.append("\n  };");

                buffer.append("\n  struct OperationDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    TypeCode result;");
                buffer.append("\n    OperationMode mode;");
                buffer.append("\n    ContextIdSeq contexts;");
                buffer.append("\n    ParDescriptionSeq parameters;");
                buffer.append("\n    ExcDescriptionSeq exceptions;");
                buffer.append("\n  };");
                }

                buffer.append("\n  typedef sequence <RepositoryId> RepositoryIdSeq;");
                if (! CompilerConf.getMinimun()) { 
                buffer.append("\n  typedef sequence <OperationDescription> OpDescriptionSeq;");
                buffer.append("\n  typedef sequence <AttributeDescription> AttrDescriptionSeq;");

                buffer.append("\n  interface InterfaceDef : Container, Contained, IDLType {");
                buffer.append("\n    // read/write interface");
                buffer.append("\n    attribute InterfaceDefSeq base_interfaces;");
                buffer.append("\n    attribute boolean is_abstract;");

                buffer.append("\n    // read interface");
                buffer.append("\n    boolean is_a (in RepositoryId interface_id);");

                buffer.append("\n    struct FullInterfaceDescription {");
                buffer.append("\n      Identifier name;");
                buffer.append("\n      RepositoryId id;");
                buffer.append("\n      RepositoryId defined_in;");
                buffer.append("\n      VersionSpec version;");
                buffer.append("\n      OpDescriptionSeq operations;");
                buffer.append("\n      AttrDescriptionSeq attributes;");
                buffer.append("\n      RepositoryIdSeq base_interfaces;");
                buffer.append("\n      TypeCode type;");
                buffer.append("\n      boolean is_abstract;");
                buffer.append("\n    };");

                buffer.append("\n    FullInterfaceDescription describe_interface();");

                buffer.append("\n    // write interface");
                buffer.append("\n    AttributeDef create_attribute (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType type,");
                buffer.append("\n      in AttributeMode mode");
                buffer.append("\n    );");

                buffer.append("\n    OperationDef create_operation (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType result,");
                buffer.append("\n      in OperationMode mode,");
                buffer.append("\n      in ParDescriptionSeq params,");
                buffer.append("\n      in ExceptionDefSeq exceptions,");
                buffer.append("\n      in ContextIdSeq contexts");
                buffer.append("\n    );");
                buffer.append("\n  };");

                buffer.append("\n  struct InterfaceDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    RepositoryIdSeq base_interfaces;");
                buffer.append("\n    boolean is_abstract;");
                buffer.append("\n  };");
                }
                buffer.append("\n  typedef short Visibility;");

                buffer.append("\n  const Visibility PRIVATE_MEMBER = 0;");
                buffer.append("\n  const Visibility PUBLIC_MEMBER = 1;");

                buffer.append("\n  struct ValueMember {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    TypeCode type;");
                buffer.append("\n    IDLType type_def;");
                buffer.append("\n    Visibility access;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <ValueMember> ValueMemberSeq;");
                if (! CompilerConf.getMinimun()) {
                buffer.append("\n  interface ValueMemberDef : Contained {");
                buffer.append("\n    readonly attribute TypeCode type;");
                buffer.append("\n    attribute IDLType type_def;");
                buffer.append("\n    attribute Visibility access;");
                buffer.append("\n  };");

                buffer.append("\n  interface ValueDef : Container, Contained, IDLType {");
                // read/write interface
                buffer.append("\n    attribute InterfaceDefSeq supported_interfaces;");
                buffer.append("\n    attribute InitializerSeq initializers;");
                buffer.append("\n    attribute ValueDef base_value;");
                buffer.append("\n    attribute ValueDefSeq abstract_base_values;");
                buffer.append("\n    attribute boolean is_abstract;");
                buffer.append("\n    attribute boolean is_custom;");
                buffer.append("\n    attribute boolean is_truncatable;");

                // read interface
                buffer.append("\n    boolean is_a(in RepositoryId id);");

                buffer.append("\n    struct FullValueDescription {");
                buffer.append("\n      Identifier name;");
                buffer.append("\n      RepositoryId id;");
                buffer.append("\n      boolean is_abstract;");
                buffer.append("\n      boolean is_custom;");
                buffer.append("\n      RepositoryId defined_in;");
                buffer.append("\n      VersionSpec version;");
                buffer.append("\n      OpDescriptionSeq operations;");
                buffer.append("\n      AttrDescriptionSeq attributes;");
                buffer.append("\n      ValueMemberSeq members;");
                buffer.append("\n      InitializerSeq initializers;");
                buffer.append("\n      RepositoryIdSeq supported_interfaces;");
                buffer.append("\n      RepositoryIdSeq abstract_base_values;");
                buffer.append("\n      boolean is_truncatable;");
                buffer.append("\n      RepositoryId base_value;");
                buffer.append("\n      TypeCode type;");
                buffer.append("\n    };");

                buffer.append("\n    FullValueDescription describe_value();");

                buffer.append("\n    ValueMemberDef create_value_member(");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType type,");
                buffer.append("\n      in Visibility access");
                buffer.append("\n    );");

                buffer.append("\n    AttributeDef create_attribute(");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType type,");
                buffer.append("\n      in AttributeMode mode");
                buffer.append("\n    );");

                buffer.append("\n    OperationDef create_operation (");
                buffer.append("\n      in RepositoryId id,");
                buffer.append("\n      in Identifier name,");
                buffer.append("\n      in VersionSpec version,");
                buffer.append("\n      in IDLType result,");
                buffer.append("\n      in OperationMode mode,");
                buffer.append("\n      in ParDescriptionSeq params,");
                buffer.append("\n      in ExceptionDefSeq exceptions,");
                buffer.append("\n      in ContextIdSeq contexts");
                buffer.append("\n    );");
                buffer.append("\n  };");

                buffer.append("\n  struct ValueDescription {");
                buffer.append("\n    Identifier name;");
                buffer.append("\n    RepositoryId id;");
                buffer.append("\n    boolean is_abstract;");
                buffer.append("\n    boolean is_custom;");
                buffer.append("\n    RepositoryId defined_in;");
                buffer.append("\n    VersionSpec version;");
                buffer.append("\n    RepositoryIdSeq supported_interfaces;");
                buffer.append("\n    RepositoryIdSeq abstract_base_values;");
                buffer.append("\n    boolean is_truncatable;");
                buffer.append("\n    RepositoryId base_value;");
                buffer.append("\n  };");

                buffer.append("\n  interface ValueBoxDef : TypedefDef {");
                buffer.append("\n    attribute IDLType original_type_def;");
                buffer.append("\n  };");
                }
                buffer.append("\n  enum TCKind { //JAVA MAPPING PIDL");
                buffer.append("\n    tk_null, tk_void,");
                buffer.append("\n    tk_short, tk_long, tk_ushort, tk_ulong,");
                buffer.append("\n    tk_float, tk_double, tk_boolean, tk_char,");
                buffer.append("\n    tk_octet, tk_any, tk_TypeCode, tk_Principal, tk_objref,");
                buffer.append("\n    tk_struct, tk_union, tk_enum, tk_string,");
                buffer.append("\n    tk_sequence, tk_array, tk_alias, tk_except,");
                buffer.append("\n    tk_longlong, tk_ulonglong, tk_longdouble,");
                buffer.append("\n    tk_wchar, tk_wstring, tk_fixed,");
                buffer.append("\n    tk_value, tk_value_box,  tk_native, tk_abstract_interface");
                buffer.append("\n  };  ");

                buffer.append("\n  typedef short ValueModifier; // JAVA MAPPING PIDL");

                buffer.append("\n  const ValueModifier VM_NONE = 0;");
                buffer.append("\n  const ValueModifier VM_CUSTOM = 1;");
                buffer.append("\n  const ValueModifier VM_ABSTRACT = 2;");
                buffer.append("\n  const ValueModifier VM_TRUNCATABLE = 3;");

                buffer.append("\n  interface TypeCode { // JAVA MAPPING PIDL ");
                buffer.append("\n    exception Bounds {};");
                buffer.append("\n    exception BadKind {};");
                buffer.append("\n };");
                buffer.append("\n  exception BadFixedValue {");
                buffer.append("\n    unsigned long offset;");
                buffer.append("\n  };");

                buffer.append("\n  abstract valuetype DataOutputStream {");
                buffer.append("\n    void write_any(in any value);");
                buffer.append("\n    void write_boolean(in boolean value);");
                buffer.append("\n    void write_char(in char value);");
                buffer.append("\n    void write_wchar(in wchar value);");
                buffer.append("\n    void write_octet(in octet value);");
                buffer.append("\n    void write_short(in short value);");
                buffer.append("\n    void write_ushort(in unsigned short value);");
                buffer.append("\n    void write_long(in long value);");
                buffer.append("\n    void write_ulong(in unsigned long value);");
                buffer.append("\n    void write_longlong(in long long value);");
                buffer.append("\n    void write_ulonglong(in unsigned long long value);");
                buffer.append("\n    void write_float(in float value);");
                buffer.append("\n    void write_double(in double value);");
                buffer.append("\n    void write_longdouble(in long double value);");
                buffer.append("\n    void write_string(in string value);");
                buffer.append("\n    void write_wstring(in wstring value);");
                buffer.append("\n    void write_Object(in Object value);");
                buffer.append("\n    void write_Abstract(in AbstractBase value);");
                buffer.append("\n    void write_Value(in ValueBase value);");
                buffer.append("\n    void write_TypeCode(in TypeCode value);");
                buffer.append("\n    void write_any_array(in AnySeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_boolean_array( in BooleanSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_char_array(in CharSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_wchar_array(in WCharSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_octet_array( in OctetSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_short_array( in ShortSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_ushort_array( in UShortSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_long_array(in LongSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_ulong_array(in ULongSeq seq, in unsigned long offset,in unsigned long length);");
                buffer.append("\n    void write_ulonglong_array(in ULongLongSeq seq,in unsigned long offset,in unsigned long length);");
                buffer.append("\n    void write_longlong_array(in LongLongSeq seq,in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_float_array(in FloatSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_double_array(in DoubleSeq seq,in unsigned long offset,in unsigned long length);");
                buffer.append("\n    void write_long_double_array(in LongDoubleSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void write_fixed(in any fixed_value) raises (BadFixedValue);");
                buffer.append("\n    void write_fixed_array(in AnySeq seq,in unsigned long offset,in unsigned long length) raises (BadFixedValue);");
                buffer.append("\n  };");

                buffer.append("\n  abstract valuetype DataInputStream {");
                buffer.append("\n    any read_any();");
                buffer.append("\n    boolean read_boolean();");
                buffer.append("\n    char read_char();");
                buffer.append("\n    wchar read_wchar();");
                buffer.append("\n    octet read_octet();");
                buffer.append("\n    short read_short();");
                buffer.append("\n    unsigned short read_ushort();");
                buffer.append("\n    long read_long();");
                buffer.append("\n    unsigned long read_ulong();");
                buffer.append("\n    long long read_longlong();");
                buffer.append("\n    unsigned long long read_ulonglong();");
                buffer.append("\n    float read_float();");
                buffer.append("\n    double read_double();");
                buffer.append("\n    long double read_longdouble();");
                buffer.append("\n    string read_string();");
                buffer.append("\n    wstring read_wstring();");
                buffer.append("\n    Object read_Object();");
                buffer.append("\n    AbstractBase read_Abstract();");
                buffer.append("\n    ValueBase read_Value();");
                buffer.append("\n    TypeCode read_TypeCode();");
                buffer.append("\n    void read_any_array(inout AnySeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_boolean_array(inout BooleanSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_char_array(inout CharSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_wchar_array(inout WCharSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_octet_array( inout OctetSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_short_array( inout ShortSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_ushort_array(inout UShortSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_long_array(inout LongSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_ulong_array( inout ULongSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_ulonglong_array(inout ULongLongSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_longlong_array( inout LongLongSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_float_array(inout FloatSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    void read_double_array( inout DoubleSeq seq, in unsigned long offset,in unsigned long length);");
                buffer.append("\n    void read_long_double_array( inout LongDoubleSeq seq, in unsigned long offset, in unsigned long length);");
                buffer.append("\n    any read_fixed( in unsigned short digits, in short scale) raises (BadFixedValue);");
                buffer.append("\n    void read_fixed_array(inout AnySeq seq, in unsigned long offset,in unsigned long length, in unsigned short digits,in short scale) raises (BadFixedValue);");
                buffer.append("\n  };");

                buffer.append("\n  abstract valuetype CustomMarshal {");
                buffer.append("\n    void marshal(in DataOutputStream os);");
                buffer.append("\n    void unmarshal(in DataInputStream is);");
                buffer.append("\n  };");

                buffer.append("\n};");
                buffer.append("\n#endif"); // por posibles inclusiones
                                           // multiples de orb.idl
                /////////////
                // rmi.idl //
                /////////////
                // FIX bug [#341] Java packages created but no needed
/*
                buffer.append("\n#pragma prefix \"\"");

                buffer.append("\n#ifndef __java_rmi_Remote__");
                buffer.append("\n#define __java_rmi_Remote__");

                buffer.append("\nmodule java {");
                buffer.append("\nmodule rmi {");
                buffer.append("\ntypedef Object Remote;");
                buffer.append("\n};");
                buffer.append("\n};");

                buffer.append("\n#endif");

                buffer.append("\n#ifndef __java_io_Serializable__");
                buffer.append("\n#define __java_io_Serializable__");

                buffer.append("\nmodule java {");
                buffer.append("\nmodule io {");
                buffer.append("\ntypedef any Serializable;");
                buffer.append("\n};");
                buffer.append("\n};");

                buffer.append("\n#endif");

                buffer.append("\n#ifndef __java_io_Externalizable__");
                buffer.append("\n#define __java_io_Externalizable__");

                buffer.append("\nmodule java {");
                buffer.append("\nmodule io {");
                buffer.append("\ntypedef any Externalizable;");
                buffer.append("\n};");
                buffer.append("\n};");

                buffer.append("\n#endif");

                buffer.append("\n#ifndef __java_lang_Object__");
                buffer.append("\n#define __java_lang_Object__");

                buffer.append("\nmodule java {");
                buffer.append("\nmodule lang {");
                buffer.append("\ntypedef any _Object;");
                buffer.append("\n};");
                buffer.append("\n};");

                buffer.append("\n#endif");
*/ 
            } else if (name.equals("PortableServer.idl")) {

                CompilerConf.getModule_Packaged().addElement("PortableServer");
                CompilerConf.getPackageToTable().put("PortableServer", pack);

                buffer.append("\n#ifndef _PORTABLE_SERVER_IDL_");
                buffer.append("\n#define _PORTABLE_SERVER_IDL_");

                buffer.append("\n#include <orb.idl>");

                buffer.append("\n#pragma prefix \"omg.org\"");

                buffer.append("\nmodule PortableServer {");

                buffer.append("\n   local interface POA; // forward declaration");

                buffer.append("\n  typedef sequence<POA> POAList;");

                buffer.append("\n  native Servant;");

                buffer.append("\n  typedef CORBA::OctetSeq ObjectId;");

                buffer.append("\n  exception ForwardRequest {");
                buffer.append("\n    Object forward_reference;");
                buffer.append("\n  };");

                // Policy interfaces
                buffer.append("\n  const CORBA::PolicyType THREAD_POLICY_ID = 16;");
                buffer.append("\n  const CORBA::PolicyType LIFESPAN_POLICY_ID = 17;");
                buffer.append("\n  const CORBA::PolicyType ID_UNIQUENESS_POLICY_ID = 18;");
                buffer.append("\n  const CORBA::PolicyType ID_ASSIGNMENT_POLICY_ID = 19;");
                buffer.append("\n  const CORBA::PolicyType IMPLICIT_ACTIVATION_POLICY_ID = 20;");
                buffer.append("\n  const CORBA::PolicyType SERVANT_RETENTION_POLICY_ID = 21;");
                buffer.append("\n  const CORBA::PolicyType REQUEST_PROCESSING_POLICY_ID = 22;");

                // Thread Policy

                buffer.append("\n  enum ThreadPolicyValue { ORB_CTRL_MODEL, SINGLE_THREAD_MODEL, MAIN_THREAD_MODEL};");

                buffer.append("\n   local interface ThreadPolicy : CORBA::Policy {");
                buffer.append("\n   readonly attribute ThreadPolicyValue value;");
                buffer.append("\n  };");

                // Lifespan Policy
                buffer.append("\n  enum LifespanPolicyValue {TRANSIENT,PERSISTENT};");

                buffer.append("\n  local interface LifespanPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute LifespanPolicyValue value;");
                buffer.append("\n  };");

                // IdUniquenessPolicy
                buffer.append("\n  enum IdUniquenessPolicyValue {UNIQUE_ID,MULTIPLE_ID};");

                buffer.append("\n  local interface IdUniquenessPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute IdUniquenessPolicyValue value;");
                buffer.append("\n  };");

                // IdAssignmentPolicy
                buffer.append("\n  enum IdAssignmentPolicyValue {USER_ID, SYSTEM_ID};");

                buffer.append("\n  local interface IdAssignmentPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute IdAssignmentPolicyValue value;");
                buffer.append("\n  };");

                // Implicit ActivationPolicy
                buffer.append("\n  enum ImplicitActivationPolicyValue {IMPLICIT_ACTIVATION, NO_IMPLICIT_ACTIVATION};");

                buffer.append("\n  local interface ImplicitActivationPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute ImplicitActivationPolicyValue value;");
                buffer.append("\n  };");

                // ServantRetentionPolicy
                buffer.append("\n  enum ServantRetentionPolicyValue {RETAIN, NON_RETAIN};");

                buffer.append("\n  local interface ServantRetentionPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute ServantRetentionPolicyValue value;");
                buffer.append("\n  };");

                // RequestProcessingPolicy
                buffer.append("\n  enum RequestProcessingPolicyValue {USE_ACTIVE_OBJECT_MAP_ONLY,");
                buffer.append("\n                                     USE_DEFAULT_SERVANT,");
                buffer.append("\n                                     USE_SERVANT_MANAGER};");

                buffer.append("\n  local interface RequestProcessingPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute RequestProcessingPolicyValue value;");
                buffer.append("\n  };");

                ///////////////////////////
                // POAManager interface //
                ///////////////////////////

                buffer.append("\n  local interface POAManager {");

                buffer.append("\n    exception AdapterInactive{};");

                buffer.append("\n    enum State {HOLDING, ACTIVE, DISCARDING, INACTIVE};");

                buffer.append("\n    void activate() raises(AdapterInactive);");

                buffer.append("\n    void hold_requests(in boolean wait_for_completion) raises(AdapterInactive);");

                buffer.append("\n    void discard_requests(in boolean wait_for_completion) raises(AdapterInactive);");

                buffer.append("\n    void deactivate(in boolean etherealize_objects, in boolean wait_for_completion)");
                buffer.append("\n      raises(AdapterInactive);");

                buffer.append("\n    State get_state();");
                buffer.append("\n  };");

                ////////////////////////////////
                // AdapterActivator interface //
                ////////////////////////////////

                buffer.append("\n  local interface AdapterActivator {");
                buffer.append("\n    boolean unknown_adapter(in POA parent,in string name);");
                buffer.append("\n  };");

                ///////////////////////////////
                // ServantManager interface //
                ///////////////////////////////

                buffer.append("\n   local interface ServantManager{ };");

                /////////////////////////////////
                // ServantActivator interface //
                /////////////////////////////////

                buffer.append("\n  local interface ServantActivator : ServantManager {");

                buffer.append("\n    Servant incarnate (in ObjectId oid,in POA adapter) raises (ForwardRequest);");

                buffer.append("\n    void etherealize (in ObjectId oid, in POA adapter, in Servant serv,");

                buffer.append("\n                      in boolean cleanup_in_progress, in boolean remaining_activations);");
                buffer.append("\n  };");

                ///////////////////////////////
                // ServantLocator interface //
                ///////////////////////////////

                buffer.append("\n  local interface ServantLocator : ServantManager {");

                buffer.append("\n    native Cookie;");

                buffer.append("\n    Servant preinvoke(in ObjectId oid, in POA adapter,");
                buffer.append("\n                      in CORBA::Identifier operation, out Cookie the_cookie)");
                buffer.append("\n      raises (ForwardRequest);");

                buffer.append("\n    void postinvoke(in ObjectId oid, in POA adapter, in CORBA::Identifier operation,");
                buffer.append("\n                    in Cookie the_cookie, in Servant the_servant);");
                buffer.append("\n  };");

                ///////////////////
                // POA interface //
                ///////////////////

                buffer.append("\n local interface POA {");

                buffer.append("\n    exception AdapterAlreadyExists {};");
                buffer.append("\n    exception AdapterNonExistent {};");
                buffer.append("\n    exception InvalidPolicy {unsigned short index;};");
                buffer.append("\n    exception NoServant {};");
                buffer.append("\n    exception ObjectAlreadyActive {};");
                buffer.append("\n    exception ObjectNotActive {};");
                buffer.append("\n    exception ServantAlreadyActive {};");
                buffer.append("\n    exception ServantNotActive {};");
                buffer.append("\n    exception WrongAdapter {};");
                buffer.append("\n    exception WrongPolicy {};");

                // POA creation and destruction

                buffer.append("\n    POA create_POA(in string adapter_name, in POAManager a_POAManager, in CORBA::PolicyList policies)");
                buffer.append("\n      raises (AdapterAlreadyExists, InvalidPolicy);");

                buffer.append("\n    POA find_POA(in string adapter_name, in boolean activate_it) raises (AdapterNonExistent);");

                buffer.append("\n    void destroy(in boolean etherealize_objects,in boolean wait_for_completion);");

                // Factories for Policy objects

                buffer.append("\n    ThreadPolicy create_thread_policy(in ThreadPolicyValue value);");

                buffer.append("\n    LifespanPolicy create_lifespan_policy(in LifespanPolicyValue value);");

                buffer.append("\n    IdUniquenessPolicy create_id_uniqueness_policy(in IdUniquenessPolicyValue value);");

                buffer.append("\n    IdAssignmentPolicy create_id_assignment_policy(in IdAssignmentPolicyValue value);");

                buffer.append("\n    ImplicitActivationPolicy create_implicit_activation_policy(in ImplicitActivationPolicyValue value);");

                buffer.append("\n    ServantRetentionPolicy create_servant_retention_policy(in ServantRetentionPolicyValue value);");

                buffer.append("\n    RequestProcessingPolicy create_request_processing_policy(in RequestProcessingPolicyValue value);");

                // POA attributes

                buffer.append("\n    readonly attribute string the_name;");
                buffer.append("\n    readonly attribute POA the_parent;");
                buffer.append("\n    readonly attribute POAList the_children;");
                buffer.append("\n    readonly attribute POAManager the_POAManager;");
                buffer.append("\n    attribute AdapterActivator the_activator;");

                // Servant Manager registration:

                buffer.append("\n    ServantManager get_servant_manager() raises (WrongPolicy);");

                buffer.append("\n    void set_servant_manager(in ServantManager imgr) raises (WrongPolicy);");

                buffer.append("\n    // operations for the USE_DEFAULT_SERVANT policy");

                buffer.append("\n    Servant get_servant() raises (NoServant, WrongPolicy);");

                buffer.append("\n    void set_servant(in Servant p_servant) raises (WrongPolicy);");

                buffer.append("\n    // object activation and deactivation");

                buffer.append("\n    ObjectId activate_object(in Servant p_servant) raises (ServantAlreadyActive, WrongPolicy);");

                buffer.append("\n    void activate_object_with_id(in ObjectId id, in Servant p_servant)");
                buffer.append("\n      raises (ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy);");

                buffer.append("\n    void deactivate_object(in ObjectId oid) raises (ObjectNotActive, WrongPolicy);");

                // reference creation operations

                buffer.append("\n    Object create_reference (in CORBA::RepositoryId intf) raises (WrongPolicy);");

                buffer.append("\n    Object create_reference_with_id (in ObjectId oid, in CORBA::RepositoryId intf);");

                // Identity mapping operations:

                buffer.append("\n    ObjectId servant_to_id(in Servant p_servant) raises (ServantNotActive, WrongPolicy);");

                buffer.append("\n    Object servant_to_reference(in Servant p_servant) raises (ServantNotActive, WrongPolicy);");

                buffer.append("\n    Servant reference_to_servant(in Object reference)");
                buffer.append("\n      raises(ObjectNotActive, WrongAdapter, WrongPolicy);");

                buffer.append("\n    ObjectId reference_to_id(in Object reference) raises (WrongAdapter, WrongPolicy);");

                buffer.append("\n    Servant id_to_servant( in ObjectId oid) raises (ObjectNotActive, WrongPolicy);");

                buffer.append("\n    Object id_to_reference(in ObjectId oid) raises (ObjectNotActive, WrongPolicy);");

                buffer.append("\n    readonly attribute CORBA::OctetSeq id;");
                buffer.append("  };");

                ///////////////////////
                // Current interface //
                ///////////////////////

                buffer.append("\n   local interface Current : CORBA::Current {");

                buffer.append("\n    exception NoContext { };");

                buffer.append("\n    POA get_POA() raises (NoContext);");

                buffer.append("\n    ObjectId get_object_id() raises (NoContext);");

                buffer.append("\n    Object get_reference() raises(NoContext);");

                buffer.append("\n    Servant get_servant() raises(NoContext);");
                buffer.append("\n  };");
                buffer.append("\n};");

                buffer.append("\n#endif");

            } else if (name.equals("IOP.idl")) {

                CompilerConf.getModule_Packaged().addElement("IOP");
                CompilerConf.getPackageToTable().put("IOP", pack);

                buffer.append("\n#ifndef _IOP_IDL_");
                buffer.append("\n#define _IOP_IDL_");

                buffer.append("\n#include <orb.idl>");

                buffer.append("\n#pragma prefix \"omg.org\"");

                buffer.append("\nmodule IOP {");

                // Standard Protocol Profile tag values

                buffer.append("\n  typedef unsigned long ProfileId;");

                buffer.append("\n  const ProfileId TAG_INTERNET_IOP = 0;");

                buffer.append("\n  const ProfileId TAG_MULTIPLE_COMPONENTS = 1;");

                buffer.append("\n  struct TaggedProfile {");
                buffer.append("\n    ProfileId tag;");
                buffer.append("\n    sequence <octet> profile_data;");
                buffer.append("\n  };");

                // an Interoperable Object Reference is a sequence of
                // object-specific protocol profiles, plus a type ID.
                buffer.append("\n  struct IOR {");
                buffer.append("\n    string type_id;");
                buffer.append("\n    sequence <TaggedProfile> profiles;");
                buffer.append("\n  };");

                // Standard way of representing multicomponent profiles.

                // This would be encapsulated in a TaggedProfile.
                buffer.append("\n  typedef unsigned long ComponentId;");

                buffer.append("\n  struct TaggedComponent {");
                buffer.append("\n    ComponentId tag;");
                buffer.append("\n    sequence <octet> component_data;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <TaggedComponent> MultipleComponentProfile;");
                buffer.append("\n  typedef sequence<TaggedComponent> TaggedComponentSeq;");

                // CORBA 2.4
                buffer.append("\n  const ComponentId TAG_ORB_TYPE = 0;");
                buffer.append("\n  const ComponentId TAG_CODE_SETS = 1;");
                buffer.append("\n  const ComponentId TAG_POLICIES = 2;");
                buffer.append("\n  const ComponentId TAG_ALTERNATE_IIOP_ADDRESS = 3;");
                buffer.append("\n  const ComponentId TAG_ASSOCIATION_OPTIONS = 13;");
                buffer.append("\n  const ComponentId TAG_SEC_NAME = 14;");
                buffer.append("\n  const ComponentId TAG_SPKM_1_SEC_MECH = 15;");
                buffer.append("\n  const ComponentId TAG_SPKM_2_SEC_MECH = 16;");
                buffer.append("\n  const ComponentId TAG_KerberosV5_SEC_MECH = 17;");
                buffer.append("\n  const ComponentId TAG_CSI_ECMA_Secret_SEC_MECH = 18;");
                buffer.append("\n  const ComponentId TAG_CSI_ECMA_Hybrid_SEC_MECH = 19;");
                buffer.append("\n  const ComponentId TAG_SSL_SEC_TRANS = 20;");
                buffer.append("\n  const ComponentId TAG_CSI_ECMA_Public_SEC_MECH = 21;");
                buffer.append("\n  const ComponentId TAG_GENERIC_SEC_MECH = 22;");
                buffer.append("\n  const ComponentId TAG_FIREWALL_TRANS = 23;");
                buffer.append("\n  const ComponentId TAG_SCCP_CONTACT_INFO = 24;");
                buffer.append("\n  const ComponentId TAG_JAVA_CODEBASE = 25;");
                buffer.append("\n  const ComponentId TAG_TRANSACTION_POLICY = 26;");
                buffer.append("\n  const ComponentId TAG_MESSAGE_ROUTERS = 30;");
                buffer.append("\n  const ComponentId TAG_OTS_POLICY = 31;");
                buffer.append("\n  const ComponentId TAG_INV_POLICY = 32;");
                buffer.append("\n  const ComponentId TAG_INET_SEC_TRANS = 123;");
                buffer.append("\n  const ComponentId TAG_COMPLETE_OBJECT_KEY = 5;");
                buffer.append("\n  const ComponentId TAG_ENDPOINT_ID_POSITION = 6;");
                buffer.append("\n  const ComponentId TAG_LOCATION_POLICY = 12;");
                buffer.append("\n  const ComponentId TAG_DCE_STRING_BINDING = 100;");
                buffer.append("\n  const ComponentId TAG_DCE_BINDING_NAME = 101;");
                buffer.append("\n  const ComponentId TAG_DCE_NO_PIPES = 102;");
                buffer.append("\n  const ComponentId TAG_DCE_SEC_MECH = 103; // Security Service");

                buffer.append("\n  typedef unsigned long ServiceId;");

                buffer.append("\n  struct ServiceContext {");
                buffer.append("\n  ServiceId context_id;");
                buffer.append("\n  sequence <octet> context_data;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence <ServiceContext>ServiceContextList;");

                buffer.append("\n  const ServiceId TransactionService = 0;");
                buffer.append("\n  const ServiceId CodeSets = 1;");
                buffer.append("\n  const ServiceId ChainBypassCheck = 2;");
                buffer.append("\n  const ServiceId ChainBypassInfo = 3;");
                buffer.append("\n  const ServiceId LogicalThreadId = 4;");
                buffer.append("\n  const ServiceId BI_DIR_IIOP = 5;");
                buffer.append("\n  const ServiceId SendingContextRunTime = 6;");
                buffer.append("\n  const ServiceId INVOCATION_POLICIES = 7;");
                buffer.append("\n  const ServiceId FORWARDED_IDENTITY = 8;");
                buffer.append("\n  const ServiceId UnknownExceptionInfo = 9;");
                buffer.append("\n  const ServiceId RTCorbaPriority = 10;");
                buffer.append("\n  const ServiceId RTCorbaPriorityRange = 11;");
                buffer.append("\n  const ServiceId ExceptionDetailMessage = 14;");

                buffer.append("\n  local interface Codec {");

                buffer.append("\n    exception InvalidTypeForEncoding {};");
                buffer.append("\n    exception FormatMismatch {};");
                buffer.append("\n    exception TypeMismatch {};");

                buffer.append("\n    CORBA::OctetSeq encode (in any data) raises (InvalidTypeForEncoding);");

                buffer.append("\n    any decode (in CORBA::OctetSeq data) raises (FormatMismatch);");

                buffer.append("\n    CORBA::OctetSeq encode_value (in any data) raises (InvalidTypeForEncoding);");

                buffer.append("\n    any decode_value (in CORBA::OctetSeq data, in CORBA::TypeCode tc)");
                buffer.append("\n      raises (FormatMismatch, TypeMismatch);");
                buffer.append("\n  };");

                buffer.append("\n  typedef short EncodingFormat;");

                buffer.append("\n  const EncodingFormat ENCODING_CDR_ENCAPS = 0;");

                buffer.append("\n  struct Encoding {");
                buffer.append("\n    EncodingFormat format;");
                buffer.append("\n    octet major_version;");
                buffer.append("\n    octet minor_version;");
                buffer.append("\n  };");

                buffer.append("\n  local interface CodecFactory {");
                buffer.append("\n    exception UnknownEncoding {};");

                buffer.append("\n    Codec create_codec (in Encoding enc)  raises (UnknownEncoding);");
                buffer.append("\n  };");
                buffer.append("\n};");

                buffer.append("\n#endif");

            } else if (name.equals("DynamicAny.idl")) {

                CompilerConf.getModule_Packaged().addElement("DynamicAny");
                CompilerConf.getPackageToTable().put("DynamicAny", pack);

                buffer.append("\n#ifndef _DYNAMIC_ANY_IDL_");
                buffer.append("\n#define _DYNAMIC_ANY_IDL_");

                buffer.append("\n#include <orb.idl>");

                buffer.append("\n#pragma prefix \"omg.org\"");

                buffer.append("\nmodule DynamicAny {");
                buffer.append("\n  local interface DynAny {");

                buffer.append("\n    exception InvalidValue {};");
                buffer.append("\n    exception TypeMismatch {};");

                buffer.append("\n    CORBA::TypeCode type();");

                buffer.append("\n    void assign(in DynAny dyn_any) raises(TypeMismatch);");

                buffer.append("\n    void from_any(in any value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    any to_any();");

                buffer.append("\n    boolean equal(in DynAny dyn_any);");

                buffer.append("\n    void destroy();");

                buffer.append("\n    DynAny copy();");

                buffer.append("\n    void insert_boolean(in boolean value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_octet(in octet value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_char(in char value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_short(in short value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_ushort(in unsigned short value) raises(TypeMismatch,InvalidValue);");
                buffer.append("\n    void insert_long(in long value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_ulong(in unsigned long value) raises(TypeMismatch,InvalidValue);");
                buffer.append("\n    void insert_float(in float value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_double(in double value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_string(in string value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_reference(in Object value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_typecode(in CORBA::TypeCode value) raises(TypeMismatch,InvalidValue);");
                buffer.append("\n    void insert_longlong(in long long value) raises(TypeMismatch,InvalidValue);");
                buffer.append("\n    void insert_ulonglong(in unsigned long long value) raises(TypeMismatch,InvalidValue);");
                buffer.append("\n    void insert_longdouble(in long double value) raises(TypeMismatch,InvalidValue);");
                buffer.append("\n    void insert_wchar(in wchar value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_wstring(in wstring value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_any(in any value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_dyn_any(in DynAny value) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    void insert_val(in ValueBase value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    boolean get_boolean() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    octet get_octet() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    char get_char() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    short get_short() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    unsigned short get_ushort() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    long get_long() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    unsigned long get_ulong() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    float get_float() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    double get_double() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    string get_string() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    Object get_reference() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    CORBA::TypeCode get_typecode() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    long long get_longlong() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    unsigned long long get_ulonglong() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    long double get_longdouble() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    wchar get_wchar() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    wstring get_wstring() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    any get_any() raises(TypeMismatch, InvalidValue);");
                buffer.append("\n    DynAny get_dyn_any() raises(TypeMismatch, InvalidValue);");
                //ValueBase get_val() raises(TypeMismatch, InvalidValue);

                buffer.append("\n    boolean seek(in long index);");
                buffer.append("\n    void rewind();");
                buffer.append("\n    boolean next();");
                buffer.append("\n    unsigned long component_count();");
                buffer.append("\n    DynAny current_component() raises(TypeMismatch);");

                buffer.append("\n    void insert_abstract(in CORBA::AbstractBase value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::AbstractBase get_abstract()");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_boolean_seq(in CORBA::BooleanSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_octet_seq(in CORBA::OctetSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_char_seq(in CORBA::CharSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_short_seq(in CORBA::ShortSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_ushort_seq(in CORBA::UShortSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_long_seq(in CORBA::LongSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_ulong_seq(in CORBA::ULongSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_float_seq(in CORBA::FloatSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_double_seq(in CORBA::DoubleSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_longlong_seq(in CORBA::LongLongSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_ulonglong_seq(in CORBA::ULongLongSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_longdouble_seq(in CORBA::LongDoubleSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    void insert_wchar_seq(in CORBA::WCharSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::BooleanSeq get_boolean_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::OctetSeq get_octet_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::CharSeq get_char_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::ShortSeq get_short_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::UShortSeq get_ushort_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::LongSeq get_long_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::ULongSeq get_ulong_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::FloatSeq get_float_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::DoubleSeq get_double_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::LongLongSeq get_longlong_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::ULongLongSeq get_ulonglong_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::LongDoubleSeq get_longdouble_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::WCharSeq get_wchar_seq() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n  };");

                buffer.append("\n  local interface DynFixed: DynAny {");
                buffer.append("\n    string get_value();");
                buffer.append("\n    boolean set_value(in string val) raises(TypeMismatch, InvalidValue);");
                buffer.append("\n  };");

                buffer.append("\n  local interface DynEnum : DynAny {");
                buffer.append("\n    string get_as_string();");
                buffer.append("\n    void set_as_string(in string value) raises(InvalidValue);");
                buffer.append("\n    unsigned long get_as_ulong();");
                buffer.append("\n    void set_as_ulong(in unsigned long value) raises(InvalidValue);");
                buffer.append("\n  };");

                buffer.append("\n  typedef string FieldName;");
                buffer.append("\n    struct NameValuePair {");
                buffer.append("\n    FieldName id;");
                buffer.append("\n    any value;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence<NameValuePair> NameValuePairSeq;");

                buffer.append("\n  struct NameDynAnyPair {");
                buffer.append("\n    FieldName id;");
                buffer.append("\n    DynAny value;");
                buffer.append("\n  };");

                buffer.append("\n  typedef sequence<NameDynAnyPair> NameDynAnyPairSeq;");

                buffer.append("\n  local interface DynStruct : DynAny {");

                buffer.append("\n    FieldName current_member_name() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::TCKind current_member_kind() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    NameValuePairSeq get_members();");

                buffer.append("\n    void set_members(in NameValuePairSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    NameDynAnyPairSeq get_members_as_dyn_any();");

                buffer.append("\n    void set_members_as_dyn_any(in NameDynAnyPairSeq value)");
                buffer.append("\n      raises(TypeMismatch, InvalidValue);");
                buffer.append("\n  };");

                buffer.append("\n  local interface DynUnion : DynAny {");

                buffer.append("\n    DynAny get_discriminator() raises(InvalidValue);");

                buffer.append("\n    void set_discriminator(in DynAny d) raises(TypeMismatch);");

                buffer.append("\n    void set_to_default_member() raises(TypeMismatch);");

                buffer.append("\n    void set_to_no_active_member() raises(TypeMismatch);");

                buffer.append("\n    boolean has_no_active_member();");

                buffer.append("\n    CORBA::TCKind discriminator_kind();");

                buffer.append("\n    DynAny member() raises(InvalidValue);");

                buffer.append("\n    FieldName member_name() raises(InvalidValue);");

                buffer.append("\n    CORBA::TCKind member_kind() raises(InvalidValue);");

                buffer.append("\n  };");

                buffer.append("\n  typedef sequence<any> AnySeq;");

                buffer.append("\n  typedef sequence<DynAny> DynAnySeq;");

                buffer.append("\n  local interface DynSequence : DynAny {");

                buffer.append("\n    unsigned long get_length();");

                buffer.append("\n    void set_length(in unsigned long len) raises(InvalidValue);");

                buffer.append("\n    AnySeq get_elements();");

                buffer.append("\n    void set_elements(in AnySeq value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    DynAnySeq get_elements_as_dyn_any();");

                buffer.append("\n    void set_elements_as_dyn_any(in DynAnySeq value) raises(TypeMismatch,InvalidValue);");

                buffer.append("\n  };");

                buffer.append("\n  local interface DynArray : DynAny {");

                buffer.append("\n    AnySeq get_elements();");

                buffer.append("\n    void set_elements(in AnySeq value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    DynAnySeq get_elements_as_dyn_any();");

                buffer.append("\n    void set_elements_as_dyn_any(in DynAnySeq value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n  };");

                buffer.append("\n  local interface DynValue : DynAny {");

                buffer.append("\n    FieldName current_member_name()raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    CORBA::TCKind current_member_kind() raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    NameValuePairSeq get_members();");

                buffer.append("\n    void set_members(in NameValuePairSeq value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n    NameDynAnyPairSeq get_members_as_dyn_any();");

                buffer.append("\n    void set_members_as_dyn_any(in NameDynAnyPairSeq value) raises(TypeMismatch, InvalidValue);");

                buffer.append("\n  };");

                buffer.append("\n  local interface DynAnyFactory {");

                buffer.append("\n    exception InconsistentTypeCode {};");

                buffer.append("\n    DynAny create_dyn_any(in any value) raises(InconsistentTypeCode);");

                buffer.append("\n    DynAny create_dyn_any_from_type_code(in CORBA::TypeCode type) raises(InconsistentTypeCode);");

                buffer.append("\n  };");

                buffer.append("\n}; // module DynamicAny");

                buffer.append("\n#endif // _DYNAMIC_ANY_IDL_");

            } else if (name.equals("SendingContext.idl")) {

                CompilerConf.getModule_Packaged().addElement("SendingContext");
                CompilerConf.getPackageToTable().put("SendingContext", pack);

                buffer.append("\n#ifndef _SENDING_CONTEXT_IDL_");
                buffer.append("\n#define _SENDING_CONTEXT_IDL_");

                buffer.append("\n#include <orb.idl>");

                buffer.append("\n#pragma prefix \"omg.org\"");

                buffer.append("\nmodule SendingContext {");

                buffer.append("\n  interface RunTime {};");

                buffer.append("\n  interface CodeBase: RunTime {");

                buffer.append("\n    typedef string URL; // blank-separated list of one or more URLs");
                buffer.append("\n    typedef sequence<URL> URLSeq;");
                buffer.append("\n    typedef sequence < CORBA::ValueDef::FullValueDescription > ValueDescSeq;");

                // Operation to obtain the IR from the sending context
                buffer.append("\n    CORBA::Repository get_ir();");

                // Operations to obtain a location of the implementation code
                buffer.append("\n    URL implementation(in CORBA::RepositoryId x);");

                buffer.append("\n    URLSeq implementations(in CORBA::RepositoryIdSeq x);");

                // Operations to obtain complete meta information about a Value
                // This is just a performance optimization the IR can provide
                // the same information
                buffer.append("\n    CORBA::ValueDef::FullValueDescription meta(in CORBA::RepositoryId x);");

                buffer.append("\n    ValueDescSeq metas(in CORBA::RepositoryIdSeq x);");

                // To obtain a type graph for a value type
                // same comment as before the IR can provide similar
                // information
                buffer.append("\n    CORBA::RepositoryIdSeq bases(in CORBA::RepositoryId x);");
                buffer.append("\n  };");
                buffer.append("\n};");

                buffer.append("\n#endif");

            } else if (name.equals("BiDirPolicy.idl")) {

                CompilerConf.getModule_Packaged().addElement("BiDirPolicy");
                CompilerConf.getPackageToTable().put("BiDirPolicy", pack);

                buffer.append("\n#ifndef _BI_DIR_POLICY_IDL_");
                buffer.append("\n#define _BI_DIR_POLICY_IDL_");

                buffer.append("\n#include <orb.idl>");

                buffer.append("\n#pragma prefix \"omg.org\"");

                // Self contained module for Bi-directional GIOP policy
                buffer.append("\nmodule BiDirPolicy {");

                buffer.append("\n  typedef unsigned short BidirectionalPolicyValue;");

                buffer.append("\n  const BidirectionalPolicyValue NORMAL = 0;");
                buffer.append("\n  const BidirectionalPolicyValue BOTH = 1;");

                buffer.append("\n  const CORBA::PolicyType BIDIRECTIONAL_POLICY_TYPE = 37;");

                buffer.append("\n  local interface BidirectionalPolicy : CORBA::Policy {");
                buffer.append("\n    readonly attribute BidirectionalPolicyValue value;");
                buffer.append("\n  };");
                buffer.append("\n};");

                buffer.append("\n#endif");

            } else if (name.equals("Dynamic.idl")) {

                CompilerConf.getModule_Packaged().addElement("Dynamic");
                CompilerConf.getPackageToTable().put("Dynamic", pack);

                buffer.append("\n#include <orb.idl>");
                buffer.append("\n#pragma prefix \"omg.org\" ");


                buffer.append("\nmodule Dynamic {");

                buffer.append("\n    struct Parameter {");
                buffer.append("\n        any argument;");
                buffer.append("\n        CORBA::ParameterMode mode;");
                buffer.append("\n    };");

                buffer.append("\n    typedef sequence<Parameter> ParameterList;");
                buffer.append("\n    typedef CORBA::StringSeq ContextList;");
                buffer.append("\n    typedef sequence<CORBA::TypeCode> ExceptionList;");
                buffer.append("\n    typedef CORBA::StringSeq RequestContext;");

                buffer.append("\n}; // module Dynamic");


            } else if (name.equals("Messaging.idl")) {

                CompilerConf.getModule_Packaged().addElement("Messaging");
                CompilerConf.getPackageToTable().put("Messaging", pack);

                buffer.append("\n#ifndef __CORBA_MESSAGING_");
                buffer.append("\n#define __CORBA_MESSAGING_");


                buffer.append("\n#include \"IOP.idl\"");
                buffer.append("\n#include \"TimeBase.idl\"");
                buffer.append("\n#include \"Dynamic.idl\"");
                
                buffer.append("\n#pragma prefix \"omg.org\"");
                
                buffer.append("\nmodule Messaging {");
                
                buffer.append("\n	typedef short RebindMode;");
                buffer.append("\n	const RebindMode TRANSPARENT = 0;");
                buffer.append("\n	const RebindMode NO_REBIND =	1;");
                buffer.append("\n	const RebindMode NO_RECONNECT = 2;");

                buffer.append("\n	typedef short SyncScope;");
                buffer.append("\n	const SyncScope SYNC_NONE = 0;");
                buffer.append("\n	const SyncScope SYNC_WITH_TRANSPORT = 1;");
                buffer.append("\n	const SyncScope SYNC_WITH_SERVER = 	2;");
                buffer.append("\n	const SyncScope SYNC_WITH_TARGET = 	3;");

                buffer.append("\n	typedef short RoutingType;");
                buffer.append("\n	const RoutingType ROUTE_NONE = 0;");
                buffer.append("\n	const RoutingType ROUTE_FORWARD = 1;");
                buffer.append("\n	const RoutingType ROUTE_STORE_AND_FORWARD = 2;");


                buffer.append("\n	typedef short Priority;");

                buffer.append("\n	typedef unsigned short Ordering;");
                buffer.append("\n	const Ordering ORDER_ANY = 0x01;");
                buffer.append("\n	const Ordering ORDER_TEMPORAL = 0x02;");
                buffer.append("\n	const Ordering ORDER_PRIORITY = 0x04;");
                buffer.append("\n	const Ordering ORDER_DEADLINE = 0x08;");



                buffer.append("\n	const CORBA::PolicyType REBIND_POLICY_TYPE = 23;");

                buffer.append("\n	local interface RebindPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute RebindMode rebind_mode;");
                buffer.append("\n	};	");



                buffer.append("\n	const CORBA::PolicyType SYNC_SCOPE_POLICY_TYPE = 24;");
                buffer.append("\n		local interface SyncScopePolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute SyncScope synchronization;");
                buffer.append("\n	};");



                buffer.append("\n	const CORBA::PolicyType REQUEST_PRIORITY_POLICY_TYPE = 25;");

                buffer.append("\n	struct PriorityRange {");
                buffer.append("\n		Priority min;");
                buffer.append("\n		Priority max;");
                buffer.append("\n	};");

                buffer.append("\n	local interface RequestPriorityPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute PriorityRange priority_range;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType REPLY_PRIORITY_POLICY_TYPE = 26;");

                buffer.append("\n	interface ReplyPriorityPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute PriorityRange priority_range;");
                buffer.append("\n	};");



                buffer.append("\n	const CORBA::PolicyType REQUEST_START_TIME_POLICY_TYPE = 27;");

                buffer.append("\n	local interface RequestStartTimePolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute TimeBase::UtcT start_time;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType REQUEST_END_TIME_POLICY_TYPE = 28;	");

                buffer.append("\n	local interface RequestEndTimePolicy : CORBA::Policy {	");
                buffer.append("\n		readonly attribute TimeBase::UtcT end_time;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType REPLY_START_TIME_POLICY_TYPE = 29;");

                buffer.append("\n	local interface ReplyStartTimePolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute TimeBase::UtcT start_time;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType REPLY_END_TIME_POLICY_TYPE = 30;");

                buffer.append("\n	local interface ReplyEndTimePolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute TimeBase::UtcT end_time;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType RELATIVE_REQ_TIMEOUT_POLICY_TYPE = 31;");

                buffer.append("\n	local interface RelativeRequestTimeoutPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute TimeBase::TimeT relative_expiry;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType RELATIVE_RT_TIMEOUT_POLICY_TYPE = 32;");

                buffer.append("\n	local interface RelativeRoundtripTimeoutPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute TimeBase::TimeT relative_expiry;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType ROUTING_POLICY_TYPE = 33;");

                buffer.append("\n	struct RoutingTypeRange {");
                buffer.append("\n		RoutingType min;");
                buffer.append("\n		RoutingType max;");
                buffer.append("\n	};");

                buffer.append("\n	local interface RoutingPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute RoutingTypeRange routing_range;");
                buffer.append("\n	};");

                buffer.append("\n	const CORBA::PolicyType MAX_HOPS_POLICY_TYPE = 34;");

                buffer.append("\n	local interface MaxHopsPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute unsigned short max_hops;");
                buffer.append("\n	};");



                buffer.append("\n	const CORBA::PolicyType QUEUE_ORDER_POLICY_TYPE = 35;");

                buffer.append("\n	local interface QueueOrderPolicy : CORBA::Policy {");
                buffer.append("\n		readonly attribute Ordering   allowed_orders;");
                buffer.append("\n	}; ");


                buffer.append("\n	struct PolicyValue {");
                buffer.append("\n		CORBA::PolicyType	ptype;");
                buffer.append("\n		sequence<octet>		pvalue;");
                buffer.append("\n	};");

                buffer.append("\n	typedef sequence<PolicyValue> PolicyValueSeq;");

                buffer.append("\n	const IOP::ComponentId TAG_POLICIES = 2;");
                buffer.append("\n	const IOP::ServiceId INVOCATION_POLICIES = 7;");

                buffer.append("\n	typedef CORBA::OctetSeq MarshaledException;");
                
                buffer.append("\n	native UserExceptionBase;");

                buffer.append("\n	valuetype ExceptionHolder {");
                buffer.append("\n          void raise_exception() raises (UserExceptionBase);");
                buffer.append("\n          void raise_exception_with_list(in Dynamic::ExceptionList exc_list)");
                buffer.append("\n              raises (UserExceptionBase);");
                
                buffer.append("\n          private boolean is_system_exception;");
                buffer.append("\n          private boolean byte_order;");
                buffer.append("\n          private MarshaledException marshaled_exception;");
                buffer.append("\n        };");
                
                
                buffer.append("\n	interface ReplyHandler { };");
                buffer.append("\n};");
                
                buffer.append("\n#endif");

            }
            buffer_table.put(name, buffer);
            return buffer.toString();
        }
    }

    public static boolean isHardCodedIDL(String name)
    {
        return (name.equals("orb.idl") || name.equals("PortableServer.idl")
                || name.equals("IOP.idl") || name.equals("DynamicAny.idl")
                || name.equals("SendingContext.idl") 
                || name.equals("BiDirPolicy.idl"));
    }

    public static boolean isHardCodedModule(String name)
    {
        return (name.equals("CORBA") || name.equals("PortableServer")
                || name.equals("IOP") || name.equals("DynamicAny")
                || name.equals("SendingContext") || name.equals("BiDirPolicy"));
    }

    public static boolean isIncludedModule(String name)
    {
        return ((buffer_table.containsKey(name + ".idl") && !name.equals("orb")) 
            || name.equals("CORBA") && buffer_table.containsKey("orb.idl"));
    }
}
