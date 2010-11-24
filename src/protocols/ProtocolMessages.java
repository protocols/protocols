package protocols;

import gw.config.CommonServices;
import gw.lang.parser.resources.ResourceKey;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

/**
 * Class description...
 *
 * @author cgross
 */
public class ProtocolMessages
{
  private static final Map<ResourceKey, String> MESSAGES = new HashMap<ResourceKey, String>();

  //ResourceKeys
  public static final ResourceKey PROTO_EXPECTED_PACKAGE = key( "PROTO_EXPECTED_PACKAGE", "Expected package to start protocol." );
  public static final ResourceKey PROTO_EXPECTED_BRACE_TO_CLOSE = key("PROTO_EXPECTED_BRACE_TO_CLOSE", "Expected closing brace to end ");
  public static final ResourceKey PROTO_EXPECTED_BRACE_TO_OPEN = key("PROTO_EXPECTED_BRACE_TO_OPEN", "Expected open brace to start protocol definition");
  public static final ResourceKey PROTO_EXPECTED_NAME_TO_START = key("PROTO_EXPECTED_NAME_TO_START", "Expected name \"{0}\" to start protocol definition");
  public static final ResourceKey PROTO_UNEXPECTED_MSG_AT_END = key("PROTO_UNEXPECTED_MSG_AT_END", "Unexpected token after protocol definition ");
  public static final ResourceKey PROTO_EXPECTED_TYPE_LITERAL = key("PROTO_EXPECTED_TYPE_LITERAL", "Expected Type Literal");
  public static final ResourceKey PROTO_EXPECTED_OPEN_PAREN = key("PROTO_EXPECTED_OPEN_PAREN", "Expected a '(' to open a function definition.");
  public static final ResourceKey PROTO_EXPECTED_PARAM_NAME = key("PROTO_EXPECTED_PARAM_NAME", "Expected a name for the parameter.");
  public static final ResourceKey PROTO_EXPECTED_COLON = key("PROTO_EXPECTED_COLON", "Expected a ':'");
  public static final ResourceKey PROTO_EXPECTED_FUNCTION_NAME = key("PROTO_EXPECTED_FUNCTION_NAME", "Expected a function name");
  public static final ResourceKey PROTO_EXPECTED_PROPERTY_NAME = key("PROTO_EXPECTED_PROPERTY_NAME", "Expected a property name");
  public static final ResourceKey PROTO_WRITABLE_ON_METHOD = key("PROTO_WRITABLE_ON_METHOD", "'writable' modifier is only allowed on properties");
  public static final ResourceKey PROTO_NAMESPACE_MUST_MATCH = key("PROTO_NAMESPACE_MUST_MATCH", "Package must match the namespace for this protocol: {0}");
  public static final ResourceKey PROTO_UNEXPECTED_TOKEN = key("PROTO_UNEXPECTED_TOKEN", "Unexpected Token : \"{0}\"");
  public static final ResourceKey PROTO_NAME_ALREADY_USED = key("PROTO_NAME_ALREADY_USED", "The parameter name \"{0}\" is already used.");
  public static final ResourceKey PROTO_CANNOT_NEST_PROTOCOLS_HERE = key("PROTO_CANNOT_NEST_PROTOCOLS_HERE", "Protocols can only be nested at covariant positions (i.e. function return types and read only property types)");

  public static String format( ResourceKey key, Object... values )
  {
    if( CommonServices.getGosuLocalizationService().exists( key ) )
    {
      return CommonServices.getGosuLocalizationService().localize( key, values );
    }
    else
    {
      return MessageFormat.format( MESSAGES.get( key ), values );
    }
  }
  
  private static ResourceKey key( String strKey, String msg )
  {
    ResourceKey resourceKey = new ResourceKey( strKey ) {
      @Override
      public String toString()
      {
        return getKey();
      }
    };
    MESSAGES.put( resourceKey, msg );
    return resourceKey;
  }
}
