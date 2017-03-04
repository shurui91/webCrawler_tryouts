package sycrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;

public class App
{
    private final static String crawlStorageFolder = "tmp/sycrawler";
    private final static String crawlSeed = "http://abcnews.go.com/";
    private final static int maxPageToFetch = 200;
    private final static int maxDepthOfCrawling = 16;
    private final static int numberOfCrawlers = 10;
    private final static int maxDownloadedSize = 1024 * 1024 * 5;
    private final static String name = "Shurui Liu";
    private final static String id = "xxxxxxxx";
    private final static String newsSite = "abcnews.go.com";

    public static void main(String[] args) throws Exception {
        CrawlController crawlController = runCrawler();

        // get sum
        CrawlState sumState = new CrawlState();
        List<Object> crawlersLocalData = crawlController.getCrawlersLocalData();
        for (Object localData : crawlersLocalData) {
            CrawlState state = (CrawlState) localData;
            sumState.attemptUrls.addAll(state.attemptUrls);
            sumState.visitedUrls.addAll(state.visitedUrls);
            sumState.discoveredUrls.addAll(state.discoveredUrls);
        }

        saveFetchCsv(sumState);
        saveVisitCsv(sumState);
        saveUrlsCsv(sumState);
        savePageRankCsv(sumState);
        saveStatistics(sumState);
    }

    public static void saveFetchCsv(CrawlState sumState) throws Exception {
        String fileName = crawlStorageFolder + "/fetch_ABCNews.csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,Status\n");
        for (UrlInfo info : sumState.attemptUrls) {
            writer.append(info.url + "," + info.statusCode + "\n");
        }
        writer.flush();
        writer.close();
    }

    public static void saveVisitCsv(CrawlState sumState) throws Exception {
        String fileName = crawlStorageFolder + "/visit_ABCNews.csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,Size,OutLinks,ContentType\n");
        for (UrlInfo info : sumState.visitedUrls) {
            if (info.type != "unknown") {
                writer.append(info.url + "," + info.size + "," + info.outgoingUrls.size() + "," + info.type + "\n");
            }
        }
        writer.flush();
        writer.close();
    }

    public static void saveUrlsCsv(CrawlState sumState) throws Exception {
        String fileName = crawlStorageFolder + "/urls.csv";
        FileWriter writer = new FileWriter(fileName);
        writer.append("URL,Type\n");
        for (UrlInfo info : sumState.discoveredUrls) {
            writer.append(info.url + "," + info.type + "\n");
        }
        writer.flush();
        writer.close();
    }

    public static void savePageRankCsv(CrawlState sumState) throws Exception {
        String fileName = crawlStorageFolder + "/pagerank.csv";
        String mapFileName = crawlStorageFolder + "/mapping.csv";
        FileWriter writer = new FileWriter(fileName);
        FileWriter mapWriter = new FileWriter(mapFileName);
        String fileId;

        for (UrlInfo info : sumState.visitedUrls) {
            if (info.extension.equals("")) {
                continue;
            }
            fileId = App.crawlStorageFolder + "/files/" + info.hash + info.extension;
            mapWriter.append(fileId + "," + info.url + "\n");
            writer.append(fileId + ",");
            for (String outgoingUrl : info.outgoingUrls) {
                // generate outgoing url
                String[] segment = outgoingUrl.split(".");
                if (segment.length > 1 && segment[segment.length - 1].length() != 0) {
                    fileId = App.crawlStorageFolder + "/files/" + UrlInfo.hashString(outgoingUrl) + "." + segment[segment.length - 1];
                    mapWriter.append(fileId + "," + outgoingUrl + "\n");
                    writer.append(fileId + ",");
                } else {
                    fileId = App.crawlStorageFolder + "/files/" + UrlInfo.hashString(outgoingUrl) + ".html";
                    mapWriter.append(fileId + "," + outgoingUrl + "\n");
                    writer.append(fileId + ",");
                }
            }
            writer.append("\n");
        }

        writer.flush();
        writer.close();
    }

    public static void saveStatistics(CrawlState sumState) throws Exception {
        String fileName = crawlStorageFolder + "/CrawlReport_ABCNews.txt";
        FileWriter writer = new FileWriter(fileName);

        // Personal Info
        writer.append("Name: " + name + "\n");
        writer.append(System.lineSeparator());
        writer.append("USC ID: " + id + "\n");
        writer.append(System.lineSeparator());
        writer.append("News site crawled: " + newsSite + "\n");
        writer.append(System.lineSeparator());

        // Fetch Statistics
        writer.append(System.lineSeparator());
        writer.append("Fetch Statistics");
        writer.append(System.lineSeparator());
        writer.append("=====================");
        writer.append(System.lineSeparator());
        writer.append("fetches attempted: " + sumState.attemptUrls.size() + "\n");
        writer.append(System.lineSeparator());
        writer.append("fetched succeeded: " + sumState.visitedUrls.size() + "\n");
        writer.append(System.lineSeparator());

        // get failed url and aborted urls
        int failedUrlsCount = 0;
        int abortedUrlsCount = 0;
        for (UrlInfo info : sumState.attemptUrls) {
            if (info.statusCode >= 300 && info.statusCode < 400) {
                abortedUrlsCount++;
            } else if (info.statusCode != 200) {
                failedUrlsCount++;
            }
        }

        writer.append("fetched aborted: " + abortedUrlsCount + "\n");
        writer.append(System.lineSeparator());
        writer.append("fetched failed: " + failedUrlsCount + "\n");
        writer.append(System.lineSeparator());

        // Outgoing URLS
        HashSet<String> hashSet = new HashSet<String>();
        int uniqueUrls = 0;
        int abcUrls = 0;
        int uscUrls = 0;
        int outUrls = 0;
        writer.append(System.lineSeparator());
        writer.append("Outgoing URLs");
        writer.append(System.lineSeparator());
        writer.append("=====================");
        writer.append(System.lineSeparator());
        writer.append("Total URLS extracted: " + sumState.discoveredUrls.size() + "\n");
        writer.append(System.lineSeparator());
        for (UrlInfo info : sumState.discoveredUrls) {
            if (!hashSet.contains(info.url)) {
                hashSet.add(info.url);
                uniqueUrls++;
                if (info.type.equals("OK")) {
                    abcUrls++;
                } else if (info.type.equals("USC")) {
                    uscUrls++;
                } else {
                    outUrls++;
                }
            }
        }
        writer.append("# unique URLs extracted: " + uniqueUrls + "\n");
        writer.append(System.lineSeparator());
        writer.append("# unique URLs within News Site: " + abcUrls + "\n");
        writer.append(System.lineSeparator());
        // writer.append("# unique USC URLs outside News Site: " + uscUrls + "\n");
        // writer.append(System.lineSeparator());
        writer.append("# unique URLs outside News Site: " + outUrls + "\n");
        writer.append(System.lineSeparator());

        // Status Code
        writer.append(System.lineSeparator());
        writer.append("Status Codes");
        writer.append(System.lineSeparator());
        writer.append("=====================");
        writer.append(System.lineSeparator());
        HashMap<Integer, Integer> hashMap = new HashMap<Integer, Integer>();
        for (UrlInfo info : sumState.attemptUrls) {
            if (hashMap.containsKey(info.statusCode)) {
                hashMap.put(info.statusCode, hashMap.get(info.statusCode) + 1);
            } else {
                hashMap.put(info.statusCode, 1);
            }
        }
        HashMap<Integer, String> statusCodeMapping = new HashMap<Integer, String>();
        statusCodeMapping.put(200, "OK");
        statusCodeMapping.put(301, "Moved Permanently");
        statusCodeMapping.put(302, "Found");
        statusCodeMapping.put(401, "Unauthorized");
        statusCodeMapping.put(403, "Forbidden");
        statusCodeMapping.put(404, "Not Found");
        statusCodeMapping.put(405, "Method Not Allowed");
        statusCodeMapping.put(500, "Internal Server Error");

        for (Integer key : hashMap.keySet()) {
            writer.append("" + key + " " + statusCodeMapping.get(key) + ": " + hashMap.get(key));
            writer.append(System.lineSeparator());
        }

        // File Size
        writer.append(System.lineSeparator());
        writer.append("File Size");
        writer.append(System.lineSeparator());
        writer.append("=====================");
        writer.append(System.lineSeparator());
        int oneK = 0;
        int tenK = 0;
        int hundredK = 0;
        int oneM = 0;
        int other = 0;
        for (UrlInfo info : sumState.visitedUrls) {
            if (info.size < 1024) {
                oneK++;
            } else if (info.size < 10240) {
                tenK++;
            } else if (info.size < 102400) {
                hundredK++;
            } else if (info.size < 1024 * 1024) {
                oneM++;
            } else {
                other++;
            }
        }
        writer.append("< 1KB: " + oneK + "\n");
        writer.append(System.lineSeparator());
        writer.append("1KB ~ <10KB: " + tenK + "\n");
        writer.append(System.lineSeparator());
        writer.append("10KB ~ <100KB: " + hundredK + "\n");
        writer.append(System.lineSeparator());
        writer.append("100KB ~ <1MB: " + oneM + "\n");
        writer.append(System.lineSeparator());
        writer.append(">= 1MB: " + other + "\n");
        writer.append(System.lineSeparator());

        // Content Types
        HashMap<String, Integer> hashMap1 = new HashMap<String, Integer>();
        writer.append(System.lineSeparator());
        writer.append("Content Types");
        writer.append(System.lineSeparator());
        writer.append("=====================");
        writer.append(System.lineSeparator());
        for (UrlInfo info : sumState.visitedUrls) {
            if (info.type.equals("unknown")) {
                continue;
            }
            if (hashMap1.containsKey(info.type)) {
                hashMap1.put(info.type, hashMap1.get(info.type) + 1);
            } else {
                hashMap1.put(info.type, 1);
            }
        }
        for (String key : hashMap1.keySet()) {
            writer.append("" + key + ": " + hashMap1.get(key) + "\n");
        }
        writer.append(System.lineSeparator());

        writer.flush();
        writer.close();
    }

    public static CrawlController runCrawler() throws Exception {
        CrawlConfig config = new CrawlConfig();
        config.setCrawlStorageFolder(crawlStorageFolder);
        config.setMaxDepthOfCrawling(maxDepthOfCrawling);
        config.setMaxPagesToFetch(maxPageToFetch);
        config.setMaxDownloadSize(maxDownloadedSize);
        config.setIncludeBinaryContentInCrawling(true);

        // initialize
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);

        // add seed
        CrawlController crawlController = new CrawlController(config, pageFetcher, robotstxtServer);
        crawlController.addSeed(crawlSeed);

        // start crawling
        SyCrawler.configure(crawlStorageFolder + "/files");
        crawlController.start(sycrawler.SyCrawler.class, numberOfCrawlers);

        return crawlController;
    }
}