package nl.jk5.pumpkin.server.scripting.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

class ReadOnlyDirMount implements IMount {

    File rootPath;

    public ReadOnlyDirMount(File rootPath) {
        this.rootPath = rootPath;
    }

    private File getRealPath(String path) throws IOException {
        return new File(this.rootPath, path);
    }

    private boolean created() {
        return this.rootPath.exists();
    }

    @Override
    public boolean exists(String path) throws IOException {
        if(!created()){
            return path.length() == 0;
        }

        File file = getRealPath(path);
        return file.exists();
    }

    @Override
    public boolean isDirectory(String path) throws IOException {
        if(!created()){
            return path.length() == 0;
        }
        File file = getRealPath(path);
        return (file.exists()) && (file.isDirectory());
    }

    @Override
    public void list(String path, List<String> contents) throws IOException {
        if(!created()){
            if(path.length() != 0){
                throw new IOException("Not a directory");
            }
        }else{
            File file = getRealPath(path);
            if((file.exists()) && (file.isDirectory())){
                String[] paths = file.list();
                for(String subPath : paths){
                    if(new File(file, subPath).exists()){
                        contents.add(subPath);
                    }
                }
            }else{
                throw new IOException("Not a directory");
            }
        }
    }

    @Override
    public long getSize(String path) throws IOException {
        if(!created()){
            if(path.length() == 0){
                return 0L;
            }
        }else{
            File file = getRealPath(path);
            if(file.exists()){
                if(file.isDirectory()){
                    return 0L;
                }

                return file.length();
            }
        }

        throw new IOException("No such file");
    }

    @Override
    public InputStream openForRead(String path) throws IOException {
        if(created()){
            File file = getRealPath(path);
            if(file.exists() && !file.isDirectory()){
                return new FileInputStream(file);
            }
        }
        throw new IOException("No such file");
    }
}
