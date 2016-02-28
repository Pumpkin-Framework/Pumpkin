package nl.jk5.pumpkin.server.scripting.filesystem;

import com.google.common.collect.Lists;
import nl.jk5.pumpkin.server.Log;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

public final class MountUtils {

    private MountUtils(){

    }

    public static IMount createResourceMount(String domain, String subPath) {
        try{
            File jar = getPumpkinJar();
            if(jar != null){
                List<IMount> mounts = Lists.newArrayList();
                subPath = "assets/" + domain + "/" + subPath;

                IMount mount;
                if(jar.getName().endsWith(".class")){
                    //Somehow we are not in a jar. Development environment?
                    mounts.add(mount = new ReadOnlyDirMount(new File(jar.getParentFile().getParentFile().getParentFile().getParentFile().getParentFile().getParentFile(), subPath)));
                }else{
                    mounts.add(mount = new ZipMount(jar, subPath));
                }

                if(mounts.size() > 1){
                    IMount[] mountArray = new IMount[mounts.size()];
                    mounts.toArray(mountArray);
                    return new ComboMount(mountArray);
                }
                return mount;
            }

            return null;
        }catch(IOException e){
            Log.error("Error while mounting assets/{}/{}", domain, subPath);
            Log.error("Error: ", e);
        }
        return null;
    }

    private static File getPumpkinJar() {
        String path = MountUtils.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        int bangIndex = path.indexOf("!");
        if(bangIndex >= 0){
            path = path.substring(0, bangIndex);
        }
        URL url;
        try{
            url = new URL(path);
        }catch(MalformedURLException e1){
            return new File(path);
        }
        File file;
        try{
            file = new File(url.toURI());
        }catch(URISyntaxException e){
            file = new File(url.getPath());
        }
        return file;
    }

    private static File getBaseDir() {
        try{
            return new File(".").getCanonicalFile();
        }catch(IOException e){
            return new File(".");
        }
    }

    private static File getResourcePackDir() {
        return new File(getBaseDir(), "resourcepacks");
    }
}
