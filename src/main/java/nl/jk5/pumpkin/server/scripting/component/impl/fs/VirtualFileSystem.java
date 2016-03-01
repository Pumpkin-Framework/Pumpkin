package nl.jk5.pumpkin.server.scripting.component.impl.fs;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import gnu.trove.list.array.TByteArrayList;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public abstract class VirtualFileSystem extends OutputStreamFileSystem {

    private final VirtualDirectory root = new VirtualDirectory();

    // ----------------------------------------------------------------------- //

    @Override
    public boolean exists(String path) {
        return root.get(segments(path)).isPresent();
    }

    @Override
    public boolean isDirectory(String path) {
        Optional<VirtualObject> object = root.get(segments(path));
        if(object.isPresent()){
            return object.get().isDirectory();
        }else{
            return false;
        }
    }

    @Override
    public long size(String path) {
        Optional<VirtualObject> object = root.get(segments(path));
        if(object.isPresent()){
            return object.get().size();
        }else{
            return 0;
        }
    }

    @Override
    public long lastModified(String path) {
        Optional<VirtualObject> object = root.get(segments(path));
        if(object.isPresent()){
            return object.get().lastModified;
        }else{
            return 0;
        }
    }

    @Override
    public String[] list(String path) throws FileNotFoundException {
        Optional<VirtualObject> object = root.get(segments(path));
        if(object.isPresent() && object.get() instanceof VirtualDirectory){
            return ((VirtualDirectory) object.get()).list();
        }else{
            return null;
        }
    }

    // ----------------------------------------------------------------------- //


    @Override
    public boolean delete(String path) {
        List<String> parts = segments(path);
        if(parts.isEmpty()){
            return true;
        }else{
            Optional<VirtualObject> obj = root.get(parts.subList(0, parts.size() - 1));
            if(obj.isPresent() && obj.get() instanceof VirtualDirectory){
                return ((VirtualDirectory) obj.get()).delete(parts.get(parts.size() - 1));
            }
            return false;
        }
    }

    @Override
    public boolean makeDirectory(String path) {
        List<String> parts = segments(path);
        if(parts.isEmpty()){
            return true;
        }else{
            Optional<VirtualObject> obj = root.get(parts.subList(0, parts.size() - 1));
            if(obj.isPresent() && obj.get() instanceof VirtualDirectory){
                return ((VirtualDirectory) obj.get()).makeDirectory(parts.get(parts.size() - 1));
            }
            return false;
        }
    }

    @Override
    public boolean rename(String from, String to) throws FileNotFoundException {
        if(from.isEmpty() || !exists(from)){
            throw new FileNotFoundException(from);
        }
        if(!exists(to)){
            List<String> segmentsTo = segments(to);
            Optional<VirtualObject> toOpt = root.get(segmentsTo.subList(0, segmentsTo.size() - 1));
            if(toOpt.isPresent() && toOpt.get() instanceof VirtualDirectory){
                VirtualDirectory toParent = ((VirtualDirectory) toOpt.get());
                String toName = segmentsTo.get(segmentsTo.size() - 1);
                List<String> segmentsFrom = segments(from);
                VirtualDirectory fromParent = (VirtualDirectory) root.get(segmentsFrom.subList(0, segmentsFrom.size() - 1)).get();
                String fromName = segmentsFrom.get(segmentsFrom.size() - 1);
                VirtualObject obj = fromParent.children.get(fromName);

                fromParent.children.remove(fromName);
                fromParent.lastModified = System.currentTimeMillis();

                toParent.children.put(toName, obj);
                toParent.lastModified = System.currentTimeMillis();

                obj.lastModified = System.currentTimeMillis();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setLastModified(String path, long time) {
        Optional<VirtualObject> obj = root.get(segments(path));
        if(obj.isPresent() && time >= 0){
            obj.get().lastModified = time;
            return true;
        }
        return false;
    }

    // ----------------------------------------------------------------------- //


    @Override
    protected Optional<InputChannel> openInputChannel(String path) throws IOException {
        Optional<VirtualObject> obj = root.get(segments(path));
        if(obj.isPresent() && obj.get() instanceof VirtualFile){
            return ((VirtualFile) obj.get()).openInputStream().map(InputStreamChannel::new);
        }
        return Optional.empty();
    }

    @Override
    protected Optional<OutputHandle> openOutputHandle(int id, String path, Mode mode) throws FileNotFoundException {
        List<String> segments = segments(path);
        if(segments.isEmpty()){
            return Optional.empty();
        }
        Optional<VirtualObject> obj = root.get(segments.subList(0, segments.size() - 1));
        if(obj.isPresent() && obj.get() instanceof VirtualDirectory){
            Optional<VirtualFile> res = ((VirtualDirectory) obj.get()).touch(segments.get(segments.size() - 1));
            if(res.isPresent() && res.get() instanceof VirtualFile){
                //noinspection unchecked
                return (Optional) res.get().openOutputHandle(this, id, path, mode);
            }
        }
        return Optional.empty();
    }

    // ----------------------------------------------------------------------- //

    protected List<String> segments(String path){
        return ImmutableList.copyOf(Splitter.on('/').omitEmptyStrings().split(path));
    }

    // ----------------------------------------------------------------------- //

    protected abstract class VirtualObject {

        protected long lastModified = System.currentTimeMillis();

        public abstract boolean isDirectory();

        public abstract long size();

        public Optional<VirtualObject> get(List<String> path){
            if(path.isEmpty()){
                return Optional.of(this);
            }else{
                return Optional.empty();
            }
        }

        public abstract boolean canDelete();
    }

    // ----------------------------------------------------------------------- //

    protected class VirtualFile extends VirtualObject {

        private TByteArrayList data = new TByteArrayList();

        private Optional<VirtualOutputHandle> handle = Optional.empty();

        @Override
        public boolean isDirectory() {
            return false;
        }

        @Override
        public long size() {
            return data.size();
        }

        public Optional<InputStream> openInputStream(){
            return Optional.of(new VirtualFileInputStream(this));
        }

        public Optional<VirtualOutputHandle> openOutputHandle(OutputStreamFileSystem owner, int id, String path, Mode mode){
            if(this.handle.isPresent()){
                return Optional.empty();
            }else{
                if(mode == Mode.Write){
                    this.data.clear();
                    this.lastModified = System.currentTimeMillis();
                }
                this.handle = Optional.of(new VirtualOutputHandle(this, owner, id, path));
                return this.handle;
            }
        }

        @Override
        public boolean canDelete() {
            return !handle.isPresent();
        }
    }

    // ----------------------------------------------------------------------- //

    protected class VirtualDirectory extends VirtualObject {

        private final Map<String, VirtualObject> children = new HashMap<>();

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public long size() {
            return 0;
        }

        public String[] list(){
            return this.children.entrySet().stream().map((e) -> e.getValue().isDirectory() ? e.getKey() + "/" : e.getKey()).toArray(String[]::new);
        }

        public boolean makeDirectory(String name){
            if(children.containsKey(name)){
                return false;
            }else{
                this.children.put(name, new VirtualDirectory());
                this.lastModified = System.currentTimeMillis();
                return true;
            }
        }

        public boolean delete (String name){
            VirtualObject child = this.children.get(name);
            if(child == null){
                return false;
            }
            if(child.canDelete()){
                this.children.remove(name);
                this.lastModified = System.currentTimeMillis();
                return true;
            }
            return false;
        }

        public Optional<VirtualFile> touch(String name){
            VirtualObject child = this.children.get(name);
            if(child != null){
                if(child instanceof VirtualFile){
                    return Optional.of(((VirtualFile) child));
                }else{
                    return Optional.empty();
                }
            }else{
                VirtualFile c = new VirtualFile();
                this.children.put(name, c);
                this.lastModified = System.currentTimeMillis();
                return Optional.of(c);
            }
        }

        @Override
        public Optional<VirtualObject> get(List<String> path) {
            Optional<VirtualObject> s = super.get(path);
            if(s.isPresent()){
                return s;
            }
            VirtualObject object = children.get(path.get(0));
            if(object != null){
                ImmutableList.Builder<String> builder = ImmutableList.builder();
                boolean first = true;
                for(String e : path){
                    if(first){
                        first = false;
                    }else{
                        builder.add(e);
                    }
                }
                return object.get(builder.build());
            }
            return Optional.empty();
        }

        @Override
        public boolean canDelete() {
            return children.isEmpty();
        }
    }

    // ----------------------------------------------------------------------- //

    protected class VirtualFileInputStream extends InputStream {

        private final VirtualFile file;

        private boolean closed = false;
        private int position = 0;

        public VirtualFileInputStream(VirtualFile file) {
            this.file = file;
        }

        @Override
        public int available() throws IOException {
            if(closed){
                return 0;
            }else{
                return Math.max(file.data.size() - position, 0);
            }
        }

        @Override
        public void close() throws IOException {
            closed = true;
        }

        @Override
        public int read() throws IOException {
            if(!closed){
                if(available() == 0){
                    return -1;
                }else {
                    position += 1;
                    return file.data.get(position - 1);
                }
            }else throw new IOException("file is closed");
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if(!closed){
                int count = available();
                if(count == 0){
                    return -1;
                }else {
                    int n = Math.min(len, count);
                    for(int i = position; i < file.data.size(); i++){
                        b[i - position] = file.data.get(i);
                    }
                    position += n;
                    return n;
                }
            }else throw new IOException("file is closed");
        }

        @Override
        public synchronized void reset() throws IOException {
            if(!closed){
                position = 0;
            }else throw new IOException("file is closed");
        }

        @Override
        public long skip(long n) throws IOException {
            if(!closed){
                position = Math.min((int) (position + n), Integer.MAX_VALUE);
                return position;
            }else throw new IOException("file is closed");
        }
    }

    // ----------------------------------------------------------------------- //

    protected class VirtualOutputHandle extends OutputHandle {

        private final VirtualFile file;

        private long position;

        public VirtualOutputHandle(VirtualFile file, OutputStreamFileSystem owner, int handle, String path) {
            super(owner, handle, path);
            this.file = file;

            this.position = file.data.size();
        }

        @Override
        public long length() throws IOException {
            return file.size();
        }

        @Override
        public long position() throws IOException {
            return file.data.size();
        }

        @Override
        public void close() {
            if(!isClosed()){
                super.close();
                assert file.handle.get() == this;
                file.handle = Optional.empty();
            }
        }

        @Override
        public long seek(long to) throws IOException {
            if(to < 0) throw new IOException("invalid offset");
            position = to;
            return position;
        }

        @Override
        public void write(byte[] value) throws IOException {
            if(!isClosed()){
                int pos = (int) position;
                byte[] arr = new byte[(pos + value.length) - file.data.size()];
                Arrays.fill(arr, (byte) 0);
                file.data.insert(file.data.size(), arr);
                for (int i = 0; i < value.length; i++) {
                    file.data.set(pos + i, value[i]);
                }
                position += value.length;
                file.lastModified = System.currentTimeMillis();
            }else throw new IOException("file is closed");
        }
    }
}
