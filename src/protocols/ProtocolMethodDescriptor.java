package protocols;

import gw.lang.reflect.IType;
import gw.util.Pair;

import java.util.List;

public class ProtocolMethodDescriptor
{
  private String _name;
  private List<Pair<String, IType>> _args;
  private IType _returnType;

  public ProtocolMethodDescriptor( String name, List<Pair<String, IType>> args, IType returnType )
  {
    _name = name;
    _args = args;
    _returnType = returnType;
  }

  public String getName()
  {
    return _name;
  }

  public List<Pair<String, IType>> getArgs()
  {
    return _args;
  }

  public IType getReturnType()
  {
    return _returnType;
  }

  @Override
  public boolean equals( Object o )
  {
    if( o instanceof ProtocolMethodDescriptor )
    {
      ProtocolMethodDescriptor that = (ProtocolMethodDescriptor)o;
      if( this.getName().equals( that.getName() ) && this._args.size() == that._args.size() )
      {
        for( int i = 0; i < _args.size(); i++ )
        {
          IType type1 = this._args.get( i ).getSecond();
          IType type2 = that._args.get( i ).getSecond();
          if( !type1.equals( type2 ) )
          {
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public int hashCode()
  {
    int result = _name != null ? _name.hashCode() : 0;
    result = 31 * result + (_args != null ? _args.size() : 0);
    return result;
  }

  @Override
  public String toString()
  {
    return "ProtocolMethodDescriptor{" +
           "_name='" + _name + '\'' +
           ", _args=" + _args +
           ", _returnType=" + _returnType +
           '}';
  }
}
