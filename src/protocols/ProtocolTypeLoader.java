package protocols;

import gw.fs.IFile;
import gw.lang.reflect.IType;
import gw.lang.reflect.ITypeLoader;
import gw.lang.reflect.TypeLoaderBase;
import gw.lang.reflect.TypeSystem;
import gw.lang.reflect.java.IJavaClassInfo;
import gw.lang.reflect.module.IModule;
import gw.util.Pair;
import gw.util.concurrent.LazyVar;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProtocolTypeLoader extends TypeLoaderBase implements ITypeLoader
{
  private IModule _module;
  private LazyVar<Set<? extends CharSequence>> _allTypeNames;

  public ProtocolTypeLoader( IModule module )
  {
    _module = module;
    _allTypeNames = new LazyVar<Set<? extends CharSequence>>()
    {
      @Override
      protected Set<? extends CharSequence> init()
      {
        HashSet<CharSequence> set = new HashSet<CharSequence>();
        List<Pair<String,IFile>> extension = _module.getResourceAccess().findAllFilesByExtension("proto");
        for( Pair<String, IFile> strs : extension )
        {
          String path = strs.getFirst();
          String stripped = path.substring( 0, path.length() - ".proto".length() );
          set.add( stripped.replace( '/', '.' ) );
        }
        return set;
      }
    };
  }
  
  @Override
  public IModule getModule()
  {
    return _module;
  }

  @Override
  public IType getIntrinsicType( Class javaClass )
  {
    return null;
  }

  @Override
  public IType getIntrinsicType( IJavaClassInfo javaClassInfo )
  {
    return null;
  }

  @Override
  public IType getType( String fullyQualifiedName )
  {
    Pair<String, List<Integer>> splitName = splitName( fullyQualifiedName );
    if( splitName != null )
    {
      if( splitName.getSecond().isEmpty() )
      {
        String outermostName = splitName.getFirst();
        String name = "/" + outermostName.replace( ".", "/" ) + ".proto";
        IFile file = _module.getResourceAccess().findFirstFile( name );
        if( file != null )
        {
          return new ProtocolType( this, fullyQualifiedName, file );
        }
      }
      else
      {
        IType outer = TypeSystem.getByFullName( splitName.getFirst() );
        if( outer instanceof IProtocolType )
        {
          for( Integer integer : splitName.getSecond() )
          {
            if( outer == null )
            {
              return null;
            }
            outer = ((IProtocolType) outer).getInnerProtocol( integer );
          }
          return outer;
        }
      }
    }
    return null;
  }

  private Pair<String, List<Integer>> splitName( String fullyQualifiedName )
  {
    String[] strings = fullyQualifiedName.split( "\\$" );
    String name = strings[0];
    List<Integer> innerPositions = Collections.emptyList();
    for( int i = 1; i < strings.length; i++ )
    {
      if( innerPositions == Collections.EMPTY_LIST )
      {
        innerPositions = new ArrayList<Integer>();
      }
      try
      {
        innerPositions.add( Integer.parseInt( strings[i] ) );
      }
      catch( NumberFormatException e )
      {
        return null;
      }
    }
    return Pair.make( name, innerPositions );
  }

  @Override
  public Set<? extends CharSequence> getAllTypeNames()
  {
    return _allTypeNames.get();
  }

  @Override
  public URL getResource( String name )
  {
    return null;
  }

  @Override
  public File getResourceFile( String name )
  {
    return null;
  }

  @Override
  public void refresh( boolean clearCachedTypes )
  {
  }

  @Override
  public boolean isCaseSensitive()
  {
    return true;
  }

  @Override
  public List<String> getHandledPrefixes()
  {
    return Collections.emptyList();
  }

  @Override
  public boolean isNamespaceOfTypeHandled( String fullyQualifiedTypeName )
  {
    return true;
  }

  @Override
  public List<Throwable> getInitializationErrors()
  {
    return null;
  }
}
