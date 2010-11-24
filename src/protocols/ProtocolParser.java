package protocols;

import gw.lang.parser.GosuParserFactory;
import gw.lang.parser.IExpression;
import gw.lang.parser.IGosuParser;
import gw.lang.parser.IParseIssue;
import gw.lang.parser.IParsedElement;
import gw.lang.parser.ISourceCodeTokenizer;
import gw.lang.parser.Keyword;
import gw.lang.parser.expressions.ITypeLiteralExpression;
import gw.lang.parser.resources.ResourceKey;
import gw.lang.parser.statements.IUsesStatementList;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.module.IFile;
import gw.util.GosuExceptionUtil;
import gw.util.Pair;
import gw.util.StreamUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ProtocolParser
{
  private IGosuParser _gosuParser;
  private ProtocolType _protocol;
  private HashSet<ProtocolMethodDescriptor> _methods;
  private HashMap<String, ProtocolPropertyDescriptor> _properties;

  public ProtocolParser(ProtocolType protocol)
  {
    _protocol = protocol;
    _methods = new HashSet<ProtocolMethodDescriptor>();
    _properties = new HashMap<String, ProtocolPropertyDescriptor>();
    Object src = getProtocol().getSource();
    if( src instanceof String )
    {
      _gosuParser = GosuParserFactory.createParser( (String) src );
    }
    else if( src instanceof IFile )
    {
      try
      {
        _gosuParser = GosuParserFactory.createParser(
          StreamUtil.getContent(
            StreamUtil.getInputStreamReader( ((IFile)src).openInputStream() ) ) );
      }
      catch( IOException e )
      {
        throw GosuExceptionUtil.forceThrow( e );
      }
    }
    else
    {
      _gosuParser = GosuParserFactory.createParser("");
      _gosuParser.setTokenizer( ((ISourceCodeTokenizer)src) );
    }
  }

  public void parse()
  {
    parseProtocol();
  }

  private ProtocolType getProtocol()
  {
    return _protocol;
  }

  private void parseProtocol()
  {
    try
    {
      getTokenizer().nextToken();
    }
    catch( IOException e )
    {
      throw GosuExceptionUtil.forceThrow( e );
    }

    verify( parsePackage(), ProtocolMessages.PROTO_EXPECTED_PACKAGE );

    IUsesStatementList stmts = _gosuParser.parseUsesStatementList( false );
    if( stmts != null )
    {
      addErrors( stmts );
    }

    while( !match( new Token(), "protocol" ) && moreTokens() )
    {
      unexpectedToken();
    }

    Token protoName = new Token();
    if( match( protoName, ISourceCodeTokenizer.TT_WORD ) )
    {
      parseProtocolBody();
    }
    else
    {
      addError( ProtocolMessages.PROTO_EXPECTED_NAME_TO_START, getProtocol().getRelativeName() );
    }
    verify( getTokenizer().isEOF() || getProtocol().isAnonymous(), ProtocolMessages.PROTO_UNEXPECTED_MSG_AT_END );
  }

  private void parseProtocolBody()
  {
    if( match( null, '{' ) )
    {
      while( parseFeatureSignature() )
      {
        // eat all the feature signatures
      }
      verify( match( null, '}' ), ProtocolMessages.PROTO_EXPECTED_BRACE_TO_CLOSE );
    }
    else
    {
      addError( ProtocolMessages.PROTO_EXPECTED_BRACE_TO_OPEN );
    }
  }

  private boolean parseFeatureSignature()
  {
    boolean writeable = match( new Token(), "writable" );
    if( match( new Token(), Keyword.KW_function ) )
    {
      Token methodName = new Token();
      verify( match( methodName, ISourceCodeTokenizer.TT_WORD ), ProtocolMessages.PROTO_EXPECTED_FUNCTION_NAME );
      log( "found method " + methodName );
      List<Pair<String, IType>> args = parseArgs();
      log( args.toString() );
      IType returnType = IJavaType.pVOID;
      if( match( null, ":", ISourceCodeTokenizer.TT_OPERATOR ) )
      {
        returnType = parseTypeLiteral( true );
        log( "return " + returnType );
      }
      addMethod( methodName.getStringValue(), args, returnType );
      verify( !writeable, ProtocolMessages.PROTO_WRITABLE_ON_METHOD );
    }
    else if( match( new Token(), Keyword.KW_property ) )
    {
      Token property = new Token();
      match( property, ISourceCodeTokenizer.TT_WORD );
      log( "found property " + property );
      match( null, ":", ISourceCodeTokenizer.TT_OPERATOR );
      IType type = parseTypeLiteral( !writeable );
      log( "return " + type );
      addProperty( !writeable, property.getStringValue(), type );
    }
    else
    {
      if( match( null, null, '}', true ) )
      {
        return false;
      }
      else if( !moreTokens() )
      {
        return false;
      }
      else
      {
        unexpectedToken();
        return true;
      }
    }
    return true;
  }

  private void addProperty( boolean readOnly, String name, IType type )
  {
    _properties.put( name, new ProtocolPropertyDescriptor( readOnly, name, type ) );
  }

  private void addMethod( String stringValue, List<Pair<String, IType>> args, IType returnType )
  {
    ProtocolMethodDescriptor md = new ProtocolMethodDescriptor( stringValue, args, returnType );
    if( verify( !_methods.contains( md ), null ) )
    {
      _methods.add( md );
    }
  }

  private IType parseTypeLiteral( boolean covariantPosition )
  {
    if( match( new Token(), "protocol" ) )
    {
      verify( covariantPosition, ProtocolMessages.PROTO_CANNOT_NEST_PROTOCOLS_HERE );
      ProtocolType innerProtocol = getProtocol().getNextInnerProtocol( getTokenizer() );
      ProtocolParser innerParser = new ProtocolParser( innerProtocol );
      innerParser.parseInnerProtocol( this );
      ProtocolTypeInfo innerTypeInfo = new ProtocolTypeInfo( innerProtocol );
      innerTypeInfo.initTypeInfo( innerParser );
      innerProtocol.setTypeInfo( innerTypeInfo );
      return innerProtocol;
    }
    else if( _gosuParser.parseTypeLiteral() )
    {
      IExpression expression = _gosuParser.popExpression();
      addErrors( expression );
      if( expression instanceof ITypeLiteralExpression )
      {
        IType type = ((ITypeLiteralExpression)expression).getType().getType();
        verify( covariantPosition || !(type instanceof IProtocolType), ProtocolMessages.PROTO_CANNOT_NEST_PROTOCOLS_HERE );
        return type;
      }
    }
    addError( ProtocolMessages.PROTO_EXPECTED_TYPE_LITERAL );
    return TypeSystem.getErrorType();
  }

  private void parseInnerProtocol(ProtocolParser outerParser)
  {
    _gosuParser.setTypeUsesMap( outerParser._gosuParser.getTypeUsesMap() );
    parseProtocolBody();
  }


  private List<Pair<String, IType>> parseArgs()
  {
    verify( match( null, '(' ), ProtocolMessages.PROTO_EXPECTED_OPEN_PAREN );
    ArrayList<Pair<String, IType>> lst = new ArrayList<Pair<String, IType>>();
    if( !match( null, ')' ) )
    {
      HashSet<String> argNames = new HashSet<String>();
      while( moreTokens() )
      {
        Token paramName = new Token();
        if( verify( match( paramName, ISourceCodeTokenizer.TT_WORD ), ProtocolMessages.PROTO_EXPECTED_PARAM_NAME ) )
        {
          String name = paramName.getStringValue();
          verify( !argNames.contains( name ), ProtocolMessages.PROTO_NAME_ALREADY_USED, name );
          argNames.add( name );
        }
        verify( match( null, ":", ISourceCodeTokenizer.TT_OPERATOR ), ProtocolMessages.PROTO_EXPECTED_COLON );
        IType type = parseTypeLiteral(false);
        lst.add( Pair.make( paramName.getStringValue(), type ) );
        if( match( null, ')' ) )
        {
          break;
        }
        else if( !match( null, ',' ) )
        {
          unexpectedToken();
        }
      }
    }
    return lst;
  }

  private boolean parsePackage()
  {
    if( match( null, Keyword.KW_package ) )
    {
      String s = _gosuParser.parseDotPathWord();
      verify( getProtocol().getNamespace().equals( s ), ProtocolMessages.PROTO_NAMESPACE_MUST_MATCH, getProtocol().getNamespace() );
      return true;
    }
    return false;
  }

  private void unexpectedToken()
  {
    addError( ProtocolMessages.PROTO_UNEXPECTED_TOKEN, getTokenizer().getTokenAsString() );
    try
    {
      getTokenizer().nextToken();
    }
    catch( IOException e )
    {
      throw GosuExceptionUtil.forceThrow( e );
    }
  }

  private boolean verify( boolean test, ResourceKey resourceKey, Object... args )
  {
    if( !test )
    {
      addError( resourceKey, args );
    }
    return test;
  }

  private void addError( ResourceKey resourceKey, Object... args )
  {
    getProtocol().addError( new ProtocolError( getTokenizer().getLineNumber(), getTokenizer().getTokenColumn(),
                                               ProtocolMessages.format( resourceKey, args ), resourceKey ) );
  }

  private void verify( boolean test, ResourceKey resourceKey, Token t, Object... args )
  {
    if( !test )
    {
      addError( resourceKey, t, args );
    }
  }

  private void addError( ResourceKey resourceKey, Token t, Object... args )
  {
    getProtocol().addError( new ProtocolError( t.getLineNumber(), t.getTokenColumn(),
                                               ProtocolMessages.format( resourceKey, args ), resourceKey ) );
  }

  private void addErrors( IParsedElement elt )
  {
    for( IParseIssue exception : elt.getParseExceptions() )
    {
      getProtocol().addError( new ProtocolError( exception.getLine(), exception.getColumn(),
                                                 exception.getPlainMessage(), exception.getMessageKey() ) );
    }
  }

  private boolean moreTokens()
  {
    return !getTokenizer().isEOF();
  }

  private void log( String s )
  {
    System.out.println( s );
  }

  public ISourceCodeTokenizer getTokenizer()
  {
    return _gosuParser.getTokenizer();
  }

  public Iterable<ProtocolMethodDescriptor> getMethodDescriptors()
  {
    return _methods;
  }

  public Iterable<ProtocolPropertyDescriptor> getPropertyDescriptors()
  {
    return _properties.values();
  }

  public static final class Token
  {
    int _iType;
    String _strValue;
    int _iDocPosition;
    int _iLine;
    int _iColumn;

    public Token()
    {
      _iType = 0;
      _strValue = null;
      _iDocPosition = 0;
      _iLine = 0;
    }

    void init( ISourceCodeTokenizer tokenizer )
    {
      _iType = tokenizer.getType();
      _strValue = maybeInternStringToken( tokenizer );
      _iDocPosition = tokenizer.getTokenStart();
      _iLine = tokenizer.getLineNumber();
      _iColumn = tokenizer.getTokenColumn();
    }

    private String maybeInternStringToken( ISourceCodeTokenizer tokenizer )
    {
      String strValue = tokenizer.getStringValue();
      if( strValue != null && strValue.length() <= 32 )
      {
        //strValue = strValue.intern();
      }
      return strValue;
    }

    public int getTokenStart()
    {
      return _iDocPosition;
    }

    public String getStringValue()
    {
      return _strValue;
    }

    public int getLineNumber()
    {
      return _iLine;
    }

    public int getTokenColumn()
    {
      return _iColumn;
    }

    @Override
    public String toString()
    {
      return "Token : [" + _strValue + "]";
    }
  }

  private boolean match( Token T, String token )
  {
    return match( T, token, 0, false );
  }

  private boolean match( Token T, int iType )
  {
    return match( T, null, iType, false );
  }

  private boolean match( Token T, String token, int iType )
  {
    return match( T, token, iType, false );
  }

  private boolean match( Token T, String token, int iType, boolean bPeek )
  {
    ISourceCodeTokenizer tokenizer = getTokenizer();
    return match( T, token, iType, bPeek, tokenizer );
  }

  private boolean match( Token T, Keyword token )
  {
    return match( T, token, false );
  }

  private boolean match( Token T, Keyword token, boolean bPeek )
  {
    boolean bMatch = false;

    if( T != null )
    {
      T.init( getTokenizer() );
    }

    ISourceCodeTokenizer tokenizer = getTokenizer();
    if( ISourceCodeTokenizer.TT_KEYWORD == tokenizer.getType() )
    {
      bMatch = token.toString().equalsIgnoreCase( tokenizer.getStringValue() );
    }

    if( bMatch && !bPeek )
    {
      try
      {
        tokenizer.nextToken();
      }
      catch( IOException e )
      {
        // ignore
      }
    }

    return bMatch;
  }

  private static boolean match( Token T, String token, int iType, boolean bPeek, ISourceCodeTokenizer tokenizer )
  {
    boolean bMatch = false;

    if( T != null )
    {
      T.init( tokenizer );
    }

    if( token != null )
    {
      if( (iType == tokenizer.getType()) || ((iType == 0) && (tokenizer.getType() == ISourceCodeTokenizer.TT_WORD)) )
      {
        bMatch = token.equalsIgnoreCase( tokenizer.getStringValue() );
      }
    }
    else
    {
      bMatch = (tokenizer.getType() == iType) || isValueKeyword( iType, tokenizer );
    }

    if( bMatch && !bPeek )
    {
      try
      {
        tokenizer.nextToken();
      }
      catch( IOException e )
      {
        // ignore
      }
    }

    return bMatch;
  }

  private static boolean isValueKeyword( int iType, ISourceCodeTokenizer tokenizer )
  {
    return iType == ISourceCodeTokenizer.TT_WORD &&
           tokenizer.getType() == ISourceCodeTokenizer.TT_KEYWORD &&
           Keyword.isReservedValue( tokenizer.getStringValue() );
  }

}
