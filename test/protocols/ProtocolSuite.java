package protocols;

import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.gs.BytecodeOptions;
import gw.test.Suite;
import gw.test.TestEnvironment;
import junit.framework.Test;

public class ProtocolSuite extends Suite
{
  public static Test suite()
  {
    BytecodeOptions.enableAggressiveVerification();
    return new ProtocolSuite()
      .withTestEnvironment( new TestEnvironment() {
        @Override
        public void initializeTypeSystem()
        {
          super.initializeTypeSystem();
          TypeSystem.pushGlobalTypeLoader( new ProtocolTypeLoader( TypeSystem.getCurrentModule() ) );          
        }
      })
      .withTest( BootstrapProtocolTest.class )
      .withTest( ProtocolParserTest.class )
      .withTest( "gw.internal.gosu.protocol.CoreProtocolTest" )
      ;
  }
}
