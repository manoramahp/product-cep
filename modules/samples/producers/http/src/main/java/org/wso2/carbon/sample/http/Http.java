/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.sample.http;

import org.apache.http.client.HttpClient;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.SystemDefaultHttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.axiom.om.util.Base64;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Http client reads a text file with multiple xml messages and post it to the given url.
 */
public class Http {
	private static Logger log = Logger.getLogger(Http.class);
	private static List<String> messagesList = new ArrayList<String>();
	private static BufferedReader bufferedReader = null;
	private static StringBuffer message = new StringBuffer("");
	private static final String asterixLine = "*****";

	public static void main(String args[]) {

		System.out.println("Starting WSO2 Http Client");
		HttpUtil.setTrustStoreParams();
		String url = args[0];
		String username = args[1];
		String password = args[2];
		String sampleNumber = args[3];
		String filePath = args[4];

		HttpClient httpClient = new SystemDefaultHttpClient();
		try {
			HttpPost method = new HttpPost(url);
			filePath = HttpUtil.getMessageFilePath(sampleNumber, filePath, url);
			readMsg(filePath);

			for (String message : messagesList) {
				StringEntity entity = new StringEntity(message);
				System.out.println("Sending message:");
				System.out.println(message);
				System.out.println();
				method.setEntity(entity);
				if (url.startsWith("https")) {
					processAuthentication(method, username, password);
				}
				httpClient.execute(method).getEntity().getContent().close();
			}
			Thread.sleep(500); // Waiting time for the message to be sent

		} catch (Throwable t) {
			log.error("Error when sending the messages", t);
		}
	}

	/**
	 * Xml messages will be read from the given filepath and stored in the array list (messagesList)
	 *
	 * @param filePath Text file to be read
	 */
	private static void readMsg(String filePath) {

		try {

			String line;
			bufferedReader = new BufferedReader(new FileReader(filePath));
			while ((line = bufferedReader.readLine()) != null) {
				if ((line.equals(asterixLine.trim()) && !"".equals(message.toString().trim()))) {
					messagesList.add(message.toString());
					message = new StringBuffer("");
				} else {
					message = message.append(String.format("\n%s", line));
				}
			}
			if (!"".equals(message.toString().trim())) {
				messagesList.add(message.toString());
			}

		} catch (FileNotFoundException e) {
			log.error("Error in reading file " + filePath, e);
		} catch (IOException e) {
			log.error("Error in reading file " + filePath, e);
		} finally {
			try {
				if (bufferedReader != null) {
					bufferedReader.close();
				}
			} catch (IOException e) {
				log.error("Error occurred when closing the file : " + e.getMessage(), e);
			}
		}

	}

	private static void processAuthentication(HttpPost method, String username, String password) {
		if (username != null && username.trim().length() > 0) {
			method.setHeader("Authorization",
			                 "Basic " + Base64.encode((username + ":" + password).getBytes()));
		}
	}

}

