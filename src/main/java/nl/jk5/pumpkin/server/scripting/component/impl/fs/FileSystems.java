package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import nl.jk5.pumpkin.server.Log;
import nl.jk5.pumpkin.server.scripting.architecture.jnlua.api.SystemApi;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.UUID;

/**
 * This class provides factory methods for creating file systems that are
 * compatible with the built-in file system driver.
 */
public final class FileSystems {

    private static final boolean IS_INSENSITIVE;

    static {
        boolean res = true;
        try {
            String uuid = UUID.randomUUID().toString();
            File lowerCase = new File(uuid + "pkrox");
            File upperCase = new File(uuid + "PKROX");
            if(lowerCase.exists()){
                lowerCase.delete();
            }
            if(upperCase.exists()){
                upperCase.exists();
            }
            lowerCase.createNewFile();
            res = upperCase.exists();
            lowerCase.delete();
        }catch(Throwable t){
            Log.warn("Couldn't determine if file system is case sensitive, falling back to insensitive.", t);
            res = true;
        }
        IS_INSENSITIVE = res;
    }

    /**
     * Creates a new file system based on the location of a class.
     * <p/>
     * This can be used to wrap a folder in the assets folder of your mod's JAR.
     * The actual path is built like this:
     * <pre>"/assets/" + domain + "/" + root</pre>
     * <p/>
     * If the class is located in a JAR file, this will create a read-only file
     * system based on that JAR file. If the class file is located in the native
     * file system, this will create a read-only file system first trying from
     * the actual location of the class file, and failing that by searching the
     * class path (i.e. it'll look for a path constructed as described above).
     * <p/>
     * If the specified path cannot be located, the creation fails and this
     * returns <tt>null</tt>.
     *
     * @param clazz  the class whose containing JAR to wrap.
     * @param domain the domain, usually your mod's ID.
     * @param root   an optional subdirectory.
     * @return a file system wrapping the specified folder.
     */
    public static FileSystem fromClass(final Class<?> clazz, final String domain, final String root) {
        String innerPath = ("/assets/" + domain + "/" + (root.trim() + "/")).replace("//", "/");

        String codeSource = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();

        String codeUrl;
        boolean isArchive;
        if(codeSource.contains(".zip!") || codeSource.contains(".jar!")){
            codeUrl = codeSource.substring(0, codeSource.lastIndexOf('!'));
            isArchive = true;
        }else{
            codeUrl = codeSource;
            isArchive = false;
        }

        URL url;
        try{
            url = new URL(codeUrl);
        }catch(MalformedURLException e){
            try{
                url = new URL("file://" + codeUrl);
            }catch(MalformedURLException e1){
                throw new RuntimeException(e1);
            }
        }

        File file;
        try{
            file = new File(url.toURI());
        }catch(URISyntaxException e){
            file = new File(url.getPath());
        }
        if(file == null){
            file = new File(codeSource);
        }

        if(isArchive){
            return ZipFileInputStreamFileSystem.fromFile(file, innerPath.substring(1));
        }else{
            if(!file.exists() || file.isDirectory()){
                return null;
            }
            File inner = new File(new File(file.getParent()), innerPath);
            if(inner.exists() && inner.isDirectory()){
                return new ReadOnlyFileSystem(inner);
            }else{
                for(String s : System.getProperty("java.class.path").split(System.getProperty("path.separator"))){
                    if(s.endsWith("build/classes/main")){
                        s = s.replace("build/classes/main", "build/resources/main");
                    }
                    File fsp = new File(new File(s), innerPath);
                    if(fsp.exists() && fsp.isDirectory()){
                        return new ReadOnlyFileSystem(fsp);
                    }
                }
                return null;
            }
        }
    }


    public static FileSystem fromDirectory(File file) {
        file.mkdirs();
        if(file.exists() && file.isDirectory()){
            return new ReadWriteFileSystem(file);
        }
        return null;
    }

    /**
     * Creates a new <em>writable</em> file system that resides in memory.
     * <p/>
     * Any contents created and written on this file system will be lost when
     * the node is removed from the network.
     * <p/>
     * This is used for computers' <tt>/tmp</tt> mount, for example.
     *
     * @param capacity the capacity of the file system.
     * @return a file system residing in memory.
     */
    public static FileSystem fromMemory(final long capacity) {
        return new RamFileSystem(); // TODO: 29-2-16 Capacity
    }

    /**
     * Wrap a file system retrieved via one of the <tt>from???</tt> methods to
     * make it read-only.
     *
     * @param fileSystem the file system to wrap.
     * @return the specified file system wrapped to be read-only.
     */
    public static FileSystem asReadOnly(final FileSystem fileSystem) {
        if(fileSystem.isReadOnly()){
            return fileSystem;
        }else{
            return new ReadOnlyWrapper(fileSystem);
        }
    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * This can be useful for providing some data if you don't wish to implement
     * your own driver. Which will probably be most of the time. If you need
     * more control over the node, implement your own, and connect this one to
     * it. In that case you will have to forward any disk driver messages to the
     * node, though.
     * <p/>
     * The container parameter is used to give the file system some physical
     * relation to the world, for example this is used by hard drives to send
     * the disk event notifications to the client that are used to play disk
     * access sounds.
     * <p/>
     * The container may be <tt>null</tt>, if no such context can be provided.
     * <p/>
     * The access sound is the name of the sound effect to play when the file
     * system is accessed, for example by listing a directory or reading from
     * a file. It may be <tt>null</tt> to create a silent file system.
     * <p/>
     * The speed multiplier controls how fast read and write operations on the
     * file system are. It must be a value in [1,6], and controls the access
     * speed, with the default being one.
     * For reference, floppies are using the default, hard drives scale with
     * their tiers, i.e. a tier one hard drive uses speed two, tier three uses
     * speed four.
     *
     * @param fileSystem  the file system to wrap.
     * @param label       the label of the file system.
     * @param host        the tile entity containing the file system.
     * @param accessSound the name of the sound effect to play when the file
     *                    system is accessed. This has to be the fully
     *                    qualified resource name, e.g.
     *                    <tt>opencomputers:floppy_access</tt>.
     * @param speed       the speed multiplier for this file system.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem, final Label label, final EnvironmentHost host, final String accessSound, int speed) {
//        if (API.fileSystem != null)
//            return API.fileSystem.asManagedEnvironment(fileSystem, label, host, accessSound, speed);
//        return null;
//    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * Creates a file system with the a read-only label and the specified
     * access sound and file system speed.
     *
     * @param fileSystem  the file system to wrap.
     * @param label       the label of the file system.
     * @param host        the tile entity containing the file system.
     * @param accessSound the name of the sound effect to play when the file
     *                    system is accessed. This has to be the fully
     *                    qualified resource name, e.g.
     *                    <tt>opencomputers:floppy_access</tt>.
     * @param speed       the speed multiplier for this file system.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem, final String label, final EnvironmentHost host, final String accessSound, int speed) {
//        if (API.fileSystem != null)
//            return API.fileSystem.asManagedEnvironment(fileSystem, label, host, accessSound, speed);
//        return null;
//    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * Creates a file system with the specified label and the specified access
     * sound, using the default file system speed.
     *
     * @param fileSystem  the file system to wrap.
     * @param label       the label of the file system.
     * @param host        the tile entity containing the file system.
     * @param accessSound the name of the sound effect to play when the file
     *                    system is accessed. This has to be the fully
     *                    qualified resource name, e.g.
     *                    <tt>opencomputers:floppy_access</tt>.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem, final Label label, final EnvironmentHost host, final String accessSound) {
//        return asManagedEnvironment(fileSystem, label, host, accessSound, 1);
//    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * Creates a file system with a read-only label and the specified access
     * sound, using the default file system speed.
     *
     * @param fileSystem  the file system to wrap.
     * @param label       the read-only label of the file system.
     * @param host        the tile entity containing the file system.
     * @param accessSound the name of the sound effect to play when the file
     *                    system is accessed. This has to be the fully
     *                    qualified resource name, e.g.
     *                    <tt>opencomputers:floppy_access</tt>.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem, final String label, final EnvironmentHost host, final String accessSound) {
//        return asManagedEnvironment(fileSystem, label, host, accessSound, 1);
//    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * Creates a file system with the specified label, without an environment
     * and access sound, using the default file system speed.
     *
     * @param fileSystem the file system to wrap.
     * @param label      the label of the file system.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem, final Label label) {
//        return asManagedEnvironment(fileSystem, label, null, null, 1);
//    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * Creates a file system with a read-only label, without an environment and
     * access sound, using the default file system speed.
     *
     * @param fileSystem the file system to wrap.
     * @param label      the read-only label of the file system.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem, final String label) {
//        return asManagedEnvironment(fileSystem, label, null, null, 1);
//    }

    /**
     * Creates a network node that makes the specified file system available via
     * the common file system driver.
     * <p/>
     * Creates an unlabeled file system (i.e. the label can neither be read nor
     * written), without an environment and access sound, using the default
     * file system speed.
     *
     * @param fileSystem the file system to wrap.
     * @return the network node wrapping the file system.
     */
//    public static ManagedEnvironment asManagedEnvironment(final li.cil.oc.api.fs.FileSystem fileSystem) {
//        return asManagedEnvironment(fileSystem, (Label) null, null, null, 1);
//    }

    // ----------------------------------------------------------------------- //

    private FileSystems() {
    }
}
