var srcDir = file("src")
var classesDir = file("classes")
var distDir = file("dist")
var libDir = file("lib")

function compile() {
  Ant.mkdir(:dir = classesDir)
  Ant.javac(:srcdir = path(srcDir),
            :classpath = classpath( libDir.fileset() ),
            :destdir = classesDir,
            :includeantruntime = false)
  classesDir.file("META-INF").mkdir()
  classesDir.file( "META-INF/MANIFEST.MF" ).write( "Gosu-Typeloaders: protocols.ProtocolTypeLoader" )
}

@Depends("compile")
function jar() {
  Ant.mkdir(:dir = distDir)
  Ant.jar(:destfile = distDir.file("protocols.jar"),
          :basedir = classesDir)
}

function clean() {
  Ant.delete(:dir = classesDir)
  Ant.delete(:dir = distDir)
}