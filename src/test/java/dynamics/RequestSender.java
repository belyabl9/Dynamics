package dynamics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.m1namoto.etc.AuthRequest;
import com.m1namoto.etc.RegRequest;

public class RequestSender {
    
    private final static String PROTOCOL = "http";
    private final static String HOST = "127.0.0.1:8080";
    private final static String USER_AGENT = "Mozilla/5.0";

    private static int sendRequest(String path, List<NameValuePair> params) throws IOException {
        String url = PROTOCOL + "://" + HOST + path;
        
        HttpClient client = new DefaultHttpClient();
    
        HttpPost post = new HttpPost(url);
        post.setHeader("User-Agent", USER_AGENT);
        post.setEntity(new UrlEncodedFormEntity(params));

        HttpResponse response = client.execute(post);
        
        return response.getStatusLine().getStatusCode();
    }
    
    private static List<NameValuePair> prepareAuthParams(AuthRequest req) {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("login", req.getLogin()));
        urlParameters.add(new BasicNameValuePair("password", req.getPassword()));
        urlParameters.add(new BasicNameValuePair("stat", req.getStat()));
        
        return urlParameters;
    }

    public static int sendAuthRequest(AuthRequest req) throws IOException {
        return sendRequest("/auth", prepareAuthParams(req));
    }
    
    public static int sendDBCleanupRequest() throws IOException {
        // TODO do not remove admin user
//        return sendRequest("/action/dbCleanup", new ArrayList<NameValuePair>());
        return 200;
    }

    public static int sendRegRequest(String name, String surname, String login, String password, String stat) throws IOException {
        List<NameValuePair> urlParameters = new ArrayList<NameValuePair>();
        urlParameters.add(new BasicNameValuePair("name", name));
        urlParameters.add(new BasicNameValuePair("surname", surname));
        urlParameters.add(new BasicNameValuePair("login", login));
        urlParameters.add(new BasicNameValuePair("password", password));
        urlParameters.add(new BasicNameValuePair("stat", stat));

        return sendRequest("/reg", urlParameters);
    }
    
    public static int sendRegRequest(RegRequest req) throws ClientProtocolException, IOException {
        return sendRegRequest(req.getName(), req.getSurname(), req.getLogin(), req.getPassword(), req.getStat());
    }
    
}
