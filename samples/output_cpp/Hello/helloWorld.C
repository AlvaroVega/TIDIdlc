//
// helloWorld.C (interface)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//

#include "Hello/helloWorld.h" 

#include "TIDorb/portable/TypeCodeFactory.h" 


//Static Members
Hello::helloWorld_ptr Hello::helloWorld::_narrow(const ::CORBA::Object_ptr obj) 
{
	return Hello::_helloWorldHelper::narrow(obj, false);
}

Hello::helloWorld_ptr Hello::helloWorld::_unchecked_narrow(const ::CORBA::Object_ptr obj) 
{
	return Hello::_helloWorldHelper::narrow(obj, true);
}

Hello::helloWorld_ptr Hello::helloWorld::_duplicate(Hello::helloWorld_ptr ref){
	if(!ref)
		return ref;
	try {
		Hello::_helloWorldStub* stub = dynamic_cast<Hello::_helloWorldStub*> (ref);
		if(stub) {
			stub->_add_ref();
			return stub;
		}
	} catch (...) {
		throw CORBA::INTERNAL(0,CORBA::COMPLETED_NO);
	}
}// end of Duplicate.

Hello::helloWorld_ptr Hello::helloWorld::_nil(){
	 return Hello::helloWorld::_narrow(CORBA::Object::_nil());
}

void Hello::_helloWorldHelper::insert(::CORBA::Any& any, Hello::helloWorld_ptr _value) {
	TIDorb::portable::Any& delegate = any.delegate();
	delegate.insert_Object((CORBA::Object_ptr)_value, type());
}

void Hello::_helloWorldHelper::insert(::CORBA::Any& any, Hello::helloWorld_ptr* _value) {
	TIDorb::portable::Any& delegate = any.delegate();
	delegate.insert_Object((CORBA::Object_ptr)(*_value), type());
	CORBA::release(*_value);
}

CORBA::Boolean Hello::_helloWorldHelper::extract(const ::CORBA::Any& any, Hello::helloWorld_ptr& _value) {
	::TIDorb::portable::Any& delegate = any.delegate();
	CORBA::Object_ptr obj;
	bool ret = delegate.extract_Object(obj);
	if (ret)
		_value = Hello::_helloWorldHelper::narrow(obj,false);
	return ret;
}

CORBA::TypeCode_ptr Hello::_helloWorldHelper::type() {
	return CORBA::TypeCode::_duplicate(Hello::_tc_helloWorld);
}

const ::CORBA::TypeCode_ptr Hello::_tc_helloWorld=Hello::_helloWorldHelper::type();

void Hello::_helloWorldHelper::read(::TIDorb::portable::InputStream& is, Hello::helloWorld_ptr& _value) {
	::CORBA::Object_ptr obj;
	is.read_Object(obj);
	_value = Hello::_helloWorldHelper::narrow(obj, true); 
}

Hello::helloWorld_ptr Hello::_helloWorldHelper::narrow(const ::CORBA::Object_ptr obj, bool is_a) {
	if (CORBA::is_nil(obj))
		return NULL;
	Hello::helloWorld_ptr _concrete_ref = dynamic_cast<Hello::helloWorld_ptr> (obj);
 	if (!CORBA::is_nil(_concrete_ref))
		return Hello::helloWorld::_duplicate(_concrete_ref);
	if (is_a || obj->_is_a(id())) {
		Hello::_helloWorldStub* result = new Hello::_helloWorldStub();
		if(result==NULL) 
			throw ::CORBA::NO_MEMORY();
		::TIDorb::portable::Stub* __aux= dynamic_cast< ::TIDorb::portable::Stub*>(obj);
 		if (__aux!=NULL) {
			result->_set_delegate(__aux->_get_delegate());
			return (Hello::helloWorld_ptr)result;
		}
	}
	return Hello::helloWorld::_nil();
}

void Hello::_helloWorldHelper::write(::TIDorb::portable::OutputStream& os, const Hello::helloWorld_ptr _value) {
	os.write_Object((::CORBA::Object_ptr)_value);
}

::CORBA::TypeCode_ptr Hello::_helloWorldHolder::_type() const { return CORBA::TypeCode::_duplicate(Hello::_tc_helloWorld); }

