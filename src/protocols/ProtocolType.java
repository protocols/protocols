package protocols;

import gw.lang.parser.ISourceCodeTokenizer;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaType;
import gw.lang.reflect.module.IFile;
import gw.util.GosuClassUtil;
import gw.util.concurrent.LazyVar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author cgross
 */
public class ProtocolType extends TypeBase implements IProtocolType
{
  private ProtocolTypeLoader _loader;
  private String _name;
  private volatile ProtocolTypeInfo _typeInfo;
  private List<ProtocolError> _errors;
  private List<ProtocolType> _innerProtocols;
  private ProtocolType _outer;
  private Object _source;

  public ProtocolType( ProtocolTypeLoader tl, String name, IFile file )
  {
    init( tl, null, name, file );
  }

  // test only
  ProtocolType( String name, String source )
  {
    init( null, null, name, source );
  }

  private ProtocolType( ProtocolType outer, String name, ISourceCodeTokenizer t )
  {
    init( (ProtocolTypeLoader)outer.getTypeLoader(), outer, name, t );
  }

  private void init( ProtocolTypeLoader loader, ProtocolType outer, String name, Object source )
  {
    _loader = loader;
    _outer = outer;
    _name = name;
    _source = source;
    _errors = new ArrayList<ProtocolError>();
    _innerProtocols = new ArrayList<ProtocolType>();
    _typeInfo = null;
  }

  @Override
  public String getName()
  {
    return _name;
  }

  @Override
  public String getRelativeName()
  {
    return GosuClassUtil.getShortClassName( _name );
  }

  @Override
  public String getNamespace()
  {
    return GosuClassUtil.getPackage( _name );
  }

  @Override
  public ITypeLoader getTypeLoader()
  {
    return _loader;
  }

  @Override
  public IType getSupertype()
  {
    return IJavaType.OBJECT;
  }

  @Override
  public List<? extends IType> getInterfaces()
  {
    return Collections.emptyList();
  }

  @Override
  public ProtocolTypeInfo getTypeInfo()
  {
    if( _typeInfo == null )
    {
      TypeSystem.lock();
      try
      {
        if( _typeInfo == null )
        {
          _typeInfo = new ProtocolTypeInfo( this );
        }
      }
      finally
      {
        TypeSystem.unlock();
      }
    }
    return _typeInfo;
  }

  @Override
  public boolean isAssignableFrom( IType type )
  {
    if( type.equals( this ) )
    {
      return true;
    }
    else
    {
      return conformsToMe( type );
    }
  }

  private boolean conformsToMe( IType type )
  {
    for( IMethodInfo methodInfo : getTypeInfo().getMethods() )
    {
      if( methodInfo instanceof ProtocolMethodInfo )
      {
        IMethodInfo mi = ITypeInfo.FIND.callableMethod( type.getTypeInfo().getMethods(), methodInfo.getName(), ((ProtocolMethodInfo)methodInfo).getParamTypes() );
        if( mi == null || !methodInfo.getReturnType().isAssignableFrom( mi.getReturnType() ) )
        {
          return false;
        }
      }
    }

    for( IPropertyInfo pi : getTypeInfo().getProperties() )
    {
      if( pi instanceof ProtocolPropertyInfo )
      {
        IPropertyInfo property = type.getTypeInfo().getProperty( pi.getName() );
        if( property == null )
        {
          return false;
        }
        if( pi.isWritable() )
        {
          if( !property.getFeatureType().equals( pi.getFeatureType() ) || !property.isWritable() )
          {
            return false;
          }
        }
        else
        {
          if( !pi.getFeatureType().isAssignableFrom( property.getFeatureType() ) )
          {
            return false;
          }
        }
      }
    }
    return true;
  }

  @Override
  public boolean isValid()
  {
    getTypeInfo(); // force compile
    return _errors.isEmpty();
  }

  public String getErrorMessage()
  {
    StringBuilder sb = new StringBuilder();
    sb.append( "Errors in " ).append( getFileName() ).append( ":\n\n" );
    for( ProtocolError error : _errors )
    {
      sb.append( "Line : " ).append( error.getLine() ).append( ", Column : " ).append( error.getColumn() ).append( ":\n" );
      sb.append( "    " ).append( error.getMessage() );
      sb.append( "\n\n" );
    }
    return sb.toString();
  }

  public void addError( ProtocolError protocolError )
  {
    _errors.add( protocolError );
  }

  public synchronized Object getSource()
  {
    return _source;
  }

  public List<ProtocolError> getErrors()
  {
    return _errors;
  }

  @Override
  public IType getEnclosingType()
  {
    return _outer;
  }

  public void verify()
  {
    if( !isValid() )
    {
      throw new IllegalStateException( getErrorMessage() );
    }
  }

  public Object getFileName()
  {
    if( _source instanceof IFile )
    {
      return ((IFile)_source).getAbsolutePath();
    }
    return getName() + ".proto";
  }

  public boolean isAnonymous()
  {
    return _outer != null;
  }

  ProtocolType getNextInnerProtocol( ISourceCodeTokenizer tokenizer )
  {
    ProtocolType type = new ProtocolType( this, getName() + "$" + _innerProtocols.size(), tokenizer );
    _innerProtocols.add( type );
    return type;
  }

  void setTypeInfo( final ProtocolTypeInfo typeInfo )
  {
    _typeInfo = typeInfo;
  }

  @Override
  public IProtocolType getInnerProtocol( Integer i )
  {
    return _innerProtocols.get( i );
  }
}
