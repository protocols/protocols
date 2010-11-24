package protocols;

import gw.lang.reflect.BaseFeatureInfo;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IPresentationInfo;
import gw.lang.reflect.IPropertyAccessor;
import gw.lang.reflect.IPropertyInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.TypeSystem;

import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author cgross
 */
public class ProtocolPropertyInfo extends BaseFeatureInfo implements IPropertyInfo
{
  private boolean _readOnly;
  private String _name;
  private IType _type;
  private IPropertyAccessor _pa;

  public ProtocolPropertyInfo( ProtocolTypeInfo protocolTypeInfo, boolean readOnly, String name, IType type )
  {
    super( protocolTypeInfo );
    _readOnly = readOnly;
    _name = name;
    _type = type;
    _pa = new IPropertyAccessor()
    {
      @Override
      public Object getValue( Object ctx )
      {
        ((ProtocolType)getOwnersType()).verify();
        IType iType = TypeSystem.getFromObject( ctx );
        IPropertyInfo property = iType.getTypeInfo().getProperty( _name );
        return property.getAccessor().getValue( ctx );
      }

      @Override
      public void setValue( Object ctx, Object value )
      {
        ((ProtocolType)getOwnersType()).verify();
        IType iType = TypeSystem.getFromObject( ctx );
        IPropertyInfo property = iType.getTypeInfo().getProperty( _name );
        property.getAccessor().setValue( ctx, value );
      }
    };
  }

  @Override
  public boolean isReadable()
  {
    return true;
  }

  @Override
  public boolean isWritable()
  {
    return !_readOnly;
  }

  @Override
  public boolean isWritable( IType whosAskin )
  {
    return !_readOnly;
  }

  @Override
  public IPropertyAccessor getAccessor()
  {
    return _pa;
  }

  @Override
  public IPresentationInfo getPresentationInfo()
  {
    return null;
  }

  @Override
  public boolean isStatic()
  {
    return false;
  }

  @Override
  public List<IAnnotationInfo> getDeclaredAnnotations()
  {
    return Collections.emptyList();
  }

  @Override
  public String getName()
  {
    return _name;
  }

  @Override
  public IType getFeatureType()
  {
    return _type;
  }
}
