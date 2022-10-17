import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import de.exware.nobuto.Utilities;
import de.exware.nobuto.java.JavaBuilder;
import de.exware.nobuto.maven.Maven;

public class Build extends JavaBuilder
{
    public static final String CLASSES_DIR = "out";
    public static final String TMP = "tmp";
    public static String ECLIPSE_URL = "https://download.eclipse.org/releases/2021-12/202112081000";

    public Build()
    {
    }
    
    @Override
    public void dist() throws Exception
    {
        new File(TMP, "maven").mkdirs();
        super.dist();
        jar("maven.compatibility.jar", CLASSES_DIR, null);
    }

    public void compile() throws Exception
    {
        addEclipseJarToClasspath("org.eclipse.core.resources_3.16.0.v20211001-2032.jar");
        addEclipseJarToClasspath("https://download.eclipse.org/releases/2021-12/202112081000/plugins/org.eclipse.m2e.core_1.18.3.20211018-0804.jar");
        addEclipseJarToClasspath("https://download.eclipse.org/releases/2022-09/202209141001/plugins/org.eclipse.m2e.core_2.0.3.20220904-1703.jar");
        addMavenJarToClasspath("org.apache.ant", "ant", "1.10.9");
        addMavenJarToClasspath("org.apache.maven", "maven-core", "3.0");
        addMavenJarToClasspath("org.apache.maven", "maven-model", "3.0");
        setOutputFolder(CLASSES_DIR);
        addClasspathItem(CLASSES_DIR);
        compile("source/java/maven/compatibility/ProjectConfigurationRequestWrapperOutdated.java");
        removeClassPathItem(TMP + "/eclipse/plugins/org.eclipse.m2e.core_1.18.3.20211018-0804.jar");
        compile("source/java/maven/compatibility/ProjectConfigurationRequestWrapperModern.java");
        compile("source/java/maven/compatibility/ProjectConfigurationRequestWrapper.java");
        System.out.println();
    }
    
    protected void addMavenJarToClasspath(String groupID, String artifactId, String version) throws IOException
    {
        Maven maven = Maven.getDefaultinstance();
        File lib = maven.get(groupID, artifactId, version, TMP + "/maven");
        if(lib == null)
        {
            throw new IOException("Library " + artifactId + " not found");
        }
        addClasspathItem(lib.getPath());
    }

    protected void addEclipseJarToClasspath(String plugin) throws IOException
    {
        String purl = plugin;
        File file = new File(TMP, "eclipse/plugins/" + plugin);
        if( purl.startsWith("http") )
        {
            file = new File(TMP, "eclipse/plugins/" + file.getName());
        }
        else
        {
            purl = ECLIPSE_URL + "/plugins/" + plugin;
        }
        URL url = new URL(purl);
        if(file.exists() == false)
        {
            file.getParentFile().mkdirs();
            BufferedInputStream in = new BufferedInputStream(url.openStream());
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(file));
            Utilities.copy(in, out);
            in.close();
            out.close();
        }
        addClasspathItem(file.getPath());
    }

}
