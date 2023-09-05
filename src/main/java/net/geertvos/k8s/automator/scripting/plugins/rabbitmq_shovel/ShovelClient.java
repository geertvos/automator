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

    public void init(String sourceUsername, String sourcePassword, String sourceHost,
                           String destUsername, String destPassword, String destHost) {
        this.sourceUsername = sourceUsername;
        this.sourcePassword = sourcePassword;
        this.sourceHost = sourceHost;
        this.destUsername = destUsername;
        this.destPassword = destPassword;
        this.destHost = destHost;
    }

    public boolean createShovel(String vhost, String srcQueue, String exchangeName) throws IOException {
        LOG.info("Creating shovel for queue {}", srcQueue);
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            String srcUri = String.format(SHOVEL_FORMAT, sourceUsername, sourcePassword, sourceHost, vhost);
            String destUri = String.format(SHOVEL_FORMAT, destUsername, destPassword, destHost, vhost);
            String shovelName = "shovel_" + srcQueue;

            // Check if the shovel already exists
            String checkUrl = String.format(SHOVEL_URL_FORMAT, sourceHost, RABBITMQ_MANAGEMENT_PORT, shovelName);
            HttpGet checkRequest = new HttpGet(checkUrl);
            HttpResponse checkResponse = httpClient.execute(checkRequest);
            int checkStatusCode = checkResponse.getStatusLine().getStatusCode();
            if (checkStatusCode == HttpStatus.SC_OK) {
                // Shovel already exists, no need to create it again
                return true;
            }
            JSONObject bindingJson = new JSONObject();
            bindingJson.put("src-uri", srcUri);
            bindingJson.put("src-queue", srcQueue);
            bindingJson.put("dest-uri", destUri);
            if (!exchangeName.isEmpty()) {
                bindingJson.put("dest-exchange", exchangeName);
            } else {
                bindingJson.put("dest-queue", srcQueue);
            }
            bindingJson.put("add-forward-headers", false);
            bindingJson.put("ack-mode", "on-confirm");
            bindingJson.put("delete-after", "never");
            JSONObject valueJson = new JSONObject();
            valueJson.put("value", bindingJson);
            String createUrl = String.format(SHOVEL_URL_FORMAT, sourceHost, RABBITMQ_MANAGEMENT_PORT, shovelName);
            HttpPut httpPut = new HttpPut(createUrl);
            httpPut.addHeader(CONTENT_TYPE, APPLICATION_JSON);
            httpPut.setEntity(new StringEntity(valueJson.toString()));
            HttpResponse response = httpClient.execute(httpPut);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return true;
            } else {
                LOG.error("Failed to create shovel. Status code: {}", statusCode);
            }
        }
        return false;
    }

    public boolean deleteAllShovels(String id) throws IOException {
        LOG.info("Deleting all shovels for stream {}", id);

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            String baseUrl = String.format("http://%s:%s/api/parameters/shovel/%%2F", sourceHost, RABBITMQ_MANAGEMENT_PORT);
            HttpGet getRequest = new HttpGet(baseUrl);
            HttpResponse getResponse = httpClient.execute(getRequest);
            if (getResponse.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                LOG.error("Failed to get shovels. Status code: {}", getResponse.getStatusLine().getStatusCode());
                return false;
            }
            HttpEntity entity = getResponse.getEntity();
            if (entity == null) {
                LOG.info("No shovels found, nothing to delete");
                return true;
            }
            String jsonString = EntityUtils.toString(entity);
            JSONArray jsonArray = new JSONArray(jsonString);
            jsonArray.forEach(shovelObj -> {
                JSONObject jsonObject = (JSONObject) shovelObj;
                String shovelName = jsonObject.getString("name");
                if (shovelName.endsWith(id + "-queue")) {
                    LOG.info("Deleting shovel {}", shovelName);
                    String deleteUrl = String.format(SHOVEL_URL_FORMAT, sourceHost, RABBITMQ_MANAGEMENT_PORT, shovelName);
                    HttpDelete deleteRequest = new HttpDelete(deleteUrl);
                    HttpResponse deleteResponse;
                    try {
                        deleteResponse = httpClient.execute(deleteRequest);
                    } catch (IOException e) {
                        LOG.error("Failed to execute delete request for shovel {}", shovelName, e);
                        return;
                    }
                    if (deleteResponse.getStatusLine().getStatusCode() != HttpStatus.SC_NO_CONTENT) {
                        LOG.error("Failed to delete shovel {}", shovelName);
                    }
                }
            });
        }
        return true;
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

    public Boolean createExchange(String vhost, String exchangeName) throws IOException {
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
                return true;
            }
            HttpPut request = new HttpPut(url);
            request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            JSONObject exchangeJson = new JSONObject();
            exchangeJson.put("type", "topic");
            request.setEntity(new StringEntity(exchangeJson.toString()));
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return true;
            } else {
                LOG.error("Failed to create exchange. Status code: {}", statusCode);
            }
        }
        return false;
    }

    public Boolean createBinding(String vhost,
                                 String exchangeName,
                                 String queueName,
                                 String routingKey,
                                 String destination) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            String url = HTTP + sourceHost + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/bindings/" + vhost + "/e/" + exchangeName + "/q/" + queueName;
            HttpPost request = new HttpPost(url);
            request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
            JSONObject bindingJson = new JSONObject();
            bindingJson.put("routing_key", routingKey);
            bindingJson.put("destination", destination);
            bindingJson.put("source", exchangeName);
            bindingJson.put("vhost", URLDecoder.decode(vhost, "UTF-8"));
            request.setEntity(new StringEntity(bindingJson.toString()));
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED) {
                return true;
            } else {
                LOG.error("Failed to create binding. Status code: {}", statusCode);
            }
        }
        return false;
    }

    public Boolean createQueue(String vhost, String queueName) throws IOException {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(sourceHost, RABBITMQ_MANAGEMENT_PORT), new UsernamePasswordCredentials(sourceUsername, sourcePassword));
        try (CloseableHttpClient httpClient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build()) {
            HttpPut request = getHttpPut(vhost, queueName);
            HttpResponse response = httpClient.execute(request);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode == HttpStatus.SC_CREATED || statusCode == HttpStatus.SC_NO_CONTENT) {
                return true;
            } else {
                LOG.error("Failed to create queue. Status code: {}", statusCode);
            }
        }
        return false;
    }

    @NotNull
    private HttpPut getHttpPut(String vhost, String queueName) throws UnsupportedEncodingException {
        String url = HTTP + sourceHost + ":" + RABBITMQ_MANAGEMENT_PORT + "/api/queues/" + vhost + "/" + queueName;
        HttpPut request = new HttpPut(url);
        request.setHeader(CONTENT_TYPE, APPLICATION_JSON);
        JSONObject queueJson = new JSONObject();
        queueJson.put("auto_delete", false);
        queueJson.put("durable", true);
        request.setEntity(new StringEntity(queueJson.toString()));
        return request;
    }

}
