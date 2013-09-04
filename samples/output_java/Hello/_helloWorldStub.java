//
// _helloWorldStub.java (stub)
//
// File generated: Tue Mar 08 12:28:52 CET 2005
//   by TIDorbJ idl2Java 1.0.4
//

package Hello;

public class _helloWorldStub
 extends org.omg.CORBA.portable.ObjectImpl
 implements helloWorld {

  public java.lang.String[] _ids() {
    return __ids;
  }

  private static java.lang.String[] __ids = {
    "IDL:Hello/helloWorld:1.0"  };

  public java.lang.String sayHello() {
    org.omg.CORBA.Request _request = this._request("sayHello");
    _request.set_return_type(org.omg.CORBA.ORB.init().get_primitive_tc(org.omg.CORBA.TCKind.tk_string));
    _request.invoke();
    java.lang.Exception _exception = _request.env().exception();
    if (_exception != null) {
      if (_exception instanceof org.omg.CORBA.UnknownUserException) {
        org.omg.CORBA.UnknownUserException _userException = 
          (org.omg.CORBA.UnknownUserException) _exception;
      }
      throw (org.omg.CORBA.SystemException) _exception;
    };
    java.lang.String _result;
    _result = _request.return_value().extract_string();
    return _result;
  }


}
