//
// helloWorld.h (skeleton)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//

#include "POA_Hello.h" // Parent Inclusion 

#ifndef _POA_HELLO_HELLOWORLD_H_
#define _POA_HELLO_HELLOWORLD_H_

struct __helloWorld__ltstr{
		bool operator()(const char* s1, const char* s2) const;
};//end __helloWorld__ltstr struct

struct __POA_helloWorldMAP {
		map<const char*,int,__helloWorld__ltstr> _methods;
		__POA_helloWorldMAP();
};// end of __POA_helloWorldMAP struct 

class helloWorld: public virtual PortableServer::DynamicImplementation
{

	public:
		Hello::helloWorld_ptr _this();

		void invoke(::CORBA::ServerRequest_ptr _request);
		const CORBA::RepositoryIdSeq_ptr _all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectId);
		CORBA::RepositoryId _primary_interface(const PortableServer::ObjectId& oid,PortableServer::POA_ptr poa);

		
		virtual char* sayHello() = 0;

	private:

		virtual const CORBA::RepositoryIdSeq_ptr _ids();
		static const CORBA::RepositoryIdSeq_ptr __ids;
		static const CORBA::RepositoryIdSeq_ptr __init_ids();
		static __POA_helloWorldMAP _mapped_methods; 
}; // end of helloWorld



#endif //_POA_HELLO_HELLOWORLD_H_  


//
// helloWorld_tie.h (tie)
//
// File generated: Tue Mar 08 12:28:22 CET 2005
//   by TIDIdlc idl2cpp 1.1.0
//

#include "POA_Hello.h" // Parent Inclusion 

#ifndef _POA_HELLO_HELLOWORLD_TIE_H_
#define _POA_HELLO_HELLOWORLD_TIE_H_

class helloWorld_tie: public helloWorld{

	public:
		helloWorld_tie(Hello::helloWorld_ptr delegate);

		Hello::helloWorld_ptr _delegate();

		virtual const CORBA::RepositoryIdSeq_ptr _all_interfaces(PortableServer::POA_ptr poa, const PortableServer::ObjectId& objectID);

		virtual const CORBA::RepositoryIdSeq_ptr _ids();

	// Operations....
	public:

		char* sayHello();

	private:
		static const CORBA::RepositoryIdSeq_ptr __ids;
		static const CORBA::RepositoryIdSeq_ptr __init_ids();
		Hello::helloWorld_ptr _delegate_tie;
}; // end of helloWorld_tie class.



#endif //_POA_HELLO_HELLOWORLD_TIE_H_  

