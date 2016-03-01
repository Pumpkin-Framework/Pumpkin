package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

interface FileInputStreamFileSystem extends FileSystem {

    File root();

    @Override
    default boolean exists(String path) {
        return new File(root(), path).exists();
    }

    @Override
    default long size(String path) {
        File f = new File(root(), path);
        if(f.isFile()){
            return f.length();
        }
        return 0;
    }

    @Override
    default boolean isDirectory(String path) {
        return new File(root(), path).isDirectory();
    }

    @Override
    default long lastModified(String path) {
        return new File(root(), path).lastModified();
    }

    @Override
    default String[] list(String path) throws FileNotFoundException {
        File f = new File(root(), path);
        if(f.exists() && f.isFile()){
            return new String[]{f.getName()};
        }else if(f.exists() && f.isDirectory() && f.listFiles() != null){
            return Arrays.asList(f.listFiles()).stream().map(dir -> dir.isDirectory() ? dir.getName() + "/" : dir.getName()).toArray(String[]::new);
        }else{
            throw new FileNotFoundException("no such file or directory: " + path);
        }
    }
}
