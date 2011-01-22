package protocols;

import com.sun.jndi.dns.DnsName;
import gw.lang.parser.resources.ResourceKey;
import gw.lang.shell.Gosu;
import junit.framework.TestCase;
import protocols.ProtocolMessages;
import protocols.ProtocolType;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 */
public class ProtocolParserTest extends TestCase
{
  @Override
  protected void setUp() throws Exception {
    Gosu.initGosu(null, makeClasspathFromSystemClasspath());
  }

  private List<File> makeClasspathFromSystemClasspath() {
    List<File> files = new ArrayList<File>();
    for (String s : System.getProperty("java.class.path").split(File.pathSeparator)) {
      File file = new File(s);
      files.add(file);
    }
    return files;
  }

  public void testMustStartWithPackage()
  {
    ProtocolType badType = new ProtocolType( "test.Test", "" );
    assertFalse( badType.isValid() );
    assertHasErrorOnLine( ProtocolMessages.PROTO_EXPECTED_PACKAGE, 1,  badType );
  }

  public void testNamespaceMustMatch()
  {
    for( String s : Arrays.asList("", "asdf", "tes", "tests.", "test.test") )
    {
      ProtocolType badType = new ProtocolType( "test.Test", "package " + s );
      assertFalse( badType.isValid() );
      assertHasErrorOnLine( ProtocolMessages.PROTO_NAMESPACE_MUST_MATCH, 1, badType );
    }
  }

  public void testMustHaveProtocolKeyword()
  {
    for( String s : Arrays.asList( "a", "asdf protocol", "tes", "asdf asdf protocol" ) )
    {
      ProtocolType badType = new ProtocolType( "test.Test", "package test\n" +
                                                            "" + s );
      assertFalse( badType.isValid() );
      assertHasErrorOnLine( ProtocolMessages.PROTO_UNEXPECTED_TOKEN, 2, badType );
    }
  }

  public void testMustHaveProtocolNameMustMatch()
  {
    for( String s : Arrays.asList( "Test1", "Foo", "Bar" ) )
    {
      ProtocolType badType = new ProtocolType( "test.Test", "package test\n" +
                                                            "protocol" + s );
      assertFalse( badType.isValid() );
      assertHasErrorOnLine( ProtocolMessages.PROTO_EXPECTED_NAME_TO_START, 2, badType );
    }
  }

  public void testMustHaveProtocolMustHaveOpenParen()
  {
    for( String s : Arrays.asList( "", " asdf {", "}" ) )
    {
      ProtocolType badType = new ProtocolType( "test.Test", "package test\n" +
                                                            "protocol Test " + s );
      assertFalse( badType.isValid() );
      assertHasErrorOnLine( ProtocolMessages.PROTO_EXPECTED_BRACE_TO_OPEN, 2, badType );
    }
  }

  public void testJibberishInBodyCausesError()
  {
    for( String s : Arrays.asList( " asdf {", "{ }" , "/" ) )
    {
      ProtocolType badType = new ProtocolType( "test.Test", "package test\n" +
                                                            "protocol Test {" + s );
      assertTrue( badType.getSource().toString(), !badType.isValid() );
      assertHasErrorOnLine( ProtocolMessages.PROTO_UNEXPECTED_TOKEN, 2, badType );
    }
  }

  public void testAllPartialVersionsOfProperProtocolParseWithError()
  {
    String s = "package test \n" +
               "\n" +
               "uses java.util.* \n" +
               "uses gw.util.Shell \n" +
               "\n" +
               "protocol Test {\n" +
               "\n" +
               "  function m1 ( s : String ) : String \n" +
               "  function m2 ( s : String )\n" +
               "  function m3 ( ) : String \n" +
               "  function m1 ( s : String, s2 : String ) : String \n" +
               "  property Prop1 : String \n" +
               "  writable property Prop2 : String \n" +
               "}\n";
    ProtocolType goodType = new ProtocolType( "test.Test", s );
    boolean valid = goodType.isValid();
    assertTrue( goodType.getErrorMessage(), valid );
    String[] components = s.split( "\\s" );
    StringBuilder sb = new StringBuilder();
    for( int i = 0; i < components.length; i++ )
    {
      String component = components[i];
      sb.append( " " ).append( component );
      if( i == components.length - 1 )
      {
        sb.append( " bad tokens at end" ); //jack some stuff on in the last loop, to put tokens afterwards
      }
      ProtocolType badTypeType = new ProtocolType( "test.Test", sb.toString() );
      assertFalse( sb.toString(), badTypeType.isValid() );
      ProtocolType badTypeType2 = new ProtocolType( "test.Test", sb.reverse().toString() );
      assertFalse( sb.toString(), badTypeType2.isValid() );
    }
  }

  private void assertHasErrorOnLine( ResourceKey key, int line, ProtocolType type )
  {
    List<ProtocolError> protocolErrors = type.getErrors();
    for( ProtocolError protocolError : protocolErrors )
    {
      if( protocolError.getMessageKey().equals( key ) && protocolError.getLine() == line )
      {
        return;
      }
    }
    fail( "Could not find error with key " + key + " on line " + line + " in "+ type.getErrorMessage() );
  }

}
