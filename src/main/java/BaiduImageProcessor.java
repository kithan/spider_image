import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.processor.PageProcessor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class BaiduImageProcessor implements PageProcessor {

   private  final Logger LOGGER= LoggerFactory.getLogger(BaiduImageProcessor.class);
    public static final String KEY ="餐桌";
    public static final String SAVE ="dining table";

    private AtomicInteger count=new AtomicInteger(1);
    private Site site = Site.me().setRetryTimes(3)
            .addHeader("User-Agent","Mozilla/5.0 (Windows NT 6.1; WOW64; rv:23.0) Gecko/20100101 Firefox/23.0")
            .setSleepTime(1000).setTimeOut(10000);
    public static final  int PAGE=20;
    public void process(Page page) {
            if(page.getRequest().getUrl().contains("&pn=1&")){
                List<String>requests=new ArrayList<String>();
                for(int i=2;i<=PAGE;i++){
                    requests.add("http://image.baidu.com/search/avatarjson?tn=resultjsonavatarnew&ie=utf-8&word="
                            + URLEncoder.encode(KEY) + "&pn="+i  + "&rn=60&itg=0&z=0&fr=&width=&height=&lm=-1&ic=0&s=0&st=-1&gsm=1e0000001e");
                }
                page.addTargetRequests(requests);
            }
        JSONObject object= JSON.parseObject(page.getJson().toString());
        JSONArray imgs=object.getJSONArray("imgs");
         for(int i=0;i<imgs.size();i++){
             JSONObject jsonObject=imgs.getJSONObject(i);
             String img=jsonObject.getString("objURL");
             LOGGER.info(img);
             try {
                download(img,count.getAndIncrement()+".jpg", SAVE);
             } catch (Exception e) {
                 LOGGER.error(e.toString());
             }
         }

    }

    public Site getSite() {
        return site;
    }

    private static void download(String urlString, String filename, String savePath) throws Exception {
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        con.addRequestProperty("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");
        con.setConnectTimeout(5 * 1000);
        InputStream is = con.getInputStream();
        byte[] bs = new byte[1024];
        int len;
        File sf = new File(savePath);
        if (!sf.exists()) {
            sf.mkdirs();
        }
        OutputStream os = new FileOutputStream(sf.getPath() + "\\" + filename);
        while ((len = is.read(bs)) != -1) {
            os.write(bs, 0, len);
        }
        os.close();
        is.close();
    }

    public static void main(String[] args) {
        String url="http://image.baidu.com/search/avatarjson?tn=resultjsonavatarnew&ie=utf-8&word="
                + URLEncoder.encode(KEY) + "&pn=1"  + "&rn=60&itg=0&z=0&fr=&width=&height=&lm=-1&ic=0&s=0&st=-1&gsm=1e0000001e";
        Spider.create(new BaiduImageProcessor())
                .addUrl(url).thread(5).run();
    }

}