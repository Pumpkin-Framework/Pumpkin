package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import com.google.common.base.Joiner;
import nl.jk5.pumpkin.server.scripting.Arguments;
import nl.jk5.pumpkin.server.scripting.Callback;
import nl.jk5.pumpkin.server.scripting.Context;
import nl.jk5.pumpkin.server.scripting.component.AnnotatedComponent;

import java.io.FileNotFoundException;
import java.io.IOException;

@AnnotatedComponent.Type("filesystem")
public class FileSystemComponent extends AnnotatedComponent {

    private final String address;
    private final FileSystem fileSystem;

    public FileSystemComponent(String address, FileSystem fileSystem) {
        this.address = address;
        this.fileSystem = fileSystem;
    }

    @Override
    public String address() {
        return address;
    }

    @Callback(direct = true, doc = "function():boolean -- Returns whether the file system is read-only.")
    public Object[] isReadOnly(Context context, Arguments args){
        synchronized (this.fileSystem) {
            return new Object[]{fileSystem.isReadOnly()};
        }
    }

    @Callback(direct = true, doc = "function():number -- The overall capacity of the file system, in bytes.")
    public Object[] spaceTotal(Context context, Arguments args){
        synchronized (this.fileSystem) {
            long space = fileSystem.spaceTotal();
            if(space < 0){
                return new Object[]{Double.POSITIVE_INFINITY};
            }else{
                return new Object[]{space};
            }
        }
    }

    @Callback(direct = true, doc = "function():number -- The currently used capacity of the file system, in bytes.")
    public Object[] spaceUsed(Context context, Arguments args){
        synchronized (this.fileSystem) {
            return new Object[]{fileSystem.spaceUsed()};
        }
    }

    @Callback(direct = true, doc = "function(path:string):boolean -- Returns whether an object exists at the specified absolute path in the file system.")
    public Object[] exists(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            return new Object[]{fileSystem.exists(clean(args.checkString(0)))};
        }
    }

    @Callback(direct = true, doc = "function(path:string):number -- Returns the size of the object at the specified absolute path in the file system.")
    public Object[] size(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            return new Object[]{fileSystem.size(clean(args.checkString(0)))};
        }
    }

    @Callback(direct = true, doc = "function(path:string):boolean -- Returns whether the object at the specified absolute path in the file system is a directory.")
    public Object[] isDirectory(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            return new Object[]{fileSystem.isDirectory(clean(args.checkString(0)))};
        }
    }

    @Callback(direct = true, doc = "function(path:string):number -- Returns the (real world) timestamp of when the object at the specified absolute path in the file system was modified.")
    public Object[] lastModified(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            return new Object[]{fileSystem.lastModified(clean(args.checkString(0)))};
        }
    }

    @Callback(doc = "function(path:string):table -- Returns a list of names of objects in the directory at the specified absolute path in the file system.")
    public Object[] list(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            String[] files = this.fileSystem.list(clean(args.checkString(0)));
            if(files == null || files.length == 0){
                return null;
            }
            return new Object[]{files};
        }
    }

    @Callback(doc = "function(path:string):boolean -- Creates a directory at the specified absolute path in the file system. Creates parent directories, if necessary.")
    public Object[] makeDirectory(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            boolean success = recurseMkdir(clean(args.checkString(0)));
            return new Object[]{success};
        }
    }

    private boolean recurseMkdir(String path){
        return !fileSystem.exists(path) && (fileSystem.makeDirectory(path) || (recurseMkdir(dropRight(path)) && fileSystem.makeDirectory(path)));
    }

    @Callback(doc = "function(path:string):boolean -- Removes the object at the specified absolute path in the file system.")
    public Object[] remove(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            boolean success = recurseRemove(clean(args.checkString(0)));
            return new Object[]{success};
        }
    }

    private boolean recurseRemove(String path) throws FileNotFoundException {
        if(!fileSystem.isDirectory(path)){
            return false;
        }
        boolean ret = true;
        for(String s : fileSystem.list(path)){
            if(!recurseRemove(path + "/" + s)){
                ret = false;
            }
        }
        if(ret){
            fileSystem.delete(path);
        }
        return ret;
    }

    @Callback(doc = "function(from:string, to:string):boolean -- Renames/moves an object from the first specified absolute path in the file system to the second.")
    public Object[] rename(Context context, Arguments args) throws FileNotFoundException {
        synchronized (this.fileSystem) {
            boolean success = fileSystem.rename(clean(args.checkString(0)), clean(args.checkString(1)));
            return new Object[]{success};
        }
    }

    @Callback(direct = true, doc = "function(handle:userdata) -- Closes an open file descriptor with the specified handle.")
    public Object[] close(Context context, Arguments args) throws IOException {
        synchronized (this.fileSystem) {
            close(context, checkHandle(args, 0));
            return null;
        }
    }

    @Callback(direct = true, limit = 4, doc = "function(path:string[, mode:string='r']):userdata -- Opens a new file descriptor and returns its handle.")
    public Object[] open(Context context, Arguments args) throws IOException {
        synchronized (this.fileSystem) {
            //TODO: max handles protection?
            String path = args.checkString(0);
            String mode = args.optString(1, "r");
            int handle = this.fileSystem.open(clean(path), parseMode(mode));
            return new Object[]{new HandleValue(address(), handle)};
        }
    }

    @Callback(direct = true, limit = 15, doc = "function(handle:userdata, count:number):string or nil -- Reads up to the specified amount of data from an open file descriptor with the specified handle. Returns nil when EOF is reached.")
    public Object[] read(Context context, Arguments args) throws IOException {
        synchronized (this.fileSystem) {
            int handle = checkHandle(args, 0);
            int n = Math.min(2048, Math.max(0, args.checkInteger(1)));  // TODO: 29-2-16 Make read buffer configurable
            Handle file = this.fileSystem.getHandle(handle); //TODO make sure handles of other machines are not touched
            if(file == null){
                throw new IOException("bad file descriptor");
            }
            byte[] buffer = new byte[n];
            int read = file.read(buffer);
            if(read >= 0){
                byte[] bytes;
                if(read == buffer.length){
                    bytes = buffer;
                }else{
                    bytes = new byte[read];
                    System.arraycopy(buffer, 0, bytes, 0, read);
                }
                return new Object[]{bytes};
            }else{
                return new Object[]{};
            }
        }
    }

    @Callback(direct = true, doc = "function(handle:userdata, whence:string, offset:number):number -- Seeks in an open file descriptor with the specified handle. Returns the new pointer position.")
    public Object[] seek(Context context, Arguments args) throws IOException {
        synchronized (this.fileSystem) {
            int handle = checkHandle(args, 0);
            String whence = args.checkString(1);
            int offset = args.checkInteger(2);
            Handle file = this.fileSystem.getHandle(handle);
            if(file == null){
                throw new IOException("bad file descriptor");
            }
            if(whence.equals("cur")){
                file.seek(file.position() + offset);
            }else if(whence.equals("set")){
                file.seek(offset);
            }else if(whence.equals("end")){
                file.seek(file.length() + offset);
            }else{
                throw new IllegalArgumentException("invalid mode");
            }
            return new Object[]{file.position()};
        }
    }

    @Callback(direct = true, doc = "function(handle:userdata, value:string):boolean -- Writes the specified data to an open file descriptor with the specified handle.")
    public Object[] write(Context context, Arguments args) throws IOException {
        synchronized (this.fileSystem) {
            int handle = checkHandle(args, 0);
            byte[] value = args.checkByteArray(1);
            Handle file = this.fileSystem.getHandle(handle);
            if(file == null){
                throw new IOException("bad file descriptor");
            }
            file.write(value);
            return new Object[]{true};
        }
    }

    private int checkHandle(Arguments args, int index) throws IOException {
        if(args.isInteger(index)){
            return args.checkInteger(index);
        }else{
            Object handle = args.checkAny(0);
            if(handle instanceof HandleValue){
                return ((HandleValue) handle).getHandle();
            }
            throw new IOException("bad file descriptor");
        }
    }

    public void close(Context context, int fd) throws IOException {
        Handle handle = this.fileSystem.getHandle(fd);
        if(handle == null){
            throw new IOException("bad file descriptor");
        }
        handle.close();
    }

    private static String dropRight(String input){
        String[] pts = input.split("/");
        String[] out = new String[pts.length - 1];
        System.arraycopy(pts, 0, out, 0, pts.length - 1);
        return Joiner.on('/').join(out);
    }

    private static String clean(String path) throws FileNotFoundException {
        String res = com.google.common.io.Files.simplifyPath(path);
        if(res.startsWith("../")) throw new FileNotFoundException();
        if(res.equals("/") || res.equals(".")){
            return "";
        }else{
            return res;
        }
    }

    private Mode parseMode(String value){
        if(value.equals("r") || value.equals("rb")){
            return Mode.Read;
        }else if(value.equals("w") || value.equals("wb")){
            return Mode.Write;
        }if(value.equals("a") || value.equals("ab")){
            return Mode.Append;
        }
        throw new IllegalArgumentException("unsupported mode");
    }
}
