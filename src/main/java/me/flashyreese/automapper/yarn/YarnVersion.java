package me.flashyreese.automapper.yarn;

public class YarnVersion {
    public String gameVersion;
    public String separator;
    public int build;
    public String maven;
    public String version;
    public boolean stable;

    public YarnVersion(String gameVersion, String separator, int build, String maven, String version, boolean stable) {
        this.gameVersion = gameVersion;
        this.separator = separator;
        this.build = build;
        this.maven = maven;
        this.version = version;
        this.stable = stable;
    }
}