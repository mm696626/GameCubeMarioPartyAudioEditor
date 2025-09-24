package io;

public class QueueJob {
    public enum Type { DUMP, MODIFY }

    Type type;
    String songName;
    int songIndex;
    String leftChannel;
    String rightChannel;

    public QueueJob(Type type, String songName, int songIndex, String leftChannel, String rightChannel) {
        this.type = type;
        this.songName = songName;
        this.songIndex = songIndex;
        this.leftChannel = leftChannel;
        this.rightChannel = rightChannel;
    }

    @Override
    public String toString() {
        return type + ": " + songName +
                (type == Type.MODIFY ? (" | Left: " + leftChannel + " | Right: " + rightChannel) : "");
    }

    public Type getType() {
        return type;
    }

    public String getSongName() {
        return songName;
    }

    public int getSongIndex() {
        return songIndex;
    }

    public String getLeftChannel() {
        return leftChannel;
    }

    public String getRightChannel() {
        return rightChannel;
    }
}
