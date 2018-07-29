package SimpleTanner;

import org.rspeer.runetek.api.commons.Time;
import org.rspeer.ui.Log;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class FetchHelper {
    static int fetchItemPrice(int itemId, int fallbackPrice) {
        try {
            String urlString = "https://api.rsbuddy.com/grandExchange?a=guidePrice&i=" + itemId;
            String data = FetchHelper.sendGET(urlString, 4);

            if (data == null) {
                throw new Exception("FAILED to fetch item price. The RSBuddy API is not responding." + urlString);
            }
            // overall price is the first property in the JSON response
            String overallPrice = data.substring(data.indexOf(":") + 1, data.indexOf(","));
            return Integer.valueOf(overallPrice);
        } catch (Exception e) {
            Log.severe(e.getMessage());
            return fallbackPrice;
        }
    }

    static Image getImage(String url){
        try {
            return ImageIO.read(new URL(url));
        } catch (IOException e){
            return null;
        }
    }

    static Font getRunescapeFont(String fallbackFontName) {
        try {
            ClassLoader cLoader = FetchHelper.class.getClassLoader();

            // for some reason, getResourceAsStream(...) throws an exception
            // if we dont create any temp file beforehand
            File tmp = File.createTempFile("getResourceAsStream_uses_temp_files", ".tmp");
            tmp.deleteOnExit();

            String fontpath = "runescape_uf.ttf";

            return Font.createFont(Font.TRUETYPE_FONT, cLoader.getResourceAsStream(fontpath));
        } catch (Exception e) {
            Log.severe(e);
            Log.severe("Failed to load essential font, please contact the developer");
            return new Font(fallbackFontName, Font.PLAIN, 24).deriveFont(24f); // sometimes the first size isnt used
        }
    }

    private static String sendGET(String getUrl, int retriesLeft) throws IOException {
        URL obj = new URL(getUrl);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        int responseCode = con.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) { // success
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            String inputLine;
            StringBuilder response = new StringBuilder();

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            return response.toString();
        } else {
            Log.info("GET request failed, retrying...");
            if (retriesLeft >= 1) {
                Time.sleep(200);
                return FetchHelper.sendGET(getUrl, retriesLeft - 1);
            }
            return null;
        }

    }
}
