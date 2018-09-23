package com.github.sgdc3.downloadscraper;

import lombok.extern.flogger.Flogger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.File;
import java.io.IOException;

@Flogger
public final class DownloadScraper {

    public static void main(final String[] arguments) throws IOException {
        log.atInfo().log("Starting...");

        // Base arguments
        if (arguments.length < 2) {
            log.atSevere().log("Invalid arguments!");
            return;
        }
        final File destination = new File(arguments[0]);
        final String baseUrl = arguments[1];

        // Fetch result
        String url = baseUrl;
        if (arguments.length > 2) {
            // Handle commands
            int index = 2;
            do {
                log.atInfo().log("Following " + url);
                if (arguments.length - index < 2) {
                    throw new IllegalArgumentException("Premature end of command! Index " + index);
                }
                final String base = arguments[index++];
                final String argument = arguments[index++];
                url = handle(url, base, argument);
            } while (arguments.length - index != 0);

            // Handle relative urls
            if (!url.startsWith("http")) {
                url = baseUrl + url;
            }
        }
        log.atInfo().log("Download link: " + url);
        HttpDownloadUtils.downloadFile(url, destination);
        log.atInfo().log("Done! " + destination.getPath());
    }

    private static String handle(final String url, final String strategy, final String parameter) throws IOException {
        final Document page = Jsoup.connect(url).get();
        final Element element;
        switch (strategy) {
            case "followParent":
                element = page.selectFirst(parameter).parent();
                break;
            case "follow":
                element = page.selectFirst(parameter);
                break;
            case "firstMatch":
                element = page.getElementsMatchingOwnText(parameter).first();
                break;
            default:
                throw new IllegalArgumentException(strategy + " is not a valid strategy!");
        }
        if (element == null) {
            throw new IllegalArgumentException("Element not found! Strategy: " + strategy + " Parameter: " + parameter);
        }
        return element.attr("href");
    }
}
