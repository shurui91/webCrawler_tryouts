package sycrawler;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.security.*;


public class UrlInfo {
    public int statusCode;
    public String url;
    public int size;
    public String type;
    public ArrayList<String> outgoingUrls;
    public String hash;
    public String extension;

    public UrlInfo(String url, int statusCode) {
        this.url = url;
        this.statusCode = statusCode;
    }

    public UrlInfo(String url, String type) {
        this.url = url;
        this.type = type;
    }

    public UrlInfo(String url, int size, ArrayList<String> outgoingUrls, String type, String extenstion) {
        this.url = url;
        this.size = size;
        this.outgoingUrls = outgoingUrls;
        this.type = type;
        this.hash = hashString(url);
        this.extension = extenstion;
    }

    public static String hashString(String s) {
        byte[] hash = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            hash = md.digest(s.getBytes());

        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < hash.length; ++i) {
            String hex = Integer.toHexString(hash[i]);
            if (hex.length() == 1) {
                sb.append(0);
                sb.append(hex.charAt(hex.length() - 1));
            } else {
                sb.append(hex.substring(hex.length() - 2));
            }
        }
        return sb.toString();
    }
}