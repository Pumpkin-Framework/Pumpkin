package nl.jk5.pumpkin.server.scripting.filesystem;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

class ComboMount implements IMount {

    private IMount[] parts;

    public ComboMount(IMount[] parts) {
        this.parts = parts;
    }

    @Override
    public boolean exists(String path) throws IOException {
        for(int i = this.parts.length - 1; i >= 0; i--){
            IMount part = this.parts[i];
            if(part.exists(path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isDirectory(String path) throws IOException {
        for(int i = this.parts.length - 1; i >= 0; i--){
            IMount part = this.parts[i];
            if(part.isDirectory(path)){
                return true;
            }
        }
        return false;
    }

    @Override
    public void list(String path, List<String> contents) throws IOException {
        List<String> foundFiles = null;
        int foundDirs = 0;
        for(int i = this.parts.length - 1; i >= 0; i--){
            IMount part = this.parts[i];
            if(part.exists(path) && part.isDirectory(path)){
                if(foundFiles == null){
                    foundFiles = Lists.newArrayList();
                }
                part.list(path, foundFiles);
                foundDirs++;
            }
        }

        if(foundDirs == 1){
            contents.addAll(foundFiles);
        }else if(foundDirs > 1){
            Set<String> seen = Sets.newHashSet();
            assert foundFiles != null;
            for(String foundFile : foundFiles){
                if(seen.add(foundFile)){
                    contents.add(foundFile);
                }
            }
        }else{
            throw new IOException("Not a directory");
        }
    }

    @Override
    public long getSize(String path) throws IOException {
        for(int i = this.parts.length - 1; i >= 0; i--){
            IMount part = this.parts[i];
            if(part.exists(path)){
                return part.getSize(path);
            }
        }
        throw new IOException("No such file");
    }

    @Override
    public InputStream openForRead(String path) throws IOException {
        for(int i = this.parts.length - 1; i >= 0; i--){
            IMount part = this.parts[i];
            if((part.exists(path)) && (!part.isDirectory(path))){
                return part.openForRead(path);
            }
        }
        throw new IOException("No such file");
    }
}
