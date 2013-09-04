//
// helloWorld.h (interface)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//

#include "Hello.h" // Parent Inclusion 

#ifndef _HELLO_HELLOWORLD_H_
#define _HELLO_HELLOWORLD_H_

extern const ::CORBA::TypeCode_ptr _tc_helloWorld;

class helloWorld :  public virtual CORBA::Object
{

	public:
		typedef helloWorld_ptr _ptr_type;
		typedef helloWorld_var _var_type;

	// Constructors & operators 
	protected:
		helloWorld() {};
		virtual ~helloWorld() {};

	private:
		helloWorld(const helloWorld& obj) {};
		void operator= (helloWorld_ptr obj) {};


	public: // Static members
		static Hello::helloWorld_ptr _narrow(const ::CORBA::Object_ptr obj) ;
		static Hello::helloWorld_ptr _unchecked_narrow(const ::CORBA::Object_ptr obj) ;
		static Hello::helloWorld_ptr _duplicate(Hello::helloWorld_ptr val);
		static Hello::helloWorld_ptr _nil();

	public: //Operations, Constants & Attributes Declaration 
		
		virtual char* sayHello() = 0;


}; // end of helloWorldheader definition

class _helloWorldHelper {

	public:
		static ::CORBA::TypeCode_ptr type();

		static const char* id() { return "IDL:Hello/helloWorld:1.0"; }

		static void insert(::CORBA::Any& any, Hello::helloWorld_ptr _value);

		static void insert(::CORBA::Any& any, Hello::helloWorld_ptr* _value);

		static CORBA::Boolean extract(const ::CORBA::Any& any, Hello::helloWorld_ptr& _value);

		static void read(::TIDorb::portable::InputStream& is, Hello::helloWorld_ptr& _value);

		static void write(::TIDorb::portable::OutputStream& os, const Hello::helloWorld_ptr _value);

		static Hello::helloWorld_ptr narrow(const ::CORBA::Object_ptr obj, bool is_a);

};// End of helper definition

class _helloWorldHolder: public virtual ::TIDorb::portable::Streamable {

		public:
		Hello::helloWorld_ptr value; 

		_helloWorldHolder() {value = Hello::helloWorld::_nil();}
		_helloWorldHolder(const Hello::helloWorld_ptr initial){ value=Hello::helloWorld::_duplicate((Hello::helloWorld_ptr)initial);
		}
		::CORBA::TypeCode_ptr _type() const;
		void _write(::TIDorb::portable::OutputStream& outs) const
		{
		Hello::_helloWorldHelper::write(outs,value); 
		}
		void _read(::TIDorb::portable::InputStream& ins) 
		{
		Hello::_helloWorldHelper::read(ins,value); 
		}
}; // end of holder class REF



#endif //_HELLO_HELLOWORLD_H_  

