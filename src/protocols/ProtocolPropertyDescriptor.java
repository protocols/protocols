package protocols;

import gw.lang.reflect.IType;

public class ProtocolPropertyDescriptor
{
  private boolean _readOnly;
  private String _name;
  private IType _type;

  public ProtocolPropertyDescriptor( boolean readOnly, String name, IType type )
  {
    _readOnly = readOnly;
    _name = name;
    _type = type;
  }

  public boolean getReadOnly()
  {
    return _readOnly;
  }

  public String getName()
  {
    return _name;
  }

  public IType getType()
  {
    return _type;
  }
}
