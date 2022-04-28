import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import java.io.IOException;

public class JayChou {

    public static String[] data = {
            "9166CywqGMk7WxHUcUsUxMnMjkbKpWkl2UmDDlovs7BqzY80cz8u9OghgUBMU1QIjbtwy37DU8DEYaoW",
            "82e8lm6rdLyLE-bWtZW6CFNfKX0gzcv3WXWWyyfbdI0ttXt7qo5VObxkZw0dghLVTu971F32J50zVOld",
            "3227qxGgDdSTwVnfmUS_lo8A35aU87tJhymCEfiD1Yz1aQUHr_rceXpli4E5TyfV0iJn0SPwxVy5Ja-5",
            "021dzZEx3wPfLsJas_ymNHU1t4xnfCjdphxs_kkJGT-iibqLoXXPvZdsLtsaCSWeEYriwZoniSOF_gHX",
            "69d6piX6Kw5s0t8Fswi7lYHgeRBC9xkauEqSEtAblM7MrTd1aG9IorodIuOm3nc6Sa_OW3ZXo2vJRang",
            "5d09Vtt8UjfIdmqU8UbHz2Q6xzqPdfN0nwGflp4W5vRilvzo7REmViDgSCs-V7U_KOwmJfar4tg_auqU",
            "bfaaznaiACD7_d_rwatLrK5S0m3UTeFlFJtfodJIlXgbTGTTR9iwFTW71cGzaEt40I-7-GV7Pcfn_TjR",
            "b823emUWZsGxo6FeoSSM6gsLNV0xeTAG5cwsmQRv-3fQI4zMKZSH-n7CuVyBuP34qMGcEKa_ilx1xabK",
            "d42bjafC_K9qXZsZPVaXfWoPE71ybvwe7NI5tzpgDeZTJjANt_IlN23CwDz8gokFYSfQMck3VZl3wuBo",
            "382dTzXiAHtzF14RUF9HR6NMkC8_FMVNjat6D9a3fneEJPT-URL3NOVvOMnM8a0MNmIN9Fo7iuwHzFt6gA",
            "3b70Nr2eP9bJTrc-oWyg__flpaC_fZtX-ObwxII65v02Xrin6UwBYvtVBPpL2g01SBP8D0SLWGKm0qRKfw",
            "c9f9fsC1hkb7EiNKrOXw4fdZnYn89kMzdSFq7U37tKUQrfsm4tuuxcyFq8sTq_2IbHW9xCfS8cEPJpUu-A",
            "27f5Xswvo84zsM35_-mlMhAjyyqxCOTIAPR6lyH--8e33M6ypAW7H3d0Xbha4PVWANGAtbo6lfMVN0C9jQ",
            "600aKlYFO9UyPVRnGF7QLZc6Ye2c1CA1-4WPvItqdo4usfBXNcKbOglLT7Jhw7ZhrBMJ4bwk0rg76ueoWQ",
            "3c75Cyvg0gU1OTANscE4JQkCN4vNRy4dE_twU9h7YCDap3Xa3U6NU0Pk9BO_AcAqT-KUuHWwLHfR7Emz3A",
    };


    public static void main(String[] args) throws IOException {
        // 对所有的请求进行处理
        for (String datum : data) {
            // 发送请求并获取返回体
            JSONObject resp = Tools.SearchPost( datum);
            // 提取其中的有用信息
            JSONArray list = resp.getJSONObject("data").getJSONArray("list");
            for (Object o : list) {
                JSONObject object = (JSONObject) o;
                object = Tools.pareObject(object);
                String artist = object.getString("artist");     // 歌手
                String name = object.getString("name");         // 歌曲名
                String[] formats = object.getObject("formats", String[].class);
                // 只下载周杰伦参唱的歌
                if (artist.contains("周杰伦")) {
                    for (String s : formats) {
                        if (s != null) {
                            String nUrl = Tools.getRediectUrl(s);
                            Tools.DownloadAndRename(artist, name, nUrl);
                            break;
                        }
                    }
                } else {
                    System.err.println(name + "\t" + artist + "未下载");
                }
            }
        }
    }
}