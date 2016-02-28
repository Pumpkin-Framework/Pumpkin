package nl.jk5.pumpkin.server.scripting.filesystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class FileSystem {

    private Map<String, MountWrapper> mounts = Maps.newHashMap();
    private Set<IMountedFile> openFiles = Sets.newHashSet();

    @SuppressWarnings("unused")
    public FileSystem(String rootLabel, IMount rootMount) throws FileSystemException {
        mount(rootLabel, "", rootMount);
    }

    public FileSystem(String rootLabel, IWritableMount rootMount) throws FileSystemException {
        mountWritable(rootLabel, "", rootMount);
    }

    public void unload() {
        //noinspection SynchronizeOnNonFinalField
        synchronized(this.openFiles){
            while(this.openFiles.size() > 0){
                IMountedFile file = this.openFiles.iterator().next();
                try{
                    file.close();
                }catch(IOException e){
                    this.openFiles.remove(file);
                }
            }
        }
    }

    public synchronized void mount(String label, String location, IMount mount) throws FileSystemException {
        if(mount == null){
            throw new NullPointerException();
        }
        location = sanitizePath(location);
        if(location.contains("..")){
            throw new FileSystemException("Cannot mount below the root");
        }
        mount(new MountWrapper(label, location, mount));
    }

    public synchronized void mountWritable(String label, String location, IWritableMount mount) throws FileSystemException {
        if(mount == null){
            throw new NullPointerException();
        }
        location = sanitizePath(location);
        if(location.contains("..")){
            throw new FileSystemException("Cannot mount below the root");
        }
        mount(new MountWrapper(label, location, mount));
    }

    private synchronized void mount(MountWrapper wrapper) throws FileSystemException {
        String location = wrapper.getLocation();
        if(this.mounts.containsKey(location)){
            this.mounts.remove(location);
        }
        this.mounts.put(location, wrapper);
    }

    @SuppressWarnings("unused")
    public synchronized void unmount(String path) {
        path = sanitizePath(path);
        if(this.mounts.containsKey(path)){
            this.mounts.remove(path);
        }
    }

    public synchronized String combine(String path, String childPath) {
        path = sanitizePath(path);
        childPath = sanitizePath(childPath);

        if(path.length() == 0){
            return childPath;
        }
        if(childPath.length() == 0){
            return path;
        }
        return sanitizePath(path + '/' + childPath);
    }

    public static String getDirectory(String path) {
        path = sanitizePath(path);
        if(path.length() == 0){
            return "..";
        }

        int lastSlash = path.lastIndexOf('/');
        if(lastSlash >= 0){
            return path.substring(0, lastSlash);
        }
        return "";
    }

    public static String getName(String path) {
        path = sanitizePath(path);
        if(path.length() == 0){
            return "root";
        }

        int lastSlash = path.lastIndexOf('/');
        if(lastSlash >= 0){
            return stripExtension(path.substring(lastSlash + 1));
        }
        return stripExtension(path);
    }

    public synchronized long getSize(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.getSize(path);
    }

    public synchronized String[] list(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);

        List<String> list = Lists.newArrayList();
        mount.list(path, list);

        for(MountWrapper otherMount : this.mounts.values()){
            if(getDirectory(otherMount.getLocation()).equals(path)){
                list.add(getName(otherMount.getLocation()));
            }
        }

        String[] array = new String[list.size()];
        list.toArray(array);
        return array;
    }

    private void findIn(String dir, List<String> matches, Pattern wildPattern) throws FileSystemException {
        String[] list = list(dir);
        //noinspection ForLoopReplaceableByForEach
        for(int i = 0; i < list.length; i++){
            String entry = list[i];
            String entryPath = dir + "/" + entry;
            if(wildPattern.matcher(entryPath).matches()){
                matches.add(entryPath);
            }
            if(isDir(entryPath)){
                findIn(entryPath, matches, wildPattern);
            }
        }
    }

    public synchronized String[] find(String wildPath) throws FileSystemException {
        wildPath = sanitizePath(wildPath, true);
        Pattern wildPattern = Pattern.compile("^\\Q" + wildPath.replaceAll("\\*", "\\\\E[^\\\\/]*\\\\Q") + "\\E$");
        List<String> matches = Lists.newArrayList();
        findIn("", matches, wildPattern);
        return matches.toArray(new String[matches.size()]);
    }

    public synchronized boolean exists(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.exists(path);
    }

    public synchronized boolean isDir(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.isDirectory(path);
    }

    public synchronized boolean isReadOnly(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.isReadOnly(path);
    }

    public synchronized String getMountLabel(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.getLabel();
    }

    public synchronized void makeDir(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        mount.makeDirectory(path);
    }

    public synchronized void delete(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        mount.delete(path);
    }

    public synchronized void move(String sourcePath, String destPath) throws FileSystemException {
        sourcePath = sanitizePath(sourcePath);
        destPath = sanitizePath(destPath);
        if((isReadOnly(sourcePath)) || (isReadOnly(destPath))){
            throw new FileSystemException("Access denied");
        }
        if(!exists(sourcePath)){
            throw new FileSystemException("No such file");
        }
        if(exists(destPath)){
            throw new FileSystemException("File exists");
        }
        if(contains(sourcePath, destPath)){
            throw new FileSystemException("Can't move a directory inside itself");
        }
        copy(sourcePath, destPath);
        delete(sourcePath);
    }

    public synchronized void copy(String sourcePath, String destPath) throws FileSystemException {
        sourcePath = sanitizePath(sourcePath);
        destPath = sanitizePath(destPath);
        if(isReadOnly(destPath)){
            throw new FileSystemException("Access denied");
        }
        if(!exists(sourcePath)){
            throw new FileSystemException("No such file");
        }
        if(exists(destPath)){
            throw new FileSystemException("File exists");
        }
        if(contains(sourcePath, destPath)){
            throw new FileSystemException("Can't copy a directory inside itself");
        }
        copyRecursive(sourcePath, getMount(sourcePath), destPath, getMount(destPath));
    }

    private synchronized void copyRecursive(String sourcePath, MountWrapper sourceMount, String destinationPath, MountWrapper destinationMount) throws FileSystemException {
        if(!sourceMount.exists(sourcePath)){
            return;
        }

        if(sourceMount.isDirectory(sourcePath)){
            destinationMount.makeDirectory(destinationPath);

            List<String> sourceChildren = Lists.newArrayList();
            sourceMount.list(sourcePath, sourceChildren);
            for(String child : sourceChildren){
                copyRecursive(combine(sourcePath, child), sourceMount, combine(destinationPath, child), destinationMount);
            }
        }else{
            InputStream source = null;
            OutputStream destination = null;
            try{
                source = sourceMount.openForRead(sourcePath);
                destination = destinationMount.openForWrite(destinationPath);

                byte[] buffer = new byte[1024];
                while(true){
                    int bytesRead = source.read(buffer);
                    if(bytesRead < 0){
                        break;
                    }
                    destination.write(buffer, 0, bytesRead);
                }
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }finally{
                if(destination != null){
                    try {
                        destination.close();
                    }catch(IOException e){}
                }
                if(source != null){
                    try {
                        source.close();
                    }catch(IOException e){}
                }
            }
        }
    }

    public synchronized IMountedFileNormal openForRead(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        InputStream stream = mount.openForRead(path);
        if(stream != null){
            final BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            IMountedFileNormal file = new IMountedFileNormal() {
                public String readLine() throws IOException {
                    return reader.readLine();
                }

                public void write(String s, int off, int len, boolean newLine) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void close() throws IOException {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized(FileSystem.this.openFiles){
                        FileSystem.this.openFiles.remove(this);
                        reader.close();
                    }
                }

                public void flush() throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
            //noinspection SynchronizeOnNonFinalField
            synchronized(this.openFiles){
                this.openFiles.add(file);
            }
            return file;
        }
        return null;
    }

    public synchronized IMountedFileNormal openForWrite(String path, boolean append) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        OutputStream stream = append ? mount.openForAppend(path) : mount.openForWrite(path);
        if(stream != null){
            final BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
            IMountedFileNormal file = new IMountedFileNormal() {
                public String readLine() throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void write(String s, int off, int len, boolean newLine) throws IOException {
                    writer.write(s, off, len);
                    if(newLine){
                        writer.newLine();
                    }
                }

                public void close() throws IOException {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized(FileSystem.this.openFiles){
                        FileSystem.this.openFiles.remove(this);
                        writer.close();
                    }
                }

                public void flush() throws IOException {
                    writer.flush();
                }
            };
            //noinspection SynchronizeOnNonFinalField
            synchronized(this.openFiles){
                this.openFiles.add(file);
            }
            return file;
        }
        return null;
    }

    public synchronized IMountedFileBinary openForBinaryRead(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        final InputStream stream = mount.openForRead(path);
        if(stream != null){
            IMountedFileBinary file = new IMountedFileBinary() {
                public int read() throws IOException {
                    return stream.read();
                }

                public void write(int i) throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void close() throws IOException {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized(FileSystem.this.openFiles){
                        FileSystem.this.openFiles.remove(this);
                        stream.close();
                    }
                }

                public void flush()
                        throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
            //noinspection SynchronizeOnNonFinalField
            synchronized(this.openFiles){
                this.openFiles.add(file);
            }
            return file;
        }
        return null;
    }

    public synchronized IMountedFileBinary openForBinaryWrite(String path, boolean append) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        final OutputStream stream = append ? mount.openForAppend(path) : mount.openForWrite(path);
        if(stream != null){
            IMountedFileBinary file = new IMountedFileBinary() {
                public int read() throws IOException {
                    throw new UnsupportedOperationException();
                }

                public void write(int i) throws IOException {
                    stream.write(i);
                }

                public void close() throws IOException {
                    //noinspection SynchronizeOnNonFinalField
                    synchronized(FileSystem.this.openFiles){
                        FileSystem.this.openFiles.remove(this);
                        stream.close();
                    }
                }

                public void flush() throws IOException {
                    stream.flush();
                }
            };
            //noinspection SynchronizeOnNonFinalField
            synchronized(this.openFiles){
                this.openFiles.add(file);
            }
            return file;
        }
        return null;
    }

    public long getFreeSpace(String path) throws FileSystemException {
        path = sanitizePath(path);
        MountWrapper mount = getMount(path);
        return mount.getFreeSpace();
    }

    private MountWrapper getMount(String path)
            throws FileSystemException {
        Iterator it = this.mounts.values().iterator();
        MountWrapper match = null;
        int matchLength = 999;
        while(it.hasNext()){
            MountWrapper mount = (MountWrapper) it.next();
            if(contains(mount.getLocation(), path)){
                int len = toLocal(path, mount.getLocation()).length();
                if((match == null) || (len < matchLength)){
                    match = mount;
                    matchLength = len;
                }
            }
        }
        if(match == null){
            throw new FileSystemException("Invalid Path");
        }
        return match;
    }

    private static String sanitizePath(String path) {
        return sanitizePath(path, false);
    }

    private static String sanitizePath(String path, boolean allowWildcards) {
        path = path.replace('\\', '/');

        char[] specialChars = {'"', ':', '<', '>', '?', '|'};

        StringBuilder cleanName = new StringBuilder();
        for(int i = 0; i < path.length(); i++){
            char c = path.charAt(i);
            if((c >= ' ') && (Arrays.binarySearch(specialChars, c) < 0) && (allowWildcards || c != '*')){
                cleanName.append(c);
            }
        }
        path = cleanName.toString();

        String[] parts = path.split("/");
        Stack<String> outputParts = new Stack<String>();
        //noinspection ForLoopReplaceableByForEach
        for(int n = 0; n < parts.length; n++){
            String part = parts[n];
            if((part.length() != 0) && (!".".equals(part))){
                if("..".equals(part)){
                    if(!outputParts.empty()){
                        String top = outputParts.peek();
                        if(!"..".equals(top)){
                            outputParts.pop();
                        }else{
                            outputParts.push("..");
                        }
                    }else{
                        outputParts.push("..");
                    }
                }else if(part.length() >= 255){
                    outputParts.push(part.substring(0, 255));
                }else{
                    outputParts.push(part);
                }
            }
        }

        StringBuilder result = new StringBuilder("");
        Iterator it = outputParts.iterator();
        while(it.hasNext()){
            String part = (String) it.next();
            result.append(part);
            if(it.hasNext()){
                result.append('/');
            }
        }

        return result.toString();
    }

    public static boolean contains(String pathA, String pathB) {
        pathA = sanitizePath(pathA);
        pathB = sanitizePath(pathB);

        return !"..".equals(pathB) && !pathB.startsWith("../") && (pathB.equals(pathA) || pathA.length() == 0 || pathB.startsWith(pathA + "/"));
    }

    public static String toLocal(String path, String location) {
        path = sanitizePath(path);
        location = sanitizePath(location);

        assert contains(location, path);
        String local = path.substring(location.length());
        if(local.startsWith("/")){
            return local.substring(1);
        }
        return local;
    }

    private class MountWrapper {

        private String label;
        private String location;
        private IMount mount;
        private IWritableMount writableMount;

        public MountWrapper(String label, String location, IMount mount) {
            this.label = label;
            this.location = location;
            this.mount = mount;
            this.writableMount = null;
        }

        public MountWrapper(String label, String location, IWritableMount mount) {
            this(label, location, (IMount) mount);
            this.writableMount = mount;
        }

        public long getFreeSpace() {
            if(this.writableMount == null){
                return 0L;
            }

            try{
                return this.writableMount.getRemainingSpace();
            }catch(IOException ignored){
            }
            return 0L;
        }

        public boolean isReadOnly(@SuppressWarnings("unused") String path) throws FileSystemException {
            return this.writableMount == null;
        }

        public boolean exists(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                return this.mount.exists(path);
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public boolean isDirectory(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                return (this.mount.exists(path)) && (this.mount.isDirectory(path));
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public void list(String path, List<String> contents) throws FileSystemException {
            path = toLocal(path);
            try{
                if(this.mount.exists(path) && this.mount.isDirectory(path)){
                    this.mount.list(path, contents);
                }else{
                    throw new FileSystemException("Not a directory");
                }
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public long getSize(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                if(this.mount.exists(path)){
                    if(this.mount.isDirectory(path)){
                        return 0L;
                    }

                    return this.mount.getSize(path);
                }

                throw new FileSystemException("No such file");
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public InputStream openForRead(String path) throws FileSystemException {
            path = toLocal(path);
            try{
                if((this.mount.exists(path)) && (!this.mount.isDirectory(path))){
                    return this.mount.openForRead(path);
                }

                throw new FileSystemException("No such file");
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public void makeDirectory(String path)
                throws FileSystemException {
            if(this.writableMount == null){
                throw new FileSystemException("Access Denied");
            }
            try{
                path = toLocal(path);
                if(this.mount.exists(path)){
                    if(!this.mount.isDirectory(path)){
                        throw new FileSystemException("File exists");
                    }
                }else{
                    this.writableMount.makeDirectory(path);
                }
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public void delete(String path) throws FileSystemException {
            if(this.writableMount == null){
                throw new FileSystemException("Access Denied");
            }
            try{
                path = toLocal(path);
                if(this.mount.exists(path)){
                    this.writableMount.delete(path);
                }
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public OutputStream openForWrite(String path) throws FileSystemException {
            if(this.writableMount == null){
                throw new FileSystemException("Access Denied");
            }
            try{
                path = toLocal(path);
                if((this.mount.exists(path)) && (this.mount.isDirectory(path))){
                    throw new FileSystemException("Cannot write to directory");
                }

                return this.writableMount.openForWrite(path);
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        public OutputStream openForAppend(String path) throws FileSystemException {
            if(this.writableMount == null){
                throw new FileSystemException("Access Denied");
            }
            try{
                path = toLocal(path);
                if(!this.mount.exists(path)){
                    throw new FileSystemException("No such file");
                }
                if(this.mount.isDirectory(path)){
                    throw new FileSystemException("Cannot write to directory");
                }

                return this.writableMount.openForAppend(path);
            }catch(IOException e){
                throw new FileSystemException(e.getMessage());
            }
        }

        private String toLocal(String path) {
            return FileSystem.toLocal(path, this.location);
        }

        public String getLabel() {
            return this.label;
        }

        public String getLocation() {
            return this.location;
        }
    }

    private static String stripExtension(String input){
        if (input == null) return null;

        int pos = input.lastIndexOf(".");
        if(pos == -1){
            return input;
        }

        return input.substring(0, pos);
    }
}
