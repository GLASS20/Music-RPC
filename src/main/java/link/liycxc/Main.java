package link.liycxc;

import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

/**
 * This file is part of Netease-CloudMusic-Getter project.
 * Copyright 2023 Liycxc
 * All Rights Reserved.
 *
 * @author Liycxc
 * 25/7/2023 下午4:42
 */

public class Main {

    // 网易云缓存地址，默认为AppData下
    private static final String HISTORY_FILE_PATH = System.getProperty("user.home") + File.separator +
            "AppData" + File.separator + "Local" + File.separator +
            "Netease" + File.separator + "CloudMusic" + File.separator +
            "webdata" + File.separator + "file" + File.separator + "history";

    public static void main(String[] args) {
        try {
            // 监听缓存文件更改
            WatchService watchService = FileSystems.getDefault().newWatchService();
            Path dir = Path.of(HISTORY_FILE_PATH).getParent();
            dir.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);

            while (true) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    if ("history".equals(event.context().toString())) {
                        // 监听到文件更改
                        String filePath = dir.resolve((Path) event.context()).toString();
                        System.out.println("Event " + System.currentTimeMillis() + " " + filePath);

                        // 等待缓存写入
                        Thread.sleep(1500);

                        // 获取歌曲详情
                        Map<String, Object> playingInfo = getPlaying(filePath);
                        if (playingInfo != null) {
                            String trackName = (String) playingInfo.get("track_name");
                            List<String> artistList = (List<String>) playingInfo.get("artist_list");
                            String str = "Track Name: " + trackName + " " + "Artists: " + artistList;
                            savePlayingToFile(str);
                        } else {
                            savePlayingToFile("No playing information found.");
                        }
                    }
                }
                key.reset();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, Object> getPlaying(String path) {
        JsonObject trackInfo = null;

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            String jsonString = sb.toString();

            JsonParser parser = new JsonParser();
            JsonArray jsonArray = parser.parse(jsonString).getAsJsonArray();

            for (int i = 0; i < jsonArray.size(); i++) {
                try {
                    trackInfo = jsonArray.get(i).getAsJsonObject();
                    break;
                } catch (Exception e) {
                    // JSON解析失败，继续尝试下一个元素
                }
            }

            if (trackInfo == null) {
                System.out.println("No track information found.");
                return null;
            }

            String trackName = trackInfo.getAsJsonObject("track").get("name").getAsString();

            JsonArray artistArray = trackInfo.getAsJsonObject("track").getAsJsonArray("artists");
            List<String> artistList = new ArrayList<>();
            for (int i = 0; i < artistArray.size(); i++) {
                JsonObject artistObj = artistArray.get(i).getAsJsonObject();
                String artistName = artistObj.get("name").getAsString();
                artistList.add(artistName);
            }

            System.out.println("Track Name: " + trackName);
            System.out.println("Artist List: " + artistList);

            Map<String, Object> result = new HashMap<>();
            result.put("track_name", trackName);
            result.put("artist_list", artistList);

            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static void savePlayingToFile(String playing) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("playing.txt", true))) {
            writer.write(playing);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}