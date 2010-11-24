package protocols;

import gw.lang.parser.resources.ResourceKey;

public class ProtocolError
{
  private int _line;
  private int _column;
  private String _msg;
  private ResourceKey _key;

  public ProtocolError( int line, int column, String msg, ResourceKey key )
  {
    _line = line;
    _column = column;
    _msg = msg;
    _key = key;
  }

  public int getLine()
  {
    return _line;
  }

  public int getColumn()
  {
    return _column;
  }

  public String getMessage()
  {
    return _msg;
  }

  public ResourceKey getMessageKey()
  {
    return _key;
  }
}
