import sbt._
// from here: https://github.com/ritschwumm/xsbt-reflect
// mods: 
//  - add timestamp to Reflect object

/**
usage
	<code>
	seq(ReflectPlugin.allSettings:_*)
	sourceGenerators in Compile <+= reflect map identity
	</code>
code
	println("project version=" + Reflect.version)
*/
object ReflectPlugin extends Plugin {
	val reflect			= TaskKey[Seq[File]]("reflect")
	val reflectPackage	= SettingKey[String]("reflect-package")
	val reflectClass	= SettingKey[String]("reflect-class")
	
	lazy val allSettings	= Seq(
		reflectPackage	:= "",
		reflectClass	:= "Reflect",
		reflect			<<= (Keys.sourceManaged, Keys.name, Keys.version, reflectPackage, Keys.licenses, reflectClass) map {
			(sourceManaged:File, name:String, version:String, reflectPackage:String, licenses: Seq[(String, URL)], reflectClass:String)	=>
				val	file	= sourceManaged / "reflect" / "Reflect.scala"
				val code	= 
						(
							if (reflectPackage.nonEmpty)	"package " + reflectPackage + "\n"
							else							""
						) +
						"object " + reflectClass + " {\n" + 
						   "\tval name\t= \"" + name + "\"\n" +
					     "\tval version\t= \"" + version + "\"\n" +
               "\tval licenses = List("+ licenses.map(l => "(\""+ l._1+ "\", new java.net.URL(\""+ l._2.toString +"\"))").mkString(", ") +")\n" +
               "\tval timestamp = " + System.currentTimeMillis +"L\n"+
						"}\n"  
				IO write (file, code)
				Seq(file)
		}
	)
}
