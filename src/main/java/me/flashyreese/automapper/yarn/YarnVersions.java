package me.flashyreese.automapper.yarn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YarnVersions {

    public static class GameVersion {
        public String version;
        public boolean stable;

        public GameVersion(String version, boolean stable) {
            this.version = version;
            this.stable = stable;
        }
    }

    private final Map<String, YarnVersion> latestYarnByGameVersion;
    public List<GameVersion> gameVersions = new ArrayList<>();

    public YarnVersions(List<YarnVersion> versionsJson) {
        // Collect all unique game versions and map them to their latest yarn mappings
        Map<String, YarnVersion> versionToLatestYarn = new HashMap<>();
        for (YarnVersion versionJson : versionsJson) {
            String gameVersion = versionJson.gameVersion;
            // This assumes the incoming list is already sorted newest->oldest
            if (this.gameVersions.stream().noneMatch(gv -> gv.version.equals(gameVersion))) {
                this.gameVersions.add(new GameVersion(gameVersion, versionJson.stable));
            }

            YarnVersion curVersion = versionToLatestYarn.get(gameVersion);
            if (curVersion != null && curVersion.build > versionJson.build && curVersion.stable) {
                continue; // Already have a newer stable build
            }
            versionToLatestYarn.put(gameVersion, versionJson);
        }
        this.latestYarnByGameVersion = versionToLatestYarn;
    }

    public YarnVersion getYarnForGameVersion(String gameVersion) {
        YarnVersion result = this.latestYarnByGameVersion.get(gameVersion);
        if (result == null) {
            throw new IllegalArgumentException("Unable to find Yarn version for: " + gameVersion);
        }
        return result;
    }
}
