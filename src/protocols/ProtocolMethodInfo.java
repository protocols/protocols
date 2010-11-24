package protocols;

import gw.lang.reflect.BaseFeatureInfo;
import gw.lang.reflect.IAnnotationInfo;
import gw.lang.reflect.IExceptionInfo;
import gw.lang.reflect.IMethodCallHandler;
import gw.lang.reflect.IMethodInfo;
import gw.lang.reflect.IParameterInfo;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeInfo;
import gw.lang.reflect.TypeSystem;
import gw.util.Pair;

import java.util.Collections;
import java.util.List;

/**
 * Class description...
 *
 * @author cgross
 */
public class ProtocolMethodInfo extends BaseFeatureInfo implements IMethodInfo
{
  private IParameterInfo[] _paramInfo;
  private IType[] _paramTypes;
  private IType _returnType;
  private String _name;
  private ProtocolMethodInfo.ProtocolMethodHandler _mh;

  public ProtocolMethodInfo( ProtocolTypeInfo ti, String name, List<Pair<String, IType>> args, IType returnType )
  {
    super( ti );
    _returnType = returnType;
    _name = name;
    _paramInfo = new IParameterInfo[args.size()];
    _paramTypes = new IType[args.size()];
    for( int i = 0, argsSize = args.size(); i < argsSize; i++ )
    {
      Pair<String, IType> arg = args.get( i );
      _paramInfo[i] = new ProtocolMethodParamInfo( this, arg.getFirst(), arg.getSecond() );
      _paramTypes[i] = arg.getSecond();
    }
    _mh = new ProtocolMethodHandler();
  }

  @Override
  public IParameterInfo[] getParameters()
  {
    return _paramInfo;
  }

  @Override
  public IType getReturnType()
  {
    return _returnType;
  }

  @Override
  public IMethodCallHandler getCallHandler()
  {
    return _mh;
  }

  @Override
  public String getReturnDescription()
  {
    return "";
  }

  @Override
  public List<IExceptionInfo> getExceptions()
  {
    return Collections.emptyList();
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

  public IType[] getParamTypes()
  {
    return _paramTypes;
  }

  private static class ProtocolMethodParamInfo extends BaseFeatureInfo implements IParameterInfo
  {
    String _name;
    IType _type;
    public ProtocolMethodParamInfo( ProtocolMethodInfo protocolMethodInfo, String name, IType type )
    {
      super( protocolMethodInfo );
      _name = name;
      _type = type;
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

  private class ProtocolMethodHandler implements IMethodCallHandler
  {
    @Override
    public Object handleCall( Object ctx, Object... args )
    {
      ((ProtocolType)getOwnersType()).verify();
      IType iType = TypeSystem.getFromObject( ctx );
      IMethodInfo delegateMethod = ITypeInfo.FIND.callableMethod( iType.getTypeInfo().getMethods(), _name, _paramTypes );
      return delegateMethod.getCallHandler().handleCall( ctx, args );
    }
  }
}
