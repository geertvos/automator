package net.geertvos.k8s.automator.scripting.plugins.rabbitmq_shovel;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class ShovelClient {
    private static final Logger LOG = LogManager.getLogger(ShovelClient.class);

    private static final int RABBITMQ_MANAGEMENT_PORT = 15672;
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";
    private static final String SHOVEL_FORMAT = "amqp://%s:%s@%s/%s";
    private static final String SHOVEL_URL_FORMAT = "http://%s:%s/api/parameters/shovel/%%2F/%s";
    private static final String HTTP = "http://";
    private String sourceHost;
    private String sourceUsername;
    private String sourcePassword;
    private String destHost;
    private String destUsername;
    private String destPassword;

    public enum ResponseCode {
        OK(200),
        CREATED(201),
        NO_CONTENT(204),
        BAD_REQUEST(400),
        NOT_FOUND(404),
        INTERNAL_SERVER_ERROR(500);

        private final int code;

        ResponseCode(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }
    }

    public void init(String sourceUsername, String sourcePassword, String sourceHost,
                           String destUsername, String destPassword, String destHost) {
        this.sourceUsername = sourceUsername;
        this.sourcePassword = sourcePassword;
        this.sourceHost = sourceHost;
        this.destUsername = destUsername;
        this.destPassword = destPassword;
        this.destHost = destHost;
    }

    public ResponseCode createShovel(String vhost, JSONObject bindingJson) throws IOException {
        LOG.info("Creating shovel for queue {}", bindingJson.get("src-queue"));
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            String srcUri = String.format(SHOVEL_FORMAT, sourceUsername, sourcePassword, sourceHost, vhost);
            String destUri = String.format(SHOVEL_FORMAT, destUsername, destPassword, destHost, vhost);
            String shovelName = "shovel_" + bindingJson.get("src-queue");

            // Check if the shovel already exists
            String checkUrl = String.format(SHOVEL_URL_FORMAT, sourceHost, RABBITMQ_MANAGEMENT_PORT, shovelName);
            HttpGet checkRequest = new HttpGet(checkUrl);
            HttpResponse checkResponse = httpClient.execute(checkRequest);
            int checkStatusCode = checkResponse.getStatusLine().getStatusCode();
            if (checkStatusCode == HttpStatus.SC_OK) {
                // Shovel already exists, no need to create it again
                return ResponseCode.OK;
            }
            bindingJson.put("src-uri", srcUri);
            bindingJson.put("dest-uri", destUri);
            JSONObject valueJson = new JSONObject();
            valueJson.put("value", bindingJson);
            String createUrl = String.format(SHOVEL_URL_FORMAT, sourceHost, RABBITMQ_MANAGEMENT_PORT, shovelName);
            HttpPut httpPut = new HttpPut(createUrl);
            httpPut.addHeader(CONTENT_TYPE, APPLICATION_JSON);
            httpPut.setEntity(new StringEntity(valueJson.toString()));
            HttpResponse response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return ResponseCode.CREATED;
            } else {
                LOG.error("Failed to create shovel. Status code: {}", statusCode);
            }
        }
        return ResponseCode.BAD_REQUEST;
    }

    public ResponseCode deleteShovel(String shovelName) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            LOG.info("Deleting shovel {}", shovelName);
            String deleteUrl = String.format(SHOVEL_URL_FORMAT, sourceHost, RABBITMQ_MANAGEMENT_PORT, shovelName);
            HttpDelete deleteRequest = new HttpDelete(deleteUrl);
            HttpResponse deleteResponse;
            try {
                deleteResponse = httpClient.execute(deleteRequest);
            } catch (IOException e) {
                LOG.error("Failed to execute delete request for shovel {}", shovelName, e);
                return ResponseCode.INTERNAL_SERVER_ERROR;
            }
            if (deleteResponse.getStatusLine().getStatusCode() == HttpStatus.SC_NO_CONTENT) {
                LOG.warn("Shovel {} does not exist, nothing was deleted!", shovelName);
                return ResponseCode.NOT_FOUND;
            }
        }
        return ResponseCode.OK;
    }

    public List<String> getQueueBindings(String vhost, String queueName) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(destHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(destUsername, destPassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            String url = HTTP + destHost + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/queues/" + vhost + "/" + queueName + "/bindings";
            HttpGet request = new HttpGet(url);
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
                String jsonString = EntityUtils.toString(entity);
                JSONArray jsonArray = new JSONArray(jsonString);

                List<String> bindings = new ArrayList<>();
                jsonArray.forEach(item -> bindings.add(item.toString()));

                return bindings;
            }
        }
        return Collections.emptyList();
    }

    public ResponseCode createExchange(String vhost, String exchangeName, JSONObject exchangeJson) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            // Check if the exchange already exists
            String url = HTTP + sourceHost + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/exchanges/" + vhost + "/" + exchangeName;
            HttpGet getRequest = new HttpGet(url);
            HttpResponse getResponse = httpClient.execute(getRequest);
            if (getResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                // Exchange already exists, no need to create it again
                return ResponseCode.OK;
            }
            HttpPut request = new HttpPut(url);
            request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            request.setEntity(new StringEntity(exchangeJson.toString()));
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return ResponseCode.CREATED;
            } else {
                LOG.error("Failed to create exchange. Status code: {}", statusCode);
            }
        }
        return ResponseCode.BAD_REQUEST;
    }

    public ResponseCode createBinding(String vhost, String queueName, JSONObject bindingJson) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            String url = HTTP + sourceHost + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/bindings/" + vhost + "/e/" + bindingJson.getString("source") + "/q/" + queueName;
            HttpPost request = new HttpPost(url);
            request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            bindingJson.put("vhost", URLDecoder.decode(vhost, "UTF-8"));
            request.setEntity(new StringEntity(bindingJson.toString()));
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return ResponseCode.CREATED;
            } else {
                LOG.error("Failed to create binding. Status code: {}", statusCode);
            }
        }
        return ResponseCode.BAD_REQUEST;
    }

    public ResponseCode createQueue(String vhost, String queueName, JSONObject queueJson) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            HttpPut request = getHttpPut(vhost, queueName, queueJson);
            HttpResponse response = httpClient.execute(request);
            switch (response.getStatusLine().getStatusCode()) {
                case HttpStatus.SC_CREATED:
                    return ResponseCode.CREATED;
                case HttpStatus.SC_NO_CONTENT:
                    return ResponseCode.NO_CONTENT;
                default:
                    LOG.error("Failed to create queue. Status code: {}", response.getStatusLine().getStatusCode());
            }
        }
        return ResponseCode.BAD_REQUEST;
    }

    @NotNull
    private HttpPut getHttpPut(String vhost, String queueName, JSONObject queueJson) throws UnsupportedEncodingException {
        String url = HTTP + sourceHost + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/queues/" + vhost + "/" + queueName;
        HttpPut request = new HttpPut(url);
        request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        request.setEntity(new StringEntity(queueJson.toString()));
        return request;
    }

}
