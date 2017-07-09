
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;


/**
 * Apache HTTP client を用いたＨＴＴＰ通信を担うクラス
 * @author Seo-4d696b75
 * @version 2017/07/02
 */
public class HttpClient {

    private String response;
    private int statusCode;

    public boolean get(String path){
        DefaultHttpClient client = new DefaultHttpClient();
        response = null;
        try{
            HttpGet httpGet = new HttpGet(path);
            HttpResponse httpResponse = client.execute(httpGet);
            String res = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if ( statusCode == 200 ) {
                response = res;
                return true;
            }
        }catch ( Exception e){
            e.printStackTrace();
        }finally {
            client.getConnectionManager().shutdown();
        }
        return  false;
    }

    public boolean post(String path, String entity){
        DefaultHttpClient client = new DefaultHttpClient();
        response = null;
        try {
            HttpPost httpPost = new HttpPost(path);
            httpPost.setEntity(new StringEntity(entity,"UTF-8"));
            HttpResponse httpResponse = client.execute(httpPost);
            statusCode = httpResponse.getStatusLine().getStatusCode();
            if (statusCode == 200) {
                response = EntityUtils.toString(httpResponse.getEntity(),"UTF-8");
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            client.getConnectionManager().shutdown();
        }
        return false;
    }

    public String getResponse(){
        return response;
    }

}


