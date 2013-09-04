//
// helloWorld_ext.h (from interface)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//   external Any operators definition File.//


#ifndef _HELLO_HELLO_WORLD__EXT_H_
#define _HELLO_HELLO_WORLD__EXT_H_

		#include "Hello/helloWorld.h"
		inline void operator <<= (::CORBA::Any& any, Hello::helloWorld_ptr _value)
		{
			Hello::_helloWorldHelper::insert(any,_value);
		}
		inline void operator <<= (::CORBA::Any& any, Hello::helloWorld_ptr* _value)
		{
			Hello::_helloWorldHelper::insert(any,_value);
		}
		inline CORBA::Boolean operator >>= (const ::CORBA::Any& any,  Hello::helloWorld_ptr& _value)
		{
			Hello::_helloWorldHelper::extract(any,_value);
			return true;
		}

#endif //_HELLO_HELLO_WORLD__EXT_H_

