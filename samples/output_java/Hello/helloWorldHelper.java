//
// helloWorldHelper.java (helper)
//
// File generated: Tue Mar 08 12:28:52 CET 2005
//   by TIDorbJ idl2Java 1.0.4
//

package Hello;

abstract public class helloWorldHelper {

  private static org.omg.CORBA.ORB _orb() {
    return org.omg.CORBA.ORB.init();
  }

  private static org.omg.CORBA.TypeCode _type = null;
  public static org.omg.CORBA.TypeCode type() {
    if (_type == null) {
      _type = _orb().create_interface_tc(id(), "helloWorld");
    }
    return _type;
  }

  public static String id() {
    return "IDL:Hello/helloWorld:1.0";
  };

  public static void insert(org.omg.CORBA.Any any, helloWorld value) {
    any.insert_Object((org.omg.CORBA.Object)value, type());
  };

  public static helloWorld extract(org.omg.CORBA.Any any) {
    org.omg.CORBA.Object obj = any.extract_Object();
    helloWorld value = narrow(obj);
    return value;
  };

  public static helloWorld read(org.omg.CORBA.portable.InputStream is) {
    return narrow(is.read_Object(), true); 
  }

  public static void write(org.omg.CORBA.portable.OutputStream os, helloWorld val) {
    if (!(os instanceof org.omg.CORBA_2_3.portable.OutputStream)) {;
      throw new org.omg.CORBA.BAD_PARAM();
    };
    if (val != null && !(val instanceof org.omg.CORBA.portable.ObjectImpl)) {;
      throw new org.omg.CORBA.BAD_PARAM();
    };
    os.write_Object((org.omg.CORBA.Object)val);
  }

  public static helloWorld narrow(org.omg.CORBA.Object obj) {
    return narrow(obj, false);
  }

  public static helloWorld unchecked_narrow(org.omg.CORBA.Object obj) {
    return narrow(obj, true);
  }

  private static helloWorld narrow(org.omg.CORBA.Object obj, boolean is_a) {
    if (obj == null) {
      return null;
    }
    if (obj instanceof helloWorld) {
      return (helloWorld)obj;
    }
    if (is_a || obj._is_a(id())) {
      _helloWorldStub result = (_helloWorldStub)new _helloWorldStub();
      ((org.omg.CORBA.portable.ObjectImpl) result)._set_delegate
        (((org.omg.CORBA.portable.ObjectImpl) obj)._get_delegate());
      return (helloWorld)result;
    }
    throw new org.omg.CORBA.BAD_PARAM();
  }

}
