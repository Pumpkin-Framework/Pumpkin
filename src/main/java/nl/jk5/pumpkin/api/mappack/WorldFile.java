package nl.jk5.pumpkin.api.mappack;

public interface WorldFile {

    String getPath();

    String getFileId();

    boolean isRequired();

    String getChecksum();
}
