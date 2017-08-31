package com.xml.library.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

/**
 * Created by xlc on 2016/12/29.
 */
public class HttpUtil {

    public static final String BASE_URL = A.gd();

    public static final String BASE_DOWNLOAD_URL = A.ge();

    public static final String BASE_IMG_URL = A.gf();

    public static ExecutorService executorService = Executors.newScheduledThreadPool(20);

    public static String getRequest(final String url) throws Exception {
        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() {
                BufferedReader in = null;
                String result = "";
                try {
                    URL localurl = new URL(url);

                    URLConnection conn = localurl.openConnection();

                    conn.setConnectTimeout(15000);

                    conn.setRequestProperty("accept", "*/*");

                    conn.setRequestProperty("connection", "Keep-Alive");

                    conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");

                    conn.connect();

                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String line = "";

                    while ((line = in.readLine()) != null) {
                        result += line;
                    }

                    return result;
                } catch (Exception e) {

                    e.printStackTrace();

                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                return "null";
            }
        });
        executorService.submit(task);
        return task.get();
    }

    public static String postRequest(final String url, final String rawParams) throws Exception {

        FutureTask<String> task = new FutureTask<String>(new Callable<String>() {
            @Override
            public String call() {

                PrintWriter out = null;

                BufferedReader in = null;

                String result = "";

                try {

                    URL localurl = new URL(url);

                    URLConnection conn = localurl.openConnection();
                    conn.setConnectTimeout(1500);

                    conn.setRequestProperty("accept", "*/*");

                    conn.setRequestProperty("connection", "Keep-Alive");

                    conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible;MSIE 6.0;Windows NT 5.1;SV1)");

                    conn.setDoOutput(true);

                    conn.setDoInput(true);

                    out = new PrintWriter(conn.getOutputStream());

                    out.print(rawParams);

                    out.flush();

                    in = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    String line;

                    while ((line = in.readLine()) != null) {
                        result += line;
                    }

                    return result;

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (out != null) {
                            out.close();
                        }
                        if (in != null) {

                            in.close();
                        }
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                }
                return null;

            }
        });
        // new Thread(task).start();
        executorService.submit(task);

        return task.get();
    }
}
