/*
 * DocDoku, Professional Open Source
 * Copyright 2006 - 2013 DocDoku SARL
 *
 * This file is part of DocDokuPLM.
 *
 * DocDokuPLM is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * DocDokuPLM is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with DocDokuPLM.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.docdoku.server.storage.googlestorage;

import com.docdoku.core.services.FileNotFoundException;
import com.docdoku.core.services.StorageException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;
import java.util.logging.Logger;

/**
 * @author Asmae Chadid
 */
public class GoogleStorageCloud {

    private GoogleStorageCloud(){};

    private final static GoogleStorageProperties properties = new GoogleStorageProperties();
    private final static Logger LOGGER = Logger.getLogger(GoogleStorageCloud.class.getName());

    private static String getOAuthAuthorizationHeader() throws IOException {
        return new StringBuilder().append("Bearer ").append(getOAuthAccessToken()).toString();
    }

    /*
    TODO: do not ask for an access token each time
     */
    private static String getOAuthAccessToken() throws IOException {
        Oauth2TokenGetter oAuthPath = new Oauth2TokenGetter();
        return oAuthPath.getToken();
    }

    private static HttpURLConnection getHttpURLConnection(String pathToObject) throws IOException {
        URL url = new URL(properties.getURI() + pathToObject);
        HttpURLConnection httpConnection = (HttpURLConnection) url.openConnection();
        httpConnection.setRequestProperty("Authorization", getOAuthAuthorizationHeader());
        httpConnection.setRequestProperty("x-goog-api-version", properties.getApiVersion());
        httpConnection.setRequestProperty("x-goog-project-id", properties.getProjectId());
        return httpConnection;
    }

    private static HttpURLConnection getObjectRequest(String pathToObject) throws IOException {
        HttpURLConnection httpConnection = getHttpURLConnection(pathToObject);
        httpConnection.setRequestMethod("GET");
        return httpConnection;
    }

    private static HttpURLConnection putObjectRequest(String pathToObject) throws IOException {
        HttpURLConnection httpConnection = getHttpURLConnection(pathToObject);
        httpConnection.setRequestMethod("PUT");
        httpConnection.setRequestProperty("Transfer-Encoding", "Chunked");
        httpConnection.setDoOutput(true);
        return httpConnection;
    }

    /*
    Use Apache HttpClient to get a Content-Length 0 header when no data is send, required by Google Cloud Storage
     */
    private static HttpResponse copyObjectRequest(String sourcePath, String pathToObject) throws IOException {
        DefaultHttpClient httpClient = new DefaultHttpClient();
        HttpPut httpPut = new HttpPut(properties.getURI() + pathToObject);
        httpPut.addHeader("Authorization", getOAuthAuthorizationHeader());
        httpPut.addHeader("x-goog-api-version", properties.getApiVersion());
        httpPut.addHeader("x-goog-project-id", properties.getProjectId());
        httpPut.addHeader("x-goog-copy-source", properties.getBucketName() + "/" + sourcePath);
        return httpClient.execute(httpPut);
    }

    private static HttpURLConnection deleteObjectRequest(String pathToObject) throws IOException {
        HttpURLConnection httpConnection = getHttpURLConnection(pathToObject);
        httpConnection.setRequestMethod("DELETE");
        return httpConnection;
    }

    public static InputStream getInputStream(String pathToObject) throws StorageException, FileNotFoundException {
        try {
            HttpURLConnection geObjectRequest = getObjectRequest(pathToObject);
            int responseCode = geObjectRequest.getResponseCode();
            String message = geObjectRequest.getResponseMessage();
            if (responseCode == 200) {
                return geObjectRequest.getInputStream();
            } else if (responseCode == 404) {
                   throw new FileNotFoundException(new StringBuilder().append(pathToObject).append(" not found on Google Cloud Storage. ").append(message).toString());
            } else {
                throw new StorageException(new StringBuilder().append("Error getting ").append(pathToObject).append(" on Google Cloud Storage, server respond code ").append(responseCode).append(" ").append(message).toString());
            }
        } catch (IOException e) {
            throw new StorageException(new StringBuilder().append("Error getting ").append(pathToObject).append(" on Google Cloud Storage").toString(), e);
        }
    }

    public static OutputStream getOutputStream(final String pathToObject) throws StorageException {
        try {
            final HttpURLConnection putObjectRequest = putObjectRequest(pathToObject);
            OutputStream outputStream = putObjectRequest.getOutputStream();
            FilterOutputStream filterOutputStream = new FilterOutputStream(outputStream) {
                @Override
                public void close() throws IOException {
                    super.close();
                    int responseCode = putObjectRequest.getResponseCode();
                    String responseMessage = putObjectRequest.getResponseMessage();
                    LOGGER.info(new StringBuilder().append("Upload ").append(pathToObject).append(" on Google Cloud Storage.").append(" Response is ").append(responseCode).append(" ").append(responseMessage).toString());
                }
            };
            return filterOutputStream;
        } catch (IOException e) {
            throw new StorageException(new StringBuilder().append("Error uploading ").append(pathToObject).append(" on Google Cloud Storage").toString(), e);
        }
    }


    public static void copy(String sourcePath, String pathToObject) throws StorageException, FileNotFoundException {
        try {
            HttpResponse httpResponse = copyObjectRequest(sourcePath, pathToObject);
            int responseCode = httpResponse.getStatusLine().getStatusCode();
            String message = httpResponse.getStatusLine().getReasonPhrase();
            if (responseCode == 200) {
                LOGGER.info(new StringBuilder().append("Copy of ").append(sourcePath).append(" to ").append(pathToObject).append(" done on Google Cloud Storage").toString());
            } else if (responseCode == 404) {
                throw new FileNotFoundException(new StringBuilder().append(sourcePath).append(" not found on Google Cloud Storage. ").append(message).toString());
            } else {
                throw new StorageException(new StringBuilder().append("Error copying ").append(sourcePath).append(" to ").append(pathToObject).append(" on Google Cloud Storage, server respond code ").append(responseCode).append(" : ").append(message).toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new StorageException(new StringBuilder().append("Error copying ").append(sourcePath).append(" to ").append(pathToObject).append(" on Google Cloud Storage").toString(), e);
        }
    }

    public static void delete(String pathToObject) throws StorageException {
        try {
            final HttpURLConnection deleteObjectRequest = deleteObjectRequest(pathToObject);
            int responseCode = deleteObjectRequest.getResponseCode();
            if (responseCode == 204) {
                LOGGER.info(new StringBuilder().append("Deletion of ").append(pathToObject).append(" done on Google Cloud Storage").toString());
            } else {
                throw new StorageException(new StringBuilder().append("Error deleting ").append(pathToObject).append(" on Google Cloud Storage, server respond code ").append(responseCode).append(" : ").append(deleteObjectRequest.getResponseMessage()).toString());
            }
        } catch (IOException e) {
            throw new StorageException(new StringBuilder().append("Error deleting ").append(pathToObject).append(" on Google Cloud Storage").toString(), e);
        }
    }

}