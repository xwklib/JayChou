import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Tools {

    public static String post_url = "http://59.110.45.28/m/api/search";
    public static String userDir = System.getProperty("user.dir");

    // 发送post请求
    public static JSONObject SearchPost(String datum) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        MultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add("data", datum);
        map.add("v", "2");
        HttpEntity<MultiValueMap<String, Object>> param = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<String> response = restTemplate.postForEntity(post_url, param, String.class);
            String body = response.getBody();
            return JSON.parseObject(body);
        } catch (Exception e) {
            System.err.print("返回值不是200。");
        }
        return null;
    }

    // 获取重定向URL
    public static String getRediectUrl(String url_flac) {
        try {
            StringBuffer buffer = new StringBuffer();
            String url = url_flac;
            URL serverUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) serverUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setInstanceFollowRedirects(false);
            conn.addRequestProperty("Accept-Charset", "UTF-8;");
            conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
            conn.addRequestProperty("Referer", "http://matols.com/");
            conn.connect();
            String location = null;
            //判定是否会进行302重定向
            while (conn.getResponseCode() == 302) {
                // 如果会重定向，保存302重定向地址，以及Cookies,然后重新发送请求(模拟请求)
                location = conn.getHeaderField("Location");
                String cookies = conn.getHeaderField("Set-Cookie");
                serverUrl = new URL(location);
                conn = (HttpURLConnection) serverUrl.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Cookie", cookies);
                conn.addRequestProperty("Accept-Charset", "UTF-8;");
                conn.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; zh-CN; rv:1.9.2.8) Firefox/3.6.8");
                conn.addRequestProperty("Referer", "http://matols.com/");
                conn.connect();
            }
            return location;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 从最终的URL下载文件
    public static void downloadUsingStream(String downloadUrl, String filename) throws IOException {
        URL url = new URL(downloadUrl);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(filename);
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }

    // 简化JSONObject
    public static JSONObject pareObject(JSONObject rawObject) {
        String artist = rawObject.getString("artist");     // 歌手
        String name = rawObject.getString("name");         // 歌曲名
        String url_flac = rawObject.getString("url_flac"); // 无损
        String url_320 = rawObject.getString("url_320");   // 高品质
        String url_128 = rawObject.getString("url_128");   // 标准
        String url = rawObject.getString("url");           // 流畅
        String[] formats = {url_flac, url_320, url_128, url};
        JSONObject res = new JSONObject();
        res.put("artist", artist);
        res.put("name", name);
        res.put("formats", formats);
        return res;
    }

    // 中断运行命令并返回输出
    public static String run(String command) {
        Runtime runtime = Runtime.getRuntime();
        try {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                            runtime.exec(command).getInputStream(),
                            "GB2312"
                    )
            );
            String line;
            StringBuilder b = new StringBuilder();
            while ((line = br.readLine()) != null) {
                b.append(line).append("\n");
            }
            return b.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // 加密
    public static String encode(String keywords, int page, String searchType) {
        String myfreemp3 = userDir + "\\src\\main\\resources\\MyFreeMp3.js";
        keywords = "\"" + keywords + "\"";
        String command = String.format("node %s %s %d %s",
                myfreemp3, keywords, page, searchType);
        String res = run(command);
        if (res != null) {
//            System.out.print(res);
            String data = res.substring(5, res.length() - 5);
//            System.out.println(data);
            return data;
        } else {
            return null;
        }
    }

    // 写入丢失的音乐
    public static void wirteIntoLostMusic(String content) {
        writeIntoFile("lostMusic.txt", content);
    }

    public static void writeIntoFile(String filename, String content) {
        try {
            String filepath = userDir + "\\src\\main\\resources\\" + filename;
            File file = new File(filepath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(file.getAbsoluteFile(), true);
            BufferedWriter bw = new BufferedWriter(fileWriter);
            bw.write(content);
            bw.close();
        } catch (Exception ignored) {

        }
    }

    // 搜索音乐并返回选择的音乐
    public static JSONObject search(String keywords, int page, String searchType) {
        // 将keywords加密成data
        String data = encode(keywords, page, searchType);
        // 发送post请求并获取返回的JSONObject
        JSONObject resp = SearchPost(data);
        if (resp == null) {
            System.err.println("搜索\t" + keywords + "\t时出错，可能是搜索源 " + searchType + " 不对， 已跳过");
            wirteIntoLostMusic(keywords + "\n");
            return null;
        }
        // 从JSONObject获取list，即JSONArray
        JSONArray list = resp.getJSONObject("data").getJSONArray("list");
        // 对JSONArray遍历，调用this.pareObject转换成简单的格式
//        for (int i = 0; i < list.size(); ++i) {
        System.out.println("搜索的歌曲是：" + keywords);

        boolean flag = false;
        JSONObject res = new JSONObject();
        String ke_name = keywords.split(" - ")[0];
        String ke_artist = keywords.split(" - ")[1];
        for (int i = 0; i < list.size(); ++i) {
            JSONObject object = list.getJSONObject(i);
            object = pareObject(object);
            // 输出有用信息并让用户选择
            String artist = object.getString("artist");     // 歌手
            String name = object.getString("name");         // 歌曲名
            if (ke_artist.equals(artist) && ke_name.equals(name)) { // 如果有完全匹配的歌曲
                flag = true;
                res = object;
                break;
            }
            String tips = String.format("%s\t%s", name, artist);
            System.out.println(tips);
        }

        if (!flag) {
            System.err.print("请选择歌曲：（下标从0开始）-1则跳过");
            Scanner scanner = new Scanner(System.in);
            int index = scanner.nextInt();
            if (index < 0 || index >= list.size()) {
                return null;
            }
            res = list.getJSONObject(index);
            res = pareObject(res);
            // 输出有用信息并让用户选择
            String artist = res.getString("artist");     // 歌手
            String name = res.getString("name");         // 歌曲名
            String[] formats = res.getObject("formats", String[].class); // 格式
            String tips = String.format("%-30s%-30s", name, artist);
        } else {
            System.out.println("已自动选择");
        }
        return res;
    }

}
