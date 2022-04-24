import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;

public class MyLikeMusic {

    public static String BASE_URL = "C:\\Users\\XWK\\Music\\Like\\";

    private static void DownloadAndRename(String artist, String name, String nUrl) throws IOException {
        System.out.print("开始下载\t" + artist + "\t" + name);
        long begin_time = new Date().getTime();
        // 在这里修改文件名
        Tools.downloadUsingStream(nUrl, BASE_URL + name + "-" + artist + ".flac"); // 如：青花瓷-周杰伦.flac

        long end_time = new Date().getTime();
        long seconds = (end_time - begin_time) / 1000;
        long minutes = seconds / 60;
        long second = seconds % 60;
        System.out.println(name + "\t下载完成,用时：" + minutes + "分" + second + "秒");
    }

    public static void main(String[] args) throws IOException {
        FileReader fr = new FileReader(Tools.userDir + "\\src\\main\\resources\\Like.txt");
        BufferedReader br = new BufferedReader(fr);
        String line = "";
        while ((line = br.readLine()) != null) {
//            System.out.println(line);
            JSONObject object = Tools.search(line, 1, "migu");

            if (object != null) {
                String artist = object.getString("artist");     // 歌手
                String name = object.getString("name");         // 歌曲名
                String[] formats = object.getObject("formats", String[].class);
                // 下载歌曲
                for (String s : formats) {
                    if (s != null) {
                        String nUrl = Tools.getRediectUrl(s);
                        DownloadAndRename(artist, name, nUrl);
                        Tools.writeIntoFile("Downloaded.txt", line + "\n");
                        break;
                    }
                }
            }
        }
        br.close();
        fr.close();
    }
}
