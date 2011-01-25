uses java.lang.*

var srcDir = file("src")
var buildDir = file("build")
var classesDir = buildDir.file("classes")
var gosuHome = System.getenv().get( "GOSU_HOME" )
if( gosuHome == null ) throw "Please set GOSU_HOME environment variable!" 
var gosuDir = file( gosuHome + "/jars" )

function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(srcDir),
            :classpath = classpath( gosuDir.fileset() ),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file( "META-INF/MANIFEST.MF" ).write( "Gosu-Typeloaders: protocols.ProtocolTypeLoader\n\n" )
}

@Depends("compile")
function jar() {
  Ant.mkdir(:dir = buildDir)
  Ant.jar(:destfile = buildDir.file("protocols.jar"),
          :manifest = classesDir.file( "META-INF/MANIFEST.MF" ),
          :basedir = classesDir)
}

function clean() {
  Ant.delete(:dir = file("out"))
  Ant.delete(:dir = classesDir)
  Ant.delete(:dir = buildDir)
}
