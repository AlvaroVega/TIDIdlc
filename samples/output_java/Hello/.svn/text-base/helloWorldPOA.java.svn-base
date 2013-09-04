//
// helloWorldPOA.java (skeleton)
//
// File generated: Tue Mar 08 12:28:52 CET 2005
//   by TIDorbJ idl2Java 1.0.4
//

package Hello;

abstract public class helloWorldPOA
 extends org.omg.PortableServer.DynamicImplementation
 implements helloWorldOperations {

  public helloWorld _this() {
    return helloWorldHelper.narrow(super._this_object());
  };

  public helloWorld _this(org.omg.CORBA.ORB orb) {
    return helloWorldHelper.narrow(super._this_object(orb));
  };

  public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectID) {
    return __ids;
  };

  private static java.lang.String[] __ids = {
    "IDL:Hello/helloWorld:1.0"
  };

  private static java.util.Dictionary _methods = new java.util.Hashtable();
  static {
    _methods.put("sayHello", new Integer(0));
  }

  public void invoke(org.omg.CORBA.ServerRequest _request) {
    java.lang.Object _method = _methods.get(_request.operation());
    if (_method == null) {
      throw new org.omg.CORBA.BAD_OPERATION(_request.operation());
    }
    int _method_id = ((java.lang.Integer)_method).intValue();
    switch(_method_id) {
    case 0: {
      org.omg.CORBA.NVList _params = _orb().create_list(0);
      _request.arguments(_params);
      java.lang.String _result = this.sayHello();
      org.omg.CORBA.Any _resultAny = _orb().create_any();
      _resultAny.insert_string(_result);
      _request.set_result(_resultAny);
      return;
    }
    }
  }
}
