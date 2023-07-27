package me.flashyreese.automapper;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.flashyreese.automapper.mappings.MappingsParser;
import me.flashyreese.automapper.util.Constants;
import me.flashyreese.automapper.util.Utils;
import me.flashyreese.automapper.yarn.YarnVersion;
import me.flashyreese.automapper.yarn.YarnVersions;
import net.fabricmc.api.ModInitializer;
import net.minecraft.SharedConstants;

import java.io.IOException;
import java.util.List;

public class Automapper implements ModInitializer {

    public static MappingsParser.Mappings mappings = new MappingsParser.Mappings();

    /**
     * Runs the mod initializer.
     */
    @Override
    public void onInitialize() {
        try {
            String jsonData = Utils.fetchJsonFromUrl(Constants.YARN_VERSION_ENDPOINT);
            Gson gson = new Gson();

            // Convert JSON to Java object using Gson
            List<YarnVersion> yarnVersionList = gson.fromJson(jsonData, new TypeToken<List<YarnVersion>>() {}.getType());

            YarnVersions yarnVersions = new YarnVersions(yarnVersionList);

            YarnVersion yarnVersion = yarnVersions.getYarnForGameVersion(SharedConstants.getGameVersion().getName());

            mappings = MappingsParser.downloadMappings(yarnVersion);
        } catch (IOException e) {
            System.out.println("Error trying to retrieve mappings!");
            // Todo: Fallback to FabricLoader's mappings
        }
    }
}
