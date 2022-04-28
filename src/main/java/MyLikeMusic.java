import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MyLikeMusic {

    public static void main(String[] args) throws IOException {
        // 从Like.txt按行读取音乐
        FileReader fr = new FileReader(Tools.userDir + "\\src\\main\\resources\\Like.txt");
        BufferedReader br = new BufferedReader(fr);
        String line;

        while ((line = br.readLine()) != null) {
            // 业务处理
            JSONObject object = Tools.process(line, 1, "migu");

            if (object != null) {
                String artist = object.getString("artist");     // 歌手
                String name = object.getString("name");         // 歌曲名
                String[] formats = object.getObject("formats", String[].class);
                // 下载歌曲
                for (String s : formats) {
                    if (s != null) {
                        String nUrl = Tools.getRediectUrl(s);
                        Tools.DownloadAndRename(artist, name, nUrl);
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
