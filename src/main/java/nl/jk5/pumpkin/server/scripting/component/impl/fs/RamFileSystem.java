package nl.jk5.pumpkin.server.scripting.component.impl.fs;

public class RamFileSystem extends VirtualFileSystem {

    public RamFileSystem() {
    }

    @Override
    public long spaceTotal() {
        return 0;
    }

    @Override
    public long spaceUsed() {
        return 0;
    }
}
