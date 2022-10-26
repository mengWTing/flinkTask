package cn.ffcs.is.mss.analyzer.utils.druid;

import static java.lang.String.format;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.util.Map;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

/**
 * @author linlr@ffcs.cn
 * 2016-8-04 13:37:17
 */
public class BrokerAccessor {
    private final String brokerHost;
    private int brokerPort;
    private static final String brokerUrl = "http://%s:%d/druid/v2/?pretty";
    private static final PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
    private static CloseableHttpClient httpClient = null;
    private static final int timeOut = 1800 * 1000;
    private final static Object syncLock = new Object();

    public BrokerAccessor(String host, int port, int maxConns) {
        if (host != null) {
            HttpHost targetHost = new HttpHost(host, port);
            pool.setMaxPerRoute(new HttpRoute(targetHost), maxConns);//目标主机最大连接数
        }
        this.brokerHost = host;
        this.brokerPort = port;
    }

    public void config(HttpRequestBase httpRequestBase){
        // 配置请求的超时设置
        RequestConfig requestConfig = RequestConfig.custom()
            .setConnectionRequestTimeout(timeOut)
            .setConnectTimeout(timeOut).setSocketTimeout(timeOut).build();
        httpRequestBase.setConfig(requestConfig);
    }

    /*public static Map<String, Integer> getConnectionPoolStats() {
        Map<String, Integer> stats = new HashMap<String, Integer>();
        PoolStats poolStats = pool.getTotalStats();
        stats.put("availableConnections", poolStats.getAvailable());
        stats.put("maxConnections", poolStats.getMax());
        stats.put("leasedConnections", poolStats.getLeased());
        stats.put("pendingConnections", poolStats.getPending());
        stats.put("defaultMaxPerRoute", pool.getDefaultMaxPerRoute());
        return stats;
    }*/

    public static void setMaxConnections(int max) {
        pool.setMaxTotal(max);//最大连接数
    }

    public static void setDefaultMaxConnectionsPerRout(int max) {
        pool.setDefaultMaxPerRoute(max);//每个路由基础的连接数
    }

    public CloseableHttpClient getClient() {
        if (httpClient == null) {
            synchronized (syncLock) {
                if (httpClient == null) {
                    httpClient = createHttpClient();
                }
            }
        }
        return httpClient;
    }

    public CloseableHttpClient createHttpClient(){
        // 请求重试处理
        HttpRequestRetryHandler httpRequestRetryHandler = new HttpRequestRetryHandler() {
            public boolean retryRequest(IOException exception,
                int executionCount, HttpContext context) {
                if (executionCount >= 5) {// 如果已经重试了5次，就放弃
                    return false;
                }
                if (exception instanceof NoHttpResponseException) {// 如果服务器丢掉了连接，那么就重试
                    return true;
                }
                if (exception instanceof SSLHandshakeException) {// 不要重试SSL握手异常
                    return false;
                }
                if (exception instanceof InterruptedIOException) {// 超时
                    return false;
                }
                if (exception instanceof UnknownHostException) {// 目标服务器不可达
                    return false;
                }
                if (exception instanceof ConnectTimeoutException) {// 连接被拒绝
                    return false;
                }
                if (exception instanceof SSLException) {// SSL握手异常
                    return false;
                }

                HttpClientContext clientContext = HttpClientContext
                    .adapt(context);
                HttpRequest request = clientContext.getRequest();
                // 如果请求是幂等的，就再次尝试
                if (!(request instanceof HttpEntityEnclosingRequest)) {
                    return true;
                }
                return false;
            }
        };

        HttpClientBuilder builder = HttpClients.custom().setConnectionManager(pool).setRetryHandler(httpRequestRetryHandler);
        return builder.build();
    }

    public void returnClient(CloseableHttpResponse resp) {
        try {
            if (resp != null) {
                EntityUtils.consume(resp.getEntity());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void shutdown() {
        try {
            pool.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Convenient method for POSTing json strings. It is the responsibility of the caller to call
     * returnClient() to ensure clean state of the pool.
     */
    public CloseableHttpResponse postJson(String url, String json, Map<String, String> reqHeaders){

        CloseableHttpClient req = getClient();
        CloseableHttpResponse resp = null;
        HttpPost post = new HttpPost(url);
        this.config(post);
        addHeaders(post, reqHeaders);
        //post.setHeader(json, url);
        //post.setHeader("Content-Type", "application/json; charset=UTF-8");
        StringEntity input = new StringEntity(json, ContentType.APPLICATION_JSON);
        post.setEntity(input);
        try {
            resp = req.execute(post);
            if (200 != resp.getStatusLine().getStatusCode()){
                resp = null;
            }
        }catch (IOException exception){

        }
        return resp;
    }

    /**
     * Convenient method for GETing. It is the responsibility of the
     * caller to call returnClient() to ensure clean state of the pool.
     * @param url
     * @param reqHeaders
     * @return
     * @throws IOException
     */
    public CloseableHttpResponse get(String url, Map<String, String> reqHeaders) throws IOException {
        CloseableHttpClient req = getClient();
        CloseableHttpResponse resp = null;
        HttpGet get = new HttpGet(url);
        addHeaders(get, reqHeaders);
        resp = req.execute(get);
        return resp;
    }

    private void addHeaders(HttpRequestBase req, Map<String, String> reqHeaders) {
        if (reqHeaders == null)
            return;
        for (String key:reqHeaders.keySet()) {
            req.setHeader(key, reqHeaders.get(key));
        }
    }

    public String fireQuery(String jsonQuery) {
        return this.fireQuery(jsonQuery, null);
    }

    public String fireQuery(String jsonQuery, Map<String, String> reqHeaders) {
        CloseableHttpResponse resp = null;
        String respStr;
        String url = format(brokerUrl, brokerHost, brokerPort);
        try {
            resp = postJson(url, jsonQuery, reqHeaders);
            if (resp == null){
                return null;
            }
            respStr = IOUtils.toString(resp.getEntity().getContent(), Charset.forName("UTF-8"));
            return respStr;
        } catch (IOException ex) {
            return ex.getMessage();
        } finally {
            returnClient(resp);
        }
    }
}
