//
// helloWorldPOATie.java (tie)
//
// File generated: Tue Mar 08 12:28:52 CET 2005
//   by TIDorbJ idl2Java 1.0.4
//

package Hello;

public class helloWorldPOATie
 extends helloWorldPOA
 implements helloWorldOperations {

  private helloWorldOperations _delegate;
  public helloWorldPOATie(helloWorldOperations delegate) {
    this._delegate = delegate;
  };

  public helloWorldOperations _delegate() {
    return this._delegate;
  };

  public java.lang.String[] _all_interfaces(org.omg.PortableServer.POA poa, byte[] objectID) {
    return __ids;
  };

  private static java.lang.String[] __ids = {
    "IDL:Hello/helloWorld:1.0"  };

  public java.lang.String sayHello() {
    return this._delegate.sayHello(
    );
  };


}
