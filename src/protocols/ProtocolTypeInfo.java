package protocols;

import gw.lang.reflect.BaseTypeInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.MethodInfoBuilder;
import gw.lang.reflect.ParameterInfoBuilder;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class ProtocolTypeInfo extends BaseTypeInfo implements ITypeInfo
{
  private List<IMethodInfo> _methods;
  private Map<String, IPropertyInfo> _properties;

  public ProtocolTypeInfo( ProtocolType protocolType )
  {
    super( protocolType );
    _methods = new ArrayList<IMethodInfo>();
    _properties = new HashMap<String, IPropertyInfo>();
    if( !protocolType.isAnonymous() )
    {
      ProtocolParser protocolParser = new ProtocolParser( this.getOwnersType() );
      protocolParser.parse();
      initTypeInfo( protocolParser );
      addSpecialMethods( protocolType );
    }
  }

  public void initTypeInfo( ProtocolParser protocolParser )
  {
    for( ProtocolMethodDescriptor md : protocolParser.getMethodDescriptors() )
    {
      _methods.add( new ProtocolMethodInfo( this, md.getName(), md.getArgs(), md.getReturnType() ) );
    }
    for( ProtocolPropertyDescriptor pd : protocolParser.getPropertyDescriptors() )
    {
      _properties.put( pd.getName(), new ProtocolPropertyInfo( this, pd.getReadOnly(), pd.getName(), pd.getType() ) );
    }
  }

  private void addSpecialMethods( final ProtocolType protocolType )
  {
    IMethodInfo isConformedToBy = new MethodInfoBuilder()
      .withName( "isConformedToBy" )
      .withStatic()
      .withReturnType( IJavaType.pBOOLEAN )
      .withParameters( new ParameterInfoBuilder().withName( "o" ).withType( Object.class ) )
      .withCallHandler( new IMethodCallHandler()
      {
        @Override
        public Object handleCall( Object ctx, Object... args )
        {
          ((ProtocolType)getOwnersType()).verify();
          Object arg = args[0];
          IType iType = TypeSystem.getFromObject( arg );
          return protocolType.isAssignableFrom( iType );
        }
      } ).
        build( this );
    _methods.add( isConformedToBy );
    IMethodInfo duck = new MethodInfoBuilder()
      .withName( "duck" )
      .withStatic()
      .withReturnType( protocolType )
      .withParameters( new ParameterInfoBuilder().withName( "o" ).withType( Object.class ) )
      .withCallHandler( new IMethodCallHandler()
      {
        @Override
        public Object handleCall( Object ctx, Object... args )
        {
          ((ProtocolType)getOwnersType()).verify();
          Object arg = args[0];
          IType iType = TypeSystem.getFromObject( arg );
          if( protocolType.isAssignableFrom( iType ) )
          {
            return arg;
          }
          else
          {
            return null;
          }
        }
      } ).
        build( this );
    _methods.add( duck );
  }

  @Override
  public List<? extends IMethodInfo> getMethods()
  {
    return _methods;
  }

  @Override
  public IMethodInfo getMethod( CharSequence methodName, IType... params )
  {
    return FIND.method( getMethods(), methodName, params );
  }

  @Override
  public IPropertyInfo getProperty( CharSequence propName )
  {
    return _properties.get( propName.toString() );
  }

  @Override
  public List<? extends IPropertyInfo> getProperties()
  {
    return new ArrayList<IPropertyInfo>( _properties.values() );
  }

  @Override
  public ProtocolType getOwnersType()
  {
    return (ProtocolType)super.getOwnersType();
  }
}
