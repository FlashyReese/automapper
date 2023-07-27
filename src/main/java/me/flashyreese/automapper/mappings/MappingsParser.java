package me.flashyreese.automapper.mappings;

import me.flashyreese.automapper.util.Constants;
import me.flashyreese.automapper.yarn.YarnVersion;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MappingsParser {

    private static final Pattern classRegex = Pattern.compile("(class_[0-9$]*)");

    private static final String CLASS = "c";
    private static final String METHOD = "m";
    private static final String FIELD = "f";

    public static class Mappings {
        public Map<String, String> fullClasses = new HashMap<>();
        public Map<String, String> classes = new HashMap<>();
        public Map<String, String> fields = new HashMap<>();
        public Map<String, String> methods = new HashMap<>();


        public static class MappingResult {
            public String mappedLog;
            public int classesMapped;
            public int methodsMapped;
            public int fieldsMapped;
        }

        /**
         * Maps the given intermediary log or text with the given version's mappings.
         * If the given version has no mappings for it, null is returned.
         *
         * @param log      log or intermediary code to map to yarn
         * @return The MappingResult object containing the mapped log and statistics.
         */
        public MappingResult mapLog(String log) {
            MappingResult mappingResult = new MappingResult();
            int classesMapped = 0;
            int methodsMapped = 0;
            int fieldsMapped = 0;

            // replace full classes (net.minecraft.class_xyz)
            Pattern fullClassRegex = Pattern.compile("(net.minecraft.class_[0-9$]*)");
            Matcher fullClassMatcher = fullClassRegex.matcher(log);
            Map<String, String> fullClassReplacements = this.fullClasses;
            while (fullClassMatcher.find()) {
                String match = fullClassMatcher.group();
                String replacement = fullClassReplacements.get(match);
                if (replacement != null) {
                    log = log.replace(match, replacement);
                    classesMapped++;
                }
            }

            // replace short methods (method_xyz)
            Pattern shortMethodRegex = Pattern.compile("(method_[1-9])\\d*");
            Matcher shortMethodMatcher = shortMethodRegex.matcher(log);
            Map<String, String> methodReplacements = this.methods;
            while (shortMethodMatcher.find()) {
                String match = shortMethodMatcher.group();
                String replacement = methodReplacements.get(match);
                if (replacement != null) {
                    log = log.replace(match, replacement);
                    methodsMapped++;
                }
            }

            // replace short classes (class_xyz)
            Pattern classRegex = Pattern.compile("(class_[0-9$]*)");
            Matcher classMatcher = classRegex.matcher(log);
            Map<String, String> classReplacements = this.classes;
            while (classMatcher.find()) {
                String match = classMatcher.group();
                String replacement = classReplacements.get(match);
                if (replacement != null) {
                    log = log.replace(match, replacement);
                    classesMapped++;
                }
            }

            // replace short fields
            Pattern fieldRegex = Pattern.compile("(field_[1-9])\\d*");
            Matcher fieldMatcher = fieldRegex.matcher(log);
            Map<String, String> fieldReplacements = this.fields;
            while (fieldMatcher.find()) {
                String match = fieldMatcher.group();
                String replacement = fieldReplacements.get(match);
                if (replacement != null) {
                    log = log.replace(match, replacement);
                    fieldsMapped++;
                }
            }

            mappingResult.mappedLog = log;
            mappingResult.classesMapped = classesMapped;
            mappingResult.methodsMapped = methodsMapped;
            mappingResult.fieldsMapped = fieldsMapped;

            return mappingResult;
        }
    }

    /**
     * Parses a tiny mapping v2 file
     */
    public static Mappings parseMappings(String data) {

        Mappings mappings = new Mappings();

        // iterate over each line in the file by splitting at newline
        String[] lines = data.split("\n");
        for (String line : lines) {
            String[] splitLine = line.trim().split("\t"); // remove extra spacing at back and front, split at tab character
            String type = splitLine[0];

            // parse data based on starting line character
            if (type.equals(CLASS) && splitLine.length == 3) {
                splitLine[1] = splitLine[1].replace("/", ".");
                splitLine[2] = splitLine[2].replace("/", ".");
                parseClass(splitLine[1], splitLine[2], mappings);
            } else if (type.equals(METHOD) && splitLine.length == 4) {
                parseMethod(splitLine[1], splitLine[2], splitLine[3], mappings);
            } else if (type.equals(FIELD) && splitLine.length == 4) {
                parseField(splitLine[1], splitLine[2], splitLine[3], mappings);
            }
        }

        return mappings;
    }

    /**
     * Parses and stores class information from the given data.
     *
     * @param unmapped  unmapped form of class [net/minecraft/class_1]
     * @param mapped    mapped form of class [net/minecraft/entity/MyEntity]
     * @param mappings  The mappings to append to.
     */
    private static void parseClass(String unmapped, String mapped, Mappings mappings) {
        mappings.fullClasses.put(unmapped, mapped);

        // get short class name
        Matcher matcher = classRegex.matcher(unmapped);
        if (matcher.find()) {
            String shortClassMatch = matcher.group(0);
            String[] splitReplacement = mapped.split("\\.");
            String shortClassReplacement = splitReplacement[splitReplacement.length - 1];
            mappings.classes.put(shortClassMatch, shortClassReplacement);
        }
    }

    /**
     * Parses and stores method information from the given data.
     *
     * @param params    unmapped method descriptor [(Lnet/minecraft/class_1;)V]
     * @param unmapped  unmapped method name [method_1]
     * @param mapped    mapped method name [myMethod]
     * @param mappings  The mappings to append to.
     */
    private static void parseMethod(String params, String unmapped, String mapped, Mappings mappings) {
        mappings.methods.put(unmapped, mapped);
    }

    /**
     * Parses and stores field information from the given data.
     *
     * @param type      unmapped type as a class descriptor [Lnet/minecraft/class_2941;]
     * @param unmapped  unmapped field name [field_1]
     * @param mapped    mapped field name [myField]
     * @param mappings  The mappings to append to.
     */
    private static void parseField(String type, String unmapped, String mapped, Mappings mappings) {
        mappings.fields.put(unmapped, mapped);
    }

    public static CompletableFuture<Mappings> downloadMappingsAsync(YarnVersion version, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return downloadMappings(version);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }, executor);
    }

    public static Mappings downloadMappings(YarnVersion version) throws IOException {
        HttpURLConnection connection = getYarnVersionURLConnection(version);

        try (InputStream inputStream = connection.getInputStream();
             ZipInputStream zipInputStream = new ZipInputStream(inputStream, StandardCharsets.UTF_8)) {

            ZipEntry entry;
            while ((entry = zipInputStream.getNextEntry()) != null) {
                if (entry.getName().equals("mappings/mappings.tiny")) {
                    String mappingFileContent = readZipEntryContent(zipInputStream);
                    return parseMappings(mappingFileContent);
                }
            }
        }
        return new Mappings();
    }

    @NotNull
    private static HttpURLConnection getYarnVersionURLConnection(YarnVersion version) throws IOException {
        String yarnVersion = version.gameVersion + version.separator + version.build;
        String url = Constants.YARN_JAR_URL + "/" + yarnVersion + "/yarn-" + yarnVersion + "-v2.jar";

        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setRequestMethod("GET");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("Failed to download Yarn mappings: " + connection.getResponseCode() + " " + connection.getResponseMessage());
        }
        return connection;
    }

    private static String readZipEntryContent(ZipInputStream zipInputStream) throws IOException {
        byte[] buffer = new byte[1024];
        StringBuilder content = new StringBuilder();
        int bytesRead;
        while ((bytesRead = zipInputStream.read(buffer)) != -1) {
            content.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));
        }
        return content.toString();
    }
}