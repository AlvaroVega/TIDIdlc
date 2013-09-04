//
// _helloWorldStub.C (stub)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//

#include "Hello/_helloWorldStub.h" 

#include "TIDorb/portable/TypeCodeFactory.h" 

const CORBA::RepositoryIdSeq_ptr Hello::_helloWorldStub::__init_ids(){
	CORBA::RepositoryIdSeq_ptr ids = new  CORBA::RepositoryIdSeq();
	ids->length(1);
	(*ids)[0]=CORBA::string_dup("IDL:Hello/helloWorld:1.0");
	return ids;
}

const CORBA::RepositoryIdSeq_ptr Hello::_helloWorldStub::__ids =Hello::_helloWorldStub::__init_ids();

const CORBA::RepositoryIdSeq_ptr Hello::_helloWorldStub::_ids()
{
	return __ids;
}

char* Hello::_helloWorldStub::sayHello()
{
    	CORBA::Request_var _request = this->_request("sayHello");
	_request->set_return_type(CORBA::TypeCode::_duplicate(::CORBA::_tc_string));
	_request->invoke();
	CORBA::Exception* _exception = _request->env()->exception();
	if (_exception != NULL)
	{
		CORBA::SystemException_ptr __systemException = CORBA::SystemException::_downcast(_exception);
		if (__systemException != NULL)
		{
			__systemException->_raise();
		}
		throw ::CORBA::INTERNAL();
	}
	char* _l_result;
	const char* const__l_result;
	if (_request->return_value()>>=  const__l_result) {
		_l_result =::CORBA::string_dup(const__l_result);
		return _l_result;
	} else
		throw ::CORBA::INTERNAL();
}


