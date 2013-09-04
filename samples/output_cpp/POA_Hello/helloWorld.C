//
// helloWorld.C (skeleton)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//

#include "POA_Hello/helloWorld.h" 

#include "TIDorb/portable/TypeCodeFactory.h" 

#include "TIDorb/portable/ORB.h"

bool POA_Hello::__helloWorld__ltstr::operator()(const char* s1, const char* s2) const
{
	return (strcmp(s1, s2) < 0);
}

POA_Hello::__POA_helloWorldMAP::__POA_helloWorldMAP()
// Constructor
{
	_methods["sayHello"]=1;
}

Hello::helloWorld_ptr POA_Hello::helloWorld::_this() {
	CORBA::Object_var _ref = PortableServer::DynamicImplementation::_this();
	return Hello::helloWorld::_narrow(_ref);
}

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld::__init_ids(){
	CORBA::RepositoryIdSeq_ptr ids = new  CORBA::RepositoryIdSeq();
	ids->length(1);
	(*ids)[0]=CORBA::string_dup("IDL:Hello/helloWorld:1.0");
	return ids;
}

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld::__ids = POA_Hello::helloWorld::__init_ids();

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld::_ids() {
	return __ids;
}

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld::_all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectId) 
{
	return _ids();
}

CORBA::RepositoryId POA_Hello::helloWorld::_primary_interface(const PortableServer::ObjectId& oid,PortableServer::POA_ptr poa)
{
	 return CORBA::string_dup("IDL:Hello/helloWorld:1.0");
}

POA_Hello::__POA_helloWorldMAP POA_Hello::helloWorld::_mapped_methods;

void POA_Hello::helloWorld::invoke(::CORBA::ServerRequest_ptr _request)
{
	int _method_id = _mapped_methods._methods[_request->operation()];
	if (_method_id == 0)
	{// Undefined Operation 
		throw ::CORBA::BAD_OPERATION();
	}
	TIDorb::portable::ORB* __orb= dynamic_cast< TIDorb::portable::ORB* > (get_delegate()->orb(this));

		if (_method_id == 1) {
			::CORBA::NVList_var _params;
			__orb->create_list(0, _params);

			_request->arguments(_params);

			char* _result = this->sayHello();

			::CORBA::Any _resultAny;
			_resultAny <<=CORBA::Any::from_string(_result, 0);
			_request->set_result(_resultAny);


			return;
		}
		else throw ::CORBA::BAD_OPERATION();
}// end of method invoke.

POA_Hello::helloWorld_tie::helloWorld_tie(Hello::helloWorld_ptr delegate)
{
	_delegate_tie = Hello::helloWorld::_duplicate(delegate);
}

Hello::helloWorld_ptr POA_Hello::helloWorld_tie::_delegate()
{
	return _delegate_tie;
}

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld_tie::__init_ids(){
	CORBA::RepositoryIdSeq_ptr ids = new  CORBA::RepositoryIdSeq();
	ids->length(1);
	(*ids)[0]=CORBA::string_dup("IDL:Hello/helloWorld:1.0");
	return ids;
}

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld_tie::__ids =POA_Hello::helloWorld_tie::__init_ids();

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld_tie::_ids()
{
	return __ids;
}

const CORBA::RepositoryIdSeq_ptr POA_Hello::helloWorld_tie::_all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectId)
{
	return __ids;
}

char* POA_Hello::helloWorld_tie::sayHello()
{
	return _delegate_tie->sayHello();
}


