//
// helloWorldHolder.java (holder)
//
// File generated: Tue Mar 08 12:28:52 CET 2005
//   by TIDorbJ idl2Java 1.0.4
//

package Hello;

final public class helloWorldHolder
   implements org.omg.CORBA.portable.Streamable {

  public helloWorld value; 
  public helloWorldHolder() {} 
  public helloWorldHolder(helloWorld initial) {
    value = initial;
  }

  public void _read(org.omg.CORBA.portable.InputStream is) {
    value = Hello.helloWorldHelper.read(is);
  };

  public void _write(org.omg.CORBA.portable.OutputStream os) {
    Hello.helloWorldHelper.write(os, value);
  };

  public org.omg.CORBA.TypeCode _type() {
    return Hello.helloWorldHelper.type();
  };

}
